package com.example.feature.share.engine

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.feature.share.domain.ShareClip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * Ends up with a small local file containing exactly the chosen window -
 * never downloads a 40-minute lecture to share one minute of it.
 *
 * This is a remux (MediaExtractor -> MediaMuxer), not a re-encode - except
 * MediaMuxer's MP4 container only accepts AAC for direct stream-copy. This
 * app's imported lectures are MP3 ("audio/mpeg"), so MP3 sources are copied
 * as raw elementary-stream bytes instead of muxed: no container, no re-encode,
 * and ExoPlayer / MediaCodec / Media3 Transformer all decode a standalone
 * .mp3 file directly.
 */
class AudioClipExtractor @Inject constructor(
    private val shareFileStore: ShareFileStore,
    private val okHttpClient: OkHttpClient,
) {

    suspend fun extractClip(
        id: String,
        remoteUrl: String,
        localFilePath: String?,
        startMs: Long,
        durationMs: Long,
    ): Result<ShareClip> = withContext(Dispatchers.IO) {
        val preferredSource = localFilePath?.takeIf { File(it).exists() } ?: remoteUrl

        runCatching {
            val (file, actualDurationMs) = remux(preferredSource, id, startMs, durationMs)
            ShareClip(file.absolutePath, startMs, actualDurationMs)
        }.recoverCatching {
            // MediaExtractor couldn't handle the source directly (some CDNs / codecs
            // reject range requests, or the local file was unreadable). Download the
            // whole remote file once, then remux from that local copy instead.
            val downloaded = downloadWhole(remoteUrl, id)
            try {
                val (file, actualDurationMs) = remux(downloaded.absolutePath, id, startMs, durationMs)
                ShareClip(file.absolutePath, startMs, actualDurationMs)
            } finally {
                downloaded.delete()
            }
        }
    }

    private fun downloadWhole(url: String, id: String): File {
        val tempFile = shareFileStore.downloadTempFile(id)
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Download failed: HTTP ${response.code}" }
            val body = checkNotNull(response.body) { "Empty response body" }
            body.byteStream().use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return tempFile
    }

    /**
     * Copies samples from [startMs] for up to [requestedDurationMs] into a new
     * local file. Returns the output file (extension depends on codec) and the
     * actual duration written, which may be shorter than requested if the
     * source track is shorter than startMs + window.
     */
    private fun remux(sourcePath: String, id: String, startMs: Long, requestedDurationMs: Long): Pair<File, Long> {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(sourcePath)

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val candidate = extractor.getTrackFormat(i)
                val mime = candidate.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    trackIndex = i
                    format = candidate
                    break
                }
            }
            require(trackIndex >= 0 && format != null) { "No audio track found in $sourcePath" }
            extractor.selectTrack(trackIndex)

            val mime = requireNotNull(format.getString(MediaFormat.KEY_MIME))

            val totalDurationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) {
                format.getLong(MediaFormat.KEY_DURATION)
            } else {
                Long.MAX_VALUE
            }

            val startUs = (startMs * 1_000L).coerceIn(0L, (totalDurationUs - 1_000L).coerceAtLeast(0L))
            val endUs = if (totalDurationUs == Long.MAX_VALUE) {
                startUs + requestedDurationMs * 1_000L
            } else {
                (startUs + requestedDurationMs * 1_000L).coerceAtMost(totalDurationUs)
            }

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            return if (mime == MediaFormat.MIMETYPE_AUDIO_AAC) {
                val outputFile = shareFileStore.clipFile(id, "m4a")
                val actualDurationMs = muxToM4a(extractor, format, outputFile, endUs)
                outputFile to actualDurationMs
            } else {
                val outputFile = shareFileStore.clipFile(id, "mp3")
                val actualDurationMs = copyRawSamples(extractor, outputFile, endUs)
                outputFile to actualDurationMs
            }
        } finally {
            extractor.release()
        }
    }

    private fun muxToM4a(extractor: MediaExtractor, format: MediaFormat, outputFile: File, endUs: Long): Long {
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrackIndex = muxer.addTrack(format)
        muxer.start()

        val buffer = ByteBuffer.allocate(1 shl 20)
        val bufferInfo = MediaCodec.BufferInfo()
        var baseTimeUs = -1L
        var lastRelativeTimeUs = 0L

        try {
            while (true) {
                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs < 0) break
                if (baseTimeUs < 0) baseTimeUs = sampleTimeUs
                if (sampleTimeUs > endUs) break

                buffer.clear()
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                val relativeTimeUs = sampleTimeUs - baseTimeUs
                // extractor.sampleFlags is in MediaExtractor.SAMPLE_FLAG_* space (SYNC=1,
                // ENCRYPTED=2, PARTIAL_FRAME=4), not MediaCodec.BUFFER_FLAG_* space
                // (KEY_FRAME=1, CODEC_CONFIG=2, END_OF_STREAM=4, ...) - passing it straight
                // through mismapped PARTIAL_FRAME samples onto BUFFER_FLAG_END_OF_STREAM,
                // which could make the muxer treat a mid-stream sample as the end of the track.
                val muxerFlags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    0
                }
                bufferInfo.set(0, sampleSize, relativeTimeUs, muxerFlags)
                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                lastRelativeTimeUs = relativeTimeUs
                extractor.advance()
            }
        } finally {
            muxer.stop()
            muxer.release()
        }

        return lastRelativeTimeUs / 1_000L
    }

    /** MP3 (and anything else self-framed) needs no container - just copy the compressed frames as-is. */
    private fun copyRawSamples(extractor: MediaExtractor, outputFile: File, endUs: Long): Long {
        val buffer = ByteBuffer.allocate(1 shl 20)
        val bytes = ByteArray(buffer.capacity())
        var baseTimeUs = -1L
        var lastRelativeTimeUs = 0L

        outputFile.outputStream().use { output ->
            while (true) {
                val sampleTimeUs = extractor.sampleTime
                if (sampleTimeUs < 0) break
                if (baseTimeUs < 0) baseTimeUs = sampleTimeUs
                if (sampleTimeUs > endUs) break

                buffer.clear()
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                buffer.get(bytes, 0, sampleSize)
                output.write(bytes, 0, sampleSize)

                lastRelativeTimeUs = sampleTimeUs - baseTimeUs
                extractor.advance()
            }
        }

        return lastRelativeTimeUs / 1_000L
    }
}

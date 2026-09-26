package com.example.feature.share.engine

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Decodes a clip to PCM and accumulates RMS amplitude into one bucket per
 * output video frame (30/sec), so [ShareVideoExporter]'s overlay can look up
 * a bucket directly from a frame index.
 *
 * If decoding fails for any reason, returns a deterministic synthetic
 * envelope instead - a slightly-off waveform beats a failed share.
 */
class WaveformAnalyzer @Inject constructor() {

    suspend fun analyze(clipFilePath: String, durationMs: Long): FloatArray = withContext(Dispatchers.Default) {
        decodeWithWatchdog(ANALYZE_TIMEOUT_MS) { extractor -> decodeToEnvelope(extractor, clipFilePath, durationMs) }
            ?: syntheticEnvelope(bucketCountFor(durationMs))
    }

    /**
     * A cheap whole-track overview for the trim timeline: seeks to [pointCount]
     * evenly-spaced points and decodes only a short burst at each, instead of
     * decoding the entire (possibly 40-minute) track. Works against a remote
     * URL too - MediaExtractor issues HTTP range requests per seek.
     */
    suspend fun analyzeTrackOverview(
        sourcePath: String,
        totalDurationMs: Long,
        pointCount: Int = OVERVIEW_POINT_COUNT,
    ): FloatArray = withContext(Dispatchers.IO) {
        decodeWithWatchdog(OVERVIEW_TIMEOUT_MS) { extractor ->
            decodeSparseOverview(extractor, sourcePath, totalDurationMs, pointCount)
        } ?: syntheticEnvelope(pointCount)
    }

    /**
     * MediaExtractor/MediaCodec calls are blocking native calls with no coroutine
     * cancellation hook - a stalled network read (e.g. a CDN that stalls mid
     * range-request) can otherwise block the caller forever, which is why the
     * waveform would never appear rather than falling back to the synthetic one.
     * A watchdog coroutine forces the extractor closed after [timeoutMs], which
     * makes the blocked native call throw and unblocks the decode thread.
     */
    private suspend fun <T> decodeWithWatchdog(timeoutMs: Long, decode: (MediaExtractor) -> T): T? = coroutineScope {
        val extractor = MediaExtractor()
        val watchdog = launch {
            delay(timeoutMs)
            runCatching { extractor.release() }
        }
        try {
            runCatching { decode(extractor) }.getOrNull()
        } finally {
            watchdog.cancel()
        }
    }

    private fun decodeSparseOverview(
        extractor: MediaExtractor,
        sourcePath: String,
        totalDurationMs: Long,
        pointCount: Int,
    ): FloatArray {
        var codec: MediaCodec? = null
        val raw = FloatArray(pointCount)
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
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            var pcm = PcmFormat.from(format)
            val bufferInfo = MediaCodec.BufferInfo()
            val totalUs = totalDurationMs * 1_000L

            for (point in 0 until pointCount) {
                val seekUs = (point.toDouble() / pointCount * totalUs).toLong()
                extractor.seekTo(seekUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                codec.flush()

                var sumSquares = 0.0
                var sampleCount = 0
                var decodedPackets = 0
                var sawInputEos = false

                while (decodedPackets < PACKETS_PER_POINT) {
                    if (!sawInputEos) {
                        val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                        if (inputIndex >= 0) {
                            val inputBuffer = requireNotNull(codec.getInputBuffer(inputIndex))
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                sawInputEos = true
                            } else {
                                codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }

                    val outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                    if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        pcm = PcmFormat.from(codec.outputFormat)
                    } else if (outputIndex >= 0) {
                        if (bufferInfo.size > 0) {
                            val outputBuffer = requireNotNull(codec.getOutputBuffer(outputIndex)).order(ByteOrder.LITTLE_ENDIAN)
                            val n = bufferInfo.size / pcm.bytesPerSample
                            for (j in 0 until n) {
                                val s = pcm.sampleAt(outputBuffer, bufferInfo.offset + j * pcm.bytesPerSample)
                                sumSquares += s * s
                            }
                            sampleCount += n
                            decodedPackets++
                        }
                        codec.releaseOutputBuffer(outputIndex, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                    } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER && sawInputEos) {
                        break
                    }
                }

                raw[point] = if (sampleCount > 0) sqrt(sumSquares / sampleCount).toFloat() else 0f
            }
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }

        return normalize(raw)
    }

    private fun decodeToEnvelope(extractor: MediaExtractor, clipFilePath: String, durationMs: Long): FloatArray {
        val bucketCount = bucketCountFor(durationMs)
        val sumSquares = DoubleArray(bucketCount)
        val counts = IntArray(bucketCount)

        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(clipFilePath)

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
            require(trackIndex >= 0 && format != null) { "No audio track found in $clipFilePath" }
            extractor.selectTrack(trackIndex)

            val mime = requireNotNull(format.getString(MediaFormat.KEY_MIME))
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            var pcm = PcmFormat.from(format)
            // AudioClipExtractor rebases clip timestamps to 0, but don't rely on it:
            // measure every sample from the first decoded one, so the envelope always
            // spans exactly the clip wherever it sat in the source track.
            var firstPtsUs = -1L

            val bufferInfo = MediaCodec.BufferInfo()
            var sawInputEos = false
            var sawOutputEos = false

            while (!sawOutputEos) {
                if (!sawInputEos) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = requireNotNull(codec.getInputBuffer(inputIndex))
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEos = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    pcm = PcmFormat.from(codec.outputFormat)
                } else if (outputIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        if (firstPtsUs < 0) firstPtsUs = bufferInfo.presentationTimeUs
                        val outputBuffer = requireNotNull(codec.getOutputBuffer(outputIndex))
                        accumulate(outputBuffer, bufferInfo, bufferInfo.presentationTimeUs - firstPtsUs, pcm, sumSquares, counts)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEos = true
                    }
                }
            }
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }

        val raw = FloatArray(bucketCount) { i -> if (counts[i] > 0) sqrt(sumSquares[i] / counts[i]).toFloat() else 0f }
        return smooth(normalize(raw))
    }

    /** Adds each sample to the bucket its own timestamp falls in - not a whole
     * decoder buffer (often longer than one 33 ms bucket) to a single rounded bucket. */
    private fun accumulate(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
        relativePtsUs: Long,
        pcm: PcmFormat,
        sumSquares: DoubleArray,
        counts: IntArray,
    ) {
        val ordered = outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val sampleCount = bufferInfo.size / pcm.bytesPerSample
        val bucketsPerSample = BUCKETS_PER_SECOND.toDouble() / (pcm.sampleRate * pcm.channels)
        val startBucket = relativePtsUs / 1_000_000.0 * BUCKETS_PER_SECOND
        val lastBucket = sumSquares.size - 1

        for (j in 0 until sampleCount) {
            val s = pcm.sampleAt(ordered, bufferInfo.offset + j * pcm.bytesPerSample)
            val bucket = (startBucket + j * bucketsPerSample).toInt().coerceIn(0, lastBucket)
            sumSquares[bucket] += s * s
            counts[bucket]++
        }
    }

    /**
     * Per-clip normalisation against the 95th percentile, not the single loudest
     * bucket: one cough or mic bump used to set the scale and flatten every other
     * bar toward zero. The floors under the reference keep a near-silent clip from
     * being blown up into full-height noise.
     */
    private fun normalize(raw: FloatArray): FloatArray {
        if (raw.isEmpty()) return raw
        val sorted = raw.sorted()
        val peak = sorted.last()
        val p95 = sorted[((sorted.size - 1) * 0.95).roundToInt()]
        val reference = maxOf(p95, peak * PEAK_FRACTION_FLOOR, ABSOLUTE_FLOOR)
        return FloatArray(raw.size) { i -> (raw[i] / reference).coerceIn(0f, 1f).toDouble().pow(GAMMA).toFloat() }
    }

    /** Decoder output layout. Most decoders emit 16-bit PCM, but some emit float
     * PCM - reading that as shorts would produce a garbage envelope. */
    private class PcmFormat(val sampleRate: Int, val channels: Int, val isFloat: Boolean) {
        val bytesPerSample: Int get() = if (isFloat) 4 else 2

        fun sampleAt(buffer: ByteBuffer, byteIndex: Int): Double =
            if (isFloat) buffer.getFloat(byteIndex).toDouble() else buffer.getShort(byteIndex) / 32768.0

        companion object {
            fun from(format: MediaFormat): PcmFormat = PcmFormat(
                sampleRate = format.intOr(MediaFormat.KEY_SAMPLE_RATE, 44_100).coerceAtLeast(1),
                channels = format.intOr(MediaFormat.KEY_CHANNEL_COUNT, 1).coerceAtLeast(1),
                isFloat = format.intOr(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT) ==
                    AudioFormat.ENCODING_PCM_FLOAT,
            )

            private fun MediaFormat.intOr(key: String, default: Int): Int =
                if (containsKey(key)) getInteger(key) else default
        }
    }

    /** 3-tap smoothing so the waveform animation doesn't strobe frame to frame. */
    private fun smooth(values: FloatArray): FloatArray {
        if (values.size < 3) return values
        return FloatArray(values.size) { i ->
            val prev = values[(i - 1).coerceAtLeast(0)]
            val curr = values[i]
            val next = values[(i + 1).coerceAtMost(values.size - 1)]
            (prev + curr + next) / 3f
        }
    }

    /** A deterministic, natural-looking placeholder waveform for an instant first
     * paint before any real decode has run - the same shape this analyzer falls
     * back to on a decode failure/timeout, exposed so the UI can seed its initial
     * state with it instead of an empty/flat bar. */
    fun placeholderOverview(pointCount: Int = OVERVIEW_POINT_COUNT): FloatArray = syntheticEnvelope(pointCount)

    private fun syntheticEnvelope(bucketCount: Int): FloatArray {
        val random = Random(SEED)
        var value = 0.5f
        return FloatArray(bucketCount) {
            value = (value + (random.nextFloat() - 0.5f) * 0.3f).coerceIn(0.1f, 1f)
            value
        }
    }

    private fun bucketCountFor(durationMs: Long): Int =
        ((durationMs / 1000.0) * BUCKETS_PER_SECOND).roundToInt().coerceAtLeast(1)

    companion object {
        private const val BUCKETS_PER_SECOND = 30
        private const val GAMMA = 0.6
        /** The normalisation reference never drops below this share of the loudest bucket... */
        private const val PEAK_FRACTION_FLOOR = 0.3f
        /** ...nor below this absolute RMS, so a silent clip doesn't normalise noise to full height. */
        private const val ABSOLUTE_FLOOR = 0.003f
        private const val SEED = 42L
        private const val TIMEOUT_US = 10_000L
        private const val OVERVIEW_POINT_COUNT = 60
        private const val PACKETS_PER_POINT = 2
        private const val OVERVIEW_TIMEOUT_MS = 10_000L
        private const val ANALYZE_TIMEOUT_MS = 20_000L
    }
}

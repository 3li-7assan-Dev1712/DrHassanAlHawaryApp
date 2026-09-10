package com.example.feature.share.engine

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                    if (outputIndex >= 0) {
                        if (bufferInfo.size > 0) {
                            val outputBuffer = requireNotNull(codec.getOutputBuffer(outputIndex))
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            val shortBuffer = outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                            val n = shortBuffer.remaining()
                            for (j in 0 until n) {
                                val s = shortBuffer.get(j) / 32768.0
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

        val peak = raw.maxOrNull()?.takeIf { it > 0f } ?: 1f
        return FloatArray(pointCount) { i -> (raw[i] / peak).toDouble().pow(GAMMA).toFloat().coerceIn(0f, 1f) }
    }

    private fun decodeToEnvelope(extractor: MediaExtractor, clipFilePath: String, durationMs: Long): FloatArray {
        val bucketCount = bucketCountFor(durationMs)
        val sums = DoubleArray(bucketCount)
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
                if (outputIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        val outputBuffer = requireNotNull(codec.getOutputBuffer(outputIndex))
                        accumulateRms(outputBuffer, bufferInfo, durationMs, bucketCount, sums, counts)
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

        val raw = FloatArray(bucketCount) { i -> if (counts[i] > 0) (sums[i] / counts[i]).toFloat() else 0f }
        val peak = raw.maxOrNull()?.takeIf { it > 0f } ?: 1f
        val normalized = FloatArray(bucketCount) { i ->
            (raw[i] / peak).toDouble().pow(GAMMA).toFloat().coerceIn(0f, 1f)
        }
        return smooth(normalized)
    }

    private fun accumulateRms(
        outputBuffer: java.nio.ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
        durationMs: Long,
        bucketCount: Int,
        sums: DoubleArray,
        counts: IntArray,
    ) {
        val bucket = ((bufferInfo.presentationTimeUs / 1000.0 / durationMs) * bucketCount)
            .roundToInt()
            .coerceIn(0, bucketCount - 1)

        outputBuffer.position(bufferInfo.offset)
        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
        val shortBuffer = outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

        var sumSquares = 0.0
        val sampleCount = shortBuffer.remaining()
        for (j in 0 until sampleCount) {
            val s = shortBuffer.get(j) / 32768.0
            sumSquares += s * s
        }
        if (sampleCount > 0) {
            sums[bucket] += sqrt(sumSquares / sampleCount)
            counts[bucket] += 1
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
        private const val SEED = 42L
        private const val TIMEOUT_US = 10_000L
        private const val OVERVIEW_POINT_COUNT = 60
        private const val PACKETS_PER_POINT = 2
        private const val OVERVIEW_TIMEOUT_MS = 10_000L
        private const val ANALYZE_TIMEOUT_MS = 20_000L
    }
}

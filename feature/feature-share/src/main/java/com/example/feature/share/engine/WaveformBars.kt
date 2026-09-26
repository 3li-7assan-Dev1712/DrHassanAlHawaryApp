package com.example.feature.share.engine

/**
 * Turns an amplitude envelope (any length, 0..1) into the fixed number of bars
 * the frame shows. Both the preview and the export call this on the SAME
 * envelope, so they show the same bars.
 */
object WaveformBars {

    /** Bars never drop below this once stretched - quiet speech still reads as speech. */
    private const val FLOOR = 0.2f

    fun fromEnvelope(envelope: FloatArray, count: Int = ShareFrameLayout.BAR_COUNT): FloatArray {
        if (envelope.isEmpty() || count <= 0) return FloatArray(count.coerceAtLeast(0))

        val means = FloatArray(count) { i ->
            val start = (i.toLong() * envelope.size / count).toInt()
            val end = ((i + 1).toLong() * envelope.size / count).toInt().coerceIn(start + 1, envelope.size)
            var sum = 0f
            for (j in start until end) sum += envelope[j]
            sum / (end - start)
        }

        // Averaging ~50 buckets per bar flattens speech toward one height; stretch the
        // bars' own range back over FLOOR..1 so the shape stays readable. True silence
        // (everything ~0) stays at zero and is drawn as minimum-height bars.
        val min = means.min()
        val max = means.max()
        if (max < 1e-3f) return FloatArray(count)
        if (max - min < 1e-3f) return FloatArray(count) { 0.6f }
        return FloatArray(count) { i -> FLOOR + (1f - FLOOR) * (means[i] - min) / (max - min) }
    }
}

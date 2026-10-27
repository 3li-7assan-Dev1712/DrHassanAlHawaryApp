package com.example.domain.module

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackProgressTest {

    private val minute = 60_000L

    private fun progress(positionMs: Long, durationMs: Long) =
        PlaybackProgress(audioUrl = "url", title = "t", positionMs = positionMs, durationMs = durationMs, updatedAt = 0L)

    @Test
    fun `a long lecture is finished in its last thirty seconds`() {
        val hour = 60 * minute
        assertFalse(progress(hour - 31_000, hour).isFinished)
        assertTrue(progress(hour - 30_000, hour).isFinished)
        assertTrue(progress(hour, hour).isFinished)
    }

    @Test
    fun `a short fatwa is finished in its last five percent`() {
        val threeMinutes = 3 * minute
        assertFalse(progress(threeMinutes - 10_000, threeMinutes).isFinished)
        assertTrue(progress(threeMinutes - 9_000, threeMinutes).isFinished)
    }

    @Test
    fun `the first seconds are not progress`() {
        assertFalse(progress(4_999, 30 * minute).isInProgress)
        assertTrue(progress(5_000, 30 * minute).isInProgress)
    }

    @Test
    fun `resumes where it was left only while in progress`() {
        assertEquals(12 * minute, progress(12 * minute, 30 * minute).resumePositionMs)
        assertEquals(0L, progress(3_000, 30 * minute).resumePositionMs)
        assertEquals(0L, progress(30 * minute, 30 * minute).resumePositionMs)
    }

    @Test
    fun `an unknown duration is never finished and has no fraction`() {
        val unknown = progress(10 * minute, 0L)
        assertFalse(unknown.isFinished)
        assertTrue(unknown.isInProgress)
        assertEquals(0f, unknown.fraction)
        assertEquals(0L, unknown.remainingMs)
    }

    @Test
    fun `fraction and time left`() {
        val half = progress(15 * minute, 30 * minute)
        assertEquals(0.5f, half.fraction)
        assertEquals(15 * minute, half.remainingMs)
    }
}

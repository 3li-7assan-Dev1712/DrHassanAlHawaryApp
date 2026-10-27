package com.example.domain.module

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesProgressTest {

    private val minute = 60_000L
    private val lessons = listOf("l1", "l2", "l3", "l4")

    private fun heard(url: String, positionMs: Long, updatedAt: Long, durationMs: Long = 30 * minute) =
        url to PlaybackProgress(url, url, positionMs, durationMs, updatedAt)

    private fun finished(url: String, updatedAt: Long) = heard(url, 30 * minute, updatedAt)

    @Test
    fun `nothing heard plays from the start`() {
        val progress = SeriesProgress.of(lessons, emptyMap())
        assertEquals(SeriesProgress.NotStarted, progress)
        assertFalse(progress.isStarted)
    }

    @Test
    fun `resumes the lesson heard last while it is in progress`() {
        val progress = SeriesProgress.of(
            lessons,
            mapOf(finished("l1", updatedAt = 1), finished("l2", updatedAt = 2), heard("l3", 10 * minute, updatedAt = 3)),
        )
        assertEquals(SeriesProgress(completed = 2, resumeIndex = 2), progress)
        assertTrue(progress.isStarted)
    }

    @Test
    fun `after a finished lesson, resumes the next one`() {
        val progress = SeriesProgress.of(lessons, mapOf(heard("l3", 10 * minute, updatedAt = 1), finished("l1", updatedAt = 2)))
        assertEquals(SeriesProgress(completed = 1, resumeIndex = 1), progress)
    }

    @Test
    fun `after the last lesson is finished there is nothing to resume`() {
        val progress = SeriesProgress.of(lessons, mapOf(finished("l4", updatedAt = 1)))
        assertEquals(SeriesProgress(completed = 1, resumeIndex = null), progress)
        assertTrue(progress.isStarted)
    }

    @Test
    fun `a lesson only glanced at is ignored`() {
        val progress = SeriesProgress.of(lessons, mapOf(heard("l2", 10 * minute, updatedAt = 1), heard("l4", 2_000, updatedAt = 2)))
        assertEquals(SeriesProgress(completed = 0, resumeIndex = 1), progress)
    }

    @Test
    fun `positions of audios outside the series are ignored`() {
        val progress = SeriesProgress.of(lessons, mapOf(heard("other", 10 * minute, updatedAt = 1)))
        assertEquals(SeriesProgress.NotStarted, progress)
    }
}

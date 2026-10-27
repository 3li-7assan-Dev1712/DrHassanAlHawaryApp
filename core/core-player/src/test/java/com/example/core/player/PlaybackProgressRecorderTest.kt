package com.example.core.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackProgressRecorderTest {

    private val saved = mutableListOf<AudioPosition>()
    private val recorder = PlaybackProgressRecorder { saved += it }

    private fun lecture(positionMs: Long, url: String = "lecture") = AudioPosition(url, "title", positionMs, 3_600_000L)

    @Test
    fun `an audio loaded but never played is not saved`() {
        recorder.onItemChanged(lecture(0))
        recorder.onSettled(lecture(0))
        recorder.onItemChanged(lecture(0, url = "next"))

        assertEquals(emptyList<AudioPosition>(), saved)
    }

    @Test
    fun `pausing saves where it was paused`() {
        recorder.onItemChanged(lecture(60_000))
        recorder.onPlaying(lecture(60_000))
        recorder.onSettled(lecture(75_000))

        assertEquals(listOf(lecture(75_000)), saved)
    }

    @Test
    fun `while playing, saves every interval`() {
        recorder.onItemChanged(lecture(0))
        recorder.onPlaying(lecture(0))
        repeat(9) { recorder.onTick(lecture((it + 1) * 1_000L), 1_000L) }
        assertEquals(emptyList<AudioPosition>(), saved)

        recorder.onTick(lecture(10_000), 1_000L)
        assertEquals(listOf(lecture(10_000)), saved)
    }

    @Test
    fun `a replaced audio is saved where it was last read`() {
        recorder.onItemChanged(lecture(0))
        recorder.onPlaying(lecture(0))
        recorder.onTick(lecture(42_000), 1_000L)
        recorder.onItemChanged(lecture(0, url = "next"))
        // The new one has not played: stopping the old playback saves nothing for it.
        recorder.onSettled(lecture(0, url = "next"))

        assertEquals(listOf(lecture(42_000)), saved)
    }

    @Test
    fun `the same audio swapped for its downloaded file keeps being saved`() {
        recorder.onItemChanged(lecture(0))
        recorder.onPlaying(lecture(0))
        recorder.onItemChanged(lecture(30_000))
        recorder.onSettled(lecture(31_000))

        assertEquals(listOf(lecture(31_000)), saved)
    }

    @Test
    fun `an ended audio is saved finished and not overwritten when it is rewound unplayed`() {
        recorder.onItemChanged(lecture(0))
        recorder.onPlaying(lecture(3_590_000))
        recorder.onEnded(lecture(3_600_000))
        recorder.onSettled(lecture(3_600_000))
        // A fresh visit to the finished audio rewinds it without playing.
        recorder.onSettled(lecture(0))

        assertEquals(listOf(lecture(3_600_000)), saved)
    }

    @Test
    fun `items that are not resumable are never saved`() {
        recorder.onItemChanged(null)
        recorder.onPlaying(null)
        recorder.onTick(null, 10_000L)
        recorder.onSettled(null)

        assertEquals(emptyList<AudioPosition>(), saved)
    }
}

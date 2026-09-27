package com.example.domain.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipWindowTest {

    private val hour = 3_600_000L
    private val w = ClipWindow(60_000, 120_000) // 1:00 - 2:00

    @Test
    fun `moving keeps the length and stops at both ends`() {
        assertEquals(ClipWindow(65_000, 125_000), w.movedBy(5_000, hour))
        assertEquals(ClipWindow(0, 60_000), w.movedBy(-500_000, hour))
        assertEquals(ClipWindow(hour - 60_000, hour), w.movedBy(10 * hour, hour))
    }

    @Test
    fun `centring puts the clip around a tapped point`() {
        assertEquals(ClipWindow(2_370_000, 2_430_000), w.centeredAt(2_400_000, hour))
        assertEquals(ClipWindow(0, 60_000), w.centeredAt(1_000, hour))
        assertEquals(ClipWindow(hour - 60_000, hour), w.centeredAt(hour, hour))
    }

    @Test
    fun `edges resize freely but never below 5 s or outside the track`() {
        assertEquals(ClipWindow(30_000, 120_000), w.withStart(30_000, hour))
        assertEquals(ClipWindow(115_000, 120_000), w.withStart(119_000, hour))
        assertEquals(ClipWindow(0, 120_000), w.withStart(-9_000, hour))
        assertEquals(ClipWindow(60_000, 400_000), w.withEnd(400_000, hour))
        assertEquals(ClipWindow(60_000, 65_000), w.withEnd(61_000, hour))
        assertEquals(ClipWindow(60_000, hour), w.withEnd(hour * 2, hour))
    }

    @Test
    fun `edge cases that used to throw now clamp`() {
        // Start handle with the clip at the very beginning.
        assertEquals(ClipWindow(0, 5_000), ClipWindow(0, 5_000).withStart(3_000, hour))
        // End handle at the very end of the track.
        assertEquals(ClipWindow(hour - 5_000, hour), ClipWindow(hour - 5_000, hour).withEnd(hour - 2_000, hour))
        // A track shorter than the minimum: the clip is the whole track.
        assertEquals(ClipWindow(0, 3_000), ClipWindow.startingAt(1_000, 60_000, 3_000))
        assertEquals(ClipWindow(0, 3_000), ClipWindow(0, 3_000).withEnd(1_000, 3_000))
        assertEquals(ClipWindow(0, 3_000), ClipWindow(0, 3_000).withStart(2_000, 3_000))
    }

    @Test
    fun `length changes keep the start unless it would run past the end`() {
        assertEquals(ClipWindow(60_000, 105_000), w.withLength(45_000, hour))
        assertEquals(ClipWindow(hour - 90_000, hour), ClipWindow(hour - 30_000, hour).withLength(90_000, hour))
        assertEquals(ClipWindow(60_000, 65_000), w.withLength(1_000, hour))
    }

    @Test
    fun `the fine-tune view shows the clip with room on both sides`() {
        val view = ClipView.around(w, hour)
        assertEquals(120_000, view.lengthMs)
        assertEquals(30_000, view.startMs)
        assertTrue(view.fits(w, hour))
        // A clip dragged to the view's edge no longer fits: re-centre after the drag.
        assertFalse(view.fits(ClipWindow(100_000, 150_000), hour))
        // Near the track's start the view is pinned at 0 and still fits.
        val early = ClipWindow(0, 30_000)
        assertTrue(ClipView.around(early, hour).fits(early, hour))
        // Short track: the view is the whole track.
        assertEquals(ClipView(0, 20_000), ClipView.around(ClipWindow(0, 15_000), 20_000))
        assertTrue(ClipView(0, 20_000).fits(ClipWindow(0, 15_000), 20_000))
    }
}

package com.example.core.ui

import com.example.core.ui.theme.SharedAxis
import org.junit.Assert.assertEquals
import org.junit.Test

class SharedAxisTest {

    @Test
    fun `forward in RTL enters from the left and leaves to the right`() {
        // END is the left edge in Arabic: the new screen starts at a negative x offset...
        assertEquals(-1, SharedAxis.enterSign(forward = true, isRtl = true))
        // ...and the old one moves toward START (the right edge).
        assertEquals(1, SharedAxis.exitSign(forward = true, isRtl = true))
    }

    @Test
    fun `back in RTL mirrors forward`() {
        assertEquals(1, SharedAxis.enterSign(forward = false, isRtl = true))
        assertEquals(-1, SharedAxis.exitSign(forward = false, isRtl = true))
    }

    @Test
    fun `forward in LTR enters from the right and leaves to the left`() {
        assertEquals(1, SharedAxis.enterSign(forward = true, isRtl = false))
        assertEquals(-1, SharedAxis.exitSign(forward = true, isRtl = false))
    }

    @Test
    fun `back in LTR mirrors forward`() {
        assertEquals(-1, SharedAxis.enterSign(forward = false, isRtl = false))
        assertEquals(1, SharedAxis.exitSign(forward = false, isRtl = false))
    }
}

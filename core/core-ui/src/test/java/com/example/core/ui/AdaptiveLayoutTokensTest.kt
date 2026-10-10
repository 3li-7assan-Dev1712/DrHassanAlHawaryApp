package com.example.core.ui

import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.core.ui.theme.WindowClass
import com.example.core.ui.theme.toWindowClass
import com.example.core.ui.theme.windowClassForWidth
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveLayoutTokensTest {

    @Test
    fun breakpointsMatchMaterialWithoutLargeAndExtraLarge() {
        assertEquals(WindowClass.Compact, windowClassForWidth(360f))
        assertEquals(WindowClass.Compact, windowClassForWidth(599.9f))
        assertEquals(WindowClass.Medium, windowClassForWidth(600f))
        assertEquals(WindowClass.Medium, windowClassForWidth(800f))
        assertEquals(WindowClass.Medium, windowClassForWidth(839.9f))
        assertEquals(WindowClass.Expanded, windowClassForWidth(840f))
        // Google's Large (1200+) and Extra-large (1600+) use the Expanded layout.
        assertEquals(WindowClass.Expanded, windowClassForWidth(1280f))
        assertEquals(WindowClass.Expanded, windowClassForWidth(1920f))
    }

    @Test
    fun windowSizeClassesMapToTheDesignClasses() {
        assertEquals(WindowClass.Compact, WindowSizeClass(411, 891).toWindowClass())
        assertEquals(WindowClass.Medium, WindowSizeClass(600, 960).toWindowClass())
        assertEquals(WindowClass.Medium, WindowSizeClass(800, 1280).toWindowClass())
        assertEquals(WindowClass.Expanded, WindowSizeClass(840, 480).toWindowClass())
        // Large and Extra-large (if supportLargeAndXLargeWidth is ever enabled) are Expanded.
        assertEquals(WindowClass.Expanded, WindowSizeClass(WIDTH_DP_LARGE_LOWER_BOUND, 800).toWindowClass())
        assertEquals(WindowClass.Expanded, WindowSizeClass(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND, 900).toWindowClass())
    }

    @Test
    fun tokensMatchTheFigmaLayoutCollection() {
        with(AdaptiveLayoutTokens.Compact) {
            assertEquals(4, columns)
            assertEquals(16.dp, margin)
            assertEquals(16.dp, gutter)
            assertEquals(0.dp, navRailWidth)
            assertEquals(360.dp, listPaneWidth)
            assertEquals(0.dp, paneGap)
            assertEquals(328.dp, readingMaxWidth)
        }
        with(AdaptiveLayoutTokens.Medium) {
            assertEquals(8, columns)
            assertEquals(24.dp, margin)
            assertEquals(24.dp, gutter)
            assertEquals(80.dp, navRailWidth)
            assertEquals(360.dp, listPaneWidth)
            assertEquals(24.dp, paneGap)
            assertEquals(640.dp, readingMaxWidth)
        }
        with(AdaptiveLayoutTokens.Expanded) {
            assertEquals(12, columns)
            assertEquals(32.dp, margin)
            assertEquals(24.dp, gutter)
            assertEquals(80.dp, navRailWidth)
            assertEquals(400.dp, listPaneWidth)
            assertEquals(24.dp, paneGap)
            assertEquals(720.dp, readingMaxWidth)
        }
    }

    @Test
    fun forWidthPicksTheClassInstance() {
        assertEquals(AdaptiveLayoutTokens.Compact, AdaptiveLayoutTokens.forWidth(411f))
        assertEquals(AdaptiveLayoutTokens.Medium, AdaptiveLayoutTokens.forWidth(800f))
        assertEquals(AdaptiveLayoutTokens.Expanded, AdaptiveLayoutTokens.forWidth(1280f))
    }
}

package com.example.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/**
 * The window width classes of the tablet design (Figma "Tablet — Foundations"):
 * Compact is the phone layout with the bottom bar, Medium a navigation rail beside one
 * pane, Expanded the rail beside two panes where a screen has them.
 */
enum class WindowClass { Compact, Medium, Expanded }

/**
 * The design's class for a material3-adaptive [WindowSizeClass]: under 600dp Compact, under
 * 840dp Medium, everything wider Expanded. Google's Large (1200dp+) and Extra-large (1600dp+)
 * classes have no design of their own, so they are Expanded too.
 */
fun WindowSizeClass.toWindowClass(): WindowClass = when {
    isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowClass.Expanded
    isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowClass.Medium
    else -> WindowClass.Compact
}

/** The class of a window [widthDp] wide, by the same breakpoints. */
fun windowClassForWidth(widthDp: Float): WindowClass = WindowSizeClass(widthDp, 0f).toWindowClass()

/**
 * The Figma `Layout` variable collection, one instance per window class. Screens read
 * [LocalLayoutTokens]; [HassanAlHawaryTheme] provides the instance for the window's class.
 * `window/width` and `window/height` are artboard sizes only, so they are not here.
 */
@Immutable
data class AdaptiveLayoutTokens(
    val windowClass: WindowClass,
    /** grid/columns */
    val columns: Int,
    /** grid/margin: the window padding around the content. */
    val margin: Dp,
    /** grid/gutter */
    val gutter: Dp,
    /** nav/railWidth: 0 when there is no rail (Compact). */
    val navRailWidth: Dp,
    /** pane/listWidth */
    val listPaneWidth: Dp,
    /** pane/gap: between the list and detail panes. */
    val paneGap: Dp,
    /** reading/maxWidth: the widest a column of reading text gets. */
    val readingMaxWidth: Dp,
) {
    val isCompact: Boolean get() = windowClass == WindowClass.Compact
    val isExpanded: Boolean get() = windowClass == WindowClass.Expanded

    companion object {
        val Compact = AdaptiveLayoutTokens(
            windowClass = WindowClass.Compact,
            columns = 4,
            margin = 16.dp,
            gutter = 16.dp,
            navRailWidth = 0.dp,
            listPaneWidth = 360.dp,
            paneGap = 0.dp,
            readingMaxWidth = 328.dp,
        )
        val Medium = AdaptiveLayoutTokens(
            windowClass = WindowClass.Medium,
            columns = 8,
            margin = 24.dp,
            gutter = 24.dp,
            navRailWidth = 80.dp,
            listPaneWidth = 360.dp,
            paneGap = 24.dp,
            readingMaxWidth = 640.dp,
        )
        val Expanded = AdaptiveLayoutTokens(
            windowClass = WindowClass.Expanded,
            columns = 12,
            margin = 32.dp,
            gutter = 24.dp,
            navRailWidth = 80.dp,
            listPaneWidth = 400.dp,
            paneGap = 24.dp,
            readingMaxWidth = 720.dp,
        )

        fun forClass(windowClass: WindowClass): AdaptiveLayoutTokens = when (windowClass) {
            WindowClass.Compact -> Compact
            WindowClass.Medium -> Medium
            WindowClass.Expanded -> Expanded
        }

        fun forWidth(widthDp: Float): AdaptiveLayoutTokens = forClass(windowClassForWidth(widthDp))
    }
}

/** Compact by default, so a composable shown outside the theme keeps the phone layout. */
val LocalLayoutTokens = staticCompositionLocalOf { AdaptiveLayoutTokens.Compact }

/** Shorthand for [LocalLayoutTokens]. */
val layoutTokens: AdaptiveLayoutTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalLayoutTokens.current

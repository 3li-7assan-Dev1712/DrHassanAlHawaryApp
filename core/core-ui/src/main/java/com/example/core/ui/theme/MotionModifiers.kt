package com.example.core.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Lazy list items with a stable key: arrivals fade in, moves slide into place, removals fade
 * out. Nothing when reduced motion is on (items just appear where they belong).
 */
@Composable
fun LazyItemScope.animateListItem(): Modifier =
    if (reducedMotion) Modifier
    else Modifier.animateItem(
        fadeInSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
        placementSpec = tween(Motion.MEDIUM, easing = Motion.Standard),
        fadeOutSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
    )

/** [animateListItem] for lazy grids. */
@Composable
fun LazyGridItemScope.animateGridItem(): Modifier =
    if (reducedMotion) Modifier
    else Modifier.animateItem(
        fadeInSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
        placementSpec = tween(Motion.MEDIUM, easing = Motion.Standard),
        fadeOutSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
    )

/** Gap between sections in a staggered entrance. */
private const val STAGGER_MS = 40
/** Upper bound on how many sections a screen staggers; after this the window closes. */
private const val STAGGER_MAX_SECTIONS = 8

/**
 * Whether a screen should play its first-time entrance. True only on the screen's first
 * composition (saved, so coming back to it doesn't replay) and only while the entrance is
 * running, so sections composed later (scrolled into view) don't animate. False under
 * reduced motion.
 */
@Composable
fun rememberFirstEntrance(): Boolean {
    var played by rememberSaveable { mutableStateOf(false) }
    val reduced = reducedMotion
    var open by remember { mutableStateOf(!played && !reduced) }
    LaunchedEffect(Unit) {
        played = true
        if (open) {
            delay((STAGGER_MS * STAGGER_MAX_SECTIONS + Motion.MEDIUM).toLong())
            open = false
        }
    }
    return open
}

/**
 * One section of a staggered entrance: fades in and rises 8dp, `medium` decelerate,
 * starting [index] × 40ms after the screen appears. Drawn in place (graphicsLayer), so the
 * layout never moves.
 */
@Composable
fun Modifier.staggeredEntrance(index: Int, play: Boolean): Modifier {
    // Latched: once this section has started it finishes, even if the window closes.
    val animate = remember { play }
    if (!animate) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index * STAGGER_MS).toLong())
        progress.animateTo(1f, tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate))
    }
    val risePx = with(LocalDensity.current) { 8.dp.toPx() }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * risePx
    }
}

/**
 * Which kind of content a screen is showing. Key an `AnimatedContent` on it (with
 * [Motion.contentSwap]) so loading / empty / error / content crossfade instead of popping.
 */
enum class ContentPhase { Loading, Empty, Error, Content }

/** [animateListItem] for lazy staggered grids. */
@Composable
fun LazyStaggeredGridItemScope.animateStaggeredGridItem(): Modifier =
    if (reducedMotion) Modifier
    else Modifier.animateItem(
        fadeInSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
        placementSpec = tween(Motion.MEDIUM, easing = Motion.Standard),
        fadeOutSpec = tween(Motion.CONTENT_SWAP, easing = Motion.Standard),
    )

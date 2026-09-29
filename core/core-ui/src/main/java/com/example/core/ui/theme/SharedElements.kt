@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.core.ui.theme

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Container transforms (card → screen) for three flows: article card → reader, home audio
 * card → player, design tile → image viewer. The app root wraps the NavHost in a
 * `SharedTransitionLayout` and provides [LocalSharedTransitionScope]; each destination that
 * takes part provides its own [LocalNavAnimatedVisibilityScope]. Screens only apply the
 * modifiers below, so their public parameters don't change.
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The NavHost destination's enter/exit scope (see [ProvideNavAnimatedScope]). */
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Wraps one NavHost destination so shared elements inside it follow its transition. */
@Composable
fun ProvideNavAnimatedScope(scope: AnimatedVisibilityScope, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides scope, content = content)
}

/**
 * Keys: unique per item (its id or URL) and per role, so two elements never collide.
 * Article cards also carry where they are ([ArticleSource]): the same article can be on
 * Home and in the articles list, and with one key Home → list would morph one card into
 * the other. The reader registers both sources.
 */
object SharedKeys {
    fun article(id: String, source: ArticleSource) = "article-card-${source.name}-$id"
    fun articleTitle(id: String, source: ArticleSource) = "article-title-${source.name}-$id"
    fun audio(url: String) = "audio-card-$url"
    fun audioPlay(url: String) = "audio-play-$url"
    fun design(groupId: String) = "design-$groupId"
}

enum class ArticleSource { Home, List }

private val containerTransform = BoundsTransform { _, _ ->
    tween(Motion.LONG, easing = Motion.EmphasizedDecelerate)
}

/**
 * The container of a transform: a card on the list side, the whole screen on the detail
 * side. The contents crossfade while the bounds grow; the overlay is clipped to [shape].
 * Nothing when reduced motion is on, or outside a shared transition (previews, a screen
 * shown on its own): then the normal screen transition plays.
 */
@Composable
fun Modifier.sharedContainer(key: String, shape: Shape = RoundedCornerShape(12.dp)): Modifier =
    sharedBoundsOrSelf(key, shape, scaleContent = true)

/**
 * A part that morphs inside the container: the article title, the gold play circle.
 * [scaleContent] scales what is drawn between the two sizes (for text, so it doesn't
 * reflow every frame); otherwise it is re-measured to the moving bounds (plain shapes).
 */
@Composable
fun Modifier.sharedPart(key: String, shape: Shape = RectangleShape, scaleContent: Boolean = true): Modifier =
    sharedBoundsOrSelf(key, shape, scaleContent)

@Composable
private fun Modifier.sharedBoundsOrSelf(key: String, shape: Shape, scaleContent: Boolean): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    if (reducedMotion) return this
    return with(transitionScope) {
        this@sharedBoundsOrSelf.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = containerTransform,
            resizeMode = if (scaleContent) scaleToBounds() else RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}

/** The reader's side of the article transform: matches a card from either source. */
@Composable
fun Modifier.sharedArticleContainer(id: String): Modifier = this
    .sharedContainer(SharedKeys.article(id, ArticleSource.Home))
    .sharedContainer(SharedKeys.article(id, ArticleSource.List), RoundedCornerShape(14.dp))

/** The reader's title: matches the title of a card from either source. */
@Composable
fun Modifier.sharedArticleTitle(id: String): Modifier = this
    .sharedPart(SharedKeys.articleTitle(id, ArticleSource.Home))
    .sharedPart(SharedKeys.articleTitle(id, ArticleSource.List))

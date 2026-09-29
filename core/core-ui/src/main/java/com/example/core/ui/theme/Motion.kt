package com.example.core.ui.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * The app's motion rules (docs/plans/motion-pass.md, section B). Every animation reads its
 * durations, easings and ready-made transitions from here; nothing is longer than [LONG].
 */
object Motion {
    /** Small state changes: colour, icon swap, chip. */
    const val SHORT = 150
    /** Screen transitions, theme change. */
    const val MEDIUM = 300
    /** Container transform only. */
    const val LONG = 400
    /** Loading → content crossfade. */
    const val CONTENT_SWAP = 200
    /** The plain fade every screen transition becomes under reduced motion. */
    const val REDUCED_FADE = 100

    /** Fade through: the old tab leaves over this, then the new one arrives. */
    const val FADE_THROUGH_OUT = 90
    const val FADE_THROUGH_IN = 210
    const val FADE_THROUGH_SCALE = 0.92f

    /** How far screens slide in the shared-axis transition, in dp. */
    const val SHARED_AXIS_OFFSET_DP = 30

    /** Entering. */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    /** Leaving. */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    /** State changes. */
    val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Colour, size and offset changes of a single element: `short`, standard. */
    fun <T> stateChange(): FiniteAnimationSpec<T> = tween(SHORT, easing = Standard)

    // --- Screen transitions -------------------------------------------------------------

    /** Bottom-nav tab arriving: waits for the old one to fade, then fades and grows in. */
    fun fadeThroughEnter(reduced: Boolean): EnterTransition =
        if (reduced) reducedEnter() else
            fadeIn(tween(FADE_THROUGH_IN, delayMillis = FADE_THROUGH_OUT, easing = EmphasizedDecelerate)) +
                scaleIn(
                    tween(FADE_THROUGH_IN, delayMillis = FADE_THROUGH_OUT, easing = EmphasizedDecelerate),
                    initialScale = FADE_THROUGH_SCALE,
                )

    /** Bottom-nav tab leaving: a quick fade, no movement. */
    fun fadeThroughExit(reduced: Boolean): ExitTransition =
        if (reduced) reducedExit() else
            fadeOut(tween(FADE_THROUGH_OUT, easing = EmphasizedAccelerate))

    /** Going deeper: the new screen arrives from the END side. */
    fun sharedAxisEnter(offsetPx: Int, isRtl: Boolean, reduced: Boolean): EnterTransition =
        sharedAxisIn(SharedAxis.enterSign(forward = true, isRtl = isRtl), offsetPx, reduced)

    /** Going deeper: the old screen drifts toward START. */
    fun sharedAxisExit(offsetPx: Int, isRtl: Boolean, reduced: Boolean): ExitTransition =
        sharedAxisOut(SharedAxis.exitSign(forward = true, isRtl = isRtl), offsetPx, reduced)

    /** Back: the revealed screen returns from the START side. */
    fun sharedAxisPopEnter(offsetPx: Int, isRtl: Boolean, reduced: Boolean): EnterTransition =
        sharedAxisIn(SharedAxis.enterSign(forward = false, isRtl = isRtl), offsetPx, reduced)

    /** Back: the closing screen drifts toward END. */
    fun sharedAxisPopExit(offsetPx: Int, isRtl: Boolean, reduced: Boolean): ExitTransition =
        sharedAxisOut(SharedAxis.exitSign(forward = false, isRtl = isRtl), offsetPx, reduced)

    private fun sharedAxisIn(sign: Int, offsetPx: Int, reduced: Boolean): EnterTransition =
        if (reduced) reducedEnter() else
            slideInHorizontally(tween(MEDIUM, easing = EmphasizedDecelerate)) { sign * offsetPx } +
                fadeIn(tween(MEDIUM, easing = EmphasizedDecelerate))

    private fun sharedAxisOut(sign: Int, offsetPx: Int, reduced: Boolean): ExitTransition =
        if (reduced) reducedExit() else
            slideOutHorizontally(tween(MEDIUM, easing = EmphasizedAccelerate)) { sign * offsetPx } +
                fadeOut(tween(MEDIUM, easing = EmphasizedAccelerate))

    private fun reducedEnter(): EnterTransition = fadeIn(tween(REDUCED_FADE))
    private fun reducedExit(): ExitTransition = fadeOut(tween(REDUCED_FADE))

    // --- In-screen content --------------------------------------------------------------

    /**
     * Loading / error / empty / content switches: a ~200ms crossfade, the container's size
     * following on the same curve; instant when reduced.
     */
    fun contentSwap(reduced: Boolean): ContentTransform =
        if (reduced) ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
        else ContentTransform(
            targetContentEnter = fadeIn(tween(CONTENT_SWAP, easing = Standard)),
            initialContentExit = fadeOut(tween(CONTENT_SWAP, easing = Standard)),
            sizeTransform = SizeTransform(clip = false) { _, _ -> tween(CONTENT_SWAP, easing = Standard) },
        )

    /**
     * A changing number or short label ("صوتيات ٩", the speed): the new value rises from
     * below while the old one leaves upward. A plain swap when reduced.
     */
    fun countSlide(reduced: Boolean, up: Boolean = true): ContentTransform {
        if (reduced) return ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
        val sign = if (up) 1 else -1
        return ContentTransform(
            targetContentEnter = slideInVertically(tween(SHORT, easing = EmphasizedDecelerate)) { h -> sign * h / 2 } +
                fadeIn(tween(SHORT, easing = EmphasizedDecelerate)),
            initialContentExit = slideOutVertically(tween(SHORT, easing = EmphasizedAccelerate)) { h -> -sign * h / 2 } +
                fadeOut(tween(SHORT, easing = EmphasizedAccelerate)),
            sizeTransform = SizeTransform(clip = false) { _, _ -> tween(SHORT, easing = Standard) },
        )
    }
}

/**
 * Which way a horizontal shared-axis slide goes. Pure so it can be unit tested.
 *
 * "Forward" (going deeper) enters from the END side and moves toward START; "back" is the
 * mirror. END is the right edge in LTR and the left edge in RTL. The signs are for the
 * absolute x offset that `slideInHorizontally` / `slideOutHorizontally` take.
 */
object SharedAxis {
    /** Sign of the entering screen's starting x offset. */
    fun enterSign(forward: Boolean, isRtl: Boolean): Int {
        val endSide = if (isRtl) -1 else 1
        return if (forward) endSide else -endSide
    }

    /** Sign of the leaving screen's final x offset. */
    fun exitSign(forward: Boolean, isRtl: Boolean): Int = -enterSign(forward, isRtl)
}

/**
 * True when the system's "Remove animations" setting is on (animator duration scale 0).
 * Provided by [HassanAlHawaryTheme]. When true, screen transitions are a plain short fade
 * and decorative motion is off; functional feedback still happens, instantly.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Shorthand for [LocalReducedMotion]. */
val reducedMotion: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalReducedMotion.current

/** [Motion.stateChange], or an instant snap under reduced motion. */
@Composable
@ReadOnlyComposable
fun <T> stateChangeSpec(): AnimationSpec<T> =
    if (LocalReducedMotion.current) snap() else Motion.stateChange()

/** Reads the animator duration scale and follows changes while the app is open. */
@Composable
fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    var reduced by remember { mutableStateOf(context.animationsRemoved()) }
    DisposableEffect(context) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = context.animationsRemoved()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

private fun Context.animationsRemoved(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

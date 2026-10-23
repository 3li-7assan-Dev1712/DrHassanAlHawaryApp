package com.example.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.semantics.Role
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A row inside a list card (the categories, a series' lessons) that [onClick]s with a flat
 * pressed state: textPrimary at 10% over the row. Material's ripple is drawn by the
 * platform, which on Android 12+ fills it with a "sparkle" noise; on the cards' plain
 * surface that read as a grainy texture on the pressed row.
 */
@Composable
fun Modifier.rowClickable(onClick: () -> Unit, role: Role = Role.Button): Modifier = clickable(
    interactionSource = null,
    indication = PressStateLayer(Brand.colors.textPrimary, reducedMotion),
    role = role,
    onClick = onClick,
)

/** [color] at [PRESSED_ALPHA] while pressed; it fades (`short`), or snaps when [reduced]. */
private class PressStateLayer(private val color: Color, private val reduced: Boolean) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressStateLayerNode(interactionSource, color, reduced)

    override fun equals(other: Any?): Boolean =
        other is PressStateLayer && other.color == color && other.reduced == reduced

    override fun hashCode(): Int = 31 * color.hashCode() + reduced.hashCode()
}

private class PressStateLayerNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
    private val reduced: Boolean,
) : Modifier.Node(), DrawModifierNode {

    private val alpha = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            var presses = 0
            // A quick tap releases before the layer is in: it still shows, then fades.
            var showing: Job? = null
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        presses++
                        showing = launch { animateTo(PRESSED_ALPHA) }
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        presses = (presses - 1).coerceAtLeast(0)
                        if (presses == 0) {
                            val shown = showing
                            launch {
                                shown?.join()
                                if (presses == 0) animateTo(0f)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun animateTo(target: Float) {
        if (reduced) alpha.snapTo(target) else alpha.animateTo(target, tween(Motion.SHORT, easing = Motion.Standard))
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (alpha.value > 0f) drawRect(color = color, alpha = alpha.value)
    }
}

private const val PRESSED_ALPHA = 0.10f

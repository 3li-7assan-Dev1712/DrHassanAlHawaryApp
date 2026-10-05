package com.example.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.stateChangeSpec

/** Fill, border and text colour of a selectable pill (search filters, clip lengths). */
@Immutable
data class PillStyle(val fill: Color, val border: BorderStroke, val content: Color)

/**
 * The selectable-pill look: accentContainer with a 1dp accentStrong border when selected,
 * surface with a 0.5dp divider border otherwise. Every part moves with `stateChange`
 * (instant under reduced motion) instead of flipping.
 */
@Composable
fun animatedPillStyle(selected: Boolean): PillStyle {
    val colors = Brand.colors
    val fill by animateColorAsState(
        if (selected) colors.accentContainer else colors.surface, stateChangeSpec(), label = "pillFill",
    )
    val borderColor by animateColorAsState(
        if (selected) colors.accentStrong else colors.divider, stateChangeSpec(), label = "pillBorder",
    )
    val borderWidth by animateDpAsState(if (selected) 1.dp else 0.5.dp, stateChangeSpec(), label = "pillBorderWidth")
    val content by animateColorAsState(
        if (selected) colors.onAccentContainer else colors.textSecondary, stateChangeSpec(), label = "pillText",
    )
    return PillStyle(fill, BorderStroke(borderWidth, borderColor), content)
}

package com.example.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.stateChangeSpec

/** Fill and outline of a list card that can be the selected item of a two-pane layout. */
@Immutable
data class ListItemStyle(val fill: Color, val border: BorderStroke)

/** The selected list item's outline (Figma: 1.5px accentStrong). */
val SelectedItemBorderWidth: Dp = 1.5.dp

/**
 * The selected list item of a two-pane layout: accentContainer with a 1.5dp accentStrong
 * outline; otherwise the card's own [fill] and hairline. Moves with `stateChange` (instant
 * under reduced motion), like the selectable pills.
 */
@Composable
fun animatedListItemStyle(
    selected: Boolean,
    fill: Color = Brand.colors.surface,
    borderWidth: Dp = 0.5.dp,
    borderColor: Color = Brand.colors.divider,
): ListItemStyle {
    val colors = Brand.colors
    val animatedFill by animateColorAsState(
        if (selected) colors.accentContainer else fill, stateChangeSpec(), label = "itemFill",
    )
    val animatedBorderColor by animateColorAsState(
        if (selected) colors.accentStrong else borderColor, stateChangeSpec(), label = "itemBorder",
    )
    val animatedBorderWidth by animateDpAsState(
        if (selected) SelectedItemBorderWidth else borderWidth, stateChangeSpec(), label = "itemBorderWidth",
    )
    return ListItemStyle(animatedFill, BorderStroke(animatedBorderWidth, animatedBorderColor))
}

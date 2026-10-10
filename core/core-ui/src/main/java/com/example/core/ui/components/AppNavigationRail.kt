package com.example.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.stateChangeSpec

/** One destination of [AppNavigationRail]: the same icon and label as the bottom bar's tab. */
@Immutable
data class RailDestination(
    @DrawableRes val icon: Int,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

/** Figma NavRail (`47:419`), measured in dp. */
object AppNavigationRailDefaults {
    val Width = 80.dp
    val VerticalPadding = 32.dp
    val ItemSpacing = 12.dp
    val IndicatorWidth = 56.dp
    val IndicatorHeight = 32.dp
    val IndicatorToLabel = 4.dp
    val IconSize = 24.dp

    const val TestTag = "appNavigationRail"
    fun indicatorTestTag(label: String) = "railIndicator:$label"
}

/**
 * The navigation rail of Medium and Expanded windows (Figma NavRail): 80dp wide, the full
 * window height, on the start edge (right in RTL) with a 1dp divider on the edge that faces
 * the content. Selected: an accentContainer pill behind an onAccentContainer icon and a
 * goldText SemiBold label; otherwise no pill and textMuted. Material's NavigationRail has
 * other measurements, hence this one.
 *
 * Figma has no hover, focus or pressed states, so those are Material's: the ripple state
 * layer on the pill, which also shows keyboard focus.
 */
@Composable
fun AppNavigationRail(
    destinations: List<RailDestination>,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical),
) {
    val colors = Brand.colors
    Column(
        modifier = modifier
            .testTag(AppNavigationRailDefaults.TestTag)
            .width(AppNavigationRailDefaults.Width)
            .fillMaxHeight()
            .background(colors.background)
            .drawBehind {
                // The end edge faces the content: left in RTL.
                val stroke = 1.dp.toPx()
                val x = if (layoutDirection == LayoutDirection.Rtl) stroke / 2 else size.width - stroke / 2
                drawLine(colors.divider, Offset(x, 0f), Offset(x, size.height), stroke)
            }
            .windowInsetsPadding(windowInsets)
            .padding(vertical = AppNavigationRailDefaults.VerticalPadding)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppNavigationRailDefaults.ItemSpacing),
    ) {
        destinations.forEach { RailItem(it) }
    }
}

@Composable
private fun RailItem(destination: RailDestination) {
    val colors = Brand.colors
    val selected = destination.selected
    val interactionSource = remember { MutableInteractionSource() }
    val indicator by animateColorAsState(
        if (selected) colors.accentContainer else Color.Transparent, stateChangeSpec(), label = "railIndicator",
    )
    val iconTint by animateColorAsState(
        if (selected) colors.onAccentContainer else colors.textMuted, stateChangeSpec(), label = "railIcon",
    )
    val labelColor by animateColorAsState(
        if (selected) colors.goldText else colors.textMuted, stateChangeSpec(), label = "railLabel",
    )
    // The whole 80×52 destination is the touch target; the ripple is drawn on the pill.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = destination.onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppNavigationRailDefaults.IndicatorToLabel),
    ) {
        Box(
            modifier = Modifier
                .testTag(AppNavigationRailDefaults.indicatorTestTag(destination.label))
                .size(AppNavigationRailDefaults.IndicatorWidth, AppNavigationRailDefaults.IndicatorHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(indicator)
                .indication(interactionSource, ripple()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(destination.icon),
                contentDescription = null, // the label is read instead
                tint = iconTint,
                modifier = Modifier.size(AppNavigationRailDefaults.IconSize),
            )
        }
        // Nav/labelActive and Nav/label: Cairo 12/16, SemiBold when selected. Android lays a
        // Cairo line out at the font's own height (~22dp at 12sp) whatever the lineHeight,
        // so the label is centred in a 16sp slot and overflows it, as in Figma's line box;
        // the slot grows with the font size setting.
        val labelSlot = with(LocalDensity.current) { 16.sp.toDp() }
        Text(
            text = destination.label,
            color = labelColor,
            fontFamily = Cairo,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .height(labelSlot)
                .wrapContentHeight(unbounded = true),
        )
    }
}

private fun previewDestinations(selected: Int) = listOf(
    TablerIcons.Home to "الرئيسية",
    TablerIcons.Search to "بحث",
    TablerIcons.School to "المعهد",
    TablerIcons.User to "حسابي",
).mapIndexed { index, (icon, label) -> RailDestination(icon, label, index == selected, onClick = {}) }

@Preview(name = "Rail - home, light", locale = "ar", heightDp = 560)
@Composable
private fun AppNavigationRailLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { AppNavigationRail(previewDestinations(selected = 0)) }
}

@Preview(name = "Rail - profile, dark", locale = "ar", heightDp = 560)
@Composable
private fun AppNavigationRailDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { AppNavigationRail(previewDestinations(selected = 3)) }
}

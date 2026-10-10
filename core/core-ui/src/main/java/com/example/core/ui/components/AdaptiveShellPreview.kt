package com.example.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.LocalLayoutTokens
import com.example.core.ui.theme.WindowClass
import androidx.compose.ui.unit.Dp

/** The rail destination a preview shows as selected. */
enum class PreviewTab { Home, Search, Institute, Profile }

/**
 * Previews and UI tests: a screen inside the app shell the way `MainActivity` lays it out
 * for the window class — on Medium and Expanded the rail on the start edge and the screen
 * inside the window margin; on Compact the screen alone (the phone layout). [margin] false
 * is for the screens drawn to the window edges (the designs viewer) or without the rail;
 * [mediumMargin] for the grid screens drawn with another Medium margin.
 */
@Composable
fun AdaptiveShellPreview(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    selectedTab: PreviewTab = PreviewTab.Home,
    showRail: Boolean = true,
    margin: Boolean = true,
    mediumMargin: Dp? = null,
    layoutTokens: AdaptiveLayoutTokens? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    HassanAlHawaryTheme(darkTheme = darkTheme, layoutTokens = layoutTokens) {
        val tokens = LocalLayoutTokens.current
        Row(modifier = modifier.fillMaxSize().background(Brand.colors.background)) {
            if (showRail && !tokens.isCompact) {
                AppNavigationRail(
                    destinations = previewRailDestinations(selectedTab),
                    windowInsets = WindowInsets(0),
                )
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (margin) {
                    WindowMargin(margin = mediumMargin.takeIf { tokens.windowClass == WindowClass.Medium }, content = content)
                } else {
                    Box(Modifier.fillMaxSize(), content = content)
                }
            }
        }
    }
}

/** The app's four tabs (the labels live in the app module's resources). */
fun previewRailDestinations(selected: PreviewTab): List<RailDestination> = listOf(
    TablerIcons.Home to "الرئيسية",
    TablerIcons.Search to "بحث",
    TablerIcons.School to "المعهد",
    TablerIcons.User to "حسابي",
).mapIndexed { index, (icon, label) ->
    RailDestination(icon, label, selected = index == selected.ordinal, onClick = {})
}

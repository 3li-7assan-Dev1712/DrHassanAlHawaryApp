package com.example.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.layoutTokens

/** Figma pane: radius 20 with a 1dp divider outline. */
private val PaneShape = RoundedCornerShape(20.dp)

object AdaptivePanesDefaults {
    const val ListPaneTestTag = "listPane"
    const val DetailPaneTestTag = "detailPane"
    const val WindowMarginTestTag = "windowMargin"

    /**
     * The Medium margin of the grid screens (videos, designs): Figma draws them 20 from the
     * window edges, so two 328dp video cards and their 24 gap fit the 680 between.
     */
    val GridScreenMediumMargin = 20.dp

    /** First-run and system screens (welcome, onboarding, update) on a tablet: a centred column. */
    val FirstRunColumnWidth = 520.dp
    /** Their side padding there (Figma: 16 around the buttons). */
    val FirstRunPadding = 16.dp
}

/**
 * A pane of a tablet layout: outlined, not filled (transparent, 1dp divider, radius 20,
 * content clipped), so the phone cards inside keep the look they have on the background.
 * The content starts inside the outline, as in Figma (a 400 pane holds a 398 top bar).
 */
@Composable
fun Modifier.paneSurface(): Modifier = this
    .clip(PaneShape)
    .border(PaneStroke, Brand.colors.divider, PaneShape)
    .padding(PaneStroke)

private val PaneStroke = 1.dp

/**
 * The window margin (grid/margin) around a main-app screen in Medium (24) and Expanded (32)
 * windows; nothing in Compact, where the screen is the phone layout as it always was.
 * The navigation rail sits outside it.
 *
 * On Medium and Expanded the margin starts below the status bar and ends above the
 * navigation bar (the shell keeps the sides clear), and the system bars are consumed: a
 * phone screen's Scaffold inside a pane would otherwise pad for them again. (The keyboard is
 * left to the screens.)
 *
 * [margin]: a screen whose frame Figma draws with another margin (the Medium grids: 20).
 */
@Composable
fun WindowMargin(
    modifier: Modifier = Modifier,
    margin: Dp? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = layoutTokens
    Box(
        modifier = modifier
            .testTag(AdaptivePanesDefaults.WindowMarginTestTag)
            .fillMaxSize()
            .then(
                if (tokens.isCompact) {
                    Modifier
                } else {
                    Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical))
                        .consumeWindowInsets(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                        .padding(margin ?: tokens.margin)
                },
            ),
        content = content,
    )
}

/**
 * List and detail side by side (Expanded), on material3-adaptive's [ListDetailPaneScaffold]:
 * the list pane (pane/listWidth, 400) on the start side (right in RTL), pane/gap (24), then
 * the detail pane, which the scaffold gives the rest of the width (it has the highest
 * priority). Both are [paneSurface]s the full height of the content area.
 *
 * Both panes are always shown. Selection and back stay with the screens and the
 * NavController, which give Medium and Compact their own list and detail destinations.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TwoPaneLayout(
    listPane: @Composable BoxScope.() -> Unit,
    detailPane: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = layoutTokens
    val directive = remember(tokens) {
        PaneScaffoldDirective(
            maxHorizontalPartitions = 2,
            horizontalPartitionSpacerSize = tokens.paneGap,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = tokens.listPaneWidth,
            excludedBounds = emptyList(),
        )
    }
    val value = remember {
        calculateThreePaneScaffoldValue(
            maxHorizontalPartitions = 2,
            adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
            currentDestination = ThreePaneScaffoldDestinationItem<Nothing>(ListDetailPaneScaffoldRole.Detail),
        )
    }
    ListDetailPaneScaffold(
        directive = directive,
        value = value,
        listPane = { OutlinedPane(AdaptivePanesDefaults.ListPaneTestTag, listPane) },
        detailPane = { OutlinedPane(AdaptivePanesDefaults.DetailPaneTestTag, detailPane) },
        modifier = modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldPaneScope.OutlinedPane(testTag: String, content: @Composable BoxScope.() -> Unit) {
    AnimatedPane {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .fillMaxSize()
                .paneSurface(),
            content = content,
        )
    }
}

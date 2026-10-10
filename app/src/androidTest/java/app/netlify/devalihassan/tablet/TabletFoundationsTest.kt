package app.netlify.devalihassan.tablet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.AppNavigationRailDefaults
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.EmptyDetailDefaults
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.AdaptiveLayoutTokens
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The tablet shell's measurements from Figma (Tablet — Foundations, NavRail `47:419`,
 * EmptyDetail `57:138`, and the Expanded / Medium frames' Content and panes).
 */
@RunWith(AndroidJUnit4::class)
class TabletFoundationsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun expanded_railOnTheRight_twoOutlinedPanesInsideTheMargin() {
        rule.setShellContent(ExpandedWindow, AdaptiveLayoutTokens.Expanded) {
            TwoPaneLayout(
                listPane = {},
                detailPane = {
                    EmptyDetail(TablerIcons.Headphones, title = "اختر قسمًا للاستماع", subtitle = "ستظهر هنا الدروس والخطب والفتاوى")
                },
            )
        }
        rule.saveShellShot("foundations-expanded-light")

        val rail = rule.boundsInShell(AppNavigationRailDefaults.TestTag)
        assertDp(80.dp, rail.right - rail.left, "rail width")
        assertDp(800.dp, rail.bottom - rail.top, "rail height")
        assertDp(1280.dp, rail.right, "rail on the right edge (start in RTL)")

        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        assertDp(400.dp, list.right - list.left, "list pane width")
        assertDp(736.dp, list.bottom - list.top, "pane height = 800 - 2 × 32")
        assertDp(32.dp, list.top, "top margin")
        assertDp(32.dp, rail.left - list.right, "margin between list pane and rail")

        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        assertDp(32.dp, detail.left, "left margin")
        assertDp(24.dp, list.left - detail.right, "pane gap")
        assertDp(712.dp, detail.right - detail.left, "detail pane fills the rest")
        assertDp(736.dp, detail.bottom - detail.top, "detail pane height")

        val empty = rule.boundsInShell(EmptyDetailDefaults.TestTag)
        assertDp(360.dp, empty.right - empty.left, "empty detail width")
        assertDp((detail.left + detail.right) / 2, (empty.left + empty.right) / 2, "empty detail centred horizontally")
        assertDp((detail.top + detail.bottom) / 2, (empty.top + empty.bottom) / 2, "empty detail centred vertically")
        val circle = rule.boundsInShell(EmptyDetailDefaults.CircleTestTag)
        assertDp(96.dp, circle.right - circle.left, "circle width")
        assertDp(96.dp, circle.bottom - circle.top, "circle height")
    }

    @Test
    fun medium_railAndOnePaneInsideThe24Margin() {
        rule.setShellContent(MediumWindow, AdaptiveLayoutTokens.Medium) {
            Box(Modifier.fillMaxSize().testTag("content"))
        }
        val rail = rule.boundsInShell(AppNavigationRailDefaults.TestTag)
        assertDp(80.dp, rail.right - rail.left, "rail width")
        assertDp(1280.dp, rail.bottom - rail.top, "rail height")
        assertDp(800.dp, rail.right, "rail on the right edge")

        val content = rule.boundsInShell("content")
        assertDp(24.dp, content.left, "left margin")
        assertDp(24.dp, content.top, "top margin")
        assertDp(24.dp, 1280.dp - content.bottom, "bottom margin")
        assertDp(24.dp, rail.left - content.right, "margin before the rail")
        assertDp(672.dp, content.right - content.left, "content width = 800 - 80 - 2 × 24")
    }

    @Test
    fun compact_noRail_noMargin() {
        rule.setShellContent(CompactWindow, AdaptiveLayoutTokens.Compact) {
            Box(Modifier.fillMaxSize().testTag("content"))
        }
        assertEquals(0, rule.onAllNodesWithTag(AppNavigationRailDefaults.TestTag).fetchSemanticsNodes().size)
        val content = rule.boundsInShell("content")
        assertDp(0.dp, content.left, "no margin")
        assertDp(0.dp, content.top, "no margin")
        assertDp(360.dp, content.right - content.left, "full width")
    }

    @Test
    fun rail_destinations() {
        rule.setShellContent(ExpandedWindow, AdaptiveLayoutTokens.Expanded, selectedTab = PreviewTab.Home) {}
        val rail = rule.boundsInShell(AppNavigationRailDefaults.TestTag)
        val labels = listOf("الرئيسية", "بحث", "المعهد", "حسابي")
        labels.forEachIndexed { index, label ->
            val pill = rule.boundsInShell(AppNavigationRailDefaults.indicatorTestTag(label))
            assertDp(56.dp, pill.right - pill.left, "$label pill width")
            assertDp(32.dp, pill.bottom - pill.top, "$label pill height")
            assertDp((rail.left + rail.right) / 2, (pill.left + pill.right) / 2, "$label centred in the rail")
            // 32 top padding, then per destination: pill 32 + gap 4 + label 16, 12 between.
            assertDp(rail.top + 32.dp + (64.dp * index), pill.top, "$label pill top")
        }
    }

    @Test
    fun shots_lightAndDark() {
        rule.setShellContent(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true, selectedTab = PreviewTab.Profile) {
            EmptyDetail(TablerIcons.Search, title = "اختر نتيجة لعرضها", subtitle = "ابحث في المقالات والصوتيات والفتاوى والمرئيات")
        }
        rule.saveShellShot("foundations-medium-dark")
    }
}

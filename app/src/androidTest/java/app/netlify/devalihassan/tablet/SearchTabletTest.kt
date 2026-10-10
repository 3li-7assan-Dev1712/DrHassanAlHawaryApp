package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.EmptyDetailDefaults
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.search.presentation.SearchAdaptiveLayout
import com.example.search.presentation.SearchPreviewList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Search (Figma `59:939` / `59:1412` Expanded, `64:3376` / `64:3466` Medium). */
@RunWith(AndroidJUnit4::class)
class SearchTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setSearch(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme, selectedTab = PreviewTab.Search) {
            SearchAdaptiveLayout(selectedResult = null, detailPane = {}) { selectedObjectId ->
                SearchPreviewList(selectedObjectId)
            }
        }
    }

    @Test
    fun expanded_searchInTheListPane_emptyDetail() {
        setSearch(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("search-expanded-light")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val row = rule.boundsOfText("أحكام صيام المرأة")
        assertDp(366.dp, row.widthDp, "results fill the list pane: 398 - 2 × 16")
        assertDp(list.right - 17.dp, row.right, "16 padding inside the outline")
        val field = rule.boundsInShell(rule.onNode(hasSetTextAction()))
        assertDp(list.top + 1.dp + 56.dp, field.top, "the search field under the 56dp top bar")

        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val empty = rule.boundsInShell(EmptyDetailDefaults.TestTag)
        assertDp((detail.left + detail.right) / 2, (empty.left + empty.right) / 2, "empty detail centred")
        assertDp((detail.top + detail.bottom) / 2, (empty.top + empty.bottom) / 2, "empty detail centred")
    }

    @Test
    fun expanded_dark() {
        setSearch(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("search-expanded-dark")
    }

    @Test
    fun medium_singlePane() {
        setSearch(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("search-medium-light")
        val row = rule.boundsOfText("أحكام صيام المرأة")
        assertDp(640.dp, row.widthDp, "the phone list in the 672 content")
    }

    @Test
    fun medium_dark() {
        setSearch(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("search-medium-dark")
    }

    @Test
    fun compact_isThePhoneSearch() {
        setSearch(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("search-compact-light")
        val row = rule.boundsOfText("أحكام صيام المرأة")
        assertDp(328.dp, row.widthDp, "360 - 2 × 16")
    }
}

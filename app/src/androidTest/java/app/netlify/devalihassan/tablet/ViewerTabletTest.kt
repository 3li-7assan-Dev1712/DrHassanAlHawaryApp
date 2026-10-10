package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AppNavigationRailDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.image.presentation.detail.ImageViewerContent
import com.example.feature.image.presentation.detail.VIEWER_POSTER_TEST_TAG
import com.example.feature.image.presentation.detail.ViewerPreviewState
import androidx.compose.ui.test.onAllNodesWithTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Designs viewer (Figma `60:1782` / `60:2086` Expanded, `64:4976` / `64:5003` Medium). */
@RunWith(AndroidJUnit4::class)
class ViewerTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setViewer(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        // Immersive: no rail, no window margin from the shell (the viewer pads itself).
        rule.setShellContent(window, tokens, darkTheme = darkTheme, showRail = false, margin = false) {
            ImageViewerContent(uiState = ViewerPreviewState, onNavigateBack = {})
        }
    }

    @Test
    fun expanded_posterCentred_noRail() {
        setViewer(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("viewer-expanded-light")
        assertEquals(0, rule.onAllNodesWithTag(AppNavigationRailDefaults.TestTag).fetchSemanticsNodes().size)
        val poster = rule.boundsInShell(VIEWER_POSTER_TEST_TAG)
        assertDp(688.dp, poster.widthDp, "poster width")
        assertDp(460.dp, poster.heightDp, "poster height")
        assertDp(640.dp, (poster.left + poster.right) / 2, "centred in the 1280 window")
    }

    @Test
    fun expanded_dark() {
        setViewer(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("viewer-expanded-dark")
    }

    @Test
    fun medium_posterCentred() {
        setViewer(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("viewer-medium-light")
        val poster = rule.boundsInShell(VIEWER_POSTER_TEST_TAG)
        assertDp(688.dp, poster.widthDp, "poster width")
        assertDp(460.dp, poster.heightDp, "poster height")
        assertDp(400.dp, (poster.left + poster.right) / 2, "centred in the 800 window")
    }

    @Test
    fun medium_dark() {
        setViewer(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("viewer-medium-dark")
    }

    @Test
    fun compact_isThePhoneViewer() {
        setViewer(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("viewer-compact-light")
        assertEquals("no tablet poster frame on the phone", 0, rule.onAllNodesWithTag(VIEWER_POSTER_TEST_TAG).fetchSemanticsNodes().size)
    }
}

package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.about_dr_hassan.presentation.AboutDrHassanScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** About the Sheikh (Figma `61:1998` / `61:2273` Expanded, `64:6378` / `64:6446` Medium). */
@RunWith(AndroidJUnit4::class)
class AboutTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setAbout(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            AboutDrHassanScreen(onNavigateBack = {})
        }
    }

    @Test
    fun expanded_profilePaneAndTabsPane() {
        setAbout(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("about-expanded-light")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        assertDp(400.dp, list.widthDp, "profile pane 400")
        // The channels card in the profile pane, 16 padding inside the outline.
        val website = rule.boundsOfText("الموقع الرسمي")
        assertDp(list.right - 1.dp - 16.dp, website.right, "profile pane content padding 16")
        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val bioTab = rule.boundsOfText("نبذة")
        assertDp(detail.top + 1.dp + 16.dp, bioTab.top, "tabs at the top of the detail pane, padding 16")
    }

    @Test
    fun expanded_dark() {
        setAbout(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("about-expanded-dark")
    }

    @Test
    fun medium_singleColumn() {
        setAbout(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("about-medium-light")
        val website = rule.boundsOfText("الموقع الرسمي")
        assertDp(24.dp + 672.dp - 16.dp, website.right, "the phone column inside the 24 margin")
    }

    @Test
    fun medium_dark() {
        setAbout(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("about-medium-dark")
    }
}

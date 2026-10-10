package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.netlify.devalihassan.ui.q_a.QAScreen
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** فاسألوا (Figma `61:2118` / `61:2373` Expanded, `64:6544` / `64:6603` Medium). */
@RunWith(AndroidJUnit4::class)
class FasalooTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setFasaloo(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) { QAScreen() }
    }

    @Test
    fun expanded_buttonPinnedToTheIntroPane() {
        setFasaloo(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("fasaloo-expanded-light")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val button = rule.boundsOfText(OPEN_TELEGRAM)
        assertDp(list.bottom - 1.dp - 16.dp, button.bottom, "pinned 16 above the pane's bottom")
        assertDp(400.dp - 2.dp - 32.dp, button.widthDp, "16 padding on both sides")
        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val step = rule.boundsOfText("افتح فاسألوا في تيليجرام")
        assertTrue("steps in the detail pane", step.right.value < detail.right.value && step.left.value > detail.left.value)
    }

    @Test
    fun expanded_dark() {
        setFasaloo(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("fasaloo-expanded-dark")
    }

    @Test
    fun medium_buttonPinnedToTheBottom() {
        setFasaloo(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("fasaloo-medium-light")
        val button = rule.boundsOfText(OPEN_TELEGRAM)
        assertDp(1280.dp - 24.dp - 16.dp, button.bottom, "pinned 16 above the margin")
    }

    @Test
    fun medium_dark() {
        setFasaloo(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("fasaloo-medium-dark")
    }

    private companion object {
        const val OPEN_TELEGRAM = "افتح في تيليجرام"
    }
}

package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.share.presentation.ShareDialogPreviewContent
import com.example.feature.share.presentation.ShareDialogSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Share preview dialog (Figma `59:1158` / `59:1626` Expanded, `64:4477` / `64:4726` Medium). */
@RunWith(AndroidJUnit4::class)
class ShareDialogTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setDialog(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        // A dialog window: no rail, no margin from the shell.
        rule.setShellContent(window, tokens, darkTheme = darkTheme, showRail = false, margin = false) {
            ShareDialogPreviewContent()
        }
    }

    @Test
    fun expanded_720_centred() {
        setDialog(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("share-expanded-light")
        val dialog = rule.boundsInShell(ShareDialogSpec.TestTag)
        assertDp(720.dp, dialog.widthDp, "dialog width")
        assertDp(640.dp, (dialog.left + dialog.right) / 2, "centred")
        val button = rule.boundsOfText("مشاركة الفيديو")
        assertDp(720.dp - 32.dp, button.widthDp, "full-width button, 16 each side")
    }

    @Test
    fun expanded_dark() {
        setDialog(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("share-expanded-dark")
    }

    @Test
    fun medium_600_stacked() {
        setDialog(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("share-medium-light")
        val dialog = rule.boundsInShell(ShareDialogSpec.TestTag)
        assertDp(600.dp, dialog.widthDp, "dialog width")
        assertDp(400.dp, (dialog.left + dialog.right) / 2, "centred")
    }

    @Test
    fun medium_dark() {
        setDialog(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("share-medium-dark")
    }
}

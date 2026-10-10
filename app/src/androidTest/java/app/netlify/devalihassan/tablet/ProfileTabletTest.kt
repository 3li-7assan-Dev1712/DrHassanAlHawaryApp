package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.profile.presentation.profile.ProfilePreviewContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Profile (Figma `59:1054` / `59:1522` Expanded, `64:3557` / `64:3650` Medium). */
@RunWith(AndroidJUnit4::class)
class ProfileTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setProfile(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme, selectedTab = PreviewTab.Profile) {
            ProfilePreviewContent()
        }
    }

    @Test
    fun expanded_twoColumns() {
        setProfile(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("profile-expanded-light")
        // Content 32..1168: start column 612..1168, end column 32..588.
        val about = rule.boundsOfText("عن التطبيق")
        assertDp(1168.dp, about.right, "التطبيق in the start column")
        assertDp(612.dp, about.left, "the start column is 556 wide")
        val support = rule.boundsOfText("الدعم والتواصل")
        assertDp(588.dp, support.right, "الدعم والسياسات in the end column")
        assertDp(32.dp, support.left, "the end column is 556 wide")
        // Both columns start 24 under the 56 top bar.
        val supportLabel = rule.boundsOfText("الدعم والسياسات")
        assertDp(32.dp + 56.dp + 24.dp, supportLabel.top, "end column starts 24 under the top bar")
    }

    @Test
    fun expanded_dark() {
        setProfile(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("profile-expanded-dark")
    }

    @Test
    fun medium_singleColumn() {
        setProfile(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("profile-medium-light")
        val about = rule.boundsOfText("عن التطبيق")
        assertDp(640.dp, about.widthDp, "cards 640 wide (Figma 64:3557)")
    }

    @Test
    fun medium_dark() {
        setProfile(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("profile-medium-dark")
    }

    @Test
    fun compact_isThePhoneProfile() {
        setProfile(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("profile-compact-light")
        val about = rule.boundsOfText("عن التطبيق")
        assertDp(328.dp, about.widthDp, "360 - 2 × 16")
    }
}

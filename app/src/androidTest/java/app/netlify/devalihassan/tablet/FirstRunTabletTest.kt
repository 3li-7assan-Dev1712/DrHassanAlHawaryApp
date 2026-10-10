package app.netlify.devalihassan.tablet

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.UpdateScreen
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.auth.presentation.auth.AuthScreenContent
import com.example.feature.auth.presentation.AuthUiState
import com.example.feature.onboarding.presentation.OnboardingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-run and system screens (Figma Welcome `61:3812` / `64:6796`, Onboarding `61:3845` /
 * `64:6832`, Update `61:3881` / `61:3910` / `64:6873` / `64:6902`): no rail, a centred
 * 520 column, the actions at the bottom.
 */
@RunWith(AndroidJUnit4::class)
class FirstRunTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun set(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean = false, content: @Composable () -> Unit) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme, showRail = false, margin = false) { content() }
    }

    @Test
    fun welcome_expanded_520Column_buttonAtTheBottom() {
        set(ExpandedWindow, AdaptiveLayoutTokens.Expanded) { AuthScreenContent(state = AuthUiState.Idle, onGoogleClick = {}) }
        rule.saveShellShot("welcome-expanded-light")
        val google = rule.boundsOfText("المتابعة باستخدام Google")
        assertDp(488.dp, google.widthDp, "the Google button: 520 - 2 × 16")
        assertDp(640.dp, (google.left + google.right) / 2, "centred")
    }

    @Test
    fun welcome_medium_dark() {
        set(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true) { AuthScreenContent(state = AuthUiState.Idle, onGoogleClick = {}) }
        rule.saveShellShot("welcome-medium-dark")
    }

    @Test
    fun onboarding_expanded_520Column() {
        set(ExpandedWindow, AdaptiveLayoutTokens.Expanded) { OnboardingScreen(onFinished = {}) }
        rule.saveShellShot("onboarding-expanded-light")
        val skip = rule.boundsOfText("تخطي")
        assertDp(640.dp + 260.dp - 16.dp, skip.right, "skip at the start edge of the 520 column")
    }

    @Test
    fun onboarding_medium_dark() {
        set(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true) { OnboardingScreen(onFinished = {}) }
        rule.saveShellShot("onboarding-medium-dark")
    }

    @Test
    fun update_expanded_actionsAtTheBottom() {
        set(ExpandedWindow, AdaptiveLayoutTokens.Expanded) { UpdateScreen(updateUrl = "", isForceUpdate = false) }
        rule.saveShellShot("update-expanded-light")
        val later = rule.boundsOfText("ليس الآن")
        assertDp(800.dp - 24.dp, later.bottom, "the last action 24 above the bottom")
        val update = rule.boundsOfText("تحديث الآن")
        assertDp(488.dp, update.widthDp, "520 - 2 × 16")
    }

    @Test
    fun update_required_medium_dark() {
        set(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true) { UpdateScreen(updateUrl = "", isForceUpdate = true) }
        rule.saveShellShot("update-required-medium-dark")
    }
}

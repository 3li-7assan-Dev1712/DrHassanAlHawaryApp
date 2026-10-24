package app.netlify.devalihassan.tablet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.MaintenanceScreen
import com.example.core.ui.components.UpdateScreen
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.auth.presentation.auth.AuthScreenContent
import com.example.feature.auth.presentation.AuthUiState
import com.example.feature.onboarding.presentation.OnboardingScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-run and system screens (Figma Welcome `61:3812` / `64:6796`, Onboarding `61:3845` /
 * `64:6832`, Update `61:3881` / `61:3910` / `64:6873` / `64:6902`, Maintenance phone `36:1450`,
 * `61:3935` / `64:6927`): no rail, a centred 520 column, the actions at the bottom.
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

    private fun setMaintenance(
        window: DpSize,
        tokens: AdaptiveLayoutTokens,
        darkTheme: Boolean = false,
        message: String = "",
        isRetrying: Boolean = false,
        onRetry: () -> Unit = {},
    ) = set(window, tokens, darkTheme) {
        // No system bars, as in the Figma frames (on a device the actions sit above the navigation bar).
        MaintenanceScreen(message = message, isRetrying = isRetrying, onRetry = onRetry, windowInsets = WindowInsets(0, 0, 0, 0))
    }

    @Test
    fun maintenance_expanded_520Column_actionsAtTheBottom() {
        setMaintenance(ExpandedWindow, AdaptiveLayoutTokens.Expanded)
        rule.saveShellShot("maintenance-expanded-light")
        val retry = rule.boundsOfText(MaintenanceRetry)
        assertDp(488.dp, retry.widthDp, "Button/Secondary: 520 - 2 × 16")
        assertDp(48.dp, retry.heightDp, "button height")
        assertDp(640.dp, (retry.left + retry.right) / 2, "centred")
        val telegram = rule.boundsOfText(MaintenanceTelegram)
        assertDp(48.dp, telegram.heightDp, "text button height")
        assertDp(800.dp - 24.dp, telegram.bottom, "the last action 24 above the bottom")
        assertDp(retry.bottom + 4.dp, telegram.top, "4 between the actions")
        val title = rule.boundsOfText(MaintenanceTitle)
        assertDp(640.dp, (title.left + title.right) / 2, "title centred")
        rule.onNodeWithText(MaintenanceBody).assertIsDisplayed()
        rule.onNodeWithText(MaintenanceApology).assertIsDisplayed()
    }

    @Test
    fun maintenance_expanded_dark() {
        setMaintenance(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("maintenance-expanded-dark")
    }

    @Test
    fun maintenance_medium_light_actionsAtTheBottom() {
        setMaintenance(MediumWindow, AdaptiveLayoutTokens.Medium)
        rule.saveShellShot("maintenance-medium-light")
        assertDp(1280.dp - 24.dp, rule.boundsOfText(MaintenanceTelegram).bottom, "the last action 24 above the bottom")
        assertDp(488.dp, rule.boundsOfText(MaintenanceRetry).widthDp, "520 - 2 × 16")
    }

    @Test
    fun maintenance_medium_dark() {
        setMaintenance(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("maintenance-medium-dark")
    }

    @Test
    fun maintenance_compact_fullWidth() {
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact)
        rule.saveShellShot("maintenance-compact-light")
        val retry = rule.boundsOfText(MaintenanceRetry)
        assertDp(328.dp, retry.widthDp, "360 - 2 × 16")
        assertDp(16.dp, retry.left, "16 from the edge")
        assertDp(800.dp - 24.dp, rule.boundsOfText(MaintenanceTelegram).bottom, "24 above the bottom")
    }

    @Test
    fun maintenance_compact_dark() {
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = true)
        rule.saveShellShot("maintenance-compact-dark")
    }

    @Test
    fun maintenance_adminMessage_replacesTheDefaultText() {
        val message = "نحدّث قاعدة البيانات، ونعود بإذن الله الساعة التاسعة مساءً."
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact, message = message)
        rule.onNodeWithText(message).assertIsDisplayed()
        rule.onNodeWithText(MaintenanceTitle).assertIsDisplayed()
        assertEquals(0, rule.onAllNodesWithTextCount(MaintenanceBody))
        assertEquals(0, rule.onAllNodesWithTextCount(MaintenanceApology))
    }

    @Test
    fun maintenance_retry_callsBack_andShowsChecking() {
        var retries = 0
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact, onRetry = { retries++ })
        rule.onNodeWithText(MaintenanceRetry).performClick()
        assertEquals(1, retries)
    }

    @Test
    fun maintenance_retrying_isDisabled() {
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact, isRetrying = true)
        rule.onNodeWithText("جارٍ التحقق…").assertIsNotEnabled()
    }

    @Test
    fun maintenance_longMessage_scrollsToTheActions() {
        val message = List(40) { "سطر طويل من رسالة الصيانة رقم $it." }.joinToString("\n")
        setMaintenance(CompactWindow, AdaptiveLayoutTokens.Compact, message = message)
        rule.saveShellShot("maintenance-compact-long-message")
        rule.onNodeWithText(MaintenanceTelegram).performScrollTo().assertIsDisplayed()
    }
}

private const val MaintenanceTitle = "التطبيق قيد الصيانة"
private const val MaintenanceBody = "نجري حاليًا أعمال صيانة وتحسين لنقدّم لك تجربة أفضل، وسيعود التطبيق للعمل قريبًا بإذن الله."
private const val MaintenanceApology = "نعتذر عن هذا الانقطاع، ونشكر لك صبرك وتفهّمك."
private const val MaintenanceRetry = "إعادة المحاولة"
private const val MaintenanceTelegram = "تابع المستجدات على قناتنا في تيليجرام"

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
    onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().size

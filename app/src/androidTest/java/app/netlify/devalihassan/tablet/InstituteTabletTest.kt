package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.domain.module.Student
import com.example.study.presentation.dashboard.InstituteComingSoonContent
import com.example.study.presentation.dashboard.InstituteComingSoonTwoPane
import com.example.study.presentation.dashboard.InstitutePreviewStudent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Institute (Figma `61:2211` / `61:2458` Expanded). Medium is the phone layout. */
@RunWith(AndroidJUnit4::class)
class InstituteTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setInstitute(darkTheme: Boolean, student: Student? = InstitutePreviewStudent) {
        rule.setShellContent(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = darkTheme, selectedTab = PreviewTab.Institute) {
            InstituteComingSoonTwoPane(studentData = student, title = "معهد الشيخ حسن الهواري الفقهي")
        }
    }

    @Test
    fun expanded_profilePane_comingSoonCentred() {
        setInstitute(darkTheme = false)
        rule.saveShellShot("institute-expanded-light")
        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val title = rule.boundsOfText("منصة المعهد قريبًا")
        assertDp((detail.left + detail.right) / 2, (title.left + title.right) / 2, "coming-soon card centred")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val name = rule.boundsOfText(InstitutePreviewStudent.name)
        assertTrue("profile card in the start pane", name.right < list.right && name.left > list.left)
    }

    @Test
    fun expanded_dark() {
        setInstitute(darkTheme = true)
        rule.saveShellShot("institute-expanded-dark")
    }

    @Test
    fun expanded_guest_comingSoonWithoutProfile() {
        setInstitute(darkTheme = false, student = null)
        rule.saveShellShot("institute-expanded-guest-light")
        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val title = rule.boundsOfText("منصة المعهد قريبًا")
        assertDp((detail.left + detail.right) / 2, (title.left + title.right) / 2, "coming-soon card centred")
        rule.onNodeWithText(InstitutePreviewStudent.name).assertDoesNotExist()
        rule.onNodeWithText("تم ربط حسابك بقناة المعهد على تيليجرام").assertDoesNotExist()
    }

    @Test
    fun compact_guest_comingSoonWithoutProfile() {
        rule.setShellContent(CompactWindow, AdaptiveLayoutTokens.Compact, selectedTab = PreviewTab.Institute) {
            InstituteComingSoonContent(studentData = null)
        }
        rule.saveShellShot("institute-compact-guest-light")
        val title = rule.boundsOfText("منصة المعهد قريبًا")
        assertDp(CompactWindow.width / 2, (title.left + title.right) / 2, "coming-soon card centred")
        assertDp(16.dp + 24.dp + 52.dp + 14.dp, title.top, "card first: list padding, card padding, icon, gap")
        rule.onNodeWithText(InstitutePreviewStudent.name).assertDoesNotExist()
    }

}

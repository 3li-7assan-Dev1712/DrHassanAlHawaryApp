package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.theme.AdaptiveLayoutTokens
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

    private fun setInstitute(darkTheme: Boolean) {
        rule.setShellContent(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = darkTheme, selectedTab = PreviewTab.Institute) {
            InstituteComingSoonTwoPane(studentData = InstitutePreviewStudent, title = "معهد الشيخ حسن الهواري الفقهي")
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

}

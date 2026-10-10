package app.netlify.devalihassan.tablet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.home.presentation.HomePreviewState
import com.example.feature.home.presentation.HomeScreenContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Home (Figma `56:192` / `56:414` Expanded, `63:2756` / `63:2969` Medium). */
@RunWith(AndroidJUnit4::class)
class HomeTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setHome(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            HomeScreenContent(
                uiState = HomePreviewState,
                onNavigateToDetailArticle = {},
                onNavigateToDetailAudio = { _, _ -> },
                onCategoryClick = {},
            )
        }
    }

    @Test
    fun expanded_headerOverTwoColumns() {
        setHome(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("home-expanded-light")

        // Content: 32..1168 (rail at 1200). Featured column (start = right) 612..1168,
        // latest column 32..588: two 556 columns, 24 apart.
        val tile = rule.boundsOfText("المقالات")
        assertDp(180.dp, tile.widthDp, "tiles fill the column: (556 - 2 × 8) / 3")
        assertDp(1168.dp, tile.right, "the first tile at the column's start edge")
        val lastTile = rule.boundsOfText("عن الشيخ")
        assertDp(612.dp, lastTile.left, "tiles span the 556 column")

        val article = rule.boundsOfText(HomePreviewState.latestArticles[0].title)
        assertDp(266.dp, article.widthDp, "two cards across: (556 - 16 - 8) / 2")
        assertDp(588.dp - 16.dp, article.right, "16 start inset in the latest column")
        val second = rule.boundsOfText(HomePreviewState.latestArticles[1].title)
        assertDp(article.left - 8.dp, second.right, "8 between the cards")
        assertDp(32.dp, second.left, "the second card reaches the column's end edge")

        val audio = rule.boundsOfText(HomePreviewState.latestAudios[0].displayTitle)
        assertDp(266.dp, audio.widthDp, "audio cards two across too")
    }

    @Test
    fun expanded_dark() {
        setHome(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("home-expanded-dark")
    }

    @Test
    fun medium_oneColumn() {
        setHome(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("home-medium-light")
        // Content 24..696 (rail at 720).
        val tile = rule.boundsOfText("المقالات")
        assertDp(218.67.dp, tile.widthDp, "tiles fill the width: (672 - 2 × 8) / 3")
        assertDp(696.dp, tile.right, "tiles at the content's edge")
        val article = rule.boundsOfText(HomePreviewState.latestArticles[0].title)
        assertDp(324.dp, article.widthDp, "two cards across: (672 - 16 - 8) / 2")
        assertDp(696.dp - 16.dp, article.right, "16 start inset")
    }

    @Test
    fun medium_dark() {
        setHome(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("home-medium-dark")
    }

    @Test
    fun compact_isThePhoneHome() {
        setHome(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("home-compact-light")
        val tile = rule.boundsOfText("المقالات")
        assertDp(344.dp, tile.right, "phone grid: 16 from the edge")
        assertDp(104.dp, tile.widthDp, "(360 - 2 × 16 - 2 × 8) / 3")
    }
}

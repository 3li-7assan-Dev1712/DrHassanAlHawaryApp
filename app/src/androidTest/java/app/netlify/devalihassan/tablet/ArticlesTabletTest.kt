package app.netlify.devalihassan.tablet

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.domain.text.ArticleTextCleaner
import com.example.feature.article.presentation.detail.ArticleDetailContent
import com.example.feature.article.presentation.detail.ArticleReaderPaneContent
import com.example.feature.article.presentation.detail.ReaderPreviewState
import com.example.feature.article.presentation.list.ArticlesAdaptiveContent
import com.example.feature.article.presentation.list.ArticlesPreviewData
import com.example.feature.article.presentation.list.articlesPreviewPagingData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Articles and Reader (Figma `53:2` / `53:170` Expanded, `63:2412` / `63:2490` Medium
 * articles, `63:2569` / `63:2662` Medium reader).
 */
@RunWith(AndroidJUnit4::class)
class ArticlesTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setArticles(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            val articles = remember { articlesPreviewPagingData() }.collectAsLazyPagingItems()
            ArticlesAdaptiveContent(
                articles = articles,
                selectedArticleId = null,
                onSelectArticle = {},
                onNavigateToArticleDetail = {},
                onNavigateBack = {},
                readerPane = { ArticleReaderPaneContent(ReaderPreviewState, fontStep = 1, onFontStepChange = {}, onShare = {}) },
            )
        }
    }

    private fun setReader(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            ArticleDetailContent(ReaderPreviewState, fontStep = 1, onFontStepChange = {}, onNavigateBack = {}, onShare = {})
        }
    }

    @Test
    fun expanded_listPaneAndReaderPane() {
        setArticles(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("articles-expanded-light")

        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val first = rule.boundsOfText(ArticlesPreviewData[0].title)
        assertDp(366.dp, first.widthDp, "items fill the list pane: 400 - 2 × 1 outline - 2 × 16")
        assertDp(list.right - 17.dp, first.right, "16 padding inside the outline")
        assertDp(list.top + 1.dp + 56.dp + 16.dp, first.top, "below the 56dp top bar, 16 padding")
        val second = rule.boundsOfText(ArticlesPreviewData[1].title)
        assertDp(first.bottom + 12.dp, second.top, "12 between items")

        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val ornament = rule.boundsOfText(ArticleTextCleaner.ORNAMENT)
        assertDp(detail.left + 1.dp + 32.dp, ornament.left, "article body: 32 padding")
        assertDp(detail.right - 1.dp - 32.dp, ornament.right, "article body: 32 padding")
        assertDp(646.dp, ornament.widthDp, "text column 710 - 2 × 32")
    }

    @Test
    fun expanded_dark() {
        setArticles(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("articles-expanded-dark")
    }

    @Test
    fun medium_singleList() {
        setArticles(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("articles-medium-light")
        val first = rule.boundsOfText(ArticlesPreviewData[0].title)
        // Content 672 wide at 24 from the left; the phone list's 16 padding inside it.
        assertDp(24.dp + 16.dp, first.left, "list padding")
        assertDp(640.dp, first.widthDp, "items fill: 672 - 2 × 16")
        assertDp(24.dp + 56.dp + 16.dp, first.top, "margin, top bar, padding")
    }

    @Test
    fun medium_dark() {
        setArticles(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("articles-medium-dark")
    }

    @Test
    fun medium_reader_textColumnIs640Centred() {
        setReader(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("reader-medium-light")
        val ornament = rule.boundsOfText(ArticleTextCleaner.ORNAMENT)
        // ArticleBody 640 (16 padding inside) centred in the 672 content: 16 + 16 from its left.
        assertDp(24.dp + 16.dp + 16.dp, ornament.left, "centred 640 column, 16 padding")
        assertDp(608.dp, ornament.widthDp, "640 - 2 × 16")
    }

    @Test
    fun medium_reader_dark() {
        setReader(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("reader-medium-dark")
    }

    @Test
    fun compact_isThePhoneList() {
        setArticles(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("articles-compact-light")
        val first = rule.boundsOfText(ArticlesPreviewData[0].title)
        assertDp(16.dp, first.left, "phone padding")
        assertDp(328.dp, first.widthDp, "360 - 2 × 16")
    }

    @Test
    fun compact_reader_isThePhoneReader() {
        setReader(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = true)
        rule.saveShellShot("reader-compact-dark")
        val ornament = rule.boundsOfText(ArticleTextCleaner.ORNAMENT)
        assertDp(16.dp, ornament.left, "phone padding")
        assertDp(328.dp, ornament.widthDp, "360 - 2 × 16")
    }
}

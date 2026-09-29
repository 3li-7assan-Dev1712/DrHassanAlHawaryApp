package com.example.feature.article.presentation.share

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import com.example.domain.text.SelectableArticle
import com.example.domain.text.quoteCounter
import com.example.feature.article.presentation.share.components.SelectableArticleText
import com.example.feature.share.presentation.components.QuoteCardPreview

/**
 * Pick the text for the quote images: long-press on the cleaned article to select the
 * word there, keep dragging to select freely (across paragraphs, auto-scrolling at the
 * edges), then drag the handles to adjust. Hands the article id + the selection's range
 * in the display text to feature-share's quote-image screen (wired by the app module).
 */
@Composable
fun ArticleShareSelectionScreen(
    onNavigateUp: () -> Unit,
    onContinueToPreview: (articleId: String, selectionStart: Int, selectionEnd: Int) -> Unit,
    viewModel: ArticleShareSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleShareSelectionContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onSelectionChanged = viewModel::onSelectionChanged,
        onClearSelection = viewModel::onClearSelection,
        onContinue = {
            uiState.selectionRange?.let { onContinueToPreview(uiState.articleId, it.first, it.last + 1) }
        },
    )
}

@Composable
private fun ArticleShareSelectionContent(
    uiState: ArticleShareSelectionUiState,
    onNavigateUp: () -> Unit,
    onSelectionChanged: (start: Int, end: Int) -> Unit,
    onClearSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = Brand.colors
    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.share_text_selection_title), onBack = onNavigateUp) },
        bottomBar = { SelectionPanel(uiState, onClearSelection, onContinue) },
    ) { padding ->
        val article = uiState.article
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.accentStrong)

                article == null || article.text.isBlank() -> Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                )

                else -> SelectableBody(uiState, article, onSelectionChanged)
            }
        }
    }
}

@Composable
private fun SelectableBody(
    uiState: ArticleShareSelectionUiState,
    article: SelectableArticle,
    onSelectionChanged: (start: Int, end: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    var viewport by remember { mutableStateOf<Rect?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { viewport = it.boundsInWindow() }
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ArticleHeader(uiState.articleTitle)
        SelectableArticleText(
            article = article,
            selectionStart = uiState.selectionStart,
            selectionEnd = uiState.selectionEnd,
            onSelectionChanged = onSelectionChanged,
            scrollState = scrollState,
            viewport = { viewport },
            selectAllLabel = stringResource(R.string.share_text_select_all),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )
    }
}

/** "من مقال" + the cleaned title (not selectable), a hairline, then the hint. */
@Composable
private fun ArticleHeader(title: String) {
    val colors = Brand.colors
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.share_quote_source_label), fontSize = 11.sp, color = colors.textMuted)
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            lineHeight = 1.5.em,
        )
        HorizontalDivider(Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = colors.divider)
        Text(
            text = stringResource(R.string.share_text_selection_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}

/**
 * A hairline, then: the live thumbnail of the first image, the counter
 * ("٩٨ حرفًا · صورة واحدة") and "مسح"; then "متابعة".
 */
@Composable
private fun SelectionPanel(
    uiState: ArticleShareSelectionUiState,
    onClearSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = Brand.colors
    Column(Modifier.background(colors.background)) {
        HorizontalDivider(thickness = 0.5.dp, color = colors.divider)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Thumbnail(uiState)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    val counter = when {
                        !uiState.hasSelection -> stringResource(R.string.share_text_no_selection)
                        uiState.pageCount == 0 -> "…"
                        else -> quoteCounter(uiState.characterCount, uiState.pageCount)
                    }
                    val reduced = reducedMotion
                    // The counter slides to its new value: up as the selection grows, down as
                    // it shrinks.
                    AnimatedContent(
                        targetState = counter to uiState.characterCount,
                        transitionSpec = {
                            Motion.countSlide(reduced, up = targetState.second >= initialState.second)
                        },
                        contentAlignment = Alignment.CenterStart,
                        label = "quoteCounter",
                    ) { (text, _) ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.hasSelection) colors.textPrimary else colors.textMuted,
                        )
                    }
                    if (uiState.isTooLong) {
                        Text(
                            text = stringResource(R.string.share_text_too_long),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.danger,
                        )
                    }
                }
                if (uiState.hasSelection) {
                    TextButton(onClick = onClearSelection) {
                        Text(stringResource(R.string.share_text_clear), color = colors.accentText)
                    }
                }
            }
            Button(
                onClick = onContinue,
                enabled = uiState.canContinue,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentStrong, contentColor = colors.onGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(50.dp),
            ) {
                Text(stringResource(R.string.share_text_continue), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** ~36×64dp: the real first quote image (same renderer as the share step), or a placeholder. */
@Composable
private fun Thumbnail(uiState: ArticleShareSelectionUiState) {
    val shape = RoundedCornerShape(6.dp)
    val page = uiState.firstPage
    if (page != null && uiState.hasSelection) {
        QuoteCardPreview(page = page, modifier = Modifier.height(64.dp).clip(shape))
    } else {
        // The images are always the dark brand card with a gold quote mark.
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 64.dp)
                .background(BrandTokens.background, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text("❝", color = BrandTokens.gold, fontSize = 18.sp)
        }
    }
}

private val previewContent = listOf(
    "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير",
    "الشيخ د.حسن أحمد الهواري • September 23 at 9:55 PM",
    "📝 خدمة المقالات والمقتطفات 📝",
    "═══════✿✿✿═══════",
    "بسم الله الرحمن الرحيم",
    "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
    "فإن بلادنا تعيش أزمة اقتصادية طاحنة. وأسبابها جلية واضحة للجميع، فإلى الله المشتكى.",
    "▪ أولًا: المظاهر",
    "١. هبوط فظيع في قيمة العملة الوطنية. ٢. غلاء الأسعار في كل السلع والخدمات.",
).joinToString("\n")

@Composable
private fun SelectionPreview() {
    val article = SelectableArticle.build(previewContent, "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير")
    val start = article.text.indexOf("فإن بلادنا")
    ArticleShareSelectionContent(
        uiState = ArticleShareSelectionUiState(
            isLoading = false,
            articleTitle = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
            article = article,
            selectionStart = start,
            selectionEnd = start + 60,
            characterCount = 60,
            pageCount = 1,
        ),
        onNavigateUp = {},
        onSelectionChanged = { _, _ -> },
        onClearSelection = {},
        onContinue = {},
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "Quote selection - light", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun SelectionLightPreview() {
    com.example.core.ui.theme.HassanAlHawaryTheme(darkTheme = false) { SelectionPreview() }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Quote selection - dark", locale = "ar", widthDp = 360, heightDp = 720)
@Composable
private fun SelectionDarkPreview() {
    com.example.core.ui.theme.HassanAlHawaryTheme(darkTheme = true) { SelectionPreview() }
}

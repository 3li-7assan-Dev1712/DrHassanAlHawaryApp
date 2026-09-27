package com.example.feature.article.presentation.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.BrandTokens
import com.example.domain.text.ArticleTextCleaner
import com.example.domain.text.QuoteBlock
import com.example.domain.text.QuoteSentence
import com.example.domain.text.SentenceSelection
import com.example.domain.text.quoteCounter
import com.example.feature.share.presentation.components.QuoteCardPreview

/**
 * Pick the text for the quote images by tapping whole sentences of the cleaned article:
 * tap one to select it, tap the one just before or after to extend, tap an end to trim
 * it, tap anywhere else to start over. Hands the article id + the selection's range in
 * the display text to feature-share's quote-image screen (wired by the app module).
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
        onSentenceTapped = viewModel::onSentenceTapped,
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
    onSentenceTapped: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = Brand.colors
    val haptics = LocalHapticFeedback.current
    val onTap: (Int) -> Unit = { index ->
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSentenceTapped(index)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.share_text_selection_title), onBack = onNavigateUp) },
        bottomBar = { SelectionPanel(uiState, onClearSelection, onContinue) },
    ) { padding ->
        val document = uiState.document
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.accentStrong)

                document == null || document.sentences.isEmpty() -> Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    item { ArticleHeader(uiState.articleTitle) }
                    items(document.blocks) { block ->
                        when (block) {
                            QuoteBlock.Ornament -> Text(
                                text = ArticleTextCleaner.ORNAMENT,
                                color = colors.accent,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )

                            is QuoteBlock.Paragraph -> SentenceParagraph(block, uiState.selection, onTap)
                        }
                    }
                }
            }
        }
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
 * One paragraph as a single Text, each sentence a clickable link (so TalkBack can reach
 * and activate every sentence), selected ones with a background span that hugs each line.
 */
@Composable
private fun SentenceParagraph(block: QuoteBlock.Paragraph, selection: SentenceSelection?, onTap: (Int) -> Unit) {
    val colors = Brand.colors
    val baseStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 1.9.em)
    val selected = SpanStyle(background = colors.accentContainer, color = colors.onAccentContainer)
    val text = remember(block, selection, colors) { annotate(block.sentences, selection, selected, onTap) }

    when (block.kind) {
        QuoteBlock.Kind.Basmala -> Text(
            text = text,
            style = baseStyle.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        QuoteBlock.Kind.Heading -> Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(colors.accentStrong, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = baseStyle.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
        }

        // Start-aligned, never justified (Android justifies Arabic by stretching spaces).
        QuoteBlock.Kind.Body -> Text(
            text = text,
            style = baseStyle,
            color = colors.textSecondary,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 14.dp),
        )
    }
}

private fun annotate(
    sentences: List<QuoteSentence>,
    selection: SentenceSelection?,
    selectedStyle: SpanStyle,
    onTap: (Int) -> Unit,
): AnnotatedString = buildAnnotatedString {
    fun isSelected(i: Int) = selection != null && i in selection.first..selection.last
    sentences.forEachIndexed { position, sentence ->
        if (position > 0) {
            // The space between two selected sentences is highlighted too: one unbroken run.
            val bothSelected = isSelected(sentences[position - 1].index) && isSelected(sentence.index)
            if (bothSelected) withStyle(selectedStyle) { append(" ") } else append(" ")
        }
        val link = LinkAnnotation.Clickable(
            tag = "sentence-${sentence.index}",
            styles = TextLinkStyles(style = if (isSelected(sentence.index)) selectedStyle else SpanStyle()),
        ) { onTap(sentence.index) }
        withLink(link) { append(sentence.text) }
    }
}

private inline fun AnnotatedString.Builder.withStyle(style: SpanStyle, block: AnnotatedString.Builder.() -> Unit) {
    val index = pushStyle(style)
    try {
        block()
    } finally {
        pop(index)
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
                    Text(
                        text = when {
                            !uiState.hasSelection -> stringResource(R.string.share_text_no_selection)
                            uiState.pageCount == 0 -> "…"
                            else -> quoteCounter(uiState.characterCount, uiState.pageCount)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.hasSelection) colors.textPrimary else colors.textMuted,
                    )
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
    val document = com.example.domain.text.QuoteDocument.build(previewContent, "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير")
    ArticleShareSelectionContent(
        uiState = ArticleShareSelectionUiState(
            isLoading = false,
            articleTitle = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
            document = document,
            selection = SentenceSelection(2, 3),
            characterCount = 98,
            pageCount = 1,
        ),
        onNavigateUp = {},
        onSentenceTapped = {},
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

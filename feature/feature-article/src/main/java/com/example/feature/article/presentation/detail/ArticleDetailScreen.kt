package com.example.feature.article.presentation.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.AppTopBarAction
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import com.example.domain.module.Article
import com.example.domain.text.ArabicDates
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.ArticleTextCleaner
import com.example.domain.text.InlineBold
import java.util.Date

/** Reader text sizes; index 1 is the size the reader always used (bodyLarge, 16sp). */
private val FONT_STEPS = listOf(14.sp, 16.sp, 18.sp, 21.sp)

private val paragraphCleaner = ArticleTextCleaner()

@Composable
fun ArticleDetailScreen(
    viewModel: DetailArticleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToShareSelection: (articleId: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val fontStep by viewModel.fontStep.collectAsState()

    ArticleDetailContent(
        uiState = uiState,
        fontStep = fontStep,
        onFontStepChange = viewModel::setFontStep,
        onNavigateBack = onNavigateBack,
        onShare = onNavigateToShareSelection,
    )
}

@Composable
private fun ArticleDetailContent(
    uiState: DetailArticleUiState,
    fontStep: Int,
    onFontStepChange: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onShare: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val colors = Brand.colors

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column {
                // No title here: the header below shows it in full. Share lives in the bar
                // instead of a floating button over the text.
                AppTopBar(
                    title = "",
                    onBack = onNavigateBack,
                    actions = {
                        if (uiState is DetailArticleUiState.Success) {
                            FontSizeAction(fontStep = fontStep, onFontStepChange = onFontStepChange)
                            AppTopBarAction(
                                icon = TablerIcons.Share,
                                contentDescription = stringResource(R.string.share),
                                onClick = { onShare(uiState.article.id) },
                            )
                        }
                    },
                )
                ReadingProgress(scrollState)
            }
        },
    ) { innerPadding ->
        val reduced = reducedMotion
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { Motion.contentSwap(reduced) },
            contentKey = { it::class },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "readerContent",
        ) { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is DetailArticleUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colors.accentStrong)
                    }

                    is DetailArticleUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                        )
                    }

                    is DetailArticleUiState.Success -> {
                        ArticleBody(
                            article = state.article,
                            paragraphs = state.paragraphs,
                            readingMinutes = state.readingMinutes,
                            fontSize = FONT_STEPS[fontStep.coerceIn(0, FONT_STEPS.lastIndex)],
                            scrollState = scrollState,
                        )
                    }
                }
            }
        }
    }
}

/** A thin accentStrong bar under the top bar: how far the reader has scrolled. */
@Composable
private fun ReadingProgress(scrollState: ScrollState) {
    val progress by remember(scrollState) {
        derivedStateOf {
            if (scrollState.maxValue <= 0 || scrollState.maxValue == Int.MAX_VALUE) 0f
            else scrollState.value.toFloat() / scrollState.maxValue
        }
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp),
        color = Brand.colors.accentStrong,
        trackColor = Brand.colors.divider,
        strokeCap = StrokeCap.Butt,
    )
}

@Composable
private fun FontSizeAction(fontStep: Int, onFontStepChange: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val colors = Brand.colors
    Box {
        AppTopBarAction(
            icon = TablerIcons.TextSize,
            contentDescription = stringResource(R.string.reader_font_size),
            onClick = { open = true },
        )
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier.background(colors.surface),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // "A−" makes the text smaller, "A+" larger; each is disabled at its end.
                    OutlinedButton(
                        onClick = { onFontStepChange(fontStep - 1) },
                        enabled = fontStep > 0,
                    ) { Text("A−", color = colors.textPrimary) }
                    Text(
                        text = ArabicNumerals.digits(fontStep + 1),
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    OutlinedButton(
                        onClick = { onFontStepChange(fontStep + 1) },
                        enabled = fontStep < DetailArticleViewModel.MAX_FONT_STEP,
                    ) { Text("A+", color = colors.textPrimary) }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.reader_font_preview),
                    fontSize = FONT_STEPS[fontStep.coerceIn(0, FONT_STEPS.lastIndex)],
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ArticleBody(
    article: Article,
    paragraphs: List<String>,
    readingMinutes: Int,
    fontSize: TextUnit,
    scrollState: ScrollState,
) {
    val colors = Brand.colors
    // The cleaner turned the first " - " of the title into ": ", so split there.
    val (title, subtitle) = remember(article.title) {
        val i = article.title.indexOf(':')
        if (i in 1 until article.title.lastIndex) {
            article.title.substring(0, i).trim() to article.title.substring(i + 1).trim()
        } else {
            article.title to null
        }
    }
    val meta = listOf(
        stringResource(R.string.sheikh_name),
        ArabicDates.calendarDate(article.publishDate.time),
        ArabicDates.readingTime(readingMinutes),
    ).joinToString(ArabicNumerals.DATE_SEPARATOR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 1.5.em),
            color = colors.textPrimary,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 1.5.em),
                color = colors.accentText,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = meta, style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
        Spacer(Modifier.height(20.dp))

        paragraphs.forEachIndexed { index, paragraph ->
            ReaderParagraph(paragraph = paragraph, isFirst = index == 0, fontSize = fontSize)
        }
    }
}

@Composable
private fun ReaderParagraph(paragraph: String, isFirst: Boolean, fontSize: TextUnit) {
    val colors = Brand.colors
    val bodyStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize, lineHeight = 1.8.em)
    val heading = remember(paragraph) { paragraphCleaner.sectionHeading(paragraph) }
    when {
        paragraph == ArticleTextCleaner.ORNAMENT -> Text(
            text = paragraph,
            color = colors.accent,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        )

        isFirst && paragraphCleaner.isBasmala(paragraph) -> Text(
            text = paragraph,
            style = bodyStyle.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )

        heading != null -> Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(colors.accentStrong, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = remember(heading) { boldAnnotated(heading) },
                style = bodyStyle.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
        }

        // Start-aligned (right in RTL), never justified: Android justifies Arabic by
        // stretching the spaces between words.
        else -> Text(
            text = remember(paragraph) { boldAnnotated(paragraph) },
            style = bodyStyle,
            color = colors.textSecondary,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

/** WhatsApp-style `*bold*` markers as bold spans. */
private fun boldAnnotated(text: String): AnnotatedString {
    val parsed = InlineBold.parse(text)
    return buildAnnotatedString {
        append(parsed.text)
        parsed.bold.forEach { range ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), range.first, range.last + 1)
        }
    }
}

private val previewState = DetailArticleUiState.Success(
    article = Article(
        id = "preview",
        title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
        publishDate = Date(),
        content = "",
    ),
    paragraphs = listOf(
        "بسم الله الرحمن الرحيم",
        "الحمد لله، وصلى الله وسلم على رسول الله، وبعد:",
        "فإن بلادنا تعيش أزمة *اقتصادية* طاحنة، وأسبابها جلية واضحة.",
        ArticleTextCleaner.ORNAMENT,
        "▪ أولًا: المظاهر",
        "غلاء الأسعار وتدهور العملة.",
    ),
    readingMinutes = 5,
)

@Preview(name = "Reader - light", locale = "ar", widthDp = 360, heightDp = 640)
@Composable
private fun ArticleDetailLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        ArticleDetailContent(previewState, fontStep = 1, onFontStepChange = {}, onNavigateBack = {}, onShare = {})
    }
}

@Preview(name = "Reader - dark", locale = "ar", widthDp = 360, heightDp = 640)
@Composable
private fun ArticleDetailDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        ArticleDetailContent(previewState, fontStep = 1, onFontStepChange = {}, onNavigateBack = {}, onShare = {})
    }
}

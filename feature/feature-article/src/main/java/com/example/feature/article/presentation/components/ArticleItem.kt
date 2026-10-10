package com.example.feature.article.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.components.animatedListItemStyle
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Article
import com.example.domain.text.ArabicDates
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.ArticleTextCleaner
import java.util.Date

private val textCleaner = ArticleTextCleaner()

/**
 * One article in the list: the whole card opens the article. Cleaned title and excerpt
 * (display only, the stored text is untouched) and "نُشر منذ … · قراءة X دقائق".
 * [selected]: the article open in the reader pane beside the list (Expanded windows).
 */
@Composable
fun ArticleItem(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = Brand.colors
    val style = animatedListItemStyle(selected)
    val excerpt = remember(article.id, article.content) { textCleaner.excerpt(article.content, article.title) }
    val readingMinutes = remember(article.id, article.content) {
        if (article.content.isBlank()) null else textCleaner.readingMinutes(article.content)
    }
    val meta = listOfNotNull(
        ArabicDates.published(System.currentTimeMillis(), article.publishDate.time),
        readingMinutes?.let(ArabicDates::readingTime),
    ).joinToString(ArabicNumerals.DATE_SEPARATOR)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = style.fill,
        border = style.border,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 1.5.em),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = colors.textPrimary,
            )
            if (excerpt.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.6.em),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textSecondary,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(TablerIcons.Clock),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.textMuted,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = meta, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
            }
        }
    }
}

private val previewArticle = Article(
    id = "1",
    title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير",
    publishDate = Date(System.currentTimeMillis() - 5 * 3_600_000L),
    content = "بسم الله الرحمن الرحيم\nفإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى.",
)

@Preview(name = "Article card - light", locale = "ar", widthDp = 360)
@Composable
private fun ArticleItemLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { ArticleItem(previewArticle, onClick = {}, modifier = Modifier.padding(16.dp)) }
}

@Preview(name = "Article card - dark", locale = "ar", widthDp = 360)
@Composable
private fun ArticleItemDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { ArticleItem(previewArticle, onClick = {}, modifier = Modifier.padding(16.dp)) }
}

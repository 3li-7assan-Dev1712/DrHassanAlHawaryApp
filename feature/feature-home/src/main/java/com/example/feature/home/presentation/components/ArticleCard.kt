package com.example.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.domain.text.ArabicDates
import com.example.domain.text.ArabicNumerals
import com.example.feature.home.domain.model.ArticleFeed

/**
 * Compact, content-height article card: cleaned title (2 lines), first
 * meaningful paragraph (2 lines, hidden when there is none) and
 * "منذ ٣ أيام · قراءة ٧ دقائق". Size it from the row (e.g. 85% of its width).
 */
@Composable
fun ArticleCard(
    article: ArticleFeed,
    onClick: (articleId: String) -> Unit,
    modifier: Modifier = Modifier,
    nowMillis: Long = System.currentTimeMillis(),
) {
    val meta = remember(article, nowMillis) {
        listOfNotNull(
            article.publishedAt?.let { ArabicDates.relative(nowMillis, it) },
            ArabicDates.readingTime(article.readingMinutes),
        ).joinToString(ArabicNumerals.DATE_SEPARATOR)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brand.colors.surface)
            .clickable { onClick(article.id) }
            .padding(12.dp),
    ) {
        Text(
            text = ArabicNumerals.digits(article.title),
            color = Brand.colors.textPrimary,
            fontFamily = Cairo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (article.excerpt.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = ArabicNumerals.digits(article.excerpt),
                color = Brand.colors.textSecondary,
                fontFamily = Cairo,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = meta,
            color = Brand.colors.textMuted,
            fontFamily = Cairo,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

private val previewNow = 1_790_000_000_000L

@Preview(name = "Article card - very long title", widthDp = 360)
@Composable
private fun ArticleCardLongTitlePreview() {
    LazyRow(Modifier.background(Brand.colors.background).padding(vertical = 12.dp)) {
        item {
            ArticleCard(
                article = ArticleFeed(
                    id = "1",
                    title = "وجوب الاعتصام بالكتاب والسنة وفهم السلف الصالح والتحذير من البدع والمحدثات في الدين وأثر ذلك على الفرد والمجتمع",
                    excerpt = "فإن بلادنا تعيش أزمة اقتصادية طاحنة، وأسبابها جلية واضحة، فإلى الله المشتكى وإليه الملاذ والملتجى.",
                    publishedAt = previewNow - 3 * 86_400_000L,
                    readingMinutes = 7,
                ),
                onClick = {},
                nowMillis = previewNow,
                modifier = Modifier.fillParentMaxWidth(0.85f).padding(horizontal = 16.dp),
            )
        }
    }
}

@Preview(name = "Article card - no excerpt", widthDp = 360)
@Composable
private fun ArticleCardNoExcerptPreview() {
    LazyRow(Modifier.background(Brand.colors.background).padding(vertical = 12.dp)) {
        item {
            ArticleCard(
                article = ArticleFeed(id = "2", title = "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير", excerpt = "", publishedAt = null, readingMinutes = 1),
                onClick = {},
                nowMillis = previewNow,
                modifier = Modifier.width(300.dp).padding(horizontal = 16.dp),
            )
        }
    }
}

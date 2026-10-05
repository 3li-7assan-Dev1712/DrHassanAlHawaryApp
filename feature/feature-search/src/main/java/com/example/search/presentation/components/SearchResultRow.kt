package com.example.search.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.module.SearchResultMetaData
import com.example.domain.text.ArabicDates
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.ArabicSearchText
import com.example.domain.text.ArticleTextCleaner
import com.example.domain.text.AudioTitleCleaner
import com.example.domain.text.DesignTitle
import com.example.search.presentation.model.SearchHit

private val textCleaner = ArticleTextCleaner()

/** The cleaned, display-only title for a hit (the Algolia record is untouched). */
fun SearchHit.displayTitle(): String {
    val raw = title.orEmpty()
    return when (type) {
        "article" -> textCleaner.cleanTitle(raw)
        "audio" -> AudioTitleCleaner.clean(raw)
        "image_group" -> DesignTitle.clean(raw)
        else -> raw
    }
}

/** Where tapping a hit leads: the same metadata the old result cards sent. */
fun SearchHit.toMetaData() = SearchResultMetaData(
    objectID = objectID,
    title = title,
    type = type,
    url = when (type) {
        "video" -> videoUrl
        "audio" -> audioUrl
        "article" -> null
        else -> previewImageUrl
    },
)

@DrawableRes
private fun typeIcon(type: String?): Int = when (type) {
    "audio" -> TablerIcons.Headphones
    "video" -> TablerIcons.Video
    "image_group" -> TablerIcons.Photo
    else -> TablerIcons.Notebook
}

/**
 * One result: a type icon in a 34dp accentContainer circle, the cleaned title with the
 * query's whole words highlighted, an article snippet around the first match, and the
 * meta line ("مقال · قراءة ٥ دقائق", "صوتية").
 */
@Composable
fun SearchResultRow(
    hit: SearchHit,
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    val highlight = SpanStyle(color = colors.accentText, fontWeight = FontWeight.Bold)
    val titleText = remember(hit.objectID, hit.title, query, highlight) {
        val title = hit.displayTitle()
        highlighted(title, ArabicSearchText.highlightRanges(title, query), highlight)
    }
    val snippet = remember(hit.objectID, query, highlight) {
        val body = hit.content?.takeIf { hit.type == "article" && it.isNotBlank() }
        body?.let {
            val cleaned = textCleaner.cleanBody(it, hit.title.orEmpty()).replace('\n', ' ')
            ArabicSearchText.snippet(cleaned, query).let { s -> highlighted(s.text, s.highlights, highlight) }
        }
    }
    val typeLabel = when (hit.type) {
        "audio" -> stringResource(R.string.search_type_audio)
        "video" -> stringResource(R.string.search_type_video)
        "image_group" -> stringResource(R.string.search_type_image)
        else -> stringResource(R.string.search_type_article)
    }
    val meta = remember(hit.objectID, typeLabel) {
        val reading = hit.content?.takeIf { hit.type == "article" && it.isNotBlank() }
            ?.let { ArabicDates.readingTime(textCleaner.readingMinutes(it)) }
        listOfNotNull(typeLabel, reading).joinToString(ArabicNumerals.DATE_SEPARATOR)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colors.accentContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(typeIcon(hit.type)),
                    contentDescription = null,
                    tint = colors.onAccentContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (snippet != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = snippet,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 1.6.em),
                        color = colors.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(text = meta, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
            }
        }
    }
}

private fun highlighted(text: String, ranges: List<IntRange>, style: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        append(text)
        ranges.forEach { addStyle(style, it.first, it.last + 1) }
    }

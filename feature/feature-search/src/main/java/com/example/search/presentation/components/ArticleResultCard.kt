package com.example.search.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.domain.module.SearchResultMetaData
import com.example.search.presentation.model.SearchHit
import kotlin.math.max
import kotlin.math.min

@Composable
fun ArticleResultCard(
    hit: SearchHit,
    modifier: Modifier = Modifier,
    onItemClick: (SearchResultMetaData) -> Unit
) {
    Card(
        modifier = modifier
            .clickable {
                onItemClick(
                    SearchResultMetaData(
                        objectID = hit.objectID,
                        title = hit.title,
                        type = hit.type,
                    )
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.articles_icon),
                contentDescription = "Article",
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                if (hit.highlightedTitle != null) {
                    Text(
                        text = hit.highlightedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hit.highlightedContent != null) createSnippet(hit.highlightedContent) else buildAnnotatedString { "No Content" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


fun createSnippet(fullText: AnnotatedString, contextLength: Int = 200): AnnotatedString {
    // Find the first occurrence of the highlight annotation
    val firstHighlight = fullText.spanStyles.firstOrNull {

        it.item.fontWeight != null || it.item.background != SpanStyle().background
    }

    if (firstHighlight == null) {
        // If no highlight is found, just return the beginning of the text
        return if (fullText.length > contextLength * 2) {
            buildAnnotatedString {
                append(fullText.subSequence(0, contextLength * 2))
                append("...")
            }
        } else {
            fullText
        }
    }

    val highlightStart = firstHighlight.start
    val highlightEnd = firstHighlight.end

    // Calculate the start and end of the snippet window
    val snippetStart = max(0, highlightStart - contextLength)
    val snippetEnd = min(fullText.length, highlightEnd + contextLength)

    // Build the new annotated string for the snippet
    return buildAnnotatedString {
        // Add "..." if the snippet doesn't start at the beginning of the text
        if (snippetStart > 0) {
            append("... ")
        }

        // Append the relevant subsequence from the original text
        append(fullText.subSequence(snippetStart, snippetEnd))

        // Add "..." if the snippet doesn't end at the end of the text
        if (snippetEnd < fullText.length) {
            append(" ...")
        }
    }
}

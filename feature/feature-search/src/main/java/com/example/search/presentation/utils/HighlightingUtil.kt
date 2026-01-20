package com.example.search.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight


fun algoliaHighlightToAnnotatedString(
    highlightedValue: String,
    highlightColor: Color = Color(0xFFFFE082)
): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("<em>(.*?)</em>")
        var lastIndex = 0

        regex.findAll(highlightedValue).forEach { match ->
            // normal text before highlight
            append(highlightedValue.substring(lastIndex, match.range.first))

            // highlighted text
            pushStyle(
                SpanStyle(
                    background = highlightColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            append(match.groupValues[1])
            pop()

            lastIndex = match.range.last + 1
        }

        if (lastIndex < highlightedValue.length) {
            append(highlightedValue.substring(lastIndex))
        }
    }
}

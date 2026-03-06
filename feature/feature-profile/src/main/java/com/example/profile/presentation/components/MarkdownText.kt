package com.example.profile.presentation.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    onLinkClick: ((url: String) -> Unit)? = null,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val layoutDirection = LocalLayoutDirection.current
    val resolvedStyle = style.copy(
        color = MaterialTheme.colorScheme.onSurface,
        textDirection = if (layoutDirection == LayoutDirection.Rtl) TextDirection.Rtl else TextDirection.Ltr
    )

    val annotated = remember(content) {
        MarkdownParser.parse(
            markdown = content,
            h1Size = 26.sp,
            h2Size = 21.sp,
            h3Size = 18.sp
        )
    }

    ClickableText(
        modifier = modifier,
        text = annotated,
        style = resolvedStyle,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip,
        onClick = { offset ->
            if (onLinkClick == null) return@ClickableText
            val url = annotated
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.item
            if (url != null) onLinkClick(url)
        }
    )
}

private object MarkdownParser {

    fun parse(
        markdown: String,
        h1Size: TextUnit,
        h2Size: TextUnit,
        h3Size: TextUnit
    ): AnnotatedString {
        val lines = markdown.replace("\r\n", "\n").split("\n")

        val h1 = SpanStyle(fontSize = h1Size, fontWeight = FontWeight.Bold)
        val h2 = SpanStyle(fontSize = h2Size, fontWeight = FontWeight.Bold)
        val h3 = SpanStyle(fontSize = h3Size, fontWeight = FontWeight.Bold)

        return buildAnnotatedString {
            lines.forEachIndexed { index, raw ->
                val line = raw.trimEnd()

                when {
                    line.isBlank() -> {
                        append("\n")
                    }

                    isDivider(line) -> {
                        append("\n")
                        append("────────────────────────\n\n")
                    }

                    line.startsWith("### ") -> {
                        val start = length
                        append(line.removePrefix("### ").trim())
                        addStyle(h3, start, length)
                        append("\n\n")
                    }

                    line.startsWith("## ") -> {
                        val start = length
                        append(line.removePrefix("## ").trim())
                        addStyle(h2, start, length)
                        append("\n\n")
                    }

                    line.startsWith("# ") -> {
                        val start = length
                        append(line.removePrefix("# ").trim())
                        addStyle(h1, start, length)
                        append("\n\n")
                    }

                    isBullet(line) -> {
                        append("• ")
                        appendInlineWithStyles(line.drop(2).trim())
                        append("\n")
                    }

                    else -> {
                        appendInlineWithStyles(line)
                        if (index != lines.lastIndex) append("\n\n")
                    }
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendInlineWithStyles(text: String) {
        var i = 0
        var plainStart = 0

        fun flushPlain(until: Int) {
            if (until > plainStart) {
// Append the whole substring at once (Arabic shaping safe)
                append(text.substring(plainStart, until))
            }
            plainStart = until
        }

        while (i < text.length) {

// **bold**
            if (text.startsWith("**", i)) {
                val end = text.indexOf("**", startIndex = i + 2)
                if (end != -1) {
                    flushPlain(i)

                    val boldText = text.substring(i + 2, end)
                    val start = length
                    append(boldText)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)

                    i = end + 2
                    plainStart = i
                    continue
                }
            }

// [text](url)
            if (text[i] == '[') {
                val closeBracket = text.indexOf(']', startIndex = i + 1)
                val openParen =
                    if (closeBracket != -1) text.indexOf('(', startIndex = closeBracket + 1) else -1
                val closeParen =
                    if (openParen != -1) text.indexOf(')', startIndex = openParen + 1) else -1

                if (closeBracket != -1 && openParen == closeBracket + 1 && closeParen != -1) {
                    flushPlain(i)

                    val label = text.substring(i + 1, closeBracket)
                    val url = text.substring(openParen + 1, closeParen)

                    val start = length
                    append(label)
                    val end = length

                    addStringAnnotation(tag = "URL", annotation = url, start = start, end = end)
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = start,
                        end = end
                    )

                    i = closeParen + 1
                    plainStart = i
                    continue
                }
            }

            i++
        }

        flushPlain(text.length)
    }

    private fun isDivider(line: String): Boolean {
        val t = line.trim()
        return t == "---" || t == "----" || t == "___"
    }

    private fun isBullet(line: String): Boolean {
        val t = line.trimStart()
        return (t.startsWith("- ") || t.startsWith("* ")) && t.length > 2
    }
}

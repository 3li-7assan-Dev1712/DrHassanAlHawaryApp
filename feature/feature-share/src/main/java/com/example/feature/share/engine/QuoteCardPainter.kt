package com.example.feature.share.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import com.example.core.ui.R
import com.example.domain.text.ArabicNumerals
import com.example.feature.share.engine.ShareFrameLayout.Colors
import kotlin.math.ceil
import kotlin.math.floor

/** One image of a (possibly multi-image) quote. [text] may carry gold highlight spans. */
data class QuotePage(
    val text: CharSequence,
    val index: Int,
    val count: Int,
    val sourceTitle: String?,
    val logoResId: Int,
)

/**
 * Draws the article quote images described by [QuoteCardLayout], and splits a
 * long excerpt into pages with the very same text paint - so a page break can
 * never be computed for text that then renders differently.
 *
 * The one implementation of the design: the share screen previews exactly the
 * bitmaps [QuoteCardRenderer] produces with this, and shares those files.
 * Not thread-safe: give each thread its own instance.
 */
class QuoteCardPainter(context: Context) {

    private val brand = BrandFramePainter(context)
    private val subtitleText = context.getString(R.string.share_quote_subtitle)
    private val sourceLabelText = context.getString(R.string.share_quote_source_label)

    private val bodyPaint = brand.textPaint(brand.regular, QuoteCardLayout.BODY_TEXT_SIZE, Colors.onSurface)
    private val markPaint = brand.textPaint(brand.bold, QuoteCardLayout.QUOTE_MARK_SIZE, Colors.gold)
    private val pageNumberPaint = brand.textPaint(brand.regular, QuoteCardLayout.PAGE_NUMBER_SIZE, Colors.secondaryText)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    /** The quote mark's actual ink bounds - its font line box is far taller than the glyph. */
    private val markBounds = Rect().also { markPaint.getTextBounds(QUOTE_MARK, 0, QUOTE_MARK.length, it) }
    private val pageNumberHeight = brand.layout("١ / ٢", pageNumberPaint, 200, Layout.Alignment.ALIGN_NORMAL).height

    /** Height the excerpt itself may take on one page (page number always reserved). */
    private val textBudget: Float =
        (QuoteCardLayout.BODY_BOTTOM - QuoteCardLayout.BODY_TOP) - markBounds.height() -
            QuoteCardLayout.QUOTE_MARK_GAP - QuoteCardLayout.PAGE_NUMBER_GAP - pageNumberHeight

    fun draw(canvas: Canvas, page: QuotePage) {
        brand.drawBackground(canvas)
        brand.drawHeader(canvas, page.logoResId, subtitleText)
        drawBody(canvas, page)
        page.sourceTitle?.takeIf { it.isNotBlank() }?.let { drawSourceCard(canvas, it) }
        brand.drawFooter(canvas)
    }

    /**
     * Splits [text] into page texts that each fit [textBudget] at the fixed body size.
     *
     * Pages are balanced (a long excerpt doesn't end on a one-line stub), and each
     * break prefers the end of a sentence or paragraph in the last ~40% of a page;
     * otherwise it breaks between words and the page ends with "…". The next page
     * starts without a leading ellipsis. Spans (the highlight) carry over.
     */
    fun paginate(text: CharSequence): List<CharSequence> {
        if (text.isBlank()) return emptyList()
        val full = bodyLayout(text)
        val lineCount = full.lineCount
        val pitch = (full.getLineBottom(0) - full.getLineTop(0)).toFloat()
        val capacity = floor((textBudget + QuoteCardLayout.BODY_LINE_SPACING_EXTRA) / pitch).toInt().coerceAtLeast(1)
        if (lineCount <= capacity && full.height <= textBudget) return listOf(text)

        val pageCount = ceil(lineCount / capacity.toFloat()).toInt()
        val target = ceil(lineCount / pageCount.toFloat()).toInt().coerceAtMost(capacity)

        val pages = ArrayList<CharSequence>()
        var line = 0
        while (line < lineCount) {
            if (lineCount - line <= capacity) {
                pageText(text, full.getLineStart(line), text.length, isLast = true)?.let(pages::add)
                break
            }
            var end = (line + target).coerceAtMost(lineCount)
            val earliest = line + ceil(target * 0.6f).toInt()
            for (k in end downTo earliest.coerceAtLeast(line + 1)) {
                if (endsSentence(text, full.getLineStart(k))) {
                    end = k
                    break
                }
            }
            // The appended "…" can wrap onto one more line - step back until the page fits.
            var page = pageText(text, full.getLineStart(line), full.getLineStart(end), isLast = false)
            while (page != null && end > line + 1 && bodyLayout(page).height > textBudget) {
                end--
                page = pageText(text, full.getLineStart(line), full.getLineStart(end), isLast = false)
            }
            page?.let(pages::add)
            line = end
        }
        return pages
    }

    private fun drawBody(canvas: Canvas, page: QuotePage) {
        val body = bodyLayout(page.text)
        val pageNumber = if (page.count > 1) {
            brand.layout(
                ArabicNumerals.digits("${page.index + 1} / ${page.count}"),
                pageNumberPaint, QuoteCardLayout.BODY_WIDTH.toInt(), Layout.Alignment.ALIGN_NORMAL,
            )
        } else {
            null
        }

        val blockHeight = markBounds.height() + QuoteCardLayout.QUOTE_MARK_GAP + body.height +
            (pageNumber?.let { QuoteCardLayout.PAGE_NUMBER_GAP + it.height } ?: 0f)
        val available = QuoteCardLayout.BODY_BOTTOM - QuoteCardLayout.BODY_TOP
        var y = QuoteCardLayout.BODY_TOP + ((available - blockHeight) / 2f).coerceAtLeast(0f)

        // Quote mark, right-aligned: position its ink (not its line box) at y.
        val mark = brand.layout(QUOTE_MARK, markPaint, QuoteCardLayout.BODY_WIDTH.toInt(), Layout.Alignment.ALIGN_NORMAL)
        brand.draw(canvas, mark, ShareFrameLayout.CONTENT_LEFT, y - (mark.getLineBaseline(0) + markBounds.top))
        y += markBounds.height() + QuoteCardLayout.QUOTE_MARK_GAP

        brand.draw(canvas, body, ShareFrameLayout.CONTENT_LEFT, y)
        y += body.height

        pageNumber?.let { brand.draw(canvas, it, ShareFrameLayout.CONTENT_LEFT, y + QuoteCardLayout.PAGE_NUMBER_GAP) }
    }

    /** "من مقال" + the article title, with a gold bar on the right (start) edge. */
    private fun drawSourceCard(canvas: Canvas, title: String) {
        rect.set(ShareFrameLayout.CONTENT_LEFT, QuoteCardLayout.SOURCE_CARD_TOP, ShareFrameLayout.CONTENT_RIGHT, QuoteCardLayout.SOURCE_CARD_BOTTOM)
        fillPaint.color = Colors.surface
        canvas.drawRoundRect(rect, QuoteCardLayout.SOURCE_CARD_RADIUS, QuoteCardLayout.SOURCE_CARD_RADIUS, fillPaint)

        val barRight = ShareFrameLayout.CONTENT_RIGHT - QuoteCardLayout.SOURCE_CARD_PADDING_H
        val barLeft = barRight - QuoteCardLayout.SOURCE_BAR_WIDTH
        rect.set(barLeft, QuoteCardLayout.SOURCE_CARD_TOP + QuoteCardLayout.SOURCE_BAR_INSET_V, barRight, QuoteCardLayout.SOURCE_CARD_BOTTOM - QuoteCardLayout.SOURCE_BAR_INSET_V)
        fillPaint.color = Colors.gold
        canvas.drawRoundRect(rect, QuoteCardLayout.SOURCE_BAR_WIDTH / 2f, QuoteCardLayout.SOURCE_BAR_WIDTH / 2f, fillPaint)

        val textLeft = ShareFrameLayout.CONTENT_LEFT + QuoteCardLayout.SOURCE_CARD_PADDING_H
        val textWidth = (barLeft - QuoteCardLayout.SOURCE_BAR_GAP - textLeft).toInt()
        val label = brand.layout(
            sourceLabelText, brand.textPaint(brand.regular, QuoteCardLayout.SOURCE_LABEL_SIZE, Colors.secondaryText),
            textWidth, Layout.Alignment.ALIGN_NORMAL, maxLines = 1,
        )
        val innerHeight = QuoteCardLayout.SOURCE_CARD_HEIGHT - 2 * 28f
        val titleLayout = brand.fitLayout(
            ArabicNumerals.digits(title), brand.medium, QuoteCardLayout.SOURCE_TITLE_SIZE, QuoteCardLayout.SOURCE_TITLE_MIN_SIZE,
            Colors.onSurface, textWidth, Layout.Alignment.ALIGN_NORMAL, QuoteCardLayout.SOURCE_TITLE_MAX_LINES,
            maxHeight = innerHeight - label.height,
        )
        val top = QuoteCardLayout.SOURCE_CARD_TOP + (QuoteCardLayout.SOURCE_CARD_HEIGHT - label.height - titleLayout.height) / 2f
        brand.draw(canvas, label, textLeft, top)
        brand.draw(canvas, titleLayout, textLeft, top + label.height)
    }

    private fun bodyLayout(text: CharSequence): StaticLayout = brand.layout(
        text, bodyPaint, QuoteCardLayout.BODY_WIDTH.toInt(), Layout.Alignment.ALIGN_NORMAL,
        lineSpacingExtra = QuoteCardLayout.BODY_LINE_SPACING_EXTRA,
    )

    /** [from, to) of [text], whitespace-trimmed, with "…" when a non-last page stops
     * mid-sentence. A break at a paragraph end ("…وبعد:" + newline) is a natural stop: no "…". */
    private fun pageText(text: CharSequence, from: Int, to: Int, isLast: Boolean): CharSequence? {
        var start = from
        var end = to
        var endsParagraph = false
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) {
            if (text[end - 1] == '\n') endsParagraph = true
            end--
        }
        if (start >= end) return null
        val page = SpannableStringBuilder(text, start, end)
        if (!isLast && !endsParagraph && page[page.length - 1] !in SENTENCE_FINAL) page.append('…')
        return page
    }

    /** True if the text just before [offset] closes a sentence or a paragraph. */
    private fun endsSentence(text: CharSequence, offset: Int): Boolean {
        var i = offset - 1
        var sawNewline = false
        while (i >= 0 && text[i].isWhitespace()) {
            if (text[i] == '\n') sawNewline = true
            i--
        }
        return sawNewline || (i >= 0 && text[i] in SENTENCE_FINAL)
    }

    private companion object {
        const val QUOTE_MARK = "”"
        const val SENTENCE_FINAL = ".؟?!…۔"
    }
}

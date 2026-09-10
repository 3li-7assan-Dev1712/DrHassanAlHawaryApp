package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import com.example.feature.share.domain.ShareBackgroundSource

/** Shared brand accent - every branded card (video frame, quote image, ...) keys off this one value. */
const val CARD_ACCENT_COLOR = 0xFF342C2B.toInt()
private const val GRADIENT_TOP_COLOR = 0xFF342C2B.toInt()
private const val GRADIENT_BOTTOM_COLOR = 0xFF1A1514.toInt()

/**
 * Canvas-drawing primitives shared by every branded-card renderer
 * ([ShareCardBitmapRenderer], [TextCardBitmapRenderer], ...) - background,
 * scrim, circular logo, auto-shrinking Arabic-aware text, and the accent
 * rule. Each renderer owns only its own layout (which [Rect01]/[TextBlock01]
 * goes where) and calls these to actually paint it, so the two card designs
 * can never drift out of sync on how a logo or a text block is drawn.
 */
fun drawCardBackground(context: Context, canvas: Canvas, source: ShareBackgroundSource, width: Int, height: Int) {
    if (source is ShareBackgroundSource.Gradient) {
        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(GRADIENT_TOP_COLOR, GRADIENT_BOTTOM_COLOR),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { shader = gradient })
        return
    }

    val bitmap = when (source) {
        is ShareBackgroundSource.FromDrawableRes -> decodeSampledBitmap(context, source.resId, width)
        is ShareBackgroundSource.FromImageUri -> BitmapFactory.decodeFile(source.uri) ?: return
        ShareBackgroundSource.Gradient -> return // handled above
    }
    val src = centerCropSrcRect(bitmap.width, bitmap.height, width.toFloat() / height)
    canvas.drawBitmap(bitmap, src, Rect(0, 0, width, height), null)
    bitmap.recycle()
}

fun drawCardScrim(canvas: Canvas, stops: List<Pair<Float, Long>>, width: Int, height: Int) {
    val positions = stops.map { it.first }.toFloatArray()
    val colors = stops.map { it.second.toInt() }.toIntArray()
    val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, positions, Shader.TileMode.CLAMP)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { shader = gradient })
}

fun drawCardLogo(context: Context, canvas: Canvas, logoResId: Int, rect: Rect01, width: Int, height: Int) {
    val left = rect.left * width
    val top = rect.top * height
    val diameter = rect.width * width

    val source = decodeSampledBitmap(context, logoResId, diameter.toInt())
    val squareSrc = centerCropSrcRect(source.width, source.height, 1f)
    val cropped = Bitmap.createBitmap(source, squareSrc.left, squareSrc.top, squareSrc.width(), squareSrc.height())

    val shader = BitmapShader(cropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val scale = diameter / cropped.width.toFloat()
    shader.setLocalMatrix(Matrix().apply {
        setScale(scale, scale)
        postTranslate(left, top)
    })

    val radius = diameter / 2f
    canvas.drawCircle(left + radius, top + radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader })

    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * (width / 1080f)
        color = CARD_ACCENT_COLOR
    }
    canvas.drawCircle(left + radius, top + radius, radius - ringPaint.strokeWidth / 2f, ringPaint)

    source.recycle()
    cropped.recycle()
}

fun drawCardAccentRule(canvas: Canvas, rect: Rect01, width: Int, height: Int) {
    canvas.drawRect(
        rect.left * width, rect.top * height, rect.right * width, rect.bottom * height,
        Paint().apply { color = CARD_ACCENT_COLOR },
    )
}

/** Arabic text is drawn with StaticLayout + TextDirectionHeuristics.RTL, never
 * canvas.drawText, which mangles multi-line Arabic shaping. Shrinks [block]'s
 * font down to its minimum before falling back to a hard, ellipsized clip. */
fun drawCardText(canvas: Canvas, text: String, block: TextBlock01, width: Int, height: Int, typeface: Typeface) {
    val rectLeft = block.rect.left * width
    val rectTop = block.rect.top * height
    val rectWidthPx = (block.rect.width * width).toInt().coerceAtLeast(1)
    val rectHeightPx = block.rect.height * height

    var textSizePx = block.fontSizePx
    var layout = buildCardStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = false)
    // Line count alone is only a proxy for "fits the box" - it assumes a line-height
    // estimate that can be wrong for a given font/script, which let text quietly
    // overflow its rect and overlap whatever was drawn below it. Checking actual
    // pixel height directly is what actually guarantees no overlap.
    while (
        (layout.lineCount > block.maxLines || layout.height > rectHeightPx) &&
        textSizePx > block.minFontSizePx
    ) {
        textSizePx = (textSizePx - 2f).coerceAtLeast(block.minFontSizePx)
        layout = buildCardStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = false)
    }
    // Guarantee no overflow even if it still doesn't fit at the minimum size.
    layout = buildCardStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = true)

    val verticalOffset = ((rectHeightPx - layout.height) / 2f).coerceAtLeast(0f)
    canvas.save()
    canvas.translate(rectLeft, rectTop + verticalOffset)
    layout.draw(canvas)
    canvas.restore()
}

private fun buildCardStaticLayout(
    text: String,
    typeface: Typeface,
    textSizePx: Float,
    widthPx: Int,
    block: TextBlock01,
    hardLimit: Boolean,
): StaticLayout {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        color = block.colorArgb.toInt()
        val baseAlpha = ((block.colorArgb ushr 24) and 0xFFL).toInt()
        alpha = (baseAlpha * block.alpha).toInt().coerceIn(0, 255)
    }
    val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, widthPx)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setTextDirection(TextDirectionHeuristics.RTL)
        .setLineSpacing(0f, 1f)
        .setIncludePad(false)
    if (hardLimit) {
        builder.setMaxLines(block.maxLines).setEllipsize(TextUtils.TruncateAt.END)
    }
    return builder.build()
}

private fun decodeSampledBitmap(context: Context, resId: Int, targetWidth: Int): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(context.resources, resId, bounds)
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= targetWidth && sampleSize < 16) sampleSize *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return requireNotNull(BitmapFactory.decodeResource(context.resources, resId, opts))
}

private fun centerCropSrcRect(bitmapWidth: Int, bitmapHeight: Int, targetAspect: Float): Rect {
    val bitmapAspect = bitmapWidth.toFloat() / bitmapHeight
    return if (bitmapAspect > targetAspect) {
        val cropWidth = (bitmapHeight * targetAspect).toInt()
        val left = (bitmapWidth - cropWidth) / 2
        Rect(left, 0, left + cropWidth, bitmapHeight)
    } else {
        val cropHeight = (bitmapWidth / targetAspect).toInt()
        val top = (bitmapHeight - cropHeight) / 2
        Rect(0, top, bitmapWidth, top + cropHeight)
    }
}

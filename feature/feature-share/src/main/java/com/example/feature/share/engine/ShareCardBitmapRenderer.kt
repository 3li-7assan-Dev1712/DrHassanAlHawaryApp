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
import androidx.core.content.res.ResourcesCompat
import com.example.core.ui.R
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import javax.inject.Inject

/**
 * spec + content -> a static 1080x1920 bitmap: everything in the design
 * EXCEPT the animated waveform bars, which [WaveformOverlay] draws per-frame
 * on top of this at export time.
 *
 * Reads the exact same [ShareCardSpec] as the Compose preview - see that
 * class's doc comment for why the two must never diverge.
 *
 * Arabic text is drawn with StaticLayout + TextDirectionHeuristics.RTL, never
 * canvas.drawText, which mangles multi-line Arabic shaping.
 */
class ShareCardBitmapRenderer @Inject constructor() {

    fun render(context: Context, content: ShareCardContent, spec: ShareCardSpec): Bitmap {
        val width = spec.referenceWidthPx.toInt()
        val height = spec.referenceHeightPx.toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(context, canvas, content.background, width, height)
        drawScrim(canvas, spec.scrimStops, width, height)
        drawLogo(context, canvas, content.logoResId, spec.logo, width, height)

        val boldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_bold))
        val semiBoldTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_semi_bold))
        val regularTypeface = requireNotNull(ResourcesCompat.getFont(context, R.font.cairo_regular))

        drawText(canvas, content.instituteName, spec.instituteLine, width, height, semiBoldTypeface)
        drawText(canvas, content.title, spec.title, width, height, boldTypeface)
        content.category?.let { drawText(canvas, it, spec.category, width, height, regularTypeface) }

        drawAccentRule(canvas, spec.accentRule, width, height)

        val brandLine = context.getString(R.string.share_brand_line)
        drawText(canvas, brandLine, spec.brandLine, width, height, semiBoldTypeface)

        return bitmap
    }

    private fun drawBackground(context: Context, canvas: Canvas, source: ShareBackgroundSource, width: Int, height: Int) {
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

    private fun drawScrim(canvas: Canvas, stops: List<Pair<Float, Long>>, width: Int, height: Int) {
        val positions = stops.map { it.first }.toFloatArray()
        val colors = stops.map { it.second.toInt() }.toIntArray()
        val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, positions, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { shader = gradient })
    }

    private fun drawLogo(context: Context, canvas: Canvas, logoResId: Int, rect: Rect01, width: Int, height: Int) {
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
            color = ACCENT_COLOR
        }
        canvas.drawCircle(left + radius, top + radius, radius - ringPaint.strokeWidth / 2f, ringPaint)

        source.recycle()
        cropped.recycle()
    }

    private fun drawAccentRule(canvas: Canvas, rect: Rect01, width: Int, height: Int) {
        canvas.drawRect(
            rect.left * width, rect.top * height, rect.right * width, rect.bottom * height,
            Paint().apply { color = ACCENT_COLOR },
        )
    }

    private fun drawText(canvas: Canvas, text: String, block: TextBlock01, width: Int, height: Int, typeface: Typeface) {
        val rectLeft = block.rect.left * width
        val rectTop = block.rect.top * height
        val rectWidthPx = (block.rect.width * width).toInt().coerceAtLeast(1)
        val rectHeightPx = block.rect.height * height

        var textSizePx = block.fontSizePx
        var layout = buildStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = false)
        while (layout.lineCount > block.maxLines && textSizePx > block.minFontSizePx) {
            textSizePx = (textSizePx - 2f).coerceAtLeast(block.minFontSizePx)
            layout = buildStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = false)
        }
        // Guarantee no overflow even if it still doesn't fit at the minimum size.
        layout = buildStaticLayout(text, typeface, textSizePx, rectWidthPx, block, hardLimit = true)

        val verticalOffset = ((rectHeightPx - layout.height) / 2f).coerceAtLeast(0f)
        canvas.save()
        canvas.translate(rectLeft, rectTop + verticalOffset)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun buildStaticLayout(
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

    companion object {
        private const val ACCENT_COLOR = 0xFF036B5C.toInt()
        // core-ui Color.kt: primaryLight -> backgroundDark, the same dark tone the
        // bottom scrim fades into.
        private const val GRADIENT_TOP_COLOR = 0xFF036B5C.toInt()
        private const val GRADIENT_BOTTOM_COLOR = 0xFF0E1513.toInt()
    }
}

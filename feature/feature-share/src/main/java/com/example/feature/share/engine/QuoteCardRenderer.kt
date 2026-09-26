package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import javax.inject.Inject

/** One [QuotePage] -> the 1080x1920 image that's both previewed and shared. */
class QuoteCardRenderer @Inject constructor() {

    fun render(context: Context, page: QuotePage): Bitmap = render(QuoteCardPainter(context), page)

    /** Reuses [painter] (fonts, paints) across the pages of one share. */
    fun render(painter: QuoteCardPainter, page: QuotePage): Bitmap {
        val bitmap = Bitmap.createBitmap(ShareFrameLayout.WIDTH.toInt(), ShareFrameLayout.HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        painter.draw(Canvas(bitmap), page)
        return bitmap
    }
}

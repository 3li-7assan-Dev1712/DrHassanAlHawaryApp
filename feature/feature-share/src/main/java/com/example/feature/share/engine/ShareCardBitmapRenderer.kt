package com.example.feature.share.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.feature.share.domain.ShareCardContent
import javax.inject.Inject

/**
 * content -> the static 1080x1920 base frame of the share video: everything in
 * the design EXCEPT the animated waveform + times, which [WaveformOverlay]
 * draws per frame on top of it at export time.
 *
 * The live preview displays this exact bitmap too (see ShareCardPreview), so
 * what the user previews is literally what gets encoded.
 */
class ShareCardBitmapRenderer @Inject constructor() {

    fun render(context: Context, content: ShareCardContent): Bitmap {
        val bitmap = Bitmap.createBitmap(
            ShareFrameLayout.WIDTH.toInt(),
            ShareFrameLayout.HEIGHT.toInt(),
            Bitmap.Config.ARGB_8888,
        )
        ShareFramePainter(context).drawStatic(Canvas(bitmap), content)
        return bitmap
    }
}

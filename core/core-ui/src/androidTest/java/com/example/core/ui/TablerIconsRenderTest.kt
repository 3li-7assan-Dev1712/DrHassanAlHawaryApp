package com.example.core.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.ui.icons.TablerIcons
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Inflates every Tabler vector drawable with the real Android parser, tints it,
 * and checks it actually draws something - then writes a contact sheet to
 * `cacheDir/tabler_icons.png` for a visual check.
 */
@RunWith(AndroidJUnit4::class)
class TablerIconsRenderTest {

    @Test
    fun everyIconInflatesTintsAndDraws() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cell = 96
        val sheet = Bitmap.createBitmap(cell * TablerIcons.all.size, cell, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(0xFF1A1512.toInt())

        TablerIcons.all.forEachIndexed { i, (name, res) ->
            val drawable = requireNotNull(ContextCompat.getDrawable(context, res)) { name }.mutate()
            drawable.setTint(0xFFEF9F27.toInt())
            drawable.setTintMode(PorterDuff.Mode.SRC_IN)

            val single = Bitmap.createBitmap(cell, cell, Bitmap.Config.ARGB_8888)
            drawable.setBounds(8, 8, cell - 8, cell - 8)
            drawable.draw(Canvas(single))
            val inked = (0 until cell * cell).count { Color.alpha(single.getPixel(it % cell, it / cell)) > 0 }
            assertTrue("$name drew nothing", inked > 50)
            assertTrue("$name is filled solid, not an outline ($inked px)", inked < cell * cell / 2)

            canvas.drawBitmap(single, (i * cell).toFloat(), 0f, null)
        }
        File(context.cacheDir, "tabler_icons.png").outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

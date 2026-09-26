package com.example.feature.share.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.core.ui.theme.ShareFrameBackground
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.engine.ShareCardBitmapRenderer
import com.example.feature.share.engine.ShareFrameLayout
import com.example.feature.share.engine.ShareFramePainter
import com.example.feature.share.engine.WaveformBars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * The live preview of the share video - not a Compose lookalike of it. It shows
 * the exact base frame [ShareCardBitmapRenderer] encodes, and draws the waveform
 * + times with the same [ShareFramePainter.drawWaveform] call the export's
 * WaveformOverlay makes per frame, on a canvas scaled from the 1080x1920
 * reference down to this card. Preview and export therefore can't drift.
 */
@Composable
fun ShareCardPreview(
    content: ShareCardContent,
    envelope: FloatArray,
    playbackPositionMs: Long,
    clipDurationMs: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val renderer = remember { ShareCardBitmapRenderer() }
    val painter = remember(context) { ShareFramePainter(context) }
    val bars = remember(envelope) { WaveformBars.fromEnvelope(envelope) }

    // Kept across re-renders so editing the quote never flashes an empty card.
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(content) {
        // Debounce re-renders while the user is typing a quote; the first render is immediate.
        if (frame != null) delay(RERENDER_DEBOUNCE_MS)
        frame = withContext(Dispatchers.Default) { renderer.render(context, content).asImageBitmap() }
    }

    Box(
        modifier = modifier
            .aspectRatio(ShareFrameLayout.WIDTH / ShareFrameLayout.HEIGHT)
            .background(ShareFrameBackground),
    ) {
        frame?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.High,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = if (clipDurationMs > 0) playbackPositionMs.toFloat() / clipDurationMs else 0f
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.save()
                native.scale(size.width / ShareFrameLayout.WIDTH, size.height / ShareFrameLayout.HEIGHT)
                painter.drawWaveform(native, bars, progress, playbackPositionMs, clipDurationMs)
                native.restore()
            }
        }
    }
}

private const val RERENDER_DEBOUNCE_MS = 200L

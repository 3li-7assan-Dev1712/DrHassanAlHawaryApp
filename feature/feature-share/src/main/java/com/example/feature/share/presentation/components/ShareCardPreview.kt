package com.example.feature.share.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ui.theme.Cairo
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.engine.Rect01
import com.example.feature.share.engine.ShareCardSpec
import com.example.feature.share.engine.ShareFontWeight
import com.example.feature.share.engine.TextBlock01
import kotlin.math.roundToInt

/**
 * Compose rendering of [ShareCardSpec] - the LIVE preview. ShareCardBitmapRenderer
 * (Canvas) renders the exact same spec for the burned-in video frame; neither
 * may hardcode a pixel value the other doesn't also derive from the spec.
 */
@Composable
fun ShareCardPreview(
    content: ShareCardContent,
    spec: ShareCardSpec,
    envelope: FloatArray,
    playbackFraction: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val accentColor = Color(0xFF342C2B) // core-ui Color.kt primaryLight (BrandBrown)

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(spec.referenceWidthPx / spec.referenceHeightPx)
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val scale = widthPx / spec.referenceWidthPx

        // The live on-screen preview is deliberately simpler than the actual burned-in
        // video: just background + circle logo + title + waveform. The institute line,
        // category, accent rule and Arabic brand line still render into the exported
        // mp4 via ShareCardBitmapRenderer - this screen alone omits them to avoid the
        // text stack overflowing the small preview card.
        Background(content.background, modifier = Modifier.fillMaxSize())

        CircleLogo(logoResId = content.logoResId, rect = spec.logo, scale = scale, density = density)

        ShareText(
            text = content.title,
            block = spec.title,
            scale = scale,
            density = density,
        )

        WaveformStrip(
            envelope = envelope,
            playbackFraction = playbackFraction,
            accentColor = accentColor,
            modifier = rectModifier(spec.waveform, scale, density),
        )
    }
}

@Composable
internal fun Background(source: ShareBackgroundSource, modifier: Modifier = Modifier) {
    when (source) {
        is ShareBackgroundSource.FromDrawableRes -> Image(
            painter = painterResource(id = source.resId),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )

        is ShareBackgroundSource.FromImageUri -> AsyncImage(
            model = source.uri,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )

        ShareBackgroundSource.Gradient -> Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    // core-ui Color.kt: primaryLight -> backgroundDark, the same
                    // dark tone the bottom scrim fades into.
                    colors = listOf(Color(0xFF342C2B), Color(0xFF1A1514)),
                )
            )
        )
    }
}

@Composable
internal fun BoxWithConstraintsScope.CircleLogo(logoResId: Int, rect: Rect01, scale: Float, density: Density) {
    val sizeDp = with(density) { (rect.width * scale * 1080f).toDp() }
    Image(
        painter = painterResource(id = logoResId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .offset(
                x = with(density) { (rect.left * scale * 1080f).toDp() },
                y = with(density) { (rect.top * scale * 1920f).toDp() },
            )
            .size(sizeDp)
            .clip(CircleShape),
    )
}

@Composable
internal fun BoxWithConstraintsScope.ShareText(
    text: String,
    block: TextBlock01,
    scale: Float,
    density: Density,
) {
    val color = Color(block.colorArgb).copy(alpha = block.alpha)
    val maxFontSp = with(density) { (block.fontSizePx * scale).toSp() }
    val minFontSp = with(density) { (block.minFontSizePx * scale).toSp() }
    var fontSize by remember(text, scale) { mutableStateOf(maxFontSp) }
    var readyToDraw by remember(text, scale) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = rectModifier(block.rect, scale, density)
            .drawWithContent { if (readyToDraw) drawContent() },
        color = color,
        fontFamily = Cairo,
        fontWeight = block.weight.toComposeWeight(),
        fontSize = fontSize,
        maxLines = block.maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        onTextLayout = { result ->
            if (result.didOverflowHeight && fontSize.value > minFontSp.value) {
                fontSize = (fontSize.value - 2f).coerceAtLeast(minFontSp.value).sp
            } else {
                readyToDraw = true
            }
        },
    )
}

@Composable
private fun WaveformStrip(
    envelope: FloatArray,
    playbackFraction: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val bars = remember(envelope) { downsample(envelope, BAR_COUNT) }
    Canvas(modifier = modifier) {
        if (bars.isEmpty()) return@Canvas
        val centerY = size.height / 2f
        val barWidth = size.width / BAR_COUNT
        val gap = barWidth * 0.25f
        val playedBars = (playbackFraction.coerceIn(0f, 1f) * BAR_COUNT).roundToInt()

        for (i in 0 until BAR_COUNT) {
            val amplitude = bars[i].coerceIn(0f, 1f)
            val barHeight = (size.height * amplitude).coerceAtLeast(size.height * MIN_HEIGHT_FRACTION)
            val x = i * barWidth + gap / 2f
            val color = if (i < playedBars) accentColor else accentColor.copy(alpha = 0.35f)
            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2f),
                size = Size((barWidth - gap).coerceAtLeast(1f), barHeight),
                cornerRadius = CornerRadius((barWidth / 2f).coerceAtLeast(1f)),
            )
        }
    }
}

private fun downsample(envelope: FloatArray, barCount: Int): FloatArray {
    if (envelope.isEmpty()) return FloatArray(barCount)
    return FloatArray(barCount) { i ->
        val start = (i * envelope.size) / barCount
        val end = (((i + 1) * envelope.size) / barCount).coerceAtLeast(start + 1).coerceAtMost(envelope.size)
        var sum = 0f
        for (j in start until end) sum += envelope[j]
        sum / (end - start)
    }
}

internal fun BoxWithConstraintsScope.rectModifier(rect: Rect01, scale: Float, density: Density): Modifier {
    val widthPx = rect.width * scale * 1080f
    val heightPx = rect.height * scale * 1920f
    return Modifier
        .offset(
            x = with(density) { (rect.left * scale * 1080f).toDp() },
            y = with(density) { (rect.top * scale * 1920f).toDp() },
        )
        .size(
            width = with(density) { widthPx.toDp() },
            height = with(density) { heightPx.toDp() },
        )
}

internal fun ShareFontWeight.toComposeWeight(): FontWeight = when (this) {
    ShareFontWeight.REGULAR -> FontWeight.Normal
    ShareFontWeight.MEDIUM -> FontWeight.Medium
    ShareFontWeight.SEMI_BOLD -> FontWeight.SemiBold
    ShareFontWeight.BOLD -> FontWeight.Bold
}

private const val BAR_COUNT = 48
private const val MIN_HEIGHT_FRACTION = 6f / 360f

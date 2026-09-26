package com.example.feature.share.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ui.theme.Cairo
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.engine.Rect01
import com.example.feature.share.engine.ShareFontWeight
import com.example.feature.share.engine.TextBlock01

// Compose building blocks for the quote-card preview (TextCardPreview), which
// mirrors TextCardSpec in Compose. The video card doesn't use these: its preview
// shows the real rendered frame instead (see ShareCardPreview).

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
                    // core-ui Color.kt: primaryLight -> backgroundDark, matching CardDrawing's gradient.
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

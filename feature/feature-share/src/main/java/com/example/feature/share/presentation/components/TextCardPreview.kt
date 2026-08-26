package com.example.feature.share.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import com.example.core.ui.R
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.engine.Rect01
import com.example.feature.share.engine.TextCardSpec

/**
 * Compose rendering of [TextCardSpec] - the LIVE preview for the quote-card
 * flow. [TextCardBitmapRenderer][com.example.feature.share.engine.TextCardBitmapRenderer]
 * renders the exact same spec for the exported PNG; neither may hardcode a
 * pixel value the other doesn't also derive from the spec (see
 * [ShareCardPreview]'s doc comment - same rule, same reason).
 *
 * Unlike [ShareCardPreview] (which deliberately simplifies the live preview -
 * the video card's title/category/waveform can crowd a small preview), this
 * renders the FULL design 1:1: the brand lockup here is small and fixed-size
 * by construction, so there's no overflow risk, and showing it live is more
 * useful for confirming the design looks right before sharing.
 *
 * Reuses [Background]/[CircleLogo]/[ShareText] from ShareCardPreview.kt - both
 * card designs share the same logo/text drawing, just different layouts.
 */
@Composable
fun TextCardPreview(
    content: ShareCardContent,
    spec: TextCardSpec,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val accentColor = Color(0xFF036B5C) // core-ui Color.kt primaryLight

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(spec.referenceWidthPx / spec.referenceHeightPx)
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val scale = widthPx / spec.referenceWidthPx

        Background(content.background, modifier = Modifier.fillMaxSize())

        QuoteMark(rect = spec.quoteMark, scale = scale, density = density, color = accentColor)

        ShareText(text = content.title, block = spec.quote, scale = scale, density = density)

        content.category?.let {
            ShareText(text = it, block = spec.attribution, scale = scale, density = density)
        }

        Divider(rect = spec.divider, scale = scale, density = density, color = accentColor)

        CircleLogo(logoResId = content.logoResId, rect = spec.brandLogo, scale = scale, density = density)

        ShareText(text = content.instituteName, block = spec.brandName, scale = scale, density = density)

        ShareText(
            text = stringResource(R.string.share_brand_line),
            block = spec.brandTagline,
            scale = scale,
            density = density,
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.QuoteMark(rect: Rect01, scale: Float, density: Density, color: Color) {
    Text(
        text = "”",
        color = color.copy(alpha = 0.55f),
        fontWeight = FontWeight.Bold,
        fontSize = with(density) { (rect.height * scale * 1920f).toSp() },
        modifier = Modifier.offset(
            x = with(density) { (rect.left * scale * 1080f).toDp() },
            y = with(density) { (rect.top * scale * 1920f).toDp() },
        ),
    )
}

@Composable
private fun BoxWithConstraintsScope.Divider(rect: Rect01, scale: Float, density: Density, color: Color) {
    Box(modifier = rectModifier(rect, scale, density).background(color))
}

package com.example.feature.share.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.core.ui.theme.ShareFrameBackground
import com.example.feature.share.engine.QuoteCardRenderer
import com.example.feature.share.engine.QuotePage
import com.example.feature.share.engine.ShareFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * One quote image, previewed as the exact bitmap [QuoteCardRenderer] shares -
 * not a Compose lookalike. Rendered off the main thread when the page is first
 * composed (a pager only composes the pages around the visible one).
 */
@Composable
fun QuoteCardPreview(page: QuotePage, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val renderer = remember { QuoteCardRenderer() }
    val bitmap by produceState<ImageBitmap?>(null, page) {
        value = withContext(Dispatchers.Default) { renderer.render(context, page).asImageBitmap() }
    }

    Box(
        modifier = modifier
            .aspectRatio(ShareFrameLayout.WIDTH / ShareFrameLayout.HEIGHT)
            .background(ShareFrameBackground),
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.High,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

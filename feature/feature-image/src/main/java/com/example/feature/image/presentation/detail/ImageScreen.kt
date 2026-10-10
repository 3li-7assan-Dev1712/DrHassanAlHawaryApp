package com.example.feature.image.presentation.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import kotlin.math.abs
import com.example.core.ui.theme.reducedMotion
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.domain.module.Image
import com.example.domain.module.ImageGroup
import com.example.domain.module.ImageGroupWithImages
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.ui.theme.layoutTokens
import com.example.core.ui.util.LightSystemBarIcons
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.testTag
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.DesignTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_ZOOM = 4f
private const val DOUBLE_TAP_ZOOM = 2.5f
/** How far (of the viewer's height) a swipe must go before letting go closes it. */
private const val DISMISS_FRACTION = 0.25f
/** How small the image gets at a full-height swipe (it tracks the distance linearly). */
private const val DISMISS_MIN_SCALE = 0.85f

/**
 * Full-screen design viewer on the app background, one image per page, pinch zoom (1×–4×),
 * double-tap zoom, bounded pan, a counter "١ من ٩", a thumbnail strip for multi-image
 * posts, and a share button that sends the current image as a PNG.
 */
@Composable
fun ImageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImageDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    ImageViewerContent(uiState = uiState, onNavigateBack = onNavigateBack, modifier = modifier)
}

/** [ImageScreen] without its ViewModel (previews and UI tests use it too). */
@Composable
fun ImageViewerContent(
    uiState: ImageDetailUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = uiState.imageGroup?.group
    val images = uiState.imageGroup?.images ?: emptyList()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // initialPage is set once, ahead of images finishing loading if need be - Pager
    // coerces it against the real page count once that's known.
    val pagerState = rememberPagerState(
        initialPage = uiState.startIndex.coerceAtLeast(0),
        pageCount = { images.size },
    )
    var zoomed by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }

    // Swipe down (or up) to dismiss, only at 1×: the image follows the finger and shrinks a
    // little with the distance (to 0.85), the bars fade, and letting go past ~25% of the height closes the
    // viewer (the pop / container transform takes it from where the finger left it);
    // otherwise it springs back. Everything is read in the draw phase: no recomposition.
    val reduced = reducedMotion
    val dismissOffset = remember { Animatable(0f) }
    var viewerHeight by remember { mutableIntStateOf(1) }
    val dismissProgress = { (abs(dismissOffset.value) / viewerHeight).coerceIn(0f, 1f) }
    val dismissState = rememberDraggableState { delta ->
        scope.launch { dismissOffset.snapTo(dismissOffset.value + delta) }
    }

    // Tablet (Medium and Expanded): the immersive viewer of Figma `60:1782` / `64:4976`, on the
    // dark viewer background in both themes, drawn to the window edges (no rail).
    val tablet = !layoutTokens.isCompact
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewerHeight = it.height.coerceAtLeast(1) }
            // The phone: the same background as the designs grid (light or dark with the
            // theme), so the transform from a tile and back never flashes a different colour.
            .background(if (tablet) Brand.colors.viewerBackground else Brand.colors.background),
    ) {
        if (tablet) LightSystemBarIcons()
        val onBackground = if (tablet) Brand.colors.onViewerBackground else Brand.colors.textPrimary
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Brand.colors.accentStrong)

            uiState.error != null -> Text(
                text = uiState.error.orEmpty(),
                color = onBackground,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )

            group != null && images.isNotEmpty() -> {
                val title = DesignTitle.clean(group.title)
                val counter = if (images.size > 1) stringResource(
                    R.string.page_counter,
                    ArabicNumerals.digits(pagerState.currentPage + 1),
                    ArabicNumerals.digits(images.size),
                ) else null
                val onShare = {
                    val url = images.getOrNull(pagerState.currentPage)?.imageUrl
                    if (url != null) {
                        sharing = true
                        scope.launch {
                            shareImage(context, url, "design_${group.id}_${pagerState.currentPage}")
                            sharing = false
                        }
                    }
                }
                val barsAlpha = { 1f - (dismissProgress() * 3f).coerceAtMost(1f) }
                val pager = @Composable { pagerModifier: Modifier ->
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !zoomed,
                    modifier = pagerModifier
                        // The drag is detected outside the moving layer, so the finger's deltas
                        // aren't cancelled by the image moving under it.
                        .draggable(
                            state = dismissState,
                            orientation = Orientation.Vertical,
                            enabled = !zoomed,
                            onDragStopped = {
                                if (abs(dismissOffset.value) > viewerHeight * DISMISS_FRACTION) {
                                    onNavigateBack()
                                } else if (reduced) {
                                    dismissOffset.snapTo(0f)
                                } else {
                                    dismissOffset.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                                    )
                                }
                            },
                        )
                        .graphicsLayer {
                            translationY = dismissOffset.value
                            val scale = 1f - dismissProgress() * (1f - DISMISS_MIN_SCALE)
                            scaleX = scale
                            scaleY = scale
                        },
                ) { page ->
                    ZoomableImage(
                        url = images[page].imageUrl,
                        isCurrent = page == pagerState.currentPage,
                        onZoomChange = { if (page == pagerState.currentPage) zoomed = it },
                    )
                }
                }
                val onSelect: (Int) -> Unit = { scope.launch { pagerState.animateScrollToPage(it) } }
                if (tablet) {
                    TabletViewer(
                        title = title,
                        counter = counter,
                        sharing = sharing,
                        onClose = onNavigateBack,
                        onShare = onShare,
                        barsAlpha = barsAlpha,
                        pager = pager,
                        thumbnails = images.map { it.imageUrl }.takeIf { it.size > 1 },
                        current = pagerState.currentPage,
                        onSelect = onSelect,
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        ViewerTopBar(
                            modifier = Modifier.graphicsLayer { alpha = barsAlpha() },
                            title = title,
                            counter = counter,
                            sharing = sharing,
                            onClose = onNavigateBack,
                            onShare = onShare,
                        )
                        pager(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        if (images.size > 1) {
                            ThumbnailStrip(
                                modifier = Modifier.graphicsLayer { alpha = barsAlpha() },
                                urls = images.map { it.imageUrl },
                                current = pagerState.currentPage,
                                onSelect = onSelect,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The tablet viewer's poster (UI tests). */
const val VIEWER_POSTER_TEST_TAG = "viewerPoster"

/** The tablet viewer, measured in Figma (`60:1782` Expanded, `64:4976` Medium). */
private object TabletViewerSpec {
    val Spacing = 16.dp
    val PosterWidth = 688.dp
    val PosterHeight = 460.dp
    val PosterCorner = 8.dp
    val ThumbnailSize = 40.dp
    val ThumbnailCorner = 6.dp
    val ThumbnailGap = 6.dp
    val ThumbnailsBottom = 20.dp
}

/**
 * The tablet viewer: inside the window margin, a centred column 16 apart: the top row
 * (close, title, counter), the poster at 688 × 460 (fit, pinch zoom, swipe to close), the
 * pinch hint, the share pill and the thumbnails; text and outlines onViewerBackground.
 */
@Composable
private fun TabletViewer(
    title: String,
    counter: String?,
    sharing: Boolean,
    onClose: () -> Unit,
    onShare: () -> Unit,
    barsAlpha: () -> Float,
    pager: @Composable (Modifier) -> Unit,
    thumbnails: List<String>?,
    current: Int,
    onSelect: (Int) -> Unit,
) {
    val colors = Brand.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(layoutTokens.margin),
        verticalArrangement = Arrangement.spacedBy(TabletViewerSpec.Spacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Figma: 16 padding around a 24 icon; the 48 touch target sits on the same centre.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = barsAlpha() }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painterResource(TablerIcons.X),
                    contentDescription = stringResource(R.string.back),
                    tint = colors.onViewerBackground,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = title,
                color = colors.onViewerBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            if (counter != null) {
                Text(
                    text = counter,
                    color = colors.onViewerBackground,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
        }
        pager(
            Modifier
                // Shrinks to fit a short window rather than pushing the rest out.
                .weight(1f, fill = false)
                .widthIn(max = TabletViewerSpec.PosterWidth)
                .heightIn(max = TabletViewerSpec.PosterHeight)
                .fillMaxSize()
                .clip(RoundedCornerShape(TabletViewerSpec.PosterCorner))
                .testTag(VIEWER_POSTER_TEST_TAG)
        )
        Text(
            text = stringResource(R.string.viewer_pinch_hint),
            color = colors.onViewerBackground,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.graphicsLayer { alpha = barsAlpha() },
        )
        Surface(
            onClick = onShare,
            enabled = !sharing,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            border = BorderStroke(1.dp, colors.onViewerBackground),
            modifier = Modifier.graphicsLayer { alpha = barsAlpha() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (sharing) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = colors.onViewerBackground, strokeWidth = 2.dp)
                } else {
                    Icon(painterResource(TablerIcons.Share), contentDescription = null, tint = colors.onViewerBackground, modifier = Modifier.size(18.dp))
                }
                Text(text = stringResource(R.string.share), color = colors.onViewerBackground, style = MaterialTheme.typography.labelLarge)
            }
        }
        if (thumbnails != null) {
            ThumbnailStrip(
                modifier = Modifier.graphicsLayer { alpha = barsAlpha() },
                urls = thumbnails,
                current = current,
                onSelect = onSelect,
                thumbnailSize = TabletViewerSpec.ThumbnailSize,
                corner = TabletViewerSpec.ThumbnailCorner,
                gap = TabletViewerSpec.ThumbnailGap,
                contentPadding = PaddingValues(bottom = TabletViewerSpec.ThumbnailsBottom),
                dimUnselected = false,
                centred = true,
            )
        }
    }
}

@Composable
private fun ViewerTopBar(
    modifier: Modifier = Modifier,
    title: String,
    counter: String?,
    sharing: Boolean,
    onClose: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(painterResource(TablerIcons.X), contentDescription = stringResource(R.string.back), tint = Brand.colors.textPrimary)
        }
        Text(
            text = title,
            color = Brand.colors.textPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (counter != null) {
            Text(
                text = counter,
                color = Brand.colors.textSecondary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
        IconButton(onClick = onShare, enabled = !sharing) {
            if (sharing) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Brand.colors.accentStrong, strokeWidth = 2.dp)
            } else {
                Icon(painterResource(TablerIcons.Share), contentDescription = stringResource(R.string.share), tint = Brand.colors.textPrimary)
            }
        }
    }
}

/**
 * Pinch zoom 1×–4×, double-tap to zoom in/out, and a pan bounded to the zoomed image.
 * One finger at 1× is NOT consumed, so the pager still swipes between images.
 */
@Composable
private fun ZoomableImage(url: String, isCurrent: Boolean, onZoomChange: (Boolean) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun bounded(candidate: Offset, atScale: Float): Offset {
        val maxX = size.width * (atScale - 1f) / 2f
        val maxY = size.height * (atScale - 1f) / 2f
        return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
    }

    // Swiping to another page resets this one.
    LaunchedEffect(isCurrent) {
        if (!isCurrent) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { tap ->
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = DOUBLE_TAP_ZOOM
                        val center = Offset(size.width / 2f, size.height / 2f)
                        offset = bounded((center - tap) * (DOUBLE_TAP_ZOOM - 1f), DOUBLE_TAP_ZOOM)
                    }
                    onZoomChange(scale > 1f)
                })
            }
            .pointerInput(Unit) {
                // detectTransformGestures would consume one-finger drags at 1× too and block
                // the pager, so this is the same zoom/pan maths with that one exception.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.count { it.pressed }
                        if (pointers > 1 || scale > 1f) {
                            val newScale = (scale * event.calculateZoom()).coerceIn(1f, MAX_ZOOM)
                            offset = if (newScale == 1f) Offset.Zero else bounded(offset + event.calculatePan(), newScale)
                            scale = newScale
                            onZoomChange(scale > 1f)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(32.dp), color = Brand.colors.accentStrong, strokeWidth = 2.dp)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

/**
 * The thumbnails of a multi-image post. The phone: 52dp, start-aligned, the others dimmed.
 * The tablet passes Figma's 40dp squares, centred, undimmed.
 */
@Composable
private fun ThumbnailStrip(
    urls: List<String>,
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = 52.dp,
    corner: Dp = 8.dp,
    gap: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    dimUnselected: Boolean = true,
    centred: Boolean = false,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(current) { listState.animateScrollToItem(current) }
    LazyRow(
        state = listState,
        contentPadding = contentPadding,
        horizontalArrangement = if (centred) Arrangement.spacedBy(gap, Alignment.CenterHorizontally) else Arrangement.spacedBy(gap),
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(urls) { index, url ->
            val selected = index == current
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(corner),
                color = Color.Transparent,
                border = if (selected) BorderStroke(2.dp, Brand.colors.accentStrong) else null,
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(thumbnailSize)
                        .clip(RoundedCornerShape(corner))
                        .graphicsLayer { alpha = if (selected || !dimUnselected) 1f else 0.6f },
                )
            }
        }
    }
}

/**
 * Loads [url] through the app's Coil image loader (usually a cache hit), writes it as a
 * PNG to cacheDir/share/ (already exposed by the app's FileProvider) and opens the share
 * sheet. No gallery save.
 */
private suspend fun shareImage(context: Context, url: String, name: String) {
    val file = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val drawable = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@withContext null
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        File(dir, "$name.png").also { out -> out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
    } ?: return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** A sample multi-image post for previews and UI tests (no image files: the posters stay empty). */
val ViewerPreviewState = ImageDetailUiState(
    isLoading = false,
    imageGroup = ImageGroupWithImages(
        group = ImageGroup(id = "1", title = "دروس وعبر من مأساة الفاشر", previewImageUrl = ""),
        images = List(6) { Image(id = "$it", imageUrl = "", orderIndex = it) },
    ),
)

@Composable
private fun ViewerPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme, showRail = false, margin = false) {
        ImageViewerContent(uiState = ViewerPreviewState, onNavigateBack = {})
    }
}

@Preview(name = "Designs viewer - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ViewerCompactLightPreview() = ViewerPreview(darkTheme = false)

@Preview(name = "Designs viewer - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ViewerCompactDarkPreview() = ViewerPreview(darkTheme = true)

@Preview(name = "Designs viewer - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ViewerMediumLightPreview() = ViewerPreview(darkTheme = false)

@Preview(name = "Designs viewer - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ViewerMediumDarkPreview() = ViewerPreview(darkTheme = true)

@Preview(name = "Designs viewer - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ViewerExpandedLightPreview() = ViewerPreview(darkTheme = false)

@Preview(name = "Designs viewer - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ViewerExpandedDarkPreview() = ViewerPreview(darkTheme = true)

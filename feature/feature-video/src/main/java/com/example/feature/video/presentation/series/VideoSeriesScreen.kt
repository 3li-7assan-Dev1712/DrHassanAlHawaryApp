package com.example.feature.video.presentation.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.SeriesDetailList
import com.example.core.ui.components.SeriesItemRow
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.module.Video
import com.example.domain.text.AudioTitleCleaner
import java.util.Date

/**
 * A series of videos (a YouTube playlist): the audio series' screen ([SeriesDetailList]) with
 * its videos by episode. "تشغيل من البداية" and a tap open the video player screen
 * ([onNavigateToVideo]).
 */
@Composable
fun VideoSeriesScreen(
    onNavigateToVideo: (videoUrl: String, title: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VideoSeriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VideoSeriesContent(state = state, onNavigateToVideo = onNavigateToVideo, onNavigateBack = onNavigateBack)
}

/**
 * [VideoSeriesScreen] without the ViewModel (previews use it too). The player is a screen of
 * its own on every window, so nothing sits beside the list: on Expanded its column keeps
 * readingMaxWidth, centred. The titles are shown without the series' name and numbering
 * ([AudioTitleCleaner.lesson], display only); the player gets the stored title.
 */
@Composable
fun VideoSeriesContent(
    state: SeriesScreenState<Video>,
    onNavigateToVideo: (videoUrl: String, title: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = layoutTokens
    val seriesTitle = state.series?.title.orEmpty()
    SeriesDetailList(
        state = state,
        key = { it.id },
        onItemClick = { onNavigateToVideo(it.videoUrl, it.title) },
        onNavigateBack = onNavigateBack,
        shareText = R.string.series_share_text_video,
        modifier = modifier,
        maxContentWidth = if (tokens.isExpanded) tokens.readingMaxWidth else Dp.Unspecified,
    ) { video, index, count ->
        val title = remember(video.title, seriesTitle) { AudioTitleCleaner.lesson(video.title, seriesTitle) }
        SeriesItemRow(
            index = index,
            count = count,
            number = video.episodeNumber ?: (index + 1L),
            title = title,
            onClick = { onNavigateToVideo(video.videoUrl, video.title) },
        )
    }
}

/** Sample video series for previews and UI tests. */
val VideoSeriesPreviewData: List<Series> = listOf(
    Triple("yt_hajj", "شرح مناسك الحج", 28L),
    Triple("yt_khotab", "خطب الجمعة", 41L),
    Triple("yt_ramadan", "دروس رمضان", 12L),
).mapIndexed { index, (id, title, count) ->
    Series(
        id = id,
        title = title,
        collection = SeriesCollection.VIDEOS,
        categoryId = if (index == 1) "khotab" else "scientific_lessons",
        order = index.toLong(),
        itemCount = count,
    )
}

/** Sample episodes of «شرح مناسك الحج», with the titles as they are stored. */
private val SeriesVideosPreviewData: List<Video> = listOf(
    "شرح مناسك الحج - 1 مقدمة في فقه الحج",
    "(٢) شروط وجوب الحج على المستطيع وأحكام النيابة فيه عن الميت والعاجز",
    "شرح مناسك الحج، المواقيت الزمانية والمكانية",
    "4 - الإحرام ومحظوراته",
    "شرح مناسك الحج - (5) أنواع النسك: التمتع والقران والإفراد",
    "طواف القدوم والسعي",
).mapIndexed { index, title ->
    Video(
        id = "yt_hajj-$index",
        title = title,
        videoUrl = "video-$index",
        publishDate = Date(),
        youtubeVideoId = null,
        categoryId = "scientific_lessons",
        seriesId = "yt_hajj",
        episodeNumber = index + 1L,
    )
}

private val SeriesPreviewState = SeriesScreenState(
    series = VideoSeriesPreviewData[0],
    items = SeriesVideosPreviewData,
    isRefreshing = false,
)

@Composable
private fun VideoSeriesPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        VideoSeriesContent(state = SeriesPreviewState, onNavigateToVideo = { _, _ -> }, onNavigateBack = {})
    }
}

@Preview(name = "Video series - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun VideoSeriesCompactLightPreview() = VideoSeriesPreview(darkTheme = false)

@Preview(name = "Video series - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun VideoSeriesCompactDarkPreview() = VideoSeriesPreview(darkTheme = true)

@Preview(name = "Video series - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun VideoSeriesMediumLightPreview() = VideoSeriesPreview(darkTheme = false)

@Preview(name = "Video series - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun VideoSeriesMediumDarkPreview() = VideoSeriesPreview(darkTheme = true)

@Preview(name = "Video series - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun VideoSeriesExpandedLightPreview() = VideoSeriesPreview(darkTheme = false)

@Preview(name = "Video series - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun VideoSeriesExpandedDarkPreview() = VideoSeriesPreview(darkTheme = true)

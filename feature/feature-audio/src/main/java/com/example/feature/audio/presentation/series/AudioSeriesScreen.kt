package com.example.feature.audio.presentation.series

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.SeriesDetailList
import com.example.core.ui.components.SeriesItemRow
import com.example.core.ui.components.SeriesPreviewData
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.components.listeningStatus
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Audio
import com.example.domain.module.PlaybackProgress
import com.example.domain.module.SeriesProgress
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.AudioTitleCleaner
import com.example.feature.audio.presentation.detail.AUDIO_PLAYER_PANE_KEY
import com.example.feature.audio.presentation.detail.AudioDetailViewModel
import com.example.feature.audio.presentation.detail.AudioPlayerPane
import com.example.feature.audio.presentation.detail.AudioSelection
import com.example.feature.audio.presentation.detail.AudioShareAction
import com.example.feature.audio.presentation.list.PlayerPanePreviewContent
import java.util.Date

/**
 * A series of audios ("شرح صحيح البخاري"): its header with "تشغيل من البداية" and "download
 * the series", then its lessons by episode in one card. Like the audio list: Expanded windows
 * put the player beside the lessons ([onSelectAudio]; the caller keeps [selectedAudio], so it
 * survives rotation and resizing), and after the window narrows to one pane a selected lesson
 * stays open in the player here ([onClearSelection] returns to the lessons). Otherwise a tap
 * opens the player screen ([onNavigateToAudioDetail]).
 */
@Composable
fun AudioSeriesScreen(
    onNavigateToAudioDetail: (title: String, audioUrl: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AudioSeriesViewModel = hiltViewModel(),
    onNavigateToShare: AudioShareAction = { _, _, _, _, _, _ -> },
    selectedAudio: AudioSelection? = null,
    onSelectAudio: (AudioSelection) -> Unit = {},
    onClearSelection: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val download by viewModel.download.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val seriesProgress by viewModel.seriesProgress.collectAsStateWithLifecycle()
    val expanded = layoutTokens.isExpanded

    if (!expanded && selectedAudio != null) {
        val playerViewModel: AudioDetailViewModel = hiltViewModel(key = AUDIO_PLAYER_PANE_KEY)
        BackHandler(onBack = onClearSelection)
        AudioPlayerPane(
            title = selectedAudio.title,
            audioUrl = selectedAudio.audioUrl,
            onNavigateToShare = onNavigateToShare,
            modifier = modifier,
            onNavigateUp = onClearSelection,
            viewModel = playerViewModel,
        )
        return
    }

    // Only the player beside the lessons (Expanded) says which one plays; the phone's player
    // is a screen of its own, so its lessons show none playing.
    val playingAudioUrl = if (expanded) {
        val playerViewModel: AudioDetailViewModel = hiltViewModel(key = AUDIO_PLAYER_PANE_KEY)
        val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
        playerState.audioUrl.takeIf { playerState.isPlaying }
    } else {
        null
    }
    AudioSeriesAdaptiveContent(
        state = state,
        selectedAudio = selectedAudio,
        playingAudioUrl = playingAudioUrl,
        onSelectAudio = onSelectAudio,
        onNavigateToAudioDetail = onNavigateToAudioDetail,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        download = download,
        onDownloadSeries = viewModel::onDownloadSeries,
        progress = progress,
        seriesProgress = seriesProgress,
        playerPane = { selection ->
            AudioPlayerPane(
                title = selection.title,
                audioUrl = selection.audioUrl,
                onNavigateToShare = onNavigateToShare,
                viewModel = hiltViewModel(key = AUDIO_PLAYER_PANE_KEY),
            )
        },
    )
}

/**
 * [AudioSeriesScreen] without its ViewModels (previews pass the player pane). Expanded: the
 * lessons and the selected one's player, or the empty state before one is chosen.
 * [progress]: where each lesson was left, by URL; [seriesProgress]: the header's.
 */
@Composable
fun AudioSeriesAdaptiveContent(
    state: SeriesScreenState<Audio>,
    selectedAudio: AudioSelection?,
    playingAudioUrl: String?,
    onSelectAudio: (AudioSelection) -> Unit,
    onNavigateToAudioDetail: (title: String, audioUrl: String) -> Unit,
    onNavigateBack: () -> Unit,
    playerPane: @Composable (AudioSelection) -> Unit,
    modifier: Modifier = Modifier,
    download: SeriesDownload = SeriesDownload(),
    onDownloadSeries: () -> Unit = {},
    progress: Map<String, PlaybackProgress> = emptyMap(),
    seriesProgress: SeriesProgress = SeriesProgress.NotStarted,
) {
    if (layoutTokens.isExpanded) {
        TwoPaneLayout(
            modifier = modifier,
            listPane = {
                AudioSeriesList(
                    state = state,
                    onAudioClick = { onSelectAudio(AudioSelection(it.title, it.audioUrl)) },
                    onNavigateBack = onNavigateBack,
                    download = download,
                    onDownloadSeries = onDownloadSeries,
                    progress = progress,
                    seriesProgress = seriesProgress,
                    selectedAudioUrl = selectedAudio?.audioUrl,
                    playingAudioUrl = playingAudioUrl,
                )
            },
            detailPane = {
                if (selectedAudio != null) {
                    playerPane(selectedAudio)
                } else {
                    EmptyDetail(
                        icon = TablerIcons.Headphones,
                        title = stringResource(R.string.empty_detail_title),
                        subtitle = stringResource(R.string.empty_detail_subtitle),
                    )
                }
            },
        )
    } else {
        AudioSeriesList(
            state = state,
            onAudioClick = { onNavigateToAudioDetail(it.title, it.audioUrl) },
            onNavigateBack = onNavigateBack,
            download = download,
            onDownloadSeries = onDownloadSeries,
            progress = progress,
            seriesProgress = seriesProgress,
            modifier = modifier,
            playingAudioUrl = playingAudioUrl,
        )
    }
}

/**
 * The audio series' [SeriesDetailList]: "تشغيل من البداية" opens the first lesson as a tap on
 * it would (the player has no queue, so the next lesson is not played after it), "متابعة:
 * الدرس ٣" the lesson to go on with once the series is started, and "download the series" is
 * beside it. Lesson titles are shown without the series' name and numbering
 * ([AudioTitleCleaner.lesson], display only), with the duration, how far each was heard and
 * "محفوظ".
 */
@Composable
private fun AudioSeriesList(
    state: SeriesScreenState<Audio>,
    onAudioClick: (Audio) -> Unit,
    onNavigateBack: () -> Unit,
    download: SeriesDownload,
    onDownloadSeries: () -> Unit,
    progress: Map<String, PlaybackProgress>,
    seriesProgress: SeriesProgress,
    modifier: Modifier = Modifier,
    // Tablet: the lesson open in the player beside the list, and the one playing there.
    selectedAudioUrl: String? = null,
    playingAudioUrl: String? = null,
) {
    val seriesTitle = state.series?.title.orEmpty()
    SeriesDetailList(
        state = state,
        key = { it.id },
        onItemClick = onAudioClick,
        onNavigateBack = onNavigateBack,
        shareText = R.string.series_share_text,
        modifier = modifier,
        headerAction = { enabled -> SeriesDownloadButton(download, onDownloadSeries, enabled) },
        progress = seriesProgress,
        itemNumber = ::lessonNumber,
    ) { audio, index, count ->
        val title = remember(audio.title, seriesTitle) { AudioTitleCleaner.lesson(audio.title, seriesTitle) }
        val heard = progress[audio.audioUrl]
        SeriesItemRow(
            index = index,
            count = count,
            number = lessonNumber(audio, index),
            title = title,
            onClick = { onAudioClick(audio) },
            selected = audio.audioUrl == selectedAudioUrl,
            playing = audio.isPlaying || audio.audioUrl == playingAudioUrl,
            progress = heard,
            meta = { LessonMeta(audio, heard) },
        )
    }
}

/** The lesson's episode, else its place in the series. */
private fun lessonNumber(audio: Audio, index: Int): Long = audio.episodeNumber ?: (index + 1L)

/**
 * A 52dp outlined circle: the download icon; while saving, a ring with its progress around
 * an X that stops it; once every lesson is saved, a check.
 */
@Composable
private fun SeriesDownloadButton(download: SeriesDownload, onClick: () -> Unit, enabled: Boolean) {
    val colors = Brand.colors
    val progress = download.progress
    val label = stringResource(
        when {
            progress != null -> R.string.series_download_cancel
            download.allSaved -> R.string.series_downloaded
            else -> R.string.series_download
        }
    )
    Surface(
        onClick = onClick,
        enabled = enabled && (progress != null || !download.allSaved),
        shape = CircleShape,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier
            .size(52.dp)
            .semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                progress != null -> {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = colors.accentStrong,
                        strokeWidth = 2.dp,
                        trackColor = Color.Transparent,
                        gapSize = 0.dp,
                    )
                    Icon(painterResource(TablerIcons.X), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
                download.allSaved ->
                    Icon(painterResource(TablerIcons.CircleCheck), contentDescription = null, tint = colors.accentText, modifier = Modifier.size(22.dp))
                else ->
                    Icon(painterResource(TablerIcons.Download), contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

/**
 * "٠١:٠٥", then how far it was heard ("متبقٍ ١٢ د", "تم الاستماع") and, for a saved lesson,
 * the download icon and "محفوظ" (as the fatwa rows).
 */
@Composable
private fun LessonMeta(audio: Audio, progress: PlaybackProgress?) {
    val colors = Brand.colors
    val timing = listOfNotNull(
        ArabicNumerals.formatDuration(audio.durationInMillis).takeIf { audio.durationInMillis > 0 },
        listeningStatus(progress),
    )
    if (timing.isEmpty() && !audio.isDownloaded) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        if (timing.isNotEmpty()) {
            Text(
                text = timing.joinToString(ArabicNumerals.DATE_SEPARATOR),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
        }
        if (audio.isDownloaded) {
            if (timing.isNotEmpty()) Spacer(Modifier.width(10.dp))
            Icon(painterResource(TablerIcons.Download), contentDescription = null, tint = colors.accent, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = stringResource(R.string.audio_saved), style = MaterialTheme.typography.labelSmall, color = colors.accentText)
        }
    }
}

/** Sample lessons of «شرح مناسك الحج» for previews, with the titles as they are stored. */
private val SeriesAudiosPreviewData: List<Audio> = listOf(
    "شرح مناسك الحج - 1 مقدمة في فقه الحج" to 1_820_000L,
    "(٢) شروط وجوب الحج على المستطيع وأحكام النيابة فيه عن الميت والعاجز" to 2_410_000L,
    "شرح مناسك الحج، المواقيت الزمانية والمكانية" to 2_105_000L,
    "4 - الإحرام ومحظوراته" to 2_940_000L,
    "شرح مناسك الحج - (5) أنواع النسك: التمتع والقران والإفراد" to 2_260_000L,
    "طواف القدوم والسعي" to 1_985_000L,
).mapIndexed { index, (title, duration) ->
    Audio(
        id = "hajj-$index",
        categoryId = "scientific_lessons",
        title = title,
        audioUrl = "hajj-audio-$index",
        durationInMillis = duration,
        publishDate = Date(),
        isDownloaded = index == 0,
        lastPlayedTimestamp = null,
        seriesId = "hajj",
        episodeNumber = index + 1L,
    )
}

private val SeriesPreviewState = SeriesScreenState(
    series = SeriesPreviewData[1],
    items = SeriesAudiosPreviewData,
    isRefreshing = false,
)

/** Lesson 1 heard to the end, lesson 3 left part way (Figma 84:1715's states). */
private val SeriesPreviewProgress: Map<String, PlaybackProgress> = listOf(
    SeriesAudiosPreviewData[0].let { PlaybackProgress(it.audioUrl, it.title, it.durationInMillis, it.durationInMillis, updatedAt = 1L) },
    SeriesAudiosPreviewData[2].let { PlaybackProgress(it.audioUrl, it.title, 1_385_000L, it.durationInMillis, updatedAt = 2L) },
).associateBy { it.audioUrl }

@Composable
private fun AudioSeriesPreview(darkTheme: Boolean, download: SeriesDownload = SeriesDownload()) {
    val selection = AudioSelection(SeriesAudiosPreviewData[1].title, SeriesAudiosPreviewData[1].audioUrl)
    AdaptiveShellPreview(darkTheme = darkTheme) {
        AudioSeriesAdaptiveContent(
            state = SeriesPreviewState,
            selectedAudio = selection,
            playingAudioUrl = selection.audioUrl,
            onSelectAudio = {},
            onNavigateToAudioDetail = { _, _ -> },
            onNavigateBack = {},
            playerPane = { PlayerPanePreviewContent() },
            download = download,
            progress = SeriesPreviewProgress,
            seriesProgress = SeriesProgress.of(SeriesAudiosPreviewData.map { it.audioUrl }, SeriesPreviewProgress),
        )
    }
}

// Compact and Medium show the lessons alone (the player is its own screen there; the second
// lesson shows the playing look the tablet's player gives it).
@Preview(name = "Audio series - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioSeriesCompactLightPreview() = AudioSeriesPreview(darkTheme = false)

@Preview(name = "Audio series - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioSeriesCompactDarkPreview() = AudioSeriesPreview(darkTheme = true)

@Preview(name = "Audio series - compact, downloading", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioSeriesCompactDownloadingPreview() = AudioSeriesPreview(darkTheme = false, download = SeriesDownload(progress = 0.4f))

@Preview(name = "Audio series - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioSeriesMediumLightPreview() = AudioSeriesPreview(darkTheme = false)

@Preview(name = "Audio series - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioSeriesMediumDarkPreview() = AudioSeriesPreview(darkTheme = true)

@Preview(name = "Audio series + player - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioSeriesExpandedLightPreview() = AudioSeriesPreview(darkTheme = false)

@Preview(name = "Audio series + player - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioSeriesExpandedDarkPreview() = AudioSeriesPreview(darkTheme = true)

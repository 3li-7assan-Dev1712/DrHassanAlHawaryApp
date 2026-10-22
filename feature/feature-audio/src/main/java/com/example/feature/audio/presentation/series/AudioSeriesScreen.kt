package com.example.feature.audio.presentation.series

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.SeriesList
import com.example.core.ui.components.SeriesPreviewData
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Audio
import com.example.feature.audio.presentation.components.AudioListItem
import com.example.feature.audio.presentation.detail.AUDIO_PLAYER_PANE_KEY
import com.example.feature.audio.presentation.detail.AudioDetailViewModel
import com.example.feature.audio.presentation.detail.AudioPlayerPane
import com.example.feature.audio.presentation.detail.AudioSelection
import com.example.feature.audio.presentation.detail.AudioShareAction
import com.example.feature.audio.presentation.list.PlayerPanePreviewContent
import java.util.Date

/**
 * A series of audios ("شرح صحيح البخاري"): its header, then its lessons by episode, each
 * numbered, on the audio list's rows. Like the audio list: Expanded windows put the player
 * beside the lessons ([onSelectAudio]; the caller keeps [selectedAudio], so it survives
 * rotation and resizing), and after the window narrows to one pane a selected lesson stays
 * open in the player here ([onClearSelection] returns to the lessons). Otherwise a tap opens
 * the player screen ([onNavigateToAudioDetail]).
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
) {
    if (layoutTokens.isExpanded) {
        TwoPaneLayout(
            modifier = modifier,
            listPane = {
                AudioSeriesList(
                    state = state,
                    onAudioClick = { onSelectAudio(AudioSelection(it.title, it.audioUrl)) },
                    onNavigateBack = onNavigateBack,
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
            modifier = modifier,
        )
    }
}

@Composable
private fun AudioSeriesList(
    state: SeriesScreenState<Audio>,
    onAudioClick: (Audio) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Tablet: the lesson open in the player beside the list, and the one playing there.
    selectedAudioUrl: String? = null,
    playingAudioUrl: String? = null,
) {
    SeriesList(
        state = state,
        onNavigateBack = onNavigateBack,
        key = { it.id },
        episodeNumber = { it.episodeNumber },
        emptyText = stringResource(R.string.no_audios_available),
        modifier = modifier,
    ) { audio ->
        AudioListItem(
            audio = audio,
            onClick = { onAudioClick(audio) },
            selected = audio.audioUrl == selectedAudioUrl,
            playing = audio.isPlaying || audio.audioUrl == playingAudioUrl,
        )
    }
}

/** Sample lessons of «شرح مناسك الحج» for previews. */
private val SeriesAudiosPreviewData: List<Audio> = listOf(
    "مقدمة في فقه الحج" to 1_820_000L,
    "شروط وجوب الحج" to 2_410_000L,
    "المواقيت الزمانية والمكانية" to 2_105_000L,
    "الإحرام ومحظوراته" to 2_940_000L,
    "أنواع النسك: التمتع والقران والإفراد" to 2_260_000L,
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

@Composable
private fun AudioSeriesPreview(darkTheme: Boolean) {
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
        )
    }
}

// Compact and Medium show the lessons alone (the player is its own screen there).
@Preview(name = "Audio series - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioSeriesCompactLightPreview() = AudioSeriesPreview(darkTheme = false)

@Preview(name = "Audio series - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioSeriesCompactDarkPreview() = AudioSeriesPreview(darkTheme = true)

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

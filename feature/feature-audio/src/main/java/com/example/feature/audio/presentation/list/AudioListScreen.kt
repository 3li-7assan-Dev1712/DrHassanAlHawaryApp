package com.example.feature.audio.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.feature.audio.presentation.detail.AudioDetailScreen
import com.example.feature.audio.presentation.detail.PlayerPreviewState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.layoutTokens
import com.example.feature.audio.presentation.detail.AUDIO_PLAYER_PANE_KEY
import com.example.feature.audio.presentation.detail.AudioDetailViewModel
import com.example.feature.audio.presentation.detail.AudioPlayerPane
import com.example.feature.audio.presentation.detail.AudioSelection
import com.example.feature.audio.presentation.detail.AudioShareAction
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.Brand
import com.example.domain.module.Audio
import com.example.feature.audio.presentation.components.AudioListItem


/**
 * The audios of a category. Expanded windows: the list and the player side by side; a tap
 * selects the audio ([onSelectAudio]; the caller keeps [selectedAudio], so it survives
 * rotation and resizing). After the window narrows to one pane, a selected audio stays open
 * in the player here, on the same player ViewModel, so playback carries on;
 * [onClearSelection] (back) returns to the list. Otherwise the phone's list, where a tap
 * opens the player screen ([onNavigateToAudioDetail]).
 *
 * [playerViewModelKey]: the player ViewModel this entry uses (the player route passes null,
 * its own; the list route a key of its own).
 */
@Composable
fun AudioListScreen(
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    audiosViewModel: AudioListViewModel = hiltViewModel(),
    onNavigateToShare: AudioShareAction = { _, _, _, _, _, _ -> },
    selectedAudio: AudioSelection? = null,
    onSelectAudio: (AudioSelection) -> Unit = {},
    onClearSelection: () -> Unit = {},
    playerViewModelKey: String? = AUDIO_PLAYER_PANE_KEY,
) {
    val audios = audiosViewModel.audios.collectAsLazyPagingItems()
    val expanded = layoutTokens.isExpanded

    if (!expanded && selectedAudio != null) {
        val playerViewModel: AudioDetailViewModel = hiltViewModel(key = playerViewModelKey)
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
        val playerViewModel: AudioDetailViewModel = hiltViewModel(key = playerViewModelKey)
        val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
        playerState.audioUrl.takeIf { playerState.isPlaying }
    } else {
        null
    }
    AudioListAdaptiveContent(
        modifier = modifier,
        audios = audios,
        categoryTitle = audiosViewModel.categoryTitle,
        selectedAudio = selectedAudio,
        playingAudioUrl = playingAudioUrl,
        onSelectAudio = onSelectAudio,
        onNavigateToAudioDetail = onNavigateToAudioDetail,
        onNavigateBack = onNavigateBack,
        playerPane = { selection ->
            AudioPlayerPane(
                title = selection.title,
                audioUrl = selection.audioUrl,
                onNavigateToShare = onNavigateToShare,
                viewModel = hiltViewModel(key = playerViewModelKey),
            )
        },
    )
}

/**
 * [AudioListScreen] without its ViewModels (previews and UI tests pass the player pane).
 * Expanded: the list pane and the detail pane, which shows the selected audio's player or,
 * before anything is chosen, the empty state.
 */
@Composable
fun AudioListAdaptiveContent(
    audios: LazyPagingItems<Audio>,
    categoryTitle: String?,
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
                AudioListComposable(
                    audios = audios,
                    categoryTitle = categoryTitle,
                    onNavigateToAudioDetail = { title, audioUrl -> onSelectAudio(AudioSelection(title, audioUrl)) },
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
        AudioListComposable(
            modifier = modifier,
            audios = audios,
            categoryTitle = categoryTitle,
            onNavigateToAudioDetail = onNavigateToAudioDetail,
            onNavigateBack = onNavigateBack,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListComposable(
    modifier: Modifier = Modifier,
    audios: LazyPagingItems<Audio>,
    categoryTitle: String? = null,
    onNavigateToAudioDetail: (title: String, audioId: String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {},
    // Tablet: the row open in the player beside the list, and the one playing there.
    selectedAudioUrl: String? = null,
    playingAudioUrl: String? = null,
) {

    val listState = rememberLazyListState()

    // Same "list looks scrolled down" issue as the article list: the mediator's
    // remote fetch (or a genuinely new audio) can insert newer items above the
    // anchor after the local cache already rendered. Track the newest item's key
    // and snap/animate back to it when it changes.
    var topItemKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(audios.itemCount) {
        if (audios.itemCount == 0) return@LaunchedEffect
        val newestKey = audios.peek(0)?.id
        if (newestKey != null && newestKey != topItemKey) {
            if (topItemKey == null) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
            topItemKey = newestKey
        }
    }

    Scaffold(
        containerColor = Brand.colors.background,
        topBar = {
            AppTopBar(title = categoryTitle ?: stringResource(R.string.audios), onBack = onNavigateBack)
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            val isMediatorRefreshing = audios.loadState.mediator?.refresh is LoadState.Loading
            val phase = when {
                isMediatorRefreshing -> ContentPhase.Loading
                audios.itemCount == 0 && audios.loadState.refresh is LoadState.NotLoading -> ContentPhase.Empty
                else -> ContentPhase.Content
            }
            val reduced = reducedMotion

            AnimatedContent(
                targetState = phase,
                transitionSpec = { Motion.contentSwap(reduced) },
                label = "audiosContent",
            ) { shown ->
                when (shown) {
                    ContentPhase.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Brand.colors.accentStrong,
                            strokeWidth = 3.dp
                        )
                    }

                    ContentPhase.Empty, ContentPhase.Error -> Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headset,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Brand.colors.textMuted.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_audios_available),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Brand.colors.textMuted
                        )
                    }

                    ContentPhase.Content -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = audios.itemCount,
                            key = audios.itemKey { it.id }

                        ) { audioIndex ->
                            val audio = audios[audioIndex]
                            if (audio != null) {
                                AudioListItem(
                                    selected = audio.audioUrl == selectedAudioUrl,
                                    playing = audio.isPlaying || audio.audioUrl == playingAudioUrl,
                                    audio = audio,
                                    onClick = {
                                        onNavigateToAudioDetail(
                                            audio.title,
                                            audio.audioUrl
                                        )
                                    }
                                )
                            }
                        }

                        // Handle loading state for the next page (APPEND)
                        item {
                            if (audios.loadState.append is LoadState.Loading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp,
                                        color = Brand.colors.accentStrong
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

/** Sample fatwas for previews and UI tests: the Figma player frames' list. */
val AudiosPreviewData: List<Audio> = listOf(
    "حكم شراء الذهب من التطبيقات والتجار" to 346_000L,
    "حكم لبس النقاب" to 324_000L,
    "حكم المرابحة للآمر بالشراء: التمويل الأصغر" to 356_000L,
    "حكم تبديل العملة بمقابل" to 189_000L,
    "أحكام الربا" to 701_000L,
    "حكم العربون" to 158_000L,
).mapIndexed { index, (title, duration) ->
    Audio(
        id = "$index",
        categoryId = "fatawah",
        title = title,
        audioUrl = "audio-$index",
        durationInMillis = duration,
        publishDate = Date(),
        isDownloaded = index == 0,
        lastPlayedTimestamp = null,
    )
}

/** [AudiosPreviewData] as fully loaded paging data. */
fun audiosPreviewPagingData(): Flow<PagingData<Audio>> = flowOf(
    PagingData.from(
        AudiosPreviewData,
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)

/** The audio open in the player in the previews (the Figma frame's: «حكم لبس النقاب», playing). */
val AudioPreviewSelection = AudioSelection(AudiosPreviewData[1].title, AudiosPreviewData[1].audioUrl)

@Composable
private fun AudioListPreview(darkTheme: Boolean) {
    val audios = remember { audiosPreviewPagingData() }.collectAsLazyPagingItems()
    AdaptiveShellPreview(darkTheme = darkTheme) {
        AudioListAdaptiveContent(
            audios = audios,
            categoryTitle = "فتاوى",
            selectedAudio = AudioPreviewSelection,
            playingAudioUrl = AudioPreviewSelection.audioUrl,
            onSelectAudio = {},
            onNavigateToAudioDetail = { _, _ -> },
            onNavigateBack = {},
            playerPane = { PlayerPanePreviewContent() },
        )
    }
}

/** The player pane with sample state, for previews and UI tests. */
@Composable
fun PlayerPanePreviewContent() {
    AudioDetailScreen(
        uiState = PlayerPreviewState,
        onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {}, onForward = {},
        onCycleSpeed = {}, onDownload = {}, onCancelDownload = {}, onShare = {},
        inPane = true,
    )
}

// Compact and Medium show the list alone (the player is its own screen there).
@Preview(name = "Fatwas - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioListCompactLightPreview() = AudioListPreview(darkTheme = false)

@Preview(name = "Fatwas - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AudioListCompactDarkPreview() = AudioListPreview(darkTheme = true)

@Preview(name = "Fatwas - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioListMediumLightPreview() = AudioListPreview(darkTheme = false)

@Preview(name = "Fatwas - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun AudioListMediumDarkPreview() = AudioListPreview(darkTheme = true)

@Preview(name = "Fatwas + player - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioListExpandedLightPreview() = AudioListPreview(darkTheme = false)

@Preview(name = "Fatwas + player - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun AudioListExpandedDarkPreview() = AudioListPreview(darkTheme = true)

package com.example.feature.audio.presentation.detail

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.player.PlaybackService
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.SheikhPhoto
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.SharedKeys
import com.example.core.ui.theme.sharedPart
import com.example.core.ui.theme.reducedMotion
import com.example.core.ui.theme.stateChangeSpec
import com.example.domain.module.FixedCategories
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.AudioTitleCleaner
import com.example.domain.text.ShareTitleParser
import com.google.common.util.concurrent.ListenableFuture

/** Opens the share preview for the current audio and position. */
typealias AudioShareAction = (audioUrl: String, title: String, category: String?, localFilePath: String?, startMs: Long, totalDurationMs: Long) -> Unit

/** An audio chosen in the list beside the player (tablet). */
data class AudioSelection(val title: String, val audioUrl: String)

/**
 * The player. [audio]: the audio to show instead of the route's (the selection made beside
 * the list on a tablet, carried over when the window narrows to one pane).
 */
@Composable
fun AudioDetailScreen(
    onNavigateUp: () -> Unit,
    onNavigateToShare: AudioShareAction = { _, _, _, _, _, _ -> },
    viewModel: AudioDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    audio: AudioSelection? = null,
) {
    LaunchedEffect(audio) { audio?.let { viewModel.showAudio(it.title, it.audioUrl) } }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConnectMediaController(viewModel)
    AudioDetailScreen(
        uiState = uiState,
        modifier = modifier,
        onNavigateUp = onNavigateUp,
        onPlayPauseToggle = viewModel::onPlayPauseToggle,
        onSeek = viewModel::onSeek,
        onRewind = { viewModel.onRewind(SKIP_SECONDS) },
        onForward = { viewModel.onForward(SKIP_SECONDS) },
        onCycleSpeed = viewModel::onCycleSpeed,
        onDownload = viewModel::onDownloadClicked,
        onCancelDownload = viewModel::onCancelDownload,
        onShare = { shareCurrent(uiState, onNavigateToShare) },
    )
}

/** The ViewModel key of the player pane beside the audio list (one per back-stack entry). */
const val AUDIO_PLAYER_PANE_KEY = "audioPlayerPane"

/**
 * The player in the detail pane beside the audio list (Expanded): the player's top bar
 * without the back arrow (the list pane has it), then the player with its controls at
 * their designed 312dp, centred. Follows the selection: [title], [audioUrl].
 * With [onNavigateUp] it is the whole screen instead (the selection kept after the window
 * narrowed to one pane): the phone player, back arrow included.
 */
@Composable
fun AudioPlayerPane(
    title: String,
    audioUrl: String,
    onNavigateToShare: AudioShareAction,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
    viewModel: AudioDetailViewModel = hiltViewModel(key = AUDIO_PLAYER_PANE_KEY),
) {
    ConnectMediaController(viewModel)
    LaunchedEffect(audioUrl) { viewModel.showAudio(title, audioUrl) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Until the new selection reaches the ViewModel, its state is still the previous audio's.
    val shown = if (uiState.audioUrl == audioUrl) uiState else AudioDetailUiState()
    AudioDetailScreen(
        uiState = shown,
        modifier = modifier,
        onNavigateUp = onNavigateUp ?: {},
        onPlayPauseToggle = viewModel::onPlayPauseToggle,
        onSeek = viewModel::onSeek,
        onRewind = { viewModel.onRewind(SKIP_SECONDS) },
        onForward = { viewModel.onForward(SKIP_SECONDS) },
        onCycleSpeed = viewModel::onCycleSpeed,
        onDownload = viewModel::onDownloadClicked,
        onCancelDownload = viewModel::onCancelDownload,
        onShare = { shareCurrent(shown, onNavigateToShare) },
        inPane = onNavigateUp == null,
    )
}

/** Connects [viewModel] to the playback service for as long as this is composed. */
@Composable
private fun ConnectMediaController(viewModel: AudioDetailViewModel) {
    val context = LocalContext.current
    val sessionToken = remember {
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    }

    val controllerFuture: ListenableFuture<MediaController> = remember {
        MediaController.Builder(context, sessionToken).buildAsync()
    }

    LaunchedEffect(controllerFuture) {
        viewModel.mediaControllerFuture = controllerFuture
        Log.d("TAG", "AudioDetailRoute: created media controller and start listener")
    }

    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, PlaybackService::class.java)
        context.startService(serviceIntent)
        Log.d("TAG", "AudioDetailRoute: Start intent")
    }
}

private fun shareCurrent(uiState: AudioDetailUiState, onNavigateToShare: AudioShareAction) {
    val audioUrl = uiState.audioUrl
    // §1: audio metadata/duration may still be loading right after the screen
    // opens - sharing before totalDurationMillis is known breaks the share
    // screen's trim-window math (it'd receive a 0ms track duration).
    if (audioUrl != null && !uiState.isLoadingDetails && uiState.totalDurationMillis > 0L) {
        onNavigateToShare(
            audioUrl,
            uiState.title,
            uiState.category,
            uiState.localFilePath,
            uiState.currentPositionMillis,
            uiState.totalDurationMillis
        )
    }
}

private const val SKIP_SECONDS = 10

/** The player controls' designed width (Figma SeekBar, TransportRow, ActionRow): kept in the tablet pane. */
private val PlayerControlsWidth = 312.dp

@Composable
fun AudioDetailScreen(
    uiState: AudioDetailUiState,
    onNavigateUp: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onCycleSpeed: () -> Unit,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    inPane: Boolean = false,
) {
    val colors = Brand.colors
    Scaffold(
        modifier = modifier,
        // Back arrow only: the title is shown once, under the photo. None in the tablet's
        // pane: the list beside it has the back arrow.
        topBar = { AppTopBar(title = "", onBack = if (inPane) null else onNavigateUp) },
        containerColor = colors.background,
    ) { paddingValues ->
        val reduced = reducedMotion
        AnimatedContent(
            targetState = uiState.isLoadingDetails,
            transitionSpec = { Motion.contentSwap(reduced) },
            label = "playerDetails",
        ) { loading ->
            if (loading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp, color = colors.accentStrong)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        // The tablet pane: the controls keep their designed width, centred (the
                        // seek bar and transport row are drawn for it).
                        .then(if (inPane) Modifier.wrapContentWidth().widthIn(max = PlayerControlsWidth) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(8.dp))
                    SheikhPhoto(size = 200.dp, ringWidth = 2.dp, ringColor = colors.accent)
                    Spacer(Modifier.height(20.dp))
                    TitleBlock(uiState)

                    // Grows open and fades in (and back), so the controls below slide
                    // instead of jumping.
                    AnimatedVisibility(
                        visible = uiState.isDownloading,
                        enter = if (reduced) EnterTransition.None else
                            expandVertically(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)) +
                                fadeIn(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)),
                        exit = if (reduced) ExitTransition.None else
                            shrinkVertically(tween(Motion.MEDIUM, easing = Motion.EmphasizedAccelerate)) +
                                fadeOut(tween(Motion.SHORT, easing = Motion.EmphasizedAccelerate)),
                    ) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            DownloadCard(progress = uiState.downloadProgress, onCancel = onCancelDownload)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    SeekBar(uiState = uiState, onSeek = onSeek)
                    Spacer(Modifier.height(16.dp))
                    TransportRow(uiState, onRewind, onPlayPauseToggle, onForward)
                    Spacer(Modifier.height(24.dp))
                    ActionRow(uiState, onCycleSpeed, onDownload, onShare)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Category chip (only when the item has one), the cleaned title, a gold date line for
 * titles that carry one (Friday sermons: "خطبة بعنوان: … - الجمعة: ( ٢٤ صفر …"), and the
 * sheikh's name.
 */
@Composable
private fun TitleBlock(uiState: AudioDetailUiState) {
    val colors = Brand.colors
    val categoryTitle = remember(uiState.category) {
        FixedCategories.AUDIO_CATEGORIES.find { it.id == uiState.category }?.title
    }
    val (title, dateLine) = remember(uiState.title) {
        val parsed = ShareTitleParser.parse(uiState.title)
        val date = ArabicNumerals.formatDateLine(parsed.hijriDate, parsed.gregorianDate)
        if (date != null && parsed.title.isNotBlank()) {
            AudioTitleCleaner.clean(parsed.title) to date
        } else {
            AudioTitleCleaner.clean(uiState.title) to null
        }
    }

    if (categoryTitle != null) {
        Text(
            text = categoryTitle,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onAccentContainer,
            modifier = Modifier
                .background(colors.accentContainer, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(10.dp))
    }
    Text(
        text = ArabicNumerals.digits(title),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 1.5.em),
        color = colors.textPrimary,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
    if (dateLine != null) {
        Spacer(Modifier.height(4.dp))
        Text(text = dateLine, style = MaterialTheme.typography.bodyMedium, color = colors.accentText, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.height(4.dp))
    Text(text = stringResource(R.string.sheikh_name), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
}

/** Inline, non-blocking download progress with a cancel (x). Playback keeps streaming. */
@Composable
private fun DownloadCard(progress: Float, onCancel: () -> Unit) {
    val colors = Brand.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(TablerIcons.Download), contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.audio_downloading_lesson),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${ArabicNumerals.digits(progress.toInt())}٪",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accentText,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.accentStrong,
                    trackColor = colors.divider,
                    strokeCap = StrokeCap.Round,
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    painterResource(TablerIcons.X),
                    contentDescription = stringResource(R.string.audio_cancel_download),
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * One track, ONE round thumb (no stop dot, no bar thumb). Media timelines run left to
 * right even in RTL: elapsed on the left, total on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(uiState: AudioDetailUiState, onSeek: (Long) -> Unit) {
    val colors = Brand.colors
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(uiState.currentPositionMillis.toFloat()) }
    LaunchedEffect(uiState.currentPositionMillis) {
        if (!isUserSeeking) sliderPosition = uiState.currentPositionMillis.toFloat()
    }
    val sliderColors = SliderDefaults.colors(
        thumbColor = colors.accentStrong,
        activeTrackColor = colors.accentStrong,
        inactiveTrackColor = colors.divider,
        disabledActiveTrackColor = colors.divider,
        disabledInactiveTrackColor = colors.divider,
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(Modifier.fillMaxWidth()) {
            Slider(
                value = sliderPosition,
                valueRange = 0f..(uiState.totalDurationMillis.toFloat().coerceAtLeast(1f)),
                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    onSeek(sliderPosition.toLong())
                },
                enabled = uiState.totalDurationMillis > 0,
                colors = sliderColors,
                thumb = {
                    // Grows while the finger is on it, so it's clear what is being dragged.
                    // Scaled (16dp → 22dp) rather than resized, so the track layout never moves.
                    val thumbScale by animateFloatAsState(
                        targetValue = if (isUserSeeking) 22f / 16f else 1f,
                        animationSpec = stateChangeSpec(),
                        label = "seekThumb",
                    )
                    Box(
                        Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                scaleX = thumbScale
                                scaleY = thumbScale
                            }
                            .background(colors.accentStrong, CircleShape),
                    )
                },
                track = { state ->
                    SliderDefaults.Track(
                        sliderState = state,
                        modifier = Modifier.height(4.dp),
                        colors = sliderColors,
                        drawStopIndicator = null,
                        thumbTrackGapSize = 0.dp,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    ArabicNumerals.formatMediaTime(sliderPosition.toLong()),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
                Text(
                    ArabicNumerals.formatMediaTime(uiState.totalDurationMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted,
                )
            }
        }
    }
}

/** Left to right: back 10 (counter-clockwise), play/pause (gold circle), forward 10 (clockwise). */
@Composable
private fun TransportRow(
    uiState: AudioDetailUiState,
    onRewind: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onForward: () -> Unit,
) {
    val colors = Brand.colors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkipButton(TablerIcons.Rotate, stringResource(R.string.audio_rewind_10), onRewind)
            val haptics = LocalHapticFeedback.current
            val reduced = reducedMotion
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onPlayPauseToggle()
                    },
                    shape = CircleShape,
                    color = colors.accentStrong,
                    // The home audio card's gold circle morphs into this button.
                    modifier = Modifier
                        .then(
                            uiState.audioUrl?.let { Modifier.sharedPart(SharedKeys.audioPlay(it), CircleShape, scaleContent = false) }
                                ?: Modifier
                        )
                        .size(76.dp),
                ) {
                    AnimatedContent(
                        targetState = uiState.isPlaying,
                        transitionSpec = {
                            if (reduced) {
                                ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
                            } else {
                                ContentTransform(
                                    targetContentEnter = scaleIn(Motion.stateChange(), initialScale = 0.8f) + fadeIn(Motion.stateChange()),
                                    initialContentExit = fadeOut(Motion.stateChange()),
                                    sizeTransform = null,
                                )
                            }
                        },
                        contentAlignment = Alignment.Center,
                        label = "playPause",
                    ) { playing ->
                        Icon(
                            painter = painterResource(if (playing) TablerIcons.PlayerPause else TablerIcons.PlayerPlay),
                            contentDescription = stringResource(if (playing) R.string.share_pause else R.string.share_play),
                            tint = colors.onGold,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                // Buffering: a thin ring around the button; the button itself stays usable.
                if (uiState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(84.dp),
                        strokeWidth = 2.dp,
                        color = colors.accentStrong,
                        trackColor = Color.Transparent,
                    )
                }
            }
            SkipButton(TablerIcons.RotateClockwise, stringResource(R.string.audio_forward_10), onForward)
        }
    }
}

@Composable
private fun SkipButton(@DrawableRes icon: Int, description: String, onClick: () -> Unit) {
    val colors = Brand.colors
    IconButton(onClick = onClick, modifier = Modifier.size(64.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = description, tint = colors.textPrimary, modifier = Modifier.size(44.dp))
            Text(
                text = ArabicNumerals.digits(SKIP_SECONDS),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
        }
    }
}

/** Three labeled circles: السرعة (current speed), تحميل (idle / % / محفوظ), مشاركة. */
@Composable
private fun ActionRow(
    uiState: AudioDetailUiState,
    onCycleSpeed: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = Brand.colors
    val canShare = !uiState.isLoadingDetails && uiState.totalDurationMillis > 0L
    val reduced = reducedMotion
    val haptics = LocalHapticFeedback.current
    // Confirm only for a download that finished while the screen was open, not for an
    // item that was already saved.
    var sawDownloading by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isDownloading, uiState.isDownloaded) {
        if (uiState.isDownloading) sawDownloading = true
        if (uiState.isDownloaded && sawDownloading) {
            sawDownloading = false
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ActionCircle(label = stringResource(R.string.audio_speed), onClick = onCycleSpeed) {
            // The new speed rises in from below.
            AnimatedContent(
                targetState = speedLabel(uiState.playbackSpeed),
                transitionSpec = { Motion.countSlide(reduced) },
                contentAlignment = Alignment.Center,
                label = "speed",
            ) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.accentText,
                )
            }
        }
        val downloadPhase = when {
            uiState.isDownloaded -> DownloadPhase.Saved
            uiState.isDownloading -> DownloadPhase.Downloading
            else -> DownloadPhase.Idle
        }
        ActionCircle(
            label = stringResource(if (uiState.isDownloaded) R.string.audio_saved else R.string.audio_download),
            onClick = onDownload,
            enabled = !uiState.isDownloaded && !uiState.isDownloading,
        ) {
            AnimatedContent(
                targetState = downloadPhase,
                transitionSpec = {
                    when {
                        reduced -> ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
                        // Finished: the check pops in (0.6 → 1) with a small, well-damped spring.
                        targetState == DownloadPhase.Saved -> ContentTransform(
                            targetContentEnter = scaleIn(
                                spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                                initialScale = 0.6f,
                            ) + fadeIn(Motion.stateChange()),
                            initialContentExit = fadeOut(Motion.stateChange()),
                            sizeTransform = null,
                        )
                        else -> Motion.contentSwap(reduced = false)
                    }
                },
                contentAlignment = Alignment.Center,
                label = "download",
            ) { phase ->
                when (phase) {
                    DownloadPhase.Saved -> Icon(painterResource(TablerIcons.Check), null, tint = colors.success, modifier = Modifier.size(22.dp))
                    DownloadPhase.Downloading -> Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (uiState.downloadProgress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 2.dp,
                            color = colors.accentStrong,
                            trackColor = colors.divider,
                        )
                        Text(
                            "${ArabicNumerals.digits(uiState.downloadProgress.toInt())}٪",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textPrimary,
                        )
                    }
                    DownloadPhase.Idle -> Icon(painterResource(TablerIcons.Download), null, tint = colors.accent, modifier = Modifier.size(22.dp))
                }
            }
        }
        ActionCircle(label = stringResource(R.string.share), onClick = onShare, enabled = canShare) {
            Icon(
                painterResource(TablerIcons.Share),
                null,
                tint = if (canShare) colors.accent else colors.textMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ActionCircle(label: String, onClick: () -> Unit, enabled: Boolean = true, content: @Composable () -> Unit) {
    val colors = Brand.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = colors.surface,
            border = BorderStroke(0.5.dp, colors.divider),
            modifier = Modifier.size(52.dp),
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}

/** "١×", "١٫٢٥×", "٠٫٧٥×". */
private fun speedLabel(speed: Float): String {
    val western = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString().trimEnd('0')
    return ArabicNumerals.digits(western).replace('.', '٫') + "×"
}

@Preview(name = "Player - light", locale = "ar", widthDp = 360, heightDp = 780)
@Composable
private fun AudioDetailLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        AudioDetailScreen(
            uiState = AudioDetailUiState(
                title = "مقطع بعنوان: حكم تبديل العملة بمقابل",
                category = "fatawah",
                totalDurationMillis = 1_330_000L,
                currentPositionMillis = 189_000L,
                isLoadingDetails = false,
                isDownloading = true,
                downloadProgress = 45f,
            ),
            onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {}, onForward = {},
            onCycleSpeed = {}, onDownload = {}, onCancelDownload = {}, onShare = {},
        )
    }
}

@Preview(name = "Player - dark", locale = "ar", widthDp = 360, heightDp = 780)
@Composable
private fun AudioDetailDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        AudioDetailScreen(
            uiState = AudioDetailUiState(
                title = "خطبة بعنوان: فضل العشر - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م",
                category = "khotab",
                totalDurationMillis = 1_330_000L,
                isLoadingDetails = false,
                isDownloaded = true,
                playbackSpeed = 1.25f,
            ),
            onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {}, onForward = {},
            onCycleSpeed = {}, onDownload = {}, onCancelDownload = {}, onShare = {},
        )
    }
}

/** The Figma player frames' state (Medium `63:3658`, Expanded pane `57:646`): previews and UI tests. */
val PlayerPreviewState = AudioDetailUiState(
    audioUrl = "audio-3",
    title = "مقطع بعنوان: حكم تبديل العملة بمقابل",
    category = "fatawah",
    totalDurationMillis = 1_330_000L,
    currentPositionMillis = 189_000L,
    isLoadingDetails = false,
    isDownloading = true,
    downloadProgress = 45f,
    playbackSpeed = 1.25f,
)

// Medium: the player is its own screen beside the rail, the phone's layout (controls fill
// the width). Expanded shows it in the detail pane: see AudioListScreen's previews.
@Composable
private fun PlayerMediumPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        AudioDetailScreen(
            uiState = PlayerPreviewState,
            onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {}, onForward = {},
            onCycleSpeed = {}, onDownload = {}, onCancelDownload = {}, onShare = {},
        )
    }
}

@Preview(name = "Player - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun PlayerMediumLightPreview() = PlayerMediumPreview(darkTheme = false)

@Preview(name = "Player - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun PlayerMediumDarkPreview() = PlayerMediumPreview(darkTheme = true)

private enum class DownloadPhase { Idle, Downloading, Saved }

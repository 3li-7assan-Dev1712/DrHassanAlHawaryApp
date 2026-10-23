package com.example.feature.audio.presentation.series

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.AppTopBarAction
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.components.EmptyDetail
import com.example.core.ui.components.SERIES_HEADER_KEY
import com.example.core.ui.components.SERIES_STATUS_KEY
import com.example.core.ui.components.SeriesIconCircle
import com.example.core.ui.components.SeriesPreviewData
import com.example.core.ui.components.SeriesScreenState
import com.example.core.ui.components.SeriesStatus
import com.example.core.ui.components.TwoPaneLayout
import com.example.core.ui.components.animatedListItemStyle
import com.example.core.ui.components.rowClickable
import com.example.core.ui.components.seriesIcon
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.layoutTokens
import com.example.domain.module.Audio
import com.example.domain.module.Series
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.AudioTitleCleaner
import com.example.domain.text.SeriesText
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
    download: SeriesDownload = SeriesDownload(),
    onDownloadSeries: () -> Unit = {},
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
            modifier = modifier,
            playingAudioUrl = playingAudioUrl,
        )
    }
}

/**
 * The top bar (back and share, no title), the header, then "الدروس · ٥٩" and the lessons in
 * one surface card; the spinner, the error or the empty text while there are none.
 * "تشغيل من البداية" opens the first lesson as a tap on it would: the player has no queue, so
 * the next lesson is not played after it.
 */
@Composable
private fun AudioSeriesList(
    state: SeriesScreenState<Audio>,
    onAudioClick: (Audio) -> Unit,
    onNavigateBack: () -> Unit,
    download: SeriesDownload,
    onDownloadSeries: () -> Unit,
    modifier: Modifier = Modifier,
    // Tablet: the lesson open in the player beside the list, and the one playing there.
    selectedAudioUrl: String? = null,
    playingAudioUrl: String? = null,
) {
    val colors = Brand.colors
    val context = LocalContext.current
    val series = state.series
    val lessons = state.items
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AppTopBar(title = "", onBack = onNavigateBack) {
                if (series != null) {
                    val shareText = stringResource(
                        R.string.series_share_text,
                        series.title,
                        SeriesText.meta(series),
                        stringResource(R.string.share_cta_url),
                    )
                    AppTopBarAction(TablerIcons.Share, stringResource(R.string.share), onClick = { shareSeries(context, shareText) })
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        ) {
            if (series != null) {
                item(key = SERIES_HEADER_KEY) {
                    AudioSeriesHeader(
                        series = series,
                        canPlay = lessons.isNotEmpty(),
                        onPlayFromStart = { lessons.firstOrNull()?.let(onAudioClick) },
                        download = download,
                        onDownloadSeries = onDownloadSeries,
                    )
                }
            }
            if (state.phase == ContentPhase.Content) {
                item(key = LESSONS_TITLE_KEY) {
                    Text(
                        text = stringResource(R.string.series_lessons_count, ArabicNumerals.digits(lessons.size)),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                    )
                }
                itemsIndexed(lessons, key = { _, it -> it.id }) { index, audio ->
                    LessonRow(
                        audio = audio,
                        index = index,
                        count = lessons.size,
                        seriesTitle = series?.title.orEmpty(),
                        onClick = { onAudioClick(audio) },
                        selected = audio.audioUrl == selectedAudioUrl,
                        playing = audio.isPlaying || audio.audioUrl == playingAudioUrl,
                    )
                }
            } else {
                item(key = SERIES_STATUS_KEY) { SeriesStatus(state.phase, stringResource(R.string.series_empty)) }
            }
        }
    }
}

/**
 * The series' subject icon in a 64dp circle beside its title, "٥٩ درسًا · ٢٩ ساعة" and its
 * category's chip; below, "تشغيل من البداية" and the "download the series" circle.
 */
@Composable
private fun AudioSeriesHeader(
    series: Series,
    canPlay: Boolean,
    onPlayFromStart: () -> Unit,
    download: SeriesDownload,
    onDownloadSeries: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    val meta = remember(series) { SeriesText.meta(series) }
    val icon = remember(series.title) { seriesIcon(series.title) }
    val category = remember(series.categoryId) { ContentCategories.all.find { it.id == series.categoryId }?.name }
    Column(modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SeriesIconCircle(icon = icon, size = 64.dp, iconSize = 32.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 1.4.em),
                    color = colors.textPrimary,
                )
                Text(text = meta, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                if (category != null) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onAccentContainer,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .background(colors.accentContainer, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onPlayFromStart,
                enabled = canPlay,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentStrong, contentColor = colors.onGold),
            ) {
                Icon(painterResource(TablerIcons.PlayerPlay), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.series_play_from_start), fontWeight = FontWeight.SemiBold)
            }
            SeriesDownloadButton(download, onDownloadSeries, enabled = canPlay)
        }
    }
}

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
 * Lesson [index] of [count], as its slice of the lessons' card: its number (the episode, else
 * its place) in a 36dp surfaceMuted circle, the title without the series' name and numbering
 * ([AudioTitleCleaner.lesson], display only), the duration and "محفوظ" when it is saved.
 * While it plays, the circle is accentStrong with a pause icon and the title accentText; on a
 * tablet the lesson open in the player is [selected].
 */
@Composable
private fun LessonRow(
    audio: Audio,
    index: Int,
    count: Int,
    seriesTitle: String,
    onClick: () -> Unit,
    selected: Boolean,
    playing: Boolean,
) {
    val colors = Brand.colors
    val title = remember(audio.title, seriesTitle) { AudioTitleCleaner.lesson(audio.title, seriesTitle) }
    val number = audio.episodeNumber ?: (index + 1L)
    val fill = animatedListItemStyle(selected = selected && !playing).fill
    Column(Modifier.lessonCardSlice(index, count, fill)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .rowClickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(LessonNumberSize)
                    .background(if (playing) colors.accentStrong else colors.surfaceMuted, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (playing) {
                    Icon(painterResource(TablerIcons.PlayerPause), contentDescription = null, tint = colors.onGold, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = SeriesText.episodeNumber(number),
                        // labelLarge isn't one of the theme's Cairo styles.
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = Cairo),
                        color = colors.textSecondary,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.5.em),
                    color = if (playing) colors.accentText else colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LessonMeta(audio)
            }
        }
        if (index < count - 1) {
            HorizontalDivider(Modifier.padding(start = LessonDividerInset, end = 14.dp), thickness = 0.5.dp, color = colors.divider)
        }
    }
}

/** "٠١:٠٥" and, for a saved lesson, the download icon and "محفوظ" (as the fatwa rows). */
@Composable
private fun LessonMeta(audio: Audio) {
    val colors = Brand.colors
    if (audio.durationInMillis <= 0 && !audio.isDownloaded) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        if (audio.durationInMillis > 0) {
            Text(
                text = ArabicNumerals.formatDuration(audio.durationInMillis),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
        }
        if (audio.isDownloaded) {
            if (audio.durationInMillis > 0) Spacer(Modifier.width(10.dp))
            Icon(painterResource(TablerIcons.Download), contentDescription = null, tint = colors.accent, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = stringResource(R.string.audio_saved), style = MaterialTheme.typography.labelSmall, color = colors.accentText)
        }
    }
}

/**
 * Lesson [index] of [count] as its slice of one card: [fill] and the 0.5dp divider outline
 * over it, rounded at the first lesson's top and the last one's bottom, so the lazy rows
 * read as a single card. The row (and its ripple) is clipped to the slice.
 */
@Composable
private fun Modifier.lessonCardSlice(index: Int, count: Int, fill: Color): Modifier {
    val first = index == 0
    val last = index == count - 1
    val shape = RoundedCornerShape(
        topStart = if (first) LessonCardRadius else 0.dp,
        topEnd = if (first) LessonCardRadius else 0.dp,
        bottomStart = if (last) LessonCardRadius else 0.dp,
        bottomEnd = if (last) LessonCardRadius else 0.dp,
    )
    return this
        .clip(shape)
        .background(fill)
        .cardOutline(first, last, Brand.colors.divider)
}

/** The card's outline, stretched past the slice's open ends and clipped to it. */
private fun Modifier.cardOutline(first: Boolean, last: Boolean, color: Color): Modifier = drawBehind {
    val stroke = 0.5.dp.toPx()
    val radius = LessonCardRadius.toPx()
    val top = if (first) stroke / 2 else -radius - stroke
    val bottom = if (last) size.height - stroke / 2 else size.height + radius + stroke
    clipRect {
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, top),
            size = Size(size.width - stroke, bottom - top),
            cornerRadius = CornerRadius(radius - stroke / 2),
            style = Stroke(stroke),
        )
    }
}

private const val LESSONS_TITLE_KEY = "series:lessons"
private val LessonCardRadius = 14.dp
private val LessonNumberSize = 36.dp
// Under the title: past the 14dp padding, the 36dp number and the 12dp gap.
private val LessonDividerInset = 62.dp

/** The system share sheet with the series' title, its meta line and the store link. */
private fun shareSeries(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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

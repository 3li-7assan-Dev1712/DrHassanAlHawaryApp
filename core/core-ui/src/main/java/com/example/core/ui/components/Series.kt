package com.example.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.text.SeriesText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A series above a list's standalone items: the stack icon in a 40dp accentContainer circle,
 * the title, "٥٩ درسًا · ٢٨ ساعة" and a chevron, on the list items' surface card. The whole
 * card opens the series.
 */
@Composable
fun SeriesCard(series: Series, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Brand.colors
    val meta = remember(series) { SeriesText.meta(series) }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeriesIcon(size = 40.dp, iconSize = 20.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(TablerIcons.ChevronLeft),
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * The top of a series screen: the stack icon in a 48dp circle, the title in full and
 * "٥٩ درسًا · ٢٨ ساعة". The screen's top bar shows the title once this has scrolled away.
 */
@Composable
fun SeriesHeader(series: Series, modifier: Modifier = Modifier) {
    val colors = Brand.colors
    val meta = remember(series) { SeriesText.meta(series) }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SeriesIcon(size = 48.dp, iconSize = 24.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = series.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 1.4.em),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(text = meta, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

/**
 * One item of a series, its episode [number] ("١") before it at the start, centred in a
 * fixed 28dp column so the items line up.
 */
@Composable
fun EpisodeRow(number: Long, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = SeriesText.episodeNumber(number),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Brand.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

/**
 * A series screen's list: the top bar (its title shows once the header has scrolled away),
 * [SeriesHeader], then the items by episode, each numbered, or the spinner, the error or
 * [emptyText] while there are none. [item] draws one item: the feature's own row.
 */
@Composable
fun <T> SeriesList(
    state: SeriesScreenState<T>,
    onNavigateBack: () -> Unit,
    key: (T) -> Any,
    episodeNumber: (T) -> Long?,
    emptyText: String,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 8.dp,
    item: @Composable (T) -> Unit,
) {
    val colors = Brand.colors
    val listState = rememberLazyListState()
    val titleVisible by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { AppTopBar(title = state.series?.title.orEmpty(), onBack = onNavigateBack, titleVisible = titleVisible) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            state.series?.let { series ->
                item(key = SERIES_HEADER_KEY) { SeriesHeader(series, Modifier.padding(bottom = 8.dp)) }
            }
            when (state.phase) {
                ContentPhase.Content -> itemsIndexed(state.items, key = { _, it -> key(it) }) { index, it ->
                    EpisodeRow(number = episodeNumber(it) ?: (index + 1L)) { item(it) }
                }

                ContentPhase.Loading -> item(key = SERIES_STATUS_KEY) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accentStrong, strokeWidth = 3.dp)
                    }
                }

                ContentPhase.Error -> item(key = SERIES_STATUS_KEY) {
                    EmptyState(
                        illustration = Illustration.ComputerAndServer,
                        title = stringResource(R.string.empty_no_connection),
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    )
                }

                ContentPhase.Empty -> item(key = SERIES_STATUS_KEY) {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    )
                }
            }
        }
    }
}

// Apart from the items' ids in the same list.
private const val SERIES_HEADER_KEY = "series:header"
private const val SERIES_STATUS_KEY = "series:status"

@Composable
private fun SeriesIcon(size: Dp, iconSize: Dp) {
    val colors = Brand.colors
    Box(
        modifier = Modifier
            .size(size)
            .background(colors.accentContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(TablerIcons.Stack2),
            contentDescription = null,
            tint = colors.onAccentContainer,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** A series screen: the series, its cached items by episode, and the refresh from the server. */
data class SeriesScreenState<T>(
    val series: Series? = null,
    val items: List<T> = emptyList(),
    val isRefreshing: Boolean = true,
    val refreshFailed: Boolean = false,
) {
    /** Cached items show at once; the spinner or the error only while there are none. */
    val phase: ContentPhase
        get() = when {
            items.isNotEmpty() -> ContentPhase.Content
            isRefreshing -> ContentPhase.Loading
            refreshFailed -> ContentPhase.Error
            else -> ContentPhase.Empty
        }
}

/**
 * [SeriesScreenState] from the cached [series] and [items]. [refreshItems] fetches the items
 * from the server once, as the screen opens; the cache emits them as they land.
 */
fun <T> CoroutineScope.seriesScreenState(
    series: Flow<Series?>,
    items: Flow<List<T>>,
    refreshItems: suspend (Series) -> Boolean,
): StateFlow<SeriesScreenState<T>> {
    // null while the refresh runs, then whether it succeeded.
    val refreshed = MutableStateFlow<Boolean?>(null)
    launch {
        // Series screens open from a cached series' card, so it is there. A failed fetch is
        // logged at the source; a failed cache write leaves what was cached.
        val cached = series.first()
        refreshed.value = cached != null && runCatching { refreshItems(cached) }.getOrDefault(false)
    }
    return combine(series, items, refreshed) { shownSeries, shownItems, ok ->
        SeriesScreenState(series = shownSeries, items = shownItems, isRefreshing = ok == null, refreshFailed = ok == false)
    }.stateIn(this, SharingStarted.WhileSubscribed(5_000), SeriesScreenState())
}

/** Sample series for previews and UI tests. */
val SeriesPreviewData: List<Series> = listOf(
    Series(
        id = "bukhari",
        title = "شرح صحيح البخاري",
        collection = SeriesCollection.AUDIOS,
        categoryId = "scientific_lessons",
        itemCount = 59,
        totalDurationMs = 28 * 3_600_000L,
    ),
    Series(
        id = "hajj",
        title = "شرح مناسك الحج",
        collection = SeriesCollection.AUDIOS,
        categoryId = "scientific_lessons",
        itemCount = 28,
        totalDurationMs = 19 * 3_600_000L + 40 * 60_000L,
    ),
)

@Composable
private fun SeriesComponentsPreviewContent() {
    Column(
        modifier = Modifier
            .background(Brand.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SeriesHeader(SeriesPreviewData[0])
        SeriesPreviewData.forEach { SeriesCard(it, onClick = {}) }
        EpisodeRow(number = 1) { SeriesCard(SeriesPreviewData[1], onClick = {}) }
    }
}

@Preview(name = "Series - light", locale = "ar", widthDp = 360)
@Composable
private fun SeriesComponentsLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { SeriesComponentsPreviewContent() }
}

@Preview(name = "Series - dark", locale = "ar", widthDp = 360)
@Composable
private fun SeriesComponentsDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { SeriesComponentsPreviewContent() }
}

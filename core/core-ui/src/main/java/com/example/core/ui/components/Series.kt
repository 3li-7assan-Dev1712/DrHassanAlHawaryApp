package com.example.core.ui.components

import androidx.annotation.DrawableRes
import com.example.core.ui.theme.layoutTokens
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Series
import com.example.domain.module.SeriesCollection
import com.example.domain.text.SeriesIcon
import com.example.domain.text.SeriesIcons
import com.example.domain.text.ArabicNumerals
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
 * A series on a content screen: its subject's icon ([seriesIcon]) in a 48dp accentContainer
 * circle, the title (two lines at most), "٥٩ درسًا · ٢٨ ساعة" and a chevron, on the list
 * items' surface card. The whole card opens the series. Once the listener has started it
 * ([completed] lessons finished, not null), "أكملت ١٢ من ٥٩" and its bar under the meta
 * (Figma SeriesCard/Progress).
 */
@Composable
fun SeriesCard(series: Series, onClick: () -> Unit, modifier: Modifier = Modifier, completed: Int? = null) {
    val meta = remember(series) { SeriesText.meta(series) }
    val icon = remember(series.title) { seriesIcon(series.title) }
    SeriesCardLayout(icon, series.title, meta, onClick, modifier) {
        if (completed != null) SeriesCardProgress(completed, series.itemCount)
    }
}

/** "أكملت ١٢ من ٥٩" at the start, the bar filling the rest. */
@Composable
private fun SeriesCardProgress(completed: Int, itemCount: Long) {
    val total = maxOf(itemCount, completed.toLong())
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.series_completed, ArabicNumerals.digits(completed), ArabicNumerals.digits(total.toString())),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
            color = Brand.colors.accentText,
            maxLines = 1,
        )
        SeriesProgressBar(
            fraction = if (total > 0) completed.toFloat() / total else 0f,
            modifier = Modifier.weight(1f),
        )
    }
}

/** «متفرقات», after the series cards: opens the items that are in no series. */
@Composable
fun SeriesMiscCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SeriesCardLayout(
        icon = TablerIcons.Category,
        title = stringResource(R.string.series_misc),
        subtitle = stringResource(R.string.series_misc_subtitle),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun SeriesCardLayout(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier,
    below: @Composable () -> Unit = {},
) {
    val colors = Brand.colors
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeriesIconCircle(icon = icon, size = 48.dp, iconSize = 24.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
                below()
            }
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
 * The top of an article or design series' screen: its subject's icon in a 48dp circle, the
 * title in full and "١٢ مقالًا". The screen's top bar shows the title once this has scrolled
 * away. (Audio series have a header of their own, with the play button.)
 */
@Composable
fun SeriesHeader(series: Series, modifier: Modifier = Modifier) {
    val colors = Brand.colors
    val meta = remember(series) { SeriesText.meta(series) }
    val icon = remember(series.title) { seriesIcon(series.title) }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SeriesIconCircle(icon = icon, size = 48.dp, iconSize = 24.dp)
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
            if (state.phase == ContentPhase.Content) {
                itemsIndexed(state.items, key = { _, it -> key(it) }) { index, it ->
                    EpisodeRow(number = episodeNumber(it) ?: (index + 1L)) { item(it) }
                }
            } else {
                item(key = SERIES_STATUS_KEY) { SeriesStatus(state.phase, emptyText) }
            }
        }
    }
}

/**
 * A series screen with no items to show yet: the spinner while they load, the connection
 * error if that failed, or [emptyText]. Nothing for [ContentPhase.Content].
 */
@Composable
fun SeriesStatus(phase: ContentPhase, emptyText: String, modifier: Modifier = Modifier) {
    val colors = Brand.colors
    when (phase) {
        ContentPhase.Content -> Unit

        ContentPhase.Loading -> Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.accentStrong, strokeWidth = 3.dp)
        }

        ContentPhase.Error -> EmptyState(
            illustration = Illustration.ComputerAndServer,
            title = stringResource(R.string.empty_no_connection),
            modifier = modifier.fillMaxWidth().padding(top = 32.dp),
        )

        ContentPhase.Empty -> Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth().padding(top = 32.dp),
        )
    }
}

// Apart from the items' ids in the same list or grid.
const val SERIES_HEADER_KEY = "series:header"
const val SERIES_STATUS_KEY = "series:status"
private const val SERIES_MISC_KEY = "series:misc"
private const val SERIES_SUMMARY_KEY = "series:summary"

/** [icon] tinted onAccentContainer, centred in a [size] accentContainer circle. */
@Composable
fun SeriesIconCircle(icon: Int, size: Dp, iconSize: Dp, modifier: Modifier = Modifier) {
    val colors = Brand.colors
    Box(
        modifier = modifier
            .size(size)
            .background(colors.accentContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.onAccentContainer,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** The Tabler icon of a series titled [title] (see [SeriesIcons.forTitle]). */
@DrawableRes
fun seriesIcon(title: String): Int = when (SeriesIcons.forTitle(title)) {
    SeriesIcon.Kaaba -> TablerIcons.Kaaba
    SeriesIcon.MoonStars -> TablerIcons.MoonStars
    SeriesIcon.Rings -> TablerIcons.Rings
    SeriesIcon.HomeHeart -> TablerIcons.HomeHeart
    SeriesIcon.Hourglass -> TablerIcons.Hourglass
    SeriesIcon.Scale -> TablerIcons.Scale
    SeriesIcon.Feather -> TablerIcons.Feather
    SeriesIcon.Quote -> TablerIcons.Quote
    SeriesIcon.Book2 -> TablerIcons.Book2
    SeriesIcon.Stack2 -> TablerIcons.Stack2
}

/**
 * A content screen whose items are grouped in series: "١٢ سلسلة · ١٥٨ درسًا", the series'
 * cards, then «متفرقات» for
 * the items in none. Expanded: the cards in the list pane, [emptyDetail] beside them (a card
 * opens its own screen). [completion]: the lessons finished in each started series, by id.
 */
@Composable
fun SeriesCategoryContent(
    title: String,
    series: List<Series>,
    onSeriesClick: (Series) -> Unit,
    onMiscClick: () -> Unit,
    onNavigateBack: () -> Unit,
    emptyDetail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    completion: Map<String, Int> = emptyMap(),
) {
    if (layoutTokens.isExpanded) {
        TwoPaneLayout(
            modifier = modifier,
            listPane = { SeriesCards(title, series, completion, onSeriesClick, onMiscClick, onNavigateBack) },
            detailPane = { emptyDetail() },
        )
    } else {
        SeriesCards(title, series, completion, onSeriesClick, onMiscClick, onNavigateBack, modifier)
    }
}

@Composable
private fun SeriesCards(
    title: String,
    series: List<Series>,
    completion: Map<String, Int>,
    onSeriesClick: (Series) -> Unit,
    onMiscClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Brand.colors.background,
        topBar = { AppTopBar(title = title, onBack = onNavigateBack) },
    ) { padding ->
        val summary = remember(series) { SeriesText.summary(series) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (summary != null) {
                item(key = SERIES_SUMMARY_KEY) {
                    Text(text = summary, style = MaterialTheme.typography.labelSmall, color = Brand.colors.textMuted)
                }
            }
            items(series, key = { it.id }) { item ->
                SeriesCard(series = item, onClick = { onSeriesClick(item) }, completed = completion[item.id])
            }
            item(key = SERIES_MISC_KEY) { SeriesMiscCard(onClick = onMiscClick) }
        }
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
        // The first one started (Figma SeriesCard/Progress).
        SeriesCard(SeriesPreviewData[0], onClick = {}, completed = 12)
        SeriesCard(SeriesPreviewData[1], onClick = {})
        SeriesMiscCard(onClick = {})
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

/** One series per icon rule (see SeriesIcons), the long titles wrapping to two lines. */
private val SeriesListPreviewData: List<Series> = listOf(
    "شرح صحيح البخاري" to 59L,
    "شرح مناسك الحج" to 28L,
    "شرح ألفية السيوطي في مصطلح الحديث مع بيان مسائلها وأمثلتها" to 34L,
    "شرح كتاب النكاح" to 17L,
    "شرح حديث جبريل الطويل" to 6L,
    "شرح منظومة القواعد الفقهية للسعدي" to 12L,
    "فقه الصيام" to 9L,
    "فقه الحياة الزوجية" to 2L,
    "فقه الجنائز" to 1L,
    "مجالس التزكية" to 11L,
).mapIndexed { index, (title, count) ->
    Series(
        id = "preview-$index",
        title = title,
        collection = SeriesCollection.AUDIOS,
        categoryId = "scientific_lessons",
        itemCount = count,
        totalDurationMs = count * 40 * 60_000L,
    )
}

@Composable
private fun SeriesListPreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme) {
        SeriesCategoryContent(
            title = "دروس علمية",
            series = SeriesListPreviewData,
            onSeriesClick = {},
            onMiscClick = {},
            onNavigateBack = {},
            emptyDetail = {},
            // As the Figma frame: the first series started.
            completion = mapOf("preview-0" to 12),
        )
    }
}

@Preview(name = "Series list - light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun SeriesListLightPreview() = SeriesListPreview(darkTheme = false)

@Preview(name = "Series list - dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun SeriesListDarkPreview() = SeriesListPreview(darkTheme = true)

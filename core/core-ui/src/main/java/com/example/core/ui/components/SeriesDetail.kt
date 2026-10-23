package com.example.core.ui.components

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.ContentPhase
import com.example.domain.module.Series
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.SeriesText

/**
 * A series' own screen, for audio lessons and video episodes alike: the top bar (back and
 * share, no title), the header with "تشغيل من البداية" (it opens the first item, as a tap
 * on it would: [onItemClick]) and [headerAction] beside it, then "الدروس · ٥٩" and the items
 * by episode in one surface card, each drawn by [item] (a [SeriesItemRow]); the spinner, the
 * error or the empty text while there are none.
 *
 * [shareText]: the share sheet's text, given the series' title, its meta line and the store
 * link. [maxContentWidth]: the column's width at most, centred (a wide single pane).
 */
@Composable
fun <T> SeriesDetailList(
    state: SeriesScreenState<T>,
    key: (T) -> Any,
    onItemClick: (T) -> Unit,
    onNavigateBack: () -> Unit,
    @StringRes shareText: Int,
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = Dp.Unspecified,
    headerAction: @Composable (enabled: Boolean) -> Unit = {},
    item: @Composable (item: T, index: Int, count: Int) -> Unit,
) {
    val colors = Brand.colors
    val context = LocalContext.current
    val series = state.series
    val items = state.items
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AppTopBar(title = "", onBack = onNavigateBack) {
                if (series != null) {
                    val text = stringResource(
                        shareText,
                        series.title,
                        SeriesText.meta(series),
                        stringResource(R.string.share_cta_url),
                    )
                    AppTopBarAction(TablerIcons.Share, stringResource(R.string.share), onClick = { shareSeries(context, text) })
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // The whole width scrolls; only the content keeps maxContentWidth.
            val side = if (maxContentWidth.isSpecified) max(16.dp, (maxWidth - maxContentWidth) / 2) else 16.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = side, end = side, top = 4.dp, bottom = 16.dp),
            ) {
                if (series != null) {
                    item(key = SERIES_HEADER_KEY) {
                        SeriesDetailHeader(
                            series = series,
                            canPlay = items.isNotEmpty(),
                            onPlayFromStart = { items.firstOrNull()?.let(onItemClick) },
                            action = headerAction,
                        )
                    }
                }
                if (state.phase == ContentPhase.Content) {
                    item(key = SERIES_ITEMS_TITLE_KEY) {
                        Text(
                            text = stringResource(R.string.series_lessons_count, ArabicNumerals.digits(items.size)),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                        )
                    }
                    itemsIndexed(items, key = { _, it -> key(it) }) { index, it -> item(it, index, items.size) }
                } else {
                    item(key = SERIES_STATUS_KEY) { SeriesStatus(state.phase, stringResource(R.string.series_empty)) }
                }
            }
        }
    }
}

/**
 * The series' subject icon in a 64dp circle beside its title, "٥٩ درسًا · ٢٩ ساعة" and its
 * category's chip; below, "تشغيل من البداية" and [action] (audio: "download the series").
 */
@Composable
private fun SeriesDetailHeader(
    series: Series,
    canPlay: Boolean,
    onPlayFromStart: () -> Unit,
    action: @Composable (enabled: Boolean) -> Unit,
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
            action(canPlay)
        }
    }
}

/**
 * Item [index] of [count] on a series' screen, as its slice of the items' card: its [number]
 * (the episode, else its place) in a 36dp surfaceMuted circle, the [title] (two lines at
 * most) and [meta] under it. While it plays, the circle is accentStrong with a pause icon
 * and the title accentText; on a tablet the item open beside the list is [selected].
 */
@Composable
fun SeriesItemRow(
    index: Int,
    count: Int,
    number: Long,
    title: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    playing: Boolean = false,
    meta: @Composable () -> Unit = {},
) {
    val colors = Brand.colors
    val fill = animatedListItemStyle(selected = selected && !playing).fill
    Column(Modifier.seriesCardSlice(index, count, fill)) {
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
                    .size(ItemNumberSize)
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
                meta()
            }
        }
        if (index < count - 1) {
            HorizontalDivider(Modifier.padding(start = ItemDividerInset, end = 14.dp), thickness = 0.5.dp, color = colors.divider)
        }
    }
}

/**
 * Item [index] of [count] as its slice of one card: [fill] and the 0.5dp divider outline
 * over it, rounded at the first item's top and the last one's bottom, so the lazy rows read
 * as a single card. The row (and its press state) is clipped to the slice.
 */
@Composable
private fun Modifier.seriesCardSlice(index: Int, count: Int, fill: Color): Modifier {
    val first = index == 0
    val last = index == count - 1
    val shape = RoundedCornerShape(
        topStart = if (first) ItemCardRadius else 0.dp,
        topEnd = if (first) ItemCardRadius else 0.dp,
        bottomStart = if (last) ItemCardRadius else 0.dp,
        bottomEnd = if (last) ItemCardRadius else 0.dp,
    )
    return this
        .clip(shape)
        .background(fill)
        .cardOutline(first, last, Brand.colors.divider)
}

/** The card's outline, stretched past the slice's open ends and clipped to it. */
private fun Modifier.cardOutline(first: Boolean, last: Boolean, color: Color): Modifier = drawBehind {
    val stroke = 0.5.dp.toPx()
    val radius = ItemCardRadius.toPx()
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

private const val SERIES_ITEMS_TITLE_KEY = "series:lessons"
private val ItemCardRadius = 14.dp
private val ItemNumberSize = 36.dp
// Under the title: past the 14dp padding, the 36dp number and the 12dp gap.
private val ItemDividerInset = 62.dp

/** The system share sheet with [text]. */
private fun shareSeries(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

package com.example.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.components.ListeningProgressBar
import com.example.core.ui.components.listeningStatus
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.PlaybackProgress
import com.example.domain.text.AudioTitleCleaner
import com.example.feature.home.R

/**
 * An audio left part way, in «متابعة الاستماع»: the [AudioCard]'s gold play circle and cleaned
 * title, "متبقٍ ١٢ د" under it and the part heard as the lesson rows' bar. Tapping it opens
 * the player, which opens the audio where it was left.
 *
 * No shared-element keys: the same audio can be in «أحدث الصوتيات» on the same screen.
 */
@Composable
fun ContinueListeningCard(
    progress: PlaybackProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = remember(progress.title) { AudioTitleCleaner.clean(progress.title) }
    val status = listeningStatus(progress)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brand.colors.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Brand.colors.goldSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(TablerIcons.PlayerPlay),
                contentDescription = stringResource(R.string.audio_icon_description),
                tint = Brand.colors.onGold,
                modifier = Modifier.size(18.dp),
            )
        }
        Column {
            Text(
                text = title,
                color = Brand.colors.textPrimary,
                fontFamily = Cairo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (status != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = status,
                    color = Brand.colors.textMuted,
                    fontFamily = Cairo,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            ListeningProgressBar(progress.fraction, Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun ContinueListeningCardPreviewContent() {
    LazyRow(Modifier.background(Brand.colors.background).padding(12.dp)) {
        item {
            ContinueListeningCard(
                progress = PlaybackProgress(
                    audioUrl = "",
                    title = "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م",
                    positionMs = 780_000,
                    durationMs = 1_499_000,
                    updatedAt = 0L,
                ),
                onClick = {},
                modifier = Modifier.width(280.dp),
            )
        }
    }
}

@Preview(name = "Continue listening card - light", locale = "ar", widthDp = 360)
@Composable
private fun ContinueListeningCardLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { ContinueListeningCardPreviewContent() }
}

@Preview(name = "Continue listening card - dark", locale = "ar", widthDp = 360)
@Composable
private fun ContinueListeningCardDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { ContinueListeningCardPreviewContent() }
}

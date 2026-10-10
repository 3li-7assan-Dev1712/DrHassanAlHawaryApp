package com.example.feature.audio.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.R
import com.example.core.ui.components.animatedListItemStyle
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Audio
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.AudioTitleCleaner
import java.util.Date

/**
 * A compact fatwa row: 40dp play circle, the cleaned title (display only - the stored
 * title and the one passed to the player are untouched), the duration in Arabic-Indic
 * digits, and "محفوظ" with a download icon when the file is saved on the device.
 * On a tablet the row open in the player beside the list is [selected], and [playing]
 * while it plays.
 */
@Composable
fun AudioListItem(
    audio: Audio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    // The phone's list has no access to the player, so audio.isPlaying is always false
    // there today; the tablet's list beside the player passes its state.
    playing: Boolean = audio.isPlaying,
) {
    val colors = Brand.colors
    val title = remember(audio.id, audio.title) { AudioTitleCleaner.clean(audio.title) }
    val isPlaying = playing
    // The playing look wins over the selected one.
    val selection = animatedListItemStyle(selected = selected && !isPlaying)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = selection.fill,
        border = if (isPlaying) BorderStroke(0.5.dp, colors.accentStrong) else selection.border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isPlaying) colors.accentStrong else colors.accentContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) TablerIcons.PlayerPause else TablerIcons.PlayerPlay),
                    contentDescription = null,
                    tint = if (isPlaying) colors.surface else colors.onAccentContainer,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    if (audio.durationInMillis > 0) {
                        Text(
                            text = ArabicNumerals.formatDuration(audio.durationInMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                        )
                    }
                    if (audio.isDownloaded) {
                        if (audio.durationInMillis > 0) Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            painter = painterResource(TablerIcons.Download),
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.audio_saved),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accentText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioListItemPreviewContent() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AudioListItem(
            audio = Audio(
                id = "1",
                title = "خدمة المقاطع الصوتية - مقطع بعنوان: حكم لبس النقاب - خدمة فضيلة الشيخ د. حسن",
                audioUrl = "",
                durationInMillis = 1_100_000,
                publishDate = Date(),
                isDownloaded = true,
                lastPlayedTimestamp = null,
            ),
            onClick = {},
        )
        AudioListItem(
            audio = Audio(
                id = "2",
                title = "مقطع بعنوان: ⏪(٢) التعريف بصحيح البخاري",
                audioUrl = "",
                durationInMillis = 185_000,
                publishDate = Date(),
                lastPlayedTimestamp = null,
            ),
            onClick = {},
        )
    }
}

@Preview(name = "Fatwa rows - light", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun AudioListItemLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { AudioListItemPreviewContent() }
}

@Preview(name = "Fatwa rows - dark", locale = "ar", widthDp = 360, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun AudioListItemDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { AudioListItemPreviewContent() }
}

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
import com.example.core.ui.theme.SharedKeys
import com.example.core.ui.theme.sharedContainer
import com.example.core.ui.theme.sharedPart
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.HijriDate
import com.example.feature.home.R
import com.example.feature.home.domain.model.AudioFeed

/**
 * Gold play circle -> topic title over "٦ ربيع الآخر ١٤٤٨هـ · ٢٤:٥٩".
 * Tapping anywhere opens the audio, as before.
 */
@Composable
fun AudioCard(
    audio: AudioFeed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val meta = remember(audio) {
        listOfNotNull(
            audio.hijriDate?.let(ArabicNumerals::formatHijri),
            ArabicNumerals.formatDuration(audio.duration),
        ).joinToString(ArabicNumerals.DATE_SEPARATOR)
    }
    Row(
        modifier = modifier
            // Grows into the player; the gold circle morphs into its play button.
            .sharedContainer(SharedKeys.audio(audio.audioUrl))
            .clip(RoundedCornerShape(12.dp))
            .background(Brand.colors.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .sharedPart(SharedKeys.audioPlay(audio.audioUrl), CircleShape, scaleContent = false)
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
                text = audio.displayTitle,
                color = Brand.colors.textPrimary,
                fontFamily = Cairo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = meta,
                color = Brand.colors.textMuted,
                fontFamily = Cairo,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

@Preview(name = "Audio card - with topic", widthDp = 360)
@Composable
private fun AudioCardPreview() {
    LazyRow(Modifier.background(Brand.colors.background).padding(12.dp)) {
        item {
            AudioCard(
                audio = AudioFeed(
                    id = "1",
                    title = "خطبة بعنوان: فضل العشر، والأضحية - الجمعة: ( ٢٧ ذو القعدة ١٤٤٧هـ، 2026/5/15م",
                    duration = 1_499_000,
                    audioUrl = "",
                    displayTitle = "فضل العشر، والأضحية",
                    hijriDate = HijriDate(27, "ذو القعدة", 1447),
                ),
                onClick = {},
                modifier = Modifier.width(280.dp),
            )
        }
    }
}

@Preview(name = "Audio card - no topic", widthDp = 360)
@Composable
private fun AudioCardNoTopicPreview() {
    LazyRow(Modifier.background(Brand.colors.background).padding(12.dp)) {
        item {
            AudioCard(
                audio = AudioFeed(
                    id = "2",
                    title = "محاضرة - 6 ربيع الآخر 1448هـ",
                    duration = 1_499_000,
                    audioUrl = "",
                    displayTitle = "محاضرة",
                    hijriDate = HijriDate(6, "ربيع الآخر", 1448),
                ),
                onClick = {},
                modifier = Modifier.width(280.dp),
            )
        }
    }
}

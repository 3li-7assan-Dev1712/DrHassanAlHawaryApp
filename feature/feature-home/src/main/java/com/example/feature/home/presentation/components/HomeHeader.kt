package com.example.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.R as CoreR
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Cairo
import com.example.core.ui.util.HijriCalendar
import java.util.Date

/**
 * Logo (with a thin gold ring) -> "الشيخ د. حسن الهواري" over today's Hijri date.
 * No notifications bell: the app has no notifications screen to open.
 */
@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    today: Date = Date(),
) {
    val hijriToday = remember(today) { HijriCalendar.formatWithWeekday(today) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(CoreR.drawable.admin_logo_app),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, BrandTokens.goldStroke, CircleShape),
        )
        Column {
            Text(
                text = stringResource(CoreR.string.app_name),
                color = BrandTokens.textPrimary,
                fontFamily = Cairo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = hijriToday,
                color = BrandTokens.textSecondary,
                fontFamily = Cairo,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

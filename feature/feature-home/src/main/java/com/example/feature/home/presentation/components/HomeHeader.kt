package com.example.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Cairo
import com.example.core.ui.util.HijriCalendar
import com.example.feature.home.R
import java.util.Date

/**
 * Logo (with a thin gold ring) -> "الشيخ د. حسن الهواري" over today's Hijri date,
 * with a notifications bell at the far end (gold dot while any are unread).
 */
@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    today: Date = Date(),
    hasUnreadNotifications: Boolean = false,
    onNotificationsClick: () -> Unit = {},
) {
    val hijriToday = remember(today) { HijriCalendar.formatWithWeekday(today) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(CoreR.drawable.admin_logo_app),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.dp, BrandTokens.goldStroke, CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(CoreR.string.app_name),
                color = BrandTokens.textPrimary,
                fontFamily = Cairo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = hijriToday,
                color = BrandTokens.textSecondary,
                fontFamily = Cairo,
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
        IconButton(onClick = onNotificationsClick) {
            Box {
                Icon(
                    painter = painterResource(TablerIcons.Bell),
                    contentDescription = stringResource(R.string.notifications),
                    tint = BrandTokens.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(BrandTokens.gold)
                            .border(1.5.dp, BrandTokens.background, CircleShape),
                    )
                }
            }
        }
    }
}

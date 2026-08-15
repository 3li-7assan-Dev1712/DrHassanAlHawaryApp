package com.example.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.core.ui.R
import java.util.Date

@Composable
fun getRelativeTimeText(date: Date, defaultFormattedDate: String): String {
    val diff = System.currentTimeMillis() - date.time
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        minutes < 1 -> stringResource(R.string.just_now)
        minutes < 60 -> stringResource(R.string.since_minutes, minutes.toInt())
        hours < 24 -> stringResource(R.string.since_hours, hours.toInt())
        days == 2L -> stringResource(R.string.since_2_days)
        days < 7 -> stringResource(R.string.since_days, days.toInt())
        weeks == 1L -> stringResource(R.string.since_week)
        weeks == 2L -> stringResource(R.string.since_2_weeks)
        days < 30 -> stringResource(R.string.since_days, days.toInt())
        days in 30..60 -> stringResource(R.string.since_month)
        else -> defaultFormattedDate
    }
}

package com.example.feature.audio.presentation.components

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Formats a duration from milliseconds into a HH:mm:ss or mm:ss string.
 * @param millis The duration in milliseconds.
 * @return A formatted string like "01:39:21" or "39:21".
 */
fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

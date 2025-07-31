package com.example.hassanalhawary.core.util

import java.text.SimpleDateFormat
import java.util.Date


fun formatDate(
    date: Date,
    pattern: String = "dd MMM, yyyy",
    locale: java.util.Locale = java.util.Locale.getDefault() // Use java.util.Locale here
): String {
    // SimpleDateFormat requires java.util.Locale
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(date)
}
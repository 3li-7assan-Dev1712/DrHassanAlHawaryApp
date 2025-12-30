package com.example.hassanalhawary.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun formatDate(
    date: Date,
    pattern: String = "dd MMM, yyyy",
    locale: Locale = Locale.getDefault() // Use java.util.Locale here
): String {
    // SimpleDateFormat requires java.util.Locale
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(date)
}
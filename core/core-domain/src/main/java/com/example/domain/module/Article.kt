package com.example.domain.module

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Article(
    val id: String = "",
    val title: String = "",
    val publishDate: Date = Date(),
    val content: String = "",
    val type: String = "",
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false
)
fun Date.toIsoString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(this)
}
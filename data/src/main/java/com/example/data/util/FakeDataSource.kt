package com.example.data.util

import com.example.domain.module.Audio
import com.example.domain.module.Category
import java.util.Date
import java.util.concurrent.TimeUnit

object FakeDataSource {

    val audioCategories = listOf(
        Category(id = "aqidah", name = "Aqidah", imageUrl = "..."),
        Category(id = "tafsir", name = "Tafsir", imageUrl = "..."),
        Category(id = "hadith", name = "Hadith", imageUrl = "...")
    )

    val allAudios = listOf(
        Audio(
            id = "a1",
            categoryId = "aqidah",
            title = "Understanding Tawhid",
            audioUrl = "https://example.com/audio/tawhid.mp3",
            durationInMillis = TimeUnit.MINUTES.toMillis(35) + TimeUnit.SECONDS.toMillis(10), // 35:10
            publishDate = getDate(5), // Published 5 days ago
            isFavorite = true, // User favorited this one
            isDownloaded = true,
            lastPlayedTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2), // Played 2 hours ago
            isPlaying = false,
            localFilePath = "/data/user/0/com.example.app/files/audio/tawhid.mp3"
        ),
        Audio(
            id = "a2",
            categoryId = "aqidah",
            title = "The Pillars of Iman",
            audioUrl = "https://example.com/audio/iman_pillars.mp3",
            durationInMillis = TimeUnit.MINUTES.toMillis(45) + TimeUnit.SECONDS.toMillis(20), // 45:20
            publishDate = getDate(12), // Published 12 days ago
            isFavorite = false,
            isDownloaded = false,
            lastPlayedTimestamp = null, // Never played
            isPlaying = false,
            localFilePath = null
        ),

        // --- Category: Tafsir (id: "tafsir") ---
        Audio(
            id = "a3",
            categoryId = "tafsir",
            title = "Explanation of Surah Al-Fatiha",
            audioUrl = "https://example.com/audio/fatiha_tafsir.mp3",
            durationInMillis = TimeUnit.MINUTES.toMillis(55) + TimeUnit.SECONDS.toMillis(30), // 55:30
            publishDate = getDate(20), // Published 20 days ago
            isFavorite = false,
            isDownloaded = true,
            lastPlayedTimestamp = null,
            isPlaying = false,
            localFilePath = "/data/user/0/com.example.app/files/audio/fatiha_tafsir.mp3"
        ),
        Audio(
            id = "a4",
            categoryId = "tafsir",
            title = "Lessons from Surah Yusuf",
            audioUrl = "https://example.com/audio/yusuf_lessons.mp3",
            durationInMillis = TimeUnit.MINUTES.toMillis(65), // 65:00
            publishDate = getDate(3), // Published 3 days ago
            isFavorite = true,
            isDownloaded = false,
            lastPlayedTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1), // Played yesterday
            isPlaying = false,
            localFilePath = null
        ),

        // --- Category: Hadith (id: "hadith") ---
        Audio(
            id = "a5",
            categoryId = "hadith",
            title = "Forty Hadith of Imam Nawawi - Part 1",
            audioUrl = "https://example.com/audio/nawawi_part1.mp3",
            durationInMillis = TimeUnit.MINUTES.toMillis(28) + TimeUnit.SECONDS.toMillis(45), // 28:45
            publishDate = getDate(30), // Published a month ago
            isFavorite = false,
            isDownloaded = false,
            lastPlayedTimestamp = null,
            isPlaying = false,
            localFilePath = null
        )
    )

    // Helper function to get audios for a specific category
    fun getAudiosForCategory(categoryId: String): List<Audio> {
        return allAudios.filter { it.categoryId == categoryId }
    }
    private fun getDate(daysAgo: Int): Date {
        return Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo.toLong()))
    }
}
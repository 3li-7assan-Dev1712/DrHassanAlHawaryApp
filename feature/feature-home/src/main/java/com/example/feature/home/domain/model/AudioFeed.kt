package com.example.feature.home.domain.model

import com.example.domain.text.HijriDate

/** An audio as the home screen shows it; display fields are derived by the repository. */
data class AudioFeed(
    val id: String,
    /** The stored title, as-is - what navigation passes on to the audio detail screen. */
    val title: String,
    val duration: Long,
    val audioUrl: String,
    /** [title] cleaned the same way the audio list cleans it (AudioTitleCleaner). */
    val displayTitle: String = title,
    /** Computed (Umm al-Qura) from the publish date; null when the title already names a date. */
    val hijriDate: HijriDate? = null,
)

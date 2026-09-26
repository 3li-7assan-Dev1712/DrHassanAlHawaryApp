package com.example.feature.home.domain.model

import com.example.domain.text.HijriDate

/** An audio as the home screen shows it; display fields are derived by the repository. */
data class AudioFeed(
    val id: String,
    /** The stored title, as-is - what navigation passes on to the audio detail screen. */
    val title: String,
    val duration: Long,
    val audioUrl: String,
    /** The topic parsed out of [title]; falls back to the kind, then to "محاضرة". */
    val displayTitle: String = title,
    /** The date announced in the title, else computed (Umm al-Qura) from the publish date. */
    val hijriDate: HijriDate? = null,
)

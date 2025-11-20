package com.example.data_firebase.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep // Prevents Proguard/R8 from removing this class during release builds, which would break Firebase mapping.
@IgnoreExtraProperties // to ignore extra properties from firebase
data class AudioDto(
    val id: String = "",
    val title: String = "",
    val audioUrl: String ="",
    val durationInMillis: Long = 0L,
    val publishDate: Long = 0L
)
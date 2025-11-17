package com.example.data_firebase

import com.example.domain.module.Audio
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject


class FirebaseAudioSource @Inject constructor(
    private val realTimeDb: FirebaseDatabase
) {
    suspend fun getAllAudiosFromRealTimeDb(): List<Audio> {
        return emptyList()
    }
}
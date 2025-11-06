package com.example.hassanalhawary.data.remote

import com.example.hassanalhawary.domain.model.Audio
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAudioSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun getAllAudiosFromFirebase(): List<Audio> {
        val storageRef = storage.reference.child("lectures")
        val listAllResultTask = storageRef.listAll().await()

        return listAllResultTask.items.map { item ->
            val downloadUrl = item.downloadUrl.await().toString()
            Audio(
                title = item.name.substringAfter("_").substringBeforeLast("."),
                audioUrl = downloadUrl
            )
        }
    }
}
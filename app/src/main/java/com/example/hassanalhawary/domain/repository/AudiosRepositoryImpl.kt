package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.AudiosResult
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AudiosRepositoryImpl

    @Inject constructor(

        private val storage: FirebaseStorage,

        )
    : AudiosRepository {



    override fun filterAudios(audios: List<Audio>, query: String): List<Audio> {

        if (query.isBlank()) {
            return audios
        }
        return audios.filter { audio ->
            audio.title.contains(query, ignoreCase = true)
        }

    }


    override suspend fun getAllAudios(): AudiosResult {

        val audios = mutableListOf<Audio>()
        val storageRef = storage.reference.child("lectures")
        val listAllResultTask = storageRef.listAll()
        var errorMessage: String? = null
        listAllResultTask.addOnSuccessListener { listResult ->
            for (fileRef in listResult.items) {
                val audio = Audio(title = fileRef.name, id = fileRef.downloadUrl.toString())
                audios.add(audio)
            }
            val audiosResult = AudiosResult(audios, null)
        }.await()
        listAllResultTask.addOnFailureListener { exception ->
           errorMessage = exception.message
        }.await()

        return AudiosResult(audios = audios, null, errorMessage)
    }
}



package com.example.hassanalhawary.domain.repository

import android.util.Log
import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.AudiosResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(

    private val storage: FirebaseStorage,

    ) : AudiosRepository {


    override fun filterAudios(audios: List<Audio>, query: String): List<Audio> {

        if (query.isBlank()) {
            return audios
        }
        return audios.filter { audio ->
            audio.title.contains(query, ignoreCase = true)
        }

    }


    override suspend fun getAllAudios(): AudiosResult {


        return try {
            val storageRef = storage.reference.child("lectures")
            val listAllResultTask = storageRef.listAll().await()

            val audios = mutableListOf<Audio>()
            for (item: StorageReference in listAllResultTask.items) {

                val downloadUrl =
                    item.downloadUrl.await().toString() // Suspend until URL is fetched
                val audio = Audio(
                    title = item.name.substringAfter("_").substringBeforeLast("."),
                    audioUrl = downloadUrl
                )
                audios.add(audio)
                Log.d("AllLecturesViewModel", "Fetched URL for ${item.name}: $downloadUrl")
            }
            AudiosResult(audios = audios)

        } catch (e: Exception) {
            AudiosResult(errorMessage = e.message)
        }
    }

    override suspend fun getLatestAudios(): AudiosResult {
        return try {
            val storageRef = storage.reference.child("lectures")
            // get last 5 audios
            val listAllResultTask = storageRef.list(

                3

            ).await()
            val audios = mutableListOf<Audio>()
            for (item: StorageReference in listAllResultTask.items) {
                val downloadUrl =
                    item.downloadUrl.await().toString() // Suspend until URL is fetched
                val audio = Audio(
                    title = item.name.substringAfter("_").substringBeforeLast("."),
                    audioUrl = downloadUrl
                )
                audios.add(audio)
                Log.d("AllLecturesViewModel", "Fetched URL for ${item.name}: $downloadUrl")
            }
            AudiosResult(audios = audios)
        } catch (e: Exception) {
            AudiosResult(errorMessage = e.message)
        }
    }
}



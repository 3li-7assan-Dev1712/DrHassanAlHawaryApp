package com.example.hassanalhawary.domain.repository

import android.util.Log
import com.example.hassanalhawary.data.local.AudioDao
import com.example.hassanalhawary.data.local.model.AudioEntity
import com.example.hassanalhawary.data.local.model.toDomainModel
import com.example.hassanalhawary.data.remote.FirebaseAudioSource
import com.example.hassanalhawary.domain.model.Audio
import com.example.hassanalhawary.domain.model.AudiosResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(

    private val storage: FirebaseStorage,
    private val audioDao: AudioDao,
    private val firebaseAudioSource: FirebaseAudioSource
    ) : AudiosRepository {


    override fun filterAudios(audios: List<Audio>, query: String): List<Audio> {

        if (query.isBlank()) {
            return audios
        }
        return audios.filter { audio ->
            audio.title.contains(query, ignoreCase = true)
        }

    }


    override suspend fun getAudios(): Flow<List<Audio>> {

        return audioDao.getAudiosFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }


    override suspend fun syncAudios() {
        try {
            val networkAudios = firebaseAudioSource.getAllAudiosFromFirebase()
            val audioEntities = networkAudios.map { audio ->
                AudioEntity(audioUrl = audio.audioUrl, title = audio.title)
            }
            Log.d("TAG", "syncAudios: number of audios is: ${audioEntities.size}")
            audioDao.saveAudios(audioEntities)
        } catch (e: Exception) {
            // Handle error (e.g., log it). The UI will still have the old data.
            e.printStackTrace()
            Log.d("TAG", "syncAudios: error 1712")
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



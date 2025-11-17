package com.example.data

import android.util.Log
import com.example.data_firebase.FirebaseAudioSource
import com.example.data_local.AudioDao
import com.example.data_local.model.AudioEntity
import com.example.data_local.model.toDomainModel
import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(


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


    override suspend fun getAudiosFromDb(): Flow<List<Audio>> {

        return audioDao.getAudiosFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncAudiosDbWithServer() {
        try {
            val networkAudios = firebaseAudioSource.getAllAudiosFromRealTimeDb()
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

    override suspend fun getAudiosFromServer(): AudiosResult {
        return AudiosResult(emptyList())
    }

    /*override suspend fun syncAudios() {
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
    override suspend fun getAudiosFromStorage(): AudiosResult {
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
    }*/
}



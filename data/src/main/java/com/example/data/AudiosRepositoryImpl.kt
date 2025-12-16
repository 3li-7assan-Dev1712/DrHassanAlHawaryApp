package com.example.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.mappers.toDomainModel
import com.example.data.mappers.toEntity
import com.example.data.util.AudioRemoteMediator
import com.example.data_firebase.FirebaseMediaSource
import com.example.data_local.AppDatabase
import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import com.example.domain.repository.AudiosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class AudiosRepositoryImpl
@Inject constructor(
    private val appDatabase: AppDatabase,
    private val firebaseMediaSource: FirebaseMediaSource,
    private val audioRemoteMediator: AudioRemoteMediator
    ) : AudiosRepository {


    private val audioDao = appDatabase.audioDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getAudiosPagingData(query: String): Flow<PagingData<Audio>> {
        return Pager(
            config = PagingConfig(
                // Set a page size. This is passed to your RemoteMediator's 'state'.
                pageSize = 10 ,
                enablePlaceholders = false
            ),
            remoteMediator = audioRemoteMediator,
            // The PagingSourceFactory ALWAYS points to the local database (Room).
            // The RemoteMediator will fill this database for the PagingSource to read.
            pagingSourceFactory = {
                audioDao.getAudiosPagingSource(query)
            }
        ).flow.map { pagingData ->
            // The data from the PagingSource is ArticleEntity, so we map it to the domain model
            pagingData.map { articleEntity ->
                articleEntity.toDomainModel()
            }
        }
    }



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
            val networkAudios = firebaseMediaSource.getAllAudiosFromRealTimeDb()
            val audioEntities = networkAudios.map { audio ->
                audio.toEntity()
            }
            Log.d("TAG", "syncAudios: number of audios is: ${audioEntities.size}")
            audioDao.upsertAll(audioEntities)
        } catch (e: Exception) {
            // Handle error (e.g., log it). The UI will still have the old data.
            e.printStackTrace()
            Log.d("TAG", "syncAudios: error 1712")
        }
    }

    override suspend fun getAudiosFromServer(): AudiosResult {
        return AudiosResult(emptyList())
    }

    override suspend fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long
    ): Flow<UploadResult> {


        return firebaseMediaSource.uploadAudio(title, uriString, durationInMillis)



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



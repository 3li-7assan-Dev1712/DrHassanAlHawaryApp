package com.example.domain.repository

import androidx.paging.PagingData
import com.example.domain.module.Audio
import com.example.domain.module.AudiosResult
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow

interface AudiosRepository {


    fun filterAudios(audios: List<Audio>, query: String): List<Audio>

    suspend fun getAudiosFromDb(): Flow<List<Audio>>

    suspend fun getAudiosFromServer(): AudiosResult

    suspend fun uploadAudio(
        title: String,
        uriString: String,
        durationInMillis: Long,
        type: String = ""
    ): Flow<UploadResult>

    suspend fun updateAudio(
        id: String,
        title: String,
        newUriString: String?,
        existingUrl: String,
        durationInMillis: Long,
        type: String? = null
    ): Flow<UploadResult>

    suspend fun deleteAudio(audioId: String, audioUrl: String): Result<Unit>

    suspend fun getAudioById(audioId: String): Audio?

    suspend fun getAllRemoteAudios(): List<Audio>

    suspend fun downloadAudio(audio: Audio): Flow<DownloadResult>
    
    fun getAudioByUrl(url: String): Flow<Audio?>


    fun getPaginatedAudio(query: String): Flow<PagingData<Audio>>
}

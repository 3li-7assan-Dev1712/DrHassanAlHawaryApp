package com.example.data

import com.example.data_firebase.VideoFirestoreSource
import com.example.domain.repository.VideosRepository
import com.example.domain.use_cases.audios.UploadResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class VideosRepositoryImpl @Inject constructor(
    private val videoFirestoreSource: VideoFirestoreSource,
) : VideosRepository {


    override fun uploadVideo(
        title: String,
        youtubeUrl: String,
    ): Flow<UploadResult> {

        return videoFirestoreSource.uploadVideo(title, youtubeUrl)

    }
}
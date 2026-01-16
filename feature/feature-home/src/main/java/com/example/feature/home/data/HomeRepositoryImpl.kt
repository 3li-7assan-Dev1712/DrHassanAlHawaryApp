package com.example.feature.home.data

import android.util.Log
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_local.ArticleDao
import com.example.data_local.AudioDao
import com.example.data_local.ImageDao
import com.example.data_local.model.ArticleEntity
import com.example.data_local.model.AudioEntity
import com.example.data_local.model.ImageEntity
import com.example.data_local.model.ImageGroupEntity
import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.domain.model.ImageFeed
import com.example.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val audioDao: AudioDao,
    private val imageDao: ImageDao,
    private val audioFirestoreSource: AudioFirestoreSource,
    private val imageFirestoreSource: ImageFirestoreSource,
    private val firebaseArticlesSource: FirebaseArticlesSource
) : HomeRepository {


    override fun getLatestArticles(): Flow<List<ArticleFeed>> {
        return articleDao.getLatestArticles().map {
            it.map { article ->
                ArticleFeed(
                    id = article.id,
                    title = article.title,
                    contentPreview = article.content
                )
            }
        }
    }

    override fun getLatestAudios(): Flow<List<AudioFeed>> {
        return audioDao.getLatestAudios().map {
            it.map { audio ->

                AudioFeed(
                    id = audio.id,
                    title = audio.title,
                    duration = audio.durationInMillis,
                    audioUrl = audio.audioUrl
                )

            }
        }
    }

    override fun getLatestImages(): Flow<List<ImageFeed>> {
        return imageDao.getLastImageGroup().map { imageGroup ->
            imageGroup.images.map { imageEntity ->
                ImageFeed(
                    imageUrl = imageEntity.imageUrl,
                )
            }
        }

    }


    override suspend fun syncLatestArticles(limit: Long) {
        val latestArticlesFromApi = firebaseArticlesSource.getArticles(null, limit).first
        // You'll need a mapper to convert Network DTOs to DB Entities
        val articleEntities = latestArticlesFromApi.map {
            ArticleEntity(
                id = it.id,
                title = it.title,
                content = it.content,
                publishDate = it.publishDate.time
            )
        }
        articleDao.upsertAll(articleEntities)
    }

    override suspend fun syncLatestAudios(limit: Int) {
        val latestAudiosFromFirebase =
            audioFirestoreSource.fetchAudioPage(startAfterKey = null, limit)
        val audioEntities = latestAudiosFromFirebase.map {
            AudioEntity(
                id = it.id,
                title = it.title,
                audioUrl = it.audioUrl,
                publishDate = it.publishDate.time,
                durationInMillis = it.durationInMillis,
            )
        }
        audioDao.upsertAll(audioEntities)
    }

    override suspend fun syncLatestImageGroup() {
//        val group = firebaseMediaSource.fetchLatestImageGroup() ?: return
        val group = imageFirestoreSource.fetchLatestImageGroup() ?: return
//        val group = group.firstOrNull()
        Log.d("syncLatestImageGroup", "syncLatestImageGroup: group == $group")
        /*if (group == null)
            return*/
        val groupId = group.id
        // saving group
        val imageGroupEntity = ImageGroupEntity(
            id = groupId,
            title = group.title,
            publishDate = group.publishDate.time,
            previewImageUrl = group.previewImageUrl
        )
        imageDao.upsertImageGroups(listOf(imageGroupEntity))
        // saving images
        val remoteImages = imageFirestoreSource.fetchImagesForGroup(groupId)
        Log.d("syncLatestImageGroup", "syncLatestImageGroup: images count = ${remoteImages.size}")
        if (remoteImages.isNotEmpty()) {
            val imageEntities = remoteImages.map {
                ImageEntity(
                    id = it.id.ifBlank { it.imageUrl },
                    groupId = groupId,
                    orderIndex = it.orderIndex,
                    imageUrl = it.imageUrl
                )
            }
            imageDao.upsertImages(imageEntities)
        }


    }
}
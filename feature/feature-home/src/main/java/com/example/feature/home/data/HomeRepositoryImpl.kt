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
        return articleDao.getLatestArticles().map { list ->
            list.map { article ->
                ArticleFeed(
                    id = article.id,
                    title = article.title,
                    contentPreview = article.content
                )
            }
        }
    }

    override fun getLatestAudios(): Flow<List<AudioFeed>> {
        return audioDao.getLatestAudios().map { list ->
            list.map { audio ->
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
        return imageDao.getLastImageGroup().map { imageGroupWithImages ->
            imageGroupWithImages?.images?.map { imageEntity ->
                ImageFeed(
                    imageUrl = imageEntity.imageUrl,
                )
            } ?: emptyList()
        }
    }


    override suspend fun syncLatestArticles(limit: Long) {
        try {
            val (articlesPage, _) = firebaseArticlesSource.getArticlesPage(null, limit)
            
            val (deletedItems, activeItems) = articlesPage.partition { it.isDeleted }

            deletedItems.forEach { articleDao.deleteById(it.id) }

            val articleEntities = activeItems.map { dto ->
                ArticleEntity(
                    id = dto.id,
                    title = dto.title,
                    content = dto.content,
                    publishDate = dto.publishDate?.toDate()?.time ?: 0L,
                    updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
                    isDeleted = dto.isDeleted
                )
            }
            articleDao.upsertAll(articleEntities)
        } catch (e: Exception) {
            Log.e("HomeRepositoryImpl", "syncLatestArticles failed", e)
        }
    }

    override suspend fun syncLatestAudios(limit: Int) {
        try {
            val latestAudiosFromFirebase =
                audioFirestoreSource.fetchAudioPage(startAfterPublishDate = null, limit)
            
            val (deletedItems, activeItems) = latestAudiosFromFirebase.partition { it.isDeleted }

            deletedItems.forEach { audioDao.deleteById(it.id) }

            val audioEntities = activeItems.map { dto ->
                AudioEntity(
                    id = dto.id,
                    title = dto.title,
                    audioUrl = dto.audioUrl,
                    durationInMillis = dto.durationInMillis,
                    publishDate = dto.publishDate?.toDate()?.time ?: 0L,
                    updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
                    isDeleted = dto.isDeleted
                )
            }
            audioDao.upsertAll(audioEntities)
        } catch (e: Exception) {
            Log.e("HomeRepositoryImpl", "syncLatestAudios failed", e)
        }
    }

    override suspend fun syncLatestImageGroup() {
        try {
            val group = imageFirestoreSource.fetchLatestImageGroup() ?: return
            val groupId = group.id
            
            // saving group
            val imageGroupEntity = ImageGroupEntity(
                id = groupId,
                title = group.title,
                publishDate = group.publishDate.time,
                previewImageUrl = group.previewImageUrl,
                updatedAt = System.currentTimeMillis(),
                isDeleted = false
            )
            imageDao.upsertImageGroups(listOf(imageGroupEntity))
            
            // saving images
            val remoteImages = imageFirestoreSource.fetchImagesForGroup(groupId)
            if (remoteImages.isNotEmpty()) {
                val imageEntities = remoteImages.map { img ->
                    ImageEntity(
                        id = img.id.ifBlank { img.imageUrl },
                        groupId = groupId,
                        orderIndex = img.orderIndex,
                        imageUrl = img.imageUrl
                    )
                }
                imageDao.upsertImages(imageEntities)
            }
        } catch (e: Exception) {
            Log.e("HomeRepositoryImpl", "syncLatestImageGroup failed", e)
        }
    }
}

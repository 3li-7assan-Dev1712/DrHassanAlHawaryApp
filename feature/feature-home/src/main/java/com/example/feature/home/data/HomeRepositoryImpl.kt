package com.example.feature.home.data

import android.util.Log
import androidx.room.withTransaction
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_local.AppDatabase
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
    private val appDatabase: AppDatabase,
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
                    type = dto.type,
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


            appDatabase.withTransaction {
                deletedItems.forEach { dto ->
                    audioDao.deleteById(dto.id)
                }

                if (activeItems.isNotEmpty()) {
                    val serverAudioIds = activeItems.map { it.id }
                    val localAudiosMap =
                        audioDao.getAudiosByIds(serverAudioIds).associateBy { it.id }

                    val mergedEntities = activeItems.map { dto ->
                        val localAudio = localAudiosMap[dto.id]

                        val serverEntity = AudioEntity(
                            id = dto.id,
                            title = dto.title,
                            audioUrl = dto.audioUrl,
                            durationInMillis = dto.durationInMillis,
                            publishDate = dto.publishDate?.toDate()?.time ?: 0L,
                            updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
                            isDeleted = dto.isDeleted,
                            type = dto.type
                        )

                        if (localAudio != null) {
                            serverEntity.copy(
                                isFavorite = localAudio.isFavorite,
                                lastPlayedTimestamp = localAudio.lastPlayedTimestamp,
                                localFilePath = localAudio.localFilePath,
                                isDownloaded = localAudio.isDownloaded,
                            )
                        } else {
                            Log.d("HomeRepositoryImpl", "syncLatestAudios: going to use server entity")
                            serverEntity
                        }
                    }
                    audioDao.upsertAll(mergedEntities)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeRepositoryImpl", "syncLatestAudios failed", e)
        }
    }

    override suspend fun syncLatestImageGroup() {
        try {
            // Unlike syncLatestArticles/syncLatestAudios, this used to only ever fetch
            // fetchLatestImageGroup() - a server-side query already filtered to
            // isDeleted == false - and upsert whatever it returned. That never told the
            // local db about a group that WAS the cached latest but got deleted since:
            // the filtered query just silently stops returning it, nothing here ever
            // pruned the stale local row, and it kept winning getLastImageGroup()'s
            // "most recent isDeleted=0 row" query forever. Fetching a small unfiltered
            // batch and reconciling deletions - same pattern the article/audio syncs
            // already use - fixes that.
            val (recentGroups, _) = imageFirestoreSource.fetchImageGroupsPage(
                startAfterPublishDate = null,
                limit = RECENT_GROUPS_SYNC_LIMIT,
            )
            val (deletedGroups, activeGroups) = recentGroups.partition { it.isDeleted }

            appDatabase.withTransaction {
                deletedGroups.forEach { imageDao.deleteGroupWithImages(it.id) }

                if (activeGroups.isNotEmpty()) {
                    val entities = activeGroups.map { group ->
                        ImageGroupEntity(
                            id = group.id,
                            title = group.title,
                            publishDate = group.publishDate.time,
                            previewImageUrl = group.previewImageUrl,
                            updatedAt = System.currentTimeMillis(),
                            isDeleted = false,
                            type = group.type,
                        )
                    }
                    imageDao.upsertImageGroups(entities)
                }
            }

            // activeGroups is ordered by publishDate DESC (server query order), so the
            // first entry is the actual latest - only its images are needed here.
            val latestActiveGroupId = activeGroups.firstOrNull()?.id ?: return

            val remoteImages = imageFirestoreSource.fetchImagesForGroup(latestActiveGroupId)
            if (remoteImages.isNotEmpty()) {
                val imageEntities = remoteImages.map { img ->
                    ImageEntity(
                        id = img.id.ifBlank { img.imageUrl },
                        groupId = latestActiveGroupId,
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

    companion object {
        private const val RECENT_GROUPS_SYNC_LIMIT = 5
    }
}

package com.example.feature.home.data

import com.example.data_local.ArticleDao
import com.example.data_local.AudioDao
import com.example.data_local.ImageDao
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
    private val imageDao: ImageDao
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
}
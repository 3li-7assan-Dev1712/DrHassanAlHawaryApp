package com.example.feature.home.domain.repository


import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.domain.model.ImageFeed
import kotlinx.coroutines.flow.Flow

interface HomeRepository {


    // getting data from room database
    fun getLatestArticles(): Flow<List<ArticleFeed>>
    fun getLatestAudios(): Flow<List<AudioFeed>>
    fun getLatestImages(): Flow<List<ImageFeed>>


    // sync data in room database with firebase
    suspend fun syncLatestArticles(limit: Long)
    suspend fun syncLatestAudios(limit: Int)
    suspend fun syncLatestImageGroup()


}
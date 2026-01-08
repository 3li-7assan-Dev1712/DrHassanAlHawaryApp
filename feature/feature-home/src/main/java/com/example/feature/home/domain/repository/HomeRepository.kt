package com.example.feature.home.domain.repository


import com.example.feature.home.domain.model.ArticleFeed
import com.example.feature.home.domain.model.AudioFeed
import com.example.feature.home.domain.model.ImageFeed
import kotlinx.coroutines.flow.Flow

interface HomeRepository {


    fun getLatestArticles(): Flow<List<ArticleFeed>>
    fun getLatestAudios(): Flow<List<AudioFeed>>
    fun getLatestImages(): Flow<List<ImageFeed>>


}
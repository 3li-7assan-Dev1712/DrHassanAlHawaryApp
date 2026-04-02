package com.example.domain.repository

import com.example.domain.module.Channel
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(): Flow<List<Channel>>
    suspend fun addChannel(channel: Channel): Result<Unit>
    suspend fun deleteChannel(channelId: String): Result<Unit>
}

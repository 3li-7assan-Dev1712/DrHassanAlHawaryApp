package com.example.data

import com.example.data_firebase.ChannelFirestoreSource
import com.example.domain.module.Channel
import com.example.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val channelFirestoreSource: ChannelFirestoreSource
) : ChannelRepository {

    override fun getChannels(): Flow<List<Channel>> {
        return channelFirestoreSource.getChannels()
    }

    override suspend fun addChannel(channel: Channel): Result<Unit> {
        return channelFirestoreSource.addChannel(channel)
    }

    override suspend fun deleteChannel(channelId: String): Result<Unit> {
        return channelFirestoreSource.deleteChannel(channelId)
    }
}

package com.example.domain.use_cases.channel

import com.example.domain.module.Channel
import com.example.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChannelsUseCase @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    operator fun invoke(): Flow<List<Channel>> {
        return channelRepository.getChannels()
    }
}

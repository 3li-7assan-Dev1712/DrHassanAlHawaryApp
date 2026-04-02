package com.example.domain.use_cases.channel

import com.example.domain.module.Channel
import com.example.domain.repository.ChannelRepository
import javax.inject.Inject

class AddChannelUseCase @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channel: Channel): Result<Unit> {
        if (channel.channelId.isBlank()) return Result.failure(Exception("Channel ID cannot be empty"))
        if (channel.batch.isBlank()) return Result.failure(Exception("Batch cannot be empty"))
        if (channel.order < 0) return Result.failure(Exception("Order must be a positive number"))
        
        return channelRepository.addChannel(channel)
    }
}

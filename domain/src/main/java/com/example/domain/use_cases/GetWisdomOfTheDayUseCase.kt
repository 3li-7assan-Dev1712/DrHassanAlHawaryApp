package com.example.domain.use_cases

import com.example.domain.module.Wisdom
import com.example.domain.module.WisdomResult
import com.example.domain.repository.NetworkRepository
import com.example.domain.repository.WisdomRepository

import javax.inject.Inject

class GetWisdomOfTheDayUseCase @Inject constructor(
    private val wisdomRepository: WisdomRepository,
    private val networkRepository: NetworkRepository

) {


    suspend operator fun invoke(): WisdomResult<Wisdom, String> =
        /* val currentNetworkStatus = networkMonitor.networkStatus.first()
         return if (currentNetworkStatus == NetworkStatus.Unavailable) {
             WisdomResult.Failure("No internet connection")
         } else {
         }*/
        wisdomRepository.getWisdomOfTheDay()


}
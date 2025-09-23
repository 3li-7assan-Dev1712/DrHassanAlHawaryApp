package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.core.util.NetworkMonitor
import com.example.hassanalhawary.domain.model.Wisdom
import com.example.hassanalhawary.domain.model.WisdomResult
import com.example.hassanalhawary.domain.repository.WisdomRepository
import javax.inject.Inject

class GetWisdomOfTheDayUseCase @Inject constructor(
    private val wisdomRepository: WisdomRepository,
    private val networkMonitor: NetworkMonitor

) {


    suspend operator fun invoke(): WisdomResult<Wisdom, String> =
        /* val currentNetworkStatus = networkMonitor.networkStatus.first()
         return if (currentNetworkStatus == NetworkStatus.Unavailable) {
             WisdomResult.Failure("No internet connection")
         } else {
         }*/
        wisdomRepository.getWisdomOfTheDay()


}
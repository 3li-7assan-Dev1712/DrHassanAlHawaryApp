package com.example.hassanalhawary.domain.use_cases

import com.example.hassanalhawary.domain.repository.WisdomRepository
import javax.inject.Inject

class GetWisdomOfTheDayUseCase @Inject constructor(
    private val wisdomRepository: WisdomRepository

) {


    suspend operator fun invoke(): String {
        return wisdomRepository.getWisdomOfTheDay()
    }


}
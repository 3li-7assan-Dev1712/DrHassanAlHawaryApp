package com.example.data


import com.example.domain.module.Wisdom
import com.example.domain.module.WisdomResult
import com.example.domain.repository.WisdomRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class WisdomRepositoryImpl @Inject constructor(): WisdomRepository {




    override suspend fun getWisdomOfTheDay(): WisdomResult<Wisdom, String> {

        delay(2000)

        val wisdom = Wisdom(id = "1", wisdomText = "The only way to do great work is to love what you do. - Steve Jobs")
        return WisdomResult.Success(wisdom)


    }
}
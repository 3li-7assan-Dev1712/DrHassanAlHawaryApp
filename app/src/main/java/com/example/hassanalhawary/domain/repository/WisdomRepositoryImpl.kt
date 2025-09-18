package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Wisdom
import com.example.hassanalhawary.domain.model.WisdomResult
import kotlinx.coroutines.delay

class WisdomRepositoryImpl: WisdomRepository {




    override suspend fun getWisdomOfTheDay(): WisdomResult<Wisdom, String> {

        delay(2000)

        val wisdom = Wisdom(id = "1", wisdomText = "The only way to do great work is to love what you do. - Steve Jobs")
        return WisdomResult.Success(wisdom)


    }
}
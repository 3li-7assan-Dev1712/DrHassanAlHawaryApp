package com.example.hassanalhawary.domain.repository

import com.example.hassanalhawary.domain.model.Wisdom
import com.example.hassanalhawary.domain.model.WisdomResult

interface WisdomRepository {



    suspend fun getWisdomOfTheDay(): WisdomResult<Wisdom, String>


}
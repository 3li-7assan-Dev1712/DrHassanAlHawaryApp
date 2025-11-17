package com.example.domain.repository

import com.example.domain.module.Wisdom
import com.example.domain.module.WisdomResult


interface WisdomRepository {



    suspend fun getWisdomOfTheDay(): WisdomResult<Wisdom, String>


}
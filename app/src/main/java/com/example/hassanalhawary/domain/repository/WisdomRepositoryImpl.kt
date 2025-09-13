package com.example.hassanalhawary.domain.repository

import kotlinx.coroutines.delay

class WisdomRepositoryImpl: WisdomRepository {




    override suspend fun getWisdomOfTheDay(): String {

        delay(2000)

        return "Wisdom from the server!"


    }
}
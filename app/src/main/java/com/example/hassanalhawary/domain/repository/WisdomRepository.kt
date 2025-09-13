package com.example.hassanalhawary.domain.repository

interface WisdomRepository {



    suspend fun getWisdomOfTheDay(): String


}
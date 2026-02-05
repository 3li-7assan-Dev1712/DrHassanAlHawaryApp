package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {


    fun observeCompleted(): Flow<Boolean>


    suspend fun setCompleted(completed: Boolean)



}
package com.example.search.domain.repository

import com.example.search.domain.model.SearchResult

interface SearchRepository {



    suspend fun searchContent(query: String): List<SearchResult>



}
package com.example.domain.module

data class SearchResultMetaData(
    val objectID:String,
    val title: String? = null,
    val type: String? = null,
    val url: String? = null,
    val content: String? = null,
    val index: Int? = null
)

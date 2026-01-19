package com.example.search.data

import com.example.search.domain.model.SearchResult
import com.example.search.domain.repository.SearchRepository

class SearchRepositoryImpl(

    ) : SearchRepository {

    private val indexName = "search_content"


    override suspend fun searchContent(query: String): List<SearchResult> {

     /*   val searchParams = client.search(
            query = Query(
                query = query,
                attributesToRetrieve = listOf("id", "type", "title", "description", "groupId"),
                hitsPerPage = 50
            ),
            indexName = this.indexName
        )

        val response = client.searchSingleIndex(
            indexName = indexName,
            searchParams = searchParams
        )

        return response.hits.map { hit ->
            SearchResult(
                id = hit["id"] as String,
                type = hit["type"] as String,
                title = hit["title"] as String,
                description = hit["description"] as? String,
                groupId = hit["groupId"] as? String
            )
        }

*/
        return listOf()
    }
}
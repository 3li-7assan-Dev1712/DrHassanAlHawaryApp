package com.example.domain.repository

interface ContentSyncRepository {

    /**
     * Brings the cached audios, articles, videos and image groups up to date with what
     * changed on the server since the last sync (by updatedAt).
     */
    suspend fun syncChangedContent()
}

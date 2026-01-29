package com.example.study.domain.repository

import com.example.domain.module.Playlist
import com.example.study.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudyRepository {


    fun getStudentData(): Flow<Student?>

    suspend fun saveStudentData(telegramId: Long)

    suspend fun disconnectTelegram()


    fun getPlaylistsForLevel(level: Int): Flow<List<Playlist>?>

    suspend fun syncPlaylists(level: Int)






}
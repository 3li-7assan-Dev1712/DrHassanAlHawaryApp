package com.example.data_local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data_local.model.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {


    @Query("SELECT * FROM students LIMIT 1")
    fun getCurrentStudentData(): Flow<StudentEntity?>


    @Query("SELECT * FROM students WHERE isChannelMember = 1")
    fun getAllStudentsInChannel(): Flow<List<StudentEntity>>

    @Upsert
    suspend fun storeStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE telegramId = :telegramId")
    suspend fun deleteStudentByTelegramId(telegramId: Long)


}
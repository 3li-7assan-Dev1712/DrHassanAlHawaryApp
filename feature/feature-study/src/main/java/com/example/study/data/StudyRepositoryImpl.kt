package com.example.study.data

import android.util.Log
import com.example.data_firebase.StudentFirestoreSource
import com.example.data_local.StudentDao
import com.example.study.data.mappers.toDomain
import com.example.study.data.mappers.toEntity
import com.example.study.domain.model.Student
import com.example.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudyRepositoryImpl @Inject constructor(
    private val studentFirestoreSource: StudentFirestoreSource,
    private val studentDao: StudentDao
) : StudyRepository {


    val TAG = "StudyRepositoryImpl"

    override fun getStudentData(): Flow<Student?> {
        return studentDao.getCurrentStudentData().map {
            it?.toDomain()
        }
    }

    override suspend fun disconnectTelegram() {

        // load the student info
        val telegramId = studentDao.getCurrentStudentData().last()?.telegramId
        if (telegramId != null) {
            studentFirestoreSource.updateStudentConnectionStatus(telegramId, false)
            studentDao.deleteStudentByTelegramId(telegramId)
        } else
            throw NullPointerException("Student data is null")
    }


    override suspend fun saveStudentData(telegramId: Long) {
        Log.d(TAG, "saveStudentData: telegram id = $telegramId")
        val studentData = studentFirestoreSource.getStudentByTelegramId(telegramId)?.toEntity()
        if (studentData != null) {
            studentDao.storeStudent(studentData)
        } else {
            throw NullPointerException("Student data is null")
        }


    }
}
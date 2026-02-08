package com.example.domain.use_cases.study

import com.example.domain.module.Student
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    operator fun invoke(): Flow<Student?> {
        return studyRepository.getStudentData()
    }
}
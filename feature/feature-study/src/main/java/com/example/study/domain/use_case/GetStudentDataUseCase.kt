package com.example.study.domain.use_case

import com.example.study.domain.model.Student
import com.example.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStudentDataUseCase @Inject constructor(
    private val studyRepository: StudyRepository
) {


    operator fun invoke(): Flow<Student?> {
        return studyRepository.getStudentData()
    }
}
package com.example.study.domain.use_case


import com.example.domain.module.UserData
import com.example.domain.repository.StudyRepository
import javax.inject.Inject


class GetStudentAuthDataUseCase @Inject constructor(

    private val studyRepository: StudyRepository
) {
    suspend operator fun invoke(
    ): UserData? {
        return studyRepository.getStudentAuthData()
    }

}
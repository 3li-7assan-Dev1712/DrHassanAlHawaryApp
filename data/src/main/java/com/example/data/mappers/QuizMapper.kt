package com.example.data.mappers

import com.example.data_firebase.model.QuestionDto
import com.example.data_firebase.model.QuestionTypeDto
import com.example.data_firebase.model.QuizDto
import com.example.domain.module.Question
import com.example.domain.module.QuestionType
import com.example.domain.module.Quiz

fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id,
        title = title,
        questions = questions.map { it.toDomain() },
        createdAt = createdAt,
        type = type,
        targetLevelId = targetLevelId
    )
}

fun QuestionDto.toDomain(): Question {
    return Question(
        id = id,
        text = text,
        type = when (type) {
            QuestionTypeDto.MCQ -> QuestionType.MCQ
            QuestionTypeDto.TF -> QuestionType.TF
        },
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        correctBooleanAnswer = correctBooleanAnswer,
    )
}

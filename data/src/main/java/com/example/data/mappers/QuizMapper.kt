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
        targetLevelId = targetLevelId,
        batchIds = batchIds
    )
}

fun Quiz.toDto(): QuizDto {
    return QuizDto(
        id = id,
        title = title,
        questions = questions.map { it.toDto() },
        createdAt = createdAt,
        type = type,
        targetLevelId = targetLevelId,
        batchIds = batchIds
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

fun Question.toDto(): QuestionDto {
    return QuestionDto(
        id = id,
        text = text,
        type = when (type) {
            QuestionType.MCQ -> QuestionTypeDto.MCQ
            QuestionType.TF -> QuestionTypeDto.TF
        },
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        correctBooleanAnswer = correctBooleanAnswer,
    )
}

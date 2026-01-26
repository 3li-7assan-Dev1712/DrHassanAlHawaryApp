package com.example.study.data.mappers

import com.example.data_firebase.model.StudentDto
import com.example.data_local.model.StudentEntity
import com.example.study.domain.model.Student

fun StudentEntity.toDomain(): Student = Student(
    telegramId = telegramId,
    name = name,
    username = username,
    photoUrl = photoUrl,
    isCourseMember = isChannelMember,
    isConnectedToTelegram = isConnectedToTelegram
)

fun StudentDto.toEntity(): StudentEntity = StudentEntity(
    telegramId = id,
    name = "$firstName $lastName",
    username = username,
    photoUrl = photoUrl,
    isChannelMember = isChannelMember,
    isConnectedToTelegram = isConnectedToTelegram
)
package com.example.data.mappers

import com.example.data_firebase.model.UserDto
import com.example.data_local.model.UserEntity
import com.example.domain.module.User

fun UserDto.toDomain(): User {
    return User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        telegramId = telegramId,
        telegramUsername = telegramUsername,
        telegramFirstName = telegramFirstName,
        telegramLastName = telegramLastName,
        telegramPhotoUrl = telegramPhotoUrl,
        isChannelMember = isChannelMember,
        membershipState = membershipState,
        isConnectedToTelegram = isConnectedToTelegram,
        currentLevelId = currentLevelId
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        telegramId = telegramId,
        telegramUsername = telegramUsername,
        telegramFirstName = telegramFirstName,
        telegramLastName = telegramLastName,
        telegramPhotoUrl = telegramPhotoUrl,
        isChannelMember = isChannelMember,
        membershipState = membershipState,
        isConnectedToTelegram = isConnectedToTelegram,
        currentLevelId = currentLevelId
    )
}

fun UserEntity.toDomain(): User {
    return User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        telegramId = telegramId,
        telegramUsername = telegramUsername,
        telegramFirstName = telegramFirstName,
        telegramLastName = telegramLastName,
        telegramPhotoUrl = telegramPhotoUrl,
        isChannelMember = isChannelMember,
        membershipState = membershipState,
        isConnectedToTelegram = isConnectedToTelegram,
        currentLevelId = currentLevelId
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        telegramId = telegramId,
        telegramUsername = telegramUsername,
        telegramFirstName = telegramFirstName,
        telegramLastName = telegramLastName,
        telegramPhotoUrl = telegramPhotoUrl,
        membershipState = membershipState,
        isChannelMember = isChannelMember,
        isConnectedToTelegram = isConnectedToTelegram,
        currentLevelId = currentLevelId
    )
}

package com.example.data.mappers

import com.example.data_firebase.model.ProfileDto
import com.example.data_firebase.model.ProgressDto
import com.example.data_firebase.model.TelegramDto
import com.example.data_firebase.model.UserDto
import com.example.data_local.model.UserEntity
import com.example.domain.module.User

fun UserDto.toDomain(fallbackUid: String): User {
    val tg = telegram
    val prof = profile
    val prog = progress

    return User(
        uid = uid ?: fallbackUid,
        email = email,
        displayName = prof?.displayName,
        photoUrl = prof?.photoUrl,

        //telegram
        telegramId = tg?.id,
        telegramUsername = tg?.username,
        telegramFirstName = tg?.firstName,
        telegramLastName = tg?.lastName,
        telegramPhotoUrl = tg?.photoUrl,


        isChannelMember = tg?.isChannelMember ?: false,
        membershipState = tg?.membershipState,
        isConnectedToTelegram = tg?.isConnectedToTelegram ?: false,


        // progress
        currentLevelId = prog?.currentLevelId ?: "level_1"

    )
}

fun User.toDto(): UserDto {
    return UserDto(
        uid = uid,
        email = email,
        profile = ProfileDto(
            displayName = displayName,
            photoUrl = photoUrl
        ),
        telegram = TelegramDto(
            id = telegramId,
            username = telegramUsername,
            firstName = telegramFirstName,
            lastName = telegramLastName,
            photoUrl = telegramPhotoUrl,
            isChannelMember = isChannelMember,
            membershipState = membershipState,
            isConnectedToTelegram = isConnectedToTelegram
        ),
        progress = ProgressDto(
            currentLevelId = currentLevelId
        )
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

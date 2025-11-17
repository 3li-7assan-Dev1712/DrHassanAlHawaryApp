package com.example.domain.module

val fakeWisdom = Wisdom(id = "1", wisdomText = "The only way to do great work is to love what you do. - Steve Jobs")
data class Wisdom(
    val id: String,
    val wisdomText: String

)

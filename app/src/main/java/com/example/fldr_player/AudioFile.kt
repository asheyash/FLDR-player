package com.example.fldr_player

data class AudioFile(
    val name: String,
    val uri: String,
    val trackNumber: Int? = null
)

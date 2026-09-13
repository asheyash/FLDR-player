package com.example.fldr_player

data class TrackMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: String?,
    val genre: String?,
    val duration: Long?,
    val artwork: ByteArray?
)
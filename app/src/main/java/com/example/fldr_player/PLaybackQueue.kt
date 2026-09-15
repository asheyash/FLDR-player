package com.example.fldr_player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackQueue {

    private val _songs = MutableStateFlow<List<AudioFile>>(emptyList())

    val songs: StateFlow<List<AudioFile>> =
        _songs.asStateFlow()

    fun addSong(audioFile: AudioFile) {
        _songs.value = _songs.value + audioFile
    }

    fun addSongs(audioFiles: List<AudioFile>) {
        _songs.value = _songs.value + audioFiles
    }

    fun removeSong(audioFile: AudioFile) {
        _songs.value = _songs.value - audioFile
    }

    fun clear() {
        _songs.value = emptyList()
    }



    fun getSongs(): List<AudioFile> {
        return _songs.value
    }

    fun isEmpty(): Boolean {
        return _songs.value.isEmpty()
    }

    fun restoreSongs(audioFiles: List<AudioFile>) {
        _songs.value = audioFiles
    }
}
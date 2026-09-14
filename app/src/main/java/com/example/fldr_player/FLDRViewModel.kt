
package com.example.fldr_player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FLDRViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = MusicScanner(application)
    private val metadataReader = MetadataReader(application)
    private val playbackQueue = PlaybackQueue()
    val queueSongs = playbackQueue.songs

    fun scanFolders(
        uri: String,
        onComplete: (List<MusicFolder>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val folders = scanner.scanFolders(uri)

            withContext(Dispatchers.Main) {
                onComplete(folders)
            }
        }
    }

    fun scanSongs(
        uri: String,
        onComplete: (List<AudioFile>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val files = scanner.scanSongs(uri)

            withContext(Dispatchers.Main) {
                onComplete(files)
            }
        }
    }

    fun readMetadata(
        audioFile: AudioFile,
        onComplete: (TrackMetadata) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val metadata = metadataReader.read(audioFile.uri)

            withContext(Dispatchers.Main) {
                onComplete(metadata)
            }
        }
    }
    fun getQueueSongs(): List<AudioFile> {
        return playbackQueue.getSongs()
    }

    fun addToQueue(audioFile: AudioFile) {
        playbackQueue.addSong(audioFile)
    }

    fun addSongsToQueue(audioFiles: List<AudioFile>) {
        playbackQueue.addSongs(audioFiles)
    }

    fun removeFromQueue(audioFile: AudioFile) {
        playbackQueue.removeSong(audioFile)
    }

    fun clearQueue() {
        playbackQueue.clear()
    }

    private suspend fun sortSongsByAlbumAndTrack(
        files: List<AudioFile>
    ): List<AudioFile> {
        val songsWithMetadata = files.map { audioFile ->
            val metadata = metadataReader.read(audioFile.uri)

            audioFile to metadata
        }

        return songsWithMetadata
            .groupBy { (_, metadata) ->
                metadata.albumArtist.orEmpty() to metadata.album.orEmpty()
            }
            .toList()
            .sortedBy { (albumKey, _) ->
                albumKey.first + albumKey.second
            }
            .flatMap { (_, albumSongs) ->
                albumSongs.sortedWith(
                    compareBy<Pair<AudioFile, TrackMetadata>> {
                        it.second.discNumber ?: Int.MAX_VALUE
                    }.thenBy {
                        it.second.trackNumber ?: Int.MAX_VALUE
                    }
                )
            }
            .map { (audioFile, _) ->
                audioFile
            }
    }

    fun addFolderToQueue(
        uri: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = scanner.scanSongsRecursively(uri)
            val sortedFiles = sortSongsByAlbumAndTrack(files)

            playbackQueue.addSongs(sortedFiles)

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
    fun replaceQueueWithFolder(
        uri: String,
        onComplete: (List<AudioFile>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = scanner.scanSongs(uri)
            val sortedFiles = sortSongsByAlbumAndTrack(files)

            playbackQueue.clear()
            playbackQueue.addSongs(sortedFiles)

            withContext(Dispatchers.Main) {
                onComplete(sortedFiles)
            }
        }
    }
    fun replaceQueueWithFolderRecursively(
        uri: String,
        onComplete: (List<AudioFile>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = scanner.scanSongsRecursively(uri)
            val sortedFiles = sortSongsByAlbumAndTrack(files)

            playbackQueue.clear()
            playbackQueue.addSongs(sortedFiles)

            withContext(Dispatchers.Main) {
                onComplete(sortedFiles)
            }
        }
    }
    fun replaceQueueWithSong(
        audioFile: AudioFile,
        onComplete: (List<AudioFile>) -> Unit = {}
    ) {
        playbackQueue.clear()
        playbackQueue.addSong(audioFile)

        onComplete(listOf(audioFile))
    }
}
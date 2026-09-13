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

    fun scanFolder(uri: String, onComplete: (List<AudioFile>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = scanner.scan(uri)
            withContext(Dispatchers.Main) {
                onComplete(files)
            }
        }
    }
    fun readMetadata(audioFile: AudioFile, onComplete: (TrackMetadata) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val metadata = metadataReader.read(audioFile.uri)

            withContext(Dispatchers.Main) {
                onComplete(metadata)
            }
        }
    }

}
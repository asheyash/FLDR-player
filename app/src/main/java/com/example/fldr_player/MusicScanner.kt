package com.example.fldr_player

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri

class MusicScanner(private val context: Context) {

    private val audioExtensions = setOf(
        "flac",
        "mp3",
        "m4a",
        "ogg",
        "opus",
        "wav",
        "aac"
    )

    fun scan(uriString: String): List<AudioFile> {
        val uri = uriString.toUri()

        val root = DocumentFile.fromTreeUri(context, uri)
            ?: return emptyList()

        val results = mutableListOf<AudioFile>()

        scanDirectory(root, results)

        return results
    }

    private fun scanDirectory(
        directory: DocumentFile,
        results: MutableList<AudioFile>
    ) {
        for (file in directory.listFiles()) {
            if (file.isDirectory) {
                scanDirectory(file, results)
            } else if (file.isFile && isAudioFile(file)) {
                results.add(
                    AudioFile(
                        name = file.name ?: "Unknown",
                        uri = file.uri.toString()
                    )
                )
            }
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        val name = file.name ?: return false

        val extension = name
            .substringAfterLast('.', "")
            .lowercase()

        return extension in audioExtensions
    }
}

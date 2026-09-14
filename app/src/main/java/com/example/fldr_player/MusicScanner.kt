package com.example.fldr_player

import android.content.Context
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile

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

    fun scanFolders(uriString: String): List<MusicFolder> {
        val root = getDocumentFile(uriString)
            ?: return emptyList()

        val results = mutableListOf<MusicFolder>()

        for (file in root.listFiles()) {
            val name = file.name ?: continue

            if (
                file.isDirectory &&
                !name.startsWith(".")
            ) {
                results.add(
                    MusicFolder(
                        name = name,
                        uri = file.uri.toString()
                    )
                )
            }
        }

        return results
    }

    fun scanSongs(uriString: String): List<AudioFile> {
        val root = getDocumentFile(uriString)
            ?: return emptyList()

        val results = mutableListOf<AudioFile>()

        for (file in root.listFiles()) {
            if (file.isFile && isAudioFile(file)) {
                results.add(
                    AudioFile(
                        name = file.name ?: "Unknown",
                        uri = file.uri.toString()
                    )
                )
            }
        }

        return results
    }

    fun scanSongsRecursively(uriString: String): List<AudioFile> {
        val root = getDocumentFile(uriString)
            ?: return emptyList()

        val results = mutableListOf<AudioFile>()

        fun scanFolder(folder: DocumentFile) {
            for (file in folder.listFiles()) {
                val name = file.name ?: continue

                if (file.isDirectory) {
                    if (!name.startsWith(".")) {
                        scanFolder(file)
                    }
                } else if (file.isFile && isAudioFile(file)) {
                    results.add(
                        AudioFile(
                            name = name,
                            uri = file.uri.toString()
                        )
                    )
                }
            }
        }

        scanFolder(root)

        return results
    }
    private fun getDocumentFile(uriString: String): DocumentFile? {
        val uri = uriString.toUri()

        return if (DocumentsContract.isTreeUri(uri)) {
            DocumentFile.fromTreeUri(context, uri)
        } else {
            DocumentFile.fromSingleUri(context, uri)
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
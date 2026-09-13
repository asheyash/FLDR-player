package com.example.fldr_player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

class MetadataReader(private val context: Context) {
    fun read(uriString: String): TrackMetadata {
        val retriever = MediaMetadataRetriever()

        return try {
            val uri = Uri.parse(uriString)
            retriever.setDataSource(context, uri)

            TrackMetadata(
                title = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_TITLE
                ),
                artist = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ARTIST
                ),
                album = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ALBUM
                ),
                albumArtist = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST
                ),
                duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull(),
                genre = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_GENRE
                ),
                year = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_YEAR
                ),
                trackNumber = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER
                )?.substringBefore(" / ")?.toIntOrNull(),
                discNumber = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER
                )?.substringBefore("/")?.toIntOrNull(),
            )
        } finally {
            retriever.release()
        }
    }
}
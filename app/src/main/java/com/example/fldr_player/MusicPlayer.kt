package com.example.fldr_player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController

class MusicPlayer(context: Context) {

    private val playbackController = PlaybackController(context)

    private var player: MediaController? = null

    private fun getPlayerOrNull(): MediaController? {
        if (player == null) {
            player = playbackController.getController()
        }

        return player
    }

    fun playQueue(
        audioFiles: List<AudioFile>,
        selectedIndex: Int
    ) {
        val controller = getPlayerOrNull() ?: return

        val mediaItems = audioFiles.map { audioFile ->
            MediaItem.fromUri(audioFile.uri)
        }

        controller.setMediaItems(
            mediaItems,
            selectedIndex,
            0L
        )

        controller.repeatMode = Player.REPEAT_MODE_ALL

        controller.prepare()
        controller.play()
    }

    fun updateQueue(audioFiles: List<AudioFile>) {
        val controller = getPlayerOrNull() ?: return

        val currentCount = controller.mediaItemCount

        // Add only songs that Media3 does not already have.
        val existingUris = (0 until currentCount).mapNotNull { index ->
            controller.getMediaItemAt(index)
                .localConfiguration
                ?.uri
                ?.toString()
        }.toSet()

        val newAudioFiles = audioFiles.filter {
            it.uri !in existingUris
        }

        if (newAudioFiles.isNotEmpty()) {
            val newMediaItems = newAudioFiles.map { audioFile ->
                MediaItem.fromUri(audioFile.uri)
            }

            controller.addMediaItems(newMediaItems)
        }

        controller.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun playCurrentQueue(audioFiles: List<AudioFile>) {
        val controller = getPlayerOrNull() ?: return

        if (audioFiles.isEmpty()) {
            return
        }

        if (controller.mediaItemCount == 0) {
            playQueue(
                audioFiles = audioFiles,
                selectedIndex = 0
            )
        } else {
            controller.play()
        }
    }
    fun replaceQueue(
        audioFiles: List<AudioFile>,
        selectedIndex: Int = 0
    ) {
        val controller = getPlayerOrNull() ?: return

        if (audioFiles.isEmpty()) {
            controller.clearMediaItems()
            controller.stop()
            return
        }

        val mediaItems = audioFiles.map { audioFile ->
            MediaItem.fromUri(audioFile.uri)
        }

        controller.setMediaItems(
            mediaItems,
            selectedIndex.coerceIn(0, mediaItems.lastIndex),
            0L
        )

        controller.repeatMode = Player.REPEAT_MODE_ALL
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = getPlayerOrNull() ?: return

        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun getCurrentPosition(): Long {
        return getPlayerOrNull()?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return getPlayerOrNull()?.duration?.coerceAtLeast(0L) ?: 0L
    }

    fun seekTo(position: Long) {
        getPlayerOrNull()?.seekTo(position)
    }

    fun getPlayer(): Player? {
        return getPlayerOrNull()
    }

    fun pause() {
        getPlayerOrNull()?.pause()
    }

    fun resume() {
        getPlayerOrNull()?.play()
    }

    fun stop() {
        getPlayerOrNull()?.stop()
    }

    fun getRepeatMode(): Int {
        return getPlayerOrNull()?.repeatMode
            ?: Player.REPEAT_MODE_ALL
    }

    fun toggleRepeatMode() {
        val controller = getPlayerOrNull() ?: return

        controller.repeatMode =
            if (controller.repeatMode == Player.REPEAT_MODE_ALL) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_ALL
            }
    }

    fun release() {
        playbackController.release()
    }

    fun isPlaying(): Boolean {
        return getPlayerOrNull()?.isPlaying ?: false
    }

    fun getCurrentMediaItemIndex(): Int {
        return getPlayerOrNull()?.currentMediaItemIndex ?: -1
    }

    fun getCurrentMediaItemUri(): String? {
        return getPlayerOrNull()
            ?.currentMediaItem
            ?.localConfiguration
            ?.uri
            ?.toString()
    }

    fun skipToNext() {
        getPlayerOrNull()?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        getPlayerOrNull()?.seekToPreviousMediaItem()
    }
}
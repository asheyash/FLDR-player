package com.example.fldr_player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player

class MusicPlayer(context: Context) {

    private val player = ExoPlayer.Builder(context).build()

    fun play(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    fun getDuration(): Long {
        return player.duration.coerceAtLeast(0L)
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun getPlayer(): Player {
        return player
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }
}
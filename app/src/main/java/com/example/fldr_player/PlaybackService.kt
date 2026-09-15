package com.example.fldr_player

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import android.util.Log
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        const val CHANNEL_ID = "fldr_playback_channel"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        Log.d("FLDR_DEBUG", "PlaybackService onCreate")

        createNotificationChannel()

        val renderersFactory = DefaultRenderersFactory(this)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000, // minBufferMs
                60_000, // maxBufferMs
                2_500,  // bufferForPlaybackMs
                5_000   // bufferForPlaybackAfterRebufferMs
            )
            .build()

        val player = ExoPlayer.Builder(this, renderersFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(
            object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(
                        "FLDR_DEBUG",
                        "isPlaying changed: $isPlaying"
                    )
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(
                        "FLDR_DEBUG",
                        "playbackState changed: $playbackState"
                    )
                    if (playbackState == Player.STATE_BUFFERING) {
                        Log.d("FLDR_DEBUG", "Player is buffering...")
                    }
                }

                override fun onPlayWhenReadyChanged(
                    playWhenReady: Boolean,
                    reason: Int
                ) {
                    Log.d(
                        "FLDR_DEBUG",
                        "playWhenReady: $playWhenReady, reason=$reason"
                    )
                }

                override fun onPlaybackSuppressionReasonChanged(
                    playbackSuppressionReason: Int
                ) {
                    Log.d(
                        "FLDR_DEBUG",
                        "suppressionReason: $playbackSuppressionReason"
                    )
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(
                        "FLDR_DEBUG",
                        "PLAYER ERROR: ${error.errorCodeName} (${error.errorCode})",
                        error
                    )
                    // If the error is related to the decoder or high bitrate, we might want to skip or stop.
                    if (error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED) {
                        Log.w("FLDR_DEBUG", "Critical playback error, stopping player to prevent loop.")
                        player.stop()
                    }
                }

                override fun onMediaItemTransition(
                    mediaItem: androidx.media3.common.MediaItem?,
                    reason: Int
                ) {
                    Log.d(
                        "FLDR_DEBUG",
                        "media item transition: ${mediaItem?.mediaId}, reason: $reason"
                    )
                }
            }
        )

        mediaSession = MediaSession.Builder(
            this,
            player
        )
            .setCallback(object : MediaSession.Callback {
                @Suppress("DEPRECATION")
                override fun onPlayerCommandRequest(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    playerCommand: Int
                ): Int {
                    Log.d("FLDR_DEBUG", "Session Command: $playerCommand from ${controller.packageName}")
                    return super.onPlayerCommandRequest(session, controller, playerCommand)
                }
            })
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Playback Service"
            val descriptionText = "Handles music playback"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d("FLDR_DEBUG", "PlaybackService onDestroy")

        mediaSession?.run {
            player.release()
            release()
        }

        mediaSession = null

        super.onDestroy()
    }
}
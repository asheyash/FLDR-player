package com.example.fldr_player

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class PlaybackController(context: Context) {

    private val controllerFuture: ListenableFuture<MediaController>

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(
                context,
                PlaybackService::class.java
            )
        )

        controllerFuture = MediaController.Builder(
            context,
            sessionToken
        ).buildAsync()
    }

    fun getController(): MediaController? {
        if (!controllerFuture.isDone || controllerFuture.isCancelled) {
            return null
        }

        return try {
            controllerFuture.get()
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        if (controllerFuture.isDone) {
            MediaController.releaseFuture(controllerFuture)
        }
    }
}
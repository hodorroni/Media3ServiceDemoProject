package com.rony.media3tutorial.managers

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.rony.media3tutorial.providers.MediaControllerProvider
import javax.inject.Inject

class MediaControllerManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider
) {

    private val controller: MediaController?
        get() = controllerProvider.controller.value

    fun play() {
        controller?.play()
    }

    fun resume() {
        controller?.playWhenReady = true
    }

    fun pause() {
        controller?.pause()
    }

    fun seekForward(positionMs: Long) {
        controller?.seekTo(controller!!.currentPosition + positionMs)
    }

    fun seekForward(index: Int, positionMs: Long) {
        controller?.seekTo(index, positionMs)
    }

    fun seekBack(ms: Long) {
        controller?.seekTo(controller!!.currentPosition - ms)
    }

    fun setMediaItems(items: List<MediaItem>) {
        controller?.setMediaItems(items)
        controller?.prepare()
    }

    fun setMediaItems(items: List<MediaItem>, startIndex: Int = 0) {
        controller?.setMediaItems(items, startIndex, 0L)
        controller?.prepare()
    }

    fun togglePlayPause() {
        val c = controller ?: return

        when {
            c.isPlaying -> c.pause()
            c.playbackState == Player.STATE_ENDED -> {
                c.seekToDefaultPosition()
                c.play()
            }
            else -> c.play()
        }
    }

    fun playItem(mediaId: String) {
        controller?.let { c ->
            val index = (0 until c.mediaItemCount)
                .firstOrNull { i -> c.getMediaItemAt(i).mediaId == mediaId }
                ?: return

            c.seekTo(index, 0)
            c.play()
        }
    }

    fun getItemCount(): Int {
        val c = controller ?: return 0
        return c.mediaItemCount
    }

    fun previousItem() {
        val c = controller ?: return
        if (c.hasPreviousMediaItem()) {
            c.seekToPreviousMediaItem()
        }
    }

    fun nextItem() {
        val c = controller ?: return
        if (c.hasNextMediaItem()) {
            c.seekToNextMediaItem()
        }
    }

    fun restart() {
        controller?.seekTo(0)
    }
}
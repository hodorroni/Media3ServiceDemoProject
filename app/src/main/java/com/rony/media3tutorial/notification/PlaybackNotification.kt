package com.rony.media3tutorial.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PlaybackNotification {

    const val CHANNEL_ID = "media_playback"
    const val CHANNEL_NAME = "Media playback"
    const val NOTIFICATION_ID = 1001


    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW //without sound
                ).apply {
                    description = "Media playback controls"
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC //lock screen controls
                }

                manager.createNotificationChannel(channel)
            }
        }
    }
}
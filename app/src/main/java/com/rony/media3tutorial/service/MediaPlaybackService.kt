package com.rony.media3tutorial.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rony.media3tutorial.MainActivity
import com.rony.media3tutorial.R
import com.rony.media3tutorial.VideoPlayerApp
import com.rony.media3tutorial.notification.NotificationPlayerCustomCommandButton
import com.rony.media3tutorial.notification.PlaybackNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {

    companion object {
        private const val ACTION_FAVORITE = "custom_action_fav"
        private const val REWIND_5 = "rewind_5"
        private const val NEXT_5 = "next_5"
        const val ACTION_PLAY = "action_play"
        const val EXTRA_MEDIA_URIS = "EXTRA_MEDIA_URIS"

        fun ensurePlaybackService(
            context: Context,
            uris: List<Uri>
        ) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putParcelableArrayListExtra(
                    EXTRA_MEDIA_URIS,
                    ArrayList(uris)
                )
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }


    @Inject lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null
    private var notificationManager: PlayerNotificationManager? = null
    private val useLegacyNotification =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU

    private val mediaSessionCallback = object : MediaSession.Callback {

        @Deprecated("Deprecated in Java")
        override fun onPlayerCommandRequest( //without it, when user swipes notification when media is paused, the media session is still alive hence it sends back the notification.
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            if (playerCommand == Player.COMMAND_STOP) {
                session.player.stop()
                session.player.clearMediaItems()
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
//            val result = super.onConnect(session, controller)
//            if (useLegacyNotification) {
//                return result
//            }
//            Log.d("stamstam","setting custom layout!! within onConnect")
//            val sessionCommandsBuilder =
//                result.availableSessionCommands.buildUpon()
//
//            val playerCommandsBuilder =
//                result.availablePlayerCommands.buildUpon()
//
//            if (session.isMediaNotificationController(controller)) {
//                playerCommandsBuilder
//                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
//                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
//                    .remove(Player.COMMAND_SEEK_TO_NEXT)
//                    .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
//                notificationPlayerCustomCommandButtons.forEach { commandButton ->
//                    commandButton.sessionCommand?.let(sessionCommandsBuilder::add)
//                }
//            } else if (session.isAutomotiveController(controller)) {
//                playerCommandsBuilder
//                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
//                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
//                    .remove(Player.COMMAND_SEEK_TO_NEXT)
//                    .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
//                notificationPlayerCustomCommandButtons.forEachIndexed { index, commandButton ->
//                    if(index != 0) {
//                        commandButton.sessionCommand?.let(sessionCommandsBuilder::add)
//                    }
//                }
//            }
//
//            return MediaSession.ConnectionResult.accept(
//                sessionCommandsBuilder.build(),
//                playerCommandsBuilder.build()
//            )

            val base = super.onConnect(session, controller)

            if (useLegacyNotification) { //for older devices, the notification doesnt work with custom layout and custom commands hence we provide layout to the notification manager.
                return base
            }
            val isAuto =
                session.isAutomotiveController(controller) ||
                        session.isAutoCompanionController(controller)

            if (isAuto) {
                Log.d("AUTO_DEBUG", "Auto/DHU controller connected")

                val sessionCommands =
                    base.availableSessionCommands.buildUpon().apply {
                        notificationPlayerCustomCommandButtons.forEach {
                            it.sessionCommand?.let(::add)
                        }
                    }.build()

                return MediaSession.ConnectionResult.accept(
                    sessionCommands,
                    base.availablePlayerCommands
                )
            }

            if (session.isMediaNotificationController(controller)) {
                val sessionCommands =
                    base.availableSessionCommands.buildUpon().apply {
                        notificationPlayerCustomCommandButtons.forEach {
                            it.sessionCommand?.let(::add)
                        }
                    }.build()

                val playerCommands =
                    base.availablePlayerCommands.buildUpon()
                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .remove(Player.COMMAND_SEEK_TO_NEXT)
                        .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .build()

                return MediaSession.ConnectionResult.accept(
                    sessionCommands,
                    playerCommands
                )
            }

            return base
        }

        override fun onPostConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            super.onPostConnect(session, controller)
            if (useLegacyNotification) return
            Log.d("stamstam","setting custom layout!! within onPostConnect")
            if (notificationPlayerCustomCommandButtons.isNotEmpty()) {
                session.setCustomLayout(notificationPlayerCustomCommandButtons)
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == NotificationPlayerCustomCommandButton.REWIND.customAction) {
                        session.player.seekBack()
                    }
                    else if (customCommand.customAction == NotificationPlayerCustomCommandButton.FORWARD.customAction) {
                        session.player.seekForward()
                    }

//            if (customCommand.customAction == NotificationPlayerCustomCommandButton.FAVORITE.customAction) {
//                //add to favorites!!
//                Log.d("stamstam","Clicked on add to favourite!")
//            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private val notificationPlayerCustomCommandButtons =
        NotificationPlayerCustomCommandButton.entries.map { command -> command.commandButton }

    override fun onCreate() {
        super.onCreate()
        Log.d("stamstam","create service called!")
        setExoListener()
        initMediaSession()
        initNotificationManager()
    }

    private fun setExoListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("stamstam", "Player error", error)
            }
        })
    }


    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.d("stamstam","onStartCommand called!!!")
        when (intent?.action) {
            ACTION_PLAY -> {
                Log.d("stamstam", "ACTION_PLAY received")
                val uris =
                    intent.getParcelableArrayListExtra<Uri>(EXTRA_MEDIA_URIS)
                if (exoPlayer.mediaItemCount == 0 && !uris.isNullOrEmpty()) {
                    val items = uris.map { MediaItem.fromUri(it) }
                    exoPlayer.setMediaItems(items)
                    exoPlayer.prepare()
                }
                exoPlayer.play()
            }
        }
        ensureNotificationReady()
        return START_STICKY
    }

    private fun ensureNotificationReady() {
        notificationManager?.setPlayer(exoPlayer)
    }


    private fun initMediaSession() {
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(mediaSessionCallback)
            .build()
    }

    private val descriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {

            override fun getCurrentContentTitle(player: Player): CharSequence =
                player.mediaMetadata.title ?: "Playing"

            override fun getCurrentContentText(player: Player): CharSequence? =
                player.mediaMetadata.artist

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? = null // letting MediaMetadata.artworkUri handle it

            override fun createCurrentContentIntent(player: Player): PendingIntent =
                PendingIntent.getActivity(
                    this@MediaPlaybackService,
                    0,
                    Intent(this@MediaPlaybackService, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        }

    private val notificationListener =
        object : PlayerNotificationManager.NotificationListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {

                if (ongoing) {
                    Log.d("stamstam", "called onNotificationPosted and start foreground service")
                    startForeground(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                Log.d("stamstam", "called onNotificationCancelled with byUser: $dismissedByUser")
                // user explicitly dismissed playback!!!!
                if (dismissedByUser) {
                    notificationManager?.setPlayer(null)
                    exoPlayer.pause()
                    stopSelf()
                }
            }
        }

    //allows us to actually kill the app and if the playback is ongoing it will resume!
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("stamstam", "app swiped away, exoPlayer.isPlaying =  ${exoPlayer.isPlaying}")
        if(!exoPlayer.isPlaying) {
            pauseAllPlayersAndStopSelf()
        }
    }

    private fun initNotificationManager() {
        PlaybackNotification.ensureChannel(this)
        mediaSession?.let {
            notificationManager =
                PlayerNotificationManager.Builder(
                    this,
                    PlaybackNotification.NOTIFICATION_ID,
                    PlaybackNotification.CHANNEL_ID
                )
                    .setMediaDescriptionAdapter(descriptionAdapter)
                    .setNotificationListener(notificationListener)
                    .setCustomActionReceiver(customActionReceiver)
                    .build()
                    .apply {
                        setMediaSessionToken(mediaSession!!.platformToken)
                        setUsePreviousAction(false)
                        setUseNextAction(false)
                        setUsePlayPauseActions(true)
                        setUseFastForwardAction(false)
                        setUseRewindAction(false)
                        setPlayer(exoPlayer)
                    }

        }
    }

    private val customActionReceiver =
        object : PlayerNotificationManager.CustomActionReceiver {

            override fun createCustomActions(
                context: Context,
                instanceId: Int
            ): Map<String, NotificationCompat.Action> {
                val rewindIntent = Intent(REWIND_5)
                val rewindPendingIntent = PendingIntent.getBroadcast(
                    context,
                    instanceId + 1,
                    rewindIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val forwardIntent = Intent(NEXT_5)
                val forwardPendingIntent = PendingIntent.getBroadcast(
                    context,
                    instanceId + 2,
                    forwardIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val rewindAction =
                    NotificationCompat.Action(
                        R.drawable.ic_rewind_5,
                        "Rewind 5s",
                        rewindPendingIntent
                    )

                val forwardAction =
                    NotificationCompat.Action(
                        R.drawable.ic_forward_5,
                        "Forward 5s",
                        forwardPendingIntent
                    )

                return mapOf(
                    REWIND_5 to rewindAction,
                    NEXT_5 to forwardAction
                )
            }

            override fun getCustomActions(player: Player): List<String> {
                //return listOf(ACTION_FAVORITE)
                return listOf(
                    REWIND_5,
                    NEXT_5
                )
            }

            override fun onCustomAction(player: Player, action: String, intent: Intent) {
                when (action) {
                    REWIND_5 -> {
                        player.seekBack()
                    }
                    NEXT_5 -> {
                        player.seekForward()
                    }
                }
            }
        }

    //for old android devices when clicking on any of the custom buttons the notification will blink because media3 notification and legacy one will fight.
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        // If you are using PlayerNotificationManager, leave this EMPTY.
        // This prevents Media3's default notification logic from running
        // and clashing with your legacy manager.
    }
}
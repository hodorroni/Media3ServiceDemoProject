package com.rony.media3tutorial.notification

import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import com.rony.media3tutorial.R

const val CUSTOM_COMMAND_REWIND_ACTION_ID = "REWIND_15"
const val CUSTOM_COMMAND_FORWARD_ACTION_ID = "FAST_FWD_15"
const val CUSTOM_COMMAND_FAVORITE_ACTION_ID = "FAVORITE"

enum class NotificationPlayerCustomCommandButton(
    val customAction: String,
    val commandButton: CommandButton,
) {
    FORWARD(
        customAction = CUSTOM_COMMAND_FORWARD_ACTION_ID,
        commandButton = CommandButton.Builder()
            .setDisplayName("Forward")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_FORWARD_ACTION_ID, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_forward_5)
            .build(),
    ),
    REWIND(
        customAction = CUSTOM_COMMAND_REWIND_ACTION_ID,
        commandButton = CommandButton.Builder()
            .setDisplayName("Rewind")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_REWIND_ACTION_ID, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_rewind_5)
            .build(),
    );
//    FAVORITE(
//        customAction = CUSTOM_COMMAND_FAVORITE_ACTION_ID,
//        commandButton = CommandButton.Builder()
//            .setDisplayName("Favorite")
//            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_FAVORITE_ACTION_ID, Bundle.EMPTY))
//            .setIconResId(R.drawable.ic_favorite)
//            .build(),
}
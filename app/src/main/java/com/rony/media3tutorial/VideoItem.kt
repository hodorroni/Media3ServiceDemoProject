package com.rony.media3tutorial

import android.net.Uri
import androidx.media3.common.MediaItem

data class VideoItem(
    val contentUri: Uri,
    val mediaItem: MediaItem,
    val name: String
)

fun mediaItemFromUri(uri: Uri): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(uri.toString())
        .build()
}

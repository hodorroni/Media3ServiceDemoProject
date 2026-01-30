package com.rony.media3tutorial

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VideoPlayerApp: Application(), LifecycleEventObserver {

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        lifecycleState = event.targetState
    }

    companion object {
        var lifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED
    }
}
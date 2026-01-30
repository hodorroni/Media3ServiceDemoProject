package com.rony.media3tutorial.providers

import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControllerProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionToken: SessionToken
) {
    val controllerFuture =
        MediaController.Builder(context, sessionToken).buildAsync()

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()

    init {
        controllerFuture.addListener(
            {
                _controller.value = controllerFuture.get()
            },
            MoreExecutors.directExecutor()
        )
    }

    fun release() {
        _controller.value?.release()
        _controller.value = null
    }
}
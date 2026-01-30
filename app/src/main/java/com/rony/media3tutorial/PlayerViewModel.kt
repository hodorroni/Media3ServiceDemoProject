package com.rony.media3tutorial

import androidx.media3.session.MediaController
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.rony.media3tutorial.managers.MediaControllerManager
import com.rony.media3tutorial.mediaItemFromUri
import com.rony.media3tutorial.providers.MediaControllerProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: MediaControllerManager,
    controllerProvider: MediaControllerProvider,
    private val metaDataReader: MetaDataReader,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val controller: StateFlow<MediaController?> = controllerProvider.controller

    private val _action = MutableSharedFlow<Boolean>() //only for starting the service.
    val action = _action.asSharedFlow()

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            controllerProvider.controller.value?.let(::updatePlaybackState)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            controllerProvider.controller.value?.let(::updatePlaybackState)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            controllerProvider.controller.value?.let(::updatePlaybackState)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            controllerProvider.controller.value?.let(::updatePlaybackState)
        }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private val videoUris = savedStateHandle.getStateFlow("videoUris", emptyList<Uri>())

    //Since MediaItems arent parcelable we can't save then insdie savedStateHandle upon process death so we inspect changes within videoUris and will derive from it since it actually can be saved.
     val videoItems = videoUris.map {
         uris -> uris.map { uri ->
             VideoItem(
                 contentUri = uri,
                 mediaItem = mediaItemFromUri(uri),
                 name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No name" ) } }
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    init {
        observeController()
    }

    private fun observeController() {
        viewModelScope.launch {
            controller
                .filterNotNull()
                .distinctUntilChanged()
                .collect { c ->
                    c.addListener(playerListener)
                    updatePlaybackState(c)
                }
        }
    }

    private fun updatePlaybackState(controller: MediaController) {
        _uiState.update { current ->
            current.copy(
                isPlaying = controller.isPlaying,
                canGoPrevious = controller.hasPreviousMediaItem(),
                canGoNext = controller.hasNextMediaItem()
            )
        }
    }

    fun addVideoUri(uri: Uri) {
        val updated = videoUris.value + uri
        savedStateHandle["videoUris"] = updated
        playbackManager.setMediaItems(updated.map(::mediaItemFromUri))
    }

    fun getUris(): List<Uri> {
        return videoUris.value
    }

    fun previousItem() = playbackManager.previousItem()
    fun nextItem() = playbackManager.nextItem()
    fun pauseOrResume() {
        playbackManager.togglePlayPause()
    }
    fun play(videoItem: VideoItem) {
        //need to restart the service
        if(playbackManager.getItemCount() == 0) {
            Log.d("stamstam","item count is 0!")
            viewModelScope.launch {
                _action.emit(true)
            }
        } else {
            Log.d("stamstam","item count isnt 0!")
            playbackManager.playItem(videoItem.mediaItem.mediaId)
        }
    }
    fun resume() = playbackManager.resume()
    fun pause() = playbackManager.pause()
    fun seekBack() = playbackManager.seekBack(5_000)
    fun seekForward() = playbackManager.seekForward(5_000)
}
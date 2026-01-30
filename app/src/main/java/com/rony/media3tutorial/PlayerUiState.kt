package com.rony.media3tutorial

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    val videoItems: List<VideoItem> = emptyList()
)

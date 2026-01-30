package com.rony.media3tutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.rony.media3tutorial.ui.theme.Media3TutorialTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.ui.PlayerView
import com.rony.media3tutorial.service.MediaPlaybackService
import com.rony.media3tutorial.ui.player_no_media_controller.buttons.PlayMediaButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(UnstableApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Media3TutorialTheme {
                val viewModel = hiltViewModel<PlayerViewModel>()
                val videoItems by viewModel.videoItems.collectAsStateWithLifecycle()
                val controller by viewModel.controller.collectAsStateWithLifecycle()
                val lifeCycleOwner = LocalLifecycleOwner.current
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                LaunchedEffect(lifeCycleOwner) {
                    lifeCycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        withContext(Dispatchers.Main.immediate) {
                            viewModel.action.collect { event ->
                                if (event) {
                                    MediaPlaybackService.ensurePlaybackService(
                                        context = context,
                                        uris = viewModel.getUris()
                                    )
                                }
                            }
                        }
                    }
                }

                val selectVideoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri ?: return@rememberLauncherForActivityResult
                    val resolver = context.contentResolver
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                    val hasPersistableGrant =
                        resolver.persistedUriPermissions.any { it.uri == uri }

                    try {
                        if (!hasPersistableGrant) {
                            resolver.takePersistableUriPermission(uri, flags)
                        }
                    } catch (e: SecurityException) {
                        Log.d("stamstam", "some media file that isn't supported to persist: $e")
                    }

                    viewModel.addVideoUri(uri)
                }




                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(16f / 9f)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    PlayerView(context).apply {
                                        player = controller
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                update = {
                                    it.player = controller
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 22.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                6.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {

                            PlayMediaButton(
                                isVisible = state.canGoPrevious,
                                content = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_back_media),
                                        contentDescription = "Seek forward",
                                        tint = Color.Black
                                    )
                                }
                            ) {
                                viewModel.previousItem()
                            }

                            PlayMediaButton(
                                isVisible = true,
                                content = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_rewind_5),
                                        contentDescription = "Seek back",
                                        tint = Color.Black
                                    )
                                }
                            ) {
                                viewModel.seekBack()
                            }

                            PlayMediaButton(
                                isVisible = true,
                                content = {
                                    Icon(
                                        painter = if (state.isPlaying) {
                                            painterResource(R.drawable.ic_pause)
                                        } else painterResource(R.drawable.ic_play),
                                        contentDescription = "Play Pause",
                                        tint = Color.Black
                                    )
                                }
                            ) {
                                //startPlaybackService(context)
                                MediaPlaybackService.ensurePlaybackService(
                                    context = context,
                                    uris = viewModel.getUris()
                                )
                                viewModel.pauseOrResume()
                            }

                            PlayMediaButton(
                                isVisible = true,
                                content = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_forward_5),
                                        contentDescription = "Seek back",
                                        tint = Color.Black
                                    )
                                }
                            ) {
                                viewModel.seekForward()
                            }

                            PlayMediaButton(
                                isVisible = state.canGoNext,
                                content = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_next_media),
                                        contentDescription = "Next item",
                                        tint = Color.Black
                                    )
                                }
                            ) {
                                viewModel.nextItem()
                            }
                        }


                        IconButton(
                            onClick = {
                                selectVideoLauncher.launch("video/*")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = "Select video"
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(videoItems) { item ->
                                Text(
                                    text = item.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.play(item)
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    fun startPlaybackService(context: Context) {
        val intent = Intent(context, MediaPlaybackService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}


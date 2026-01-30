package com.rony.media3tutorial.ui.player_no_media_controller.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlayMediaButton(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    buttonAction: () -> Unit,
) {
    val baseModifier = modifier.size(50.dp)

    if (isVisible) {
        Box(
            modifier = baseModifier
                .clip(RoundedCornerShape(100))
                .background(Color.Gray.copy(alpha = 0.1f))
                .clickable { buttonAction() },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    } else {
        Spacer(modifier = baseModifier)
    }
}
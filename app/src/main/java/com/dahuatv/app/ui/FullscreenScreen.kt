package com.dahuatv.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dahuatv.app.data.CameraInfo
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.Stream
import com.dahuatv.app.ui.player.CameraVideo
import kotlinx.coroutines.delay

@Composable
fun FullscreenScreen(camera: Camera, info: CameraInfo?) {
    // The grid's players are already released (it left composition), so 4K gets the decoder to itself.
    var showLabel by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3_000)
        showLabel = false
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraVideo(
            camera = camera,
            stream = Stream.MAIN,
            configuredFps = info?.mainFps,
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = showLabel,
            exit = fadeOut(),
            // Top-right: the FPS badge owns the top-left corner.
            modifier = Modifier.align(Alignment.TopEnd).padding(32.dp),
        ) {
            Text(
                text = "${info?.title ?: camera.name} · Back for grid",
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}

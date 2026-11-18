package com.dahuatv.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dahuatv.app.R
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

    // Audio starts muted every time fullscreen opens; OK on the remote toggles it.
    var muted by remember { mutableStateOf(true) }
    var hasAudio by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraVideo(
            camera = camera,
            stream = Stream.MAIN,
            configuredFps = info?.mainFps,
            muted = muted,
            onHasAudio = { hasAudio = it },
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
        if (hasAudio) {
            MuteButton(
                muted = muted,
                onToggle = { muted = !muted },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            )
        }
    }
}

/** The only control on the fullscreen screen, so it always has focus: OK toggles sound. */
@Composable
private fun MuteButton(muted: Boolean, onToggle: () -> Unit, modifier: Modifier) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(40.dp).focusRequester(focus),
        scale = IconButtonDefaults.scale(focusedScale = 1f),
        colors = IconButtonDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.55f),
            contentColor = Color.White,
            focusedContainerColor = Color.Black.copy(alpha = 0.55f),
            focusedContentColor = Color.White,
        ),
    ) {
        Icon(
            painter = painterResource(if (muted) R.drawable.ic_volume_off else R.drawable.ic_volume_up),
            contentDescription = if (muted) "Sound off, press OK to turn on" else "Sound on, press OK to mute",
            modifier = Modifier.size(22.dp),
        )
    }
}

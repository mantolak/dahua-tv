package com.dahuatv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.Stream

@Composable
fun FullscreenScreen(camera: Camera) {
    // TODO(player step): grid players are released before this screen starts the 4K main stream.
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(camera.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                text = if (camera.isConfigured) camera.rtspUrl(Stream.MAIN) else "Not set up",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Text("Back to return to grid", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

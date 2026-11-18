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

@Composable
fun SettingsScreen(camera: Camera) {
    // TODO(settings step): name / IP / port / user / password fields, saved encrypted on the device.
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Settings · ${camera.name}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Coming next: IP, port, user, password", color = Color.White.copy(alpha = 0.6f))
            Text("Back to return to grid", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

package com.dahuatv.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.Stream
import com.dahuatv.app.ui.player.CameraVideo

private val GAP = 6.dp

@Composable
fun GridScreen(
    cameras: List<Camera>,
    initialFocus: Int,
    onOpen: (Int) -> Unit,
    onEdit: (Int) -> Unit,
) {
    val focus = remember { List(Camera.GRID_SIZE) { FocusRequester() } }
    LaunchedEffect(Unit) { focus[initialFocus].requestFocus() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(GAP),
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        for (row in 0 until 2) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GAP),
            ) {
                for (col in 0 until 2) {
                    val i = row * 2 + col
                    CameraTile(
                        camera = cameras[i],
                        modifier = Modifier.weight(1f).fillMaxHeight().focusRequester(focus[i]),
                        onClick = { onOpen(i) },
                        onLongClick = { onEdit(i) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraTile(
    camera: Camera,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        // Camera wall: no zoom on focus, just a clear white frame around the selected tile.
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Color.White), shape = shape),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        if (camera.isConfigured) {
            CameraVideo(camera = camera, stream = Stream.SUB, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(camera.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(
                        text = "Not set up · press OK to configure",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
            }
        }
        Text(
            text = camera.name,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.9f),
        )
    }
}

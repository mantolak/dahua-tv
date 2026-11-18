package com.dahuatv.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dahuatv.app.data.CameraRepository

/** The three screens of the app. Kept as plain state: the app is too small to need a navigation library. */
sealed interface Screen {
    data object Grid : Screen
    data class Fullscreen(val index: Int) : Screen
    data class Settings(val index: Int) : Screen
}

@Composable
fun DahuaTvApp(repository: CameraRepository) {
    val cameras by repository.cameras.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf<Screen>(Screen.Grid) }
    // Remember the last tile so focus goes back to it when returning to the grid.
    var lastIndex by remember { mutableIntStateOf(0) }
    val backToGrid = { screen = Screen.Grid }

    when (val s = screen) {
        Screen.Grid -> GridScreen(
            cameras = cameras,
            initialFocus = lastIndex,
            onOpen = { i ->
                lastIndex = i
                // An empty slot has nothing to show yet, so OK goes straight to its settings.
                screen = if (cameras[i].isConfigured) Screen.Fullscreen(i) else Screen.Settings(i)
            },
            onEdit = { lastIndex = it; screen = Screen.Settings(it) },
        )

        is Screen.Fullscreen -> {
            BackHandler(onBack = backToGrid)
            FullscreenScreen(camera = cameras[s.index])
        }

        is Screen.Settings -> {
            BackHandler(onBack = backToGrid)
            SettingsScreen(
                index = s.index,
                camera = cameras[s.index],
                onSave = { repository.save(s.index, it); backToGrid() },
                onRemove = { repository.remove(s.index); backToGrid() },
                onCancel = backToGrid,
            )
        }
    }
}

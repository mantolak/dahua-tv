package com.dahuatv.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dahuatv.app.model.Camera

/** The three screens of the app. Kept as plain state: the app is too small to need a navigation library. */
sealed interface Screen {
    data object Grid : Screen
    data class Fullscreen(val index: Int) : Screen
    data class Settings(val index: Int) : Screen
}

@Composable
fun DahuaTvApp() {
    // TODO(storage step): load from encrypted on-device storage instead of placeholders.
    val cameras = remember { Camera.placeholders() }
    var screen by remember { mutableStateOf<Screen>(Screen.Grid) }
    // Remember the last tile so focus goes back to it when returning to the grid.
    var lastIndex by remember { mutableIntStateOf(0) }

    when (val s = screen) {
        Screen.Grid -> GridScreen(
            cameras = cameras,
            initialFocus = lastIndex,
            onOpen = { lastIndex = it; screen = Screen.Fullscreen(it) },
            onEdit = { lastIndex = it; screen = Screen.Settings(it) },
        )

        is Screen.Fullscreen -> {
            BackHandler { screen = Screen.Grid }
            FullscreenScreen(camera = cameras[s.index])
        }

        is Screen.Settings -> {
            BackHandler { screen = Screen.Grid }
            SettingsScreen(camera = cameras[s.index])
        }
    }
}

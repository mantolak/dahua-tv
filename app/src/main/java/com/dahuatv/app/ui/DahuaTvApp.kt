package com.dahuatv.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.dahuatv.app.data.CameraInfo
import com.dahuatv.app.data.CameraRepository
import com.dahuatv.app.data.DahuaApi

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

    // Ask each camera for its own name and configured FPS; re-asks when a camera's settings change.
    val api = remember { DahuaApi() }
    // One entry per tile, written independently: the 4 checks run concurrently, so a
    // read-modify-write of a whole map would let the last one to finish erase the others.
    val infos = remember { mutableStateMapOf<Int, CameraInfo>() }
    // Bumped by OK on a tile whose login was refused: one new check, nothing automatic.
    val recheck = remember { mutableStateMapOf<Int, Int>() }
    cameras.forEachIndexed { i, camera ->
        LaunchedEffect(camera, recheck[i]) {
            infos.remove(i)
            if (camera.isConfigured) infos[i] = api.fetchInfo(camera)
        }
    }

    when (val s = screen) {
        Screen.Grid -> GridScreen(
            cameras = cameras,
            infos = infos,
            initialFocus = lastIndex,
            onOpen = { i ->
                lastIndex = i
                when {
                    // An empty slot has nothing to show yet, so OK goes straight to its settings.
                    !cameras[i].isConfigured -> screen = Screen.Settings(i)
                    infos[i]?.login != null -> recheck[i] = (recheck[i] ?: 0) + 1
                    else -> screen = Screen.Fullscreen(i)
                }
            },
            onEdit = { lastIndex = it; screen = Screen.Settings(it) },
        )

        is Screen.Fullscreen -> {
            BackHandler(onBack = backToGrid)
            FullscreenScreen(camera = cameras[s.index], info = infos[s.index])
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

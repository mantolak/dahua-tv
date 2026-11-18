package com.dahuatv.app.data

import android.content.Context
import androidx.core.content.edit
import com.dahuatv.app.model.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The 4 grid slots, persisted on the TV. Name/IP/port/channel are stored as-is;
 * username and password are encrypted with [SecretCipher] before they touch disk.
 */
class CameraRepository(
    context: Context,
    private val cipher: SecretCipher = KeystoreCipher(),
) {
    private val prefs = context.getSharedPreferences("cameras", Context.MODE_PRIVATE)

    private val _cameras = MutableStateFlow(List(Camera.GRID_SIZE, ::load))
    val cameras: StateFlow<List<Camera>> = _cameras.asStateFlow()

    fun save(index: Int, camera: Camera) {
        prefs.edit {
            putString(key(index, NAME), camera.name)
            putString(key(index, HOST), camera.host)
            putInt(key(index, PORT), camera.port)
            putInt(key(index, CHANNEL), camera.channel)
            putString(key(index, USER), cipher.encrypt(camera.username))
            putString(key(index, PASS), cipher.encrypt(camera.password))
        }
        set(index, camera)
    }

    fun remove(index: Int) {
        prefs.edit { ALL_FIELDS.forEach { remove(key(index, it)) } }
        set(index, Camera.empty(index))
    }

    private fun set(index: Int, camera: Camera) =
        _cameras.update { list -> list.toMutableList().also { it[index] = camera } }

    private fun load(index: Int): Camera {
        val host = prefs.getString(key(index, HOST), null) ?: return Camera.empty(index)
        return Camera(
            name = prefs.getString(key(index, NAME), null) ?: Camera.defaultName(index),
            host = host,
            port = prefs.getInt(key(index, PORT), Camera.DEFAULT_PORT),
            username = secret(index, USER),
            password = secret(index, PASS),
            channel = prefs.getInt(key(index, CHANNEL), 1),
        )
    }

    private fun secret(index: Int, field: String): String =
        prefs.getString(key(index, field), null)?.let(cipher::decrypt).orEmpty()

    private fun key(index: Int, field: String) = "camera$index.$field"

    private companion object {
        const val NAME = "name"
        const val HOST = "host"
        const val PORT = "port"
        const val CHANNEL = "channel"
        const val USER = "user"
        const val PASS = "pass"
        val ALL_FIELDS = listOf(NAME, HOST, PORT, CHANNEL, USER, PASS)
    }
}

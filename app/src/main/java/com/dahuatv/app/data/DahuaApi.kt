package com.dahuatv.app.data

import android.util.Log
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.LoginProblem
import com.dahuatv.app.model.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import kotlin.math.roundToInt

/** What the camera itself reports: its channel title (OSD name) and the configured FPS of both streams. */
data class CameraInfo(
    val title: String?,
    val mainFps: Int?,
    val subFps: Int?,
) {
    fun fps(stream: Stream): Int? = if (stream == Stream.MAIN) mainFps else subFps
}

/** Minimal client for the Dahua HTTP API (`/cgi-bin/configManager.cgi`), using the camera's own login. */
class DahuaApi(private val httpPort: Int = 80) {

    suspend fun fetchInfo(camera: Camera): CameraInfo = withContext(Dispatchers.IO) {
        val ch = camera.channel - 1
        val titleCfg = getConfig(camera, "ChannelTitle")
        // A refused login must not be repeated (camera lockout), so skip the second request.
        val encode = if (titleCfg == null) emptyMap() else getConfig(camera, "Encode").orEmpty()
        val title = titleCfg?.get("table.ChannelTitle[$ch].Name")
        CameraInfo(
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            mainFps = encode["table.Encode[$ch].MainFormat[0].Video.FPS"]?.toFloatOrNull()?.roundToInt(),
            subFps = encode["table.Encode[$ch].ExtraFormat[0].Video.FPS"]?.toFloatOrNull()?.roundToInt(),
        ).also { Log.i(TAG, "${camera.host}: $it") }
    }

    /** null = the camera refused our login; empty = other failure. */
    private fun getConfig(camera: Camera, name: String): Map<String, String>? =
        try {
            parseConfig(get(camera, "/cgi-bin/configManager.cgi?action=getConfig&name=$name"))
        } catch (e: LoginRefused) {
            Log.w(TAG, "${camera.host}: getConfig $name refused: HTTP ${e.status}")
            null
        } catch (e: Exception) {
            Log.w(TAG, "${camera.host}: getConfig $name failed: ${e.message}")
            emptyMap()
        }

    private class LoginRefused(val status: Int) : IOException("HTTP $status")

    private fun get(camera: Camera, pathAndQuery: String): String {
        val url = URL("http", camera.host, httpPort, pathAndQuery)
        var conn = open(url)
        if (conn.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            val challenges = conn.headerFields
                .filterKeys { it.equals("WWW-Authenticate", ignoreCase = true) }
                .values.flatten()
            conn.errorStream?.close()
            conn.disconnect()
            val digest = challenges.firstOrNull { it.startsWith("Digest", ignoreCase = true) }
            val auth = if (digest != null) {
                HttpAuth.digest(
                    camera.username, camera.password, "GET", pathAndQuery,
                    HttpAuth.parseChallenge(digest), cnonce = randomHex(),
                )
            } else {
                HttpAuth.basic(camera.username, camera.password)
            }
            conn = open(url).apply { setRequestProperty("Authorization", auth) }
        }
        try {
            val status = conn.responseCode
            if (LoginProblem.fromStatus(status) != null) throw LoginRefused(status)
            if (status != HttpURLConnection.HTTP_OK) throw IOException("HTTP $status")
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun open(url: URL) = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 4_000
        readTimeout = 4_000
        instanceFollowRedirects = false
    }

    private fun randomHex() = ByteArray(8).also(SecureRandom()::nextBytes).joinToString("") { "%02x".format(it) }

    companion object {
        private const val TAG = "DahuaApi"

        /** Dahua answers getConfig with lines like `table.ChannelTitle[0].Name=Front door`. */
        fun parseConfig(body: String): Map<String, String> =
            body.lineSequence()
                .map { it.trim() }
                .filter { '=' in it }
                .associate { it.substringBefore('=') to it.substringAfter('=') }
    }
}

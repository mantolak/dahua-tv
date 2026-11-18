package com.dahuatv.app.ui.player

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dahuatv.app.model.Camera
import com.dahuatv.app.model.Stream
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

private const val TAG = "CameraVideo"

sealed interface PlayerStatus {
    data object Connecting : PlayerStatus
    data object Playing : PlayerStatus
    data class Retrying(val reason: String, val inSeconds: Long) : PlayerStatus
}

/**
 * Live video of one camera. The player exists only while the screen is started and this
 * composable is on screen: leaving the grid releases all 4 decoders before fullscreen starts 4K.
 */
@Composable
fun CameraVideo(
    camera: Camera,
    stream: Stream,
    modifier: Modifier = Modifier,
    /** FPS configured in the camera for this stream, shown next to the measured value. */
    configuredFps: Int? = null,
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var status by remember { mutableStateOf<PlayerStatus>(PlayerStatus.Connecting) }
    var videoSize by remember { mutableStateOf<VideoSize?>(null) }
    var fps by remember { mutableStateOf<Float?>(null) }
    val renderedFrames = remember { AtomicInteger() }

    LifecycleStartEffect(camera, stream) {
        status = PlayerStatus.Connecting
        val p = createPlayer(context, camera, stream)
        val reconnector = Reconnector(p, label = "${camera.name}/${stream.name}") { status = it }
        val sizeListener = object : Player.Listener {
            override fun onVideoSizeChanged(size: VideoSize) {
                if (size.width > 0) videoSize = size
            }
        }
        p.addListener(reconnector)
        p.addListener(sizeListener)
        // Called on the playback thread for every frame actually sent to the screen.
        p.setVideoFrameMetadataListener { _, _, _, _ -> renderedFrames.incrementAndGet() }
        p.prepare()
        player = p
        onStopOrDispose {
            reconnector.cancel()
            p.removeListener(sizeListener)
            player = null
            fps = null
            p.release()
        }
    }

    // Measured FPS: frames rendered per second, sampled every second.
    LaunchedEffect(player) {
        if (player == null) return@LaunchedEffect
        renderedFrames.set(0)
        var last = System.nanoTime()
        while (true) {
            delay(1_000)
            val now = System.nanoTime()
            fps = renderedFrames.getAndSet(0) * 1e9f / (now - last)
            last = now
        }
    }

    Box(modifier.background(Color.Black)) {
        AndroidView(
            factory = ::createPlayerView,
            update = { it.player = player },
            onRelease = { it.player = null },
            modifier = Modifier.fillMaxSize(),
        )
        StatusBadge(status, Modifier.align(Alignment.Center))
        StatsBadge(
            stream = stream,
            size = videoSize,
            fps = fps.takeIf { status == PlayerStatus.Playing },
            configuredFps = configuredFps,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        )
    }
}

/** e.g. "Sub 704×480 · 19.8/20 fps" — measured / configured in camera. */
@Composable
private fun StatsBadge(stream: Stream, size: VideoSize?, fps: Float?, configuredFps: Int?, modifier: Modifier) {
    val text = buildString {
        append(stream.label)
        size?.let { append(" ${it.width}×${it.height}") }
        append(" · ")
        append(fps?.let { "%.1f".format(it) } ?: "–")
        configuredFps?.let { append("/$it") }
        append(" fps")
    }
    Text(
        text = text,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.9f),
    )
}

@Composable
private fun StatusBadge(status: PlayerStatus, modifier: Modifier) {
    val text = when (status) {
        PlayerStatus.Playing -> return
        PlayerStatus.Connecting -> "Connecting…"
        is PlayerStatus.Retrying -> "${status.reason}\nRetrying in ${status.inSeconds}s"
    }
    Text(
        text = text,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
    )
}

@OptIn(UnstableApi::class)
private fun createPlayerView(context: Context) = PlayerView(context).apply {
    useController = false
    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
    setShutterBackgroundColor(android.graphics.Color.BLACK)
    // Keep the last frame on screen while reconnecting instead of flashing black.
    setKeepContentOnPlayerReset(true)
    // The tile around the video owns D-pad focus, never the video view.
    isFocusable = false
    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
}

@OptIn(UnstableApi::class)
private fun createPlayer(context: Context, camera: Camera, stream: Stream): ExoPlayer {
    val renderers = DefaultRenderersFactory(context)
        // If the hardware HEVC decoder refuses (e.g. too many instances), try the next decoder.
        .setEnableDecoderFallback(true)
    // Live camera: small buffer = low delay. Values in ms.
    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(1_000, 3_000, 500, 500)
        .build()
    val player = ExoPlayer.Builder(context, renderers)
        .setLoadControl(loadControl)
        .build()

    // Camera audio is not used: don't even decode it.
    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
        .build()
    player.volume = 0f

    val source = RtspMediaSource.Factory()
        // TCP: no lost packets/grey smearing on Wi-Fi, and works through any NAT (incl. the emulator).
        .setForceUseRtpTcp(true)
        .setTimeoutMs(8_000)
        .createMediaSource(MediaItem.fromUri(camera.playerUrl(stream)))
    player.setMediaSource(source)
    player.playWhenReady = true
    // Debug builds: log every player event (tag EventLogger) for diagnosing streams.
    if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
        player.addAnalyticsListener(EventLogger("${camera.name}/${stream.name}"))
    }
    return player
}

/**
 * Keeps a live stream alive: on error, end of stream, or a stall it re-prepares the player
 * with exponential backoff (1, 2, 4, 8, 16, 30, 30 … seconds). Success resets the backoff.
 */
private class Reconnector(
    private val player: ExoPlayer,
    private val label: String,
    private val onStatus: (PlayerStatus) -> Unit,
) : Player.Listener {
    private val handler = Handler(Looper.getMainLooper())
    private var attempt = 0

    private val retry = Runnable {
        onStatus(PlayerStatus.Connecting)
        player.prepare()
    }

    /** RTSP over TCP can hang silently if the camera vanishes: treat long buffering as a failure. */
    private val stallCheck = Runnable {
        Log.w(TAG, "$label: stalled, reconnecting")
        player.stop()
        scheduleRetry("No video")
    }

    override fun onPlaybackStateChanged(state: Int) {
        handler.removeCallbacks(stallCheck)
        when (state) {
            Player.STATE_READY -> {
                attempt = 0
                onStatus(PlayerStatus.Playing)
            }
            Player.STATE_BUFFERING -> handler.postDelayed(stallCheck, STALL_TIMEOUT_MS)
            Player.STATE_ENDED -> scheduleRetry("Stream ended")
            Player.STATE_IDLE -> Unit
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.w(TAG, "$label: ${error.errorCodeName}: ${error.cause?.message}")
        scheduleRetry(describe(error))
    }

    private fun scheduleRetry(reason: String) {
        handler.removeCallbacks(retry)
        handler.removeCallbacks(stallCheck)
        val delayMs = min(MAX_BACKOFF_MS, 1_000L shl min(attempt, 5))
        attempt++
        onStatus(PlayerStatus.Retrying(reason, delayMs / 1_000))
        handler.postDelayed(retry, delayMs)
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        player.removeListener(this)
    }

    private fun describe(e: PlaybackException): String {
        val detail = generateSequence(e.cause) { it.cause }.mapNotNull { it.message }.joinToString(" ")
        return when {
            "401" in detail || "Unauthorized" in detail -> "Wrong user or password"
            e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Can't reach camera"
            e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Camera not responding"
            e.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "TV has no free video decoder"
            e.errorCode == PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> "Stream too large for this TV"
            else -> "Stream error (${e.errorCodeName})"
        }
    }

    private companion object {
        const val STALL_TIMEOUT_MS = 15_000L
        const val MAX_BACKOFF_MS = 30_000L
    }
}

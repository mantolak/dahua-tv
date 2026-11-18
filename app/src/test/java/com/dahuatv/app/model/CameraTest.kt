package com.dahuatv.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraTest {
    private val cam = Camera(name = "Gate", host = "192.168.0.20", username = "admin", password = "s3cret!")

    @Test
    fun `grid uses sub-stream subtype 1`() {
        assertEquals(
            "rtsp://192.168.0.20:554/cam/realmonitor?channel=1&subtype=1",
            cam.rtspUrl(Stream.SUB),
        )
    }

    @Test
    fun `fullscreen uses 4K main stream subtype 0`() {
        assertEquals(
            "rtsp://192.168.0.20:554/cam/realmonitor?channel=1&subtype=0",
            cam.rtspUrl(Stream.MAIN),
        )
    }

    @Test
    fun `password never appears in URL or toString`() {
        assertFalse(cam.rtspUrl(Stream.MAIN).contains("s3cret"))
        assertFalse(cam.toString().contains("s3cret"))
    }

    @Test
    fun `player URL carries percent-encoded credentials`() {
        val c = cam.copy(username = "admin", password = "t3st@P#ss:/ ł")
        assertEquals(
            "rtsp://admin:t3st%40P%23ss%3A%2F%20%C5%82@192.168.0.20:554/cam/realmonitor?channel=1&subtype=1",
            c.playerUrl(Stream.SUB),
        )
    }

    @Test
    fun `player URL without username has no userinfo`() {
        val c = cam.copy(username = "", password = "")
        assertEquals(c.rtspUrl(Stream.MAIN), c.playerUrl(Stream.MAIN))
    }

    @Test
    fun `percent-encoding keeps unreserved characters`() {
        assertEquals("Az09-._~", Camera.percentEncode("Az09-._~"))
    }

    @Test
    fun `empty slots are unconfigured and numbered from 1`() {
        val slot = Camera.empty(2)
        assertEquals("Camera 3", slot.name)
        assertTrue(!slot.isConfigured)
    }
}

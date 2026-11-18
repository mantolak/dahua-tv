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
    fun `empty slots are unconfigured and numbered from 1`() {
        val slot = Camera.empty(2)
        assertEquals("Camera 3", slot.name)
        assertTrue(!slot.isConfigured)
    }
}

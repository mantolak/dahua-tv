package com.dahuatv.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraFormTest {
    private val valid = CameraForm(
        name = " Gate ",
        host = " 192.168.0.20 ",
        port = "554",
        username = " admin ",
        password = " p@ss:word ",
        channel = "1",
    )

    @Test
    fun `valid form converts to trimmed camera but keeps password exactly`() {
        assertEquals(emptyMap<Field, String>(), valid.errors())
        assertEquals(
            Camera(name = "Gate", host = "192.168.0.20", port = 554, username = "admin", password = " p@ss:word ", channel = 1),
            valid.toCamera(),
        )
    }

    @Test
    fun `host is required`() {
        assertTrue(Field.HOST in valid.copy(host = "  ").errors())
    }

    @Test
    fun `rejects malformed IPv4 addresses`() {
        listOf("192.168.0.256", "192.168.0", "192.168.0.1.5", "1..2.3", "01.2.3.4").forEach {
            assertFalse("$it should be invalid", CameraForm.isValidHost(it))
        }
    }

    @Test
    fun `accepts IPv4 addresses and hostnames`() {
        listOf("192.168.0.20", "10.0.0.1", "255.255.255.255", "cam-gate.local", "nvr").forEach {
            assertTrue("$it should be valid", CameraForm.isValidHost(it))
        }
    }

    @Test
    fun `rejects hostnames with bad characters`() {
        listOf("cam gate", "-cam.local", "cam_.local", "http://cam").forEach {
            assertFalse("$it should be invalid", CameraForm.isValidHost(it))
        }
    }

    @Test
    fun `port must be 1 to 65535`() {
        listOf("", "0", "65536", "abc").forEach {
            assertTrue("port '$it' should be invalid", Field.PORT in valid.copy(port = it).errors())
        }
        assertFalse(Field.PORT in valid.copy(port = "65535").errors())
    }

    @Test
    fun `channel must be 1 to 64`() {
        assertTrue(Field.CHANNEL in valid.copy(channel = "0").errors())
        assertTrue(Field.CHANNEL in valid.copy(channel = "").errors())
        assertFalse(Field.CHANNEL in valid.copy(channel = "2").errors())
    }

    @Test
    fun `password without username is an error, no credentials is fine`() {
        assertTrue(Field.USERNAME in valid.copy(username = "").errors())
        assertEquals(emptyMap<Field, String>(), valid.copy(username = "", password = "").errors())
    }

    @Test
    fun `name is required`() {
        assertTrue(Field.NAME in valid.copy(name = " ").errors())
    }

    @Test
    fun `toCamera refuses an invalid form`() {
        assertThrows(IllegalStateException::class.java) { valid.copy(host = "").toCamera() }
    }

    @Test
    fun `form round-trips a saved camera`() {
        val cam = valid.toCamera()
        assertEquals(cam, CameraForm.from(cam).toCamera())
    }
}

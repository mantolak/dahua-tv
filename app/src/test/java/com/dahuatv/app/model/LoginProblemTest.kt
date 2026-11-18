package com.dahuatv.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginProblemTest {

    @Test
    fun `401 on any RTSP step is a wrong password`() {
        assertEquals(LoginProblem.WRONG_PASSWORD, LoginProblem.fromRtspError("DESCRIBE 401"))
        assertEquals(LoginProblem.WRONG_PASSWORD, LoginProblem.fromRtspError("java.io.IOException: OPTIONS 401"))
    }

    @Test
    fun `403 means the camera locked us out`() {
        assertEquals(LoginProblem.LOCKED, LoginProblem.fromRtspError("OPTIONS 403"))
        assertEquals(LoginProblem.LOCKED, LoginProblem.fromStatus(403))
    }

    @Test
    fun `other failures are not login problems`() {
        assertNull(LoginProblem.fromRtspError("DESCRIBE 404"))
        assertNull(LoginProblem.fromRtspError("Failed to connect to /192.168.0.401"))
        assertNull(LoginProblem.fromRtspError("SETUP 461"))
        assertNull(LoginProblem.fromStatus(500))
    }
}

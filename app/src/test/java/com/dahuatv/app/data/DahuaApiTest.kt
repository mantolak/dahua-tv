package com.dahuatv.app.data

import com.dahuatv.app.model.Stream
import org.junit.Assert.assertEquals
import org.junit.Test

class DahuaApiTest {

    @Test
    fun `parses getConfig key-value lines`() {
        val body = "table.ChannelTitle[0].Name=Front door\r\ntable.Encode[0].MainFormat[0].Video.FPS=20.000000\r\n\r\n"
        val cfg = DahuaApi.parseConfig(body)
        assertEquals("Front door", cfg["table.ChannelTitle[0].Name"])
        assertEquals("20.000000", cfg["table.Encode[0].MainFormat[0].Video.FPS"])
    }

    @Test
    fun `value may contain equals sign`() {
        assertEquals("a=b", DahuaApi.parseConfig("k=a=b")["k"])
    }

    @Test
    fun `info returns fps per stream`() {
        val info = CameraInfo(title = "Gate", mainFps = 20, subFps = 15)
        assertEquals(20, info.fps(Stream.MAIN))
        assertEquals(15, info.fps(Stream.SUB))
    }
}

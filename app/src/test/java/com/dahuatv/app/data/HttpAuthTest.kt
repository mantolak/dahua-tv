package com.dahuatv.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpAuthTest {

    // The worked example from RFC 2617 section 3.5.
    private val rfcChallenge =
        """Digest realm="testrealm@host.com", qop="auth,auth-int", nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093", opaque="5ccc069c403ebaf9f0171e9517f40e41""""

    @Test
    fun `parses a digest challenge`() {
        val c = HttpAuth.parseChallenge(rfcChallenge)
        assertEquals("testrealm@host.com", c["realm"])
        assertEquals("auth,auth-int", c["qop"])
        assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", c["nonce"])
        assertEquals("5ccc069c403ebaf9f0171e9517f40e41", c["opaque"])
    }

    @Test
    fun `digest response matches the RFC 2617 example`() {
        val header = HttpAuth.digest(
            username = "Mufasa",
            password = "Circle Of Life",
            method = "GET",
            uri = "/dir/index.html",
            challenge = HttpAuth.parseChallenge(rfcChallenge),
            cnonce = "0a4f113b",
        )
        assertTrue(header, header.contains("""response="6629fae49393a05397450978507c4ef1""""))
        assertTrue(header.contains("qop=auth, nc=00000001, cnonce=\"0a4f113b\""))
        assertTrue(header.contains("opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""))
    }

    @Test
    fun `parses unquoted challenge values`() {
        val c = HttpAuth.parseChallenge("""Digest realm="Login to 6J0B", qop=auth, nonce="123", algorithm=MD5""")
        assertEquals("Login to 6J0B", c["realm"])
        assertEquals("auth", c["qop"])
        assertEquals("MD5", c["algorithm"])
    }

    @Test
    fun `basic header`() {
        assertEquals("Basic YWRtaW46cEBzcw==", HttpAuth.basic("admin", "p@ss"))
    }
}

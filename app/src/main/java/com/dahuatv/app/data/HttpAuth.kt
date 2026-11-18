package com.dahuatv.app.data

import java.security.MessageDigest
import java.util.Base64

/** HTTP Basic and Digest (RFC 2617, MD5) — Android's HttpURLConnection only does Basic on its own. */
object HttpAuth {

    private val PARAM = Regex("""(\w+)=(?:"([^"]*)"|([^,\s]+))""")

    /** `Digest realm="x", nonce="y", qop="auth"` -> {realm=x, nonce=y, qop=auth} */
    fun parseChallenge(header: String): Map<String, String> =
        PARAM.findAll(header.substringAfter(' ')).associate { m ->
            m.groupValues[1].lowercase() to (m.groups[2]?.value ?: m.groupValues[3])
        }

    fun digest(
        username: String,
        password: String,
        method: String,
        uri: String,
        challenge: Map<String, String>,
        cnonce: String,
        nc: String = "00000001",
    ): String {
        val realm = challenge["realm"].orEmpty()
        val nonce = challenge["nonce"].orEmpty()
        val qop = challenge["qop"]?.split(',')?.map { it.trim() }?.firstOrNull { it == "auth" }
        val ha1 = md5("$username:$realm:$password")
        val ha2 = md5("$method:$uri")
        val response = if (qop != null) md5("$ha1:$nonce:$nc:$cnonce:$qop:$ha2") else md5("$ha1:$nonce:$ha2")
        return buildString {
            append("Digest username=\"$username\", realm=\"$realm\", nonce=\"$nonce\", uri=\"$uri\", response=\"$response\"")
            if (qop != null) append(", qop=$qop, nc=$nc, cnonce=\"$cnonce\"")
            challenge["opaque"]?.let { append(", opaque=\"$it\"") }
            challenge["algorithm"]?.let { append(", algorithm=$it") }
        }
    }

    fun basic(username: String, password: String): String =
        "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray(Charsets.UTF_8))

    internal fun md5(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

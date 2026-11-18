package com.dahuatv.app.model

/** Which Dahua stream to pull: sub-stream for the 2x2 grid, main (4K) stream for fullscreen. */
enum class Stream(val subtype: Int) {
    SUB(1),
    MAIN(0),
}

data class Camera(
    val name: String,
    val host: String = "",
    val port: Int = DEFAULT_PORT,
    val username: String = "",
    val password: String = "",
    val channel: Int = 1,
) {
    val isConfigured: Boolean get() = host.isNotBlank()

    /** Dahua RTSP URL without credentials: safe to show on screen or log. */
    fun rtspUrl(stream: Stream): String = buildUrl(stream, userInfo = "")

    /**
     * URL for the player only: Media3 reads RTSP credentials from the userinfo part and strips them
     * before sending requests. Credentials are percent-encoded so characters like @ # : / survive.
     * Never log or display this value.
     */
    fun playerUrl(stream: Stream): String =
        if (username.isEmpty()) rtspUrl(stream)
        else buildUrl(stream, userInfo = "${percentEncode(username)}:${percentEncode(password)}@")

    private fun buildUrl(stream: Stream, userInfo: String) =
        "rtsp://$userInfo$host:$port/cam/realmonitor?channel=$channel&subtype=${stream.subtype}"

    /** Safe to log or show on screen: never includes the password. */
    override fun toString(): String =
        "Camera(name=$name, host=$host, port=$port, username=$username, channel=$channel)"

    companion object {
        const val GRID_SIZE = 4
        const val DEFAULT_PORT = 554

        fun defaultName(index: Int) = "Camera ${index + 1}"

        /** An empty, not-yet-configured grid slot. */
        fun empty(index: Int) = Camera(name = defaultName(index))

        /** RFC 3986 percent-encoding: everything except unreserved characters becomes %XX (UTF-8). */
        internal fun percentEncode(s: String): String = buildString {
            for (b in s.toByteArray(Charsets.UTF_8)) {
                val c = b.toInt() and 0xFF
                val ch = c.toChar()
                if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' || ch in "-._~") append(ch)
                else append('%').append("%02X".format(c))
            }
        }
    }
}

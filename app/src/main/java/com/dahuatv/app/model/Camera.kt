package com.dahuatv.app.model

/** Which Dahua stream to pull: sub-stream for the 2x2 grid, main (4K) stream for fullscreen. */
enum class Stream(val subtype: Int) {
    SUB(1),
    MAIN(0),
}

data class Camera(
    val name: String,
    val host: String = "",
    val port: Int = 554,
    val username: String = "",
    val password: String = "",
    val channel: Int = 1,
) {
    val isConfigured: Boolean get() = host.isNotBlank()

    /** Dahua RTSP URL without credentials; credentials are passed to the player separately. */
    fun rtspUrl(stream: Stream): String =
        "rtsp://$host:$port/cam/realmonitor?channel=$channel&subtype=${stream.subtype}"

    /** Safe to log or show on screen: never includes the password. */
    override fun toString(): String =
        "Camera(name=$name, host=$host, port=$port, username=$username, channel=$channel)"

    companion object {
        const val GRID_SIZE = 4

        /** Placeholder until the settings + encrypted storage step. */
        fun placeholders(): List<Camera> = List(GRID_SIZE) { Camera(name = "Camera ${it + 1}") }
    }
}

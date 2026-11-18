package com.dahuatv.app.model

enum class Field { NAME, HOST, PORT, USERNAME, PASSWORD, CHANNEL }

/** What the user types on the settings screen: raw strings, validated before becoming a [Camera]. */
data class CameraForm(
    val name: String = "",
    val host: String = "",
    val port: String = Camera.DEFAULT_PORT.toString(),
    val username: String = "",
    val password: String = "",
    val channel: String = "1",
) {
    fun errors(): Map<Field, String> = buildMap {
        if (name.isBlank()) put(Field.NAME, "Enter a name")

        val h = host.trim()
        when {
            h.isEmpty() -> put(Field.HOST, "Enter the camera IP address")
            !isValidHost(h) -> put(Field.HOST, "Not a valid IP address or hostname")
        }

        val p = port.trim().toIntOrNull()
        if (p == null || p !in 1..65535) put(Field.PORT, "Port must be 1–65535")

        val c = channel.trim().toIntOrNull()
        if (c == null || c !in 1..64) put(Field.CHANNEL, "Channel must be 1–64")

        if (username.isBlank() && password.isNotEmpty()) put(Field.USERNAME, "Enter the user for this password")
    }

    /** Converts a valid form. Passwords are kept exactly as typed (no trimming). */
    fun toCamera(): Camera {
        check(errors().isEmpty()) { "Form has validation errors" }
        return Camera(
            name = name.trim(),
            host = host.trim(),
            port = port.trim().toInt(),
            username = username.trim(),
            password = password,
            channel = channel.trim().toInt(),
        )
    }

    companion object {
        fun from(c: Camera) = CameraForm(
            name = c.name,
            host = c.host,
            port = c.port.toString(),
            username = c.username,
            password = c.password,
            channel = c.channel.toString(),
        )

        private val IPV4 =
            Regex("""^((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)$""")
        private val HOSTNAME =
            Regex("""^(?=.{1,253}$)[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$""")

        /** All-digits-and-dots must be a real IPv4 address; anything else must be a valid hostname. */
        fun isValidHost(h: String): Boolean =
            if (h.all { it.isDigit() || it == '.' }) IPV4.matches(h) else HOSTNAME.matches(h)
    }
}

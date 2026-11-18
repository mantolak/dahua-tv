package com.dahuatv.app.model

/**
 * A camera refusing our login. These must NOT be retried automatically: Dahua cameras lock out
 * the client after a few failed logins, and every further attempt keeps the lock alive.
 */
enum class LoginProblem(val message: String) {
    WRONG_PASSWORD("Wrong user or password · hold OK to fix"),
    LOCKED("Camera blocked this TV after failed logins · wait, then reopen"),
    ;

    companion object {
        private val RTSP_STATUS = Regex("""\b(?:OPTIONS|DESCRIBE|SETUP|PLAY|GET_PARAMETER)\s+(\d{3})\b""")

        /** From an RTSP error text such as "DESCRIBE 401", or null if it isn't a login problem. */
        fun fromRtspError(text: String): LoginProblem? =
            RTSP_STATUS.find(text)?.groupValues?.get(1)?.toInt()?.let(::fromStatus)

        fun fromStatus(status: Int): LoginProblem? = when (status) {
            401 -> WRONG_PASSWORD
            403 -> LOCKED
            else -> null
        }
    }
}

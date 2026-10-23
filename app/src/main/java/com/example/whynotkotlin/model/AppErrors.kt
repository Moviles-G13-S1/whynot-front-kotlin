package com.example.whynotkotlin.model

/**
 * A rule the user's input broke, detected before anything reaches the backend.
 *
 * Controllers throw it so a view can show [message] the same way it shows a
 * backend failure.
 */
class ValidationException(message: String) : Exception(message)

/**
 * Firebase messages are developer-facing and sometimes leak internals, so the
 * common cases get a short message and everything else falls back to the raw
 * one rather than hiding what went wrong.
 */
fun Throwable.readableMessage(): String {
    if (this is com.google.firebase.FirebaseNetworkException) {
        return "No connection. Check your network and try again"
    }
    if (this is com.google.firebase.FirebaseTooManyRequestsException) {
        return "Too many attempts. Wait a moment and try again."
    }
    if (this is com.google.firebase.auth.FirebaseAuthException) {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "That email address is not valid"
            "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS" -> "Wrong email or password"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "That email already has an account"
            "ERROR_USER_DISABLED" -> "This account is disabled. Contact support."
            "ERROR_REQUIRES_RECENT_LOGIN", "ERROR_USER_TOKEN_EXPIRED", "ERROR_INVALID_USER_TOKEN" ->
                "Your session expired. Sign in again."
            "ERROR_WEAK_PASSWORD" -> "That password is too weak. Follow the password requirements"
            else -> "Authentication failed. Try again."
        }
    }
    val raw = message.orEmpty()

    return when {
        raw.contains("password is invalid", ignoreCase = true) ||
            raw.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            raw.contains("credential is incorrect", ignoreCase = true) ->
            "Wrong email or password"

        raw.contains("no user record", ignoreCase = true) ->
            "No account uses that email"

        raw.contains("email address is already in use", ignoreCase = true) ->
            "That email already has an account"

        raw.contains("WEAK_PASSWORD", ignoreCase = true) ||
            raw.contains("at least 6 characters", ignoreCase = true) ->
            "That password is too weak. Follow the password requirements"

        raw.contains("badly formatted", ignoreCase = true) ->
            "That email address is not valid"

        raw.contains("PERMISSION_DENIED", ignoreCase = true) ->
            "The backend rejected this operation"

        raw.contains("network", ignoreCase = true) ->
            "No connection. Check your network and try again"

        raw.isBlank() -> "Something went wrong"
        else -> raw
    }
}

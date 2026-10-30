package com.example.whynotkotlin.model

/**
 * A failure whose message was written for the user, so screens show it as it
 * is. Any other exception gets a short, generic message instead.
 */
open class UserFacingException(message: String) : Exception(message)

/**
 * A rule the user's input broke, detected before anything reaches the backend.
 *
 * Controllers throw it so a view can show [message] the same way it shows a
 * backend failure.
 */
class ValidationException(message: String) : UserFacingException(message)

private const val GENERIC_MESSAGE = "Something went wrong. Try again."

/**
 * Says the email cannot be used without stating that it belongs to an account.
 * Firebase rejects the sign-up itself, so this is as far as the app can go.
 */
private const val EMAIL_UNAVAILABLE =
    "We could not create an account with that email. If you already have one, log in instead."

/**
 * Firebase messages are developer-facing and sometimes leak internals, so the
 * common cases get a short message and anything unrecognised gets a generic one
 * instead of raw technical text. Messages the app wrote for the user
 * ([UserFacingException]) are shown as they are.
 */
fun Throwable.readableMessage(): String {
    if (this is UserFacingException) return message ?: GENERIC_MESSAGE

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
            "ERROR_EMAIL_ALREADY_IN_USE" -> EMAIL_UNAVAILABLE
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
            raw.contains("credential is incorrect", ignoreCase = true) ||
            raw.contains("no user record", ignoreCase = true) ->
            "Wrong email or password"

        raw.contains("email address is already in use", ignoreCase = true) ->
            EMAIL_UNAVAILABLE

        raw.contains("WEAK_PASSWORD", ignoreCase = true) ||
            raw.contains("at least 6 characters", ignoreCase = true) ->
            "That password is too weak. Follow the password requirements"

        raw.contains("badly formatted", ignoreCase = true) ->
            "That email address is not valid"

        raw.contains("PERMISSION_DENIED", ignoreCase = true) ->
            "The backend rejected this operation"

        raw.contains("network", ignoreCase = true) ->
            "No connection. Check your network and try again"

        else -> GENERIC_MESSAGE
    }
}

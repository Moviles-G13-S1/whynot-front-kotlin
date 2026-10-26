package com.example.whynotkotlin.model.repositories

import kotlinx.coroutines.flow.StateFlow

/**
 * Whether the device can currently reach the internet.
 *
 * Shared by every feature that changes its behaviour offline (eventual
 * connectivity): screens read [isOnline] to show the offline banner, keep
 * cached data on screen, and refresh once the connection returns.
 */
interface ConnectivityRepository {

    /** True while the device has a validated internet connection. */
    val isOnline: StateFlow<Boolean>
}

/**
 * An action that needs the backend was attempted offline. Nothing was sent, so
 * the form keeps what the user typed and can try again once back online.
 */
class OfflineException(message: String) : Exception(message)

/** Throws [OfflineException] with [message] when the device is offline. */
fun ConnectivityRepository?.requireOnline(message: String) {
    if (this != null && !isOnline.value) throw OfflineException(message)
}

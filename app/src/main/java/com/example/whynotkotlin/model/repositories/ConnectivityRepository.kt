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

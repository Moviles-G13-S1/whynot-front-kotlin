package com.example.whynotkotlin.features.nearby.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.features.nearby.domain.DeviceLocation
import com.example.whynotkotlin.features.nearby.domain.LocationPermissionDeniedException
import com.example.whynotkotlin.features.nearby.domain.LocationRepository
import com.example.whynotkotlin.features.nearby.domain.LocationUnavailableException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

/**
 * Reads the device position with Google Play Services.
 *
 * The coordinates are request input for `get_nearest_store` and are never
 * persisted, which is what the architecture requires of the Context-Aware
 * feature.
 */
class AndroidLocationRepository(
    context: Context
) : LocationRepository {

    // The application context: this repository lives as long as the process.
    private val appContext = context.applicationContext

    private val client =
        LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): DeviceLocation {
        if (!hasLocationPermission()) {
            throw LocationPermissionDeniedException()
        }

        // A fresh fix is preferred, but it can be null indoors or with location
        // switched off, so the last known position is the fallback.
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        val location = client.getCurrentLocation(request, null).await()
            ?: client.lastLocation.await()
            ?: throw LocationUnavailableException()

        return DeviceLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    /**
     * Coarse permission is enough: the backend only needs to pick the nearest
     * store out of sixteen, so city-block accuracy changes nothing.
     */
    private fun hasLocationPermission(): Boolean =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(appContext, permission) ==
            PackageManager.PERMISSION_GRANTED
}

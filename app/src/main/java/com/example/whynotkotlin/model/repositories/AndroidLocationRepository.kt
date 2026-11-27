package com.example.whynotkotlin.model.repositories

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.LocationPermissionDeniedException
import com.example.whynotkotlin.model.repositories.LocationRepository
import com.example.whynotkotlin.model.LocationUnavailableException
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

        // System Trace section, to see in the profiler how long Home waits for the fix.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Trace.beginAsyncSection(TRACE_SECTION, 0)
        val startedAt = SystemClock.elapsedRealtime()
        var source = "fresh"
        val location = try {
            // Micro-optimization (Sprint 4, Home load path): a position the
            // phone already knows from the last few minutes is reused at once.
            // Asking for a new fix right after the app starts took up to 7 s on
            // the test phone, and Home waited for it before finding the nearest
            // store; one of sixteen stores in the city does not change in a
            // few minutes.
            val known = client.lastLocation.await()
            if (known != null && known.ageMillis() <= MAX_REUSE_AGE_MILLIS) {
                source = "recent(age=${known.ageMillis() / 1000}s)"
                known
            } else {
                client.getCurrentLocation(request, null).await()
                    ?: known
                    ?: throw LocationUnavailableException()
            }
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Trace.endAsyncSection(TRACE_SECTION, 0)
        }
        Log.d(LOG_TAG, "fix in ${SystemClock.elapsedRealtime() - startedAt}ms source=$source")

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

/** System Trace section around the location fix (Sprint 4 micro-optimization). */
private const val TRACE_SECTION = "WhyNot#locationFix"
private const val LOG_TAG = "WhyNotLocation"

/** How old a known position may be to reuse it instead of asking for a new fix. */
private const val MAX_REUSE_AGE_MILLIS = 5 * 60 * 1000L

private fun android.location.Location.ageMillis(): Long =
    (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000

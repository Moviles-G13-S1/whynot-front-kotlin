package com.example.whynotkotlin.features.nearby.domain

/**
 * The user has not granted a location permission yet.
 *
 * The repository cannot ask for it: requesting a runtime permission needs an
 * Activity, so the screen must request it and call the ViewModel again.
 */
class LocationPermissionDeniedException :
    Exception("Location permission is required to find stores near you.")

/**
 * The permission is granted but the device returned no position, usually
 * because location services are switched off or no fix is available yet.
 */
class LocationUnavailableException :
    Exception("Could not read your location. Check that location is turned on.")

package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.repositories.LocationRepository
import com.example.whynotkotlin.model.repositories.NearbyStoreRepository

class NearbyStoreController(
    private val locationRepository: LocationRepository,
    private val nearbyStoreRepository: NearbyStoreRepository
) {

    /**
     * The store closest to the device, or null when the backend has none.
     *
     * The store lookup is never sent when the location cannot be read.
     */
    suspend fun loadNearestStore(): NearbyStore? {
        val location = locationRepository.getCurrentLocation()

        return nearbyStoreRepository.getNearestStore(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }
}

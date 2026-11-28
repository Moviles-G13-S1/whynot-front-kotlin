package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.repositories.LocationRepository
import com.example.whynotkotlin.model.repositories.NearbyStoreRepository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NearbyStoreController(
    private val locationRepository: LocationRepository,
    private val nearbyStoreRepository: NearbyStoreRepository,
    clockMillis: () -> Long = { System.nanoTime() / 1_000_000L }
) {

    private val cache = NearbyLookupCache(clockMillis)
    private val lookupLock = Mutex()

    /**
     * The store closest to the device, or null when the backend has none.
     *
     * The store lookup is never sent when the location cannot be read.
     */
    suspend fun loadNearestStore(
        uid: String? = null,
        category: String? = null,
        forceRefresh: Boolean = false
    ): NearbyStore? {
        val location = locationRepository.getCurrentLocation()

        if (uid == null || category == null) {
            return nearbyStoreRepository.getNearestStore(location.latitude, location.longitude)
        }
        val key = NearbyLookupCache.Key(uid, category, location)
        return lookupLock.withLock {
            if (!forceRefresh) cache.get(key)?.let { return@withLock it }
            val result = nearbyStoreRepository.getNearestStore(location.latitude, location.longitude)
            cache.put(key, result)
            result
        }
    }
}

package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.NearbyStore
import org.junit.Assert.*
import org.junit.Test

class NearbyLookupCacheTest {
    private var now = 0L
    private val cache = NearbyLookupCache { now }
    private val key = NearbyLookupCache.Key("Aa", "fashion", DeviceLocation(4.7, -74.1))
    private val store = NearbyStore("1", "Shop", "Street", 4.7, -74.1, listOf("fashion"), "", "", 0.0)

    @Test fun expiresAfterOneMinute() {
        cache.put(key, store)
        now = 59_999
        assertEquals(store, cache.get(key))
        now++
        assertNull(cache.get(key))
    }

    @Test fun differentKeysCannotReadAnotherResult() {
        cache.put(key, store)
        assertNull(cache.get(key.copy(category = "beauty")))
        assertNull(cache.get(key.copy(position = DeviceLocation(4.71, -74.1))))
        // Aa and BB have the same Java String hash.
        val other = key.copy(uid = "BB")
        assertEquals(key.hashCode(), other.hashCode())
        assertNull(cache.get(other))
        cache.put(other, store)
        assertNull(cache.get(key))
        assertEquals(store, cache.get(other))
    }

    @Test fun emptyServerResponseClearsOldResult() {
        cache.put(key, store)
        cache.put(key, null)
        assertNull(cache.get(key))
    }
}

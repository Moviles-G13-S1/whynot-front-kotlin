package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.NearbyStore

/** Direct-mapped cache. A collision replaces that slot; a full key check prevents false hits. */
internal class NearbyLookupCache(private val clockMillis: () -> Long) {
    data class Key(val uid: String, val category: String, val position: DeviceLocation)
    private data class Entry(val key: Key, val store: NearbyStore, val savedAt: Long)
    private val slots = arrayOfNulls<Entry>(8)
    private fun slot(key: Key) = key.hashCode() and (slots.size - 1)

    fun get(key: Key): NearbyStore? {
        val entry = slots[slot(key)] ?: return null
        return entry.store.takeIf { entry.key == key && clockMillis() - entry.savedAt in 0L until 60_000L }
    }

    fun put(key: Key, store: NearbyStore?) {
        slots[slot(key)] = store?.let { Entry(key, it, clockMillis()) }
    }
}

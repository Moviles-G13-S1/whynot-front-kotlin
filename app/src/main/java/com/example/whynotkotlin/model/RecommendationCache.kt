package com.example.whynotkotlin.model

import android.util.Log
import androidx.collection.LruCache

/** A cached recommendation and when it was stored. */
data class CachedRecommendation(
    val recommendation: ProductRecommendation,
    val storedAtMillis: Long
)

/**
 * In-memory LRU cache of the last recommendation per signed-in user.
 *
 * Keyed by uid, so one user never sees another's recommendation. An entry is
 * fresh for [ttlMillis]; after that [getFresh] treats it as a miss, but
 * [getStale] still returns it so the app can show something offline. When more
 * than [maxEntries] users are cached, the least recently used one is evicted.
 */
class RecommendationCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
    private val logger: (String) -> Unit = { Log.d(TAG, it) }
) {

    var hits = 0
        private set
    var misses = 0
        private set
    var expired = 0
        private set
    var evictions = 0
        private set

    private val entries = object : LruCache<String, CachedRecommendation>(maxEntries) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: CachedRecommendation,
            newValue: CachedRecommendation?
        ) {
            if (evicted) {
                evictions++
                logger("EVICTED uid=${key.short()}")
            }
        }
    }

    /** The recommendation for [uid] if it is still within its lifetime. */
    fun getFresh(uid: String): ProductRecommendation? {
        val entry = entries[uid]

        if (entry == null) {
            misses++
            logger("MISS uid=${uid.short()}")
            return null
        }

        val age = clock() - entry.storedAtMillis
        if (age > ttlMillis) {
            expired++
            logger("EXPIRED uid=${uid.short()} age=${age}ms")
            return null
        }

        hits++
        logger("HIT uid=${uid.short()} age=${age}ms")
        return entry.recommendation
    }

    /** The last entry for [uid], even if expired: the offline fallback. */
    fun getStale(uid: String): CachedRecommendation? = entries[uid]

    fun put(uid: String, recommendation: ProductRecommendation) {
        entries.put(uid, CachedRecommendation(recommendation, clock()))
        logger("PUT uid=${uid.short()} size=${entries.size()}/$maxEntries")
    }

    fun invalidate(uid: String, reason: String) {
        if (entries.remove(uid) != null) {
            logger("INVALIDATE uid=${uid.short()} reason=$reason")
        }
    }

    /** Drops every entry; removals here are not counted as evictions. */
    fun clear(reason: String) {
        entries.snapshot().keys.forEach { entries.remove(it) }
        logger("CLEAR reason=$reason")
    }

    private fun String.short(): String = take(6)

    companion object {
        const val TAG = "WhyNotCache"
        const val DEFAULT_MAX_ENTRIES = 5
        const val DEFAULT_TTL_MILLIS = 30L * 60 * 1000
    }
}

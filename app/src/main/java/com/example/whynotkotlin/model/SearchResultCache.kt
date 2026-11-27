package com.example.whynotkotlin.model

import android.util.Log

/**
 * Caching for Global Product Search (Sprint 4, Miguel): an in-memory LFU
 * (Least Frequently Used) cache of search results.
 *
 * - Key: the validated [SearchCriteria] (text, category, price range).
 * - Value: the results for the current product list.
 * - Capacity: [maxEntries]. When full, the entry used the fewest times is
 *   evicted; among equals, the oldest one (insertion order of [LinkedHashMap]).
 *
 * Why LFU and not LRU: in a search screen people go back to the same few
 * searches ("all", one category, a favourite brand) many times, while
 * one-off typos are used once. LFU keeps the popular searches and drops the
 * typos first, even if a typo was the most recent.
 *
 * Invalidation: the results only make sense for one user and one product list.
 * When the user or the list changes (a product saved, edited, purchased or
 * deleted), every entry is dropped. A result computed for an old list can
 * never be stored, because [put] checks the list it was computed from.
 */
class SearchResultCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val logger: (String) -> Unit = { Log.d(TAG, it) }
) {

    private class Entry(val results: List<Product>, var uses: Int)

    private val entries = LinkedHashMap<SearchCriteria, Entry>()
    private var ownerId: String? = null
    private var products: List<Product>? = null

    init {
        require(maxEntries > 0) { "maxEntries must be positive" }
    }

    /** Cached results for [criteria] over [productList], or null on a miss. */
    @Synchronized
    fun get(ownerId: String, productList: List<Product>, criteria: SearchCriteria): List<Product>? {
        resetIfStale(ownerId, productList)

        val entry = entries[criteria]
        if (entry == null) {
            logger("MISS \"${criteria.query}\"")
            return null
        }
        entry.uses++
        logger("HIT \"${criteria.query}\" uses=${entry.uses}")
        return entry.results
    }

    /** Stores [results]; ignored if [productList] is no longer the current list. */
    @Synchronized
    fun put(
        ownerId: String,
        productList: List<Product>,
        criteria: SearchCriteria,
        results: List<Product>
    ) {
        if (ownerId != this.ownerId || productList !== products) return

        if (criteria !in entries && entries.size >= maxEntries) evictLeastFrequent()
        entries[criteria] = Entry(results, uses = 1)
        logger("PUT \"${criteria.query}\" size=${entries.size}/$maxEntries")
    }

    @Synchronized
    fun invalidate(reason: String) {
        if (entries.isNotEmpty()) logger("INVALIDATE ($reason) dropped=${entries.size}")
        entries.clear()
        ownerId = null
        products = null
    }

    @Synchronized
    fun size(): Int = entries.size

    private fun resetIfStale(ownerId: String, productList: List<Product>) {
        if (ownerId == this.ownerId && productList === products) return

        if (this.ownerId != null) {
            invalidate(if (ownerId != this.ownerId) "user changed" else "products changed")
        }
        this.ownerId = ownerId
        this.products = productList
    }

    private fun evictLeastFrequent() {
        // The first entry with the fewest uses: iteration follows insertion
        // order, so ties evict the oldest entry.
        val victim = entries.entries.minByOrNull { it.value.uses } ?: return
        entries.remove(victim.key)
        logger("EVICT \"${victim.key.query}\" uses=${victim.value.uses}")
    }

    companion object {
        const val TAG = "WhyNotSearch"
        const val DEFAULT_MAX_ENTRIES = 32
    }
}

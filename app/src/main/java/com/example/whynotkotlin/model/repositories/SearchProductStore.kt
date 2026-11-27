package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.LocalSearchSnapshot
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.SearchFilters

/**
 * Local storage for Global Product Search (Sprint 4, Miguel).
 *
 * The products of each user are kept on the phone, so search keeps working
 * offline and right after a restart, before Firestore answers.
 */
interface SearchProductStore {

    /** The products saved for [ownerId], or null if nothing was saved yet. */
    suspend fun load(ownerId: String): LocalSearchSnapshot?

    /** Replaces everything saved for [ownerId] in one transaction. */
    suspend fun replace(ownerId: String, products: List<Product>, syncedAtMillis: Long)

    suspend fun clear(ownerId: String)
}

/** Local storage for the search form: what each user last searched for. */
interface SearchFilterStore {

    suspend fun load(ownerId: String): SearchFilters?

    suspend fun save(ownerId: String, filters: SearchFilters)
}

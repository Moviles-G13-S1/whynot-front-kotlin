package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.CachedRecommendation
import com.example.whynotkotlin.model.ProductRecommendation

/**
 * Keeps the last recommendation of each user on the phone, so it survives
 * closing the app (local storage). The in-memory RecommendationCache is lost
 * when the process dies; this store is the offline fallback after a restart.
 */
interface RecommendationFileStore {

    suspend fun save(uid: String, recommendation: ProductRecommendation)

    /** The last recommendation saved for [uid], or null when there is none. */
    suspend fun load(uid: String): CachedRecommendation?

    suspend fun delete(uid: String)

    /** Removes every user's file, so the next account never sees them. */
    suspend fun clearAll()
}

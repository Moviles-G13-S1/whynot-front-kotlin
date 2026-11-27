package com.example.whynotkotlin.model.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Sprint 4 BQ (Miguel, Type 1): "What is the average loading time of the Home
 * screen per platform?"
 *
 * One `screenLoadEvents` document per measured load. The backend metric
 * `home_load_time` averages them per platform.
 */
interface ScreenLoadRepository {
    suspend fun recordScreenLoad(ownerId: String, screen: String, durationMs: Long)
}

class FirebaseScreenLoadRepository(
    private val firestore: FirebaseFirestore
) : ScreenLoadRepository {

    override suspend fun recordScreenLoad(ownerId: String, screen: String, durationMs: Long) {
        // The rules accept exactly these keys, and createdAt must be the server time.
        val data = hashMapOf(
            "userId" to ownerId,
            "screen" to screen,
            "durationMs" to durationMs,
            "platform" to PLATFORM,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection(COLLECTION).add(data).await()
    }

    private companion object {
        const val COLLECTION = "screenLoadEvents"
        const val PLATFORM = "android"
    }
}

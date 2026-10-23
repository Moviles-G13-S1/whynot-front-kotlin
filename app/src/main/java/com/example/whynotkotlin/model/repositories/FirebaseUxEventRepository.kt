package com.example.whynotkotlin.model.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUxEventRepository(private val firestore: FirebaseFirestore) : UxEventRepository {
    override suspend fun record(ownerId: String, eventName: String, source: String) {
        firestore.collection("uxEvents").add(
            mapOf(
                "userId" to ownerId, "eventName" to eventName, "source" to source,
                "platform" to "android", "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}

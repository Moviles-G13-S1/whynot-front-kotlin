package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.repositories.NearbyStoreRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseNearbyStoreRepository(
    private val functions: FirebaseFunctions
) : NearbyStoreRepository {
    override suspend fun getNearestStore(
        latitude: Double,
        longitude: Double
    ): NearbyStore? {
        val result = functions
            .getHttpsCallable("get_nearest_store")
            .call(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            )
            .await()

        val response = result.getData() as? Map<*, *>
            ?: throw IllegalStateException(
                "Invalid nearby-store response."
            )

        val rawStore = response["store"]
            ?: return null

        val store = rawStore as? Map<*, *>
            ?: throw IllegalStateException(
                "Invalid store response."
            )

        return NearbyStore(
            id = store["id"] as? String
                ?: "",

            name = store["name"] as? String
                ?: "",

            address = store["address"] as? String
                ?: "",

            latitude = (store["latitude"] as? Number)
                ?.toDouble()
                ?: 0.0,

            longitude = (store["longitude"] as? Number)
                ?.toDouble()
                ?: 0.0,

            categoryIds = (store["categoryIds"] as? List<*>)
                ?.mapNotNull { it as? String }
                .orEmpty(),

            websiteUrl = store["websiteUrl"] as? String
                ?: "",

            imageUrl = store["imageUrl"] as? String
                ?: "",

            distanceKm = (store["distanceKm"] as? Number)
                ?.toDouble()
                ?: 0.0
        )
    }
}
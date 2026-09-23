package com.example.whynotkotlin.features.nearby.data

import com.example.whynotkotlin.features.nearby.domain.NearbyStore
import com.example.whynotkotlin.features.nearby.domain.NearbyStoreRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Real Firebase implementation of NearbyStoreRepository.
 *
 * This class calls the production Cloud Function get_nearest_store.
 * It contains NO fake data.
 *
 * Juan Felipe owns this repository as part of the Nearby integration.
 * Miguel will later provide the real Android location implementation
 * that supplies the latitude and longitude used here.
 */
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
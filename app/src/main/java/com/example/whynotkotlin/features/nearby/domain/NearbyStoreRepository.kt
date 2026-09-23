package com.example.whynotkotlin.features.nearby.domain

interface NearbyStoreRepository {

    suspend fun getNearestStore(
        latitude: Double,
        longitude: Double
    ): NearbyStore?
}
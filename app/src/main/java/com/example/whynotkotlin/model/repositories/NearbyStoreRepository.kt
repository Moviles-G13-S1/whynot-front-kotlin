package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.NearbyStore

interface NearbyStoreRepository {

    suspend fun getNearestStore(
        latitude: Double,
        longitude: Double
    ): NearbyStore?
}
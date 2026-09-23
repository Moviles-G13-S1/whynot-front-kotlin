package com.example.whynotkotlin.features.nearby.domain

interface LocationRepository {

    suspend fun getCurrentLocation(): DeviceLocation
}
package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.DeviceLocation

interface LocationRepository {

    suspend fun getCurrentLocation(): DeviceLocation
}
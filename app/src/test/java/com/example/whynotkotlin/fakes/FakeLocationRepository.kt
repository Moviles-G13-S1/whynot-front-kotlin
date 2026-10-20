package com.example.whynotkotlin.fakes

import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.repositories.LocationRepository

class FakeLocationRepository(
    var location: DeviceLocation = DeviceLocation(
        latitude = 4.7110,
        longitude = -74.0721
    ),
    var error: Exception? = null
) : LocationRepository {
    override suspend fun getCurrentLocation(): DeviceLocation {
        error?.let { throw it }
        return location
    }
}
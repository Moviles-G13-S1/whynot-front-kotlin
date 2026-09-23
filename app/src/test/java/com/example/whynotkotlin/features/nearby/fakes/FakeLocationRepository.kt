package com.example.whynotkotlin.features.nearby.fakes

import com.example.whynotkotlin.features.nearby.domain.DeviceLocation
import com.example.whynotkotlin.features.nearby.domain.LocationRepository

/**
 * FAKE SOLO PARA PRUEBAS.
 *
 * Esta funcionalidad se prueba con datos fake mientras Miguel implementa
 * AndroidLocationRepository y la obtención real de ubicación mediante GPS.
 *
 * NO usar esta clase en src/main.
 * NO registrarla en AppDependencies.
 * NO representa la implementación final de ubicación.
 *
 * Cuando Miguel termine su implementación, este fake seguirá existiendo
 * únicamente para pruebas automatizadas.
 */
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
package com.example.whynotkotlin.features.nearby.fakes

import com.example.whynotkotlin.features.nearby.domain.NearbyStore
import com.example.whynotkotlin.features.nearby.domain.NearbyStoreRepository

/**
 * FAKE SOLO PARA PRUEBAS.
 *
 * Permite validar la lógica de NearbyStoreViewModel sin depender de Firebase.
 *
 * La implementación real de acceso a tiendas es
 * FirebaseNearbyStoreRepository, ubicada en src/main.
 *
 * NO usar esta clase en AppDependencies.
 */
class FakeNearbyStoreRepository(
    var store: NearbyStore? = NearbyStore(
        id = "fake-store-1",
        name = "Fake Store",
        address = "Test address",
        latitude = 4.7110,
        longitude = -74.0721,
        categoryIds = listOf("clothes"),
        websiteUrl = "https://example.com",
        imageUrl = "",
        distanceKm = 0.8
    ),
    var error: Exception? = null
) : NearbyStoreRepository {

    var lastLatitude: Double? = null
    var lastLongitude: Double? = null

    override suspend fun getNearestStore(
        latitude: Double,
        longitude: Double
    ): NearbyStore? {

        error?.let { throw it }

        lastLatitude = latitude
        lastLongitude = longitude

        return store
    }
}
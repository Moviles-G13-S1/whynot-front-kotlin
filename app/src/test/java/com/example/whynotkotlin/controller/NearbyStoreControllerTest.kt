package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeLocationRepository
import com.example.whynotkotlin.fakes.FakeNearbyStoreRepository
import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.NearbyStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class NearbyStoreControllerTest {

    @Test
    fun `loadNearestStore uses device location and loads nearest store`() =
        runTest {

            val location = DeviceLocation(
                latitude = 4.7110,
                longitude = -74.0721
            )

            val expectedStore = NearbyStore(
                id = "store-1",
                name = "Test Store",
                address = "Bogota",
                latitude = 4.7120,
                longitude = -74.0710,
                categoryIds = listOf("clothes"),
                websiteUrl = "https://example.com",
                imageUrl = "",
                distanceKm = 0.8
            )

            val nearbyRepository =
                FakeNearbyStoreRepository(
                    store = expectedStore
                )

            val controller = NearbyStoreController(
                locationRepository =
                    FakeLocationRepository(location = location),
                nearbyStoreRepository =
                    nearbyRepository
            )

            assertEquals(
                expectedStore,
                controller.loadNearestStore()
            )

            assertEquals(
                location.latitude,
                nearbyRepository.lastLatitude
            )

            assertEquals(
                location.longitude,
                nearbyRepository.lastLongitude
            )
        }

    @Test
    fun `loadNearestStore allows a successful response with no store`() =
        runTest {

            val nearbyRepository =
                FakeNearbyStoreRepository(
                    store = null
                )

            val controller = NearbyStoreController(
                locationRepository =
                    FakeLocationRepository(),
                nearbyStoreRepository =
                    nearbyRepository
            )

            assertNull(controller.loadNearestStore())

            assertTrue(
                nearbyRepository.lastLatitude != null
            )

            assertTrue(
                nearbyRepository.lastLongitude != null
            )
        }

    @Test
    fun `location error is reported and store repository is not called`() =
        runTest {

            val nearbyRepository =
                FakeNearbyStoreRepository()

            val controller = NearbyStoreController(
                locationRepository =
                    FakeLocationRepository(
                        error = IllegalStateException(
                            "GPS unavailable"
                        )
                    ),
                nearbyStoreRepository =
                    nearbyRepository
            )

            try {
                controller.loadNearestStore()
                fail("Expected the location error")
            } catch (error: IllegalStateException) {
                assertEquals(
                    "GPS unavailable",
                    error.message
                )
            }

            assertNull(
                nearbyRepository.lastLatitude
            )

            assertNull(
                nearbyRepository.lastLongitude
            )
        }
}

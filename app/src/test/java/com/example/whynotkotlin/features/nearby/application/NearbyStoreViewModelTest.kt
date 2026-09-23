package com.example.whynotkotlin.features.nearby.application

import com.example.whynotkotlin.features.nearby.domain.DeviceLocation
import com.example.whynotkotlin.features.nearby.domain.NearbyStore
import com.example.whynotkotlin.features.nearby.fakes.FakeLocationRepository
import com.example.whynotkotlin.features.nearby.fakes.FakeNearbyStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyStoreViewModelTest {

    private lateinit var dispatcher: TestDispatcher

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNearestStore uses device location and loads nearest store`() =
        runTest(dispatcher) {

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

            val locationRepository =
                FakeLocationRepository(
                    location = location
                )

            val nearbyRepository =
                FakeNearbyStoreRepository(
                    store = expectedStore
                )

            val viewModel = NearbyStoreViewModel(
                locationRepository =
                    locationRepository,
                nearbyStoreRepository =
                    nearbyRepository
            )

            viewModel.loadNearestStore()

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertNull(state.errorMessage)
            assertEquals(
                expectedStore,
                state.store
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
        runTest(dispatcher) {

            val locationRepository =
                FakeLocationRepository()

            val nearbyRepository =
                FakeNearbyStoreRepository(
                    store = null
                )

            val viewModel = NearbyStoreViewModel(
                locationRepository =
                    locationRepository,
                nearbyStoreRepository =
                    nearbyRepository
            )

            viewModel.loadNearestStore()

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertNull(state.store)
            assertNull(state.errorMessage)

            assertTrue(
                nearbyRepository.lastLatitude != null
            )

            assertTrue(
                nearbyRepository.lastLongitude != null
            )
        }

    @Test
    fun `location error is exposed in UI state and store repository is not called`() =
        runTest(dispatcher) {

            val locationRepository =
                FakeLocationRepository(
                    error = IllegalStateException(
                        "GPS unavailable"
                    )
                )

            val nearbyRepository =
                FakeNearbyStoreRepository()

            val viewModel = NearbyStoreViewModel(
                locationRepository =
                    locationRepository,
                nearbyStoreRepository =
                    nearbyRepository
            )

            viewModel.loadNearestStore()

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertNull(state.store)

            assertEquals(
                "GPS unavailable",
                state.errorMessage
            )

            assertNull(
                nearbyRepository.lastLatitude
            )

            assertNull(
                nearbyRepository.lastLongitude
            )
        }
}
package com.example.whynotkotlin.features.nearby.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.nearby.domain.LocationRepository
import com.example.whynotkotlin.features.nearby.domain.NearbyStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates the Context-Aware nearby-store flow.
 *
 * IMPORTANT:
 * This ViewModel only depends on interfaces.
 *
 * During Juan Felipe's development it is tested with fake repositories located
 * exclusively under src/test.
 *
 * Miguel will later provide the real Android location implementation.
 * This ViewModel should not need to change when that happens.
 */
class NearbyStoreViewModel(
    private val locationRepository: LocationRepository,
    private val nearbyStoreRepository: NearbyStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NearbyStoreUiState())

    val state: StateFlow<NearbyStoreUiState> =
        _state.asStateFlow()

    fun loadNearestStore() {
        if (_state.value.loading) return

        viewModelScope.launch {
            _state.value = NearbyStoreUiState(
                loading = true
            )

            try {
                val location =
                    locationRepository.getCurrentLocation()

                val store =
                    nearbyStoreRepository.getNearestStore(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )

                _state.value = NearbyStoreUiState(
                    store = store,
                    loading = false,
                    errorMessage = null
                )

            } catch (exception: Exception) {
                _state.value = NearbyStoreUiState(
                    store = null,
                    loading = false,
                    errorMessage = exception.message
                        ?: "Could not load nearby stores."
                )
            }
        }
    }

    fun retry() {
        loadNearestStore()
    }

    fun clearError() {
        _state.value = _state.value.copy(
            errorMessage = null
        )
    }
}
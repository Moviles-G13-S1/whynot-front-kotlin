package com.example.whynotkotlin.features.nearby.application

import com.example.whynotkotlin.features.nearby.domain.NearbyStore

data class NearbyStoreUiState(
    val store: NearbyStore? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null
)

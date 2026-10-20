package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.NearbyStore

data class NearbyStoreUiState(
    val store: NearbyStore? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null
)

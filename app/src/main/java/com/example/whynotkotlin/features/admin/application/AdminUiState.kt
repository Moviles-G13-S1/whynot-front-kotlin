package com.example.whynotkotlin.features.admin.application

import com.example.whynotkotlin.features.admin.domain.SavedProductsStats

data class AdminUiState(
    val savedProductsStats: SavedProductsStats = SavedProductsStats(),
    val recommendedProductsSaved: Long = 0,
    val loading: Boolean = true,
    val errorMessage: String? = null
)
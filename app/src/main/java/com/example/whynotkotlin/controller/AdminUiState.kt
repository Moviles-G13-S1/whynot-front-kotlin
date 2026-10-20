package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.SavedProductsStats

data class AdminUiState(
    val savedProductsStats: SavedProductsStats = SavedProductsStats(),
    val recommendedProductsSaved: Long = 0,
    val loading: Boolean = true,
    val errorMessage: String? = null
)
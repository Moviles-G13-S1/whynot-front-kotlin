package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ProductRecommendation

data class RecommendationUiState(
    val recommendation: ProductRecommendation? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)
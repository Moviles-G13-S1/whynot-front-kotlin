package com.example.whynotkotlin.features.recommendations.application

import com.example.whynotkotlin.features.recommendations.domain.ProductRecommendation

data class RecommendationUiState(
    val recommendation: ProductRecommendation? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)
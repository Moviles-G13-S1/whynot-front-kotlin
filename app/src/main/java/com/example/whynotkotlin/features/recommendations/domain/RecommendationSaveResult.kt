package com.example.whynotkotlin.features.recommendations.domain

data class RecommendationSaveResult(
    val saved: Boolean,
    val alreadySaved: Boolean,
    val productId: String? = null
)
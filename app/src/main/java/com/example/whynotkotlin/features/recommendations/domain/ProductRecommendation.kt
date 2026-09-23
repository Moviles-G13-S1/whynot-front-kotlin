package com.example.whynotkotlin.features.recommendations.domain

data class ProductRecommendation(
    val recommendationEventId: String,
    val name: String,
    val brand: String,
    val price: Double,
    val imageUrl: String,
    val productUrl: String,
    val categoryId: String,
    val reason: String
)
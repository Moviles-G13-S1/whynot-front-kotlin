package com.example.whynotkotlin.model

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
package com.example.whynotkotlin.features.recommendations.domain

interface RecommendationRepository {

    suspend fun getRecommendation(): ProductRecommendation?

    suspend fun saveRecommendedProduct(
        recommendationEventId: String,
        wishlistId: String
    ): RecommendationSaveResult
}
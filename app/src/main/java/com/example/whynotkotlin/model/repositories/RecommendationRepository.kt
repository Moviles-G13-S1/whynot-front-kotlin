package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.RecommendationSaveResult

interface RecommendationRepository {

    suspend fun getRecommendation(): ProductRecommendation?

    suspend fun saveRecommendedProduct(
        recommendationEventId: String,
        wishlistId: String
    ): RecommendationSaveResult
}
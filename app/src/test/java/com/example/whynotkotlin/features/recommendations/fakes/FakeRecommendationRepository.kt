package com.example.whynotkotlin.features.recommendations.fakes

import com.example.whynotkotlin.features.recommendations.domain.ProductRecommendation
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import com.example.whynotkotlin.features.recommendations.domain.RecommendationSaveResult

class FakeRecommendationRepository(
    var recommendation: ProductRecommendation? =
        ProductRecommendation(
            recommendationEventId = "fake-event-1",
            name = "Fake Product",
            brand = "Fake Brand",
            price = 99.99,
            imageUrl = "",
            productUrl = "https://example.com",
            categoryId = "tech",
            reason = "Recommended from similar users"
        ),

    var saveResult: RecommendationSaveResult =
        RecommendationSaveResult(
            saved = true,
            alreadySaved = false,
            productId = "fake-product-1"
        ),

    var loadError: Exception? = null,
    var saveError: Exception? = null
) : RecommendationRepository {
    var lastRecommendationEventId: String? = null
    var lastWishlistId: String? = null

    override suspend fun getRecommendation(): ProductRecommendation? {
        loadError?.let { throw it }
        return recommendation
    }

    override suspend fun saveRecommendedProduct(
        recommendationEventId: String,
        wishlistId: String
    ): RecommendationSaveResult {
        saveError?.let { throw it }

        lastRecommendationEventId = recommendationEventId
        lastWishlistId = wishlistId

        return saveResult
    }
}
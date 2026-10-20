package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.repositories.RecommendationRepository

/**
 * Controls the recommendation flow presented to the user.
 *
 * This controller contains no fake data. It can be tested with
 * FakeRecommendationRepository, located exclusively under src/test.
 */
class RecommendationController(
    private val repository: RecommendationRepository
) {

    suspend fun loadRecommendation(): ProductRecommendation? =
        repository.getRecommendation()

    /**
     * Saves [recommendation] into a wishlist and tells whether it ended up saved.
     *
     * recommendationEventId is preserved because the backend uses it
     * to identify that the saved product originally came from the
     * recommendation feature. A product saved earlier counts as saved.
     */
    suspend fun saveRecommendation(
        recommendation: ProductRecommendation,
        wishlistId: String
    ): Boolean {
        val result = repository.saveRecommendedProduct(
            recommendationEventId = recommendation.recommendationEventId,
            wishlistId = wishlistId
        )

        return result.saved || result.alreadySaved
    }
}

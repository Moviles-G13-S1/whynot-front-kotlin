package com.example.whynotkotlin.features.recommendations.data

import com.example.whynotkotlin.features.recommendations.domain.ProductRecommendation
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import com.example.whynotkotlin.features.recommendations.domain.RecommendationSaveResult
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

private const val GET_RECOMMENDATION = "get_recommendation"
private const val SAVE_RECOMMENDED_PRODUCT = "save_recommended_product"

/**
 * Calls the shared callable Cloud Functions that own the Smart Recommendation
 * flow.
 *
 * The similarity algorithm and the BQ3 counter live in the backend: this class
 * only carries values across the boundary. Nothing here decides what to
 * recommend or what counts as a recommended save.
 */
class FirebaseRecommendationRepository(
    private val functions: FirebaseFunctions
) : RecommendationRepository {

    /**
     * Returns the single recommendation the backend picked, or null when the
     * most similar user has nothing new to offer.
     */
    override suspend fun getRecommendation(): ProductRecommendation? {
        val result = functions
            .getHttpsCallable(GET_RECOMMENDATION)
            .call()
            .await()

        val response = result.getData() as? Map<*, *>
            ?: throw IllegalStateException("Invalid recommendation response.")

        // The backend answers with an explicit null recommendation plus a
        // message when there is no candidate, which is not an error.
        val rawRecommendation = response["recommendation"]
            ?: return null

        val recommendation = rawRecommendation as? Map<*, *>
            ?: throw IllegalStateException("Invalid recommendation payload.")

        return ProductRecommendation(
            recommendationEventId = recommendation["recommendationEventId"] as? String
                ?: throw IllegalStateException("Recommendation has no event id."),

            name = recommendation["name"] as? String ?: "",
            brand = recommendation["brand"] as? String ?: "",
            price = (recommendation["price"] as? Number)?.toDouble() ?: 0.0,
            imageUrl = recommendation["imageUrl"] as? String ?: "",
            productUrl = recommendation["productUrl"] as? String ?: "",
            categoryId = recommendation["categoryId"] as? String ?: "",
            reason = recommendation["reason"] as? String ?: ""
        )
    }

    /**
     * Saves a recommended product into one of the user's wishlists.
     *
     * [recommendationEventId] is the id the backend handed out with the
     * recommendation; it proves the recommendation was really shown to this
     * user and makes the call idempotent, so a second tap returns the same
     * product with `alreadySaved` set instead of creating a duplicate or
     * counting twice.
     */
    override suspend fun saveRecommendedProduct(
        recommendationEventId: String,
        wishlistId: String
    ): RecommendationSaveResult {
        val result = functions
            .getHttpsCallable(SAVE_RECOMMENDED_PRODUCT)
            .call(
                mapOf(
                    "recommendationEventId" to recommendationEventId,
                    "wishlistId" to wishlistId
                )
            )
            .await()

        val response = result.getData() as? Map<*, *>
            ?: throw IllegalStateException("Invalid recommendation save response.")

        return RecommendationSaveResult(
            saved = response["saved"] as? Boolean == true,
            alreadySaved = response["alreadySaved"] as? Boolean == true,
            productId = response["productId"] as? String
        )
    }
}

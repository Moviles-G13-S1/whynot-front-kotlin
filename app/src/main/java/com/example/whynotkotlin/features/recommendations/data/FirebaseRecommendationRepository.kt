package com.example.whynotkotlin.features.recommendations.data

import com.example.whynotkotlin.features.recommendations.domain.ProductRecommendation
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import com.example.whynotkotlin.features.recommendations.domain.RecommendationSaveResult
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

/**
 * Real Firebase implementation of RecommendationRepository.
 *
 * It communicates with the shared backend used by both mobile clients.
 *
 * get_recommendation:
 * obtains one personalized recommendation and its recommendationEventId.
 *
 * save_recommended_product:
 * saves the recommended product and updates BQ3 in the backend.
 */
class FirebaseRecommendationRepository(
    private val functions: FirebaseFunctions
) : RecommendationRepository {

    override suspend fun getRecommendation(): ProductRecommendation? {

        val result = functions
            .getHttpsCallable("get_recommendation")
            .call()
            .await()

        val response = result.getData() as? Map<*, *>
            ?: throw IllegalStateException(
                "Invalid recommendation response."
            )

        val rawRecommendation =
            response["recommendation"]
                ?: return null

        val recommendation =
            rawRecommendation as? Map<*, *>
                ?: throw IllegalStateException(
                    "Invalid recommendation data."
                )

        val recommendationEventId =
            recommendation["recommendationEventId"]
                    as? String
                ?: throw IllegalStateException(
                    "Recommendation event ID is missing."
                )

        val name =
            recommendation["name"]
                    as? String
                ?: throw IllegalStateException(
                    "Recommendation name is missing."
                )

        val brand =
            recommendation["brand"]
                    as? String
                ?: throw IllegalStateException(
                    "Recommendation brand is missing."
                )

        val price =
            (recommendation["price"] as? Number)
                ?.toDouble()
                ?: throw IllegalStateException(
                    "Recommendation price is missing."
                )

        val imageUrl =
            recommendation["imageUrl"]
                    as? String
                ?: ""

        val productUrl =
            recommendation["productUrl"]
                    as? String
                ?: ""

        val categoryId =
            recommendation["categoryId"]
                    as? String
                ?: throw IllegalStateException(
                    "Recommendation category is missing."
                )

        val reason =
            recommendation["reason"]
                    as? String
                ?: ""

        return ProductRecommendation(
            recommendationEventId =
                recommendationEventId,

            name = name,

            brand = brand,

            price = price,

            imageUrl = imageUrl,

            productUrl = productUrl,

            categoryId = categoryId,

            reason = reason
        )
    }

    override suspend fun saveRecommendedProduct(
        recommendationEventId: String,
        wishlistId: String
    ): RecommendationSaveResult {

        val result = functions
            .getHttpsCallable(
                "save_recommended_product"
            )
            .call(
                mapOf(
                    "recommendationEventId" to
                            recommendationEventId,

                    "wishlistId" to
                            wishlistId
                )
            )
            .await()

        val response =
            result.getData() as? Map<*, *>
                ?: throw IllegalStateException(
                    "Invalid recommendation save response."
                )

        val saved =
            response["saved"] as? Boolean
                ?: throw IllegalStateException(
                    "Save response is missing saved."
                )

        val alreadySaved =
            response["alreadySaved"] as? Boolean
                ?: false

        val productId =
            response["productId"] as? String

        return RecommendationSaveResult(
            saved = saved,
            alreadySaved = alreadySaved,
            productId = productId
        )
    }
}
package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.SavedProductsBucket
import com.example.whynotkotlin.model.SavedProductsOverview
import com.example.whynotkotlin.model.SavedProductsStats
import com.example.whynotkotlin.model.repositories.AdminRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/** BQ1 (saved-products distribution) and BQ3 (saves from recommendations). */
class AdminSavedProductsController(
    repository: AdminRepository,
    scope: CoroutineScope
) {

    val overview: StateFlow<Result<SavedProductsOverview>?> =
        combine(
            repository.observeAllProducts(),
            repository.observeRecommendedProductSaves()
        ) { products, recommendedProductsSaved ->
            SavedProductsOverview(
                savedProductsStats = calculateSavedProductsStats(products),
                recommendedProductsSaved = recommendedProductsSaved
            )
        }
            .asResults()
            .shareIn(scope)

    private fun calculateSavedProductsStats(
        products: List<Product>
    ): SavedProductsStats {
        val productsByUser = products
            .groupingBy { product ->
                product.ownerId
            }
            .eachCount()

        val activeUsers = productsByUser.size
        val totalProducts = products.size

        val singleProductUsers = productsByUser
            .values
            .count { count ->
                count == 1
            }

        val multipleProductUsers =
            activeUsers - singleProductUsers

        val averagePerActiveUser =
            if (activeUsers == 0) {
                0.0
            } else {
                totalProducts.toDouble() / activeUsers
            }

        val distribution = listOf(
            SavedProductsBucket(
                label = "1–5",
                userCount = productsByUser.values.count {
                    it in 1..5
                }
            ),
            SavedProductsBucket(
                label = "6–10",
                userCount = productsByUser.values.count {
                    it in 6..10
                }
            ),
            SavedProductsBucket(
                label = "11–20",
                userCount = productsByUser.values.count {
                    it in 11..20
                }
            ),
            SavedProductsBucket(
                label = "21–50",
                userCount = productsByUser.values.count {
                    it in 21..50
                }
            ),
            SavedProductsBucket(
                label = "51+",
                userCount = productsByUser.values.count {
                    it >= 51
                }
            )
        )

        return SavedProductsStats(
            totalProducts = totalProducts,
            activeUsers = activeUsers,
            averagePerActiveUser = averagePerActiveUser,
            singleProductUsers = singleProductUsers,
            multipleProductUsers = multipleProductUsers,
            distribution = distribution
        )
    }
}
package com.example.whynotkotlin.features.admin.domain

/**
 * One range shown in the BQ1 saved-products distribution.
 */
data class SavedProductsBucket(
    val label: String,
    val userCount: Int
)

/**
 * BQ1:
 * How many products has a user saved?
 *
 * Users with zero products are intentionally excluded here because
 * that case belongs to BQ5.
 */
data class SavedProductsStats(
    val totalProducts: Int = 0,
    val activeUsers: Int = 0,
    val averagePerActiveUser: Double = 0.0,
    val singleProductUsers: Int = 0,
    val multipleProductUsers: Int = 0,
    val distribution: List<SavedProductsBucket> = emptyList()
)
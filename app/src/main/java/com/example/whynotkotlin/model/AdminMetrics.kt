package com.example.whynotkotlin.model

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

/**
 * BQ1 together with BQ3: the saved-products distribution and how many saved
 * products came from a recommendation.
 */
data class SavedProductsOverview(
    val savedProductsStats: SavedProductsStats = SavedProductsStats(),
    val recommendedProductsSaved: Long = 0
)

/**
 * BQ2 and BQ5: how many registered users saved two or more products, and how
 * many saved none.
 */
data class UserActivityMetrics(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val repeatSaverUsers: Int = 0,
    val repeatSaverPercentage: Double = 0.0,
    val zeroProductUsers: Int = 0,
    val zeroProductPercentage: Double = 0.0
)

/** BQ4 and the demographic profile, both computed from every product and user. */
data class AdminInsights(
    val purchasedProductsStats: PurchasedProductsStats = PurchasedProductsStats(),
    val demographicProfileStats: DemographicProfileStats = DemographicProfileStats()
)

/** Every product and every user profile, as read by the admin dashboard. */
data class AdminDataset(
    val products: List<Product> = emptyList(),
    val users: List<UserProfile> = emptyList()
)

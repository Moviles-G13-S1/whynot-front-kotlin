package com.example.whynotkotlin.features.admin.domain

/** Purchases of one category inside one month. */
data class CategoryPurchaseCount(
    val categoryId: String,
    val purchases: Int
)

/**
 * One calendar month of purchases, already resolved to its winning category.
 */
data class PurchaseMonth(
    val year: Int,
    val month: Int,
    val label: String,
    val totalPurchases: Int,
    val topCategoryId: String?,
    val topCategoryPurchases: Int,
    val byCategory: List<CategoryPurchaseCount>
)

/**
 * BQ4:
 * Which category has the highest number of products marked as purchased
 * per month?
 *
 * Counted from product documents that currently exist and carry
 * `purchased = true` with a `purchasedAt` timestamp. Deleting a product removes
 * it from these figures, which is the same rule every other product metric in
 * this project follows.
 *
 * Products purchased before the backend introduced `purchasedAt` have no month
 * and are reported in [undatedPurchases] instead of being silently dropped.
 */
data class PurchasedProductsStats(
    val months: List<PurchaseMonth> = emptyList(),
    val totalPurchases: Int = 0,
    val undatedPurchases: Int = 0,
    val overallTopCategoryId: String? = null,
    val monthsWindow: Int = DEFAULT_MONTHS_WINDOW
) {
    companion object {
        const val DEFAULT_MONTHS_WINDOW = 6

        /** The windows the screen offers, matching the Flutter client. */
        val WINDOW_OPTIONS = listOf(6, 12, 24)
    }
}

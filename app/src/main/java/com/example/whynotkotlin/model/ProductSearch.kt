package com.example.whynotkotlin.model

/**
 * Sprint 4 feature (Miguel): Global Product Search across every wishlist.
 *
 * [SearchFilters] is exactly what the user typed, kept as text so the form can
 * be restored as it was (local storage) even when a price is still incomplete.
 * [SearchCriteria] is its validated form, the key of the results cache.
 */
data class SearchFilters(
    val query: String = "",
    val categoryId: String = "",
    val minPrice: String = "",
    val maxPrice: String = ""
) {
    fun isEmpty(): Boolean =
        query.isBlank() && categoryId.isBlank() && minPrice.isBlank() && maxPrice.isBlank()
}

/** Validated filters. Two searches with the same criteria give the same results. */
data class SearchCriteria(
    val query: String,
    val categoryId: String?,
    val minPrice: Double?,
    val maxPrice: Double?
)

/** Where the products being searched came from (eventual connectivity). */
enum class SearchSource {
    /** The live Firestore listener while online. */
    SERVER,

    /** The copy kept on the phone: Room, or Firestore's own offline cache. */
    PHONE
}

/** The products a search runs over, and where they came from. */
data class SearchSnapshot(
    val products: List<Product>,
    val source: SearchSource,
    /** When this copy was last confirmed by the server; null if never. */
    val syncedAtMillis: Long?,
    /** The live listener failed; the snapshot is whatever the phone had. */
    val loadFailed: Boolean = false
)

/** The products kept on the phone for one user, and when they were saved. */
data class LocalSearchSnapshot(
    val products: List<Product>,
    val syncedAtMillis: Long
)

/** Everything the search screen draws. */
data class SearchState(
    val loading: Boolean = true,
    val results: List<Product> = emptyList(),
    val totalProducts: Int = 0,
    val source: SearchSource? = null,
    val syncedAtMillis: Long? = null,
    val invalidPriceRange: Boolean = false,
    val loadFailed: Boolean = false,
    val signedOut: Boolean = false
) {
    /** Some result still has a change waiting for the server (a purchase marked offline). */
    val hasPendingChanges: Boolean get() = results.any { it.pendingSync }
}

/** The search itself: pure functions, so they can run on any thread and be tested alone. */
object ProductSearch {

    /** [filters] as criteria, or null when the price range is not valid. */
    fun criteria(filters: SearchFilters): SearchCriteria? {
        val min = priceOrNull(filters.minPrice) ?: return null
        val max = priceOrNull(filters.maxPrice) ?: return null
        val minValue = min.value
        val maxValue = max.value
        if (minValue != null && maxValue != null && minValue > maxValue) return null

        return SearchCriteria(
            query = filters.query.trim().lowercase(),
            categoryId = filters.categoryId.ifBlank { null },
            minPrice = minValue,
            maxPrice = maxValue
        )
    }

    /** Products whose name or brand contains the query and that pass every filter, by name. */
    fun filter(products: List<Product>, criteria: SearchCriteria): List<Product> =
        products
            .filter { product ->
                val matchesText = criteria.query.isEmpty() ||
                    product.name.lowercase().contains(criteria.query) ||
                    product.brand.lowercase().contains(criteria.query)

                matchesText &&
                    (criteria.categoryId == null || product.categoryId == criteria.categoryId) &&
                    (criteria.minPrice == null || product.price >= criteria.minPrice) &&
                    (criteria.maxPrice == null || product.price <= criteria.maxPrice)
            }
            .sortedBy { it.name.lowercase() }

    /** A blank field is "no limit"; anything else must be a valid price (0 allowed). */
    private fun priceOrNull(text: String): PriceFilter? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return PriceFilter(null)
        if (trimmed == "0") return PriceFilter(0.0)
        return PriceRules.parse(trimmed)?.let { PriceFilter(it) }
    }

    private class PriceFilter(val value: Double?)
}

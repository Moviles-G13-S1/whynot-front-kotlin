package com.example.whynotkotlin.model

import java.util.Date

/**
 * A `wishlists/{wishlistId}` document.
 *
 * The backend denies updates and deletes until cascade behaviour is defined, so
 * there is no update draft for wishlists.
 */
data class Wishlist(
    val id: String,
    val ownerId: String,
    val categoryId: String,
    val imageUrl: String,
    val createdAt: Date?,
    val updatedAt: Date?
)

/** The fields a client supplies when creating a wishlist. */
data class WishlistDraft(
    val categoryId: String,
    val imageUrl: String = ""
)

/** A wishlist plus the data the list screens render next to it. */
data class WishlistSummary(
    val wishlist: Wishlist,
    val categoryName: String,
    val productCount: Int
)

/** Every wishlist of the signed-in user, plus the category catalog. */
data class WishlistCatalog(
    val summaries: List<WishlistSummary> = emptyList(),
    val categories: List<Category> = emptyList()
) {
    fun find(wishlistId: String?): WishlistSummary? =
        summaries.firstOrNull { it.wishlist.id == wishlistId }

    /**
     * Categories that do not have a wishlist yet.
     *
     * One wishlist per category is a UI behaviour, not a backend invariant:
     * auto-generated wishlist ids make the check impossible to enforce
     * atomically in Security Rules.
     */
    val availableCategories: List<Category>
        get() {
            val used = summaries.map { it.wishlist.categoryId }.toSet()
            return categories.filterNot { it.id in used }
        }
}

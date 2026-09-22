package com.example.whynotkotlin.features.wishlists.domain

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

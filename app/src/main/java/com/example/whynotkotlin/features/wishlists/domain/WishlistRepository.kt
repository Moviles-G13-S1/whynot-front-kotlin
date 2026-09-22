package com.example.whynotkotlin.features.wishlists.domain

import kotlinx.coroutines.flow.Flow

/**
 * Categories and the signed-in user's wishlists.
 *
 * Categories live here rather than in their own contract to match the Flutter
 * client, so both frontends expose the same four repositories.
 */
interface WishlistRepository {

    suspend fun getCategories(): List<Category>

    fun observeCategories(): Flow<List<Category>>

    /** Emits the wishlists owned by [ownerId], newest first. */
    fun observeWishlists(ownerId: String): Flow<List<Wishlist>>

    suspend fun getWishlist(wishlistId: String): Wishlist?

    /** Creates a wishlist owned by [ownerId] and returns its generated id. */
    suspend fun createWishlist(ownerId: String, draft: WishlistDraft): String
}

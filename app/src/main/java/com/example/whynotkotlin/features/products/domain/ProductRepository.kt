package com.example.whynotkotlin.features.products.domain

import kotlinx.coroutines.flow.Flow

/** Typed access to the signed-in user's `products` documents. */
interface ProductRepository {

    /** Emits every product owned by [ownerId], newest first. */
    fun observeProducts(ownerId: String): Flow<List<Product>>

    /** Emits the products of one wishlist owned by [ownerId], newest first. */
    fun observeWishlistProducts(ownerId: String, wishlistId: String): Flow<List<Product>>

    fun observeProduct(productId: String): Flow<Product?>

    suspend fun getProduct(productId: String): Product?

    /** Creates a product owned by [ownerId] and returns its generated id. */
    suspend fun createProduct(ownerId: String, draft: ProductDraft): String

    suspend fun updateProduct(productId: String, update: ProductUpdate)

    suspend fun setPurchased(productId: String, purchased: Boolean)

    suspend fun deleteProduct(productId: String)
}

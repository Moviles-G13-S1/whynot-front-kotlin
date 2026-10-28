package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.ProductDraft
import com.example.whynotkotlin.model.ProductSaveFailure
import com.example.whynotkotlin.model.ProductUpdate

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

    /**
     * Marks a saved product as purchased, stamping `purchasedAt` with the
     * server time.
     *
     * Purchasing is one-way: the rules reject going back to saved, so there is
     * no counterpart. Deleting the product is the only way to undo it.
     */
    suspend fun markPurchased(productId: String)

    suspend fun deleteProduct(productId: String)

    /**
     * Records one attempt by [ownerId] to save a product: a success when
     * [failure] is null, otherwise the reason it failed.
     */
    suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?)

    /**
     * Records what [ownerId] actually paid for [product] when marking it as
     * purchased, next to the price they had saved (Sprint 3 BQ: money saved).
     */
    suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double)
}

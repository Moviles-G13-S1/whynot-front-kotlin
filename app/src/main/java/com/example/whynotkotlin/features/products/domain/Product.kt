package com.example.whynotkotlin.features.products.domain

import java.util.Date

/**
 * A `products/{productId}` document.
 *
 * `saveMethod` is still deferred by the backend and is deliberately absent.
 *
 * [purchasedAt] is null while the product is saved and holds the server time of
 * the purchase once it is marked. The rules tie the two fields together, so
 * they are never written apart.
 */
data class Product(
    val id: String,
    val ownerId: String,
    val wishlistId: String,
    val categoryId: String,
    val name: String,
    val brand: String,
    val price: Double,
    val imageUrl: String,
    val productUrl: String,
    val purchased: Boolean,
    val purchasedAt: Date?,
    val createdAt: Date?,
    val updatedAt: Date?
)

/**
 * The fields a client supplies when creating a product.
 *
 * [categoryId] is not included: the rules require it to match the destination
 * wishlist, so the repository resolves it from [wishlistId] instead of trusting
 * the caller.
 */
data class ProductDraft(
    val wishlistId: String,
    val name: String,
    val brand: String,
    val price: Double,
    val imageUrl: String = "",
    val productUrl: String = ""
)

/**
 * The fields a client may change on an existing product.
 *
 * Only non-null fields are written. Moving a product to another wishlist also
 * rewrites `categoryId`, which the repository resolves from the destination.
 *
 * `purchased` is not here on purpose: the rules only accept it together with
 * `purchasedAt`, and only in the saved-to-purchased direction, so it goes
 * through [ProductRepository.markPurchased] instead.
 */
data class ProductUpdate(
    val wishlistId: String? = null,
    val name: String? = null,
    val brand: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val productUrl: String? = null
)

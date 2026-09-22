package com.example.whynotkotlin.features.products.domain

import java.util.Date

/**
 * A `products/{productId}` document.
 *
 * `saveMethod` and `purchasedAt` belong to backend Phase 4 and are rejected by
 * the current rules, so they are deliberately absent here.
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
 */
data class ProductUpdate(
    val wishlistId: String? = null,
    val name: String? = null,
    val brand: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val productUrl: String? = null,
    val purchased: Boolean? = null
)

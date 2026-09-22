package com.example.whynotkotlin.features.wishlists.domain

/**
 * A `categories/{categoryId}` document.
 *
 * Categories are seeded by the backend and are read-only for clients. The id is
 * the stable identifier stored in `preferredCategoryId`, `wishlists.categoryId`
 * and `products.categoryId`; [name] is only for display.
 */
data class Category(
    val id: String,
    val name: String
)

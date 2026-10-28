package com.example.whynotkotlin.model

/**
 * What the user has typed in the New Product form and not saved yet.
 *
 * It lives only on this phone (local storage) and is never a product: nothing
 * reaches Firestore until the user taps Save Item and the backend confirms the
 * write. Every field is kept as typed, so an unfinished price like "12," is
 * restored exactly as it was left.
 */
data class ProductFormDraft(
    val wishlistId: String = "",
    val name: String = "",
    val brand: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val productUrl: String = ""
) {
    /** True when there is nothing worth restoring. */
    fun isBlank(): Boolean =
        wishlistId.isBlank() &&
            name.isBlank() &&
            brand.isBlank() &&
            price.isBlank() &&
            imageUrl.isBlank() &&
            productUrl.isBlank()
}

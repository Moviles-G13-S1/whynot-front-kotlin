package com.example.whynotkotlin.features.wishlists.domain

/**
 * The seeded `categories` documents, mirrored from the backend data contract.
 *
 * This list exists only for the sign-up form. The Security Rules allow reading
 * `categories` only when `request.auth != null`, so a visitor filling the
 * registration form cannot query them yet — but the profile it creates already
 * needs a valid `preferredCategoryId`.
 *
 * Every other screen reads the live collection through [WishlistRepository]. If
 * the backend ever reseeds different ids, this list is the one place to update,
 * and profile creation will fail loudly because the rules check that the id
 * references an existing category.
 */
object CanonicalCategories {

    val all = listOf(
        Category("fashion", "Fashion"),
        Category("beauty", "Beauty"),
        Category("technology", "Technology"),
        Category("home", "Home"),
        Category("accessories", "Accessories"),
        Category("travel", "Travel"),
        Category("gifts", "Gifts"),
        Category("other", "Other")
    )
}

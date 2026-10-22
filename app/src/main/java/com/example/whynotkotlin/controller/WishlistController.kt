package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.WishlistCatalog
import com.example.whynotkotlin.model.WishlistDraft
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Wishlist intents.
 *
 * This also reads products, because a wishlist card shows its product count and
 * that count has no separate backend aggregate until Phase 4.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistController(
    private val authRepository: AuthRepository,
    private val wishlistRepository: WishlistRepository,
    private val productRepository: ProductRepository,
    scope: CoroutineScope
) {

    val catalog: StateFlow<Result<WishlistCatalog>?> =
        authRepository.observeSession()
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(Result.success(WishlistCatalog()))
                } else {
                    combine(
                        wishlistRepository.observeWishlists(user.uid),
                        productRepository.observeProducts(user.uid),
                        wishlistRepository.observeCategories()
                    ) { wishlists, products, categories ->
                        val countsByWishlist = products.groupingBy { it.wishlistId }.eachCount()
                        val namesById = categories.associate { it.id to it.name }

                        WishlistCatalog(
                            summaries = wishlists.map { wishlist ->
                                WishlistSummary(
                                    wishlist = wishlist,
                                    categoryName = namesById[wishlist.categoryId]
                                        ?: wishlist.categoryId,
                                    productCount = countsByWishlist[wishlist.id] ?: 0
                                )
                            },
                            categories = categories
                        )
                    }.asResults()
                }
            }
            .shareIn(scope)

    /** Creates a wishlist and returns its id. */
    suspend fun createWishlist(categoryId: String, imageUrl: String): String {
        val uid = authRepository.currentUser()?.uid
            ?: throw ValidationException("You are not signed in")

        if (categoryId.isBlank()) throw ValidationException("Select a category")

        return wishlistRepository.createWishlist(
            ownerId = uid,
            draft = WishlistDraft(categoryId = categoryId, imageUrl = imageUrl)
        )
    }
}

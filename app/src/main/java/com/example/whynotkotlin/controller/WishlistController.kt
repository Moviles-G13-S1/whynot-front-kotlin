package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.OnboardingStep
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.WishlistCatalog
import com.example.whynotkotlin.model.WishlistDraft
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.OnboardingRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
    private val onboardingRepository: OnboardingRepository,
    private val scope: CoroutineScope
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

    /**
     * Creates a wishlist and returns its id.
     *
     * One wishlist per category is checked here against the user's current
     * wishlists, not only in the form, because other flows (such as saving a
     * recommendation) also create wishlists and the backend cannot enforce it.
     */
    suspend fun createWishlist(categoryId: String, imageUrl: String): String {
        val uid = authRepository.currentUser()?.uid
            ?: throw ValidationException("You are not signed in")

        if (categoryId.isBlank()) throw ValidationException("Select a category")

        val pictureLink = imageUrl.trim()
        if (pictureLink.isNotEmpty() && !pictureLink.isWebLink()) {
            throw ValidationException("The picture link must start with http:// or https://")
        }

        val categories = wishlistRepository.observeCategories().first()
        val category = categories.firstOrNull { it.id == categoryId }
            ?: throw ValidationException("That category is no longer available")

        val existing = wishlistRepository.observeWishlists(uid).first()
        if (existing.any { it.categoryId == categoryId }) {
            throw ValidationException("You already have a ${category.name} wishlist")
        }

        val wishlistId = wishlistRepository.createWishlist(
            ownerId = uid,
            draft = WishlistDraft(categoryId = categoryId, imageUrl = pictureLink)
        )

        if (existing.isEmpty()) {
            recordOnboardingStep(uid, OnboardingStep.FIRST_WISHLIST_CREATED)
        }

        return wishlistId
    }

    /**
     * Records the step in the background. It is analytics only, so it never
     * delays the save and a failure to record it is ignored.
     */
    private fun recordOnboardingStep(userId: String, step: OnboardingStep) {
        scope.launch {
            try {
                onboardingRepository.recordStep(userId, step)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Losing one analytics event must not surface as a save error.
            }
        }
    }
}

private fun String.isWebLink(): Boolean =
    (startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)) &&
        none { it.isWhitespace() }

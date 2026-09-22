package com.example.whynotkotlin.features.wishlists.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.authentication.application.readableMessage
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.products.domain.ProductRepository
import com.example.whynotkotlin.features.wishlists.domain.Category
import com.example.whynotkotlin.features.wishlists.domain.Wishlist
import com.example.whynotkotlin.features.wishlists.domain.WishlistDraft
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
import com.example.whynotkotlin.features.wishlists.domain.WishlistSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class WishlistUiState(
    val summaries: List<WishlistSummary> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedWishlistId: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val errorMessage: String? = null
) {
    val selected: WishlistSummary?
        get() = summaries.firstOrNull { it.wishlist.id == selectedWishlistId }

    /**
     * Categories that do not have a wishlist yet.
     *
     * One wishlist per category is a UI behaviour, not a backend invariant:
     * auto-generated wishlist ids make the check impossible to enforce
     * atomically in Security Rules.
     */
    val availableCategories: List<Category>
        get() {
            val used = summaries.map { it.wishlist.categoryId }.toSet()
            return categories.filterNot { it.id in used }
        }
}

/**
 * Wishlist intents.
 *
 * This also reads products, because a wishlist card shows its product count and
 * that count has no separate backend aggregate until Phase 4.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModel(
    private val authRepository: AuthRepository,
    private val wishlistRepository: WishlistRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WishlistUiState())
    val state: StateFlow<WishlistUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession()
                .flatMapLatest { user ->
                    if (user == null) {
                        flowOf(
                            Triple(
                                emptyList<Wishlist>(),
                                emptyList<Product>(),
                                emptyList<Category>()
                            )
                        )
                    } else {
                        combine(
                            wishlistRepository.observeWishlists(user.uid),
                            productRepository.observeProducts(user.uid),
                            wishlistRepository.observeCategories()
                        ) { wishlists, products, categories ->
                            Triple(wishlists, products, categories)
                        }
                    }
                }
                .catch { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = error.readableMessage()
                    )
                }
                .collect { (wishlists, products, categories) ->
                    val countsByWishlist = products.groupingBy { it.wishlistId }.eachCount()
                    val namesById = categories.associate { it.id to it.name }

                    _state.value = _state.value.copy(
                        summaries = wishlists.map { wishlist ->
                            WishlistSummary(
                                wishlist = wishlist,
                                categoryName = namesById[wishlist.categoryId]
                                    ?: wishlist.categoryId,
                                productCount = countsByWishlist[wishlist.id] ?: 0
                            )
                        },
                        categories = categories,
                        loading = false
                    )
                }
        }
    }

    fun selectWishlist(wishlistId: String) {
        _state.value = _state.value.copy(selectedWishlistId = wishlistId)
    }

    fun createWishlist(categoryId: String, imageUrl: String, onSuccess: () -> Unit) {
        val uid = authRepository.currentUser()?.uid

        if (uid == null) {
            _state.value = _state.value.copy(errorMessage = "You are not signed in")
            return
        }

        if (categoryId.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Select a category")
            return
        }

        _state.value = _state.value.copy(saving = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                wishlistRepository.createWishlist(
                    ownerId = uid,
                    draft = WishlistDraft(categoryId = categoryId, imageUrl = imageUrl)
                )
            }
                .onSuccess {
                    _state.value = _state.value.copy(saving = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        saving = false,
                        errorMessage = error.readableMessage()
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

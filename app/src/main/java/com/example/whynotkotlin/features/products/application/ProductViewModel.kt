package com.example.whynotkotlin.features.products.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.authentication.application.readableMessage
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.products.domain.ProductDraft
import com.example.whynotkotlin.features.products.domain.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val selectedProductId: String? = null,
    /** Set by the "Why Not?" screen so the manual form opens prefilled. */
    val pendingProductUrl: String = "",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val errorMessage: String? = null
) {
    val selected: Product?
        get() = products.firstOrNull { it.id == selectedProductId }

    val purchased: List<Product>
        get() = products.filter { it.purchased }

    fun forWishlist(wishlistId: String): List<Product> =
        products.filter { it.wishlistId == wishlistId }
}

/**
 * Product intents.
 *
 * Every product the user owns is observed once and filtered in memory. Separate
 * per-wishlist listeners would mean one Firestore listener per visited wishlist
 * for no benefit at this data size.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession()
                .flatMapLatest { user ->
                    if (user == null) flowOf(emptyList())
                    else productRepository.observeProducts(user.uid)
                }
                .catch { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = error.readableMessage()
                    )
                }
                .collect { products ->
                    _state.value = _state.value.copy(products = products, loading = false)
                }
        }
    }

    fun selectProduct(productId: String) {
        _state.value = _state.value.copy(selectedProductId = productId)
    }

    fun setPendingProductUrl(url: String) {
        _state.value = _state.value.copy(pendingProductUrl = url)
    }

    fun createProduct(
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String,
        onSuccess: () -> Unit
    ) {
        val uid = authRepository.currentUser()?.uid

        if (uid == null) {
            _state.value = _state.value.copy(errorMessage = "You are not signed in")
            return
        }

        val parsedPrice = price.trim().replace(",", ".").toDoubleOrNull()

        val validationError = when {
            name.isBlank() -> "Enter a product name"
            brand.isBlank() -> "Enter a brand"
            parsedPrice == null -> "Enter a valid price"
            parsedPrice < 0 -> "The price cannot be negative"
            wishlistId.isBlank() -> "Choose a wishlist"
            else -> null
        }

        if (validationError != null) {
            _state.value = _state.value.copy(errorMessage = validationError)
            return
        }

        _state.value = _state.value.copy(saving = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                productRepository.createProduct(
                    ownerId = uid,
                    draft = ProductDraft(
                        wishlistId = wishlistId,
                        name = name,
                        brand = brand,
                        price = parsedPrice!!,
                        imageUrl = imageUrl,
                        productUrl = productUrl
                    )
                )
            }
                .onSuccess {
                    _state.value = _state.value.copy(
                        saving = false,
                        pendingProductUrl = ""
                    )
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

    /**
     * Purchasing is one-way on the backend, so an already purchased product is
     * ignored here instead of sending a write the rules would reject.
     */
    fun markPurchased(product: Product) {
        if (product.purchased) return

        _state.value = _state.value.copy(errorMessage = null)

        viewModelScope.launch {
            runCatching {
                productRepository.markPurchased(product.id)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMessage = error.readableMessage())
            }
        }
    }

    fun deleteProduct(productId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching { productRepository.deleteProduct(productId) }
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        errorMessage = error.readableMessage()
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.ProductDraft
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Product intents.
 *
 * Every product the user owns is observed once and filtered in memory. Separate
 * per-wishlist listeners would mean one Firestore listener per visited wishlist
 * for no benefit at this data size.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductController(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    scope: CoroutineScope
) {

    val products: StateFlow<Result<List<Product>>?> =
        authRepository.observeSession()
            .flatMapLatest { user ->
                if (user == null) flowOf(Result.success(emptyList()))
                else productRepository.observeProducts(user.uid).asResults()
            }
            .shareIn(scope)

    fun find(products: List<Product>, productId: String?): Product? =
        products.firstOrNull { it.id == productId }

    fun purchased(products: List<Product>): List<Product> =
        products.filter { it.purchased }

    fun forWishlist(products: List<Product>, wishlistId: String): List<Product> =
        products.filter { it.wishlistId == wishlistId }

    suspend fun createProduct(
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String
    ) {
        val uid = authRepository.currentUser()?.uid
            ?: throw ValidationException("You are not signed in")

        val parsedPrice = price.trim().replace(",", ".").toDoubleOrNull()

        val validationError = when {
            name.isBlank() -> "Enter a product name"
            brand.isBlank() -> "Enter a brand"
            parsedPrice == null -> "Enter a valid price"
            parsedPrice < 0 -> "The price cannot be negative"
            wishlistId.isBlank() -> "Choose a wishlist"
            else -> null
        }

        validationError?.let { throw ValidationException(it) }

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

    /**
     * Purchasing is one-way on the backend, so an already purchased product is
     * ignored here instead of sending a write the rules would reject.
     */
    suspend fun markPurchased(product: Product) {
        if (product.purchased) return
        productRepository.markPurchased(product.id)
    }

    suspend fun deleteProduct(productId: String) {
        productRepository.deleteProduct(productId)
    }
}

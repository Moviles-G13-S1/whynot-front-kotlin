package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.ProductDraft
import com.example.whynotkotlin.model.ProductSaveFailure
import com.example.whynotkotlin.model.ProductUpdate
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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
    private val scope: CoroutineScope
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

    /**
     * Validates and saves a product, recording the attempt for BQ Type 2: a
     * success once the product is written, or a failure with its reason when
     * validation or the backend rejects it.
     */
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
            name.isBlank() -> ProductSaveFailure.MISSING_NAME to "Enter a product name"
            brand.isBlank() -> ProductSaveFailure.MISSING_BRAND to "Enter a brand"
            parsedPrice == null -> ProductSaveFailure.INVALID_PRICE to "Enter a valid price"
            parsedPrice < 0 -> ProductSaveFailure.NEGATIVE_PRICE to "The price cannot be negative"
            wishlistId.isBlank() -> ProductSaveFailure.MISSING_WISHLIST to "Choose a wishlist"
            else -> null
        }

        validationError?.let { (failure, message) ->
            recordSaveAttempt(uid, failure)
            throw ValidationException(message)
        }

        try {
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
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            recordSaveAttempt(uid, ProductSaveFailure.BACKEND_ERROR)
            throw error
        }

        recordSaveAttempt(uid, failure = null)
    }

    /**
     * Records the attempt in the background. It is analytics only, so it never
     * delays the save and a failure to record it is ignored.
     */
    private fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) {
        scope.launch {
            try {
                productRepository.recordSaveAttempt(ownerId, failure)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Losing one analytics event must not surface as a save error.
            }
        }
    }

    /**
     * Purchasing is one-way on the backend, so an already purchased product is
     * ignored here instead of sending a write the rules would reject.
     */
    suspend fun markPurchased(product: Product) {
        if (product.purchased) return
        productRepository.markPurchased(product.id)
    }

    /** Validates and saves the edits to an existing product. */
    suspend fun updateProduct(
        productId: String,
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String
    ) {
        val parsedPrice = price.trim().replace(",", ".").toDoubleOrNull()

        when {
            name.isBlank() -> throw ValidationException("Enter a product name")
            brand.isBlank() -> throw ValidationException("Enter a brand")
            parsedPrice == null -> throw ValidationException("Enter a valid price")
            parsedPrice < 0 -> throw ValidationException("The price cannot be negative")
            wishlistId.isBlank() -> throw ValidationException("Choose a wishlist")
        }

        productRepository.updateProduct(
            productId,
            ProductUpdate(
                wishlistId = wishlistId,
                name = name,
                brand = brand,
                price = parsedPrice,
                imageUrl = imageUrl,
                productUrl = productUrl
            )
        )
    }

    suspend fun deleteProduct(productId: String) {
        productRepository.deleteProduct(productId)
    }
}

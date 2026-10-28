package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.ProductDraft
import com.example.whynotkotlin.model.ProductFormDraft
import com.example.whynotkotlin.model.ProductSaveFailure
import com.example.whynotkotlin.model.ProductUpdate
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.OfflineException
import com.example.whynotkotlin.model.repositories.ProductDraftRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.requireOnline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 *
 * Offline (eventual connectivity), products keep showing from Firestore's local
 * cache. Creating, editing and deleting need the backend, so they are refused
 * with a clear message while the form keeps what the user typed. Marking a
 * purchase is the one write accepted offline: Firestore queues it and sends it
 * when the connection returns, and the product shows as [Product.pendingSync]
 * until then.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductController(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val scope: CoroutineScope,
    private val draftRepository: ProductDraftRepository? = null,
    private val connectivity: ConnectivityRepository? = null
) {

    /** The pending debounced draft write, replaced on every keystroke. */
    private var draftSaveJob: Job? = null

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

    /** The New Product draft the signed-in user left, if any. */
    suspend fun loadDraft(): ProductFormDraft? {
        val uid = authRepository.currentUser()?.uid ?: return null

        return try {
            draftRepository?.loadDraft(uid)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // A draft that cannot be read is treated as no draft.
            null
        }
    }

    /**
     * Keeps the form on this phone while the user types.
     *
     * Writes are debounced: each call cancels the previous pending write and
     * waits [DRAFT_SAVE_DELAY_MILLIS], so typing a word costs one disk write
     * instead of one per letter. An empty form removes the draft.
     */
    fun saveDraft(draft: ProductFormDraft) {
        val uid = authRepository.currentUser()?.uid ?: return
        val repository = draftRepository ?: return

        draftSaveJob?.cancel()
        draftSaveJob = scope.launch {
            delay(DRAFT_SAVE_DELAY_MILLIS)
            runDraftWrite {
                if (draft.isBlank()) repository.clearDraft(uid)
                else repository.saveDraft(uid, draft)
            }
        }
    }

    /** Throws away the draft, for example when the user starts over. */
    fun discardDraft() {
        val uid = authRepository.currentUser()?.uid ?: return
        clearDraft(uid)
    }

    private fun clearDraft(uid: String) {
        val repository = draftRepository ?: return

        // Cancelled first, so a write still waiting on the debounce cannot
        // bring the draft back after it was cleared.
        draftSaveJob?.cancel()
        draftSaveJob = scope.launch {
            runDraftWrite { repository.clearDraft(uid) }
        }
    }

    private suspend fun runDraftWrite(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Drafts are a convenience: failing to store one must not break the form.
        }
    }

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

        val parsedPrice = price.asPrice()

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

        // Nothing is sent offline: the write would wait silently until the
        // connection returns. The draft already holds the form, so the user
        // can save it from here once back online.
        if (connectivity?.isOnline?.value == false) {
            recordSaveAttempt(uid, ProductSaveFailure.OFFLINE)
            throw OfflineException(SAVE_OFFLINE)
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

        // The product exists in Firestore now, so the local copy is no longer needed.
        clearDraft(uid)
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
     * Marks [product] as purchased and records what the user paid.
     *
     * Purchasing is one-way on the backend, so an already purchased product is
     * ignored here instead of sending a write the rules would reject.
     *
     * Online, the purchase is confirmed before returning. Offline, Firestore
     * applies it to the local cache at once and queues it, so the screen shows
     * the product as purchased and pending sync instead of waiting for a
     * connection. The paid price is recorded in the background in both cases
     * (Sprint 3 BQ: how much money users saved).
     */
    suspend fun markPurchased(product: Product, purchasePrice: String) {
        if (product.purchased) return

        val uid = authRepository.currentUser()?.uid
            ?: throw ValidationException("You are not signed in")

        val paid = purchasePrice.asPrice()
            ?: throw ValidationException("Enter the price you paid")
        if (paid < 0) throw ValidationException("The price cannot be negative")

        if (connectivity?.isOnline?.value == false) {
            scope.launch { runQueuedWrite { productRepository.markPurchased(product.id) } }
        } else {
            productRepository.markPurchased(product.id)
        }

        scope.launch { runQueuedWrite { productRepository.recordPurchase(uid, product, paid) } }
    }

    /** A write that finishes on its own once Firestore reaches the backend. */
    private suspend fun runQueuedWrite(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // The queued write was rejected later; the listener shows the
            // product as it is on the backend again.
        }
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
        val parsedPrice = price.asPrice()

        when {
            name.isBlank() -> throw ValidationException("Enter a product name")
            brand.isBlank() -> throw ValidationException("Enter a brand")
            parsedPrice == null -> throw ValidationException("Enter a valid price")
            parsedPrice < 0 -> throw ValidationException("The price cannot be negative")
            wishlistId.isBlank() -> throw ValidationException("Choose a wishlist")
        }

        connectivity.requireOnline(EDIT_OFFLINE)

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
        connectivity.requireOnline(DELETE_OFFLINE)
        productRepository.deleteProduct(productId)
    }

    private fun String.asPrice(): Double? =
        trim().replace(",", ".").toDoubleOrNull()

    companion object {
        /** How long typing must pause before the draft is written to disk. */
        const val DRAFT_SAVE_DELAY_MILLIS = 400L

        const val SAVE_OFFLINE =
            "You're offline. Your product is kept as a draft on this phone; save it when you're back online."

        const val EDIT_OFFLINE =
            "You're offline. Your changes are still here; save them when you're back online."

        const val DELETE_OFFLINE =
            "You're offline. Connect to the internet to delete this item."
    }
}

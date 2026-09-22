package com.example.whynotkotlin.features.products.data

import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.products.domain.ProductDraft
import com.example.whynotkotlin.features.products.domain.ProductRepository
import com.example.whynotkotlin.features.products.domain.ProductUpdate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val WISHLISTS = "wishlists"
private const val PRODUCTS = "products"

class FirebaseProductRepository(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    override fun observeProducts(ownerId: String): Flow<List<Product>> =
        firestore.collection(PRODUCTS)
            .whereEqualTo("ownerId", ownerId)
            .observeProductList()

    override fun observeWishlistProducts(
        ownerId: String,
        wishlistId: String
    ): Flow<List<Product>> =
        // Two equality filters are served by single-field indexes, so this
        // needs no composite index. Adding an orderBy would.
        firestore.collection(PRODUCTS)
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("wishlistId", wishlistId)
            .observeProductList()

    override fun observeProduct(productId: String): Flow<Product?> = callbackFlow {
        val registration = firestore.collection(PRODUCTS).document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toProduct())
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getProduct(productId: String): Product? =
        firestore.collection(PRODUCTS).document(productId).get().await().toProduct()

    override suspend fun createProduct(ownerId: String, draft: ProductDraft): String {
        // The rules require categoryId to match the destination wishlist, so it
        // is resolved here rather than taken from the caller.
        val categoryId = requireWishlistCategory(draft.wishlistId)
        val document = firestore.collection(PRODUCTS).document()

        val data = mapOf(
            "ownerId" to ownerId,
            "wishlistId" to draft.wishlistId,
            "categoryId" to categoryId,
            "name" to draft.name.trim(),
            "brand" to draft.brand.trim(),
            "price" to draft.price,
            "imageUrl" to draft.imageUrl.trim(),
            "productUrl" to draft.productUrl.trim(),
            "purchased" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        document.set(data).await()
        return document.id
    }

    override suspend fun updateProduct(productId: String, update: ProductUpdate) {
        val data = mutableMapOf<String, Any>()

        update.wishlistId?.let {
            data["wishlistId"] = it
            // Moving a product must rewrite categoryId to match the destination.
            data["categoryId"] = requireWishlistCategory(it)
        }
        update.name?.let { data["name"] = it.trim() }
        update.brand?.let { data["brand"] = it.trim() }
        update.price?.let { data["price"] = it }
        update.imageUrl?.let { data["imageUrl"] = it.trim() }
        update.productUrl?.let { data["productUrl"] = it.trim() }
        update.purchased?.let { data["purchased"] = it }

        if (data.isEmpty()) return

        data["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection(PRODUCTS).document(productId).update(data).await()
    }

    override suspend fun setPurchased(productId: String, purchased: Boolean) {
        updateProduct(productId, ProductUpdate(purchased = purchased))
    }

    override suspend fun deleteProduct(productId: String) {
        firestore.collection(PRODUCTS).document(productId).delete().await()
    }

    private suspend fun requireWishlistCategory(wishlistId: String): String {
        val snapshot = firestore.collection(WISHLISTS).document(wishlistId).get().await()

        require(snapshot.exists()) { "Wishlist $wishlistId does not exist" }

        return snapshot.getString("categoryId")
            ?: error("Wishlist $wishlistId has no categoryId")
    }
}

private fun Query.observeProductList(): Flow<List<Product>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }

        val products = snapshot?.documents
            ?.mapNotNull { it.toProduct() }
            ?.sortedByDescending { it.createdAt }
            .orEmpty()

        trySend(products)
    }

    awaitClose { registration.remove() }
}

private fun DocumentSnapshot.toProduct(): Product? {
    if (!exists()) return null

    return Product(
        id = id,
        ownerId = getString("ownerId").orEmpty(),
        wishlistId = getString("wishlistId").orEmpty(),
        categoryId = getString("categoryId").orEmpty(),
        name = getString("name").orEmpty(),
        brand = getString("brand").orEmpty(),
        price = getDouble("price") ?: 0.0,
        imageUrl = getString("imageUrl").orEmpty(),
        productUrl = getString("productUrl").orEmpty(),
        purchased = getBoolean("purchased") == true,
        createdAt = getTimestamp("createdAt")?.toDate(),
        updatedAt = getTimestamp("updatedAt")?.toDate()
    )
}

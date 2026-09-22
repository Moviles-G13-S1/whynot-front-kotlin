package com.example.whynotkotlin.features.wishlists.data

import com.example.whynotkotlin.features.wishlists.domain.Category
import com.example.whynotkotlin.features.wishlists.domain.Wishlist
import com.example.whynotkotlin.features.wishlists.domain.WishlistDraft
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val CATEGORIES = "categories"
private const val WISHLISTS = "wishlists"

class FirebaseWishlistRepository(
    private val firestore: FirebaseFirestore
) : WishlistRepository {

    override suspend fun getCategories(): List<Category> =
        firestore.collection(CATEGORIES).get().await()
            .documents
            .mapNotNull { it.toCategory() }
            .sortedBy { it.name }

    override fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection(CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents
                    ?.mapNotNull { it.toCategory() }
                    ?.sortedBy { it.name }
                    .orEmpty()

                trySend(categories)
            }

        awaitClose { registration.remove() }
    }

    override fun observeWishlists(ownerId: String): Flow<List<Wishlist>> = callbackFlow {
        // No orderBy: the backend ships an empty composite-index manifest, so
        // an equality filter plus a sort would need an index that does not
        // exist. Sorting happens here instead.
        val registration = firestore.collection(WISHLISTS)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val wishlists = snapshot?.documents
                    ?.mapNotNull { it.toWishlist() }
                    ?.sortedByDescending { it.createdAt }
                    .orEmpty()

                trySend(wishlists)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getWishlist(wishlistId: String): Wishlist? =
        firestore.collection(WISHLISTS).document(wishlistId).get().await().toWishlist()

    override suspend fun createWishlist(ownerId: String, draft: WishlistDraft): String {
        val document = firestore.collection(WISHLISTS).document()

        val data = mapOf(
            "ownerId" to ownerId,
            "categoryId" to draft.categoryId,
            "imageUrl" to draft.imageUrl.trim(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        document.set(data).await()
        return document.id
    }
}

private fun DocumentSnapshot.toCategory(): Category? {
    if (!exists()) return null

    return Category(
        id = id,
        name = getString("name").orEmpty()
    )
}

private fun DocumentSnapshot.toWishlist(): Wishlist? {
    if (!exists()) return null

    return Wishlist(
        id = id,
        ownerId = getString("ownerId").orEmpty(),
        categoryId = getString("categoryId").orEmpty(),
        imageUrl = getString("imageUrl").orEmpty(),
        createdAt = getTimestamp("createdAt")?.toDate(),
        updatedAt = getTimestamp("updatedAt")?.toDate()
    )
}

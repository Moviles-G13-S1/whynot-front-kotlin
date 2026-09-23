package com.example.whynotkotlin.features.admin.data

import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val PRODUCTS = "products"
private const val USERS = "users"
private const val ADMIN_METRICS = "adminMetrics"
private const val RECOMMENDED_PRODUCT_SAVES = "recommendedProductSaves"

/**
 * Real Firestore implementation of the shared Admin repository.
 *
 * NO fake data is used here.
 *
 * Firestore security rules only allow collection-wide reads when the
 * authenticated Firebase token contains admin=true.
 */
class FirebaseAdminRepository(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override fun observeAllProducts(): Flow<List<Product>> = callbackFlow {

        val registration = firestore
            .collection(PRODUCTS)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot
                    ?.documents
                    ?.mapNotNull { it.toAdminProduct() }
                    .orEmpty()

                trySend(products)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {

        val registration = firestore
            .collection(USERS)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot
                    ?.documents
                    ?.mapNotNull { it.toAdminUserProfile() }
                    .orEmpty()

                trySend(users)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeRecommendedProductSaves(): Flow<Long> = callbackFlow {

        val registration = firestore
            .collection(ADMIN_METRICS)
            .document(RECOMMENDED_PRODUCT_SAVES)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val total = snapshot
                    ?.getLong("total")
                    ?: 0L

                trySend(total)
            }

        awaitClose {
            registration.remove()
        }
    }
}

private fun DocumentSnapshot.toAdminProduct(): Product? {

    if (!exists()) {
        return null
    }

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
        purchasedAt = getTimestamp("purchasedAt")?.toDate(),
        createdAt = getTimestamp("createdAt")?.toDate(),
        updatedAt = getTimestamp("updatedAt")?.toDate()
    )
}

private fun DocumentSnapshot.toAdminUserProfile(): UserProfile? {

    if (!exists()) {
        return null
    }

    return UserProfile(
        uid = id,
        name = getString("name").orEmpty(),
        email = getString("email").orEmpty(),
        gender = getString("gender").orEmpty(),
        age = getLong("age")?.toInt() ?: 0,
        preferredCategoryId =
            getString("preferredCategoryId").orEmpty(),
        cityId = getString("cityId").orEmpty(),
        createdAt = getTimestamp("createdAt")?.toDate(),
        updatedAt = getTimestamp("updatedAt")?.toDate()
    )
}
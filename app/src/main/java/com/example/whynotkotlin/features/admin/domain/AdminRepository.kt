package com.example.whynotkotlin.features.admin.domain

import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Common data contract for the Admin module.
 *
 * Juan Felipe defines this contract so the remaining Admin BQs can reuse
 * the same access to products, users and administrative metrics.
 *
 * Firestore rules require the authenticated user to have the `admin` claim
 * for these collection-wide reads.
 */
interface AdminRepository {

    /**
     * All existing products from all users.
     *
     * Used by BQ1 and can also be reused by BQ2/BQ4/BQ5/BQ6.
     */
    fun observeAllProducts(): Flow<List<Product>>

    /**
     * All user profiles.
     *
     * Not required by BQ1/BQ3 directly, but forms part of the shared Admin
     * architecture for the demographic and zero-product BQs.
     */
    fun observeAllUsers(): Flow<List<UserProfile>>

    /**
     * BQ3:
     * How many recommended products have been saved?
     *
     * Reads adminMetrics/recommendedProductSaves.total.
     */
    fun observeRecommendedProductSaves(): Flow<Long>
}
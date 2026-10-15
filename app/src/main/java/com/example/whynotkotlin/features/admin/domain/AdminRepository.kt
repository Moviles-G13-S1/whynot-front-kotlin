package com.example.whynotkotlin.features.admin.domain

import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observeAllProducts(): Flow<List<Product>>

    fun observeAllUsers(): Flow<List<UserProfile>>

    fun observeRecommendedProductSaves(): Flow<Long>
}
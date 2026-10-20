package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observeAllProducts(): Flow<List<Product>>

    fun observeAllUsers(): Flow<List<UserProfile>>

    fun observeRecommendedProductSaves(): Flow<Long>
}
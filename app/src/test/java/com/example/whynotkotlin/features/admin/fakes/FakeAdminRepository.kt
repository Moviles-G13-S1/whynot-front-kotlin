package com.example.whynotkotlin.features.admin.fakes

import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FAKE SOLO PARA PRUEBAS.
 *
 * Permite probar AdminViewModel sin depender de Firestore
 * ni de un usuario con admin=true.
 *
 * NO usar en src/main.
 * NO registrar en AppDependencies.
 */
class FakeAdminRepository(
    initialProducts: List<Product> = emptyList(),
    initialUsers: List<UserProfile> = emptyList(),
    initialRecommendedProductSaves: Long = 0L
) : AdminRepository {

    val products = MutableStateFlow(initialProducts)

    val users = MutableStateFlow(initialUsers)

    val recommendedProductSaves =
        MutableStateFlow(initialRecommendedProductSaves)

    override fun observeAllProducts(): Flow<List<Product>> =
        products

    override fun observeAllUsers(): Flow<List<UserProfile>> =
        users

    override fun observeRecommendedProductSaves(): Flow<Long> =
        recommendedProductSaves
}
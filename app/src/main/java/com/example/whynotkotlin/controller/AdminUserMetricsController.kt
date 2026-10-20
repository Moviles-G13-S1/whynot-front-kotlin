package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.UserActivityMetrics
import com.example.whynotkotlin.model.UserProfile
import com.example.whynotkotlin.model.repositories.AdminRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/** BQ2 (repeat savers) and BQ5 (registered users with no products). */
class AdminUserMetricsController(
    repository: AdminRepository,
    scope: CoroutineScope
) {

    val metrics: StateFlow<Result<UserActivityMetrics>?> =
        combine(
            repository.observeAllProducts(),
            repository.observeAllUsers()
        ) { products, users -> calculateUserMetrics(products, users) }
            .asResults()
            .shareIn(scope)

    private fun calculateUserMetrics(
        products: List<Product>,
        users: List<UserProfile>
    ): UserActivityMetrics {
        val productCountsByUser = products
            .groupingBy { it.ownerId }
            .eachCount()

        val registeredUserIds = users
            .map { it.uid }
            .toSet()

        val totalUsers = registeredUserIds.size

        val activeUsers = registeredUserIds.count { uid ->
            (productCountsByUser[uid] ?: 0) >= 1
        }

        val repeatSaverUsers = registeredUserIds.count { uid ->
            (productCountsByUser[uid] ?: 0) >= 2
        }

        val zeroProductUsers = registeredUserIds.count { uid ->
            (productCountsByUser[uid] ?: 0) == 0
        }

        val repeatSaverPercentage =
            if (activeUsers == 0) {
                0.0
            } else {
                repeatSaverUsers * 100.0 / activeUsers
            }

        val zeroProductPercentage =
            if (totalUsers == 0) {
                0.0
            } else {
                zeroProductUsers * 100.0 / totalUsers
            }

        return UserActivityMetrics(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            repeatSaverUsers = repeatSaverUsers,
            repeatSaverPercentage = repeatSaverPercentage,
            zeroProductUsers = zeroProductUsers,
            zeroProductPercentage = zeroProductPercentage
        )
    }
}

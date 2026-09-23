package com.example.whynotkotlin.features.admin.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.admin.domain.SavedProductsBucket
import com.example.whynotkotlin.features.admin.domain.SavedProductsStats
import com.example.whynotkotlin.features.products.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the Admin module.
 *
 * Juan Felipe implements BQ1 and BQ3 here.
 *
 * BQ1 is calculated from all existing product documents.
 * BQ3 is read from the backend-maintained administrative metric.
 */
class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        observeJuanFelipeMetrics()
    }

    private fun observeJuanFelipeMetrics() {

        viewModelScope.launch {

            combine(
                repository.observeAllProducts(),
                repository.observeRecommendedProductSaves()
            ) { products, recommendedProductsSaved ->

                AdminUiState(
                    savedProductsStats =
                        calculateSavedProductsStats(products),
                    recommendedProductsSaved =
                        recommendedProductsSaved,
                    loading = false,
                    errorMessage = null
                )
            }
                .catch { exception ->

                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = exception.message
                            ?: "Unable to load admin metrics."
                    )
                }
                .collect { newState ->

                    _state.value = newState
                }
        }
    }

    fun clearError() {

        _state.value = _state.value.copy(
            errorMessage = null
        )
    }

    /**
     * BQ1:
     * How many products has a user saved?
     *
     * Only users who currently have at least one product are considered here.
     * Users with zero products belong to BQ5.
     */
    private fun calculateSavedProductsStats(
        products: List<Product>
    ): SavedProductsStats {

        val productsByUser = products
            .groupingBy { product ->
                product.ownerId
            }
            .eachCount()

        val activeUsers = productsByUser.size
        val totalProducts = products.size

        val singleProductUsers = productsByUser
            .values
            .count { count ->
                count == 1
            }

        val multipleProductUsers =
            activeUsers - singleProductUsers

        val averagePerActiveUser =
            if (activeUsers == 0) {
                0.0
            } else {
                totalProducts.toDouble() / activeUsers
            }

        val distribution = listOf(
            SavedProductsBucket(
                label = "1–5",
                userCount = productsByUser.values.count {
                    it in 1..5
                }
            ),
            SavedProductsBucket(
                label = "6–10",
                userCount = productsByUser.values.count {
                    it in 6..10
                }
            ),
            SavedProductsBucket(
                label = "11–20",
                userCount = productsByUser.values.count {
                    it in 11..20
                }
            ),
            SavedProductsBucket(
                label = "21–50",
                userCount = productsByUser.values.count {
                    it in 21..50
                }
            ),
            SavedProductsBucket(
                label = "51+",
                userCount = productsByUser.values.count {
                    it >= 51
                }
            )
        )

        return SavedProductsStats(
            totalProducts = totalProducts,
            activeUsers = activeUsers,
            averagePerActiveUser = averagePerActiveUser,
            singleProductUsers = singleProductUsers,
            multipleProductUsers = multipleProductUsers,
            distribution = distribution
        )
    }
}
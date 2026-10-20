package com.example.whynotkotlin.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.model.repositories.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AdminUserMetricsUiState(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val repeatSaverUsers: Int = 0,
    val repeatSaverPercentage: Double = 0.0,
    val zeroProductUsers: Int = 0,
    val zeroProductPercentage: Double = 0.0,
    val loading: Boolean = true,
    val errorMessage: String? = null
)

class AdminUserMetricsViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUserMetricsUiState())
    val state: StateFlow<AdminUserMetricsUiState> = _state.asStateFlow()

    init {
        observeUserMetrics()
    }

    private fun observeUserMetrics() {
        viewModelScope.launch {
            combine(
                repository.observeAllProducts(),
                repository.observeAllUsers()
            ) { products, users ->

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

                AdminUserMetricsUiState(
                    totalUsers = totalUsers,
                    activeUsers = activeUsers,
                    repeatSaverUsers = repeatSaverUsers,
                    repeatSaverPercentage = repeatSaverPercentage,
                    zeroProductUsers = zeroProductUsers,
                    zeroProductPercentage = zeroProductPercentage,
                    loading = false,
                    errorMessage = null
                )
            }
                .catch { exception ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = exception.message
                            ?: "Unable to load user metrics."
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
}
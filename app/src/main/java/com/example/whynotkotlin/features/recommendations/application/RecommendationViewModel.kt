package com.example.whynotkotlin.features.recommendations.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Controls the recommendation flow presented to the user.
 *
 * This ViewModel contains no fake data.
 *
 * While the real FirebaseRecommendationRepository is implemented,
 * this ViewModel can be tested using FakeRecommendationRepository
 * located exclusively under src/test.
 *
 * The production ViewModel does not depend on the fake implementation.
 */
class RecommendationViewModel(
    private val repository: RecommendationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecommendationUiState())
    val state: StateFlow<RecommendationUiState> = _state.asStateFlow()

    /**
     * Requests a recommendation from the repository.
     */
    fun loadRecommendation() {

        if (_state.value.loading) {
            return
        }

        viewModelScope.launch {

            _state.value = _state.value.copy(
                loading = true,
                errorMessage = null
            )

            try {

                val recommendation = repository.getRecommendation()

                _state.value = _state.value.copy(
                    recommendation = recommendation,
                    loading = false,
                    saved = false
                )

            } catch (exception: Exception) {

                _state.value = _state.value.copy(
                    loading = false,
                    errorMessage = exception.message
                        ?: "Unable to load recommendation."
                )
            }
        }
    }

    /**
     * Saves the currently displayed recommendation into a wishlist.
     *
     * recommendationEventId is preserved because the backend uses it
     * to identify that the saved product originally came from the
     * recommendation feature.
     */
    fun saveRecommendation(
        wishlistId: String
    ) {

        val recommendation = _state.value.recommendation
            ?: return

        if (_state.value.saving) {
            return
        }

        viewModelScope.launch {

            _state.value = _state.value.copy(
                saving = true,
                errorMessage = null
            )

            try {

                val result = repository.saveRecommendedProduct(
                    recommendationEventId =
                        recommendation.recommendationEventId,
                    wishlistId = wishlistId
                )

                _state.value = _state.value.copy(
                    saving = false,
                    saved = result.saved || result.alreadySaved
                )

            } catch (exception: Exception) {

                _state.value = _state.value.copy(
                    saving = false,
                    errorMessage = exception.message
                        ?: "Unable to save recommendation."
                )
            }
        }
    }

    fun retry() {
        loadRecommendation()
    }

    fun clearError() {
        _state.value = _state.value.copy(
            errorMessage = null
        )
    }
}
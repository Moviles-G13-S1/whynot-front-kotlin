package com.example.whynotkotlin.app

import androidx.lifecycle.ViewModel
import com.example.whynotkotlin.controller.SpeechViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whynotkotlin.controller.AdminInsightsViewModel
import com.example.whynotkotlin.controller.AdminViewModel
import com.example.whynotkotlin.controller.AuthViewModel
import com.example.whynotkotlin.controller.NearbyStoreViewModel
import com.example.whynotkotlin.controller.ProductViewModel
import com.example.whynotkotlin.controller.ProfileViewModel
import com.example.whynotkotlin.controller.RecommendationViewModel
import com.example.whynotkotlin.controller.WishlistViewModel
import com.example.whynotkotlin.controller.AdminUserMetricsViewModel

/**
 * Constructs the ViewModels from AppDependencies.
 */
class WhyNotViewModelFactory(
    private val dependencies: AppDependencies
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T = when {

        modelClass.isAssignableFrom(
            AuthViewModel::class.java
        ) -> AuthViewModel(
            authRepository =
                dependencies.authRepository,
            userRepository =
                dependencies.userRepository
        )

        modelClass.isAssignableFrom(
            ProfileViewModel::class.java
        ) -> ProfileViewModel(
            authRepository =
                dependencies.authRepository,
            userRepository =
                dependencies.userRepository,
            wishlistRepository =
                dependencies.wishlistRepository
        )

        modelClass.isAssignableFrom(
            WishlistViewModel::class.java
        ) -> WishlistViewModel(
            authRepository =
                dependencies.authRepository,
            wishlistRepository =
                dependencies.wishlistRepository,
            productRepository =
                dependencies.productRepository
        )

        modelClass.isAssignableFrom(
            ProductViewModel::class.java
        ) -> ProductViewModel(
            authRepository =
                dependencies.authRepository,
            productRepository =
                dependencies.productRepository
        )

        modelClass.isAssignableFrom(
            AdminViewModel::class.java
        ) -> AdminViewModel(
            repository =
                dependencies.adminRepository
        )

        modelClass.isAssignableFrom(
            AdminInsightsViewModel::class.java
        ) -> AdminInsightsViewModel(
            repository =
                dependencies.adminRepository
        )

        modelClass.isAssignableFrom(
            AdminUserMetricsViewModel::class.java
        ) -> AdminUserMetricsViewModel(
            repository =
                dependencies.adminRepository
        )

        modelClass.isAssignableFrom(
            NearbyStoreViewModel::class.java
        ) -> NearbyStoreViewModel(
            locationRepository =
                dependencies.locationRepository,
            nearbyStoreRepository =
                dependencies.nearbyStoreRepository
        )

        modelClass.isAssignableFrom(
            RecommendationViewModel::class.java
        ) -> RecommendationViewModel(
            repository =
                dependencies.recommendationRepository
        )

        modelClass.isAssignableFrom(SpeechViewModel::class.java) ->
            SpeechViewModel(dependencies.speechRecognitionRepository)

        else -> throw IllegalArgumentException(
            "Unknown ViewModel: ${modelClass.name}"
        )

    } as T
}
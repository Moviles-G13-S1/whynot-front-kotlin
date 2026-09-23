package com.example.whynotkotlin.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whynotkotlin.features.admin.application.AdminInsightsViewModel
import com.example.whynotkotlin.features.admin.application.AdminViewModel
import com.example.whynotkotlin.features.authentication.application.AuthViewModel
import com.example.whynotkotlin.features.nearby.application.NearbyStoreViewModel
import com.example.whynotkotlin.features.products.application.ProductViewModel
import com.example.whynotkotlin.features.profile.application.ProfileViewModel
import com.example.whynotkotlin.features.recommendations.application.RecommendationViewModel
import com.example.whynotkotlin.features.wishlists.application.WishlistViewModel

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

        else -> throw IllegalArgumentException(
            "Unknown ViewModel: ${modelClass.name}"
        )

    } as T
}
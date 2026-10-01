package com.example.whynotkotlin.core.di

import androidx.lifecycle.ViewModel
import com.example.whynotkotlin.features.speech.application.SpeechViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whynotkotlin.features.admin.application.AdminViewModel
import com.example.whynotkotlin.features.authentication.application.AuthViewModel
import com.example.whynotkotlin.features.products.application.ProductViewModel
import com.example.whynotkotlin.features.profile.application.ProfileViewModel
import com.example.whynotkotlin.features.recommendations.application.RecommendationViewModel
import com.example.whynotkotlin.features.wishlists.application.WishlistViewModel

/**
 * Constructs ViewModels using the application dependencies.
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
            RecommendationViewModel::class.java
        ) -> RecommendationViewModel(
            repository =
                dependencies.recommendationRepository
        )

        modelClass.isAssignableFrom(
            AdminViewModel::class.java
        ) -> AdminViewModel(
            repository =
                dependencies.adminRepository
        )

        modelClass.isAssignableFrom(SpeechViewModel::class.java) ->
            SpeechViewModel(dependencies.speechRecognitionRepository)

        else ->
            throw IllegalArgumentException(
                "Unknown ViewModel: ${modelClass.name}"
            )

    } as T
}
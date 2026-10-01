package com.example.whynotkotlin.core.di

import com.example.whynotkotlin.features.admin.data.FirebaseAdminRepository
import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.authentication.data.FirebaseAuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.products.data.FirebaseProductRepository
import com.example.whynotkotlin.features.products.domain.ProductRepository
import com.example.whynotkotlin.features.profile.data.FirebaseUserRepository
import com.example.whynotkotlin.features.profile.domain.UserRepository
import com.example.whynotkotlin.features.recommendations.data.FirebaseRecommendationRepository
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import com.example.whynotkotlin.features.wishlists.data.FirebaseWishlistRepository
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
import android.content.Context
import com.example.whynotkotlin.features.speech.domain.SpeechRecognitionRepository
import com.example.whynotkotlin.features.speech.data.AndroidSpeechRecognitionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/**
 * Composition root.
 *
 * This is the single place where the production application decides
 * which implementations are used for each repository.
 */
class AppDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val wishlistRepository: WishlistRepository,
    val productRepository: ProductRepository,
    val recommendationRepository: RecommendationRepository,
    val speechRecognitionRepository: SpeechRecognitionRepository,
    val adminRepository: AdminRepository
) {

    companion object {

        /**
         * Builds the real Firebase dependency graph.
         */
        fun firebase(context: Context): AppDependencies {

            val auth =
                FirebaseAuth.getInstance()

            val firestore =
                FirebaseFirestore.getInstance()

            val functions =
                FirebaseFunctions.getInstance(
                    "us-central1"
                )

            return AppDependencies(
                speechRecognitionRepository = AndroidSpeechRecognitionRepository(context),

                authRepository =
                    FirebaseAuthRepository(
                        auth
                    ),

                userRepository =
                    FirebaseUserRepository(
                        firestore
                    ),

                wishlistRepository =
                    FirebaseWishlistRepository(
                        firestore
                    ),

                productRepository =
                    FirebaseProductRepository(
                        firestore
                    ),

                recommendationRepository =
                    FirebaseRecommendationRepository(
                        functions
                    ),

                adminRepository =
                    FirebaseAdminRepository(
                        firestore
                    )
            )
        }
    }
}
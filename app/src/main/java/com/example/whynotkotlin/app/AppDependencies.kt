package com.example.whynotkotlin.app

import com.example.whynotkotlin.model.repositories.FirebaseAdminRepository
import com.example.whynotkotlin.model.repositories.AdminRepository
import android.content.Context
import com.example.whynotkotlin.model.repositories.FirebaseAuthRepository
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.AndroidLocationRepository
import com.example.whynotkotlin.model.repositories.FirebaseNearbyStoreRepository
import com.example.whynotkotlin.model.repositories.LocationRepository
import com.example.whynotkotlin.model.repositories.NearbyStoreRepository
import com.example.whynotkotlin.model.repositories.FirebaseProductRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.FirebaseUserRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import com.example.whynotkotlin.model.repositories.FirebaseRecommendationRepository
import com.example.whynotkotlin.model.repositories.RecommendationRepository
import com.example.whynotkotlin.model.repositories.AndroidSpeechRecognitionRepository
import com.example.whynotkotlin.model.repositories.SpeechRecognitionRepository
import com.example.whynotkotlin.model.repositories.FirebaseWishlistRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/**
 * Composition root: the single place that decides which repository
 * implementation the app runs against.
 */
class AppDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val wishlistRepository: WishlistRepository,
    val productRepository: ProductRepository,
    val adminRepository: AdminRepository,
    val locationRepository: LocationRepository,
    val nearbyStoreRepository: NearbyStoreRepository,
    val recommendationRepository: RecommendationRepository,
    val speechRecognitionRepository: SpeechRecognitionRepository
) {

    companion object {

        /**
         * Wires the real Firebase implementations.
         *
         * [context] is only used by the two device repositories: location
         * needs it to reach Play Services, speech to reach the recognition
         * service, and both to check their runtime permission.
         */
        fun firebase(context: Context): AppDependencies {

            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val functions = FirebaseFunctions.getInstance("us-central1")

            return AppDependencies(
                authRepository =
                    FirebaseAuthRepository(auth),

                userRepository =
                    FirebaseUserRepository(firestore),

                wishlistRepository =
                    FirebaseWishlistRepository(firestore),

                productRepository =
                    FirebaseProductRepository(firestore),

                adminRepository =
                    FirebaseAdminRepository(firestore),

                locationRepository =
                    AndroidLocationRepository(context),

                nearbyStoreRepository =
                    FirebaseNearbyStoreRepository(functions),

                recommendationRepository =
                    FirebaseRecommendationRepository(functions),

                speechRecognitionRepository =
                    AndroidSpeechRecognitionRepository(context)
            )
        }
    }
}
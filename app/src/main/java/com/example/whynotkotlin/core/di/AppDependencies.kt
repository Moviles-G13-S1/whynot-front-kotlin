package com.example.whynotkotlin.core.di

import com.example.whynotkotlin.features.admin.data.FirebaseAdminRepository
import com.example.whynotkotlin.features.admin.domain.AdminRepository
import android.content.Context
import com.example.whynotkotlin.features.authentication.data.FirebaseAuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.nearby.data.AndroidLocationRepository
import com.example.whynotkotlin.features.nearby.data.FirebaseNearbyStoreRepository
import com.example.whynotkotlin.features.nearby.domain.LocationRepository
import com.example.whynotkotlin.features.nearby.domain.NearbyStoreRepository
import com.example.whynotkotlin.features.products.data.FirebaseProductRepository
import com.example.whynotkotlin.features.products.domain.ProductRepository
import com.example.whynotkotlin.features.profile.data.FirebaseUserRepository
import com.example.whynotkotlin.features.profile.domain.UserRepository
import com.example.whynotkotlin.features.recommendations.data.FirebaseRecommendationRepository
import com.example.whynotkotlin.features.recommendations.domain.RecommendationRepository
import com.example.whynotkotlin.features.wishlists.data.FirebaseWishlistRepository
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
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
    val recommendationRepository: RecommendationRepository
) {

    companion object {

        /**
         * Wires the real Firebase implementations.
         *
         * [context] is only used by the location repository, which needs it to
         * reach Play Services and to check the runtime permission.
         */
        fun firebase(context: Context): AppDependencies {

            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val functions = FirebaseFunctions.getInstance()

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
                    FirebaseRecommendationRepository(functions)
            )
        }
    }
}
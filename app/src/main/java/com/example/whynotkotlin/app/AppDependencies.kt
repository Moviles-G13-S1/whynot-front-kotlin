package com.example.whynotkotlin.app

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.whynotkotlin.controller.AuthController
import com.example.whynotkotlin.controller.NearbyStoreController
import com.example.whynotkotlin.controller.ProductController
import com.example.whynotkotlin.controller.ProfileController
import com.example.whynotkotlin.controller.RecommendationController
import com.example.whynotkotlin.controller.WishlistController
import com.example.whynotkotlin.model.repositories.AndroidLocationRepository
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.FirebaseAuthRepository
import com.example.whynotkotlin.model.repositories.FirebaseNearbyStoreRepository
import com.example.whynotkotlin.model.repositories.FirebaseOnboardingRepository
import com.example.whynotkotlin.model.repositories.FirebaseProductRepository
import com.example.whynotkotlin.model.repositories.FirebaseRecommendationRepository
import com.example.whynotkotlin.model.repositories.FirebaseUserRepository
import com.example.whynotkotlin.model.repositories.FirebaseWishlistRepository
import com.example.whynotkotlin.model.repositories.LocationRepository
import com.example.whynotkotlin.model.repositories.NearbyStoreRepository
import com.example.whynotkotlin.model.repositories.OnboardingRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.RecommendationRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Composition root: the single place that decides which repository
 * implementation the app runs against, and that hands each controller its
 * repositories.
 *
 * [scope] lives as long as the app and keeps the controllers' shared streams.
 */
class AppDependencies(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    wishlistRepository: WishlistRepository,
    productRepository: ProductRepository,
    onboardingRepository: OnboardingRepository,
    locationRepository: LocationRepository,
    nearbyStoreRepository: NearbyStoreRepository,
    recommendationRepository: RecommendationRepository,
    scope: CoroutineScope
) {

    val authController =
        AuthController(authRepository, userRepository, onboardingRepository, scope)

    val profileController =
        ProfileController(authRepository, userRepository, wishlistRepository, scope)

    val wishlistController =
        WishlistController(
            authRepository,
            wishlistRepository,
            productRepository,
            onboardingRepository,
            scope
        )

    val productController =
        ProductController(authRepository, productRepository, scope)

    val nearbyStoreController =
        NearbyStoreController(locationRepository, nearbyStoreRepository)

    val recommendationController =
        RecommendationController(recommendationRepository)

    companion object {

        /**
         * Wires the real Firebase implementations.
         *
         * [context] is only used by the location repository, to reach Play
         * Services and to check its runtime permission.
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

                onboardingRepository =
                    FirebaseOnboardingRepository(firestore),

                locationRepository =
                    AndroidLocationRepository(context),

                nearbyStoreRepository =
                    FirebaseNearbyStoreRepository(functions),

                recommendationRepository =
                    FirebaseRecommendationRepository(functions),

                scope =
                    CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            )
        }
    }
}

/**
 * Hands the controllers to every view below MainActivity, the way Flutter's
 * DependenciesScope does.
 */
val LocalAppDependencies = staticCompositionLocalOf<AppDependencies> {
    error("AppDependencies were not provided")
}

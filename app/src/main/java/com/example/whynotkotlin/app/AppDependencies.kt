package com.example.whynotkotlin.app

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.whynotkotlin.controller.AuthController
import com.example.whynotkotlin.controller.NearbyStoreController
import com.example.whynotkotlin.controller.ProductController
import com.example.whynotkotlin.controller.ProfileController
import com.example.whynotkotlin.controller.RecommendationController
import com.example.whynotkotlin.controller.WishlistController
import com.example.whynotkotlin.model.RecommendationCache
import com.example.whynotkotlin.model.repositories.AndroidConnectivityRepository
import com.example.whynotkotlin.model.repositories.AndroidLocationRepository
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
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
import com.example.whynotkotlin.model.repositories.DataStoreProductDraftRepository
import com.example.whynotkotlin.model.repositories.ProductDraftRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.RecommendationRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
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
    productDraftRepository: ProductDraftRepository,
    onboardingRepository: OnboardingRepository,
    locationRepository: LocationRepository,
    nearbyStoreRepository: NearbyStoreRepository,
    recommendationRepository: RecommendationRepository,
    scope: CoroutineScope,
    uxEventRepository: com.example.whynotkotlin.model.repositories.UxEventRepository,

    /** Shared online/offline state, read by any screen through LocalAppDependencies. */
    val connectivity: ConnectivityRepository
) {

    val homeUxController = com.example.whynotkotlin.controller.HomeUxController(uxEventRepository, scope)

    val authController =
        AuthController(authRepository, userRepository, onboardingRepository, scope, connectivity) {
            recommendationController.onSignedOut()
        }

    val profileController =
        ProfileController(authRepository, userRepository, wishlistRepository, scope, connectivity) { uid ->
            recommendationController.onProfileUpdated(uid)
        }

    val wishlistController =
        WishlistController(
            authRepository,
            wishlistRepository,
            productRepository,
            onboardingRepository,
            scope,
            connectivity
        )

    val productController =
        ProductController(
            authRepository,
            productRepository,
            scope,
            draftRepository = productDraftRepository,
            connectivity = connectivity
        )

    val nearbyStoreController =
        NearbyStoreController(locationRepository, nearbyStoreRepository)

    val recommendationController =
        RecommendationController(
            repository = recommendationRepository,
            cache = RecommendationCache(),
            connectivity = connectivity,
            currentUid = { authRepository.currentUser()?.uid }
        )

    companion object {

        /**
         * Wires the real Firebase implementations.
         *
         * [context] is only used by the device repositories: location, to reach
         * Play Services and check its runtime permission, connectivity, and the
         * product drafts kept in DataStore.
         */
        fun firebase(context: Context): AppDependencies {

            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance().apply {
                firestoreSettings = offlineFirestoreSettings()
            }
            val functions = FirebaseFunctions.getInstance("us-central1")
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

            return AppDependencies(
                authRepository =
                    FirebaseAuthRepository(auth),

                userRepository =
                    FirebaseUserRepository(firestore),

                wishlistRepository =
                    FirebaseWishlistRepository(firestore),

                productRepository =
                    FirebaseProductRepository(firestore),

                productDraftRepository =
                    DataStoreProductDraftRepository(context),

                onboardingRepository =
                    FirebaseOnboardingRepository(firestore),

                locationRepository =
                    AndroidLocationRepository(context),

                nearbyStoreRepository =
                    FirebaseNearbyStoreRepository(functions),

                recommendationRepository =
                    FirebaseRecommendationRepository(functions),

                uxEventRepository =
                    com.example.whynotkotlin.model.repositories.FirebaseUxEventRepository(firestore),

                scope =
                    scope,

                connectivity =
                    AndroidConnectivityRepository(context, scope)
            )
        }
    }
}

/**
 * Firestore's offline cache, set explicitly instead of relying on the default.
 *
 * With a persistent cache, wishlists and products already seen keep showing
 * offline, writes queued offline (a purchase marked without connection)
 * survive closing the app and are sent once the connection returns. 50 MB is
 * far above what one user's wishlists and products take.
 */
private fun offlineFirestoreSettings(): FirebaseFirestoreSettings =
    FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(
            PersistentCacheSettings.newBuilder()
                .setSizeBytes(FIRESTORE_CACHE_BYTES)
                .build()
        )
        .build()

private const val FIRESTORE_CACHE_BYTES = 50L * 1024 * 1024

/**
 * Hands the controllers to every view below MainActivity, the way Flutter's
 * DependenciesScope does.
 */
val LocalAppDependencies = staticCompositionLocalOf<AppDependencies> {
    error("AppDependencies were not provided")
}

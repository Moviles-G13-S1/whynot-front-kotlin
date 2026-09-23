package com.example.whynotkotlin.core.di

import com.example.whynotkotlin.features.admin.data.FirebaseAdminRepository
import com.example.whynotkotlin.features.admin.domain.AdminRepository
import com.example.whynotkotlin.features.authentication.data.FirebaseAuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.products.data.FirebaseProductRepository
import com.example.whynotkotlin.features.products.domain.ProductRepository
import com.example.whynotkotlin.features.profile.data.FirebaseUserRepository
import com.example.whynotkotlin.features.profile.domain.UserRepository
import com.example.whynotkotlin.features.wishlists.data.FirebaseWishlistRepository
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Composition root: the single place that decides which repository
 * implementation the app runs against.
 */
class AppDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val wishlistRepository: WishlistRepository,
    val productRepository: ProductRepository,
    val adminRepository: AdminRepository
) {

    companion object {

        /**
         * Wires the real Firebase implementations.
         */
        fun firebase(): AppDependencies {

            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()

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
                    FirebaseAdminRepository(firestore)
            )
        }
    }
}
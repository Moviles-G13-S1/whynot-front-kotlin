package com.example.whynotkotlin.view.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController

/**
 * Every move between screens, so the screens only say where they want to go.
 */
class WhyNotNavigator(
    private val navController: NavHostController
) {

    fun back() {
        navController.popBackStack()
    }

    fun toHome() = navigateMain(WhyNotRoutes.HOME)

    fun toWishlists() = navigateMain(WhyNotRoutes.WISHLISTS)

    fun toAdd() = navigateMain(WhyNotRoutes.ADD)

    fun toPurchases() = navigateMain(WhyNotRoutes.PURCHASES)

    fun toProfile() = navigateMain(WhyNotRoutes.PROFILE)

    fun toRegister() {
        navController.navigate(WhyNotRoutes.REGISTER)
    }

    /** After sign-in or sign-up: the auth screens leave the back stack. */
    fun toHomeAfterSignIn() {
        navController.navigate(WhyNotRoutes.HOME) {
            popUpTo(WhyNotRoutes.LOGIN) {
                inclusive = true
            }
        }
    }

    fun toLoginAfterSignOut() {
        navController.navigate(WhyNotRoutes.LOGIN) {
            popUpTo(WhyNotRoutes.HOME) {
                inclusive = true
            }
        }
    }

    fun toNewWishlist() {
        navController.navigate(WhyNotRoutes.NEW_WISHLIST)
    }

    fun toWishlistDetail(wishlistId: String) {
        navController.navigate(WhyNotRoutes.wishlistDetail(wishlistId))
    }

    fun toNewProduct(productUrl: String = "") {
        navController.navigate(WhyNotRoutes.newProduct(productUrl))
    }

    fun toProductDetail(productId: String) {
        navController.navigate(WhyNotRoutes.productDetail(productId))
    }

    fun toEditProfile() {
        navController.navigate(WhyNotRoutes.EDIT_PROFILE)
    }

    fun toChangePassword() {
        navController.navigate(WhyNotRoutes.CHANGE_PASSWORD)
    }

    fun toAdmin() {
        navController.navigate(WhyNotRoutes.ADMIN_ROOT) {
            launchSingleTop = true
        }
    }

    fun toAdminSavedProducts() = navigateAdmin(WhyNotRoutes.ADMIN_SAVED_PRODUCTS)

    fun toAdminRepeatSavers() = navigateAdmin(WhyNotRoutes.ADMIN_REPEAT_SAVERS)

    fun toAdminRecommendedSaves() = navigateAdmin(WhyNotRoutes.ADMIN_RECOMMENDED_SAVES)

    fun toAdminPurchasesByCategory() = navigateAdmin(WhyNotRoutes.ADMIN_PURCHASES_BY_CATEGORY)

    fun toAdminZeroProducts() = navigateAdmin(WhyNotRoutes.ADMIN_ZERO_PRODUCTS)

    fun toAdminDemographicProfile() = navigateAdmin(WhyNotRoutes.ADMIN_DEMOGRAPHIC_PROFILE)

    /** Sends a user who is not an admin out of the admin graph. */
    fun leaveAdmin(isSignedIn: Boolean) {
        val destination =
            if (isSignedIn) WhyNotRoutes.HOME else WhyNotRoutes.LOGIN

        navController.navigate(destination) {
            popUpTo(WhyNotRoutes.ADMIN_ROOT) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }

    /**
     * State shared by every admin screen while the admin graph is open, such
     * as the selected months window.
     */
    fun adminGraphState(): SavedStateHandle =
        navController.getBackStackEntry(WhyNotRoutes.ADMIN_ROOT).savedStateHandle

    private fun navigateMain(route: String) {
        navController.navigate(route) {
            popUpTo(WhyNotRoutes.HOME) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }

    private fun navigateAdmin(route: String) {
        navController.navigate(route) {
            popUpTo(WhyNotRoutes.ADMIN_SAVED_PRODUCTS) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }
}

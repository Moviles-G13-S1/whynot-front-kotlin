package com.example.whynotkotlin.view.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.example.whynotkotlin.view.components.WhyNotTab

/**
 * The navigator, for shared components such as the bottom bar that move
 * between screens on their own.
 */
val LocalWhyNotNavigator = staticCompositionLocalOf<WhyNotNavigator?> { null }

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

    /** The Add tab opens the product form, as on iOS. */
    fun toAdd() = toNewProduct()

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

    fun toProductDetail(productId: String, sourceTab: WhyNotTab = WhyNotTab.WISHLISTS) {
        navController.navigate(WhyNotRoutes.productDetail(productId, sourceTab.name))
    }

    fun toEditProduct(productId: String, sourceTab: WhyNotTab) {
        navController.navigate(WhyNotRoutes.editProduct(productId, sourceTab.name))
    }

    fun toEditProfile() {
        navController.navigate(WhyNotRoutes.EDIT_PROFILE)
    }

    fun toChangePassword() {
        navController.navigate(WhyNotRoutes.CHANGE_PASSWORD)
    }

    private fun navigateMain(route: String) {
        navController.navigate(route) {
            popUpTo(WhyNotRoutes.HOME) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }
}

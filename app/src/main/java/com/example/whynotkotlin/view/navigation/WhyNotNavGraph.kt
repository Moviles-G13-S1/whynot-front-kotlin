package com.example.whynotkotlin.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Text
import com.example.whynotkotlin.model.readableMessage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.components.WhyNotLoading
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.screens.auth.LoginRoute
import com.example.whynotkotlin.view.screens.auth.RegisterRoute
import com.example.whynotkotlin.view.screens.home.HomeRoute
import com.example.whynotkotlin.view.screens.products.EditProductRoute
import com.example.whynotkotlin.view.screens.products.NewProductRoute
import com.example.whynotkotlin.view.screens.products.ProductDetailRoute
import com.example.whynotkotlin.view.screens.profile.ChangePasswordRoute
import com.example.whynotkotlin.view.screens.profile.EditProfileRoute
import com.example.whynotkotlin.view.screens.profile.ProfileRoute
import com.example.whynotkotlin.view.screens.purchases.PurchasesRoute
import com.example.whynotkotlin.view.screens.wishlists.NewWishlistRoute
import com.example.whynotkotlin.view.screens.wishlists.WishlistDetailRoute
import com.example.whynotkotlin.view.screens.wishlists.WishlistsRoute

/**
 * The route table. Each destination only names the screen it shows; what a
 * screen reads and does lives in the screen and its controllers.
 */
@Composable
fun WhyNotNavigation() {
    val navController =
        rememberNavController()

    val navigator =
        remember(navController) {
            WhyNotNavigator(navController)
        }

    val session by LocalAppDependencies.current
        .authController
        .session
        .collectAsStateWithLifecycle()

    val resolvedSession = session

    if (resolvedSession == null) {
        WhyNotLoading()
        return
    }

    if (resolvedSession.isFailure) {
        Text(resolvedSession.exceptionOrNull()!!.readableMessage())
        return
    }
    val entry by navController.currentBackStackEntryAsState()
    LaunchedEffect(resolvedSession.getOrNull()?.uid, entry?.destination?.route) {
        val route = entry?.destination?.route
        if (resolvedSession.getOrNull() == null && route != null &&
            route != WhyNotRoutes.LOGIN && route != WhyNotRoutes.REGISTER) {
            navigator.toLoginAfterSignOut()
        }
    }

    val startDestination =
        if (resolvedSession.getOrNull() == null) {
            WhyNotRoutes.LOGIN
        } else {
            WhyNotRoutes.HOME
        }

    CompositionLocalProvider(LocalWhyNotNavigator provides navigator) {
        NavHost(
            navController = navController,
            startDestination =
                startDestination
        ) {
            composable(WhyNotRoutes.LOGIN) {
                LoginRoute(navigator)
            }

            composable(WhyNotRoutes.REGISTER) {
                RegisterRoute(navigator)
            }

            composable(WhyNotRoutes.HOME) {
                HomeRoute(navigator)
            }

            composable(WhyNotRoutes.WISHLISTS) {
                WishlistsRoute(navigator)
            }

            composable(WhyNotRoutes.NEW_WISHLIST) {
                NewWishlistRoute(navigator)
            }

            composable(
                WhyNotRoutes.WISHLIST_DETAIL,
                arguments = listOf(
                    navArgument(WhyNotRoutes.ARG_WISHLIST_ID) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                WishlistDetailRoute(
                    wishlistId =
                        backStackEntry.arguments
                            ?.getString(WhyNotRoutes.ARG_WISHLIST_ID)
                            .orEmpty(),
                    navigator = navigator
                )
            }

            composable(
                WhyNotRoutes.PRODUCT_DETAIL,
                arguments = productArguments
            ) { backStackEntry ->
                ProductDetailRoute(
                    productId =
                        backStackEntry.arguments
                            ?.getString(WhyNotRoutes.ARG_PRODUCT_ID)
                            .orEmpty(),
                    sourceTab = backStackEntry.sourceTab(),
                    navigator = navigator
                )
            }

            composable(
                WhyNotRoutes.EDIT_PRODUCT,
                arguments = productArguments
            ) { backStackEntry ->
                EditProductRoute(
                    productId =
                        backStackEntry.arguments
                            ?.getString(WhyNotRoutes.ARG_PRODUCT_ID)
                            .orEmpty(),
                    sourceTab = backStackEntry.sourceTab(),
                    navigator = navigator
                )
            }

            composable(
                WhyNotRoutes.NEW_PRODUCT,
                arguments = listOf(
                    navArgument(WhyNotRoutes.ARG_PRODUCT_URL) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                NewProductRoute(
                    prefilledProductUrl =
                        backStackEntry.arguments
                            ?.getString(WhyNotRoutes.ARG_PRODUCT_URL)
                            .orEmpty(),
                    navigator = navigator
                )
            }

            composable(WhyNotRoutes.PURCHASES) {
                PurchasesRoute(navigator)
            }

            composable(WhyNotRoutes.PROFILE) {
                ProfileRoute(navigator)
            }

            composable(WhyNotRoutes.EDIT_PROFILE) {
                EditProfileRoute(navigator)
            }

            composable(WhyNotRoutes.CHANGE_PASSWORD) {
                ChangePasswordRoute(navigator)
            }
        }
    }
}

private val productArguments = listOf(
    navArgument(WhyNotRoutes.ARG_PRODUCT_ID) {
        type = NavType.StringType
    },
    navArgument(WhyNotRoutes.ARG_SOURCE_TAB) {
        type = NavType.StringType
        defaultValue = WhyNotTab.WISHLISTS.name
    }
)

private fun NavBackStackEntry.sourceTab(): WhyNotTab =
    arguments
        ?.getString(WhyNotRoutes.ARG_SOURCE_TAB)
        ?.let { name -> WhyNotTab.entries.firstOrNull { it.name == name } }
        ?: WhyNotTab.WISHLISTS

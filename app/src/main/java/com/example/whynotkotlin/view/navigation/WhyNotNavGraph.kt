package com.example.whynotkotlin.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.components.WhyNotLoading
import com.example.whynotkotlin.view.screens.admin.AdminDemographicProfileRoute
import com.example.whynotkotlin.view.screens.admin.AdminPurchasesByCategoryRoute
import com.example.whynotkotlin.view.screens.admin.AdminRecommendedSavesRoute
import com.example.whynotkotlin.view.screens.admin.AdminRepeatSaversRoute
import com.example.whynotkotlin.view.screens.admin.AdminSavedProductsRoute
import com.example.whynotkotlin.view.screens.admin.AdminZeroProductsRoute
import com.example.whynotkotlin.view.screens.auth.LoginRoute
import com.example.whynotkotlin.view.screens.auth.RegisterRoute
import com.example.whynotkotlin.view.screens.home.HomeRoute
import com.example.whynotkotlin.view.screens.products.AddProductRoute
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

    val startDestination =
        if (resolvedSession.getOrNull() == null) {
            WhyNotRoutes.LOGIN
        } else {
            WhyNotRoutes.HOME
        }

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
            arguments = listOf(
                navArgument(WhyNotRoutes.ARG_PRODUCT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            ProductDetailRoute(
                productId =
                    backStackEntry.arguments
                        ?.getString(WhyNotRoutes.ARG_PRODUCT_ID)
                        .orEmpty(),
                navigator = navigator
            )
        }

        composable(WhyNotRoutes.ADD) {
            AddProductRoute(navigator)
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

        navigation(
            route =
                WhyNotRoutes.ADMIN_ROOT,

            startDestination =
                WhyNotRoutes.ADMIN_SAVED_PRODUCTS
        ) {
            composable(WhyNotRoutes.ADMIN_SAVED_PRODUCTS) {
                AdminRouteGuard(navigator) {
                    AdminSavedProductsRoute(navigator)
                }
            }

            composable(WhyNotRoutes.ADMIN_REPEAT_SAVERS) {
                AdminRouteGuard(navigator) {
                    AdminRepeatSaversRoute(navigator)
                }
            }

            composable(WhyNotRoutes.ADMIN_RECOMMENDED_SAVES) {
                AdminRouteGuard(navigator) {
                    AdminRecommendedSavesRoute(navigator)
                }
            }

            composable(WhyNotRoutes.ADMIN_PURCHASES_BY_CATEGORY) {
                AdminRouteGuard(navigator) {
                    AdminPurchasesByCategoryRoute(navigator)
                }
            }

            composable(WhyNotRoutes.ADMIN_ZERO_PRODUCTS) {
                AdminRouteGuard(navigator) {
                    AdminZeroProductsRoute(navigator)
                }
            }

            composable(WhyNotRoutes.ADMIN_DEMOGRAPHIC_PROFILE) {
                AdminRouteGuard(navigator) {
                    AdminDemographicProfileRoute(navigator)
                }
            }
        }
    }
}

/**
 * Only shows [content] to a signed-in admin; anyone else is sent home, or to
 * the login screen when signed out.
 */
@Composable
private fun AdminRouteGuard(
    navigator: WhyNotNavigator,
    content: @Composable () -> Unit
) {
    val session by LocalAppDependencies.current
        .authController
        .session
        .collectAsStateWithLifecycle()

    val user = session?.getOrNull()
    val isSignedIn = user != null
    val isAdmin = user?.isAdmin == true

    if (!isAdmin) {
        LaunchedEffect(
            isSignedIn,
            isAdmin
        ) {
            navigator.leaveAdmin(isSignedIn)
        }

        WhyNotLoading()
        return
    }

    content()
}

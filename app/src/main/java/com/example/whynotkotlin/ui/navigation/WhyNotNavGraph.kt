package com.example.whynotkotlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.whynotkotlin.core.di.AppDependencies
import com.example.whynotkotlin.core.di.WhyNotViewModelFactory
import com.example.whynotkotlin.features.admin.application.AdminInsightsUiState
import com.example.whynotkotlin.features.admin.application.AdminInsightsViewModel
import com.example.whynotkotlin.features.admin.application.AdminUserMetricsUiState
import com.example.whynotkotlin.features.admin.application.AdminUserMetricsViewModel
import com.example.whynotkotlin.features.admin.application.AdminViewModel
import com.example.whynotkotlin.features.authentication.application.AuthUiState
import com.example.whynotkotlin.features.authentication.application.AuthViewModel
import com.example.whynotkotlin.features.nearby.application.NearbyStoreViewModel
import com.example.whynotkotlin.features.products.application.ProductViewModel
import com.example.whynotkotlin.features.profile.domain.UserProfileDraft
import com.example.whynotkotlin.features.recommendations.application.RecommendationViewModel
import com.example.whynotkotlin.features.wishlists.application.WishlistViewModel
import com.example.whynotkotlin.ui.components.WhyNotLoading
import com.example.whynotkotlin.ui.screens.admin.AdminDemographicProfileScreen
import com.example.whynotkotlin.ui.screens.admin.AdminPurchasesByCategoryScreen
import com.example.whynotkotlin.ui.screens.admin.AdminRepeatSaversScreen
import com.example.whynotkotlin.ui.screens.admin.AdminRecommendedSavesScreen
import com.example.whynotkotlin.ui.screens.admin.AdminSavedProductsScreen
import com.example.whynotkotlin.ui.screens.admin.AdminZeroProductsScreen
import com.example.whynotkotlin.ui.screens.auth.LoginScreen
import com.example.whynotkotlin.ui.screens.auth.RegisterScreen
import com.example.whynotkotlin.ui.screens.home.HomeScreen
import com.example.whynotkotlin.ui.screens.products.AddProductScreen
import com.example.whynotkotlin.ui.screens.products.NewProductScreen
import com.example.whynotkotlin.ui.screens.products.ProductDetailScreen
import com.example.whynotkotlin.ui.screens.profile.ChangePasswordScreen
import com.example.whynotkotlin.ui.screens.profile.EditProfileScreen
import com.example.whynotkotlin.ui.screens.profile.ProfileScreen
import com.example.whynotkotlin.ui.screens.purchases.PurchasesScreen
import com.example.whynotkotlin.ui.screens.wishlists.NewWishlistScreen
import com.example.whynotkotlin.ui.screens.wishlists.WishlistDetailScreen
import com.example.whynotkotlin.ui.screens.wishlists.WishlistsScreen

object WhyNotRoutes {

    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME = "home"
    const val WISHLISTS = "wishlists"
    const val ADD = "add"
    const val PURCHASES = "purchases"
    const val PROFILE = "profile"

    const val EDIT_PROFILE =
        "edit_profile"

    const val CHANGE_PASSWORD =
        "change_password"

    const val NEW_WISHLIST =
        "new_wishlist"

    const val WISHLIST_DETAIL =
        "wishlist_detail"

    const val NEW_PRODUCT =
        "new_product"

    const val PRODUCT_DETAIL =
        "product_detail"

    /*
     * Admin module.
     */
    const val ADMIN_ROOT =
        "admin"

    const val ADMIN_SAVED_PRODUCTS =
        "admin_saved_products"

    const val ADMIN_REPEAT_SAVERS =
        "admin_repeat_savers"

    const val ADMIN_RECOMMENDED_SAVES =
        "admin_recommended_saves"

    const val ADMIN_PURCHASES_BY_CATEGORY =
        "admin_purchases_by_category"

    const val ADMIN_ZERO_PRODUCTS =
        "admin_zero_products"

    const val ADMIN_DEMOGRAPHIC_PROFILE =
        "admin_demographic_profile"
}

@Composable
fun WhyNotNavigation(
    dependencies: AppDependencies
) {
    val navController =
        rememberNavController()

    val factory =
        WhyNotViewModelFactory(
            dependencies
        )

    val authViewModel:
            AuthViewModel =
        viewModel(
            factory = factory
        )

    val wishlistViewModel:
            WishlistViewModel =
        viewModel(
            factory = factory
        )

    val productViewModel:
            ProductViewModel =
        viewModel(
            factory = factory
        )

    val authState by
    authViewModel
        .state
        .collectAsStateWithLifecycle()

    val wishlistState by
    wishlistViewModel
        .state
        .collectAsStateWithLifecycle()

    val productState by
    productViewModel
        .state
        .collectAsStateWithLifecycle()

    /*
     * Wait until Firebase Authentication has resolved the
     * current session and its custom claims.
     */
    if (authState.checkingSession) {
        WhyNotLoading()
        return
    }

    val startDestination =
        if (authState.session == null) {
            WhyNotRoutes.LOGIN
        } else {
            WhyNotRoutes.HOME
        }

    NavHost(
        navController = navController,
        startDestination =
            startDestination
    ) {

        /*
         * ---------------------------------------------------------
         * AUTH
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.LOGIN
        ) {

            ClearErrorOnEnter(
                authViewModel::clearError
            )

            LoginScreen(
                submitting =
                    authState.submitting,

                errorMessage =
                    authState.errorMessage,

                onSubmit = {
                        email,
                        password ->

                    authViewModel.signIn(
                        email,
                        password
                    ) {

                        navController.navigate(
                            WhyNotRoutes.HOME
                        ) {

                            popUpTo(
                                WhyNotRoutes.LOGIN
                            ) {
                                inclusive = true
                            }
                        }
                    }
                },

                onCreateAccountClick = {

                    navController.navigate(
                        WhyNotRoutes.REGISTER
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.REGISTER
        ) {

            ClearErrorOnEnter(
                authViewModel::clearError
            )

            RegisterScreen(
                submitting =
                    authState.submitting,

                errorMessage =
                    authState.errorMessage,

                onSubmit = {
                        name,
                        email,
                        password,
                        gender,
                        age,
                        categoryId,
                        cityId ->

                    authViewModel.signUp(
                        email = email,
                        password = password,

                        draft =
                            UserProfileDraft(
                                name = name,
                                gender = gender,

                                age =
                                    age
                                        .trim()
                                        .toIntOrNull()
                                        ?: 0,

                                preferredCategoryId =
                                    categoryId,

                                cityId =
                                    cityId
                            )
                    ) {

                        navController.navigate(
                            WhyNotRoutes.HOME
                        ) {

                            popUpTo(
                                WhyNotRoutes.LOGIN
                            ) {
                                inclusive = true
                            }
                        }
                    }
                },

                onBackToLoginClick = {

                    navController
                        .popBackStack()
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * HOME
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.HOME
        ) {

            val nearbyStoreViewModel:
                    NearbyStoreViewModel =
                viewModel(
                    factory = factory
                )

            val recommendationViewModel:
                    RecommendationViewModel =
                viewModel(
                    factory = factory
                )

            val nearbyState by
            nearbyStoreViewModel
                .state
                .collectAsStateWithLifecycle()

            val recommendationState by
            recommendationViewModel
                .state
                .collectAsStateWithLifecycle()

            HomeScreen(
                nearbyState = nearbyState,
                recommendationState = recommendationState,
                wishlistSummaries = wishlistState.summaries,
                onLoadNearby =
                    nearbyStoreViewModel::loadNearestStore,
                onLoadRecommendation =
                    recommendationViewModel::loadRecommendation,
                onSaveRecommendation =
                    recommendationViewModel::saveRecommendation,
                onHomeClick = {},

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onWishlistClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * WISHLISTS
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.WISHLISTS
        ) {

            WishlistsScreen(
                state =
                    wishlistState,

                onWishlistClick = {
                        wishlistId ->

                    wishlistViewModel
                        .selectWishlist(
                            wishlistId
                        )

                    navController.navigate(
                        WhyNotRoutes
                            .WISHLIST_DETAIL
                    )
                },

                onNewWishlistClick = {

                    wishlistViewModel
                        .clearError()

                    navController.navigate(
                        WhyNotRoutes
                            .NEW_WISHLIST
                    )
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {},

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.NEW_WISHLIST
        ) {

            NewWishlistScreen(
                state =
                    wishlistState,

                onSave = {
                        categoryId,
                        imageUrl ->

                    wishlistViewModel
                        .createWishlist(
                            categoryId,
                            imageUrl
                        ) {

                            navController
                                .popBackStack()
                        }
                },

                onCancelClick = {

                    navController
                        .popBackStack()
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.WISHLIST_DETAIL
        ) {

            WishlistDetailScreen(
                wishlistName =
                    wishlistState
                        .selected
                        ?.categoryName
                        ?: "Wishlist",

                products =
                    wishlistState
                        .selectedWishlistId
                        ?.let {
                            productState
                                .forWishlist(it)
                        }
                        .orEmpty(),

                errorMessage =
                    productState
                        .errorMessage,

                onProductClick = {
                        productId ->

                    productViewModel
                        .selectProduct(
                            productId
                        )

                    navController.navigate(
                        WhyNotRoutes
                            .PRODUCT_DETAIL
                    )
                },

                onAddItemClick = {

                    productViewModel
                        .clearError()

                    navController.navigate(
                        WhyNotRoutes
                            .NEW_PRODUCT
                    )
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * PRODUCTS
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.PRODUCT_DETAIL
        ) {

            ProductDetailScreen(
                product =
                    productState.selected,

                errorMessage =
                    productState
                        .errorMessage,

                onMarkPurchased = {

                    productState
                        .selected
                        ?.let(
                            productViewModel::
                            markPurchased
                        )
                },

                onDeleteClick = {

                    productState
                        .selected
                        ?.let {
                                product ->

                            productViewModel
                                .deleteProduct(
                                    product.id
                                ) {

                                    navController
                                        .popBackStack()
                                }
                        }
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.ADD
        ) {

            AddProductScreen(
                onContinueWithLink = {
                        link ->

                    productViewModel
                        .setPendingProductUrl(
                            link
                        )

                    navController.navigate(
                        WhyNotRoutes
                            .NEW_PRODUCT
                    )
                },

                onAddManuallyClick = {

                    productViewModel
                        .setPendingProductUrl(
                            ""
                        )

                    navController.navigate(
                        WhyNotRoutes
                            .NEW_PRODUCT
                    )
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {},

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.NEW_PRODUCT
        ) {

            NewProductScreen(
                wishlists =
                    wishlistState
                        .summaries,

                prefilledProductUrl =
                    productState
                        .pendingProductUrl,

                saving =
                    productState
                        .saving,

                errorMessage =
                    productState
                        .errorMessage,

                onSave = {
                        wishlistId,
                        name,
                        brand,
                        price,
                        imageUrl,
                        productUrl ->

                    productViewModel
                        .createProduct(
                            wishlistId =
                                wishlistId,

                            name =
                                name,

                            brand =
                                brand,

                            price =
                                price,

                            imageUrl =
                                imageUrl,

                            productUrl =
                                productUrl
                        ) {

                            navController
                                .popBackStack()
                        }
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * PURCHASES
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.PURCHASES
        ) {

            PurchasesScreen(
                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {},

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * PROFILE
         * ---------------------------------------------------------
         */

        composable(
            WhyNotRoutes.PROFILE
        ) {

            ProfileScreen(

                /*
                 * Only authenticated users whose Firebase ID token
                 * contains admin=true see the Admin button.
                 */
                isAdmin =
                    authState
                        .session
                        ?.isAdmin == true,

                onAdminClick = {

                    navController.navigate(
                        WhyNotRoutes.ADMIN_ROOT
                    ) {
                        launchSingleTop = true
                    }
                },

                onEditProfileClick = {

                    navController.navigate(
                        WhyNotRoutes
                            .EDIT_PROFILE
                    )
                },

                onChangePasswordClick = {

                    navController.navigate(
                        WhyNotRoutes
                            .CHANGE_PASSWORD
                    )
                },

                onLogoutClick = {

                    authViewModel.signOut {

                        navController.navigate(
                            WhyNotRoutes.LOGIN
                        ) {

                            popUpTo(
                                WhyNotRoutes.HOME
                            ) {
                                inclusive = true
                            }
                        }
                    }
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {}
            )
        }

        composable(
            WhyNotRoutes.EDIT_PROFILE
        ) {

            EditProfileScreen(
                onBackClick = {

                    navController
                        .popBackStack()
                },

                onSaveChangesClick = {

                    navController
                        .popBackStack()
                },

                onChangePasswordClick = {

                    navController.navigate(
                        WhyNotRoutes
                            .CHANGE_PASSWORD
                    )
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        composable(
            WhyNotRoutes.CHANGE_PASSWORD
        ) {

            ChangePasswordScreen(
                onBackClick = {

                    navController
                        .popBackStack()
                },

                onUpdatePasswordClick = {

                    navController
                        .popBackStack()
                },

                onHomeClick = {

                    navController.navigateMain(
                        WhyNotRoutes.HOME
                    )
                },

                onWishlistsClick = {

                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onAddClick = {

                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                },

                onPurchasesClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PURCHASES
                    )
                },

                onProfileClick = {

                    navController.navigateMain(
                        WhyNotRoutes.PROFILE
                    )
                }
            )
        }

        /*
         * ---------------------------------------------------------
         * ADMIN
         * ---------------------------------------------------------
         *
         * The graph can technically be addressed by route, so every
         * destination checks the authenticated user's admin claim
         * BEFORE the AdminViewModel is created.
         *
         * Firestore Rules are still responsible for real backend
         * authorization.
         */

        navigation(
            route =
                WhyNotRoutes.ADMIN_ROOT,

            startDestination =
                WhyNotRoutes
                    .ADMIN_SAVED_PRODUCTS
        ) {

            /*
             * BQ1
             */
            composable(
                WhyNotRoutes
                    .ADMIN_SAVED_PRODUCTS
            ) {
                    backStackEntry ->

                AdminRouteGuard(
                    isSignedIn =
                        authState.session != null,

                    isAdmin =
                        authState
                            .session
                            ?.isAdmin == true,

                    navController =
                        navController
                ) {

                    val adminGraphEntry =
                        remember(
                            backStackEntry
                        ) {

                            navController
                                .getBackStackEntry(
                                    WhyNotRoutes
                                        .ADMIN_ROOT
                                )
                        }

                    val adminViewModel:
                            AdminViewModel =
                        viewModel(
                            viewModelStoreOwner =
                                adminGraphEntry,

                            factory =
                                factory
                        )

                    val adminState by
                    adminViewModel
                        .state
                        .collectAsStateWithLifecycle()

                    AdminSavedProductsScreen(
                        state =
                            adminState,

                        onRecommendedSavesClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_RECOMMENDED_SAVES
                                )
                        },

                        onRepeatSaversClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_REPEAT_SAVERS
                                )
                        },

                        onPurchasesByCategoryClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_PURCHASES_BY_CATEGORY
                                )
                        },

                        onZeroProductsClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_ZERO_PRODUCTS
                                )
                        },

                        onDemographicProfileClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_DEMOGRAPHIC_PROFILE
                                )
                        }
                    )
                }
            }

            /*
             * BQ2 — Martin
             */
            composable(
                WhyNotRoutes
                    .ADMIN_REPEAT_SAVERS
            ) {
                    backStackEntry ->

                AdminUserMetricsHost(
                    authState = authState,
                    navController = navController,
                    backStackEntry = backStackEntry,
                    factory = factory
                ) { metricsState ->

                    AdminRepeatSaversScreen(
                        state = metricsState,

                        onSavedProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_SAVED_PRODUCTS
                            )
                        },

                        onRecommendedSavesClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_RECOMMENDED_SAVES
                            )
                        },

                        onPurchasesByCategoryClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_PURCHASES_BY_CATEGORY
                            )
                        },

                        onZeroProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_ZERO_PRODUCTS
                            )
                        },

                        onDemographicProfileClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_DEMOGRAPHIC_PROFILE
                            )
                        }
                    )
                }
            }

            /*
             * BQ3
             */
            composable(
                WhyNotRoutes
                    .ADMIN_RECOMMENDED_SAVES
            ) {
                    backStackEntry ->

                AdminRouteGuard(
                    isSignedIn =
                        authState.session != null,

                    isAdmin =
                        authState
                            .session
                            ?.isAdmin == true,

                    navController =
                        navController
                ) {

                    val adminGraphEntry =
                        remember(
                            backStackEntry
                        ) {

                            navController
                                .getBackStackEntry(
                                    WhyNotRoutes
                                        .ADMIN_ROOT
                                )
                        }

                    val adminViewModel:
                            AdminViewModel =
                        viewModel(
                            viewModelStoreOwner =
                                adminGraphEntry,

                            factory =
                                factory
                        )

                    val adminState by
                    adminViewModel
                        .state
                        .collectAsStateWithLifecycle()

                    AdminRecommendedSavesScreen(
                        state =
                            adminState,

                        onSavedProductsClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_SAVED_PRODUCTS
                                )
                        },

                        onRepeatSaversClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_REPEAT_SAVERS
                                )
                        },

                        onPurchasesByCategoryClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_PURCHASES_BY_CATEGORY
                                )
                        },

                        onZeroProductsClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_ZERO_PRODUCTS
                                )
                        },

                        onDemographicProfileClick = {

                            navController
                                .navigateAdmin(
                                    WhyNotRoutes
                                        .ADMIN_DEMOGRAPHIC_PROFILE
                                )
                        }
                    )
                }
            }

            /*
             * BQ4 — Miguel
             */
            composable(
                WhyNotRoutes
                    .ADMIN_PURCHASES_BY_CATEGORY
            ) {
                    backStackEntry ->

                AdminInsightsHost(
                    authState = authState,
                    navController = navController,
                    backStackEntry = backStackEntry,
                    factory = factory
                ) { insightsState, insightsViewModel ->

                    AdminPurchasesByCategoryScreen(
                        state = insightsState,

                        onSavedProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_SAVED_PRODUCTS
                            )
                        },

                        onRepeatSaversClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_REPEAT_SAVERS
                            )
                        },

                        onRecommendedSavesClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_RECOMMENDED_SAVES
                            )
                        },

                        onZeroProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_ZERO_PRODUCTS
                            )
                        },

                        onDemographicProfileClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_DEMOGRAPHIC_PROFILE
                            )
                        },

                        onWindowChange =
                            insightsViewModel::selectMonthsWindow
                    )
                }
            }

            /*
             * BQ5 — Martin
             */
            composable(
                WhyNotRoutes
                    .ADMIN_ZERO_PRODUCTS
            ) {
                    backStackEntry ->

                AdminUserMetricsHost(
                    authState = authState,
                    navController = navController,
                    backStackEntry = backStackEntry,
                    factory = factory
                ) { metricsState ->

                    AdminZeroProductsScreen(
                        state = metricsState,

                        onSavedProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_SAVED_PRODUCTS
                            )
                        },

                        onRepeatSaversClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_REPEAT_SAVERS
                            )
                        },

                        onRecommendedSavesClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_RECOMMENDED_SAVES
                            )
                        },

                        onPurchasesByCategoryClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_PURCHASES_BY_CATEGORY
                            )
                        },

                        onDemographicProfileClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_DEMOGRAPHIC_PROFILE
                            )
                        }
                    )
                }
            }

            /*
             * BQ6 — Miguel
             */
            composable(
                WhyNotRoutes
                    .ADMIN_DEMOGRAPHIC_PROFILE
            ) {
                    backStackEntry ->

                AdminInsightsHost(
                    authState = authState,
                    navController = navController,
                    backStackEntry = backStackEntry,
                    factory = factory
                ) { insightsState, _ ->

                    AdminDemographicProfileScreen(
                        state = insightsState,

                        onSavedProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_SAVED_PRODUCTS
                            )
                        },

                        onRepeatSaversClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_REPEAT_SAVERS
                            )
                        },

                        onRecommendedSavesClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_RECOMMENDED_SAVES
                            )
                        },

                        onPurchasesByCategoryClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_PURCHASES_BY_CATEGORY
                            )
                        },

                        onZeroProductsClick = {
                            navController.navigateAdmin(
                                WhyNotRoutes.ADMIN_ZERO_PRODUCTS
                            )
                        }
                    )
                }
            }
        }
    }
}

/*
 * -------------------------------------------------------------
 * HELPERS
 * -------------------------------------------------------------
 */

@Composable
private fun ClearErrorOnEnter(
    clear: () -> Unit
) {
    LaunchedEffect(Unit) {
        clear()
    }
}

/**
 * Client-side Admin route guard.
 *
 * This prevents a non-admin user from creating the AdminViewModel
 * or opening one of the Admin screens.
 *
 * IMPORTANT:
 * This is a UX/access-control layer only.
 * Firestore Security Rules remain the actual security boundary.
 */
/**
 * Guards a BQ4/BQ6 destination and hands it the shared
 * [AdminInsightsViewModel].
 *
 * The ViewModel is scoped to the admin graph entry, not to the destination, so
 * moving between Purchases and Demographics keeps the same two Firestore
 * listeners instead of reopening them on every tab change.
 */
@Composable
private fun AdminInsightsHost(
    authState: AuthUiState,
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    factory: WhyNotViewModelFactory,
    content: @Composable (AdminInsightsUiState, AdminInsightsViewModel) -> Unit
) {
    AdminRouteGuard(
        isSignedIn = authState.session != null,
        isAdmin = authState.session?.isAdmin == true,
        navController = navController
    ) {
        val adminGraphEntry = remember(backStackEntry) {
            navController.getBackStackEntry(WhyNotRoutes.ADMIN_ROOT)
        }

        val insightsViewModel: AdminInsightsViewModel = viewModel(
            viewModelStoreOwner = adminGraphEntry,
            factory = factory
        )

        val insightsState by insightsViewModel
            .state
            .collectAsStateWithLifecycle()

        content(insightsState, insightsViewModel)
    }
}

@Composable
private fun AdminUserMetricsHost(
    authState: AuthUiState,
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    factory: WhyNotViewModelFactory,
    content: @Composable (AdminUserMetricsUiState) -> Unit
) {
    AdminRouteGuard(
        isSignedIn = authState.session != null,
        isAdmin = authState.session?.isAdmin == true,
        navController = navController
    ) {
        val adminGraphEntry = remember(backStackEntry) {
            navController.getBackStackEntry(WhyNotRoutes.ADMIN_ROOT)
        }

        val metricsViewModel: AdminUserMetricsViewModel = viewModel(
            viewModelStoreOwner = adminGraphEntry,
            factory = factory
        )

        val metricsState by metricsViewModel
            .state
            .collectAsStateWithLifecycle()

        content(metricsState)
    }
}

@Composable
private fun AdminRouteGuard(
    isSignedIn: Boolean,
    isAdmin: Boolean,
    navController: NavHostController,
    content: @Composable () -> Unit
) {

    if (!isAdmin) {

        LaunchedEffect(
            isSignedIn,
            isAdmin
        ) {

            val destination =
                if (isSignedIn) {
                    WhyNotRoutes.HOME
                } else {
                    WhyNotRoutes.LOGIN
                }

            navController.navigate(
                destination
            ) {

                popUpTo(
                    WhyNotRoutes.ADMIN_ROOT
                ) {
                    inclusive = true
                }

                launchSingleTop = true
            }
        }

        WhyNotLoading()
        return
    }

    content()
}

private fun NavHostController.navigateMain(
    route: String
) {

    navigate(route) {

        popUpTo(
            WhyNotRoutes.HOME
        ) {
            inclusive = false
        }

        launchSingleTop = true
    }
}

/**
 * Prevents repeatedly switching between BQ1 and BQ3 from
 * indefinitely growing the Admin back stack.
 */
private fun NavHostController.navigateAdmin(
    route: String
) {

    navigate(route) {

        popUpTo(
            WhyNotRoutes
                .ADMIN_SAVED_PRODUCTS
        ) {
            inclusive = false
        }

        launchSingleTop = true
    }
}
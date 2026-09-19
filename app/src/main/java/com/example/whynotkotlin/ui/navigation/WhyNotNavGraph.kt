package com.example.whynotkotlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.example.whynotkotlin.ui.screens.wishlists.ProductUi
import com.example.whynotkotlin.ui.screens.wishlists.WishlistDetailScreen
import com.example.whynotkotlin.ui.screens.wishlists.WishlistUi
import com.example.whynotkotlin.ui.screens.wishlists.WishlistsScreen

object WhyNotRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME = "home"
    const val WISHLISTS = "wishlists"
    const val ADD = "add"
    const val PURCHASES = "purchases"
    const val PROFILE = "profile"

    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"

    const val NEW_WISHLIST = "new_wishlist"
    const val WISHLIST_DETAIL = "wishlist_detail"
    const val NEW_PRODUCT = "new_product"
    const val PRODUCT_DETAIL = "product_detail"
}

@Composable
fun WhyNotNavigation() {
    val navController = rememberNavController()

    var selectedWishlist by remember {
        mutableStateOf(WishlistUi("Wishlist Category", 0))
    }

    var selectedProduct by remember {
        mutableStateOf(ProductUi("Product name", "\$100"))
    }

    NavHost(
        navController = navController,
        startDestination = WhyNotRoutes.LOGIN
    ) {

        composable(WhyNotRoutes.LOGIN) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(
                        WhyNotRoutes.HOME
                    ) {
                        popUpTo(
                            WhyNotRoutes.LOGIN
                        ) {
                            inclusive = true
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

        composable(WhyNotRoutes.REGISTER) {
            RegisterScreen(
                onCreateAccountClick = {
                    navController.navigate(
                        WhyNotRoutes.HOME
                    ) {
                        popUpTo(
                            WhyNotRoutes.LOGIN
                        ) {
                            inclusive = true
                        }
                    }
                },
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(WhyNotRoutes.HOME) {
            HomeScreen(
                onHomeClick = {},

                onWishlistsClick = {
                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                },

                onWishlistClick = { wishlistName ->
                    selectedWishlist = WishlistUi(
                        name = wishlistName,
                        itemCount = 6
                    )

                    navController.navigate(
                        WhyNotRoutes.WISHLIST_DETAIL
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

        composable(WhyNotRoutes.WISHLISTS) {
            WishlistsScreen(
                onWishlistClick = { wishlist ->
                    selectedWishlist = wishlist
                    navController.navigate(
                        WhyNotRoutes.WISHLIST_DETAIL
                    )
                },
                onNewWishlistClick = {
                    navController.navigate(
                        WhyNotRoutes.NEW_WISHLIST
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

        composable(WhyNotRoutes.NEW_WISHLIST) {
            NewWishlistScreen(
                onCancelClick = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.popBackStack()
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

        composable(WhyNotRoutes.WISHLIST_DETAIL) {
            WishlistDetailScreen(
                wishlistName = selectedWishlist.name,
                onProductClick = { product ->
                    selectedProduct = product
                    navController.navigate(
                        WhyNotRoutes.PRODUCT_DETAIL
                    )
                },
                onAddItemClick = {
                    navController.navigate(
                        WhyNotRoutes.NEW_PRODUCT
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

        composable(WhyNotRoutes.PRODUCT_DETAIL) {
            ProductDetailScreen(
                productName = selectedProduct.name,
                price = selectedProduct.price,
                originalPrice = selectedProduct.originalPrice,

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

        composable(WhyNotRoutes.ADD) {
            AddProductScreen(
                onAddManuallyClick = {
                    navController.navigate(
                        WhyNotRoutes.NEW_PRODUCT
                    )
                },
                onSaveItemClick = {
                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
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

        composable(WhyNotRoutes.NEW_PRODUCT) {
            NewProductScreen(
                onSaveItemClick = {
                    navController.popBackStack()
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

        composable(WhyNotRoutes.PURCHASES) {
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

        composable(WhyNotRoutes.PROFILE) {
            ProfileScreen(
                onEditProfileClick = {
                    navController.navigate(
                        WhyNotRoutes.EDIT_PROFILE
                    )
                },
                onChangePasswordClick = {
                    navController.navigate(
                        WhyNotRoutes.CHANGE_PASSWORD
                    )
                },
                onLogoutClick = {
                    navController.navigate(
                        WhyNotRoutes.LOGIN
                    ) {
                        popUpTo(
                            WhyNotRoutes.HOME
                        ) {
                            inclusive = true
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
                    navController.popBackStack()
                },
                onSaveChangesClick = {
                    navController.popBackStack()
                },
                onChangePasswordClick = {
                    navController.navigate(
                        WhyNotRoutes.CHANGE_PASSWORD
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
                    navController.popBackStack()
                },

                onUpdatePasswordClick = {
                    navController.popBackStack(
                        WhyNotRoutes.PROFILE,
                        inclusive = false
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
    }
}

private fun NavHostController.navigateMain(
    route: String
) {
    navigate(route) {
        popUpTo(WhyNotRoutes.HOME) {
            inclusive = false
        }

        launchSingleTop = true
    }
}

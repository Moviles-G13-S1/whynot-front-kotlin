package com.example.whynotkotlin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whynotkotlin.core.di.AppDependencies
import com.example.whynotkotlin.core.di.WhyNotViewModelFactory
import com.example.whynotkotlin.features.authentication.application.AuthViewModel
import com.example.whynotkotlin.features.products.application.ProductViewModel
import com.example.whynotkotlin.features.profile.domain.UserProfileDraft
import com.example.whynotkotlin.features.wishlists.application.WishlistViewModel
import com.example.whynotkotlin.ui.components.WhyNotLoading
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

    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PASSWORD = "change_password"

    const val NEW_WISHLIST = "new_wishlist"
    const val WISHLIST_DETAIL = "wishlist_detail"
    const val NEW_PRODUCT = "new_product"
    const val PRODUCT_DETAIL = "product_detail"
}

@Composable
fun WhyNotNavigation(dependencies: AppDependencies) {
    val navController = rememberNavController()
    val factory = WhyNotViewModelFactory(dependencies)

    // Created here so every destination shares one instance of each, which also
    // means one Firestore listener per collection rather than one per screen.
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val wishlistViewModel: WishlistViewModel = viewModel(factory = factory)
    val productViewModel: ProductViewModel = viewModel(factory = factory)

    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val wishlistState by wishlistViewModel.state.collectAsStateWithLifecycle()
    val productState by productViewModel.state.collectAsStateWithLifecycle()

    if (authState.checkingSession) {
        // Firebase restores a persisted session asynchronously. Rendering the
        // login screen first would flash it for a user who is already signed in.
        WhyNotLoading()
        return
    }

    val startDestination = if (authState.session == null) {
        WhyNotRoutes.LOGIN
    } else {
        WhyNotRoutes.HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(WhyNotRoutes.LOGIN) {
            ClearErrorOnEnter(authViewModel::clearError)

            LoginScreen(
                submitting = authState.submitting,
                errorMessage = authState.errorMessage,
                onSubmit = { email, password ->
                    authViewModel.signIn(email, password) {
                        navController.navigate(WhyNotRoutes.HOME) {
                            popUpTo(WhyNotRoutes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(WhyNotRoutes.REGISTER)
                }
            )
        }

        composable(WhyNotRoutes.REGISTER) {
            ClearErrorOnEnter(authViewModel::clearError)

            RegisterScreen(
                submitting = authState.submitting,
                errorMessage = authState.errorMessage,
                onSubmit = { name, email, password, gender, age, categoryId, cityId ->
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        draft = UserProfileDraft(
                            name = name,
                            gender = gender,
                            age = age.trim().toIntOrNull() ?: 0,
                            preferredCategoryId = categoryId,
                            cityId = cityId
                        )
                    ) {
                        navController.navigate(WhyNotRoutes.HOME) {
                            popUpTo(WhyNotRoutes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onBackToLoginClick = { navController.popBackStack() }
            )
        }

        composable(WhyNotRoutes.HOME) {
            // Still on sample data. Wiring this to WishlistViewModel /
            // ProductViewModel / ProfileViewModel belongs to the Home owner.
            HomeScreen(
                onHomeClick = {},
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onWishlistClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.WISHLISTS) {
            WishlistsScreen(
                state = wishlistState,
                onWishlistClick = { wishlistId ->
                    wishlistViewModel.selectWishlist(wishlistId)
                    navController.navigate(WhyNotRoutes.WISHLIST_DETAIL)
                },
                onNewWishlistClick = {
                    wishlistViewModel.clearError()
                    navController.navigate(WhyNotRoutes.NEW_WISHLIST)
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = {},
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.NEW_WISHLIST) {
            NewWishlistScreen(
                state = wishlistState,
                onSave = { categoryId, imageUrl ->
                    wishlistViewModel.createWishlist(categoryId, imageUrl) {
                        navController.popBackStack()
                    }
                },
                onCancelClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.WISHLIST_DETAIL) {
            WishlistDetailScreen(
                wishlistName = wishlistState.selected?.categoryName ?: "Wishlist",
                products = wishlistState.selectedWishlistId
                    ?.let { productState.forWishlist(it) }
                    .orEmpty(),
                errorMessage = productState.errorMessage,
                onProductClick = { productId ->
                    productViewModel.selectProduct(productId)
                    navController.navigate(WhyNotRoutes.PRODUCT_DETAIL)
                },
                onAddItemClick = {
                    productViewModel.clearError()
                    navController.navigate(WhyNotRoutes.NEW_PRODUCT)
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.PRODUCT_DETAIL) {
            ProductDetailScreen(
                product = productState.selected,
                errorMessage = productState.errorMessage,
                onMarkPurchased = {
                    productState.selected?.let(productViewModel::markPurchased)
                },
                onDeleteClick = {
                    productState.selected?.let { product ->
                        productViewModel.deleteProduct(product.id) {
                            navController.popBackStack()
                        }
                    }
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.ADD) {
            AddProductScreen(
                onContinueWithLink = { link ->
                    productViewModel.setPendingProductUrl(link)
                    navController.navigate(WhyNotRoutes.NEW_PRODUCT)
                },
                onAddManuallyClick = {
                    productViewModel.setPendingProductUrl("")
                    navController.navigate(WhyNotRoutes.NEW_PRODUCT)
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = {},
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.NEW_PRODUCT) {
            NewProductScreen(
                wishlists = wishlistState.summaries,
                prefilledProductUrl = productState.pendingProductUrl,
                saving = productState.saving,
                errorMessage = productState.errorMessage,
                onSave = { wishlistId, name, brand, price, imageUrl, productUrl ->
                    productViewModel.createProduct(
                        wishlistId = wishlistId,
                        name = name,
                        brand = brand,
                        price = price,
                        imageUrl = imageUrl,
                        productUrl = productUrl
                    ) {
                        navController.popBackStack()
                    }
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.PURCHASES) {
            // Still on sample data. The purchased products are already available
            // through ProductViewModel.state.purchased.
            PurchasesScreen(
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = {},
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.PROFILE) {
            // Still on sample data. ProfileViewModel already exposes the real
            // users/{uid} document; only sign-out is wired here because it
            // belongs to the session.
            ProfileScreen(
                onEditProfileClick = {
                    navController.navigate(WhyNotRoutes.EDIT_PROFILE)
                },
                onChangePasswordClick = {
                    navController.navigate(WhyNotRoutes.CHANGE_PASSWORD)
                },
                onLogoutClick = {
                    authViewModel.signOut {
                        navController.navigate(WhyNotRoutes.LOGIN) {
                            popUpTo(WhyNotRoutes.HOME) { inclusive = true }
                        }
                    }
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = {}
            )
        }

        composable(WhyNotRoutes.EDIT_PROFILE) {
            // Still on sample data. ProfileViewModel.updateProfile() already
            // performs the real write.
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaveChangesClick = { navController.popBackStack() },
                onChangePasswordClick = {
                    navController.navigate(WhyNotRoutes.CHANGE_PASSWORD)
                },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }

        composable(WhyNotRoutes.CHANGE_PASSWORD) {
            // Still on sample data. AuthViewModel.changePassword() already
            // reauthenticates and updates the real credential.
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() },
                onUpdatePasswordClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateMain(WhyNotRoutes.HOME) },
                onWishlistsClick = { navController.navigateMain(WhyNotRoutes.WISHLISTS) },
                onAddClick = { navController.navigateMain(WhyNotRoutes.ADD) },
                onPurchasesClick = { navController.navigateMain(WhyNotRoutes.PURCHASES) },
                onProfileClick = { navController.navigateMain(WhyNotRoutes.PROFILE) }
            )
        }
    }
}

/** Drops a stale message so a screen never opens showing the previous failure. */
@Composable
private fun ClearErrorOnEnter(clear: () -> Unit) {
    LaunchedEffect(Unit) { clear() }
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

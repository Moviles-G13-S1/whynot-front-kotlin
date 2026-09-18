package com.example.whynotkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.whynotkotlin.ui.screens.auth.LoginScreen
import com.example.whynotkotlin.ui.screens.auth.RegisterScreen
import com.example.whynotkotlin.ui.screens.profile.ChangePasswordScreen
import com.example.whynotkotlin.ui.screens.profile.EditProfileScreen
import com.example.whynotkotlin.ui.screens.products.NewProductScreen
import com.example.whynotkotlin.ui.screens.products.ProductDetailScreen
import com.example.whynotkotlin.ui.screens.profile.ProfileScreen
import com.example.whynotkotlin.ui.screens.wishlists.NewWishlistScreen
import com.example.whynotkotlin.ui.screens.wishlists.ProductUi
import com.example.whynotkotlin.ui.screens.wishlists.WishlistDetailScreen
import com.example.whynotkotlin.ui.screens.wishlists.WishlistUi
import com.example.whynotkotlin.ui.screens.wishlists.WishlistsScreen
import com.example.whynotkotlin.ui.theme.WhynotkotlinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            WhynotkotlinTheme {
                WhyNotApp()
            }
        }
    }
}

private enum class Screen {
    LOGIN,
    REGISTER,
    PROFILE,
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    WISHLISTS,
    NEW_WISHLIST,
    WISHLIST_DETAIL,
    NEW_PRODUCT,
    PRODUCT_DETAIL
}

@Composable
fun WhyNotApp() {

    var currentScreen by remember {
        mutableStateOf(Screen.LOGIN)
    }

    var selectedWishlist by remember {
        mutableStateOf(WishlistUi("Wishlist Category", 0))
    }

    var selectedProduct by remember {
        mutableStateOf(ProductUi("Product name", "\$100"))
    }

    when (currentScreen) {

        Screen.LOGIN -> {
            LoginScreen(
                onLoginClick = {
                    currentScreen = Screen.PROFILE
                },
                onCreateAccountClick = {
                    currentScreen = Screen.REGISTER
                }
            )
        }

        Screen.REGISTER -> {
            RegisterScreen(
                onCreateAccountClick = {
                    currentScreen = Screen.PROFILE
                },
                onBackToLoginClick = {
                    currentScreen = Screen.LOGIN
                }
            )
        }

        Screen.PROFILE -> {
            ProfileScreen(
                onEditProfileClick = {
                    currentScreen = Screen.EDIT_PROFILE
                },
                onChangePasswordClick = {
                    currentScreen = Screen.CHANGE_PASSWORD
                },
                onLogoutClick = {
                    currentScreen = Screen.LOGIN
                },
                onWishlistsClick = {
                    currentScreen = Screen.WISHLISTS
                },
                onAddClick = {
                    currentScreen = Screen.NEW_PRODUCT
                }
            )
        }

        Screen.EDIT_PROFILE -> {
            EditProfileScreen(
                onBackClick = {
                    currentScreen = Screen.PROFILE
                },
                onSaveChangesClick = {
                    currentScreen = Screen.PROFILE
                },
                onChangePasswordClick = {
                    currentScreen = Screen.CHANGE_PASSWORD
                }
            )
        }

        Screen.CHANGE_PASSWORD -> {
            ChangePasswordScreen(
                onBackClick = {
                    currentScreen = Screen.EDIT_PROFILE
                },
                onUpdatePasswordClick = {
                    currentScreen = Screen.PROFILE
                }
            )
        }

        Screen.WISHLISTS -> {
            WishlistsScreen(
                onWishlistClick = { wishlist ->
                    selectedWishlist = wishlist
                    currentScreen = Screen.WISHLIST_DETAIL
                },
                onNewWishlistClick = {
                    currentScreen = Screen.NEW_WISHLIST
                },
                onProfileClick = {
                    currentScreen = Screen.PROFILE
                },
                onAddClick = {
                    currentScreen = Screen.NEW_PRODUCT
                }
            )
        }

        Screen.NEW_WISHLIST -> {
            NewWishlistScreen(
                onCancelClick = {
                    currentScreen = Screen.WISHLISTS
                },
                onSaveClick = {
                    currentScreen = Screen.WISHLISTS
                },
                onProfileClick = {
                    currentScreen = Screen.PROFILE
                },
                onWishlistsClick = {
                    currentScreen = Screen.WISHLISTS
                }
            )
        }

        Screen.WISHLIST_DETAIL -> {
            WishlistDetailScreen(
                wishlistName = selectedWishlist.name,
                onProductClick = { product ->
                    selectedProduct = product
                    currentScreen = Screen.PRODUCT_DETAIL
                },
                onAddItemClick = {
                    currentScreen = Screen.NEW_PRODUCT
                },
                onProfileClick = {
                    currentScreen = Screen.PROFILE
                },
                onAddClick = {
                    currentScreen = Screen.NEW_PRODUCT
                },
                onWishlistsClick = {
                    currentScreen = Screen.WISHLISTS
                }
            )
        }

        Screen.NEW_PRODUCT -> {
            NewProductScreen(
                onSaveItemClick = {
                    currentScreen = Screen.WISHLISTS
                },
                onProfileClick = {
                    currentScreen = Screen.PROFILE
                },
                onWishlistsClick = {
                    currentScreen = Screen.WISHLISTS
                }
            )
        }

        Screen.PRODUCT_DETAIL -> {
            ProductDetailScreen(
                productName = selectedProduct.name,
                price = selectedProduct.price,
                originalPrice = selectedProduct.originalPrice,
                onProfileClick = {
                    currentScreen = Screen.PROFILE
                },
                onAddClick = {
                    currentScreen = Screen.NEW_PRODUCT
                },
                onWishlistsClick = {
                    currentScreen = Screen.WISHLISTS
                }
            )
        }
    }
}
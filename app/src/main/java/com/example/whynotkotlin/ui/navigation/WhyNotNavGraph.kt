package com.example.whynotkotlin.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.screens.auth.LoginScreen
import com.example.whynotkotlin.ui.screens.auth.RegisterScreen
import com.example.whynotkotlin.ui.screens.home.HomeScreen
import com.example.whynotkotlin.ui.screens.profile.ChangePasswordScreen
import com.example.whynotkotlin.ui.screens.profile.EditProfileScreen
import com.example.whynotkotlin.ui.screens.profile.ProfileScreen
import com.example.whynotkotlin.ui.screens.purchases.PurchasesScreen
import com.example.whynotkotlin.ui.theme.WhyNotGray

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
}

@Composable
fun WhyNotNavigation() {
    val navController = rememberNavController()

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
         * PERSON 2 HAS NOT PUSHED YET.
         *
         * These two temporary destinations keep
         * the bottom navigation functional.
         *
         * Replace them with Person 2's screens later.
         */

        composable(WhyNotRoutes.WISHLISTS) {
            PendingPersonTwoScreen(
                title = "Wishlists",
                navController = navController
            )
        }

        composable(WhyNotRoutes.ADD) {
            PendingPersonTwoScreen(
                title = "Add",
                navController = navController
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
        launchSingleTop = true
        restoreState = true

        popUpTo(WhyNotRoutes.HOME) {
            saveState = true
        }
    }
}

@Composable
private fun PendingPersonTwoScreen(
    title: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Pending Person 2 implementation",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )
        }

        WhyNotBottomBar(
            onHomeClick = {
                navController.navigateMain(
                    WhyNotRoutes.HOME
                )
            },

            onWishlistsClick = {
                if (title != "Wishlists") {
                    navController.navigateMain(
                        WhyNotRoutes.WISHLISTS
                    )
                }
            },

            onAddClick = {
                if (title != "Add") {
                    navController.navigateMain(
                        WhyNotRoutes.ADD
                    )
                }
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
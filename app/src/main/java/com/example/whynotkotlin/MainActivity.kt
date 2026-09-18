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
import com.example.whynotkotlin.ui.screens.profile.ProfileScreen
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
    CHANGE_PASSWORD
}

@Composable
fun WhyNotApp() {

    var currentScreen by remember {
        mutableStateOf(Screen.LOGIN)
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
    }
}
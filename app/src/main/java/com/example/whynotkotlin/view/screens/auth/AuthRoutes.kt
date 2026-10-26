package com.example.whynotkotlin.view.screens.auth

import androidx.compose.runtime.Composable
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.components.rememberIsOnline
import com.example.whynotkotlin.view.components.rememberSubmission
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

@Composable
fun LoginRoute(navigator: WhyNotNavigator) {
    val authController = LocalAppDependencies.current.authController
    val submission = rememberSubmission()

    LoginScreen(
        isOnline = rememberIsOnline(),
        submitting = submission.running,
        errorMessage = submission.errorMessage,
        onSubmit = { email, password ->
            submission.run(onSuccess = navigator::toHomeAfterSignIn) {
                authController.signIn(email, password)
            }
        },
        onCreateAccountClick = navigator::toRegister
    )
}

@Composable
fun RegisterRoute(navigator: WhyNotNavigator) {
    val authController = LocalAppDependencies.current.authController
    val submission = rememberSubmission()

    RegisterScreen(
        isOnline = rememberIsOnline(),
        submitting = submission.running,
        errorMessage = submission.errorMessage,
        onSubmit = { name, email, password, gender, age, categoryId, cityId ->
            submission.run(onSuccess = navigator::toHomeAfterSignIn) {
                authController.signUp(
                    name = name,
                    email = email,
                    password = password,
                    gender = gender,
                    age = age,
                    preferredCategoryId = categoryId,
                    cityId = cityId
                )
            }
        },
        onBackToLoginClick = navigator::back
    )
}

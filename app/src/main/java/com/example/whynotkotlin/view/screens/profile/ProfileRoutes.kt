package com.example.whynotkotlin.view.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.readableMessage
import com.example.whynotkotlin.view.components.rememberSubmission
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

@Composable
fun ProfileRoute(navigator: WhyNotNavigator) {
    val dependencies = LocalAppDependencies.current
    val profileController = dependencies.profileController
    val authController = dependencies.authController
    val session by authController.session.collectAsStateWithLifecycle()
    val profileResult by profileController.profile.collectAsStateWithLifecycle()
    val categoriesResult by profileController.categories.collectAsStateWithLifecycle()
    val signOut = rememberSubmission()

    val profile = profileResult?.getOrNull()

    ProfileScreen(
        profile = profile,
        preferredCategoryName =
            profileController.preferredCategoryName(
                profile,
                categoriesResult?.getOrNull().orEmpty()
            ),
        loading = profileResult == null,
        errorMessage = profileResult?.exceptionOrNull()?.readableMessage()
            ?: categoriesResult?.exceptionOrNull()?.readableMessage(),
        isAdmin = session?.getOrNull()?.isAdmin == true,
        onAdminClick = navigator::toAdmin,
        onEditProfileClick = navigator::toEditProfile,
        onChangePasswordClick = navigator::toChangePassword,
        onLogoutClick = {
            signOut.run(onSuccess = navigator::toLoginAfterSignOut) {
                authController.signOut()
            }
        },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = {}
    )
}

@Composable
fun EditProfileRoute(navigator: WhyNotNavigator) {
    val profileController = LocalAppDependencies.current.profileController
    val profileResult by profileController.profile.collectAsStateWithLifecycle()
    val categoriesResult by profileController.categories.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    val profile = profileResult?.getOrNull()
    val categories = categoriesResult?.getOrNull().orEmpty()

    EditProfileScreen(
        profile = profile,
        categories = categories,
        preferredCategoryName = profileController.preferredCategoryName(profile, categories),
        loading = profileResult == null,
        saving = submission.running,
        errorMessage = submission.errorMessage
            ?: profileResult?.exceptionOrNull()?.readableMessage()
            ?: categoriesResult?.exceptionOrNull()?.readableMessage(),
        onBackClick = navigator::back,
        onSaveChangesClick = { name, gender, age, categoryId ->
            submission.run(onSuccess = navigator::back) {
                profileController.updateProfile(profile?.uid, name, gender, age, categoryId)
            }
        },
        onChangePasswordClick = navigator::toChangePassword,
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

@Composable
fun ChangePasswordRoute(navigator: WhyNotNavigator) {
    val authController = LocalAppDependencies.current.authController
    val submission = rememberSubmission()

    ChangePasswordScreen(
        submitting = submission.running,
        errorMessage = submission.errorMessage,
        onBackClick = navigator::back,
        onUpdatePasswordClick = { current, new, confirm ->
            submission.run(onSuccess = navigator::back) {
                authController.changePassword(current, new, confirm)
            }
        },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

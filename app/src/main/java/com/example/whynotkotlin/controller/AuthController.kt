package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AuthUser
import com.example.whynotkotlin.model.UserProfileDraft
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Session and credential intents.
 *
 * Sign-up owns both the Firebase account and the `users/{uid}` document because
 * the user experiences them as one action: an account without its profile
 * cannot pass the Firestore rules on any later write.
 */
class AuthController(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    scope: CoroutineScope
) {

    /**
     * The signed-in user, or a success holding null while signed out.
     *
     * Null itself means the session has not been resolved yet.
     */
    val session: StateFlow<Result<AuthUser?>?> =
        authRepository.observeSession()
            .map { Result.success(it) }
            .stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            throw ValidationException("Enter your email and password")
        }

        authRepository.signIn(email, password)
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        cityId: String
    ) {
        val draft = UserProfileDraft(
            name = name,
            gender = gender,
            age = age.trim().toIntOrNull() ?: 0,
            preferredCategoryId = preferredCategoryId,
            cityId = cityId
        )

        validateSignUp(email, password, draft)?.let { throw ValidationException(it) }

        val user = authRepository.signUp(email, password)
        userRepository.createProfile(user.uid, user.email, draft)
    }

    suspend fun signOut() {
        authRepository.signOut()
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val validationError = when {
            currentPassword.isBlank() -> "Enter your current password"
            newPassword.length < MIN_PASSWORD_LENGTH ->
                "The new password needs at least $MIN_PASSWORD_LENGTH characters"

            newPassword != confirmPassword -> "The new passwords do not match"
            else -> null
        }

        validationError?.let { throw ValidationException(it) }

        authRepository.changePassword(currentPassword, newPassword)
    }

    private fun validateSignUp(
        email: String,
        password: String,
        draft: UserProfileDraft
    ): String? = when {
        draft.name.isBlank() -> "Enter your name"
        email.isBlank() -> "Enter your email"
        password.length < MIN_PASSWORD_LENGTH ->
            "The password needs at least $MIN_PASSWORD_LENGTH characters"

        draft.gender.isBlank() -> "Select a gender"
        draft.age <= 0 -> "Enter a valid age"
        draft.preferredCategoryId.isBlank() -> "Select a preferred category"
        draft.cityId.isBlank() -> "Select a city"
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

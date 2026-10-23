package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AuthUser
import com.example.whynotkotlin.model.OnboardingStep
import com.example.whynotkotlin.model.PasswordPolicy
import com.example.whynotkotlin.model.UserProfileDraft
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.OnboardingRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val onboardingRepository: OnboardingRepository,
    private val scope: CoroutineScope
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

        val user = authRepository.signUp(email.trim(), password)
        userRepository.createProfile(user.uid, user.email, draft)

        recordOnboardingStep(user.uid, OnboardingStep.SIGN_UP_COMPLETED)
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
            else -> PasswordPolicy.firstError(newPassword)?.replace("The password", "The new password")
                ?: when {
                    newPassword == currentPassword ->
                        "The new password must be different from the current one"

                    newPassword != confirmPassword -> "The new passwords do not match"
                    else -> null
                }
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
        !EMAIL_PATTERN.matches(email.trim()) -> "Enter a valid email, such as name@example.com"
        else -> PasswordPolicy.firstError(password) ?: when {
            draft.gender.isBlank() -> "Select a gender"
            draft.age <= 0 -> "Enter a valid age"
            draft.preferredCategoryId.isBlank() -> "Select a preferred category"
            draft.cityId.isBlank() -> "Select a city"
            else -> null
        }
    }

    /**
     * Records the step in the background. It is analytics only, so it never
     * delays sign-up and a failure to record it is ignored.
     */
    private fun recordOnboardingStep(userId: String, step: OnboardingStep) {
        scope.launch {
            try {
                onboardingRepository.recordStep(userId, step)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Losing one analytics event must not surface as a sign-up error.
            }
        }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AuthUser
import com.example.whynotkotlin.model.Genders
import com.example.whynotkotlin.model.InputRules
import com.example.whynotkotlin.model.OnboardingStep
import com.example.whynotkotlin.model.PasswordPolicy
import com.example.whynotkotlin.model.UserProfileDraft
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.OnboardingRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import com.example.whynotkotlin.model.repositories.requireOnline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Session and credential intents.
 *
 * Sign-up owns both the Firebase account and the `users/{uid}` document because
 * the user experiences them as one action: an account without its profile
 * cannot pass the Firestore rules on any later write.
 *
 * Every field is checked with [InputRules] and [PasswordPolicy], the same rules
 * the forms use to show errors under each field, before anything is sent.
 * Emails are stored in lowercase.
 */
class AuthController(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val onboardingRepository: OnboardingRepository,
    private val scope: CoroutineScope,
    private val connectivity: ConnectivityRepository? = null,
    private val onSignedOut: () -> Unit = {}
) {

    /**
     * The signed-in user, or a success holding null while signed out.
     *
     * Null itself means the session has not been resolved yet.
     */
    val session: StateFlow<Result<AuthUser?>?> =
        authRepository.observeSession()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            throw ValidationException("Enter your email and password")
        }

        connectivity.requireOnline(SIGN_IN_OFFLINE)
        authRepository.signIn(email.normalizedEmail(), password)
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        cityId: String
    ) {
        signUpError(name, email, password, confirmPassword, gender, age, preferredCategoryId, cityId)
            ?.let { throw ValidationException(it) }
        connectivity.requireOnline(SIGN_UP_OFFLINE)

        val draft = UserProfileDraft(
            name = name.trim(),
            gender = gender,
            age = age.trim().toInt(),
            preferredCategoryId = preferredCategoryId,
            cityId = cityId
        )

        val user = authRepository.signUp(email.normalizedEmail(), password)

        try {
            userRepository.createProfile(user.uid, user.email.normalizedEmail(), draft)
        } catch (error: Throwable) {
            // The account and its profile are created in two steps. When the
            // second fails (network, rules) the account is removed again, so
            // the email is not left taken by an account without a profile and
            // the user can simply try again.
            withContext(NonCancellable) { deleteAccountQuietly() }
            throw error
        }

        recordOnboardingStep(user.uid, OnboardingStep.SIGN_UP_COMPLETED)
    }

    private suspend fun deleteAccountQuietly() {
        try {
            authRepository.deleteCurrentUser()
        } catch (_: Exception) {
            // Nothing else can be done here; the original error is what the user sees.
        }
    }

    suspend fun signOut() {
        authRepository.signOut()
        onSignedOut()
    }

    /** [name] is the profile name, so the new password cannot include it. */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        name: String = ""
    ) {
        val email = authRepository.currentUser()?.email.orEmpty()

        val validationError = when {
            currentPassword.isBlank() -> "Enter your current password"
            newPassword.isEmpty() -> "Enter a new password"
            else -> PasswordPolicy.firstError(newPassword, email, name)
                ?.replace("The password", "The new password")
                ?: when {
                    newPassword == currentPassword ->
                        "The new password must be different from the current one"

                    newPassword != confirmPassword -> "The new passwords do not match"
                    else -> null
                }
        }

        validationError?.let { throw ValidationException(it) }
        connectivity.requireOnline(CHANGE_PASSWORD_OFFLINE)

        authRepository.changePassword(currentPassword, newPassword)
    }

    /** The first problem in the sign-up form, in the order the form shows its fields. */
    private fun signUpError(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        cityId: String
    ): String? =
        InputRules.name(name)
            ?: InputRules.email(email)
            ?: (if (gender !in Genders.all) "Select a gender" else null)
            ?: InputRules.age(age)
            ?: (if (password.isEmpty()) "Enter a password" else null)
            ?: PasswordPolicy.firstError(password, email, name)
            ?: (if (confirmPassword != password) "The passwords do not match" else null)
            ?: (if (preferredCategoryId.isBlank()) "Select a preferred category" else null)
            ?: (if (cityId.isBlank()) "Select a city" else null)

    private fun String.normalizedEmail(): String = trim().lowercase()

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
        const val SIGN_IN_OFFLINE =
            "No connection. Signing in needs internet; your email is kept, try again when you are back online."
        const val SIGN_UP_OFFLINE =
            "No connection. Creating an account needs internet; your answers are kept, try again when you are back online."
        const val CHANGE_PASSWORD_OFFLINE =
            "No connection. Changing your password needs internet; try again when you are back online."
    }
}

package com.example.whynotkotlin.features.authentication.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthUser
import com.example.whynotkotlin.features.profile.domain.UserProfileDraft
import com.example.whynotkotlin.features.profile.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val session: AuthUser? = null,
    val checkingSession: Boolean = true,
    val submitting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Session and credential intents.
 *
 * Sign-up owns both the Firebase account and the `users/{uid}` document because
 * the user experiences them as one action: an account without its profile
 * cannot pass the Firestore rules on any later write.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession().collect { user ->
                _state.value = _state.value.copy(
                    session = user,
                    checkingSession = false
                )
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(
                errorMessage = "Enter your email and password"
            )
            return
        }

        submit(onSuccess) {
            authRepository.signIn(email, password)
        }
    }

    fun signUp(
        email: String,
        password: String,
        draft: UserProfileDraft,
        onSuccess: () -> Unit
    ) {
        val validationError = validateSignUp(email, password, draft)

        if (validationError != null) {
            _state.value = _state.value.copy(errorMessage = validationError)
            return
        }

        submit(onSuccess) {
            val user = authRepository.signUp(email, password)
            userRepository.createProfile(user.uid, user.email, draft)
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        submit(onSuccess) {
            authRepository.signOut()
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val validationError = when {
            currentPassword.isBlank() -> "Enter your current password"
            newPassword.length < MIN_PASSWORD_LENGTH ->
                "The new password needs at least $MIN_PASSWORD_LENGTH characters"

            newPassword != confirmPassword -> "The new passwords do not match"
            else -> null
        }

        if (validationError != null) {
            _state.value = _state.value.copy(errorMessage = validationError)
            return
        }

        submit(onSuccess) {
            authRepository.changePassword(currentPassword, newPassword)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Unit) {
        _state.value = _state.value.copy(submitting = true, errorMessage = null)

        viewModelScope.launch {
            runCatching { block() }
                .onSuccess {
                    _state.value = _state.value.copy(submitting = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        submitting = false,
                        errorMessage = error.readableMessage()
                    )
                }
        }
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
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

/**
 * Firebase messages are developer-facing and sometimes leak internals, so the
 * common cases get a short message and everything else falls back to the raw
 * one rather than hiding what went wrong.
 */
internal fun Throwable.readableMessage(): String {
    val raw = message.orEmpty()

    return when {
        raw.contains("password is invalid", ignoreCase = true) ||
            raw.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            raw.contains("credential is incorrect", ignoreCase = true) ->
            "Wrong email or password"

        raw.contains("no user record", ignoreCase = true) ->
            "No account uses that email"

        raw.contains("email address is already in use", ignoreCase = true) ->
            "That email already has an account"

        raw.contains("badly formatted", ignoreCase = true) ->
            "That email address is not valid"

        raw.contains("PERMISSION_DENIED", ignoreCase = true) ->
            "The backend rejected this operation"

        raw.contains("network", ignoreCase = true) ->
            "No connection. Check your network and try again"

        raw.isBlank() -> "Something went wrong"
        else -> raw
    }
}

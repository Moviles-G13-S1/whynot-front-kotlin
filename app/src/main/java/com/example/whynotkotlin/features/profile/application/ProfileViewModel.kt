package com.example.whynotkotlin.features.profile.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.authentication.application.readableMessage
import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.profile.domain.AgeRange
import com.example.whynotkotlin.features.profile.domain.UserProfile
import com.example.whynotkotlin.features.profile.domain.UserProfileUpdate
import com.example.whynotkotlin.features.profile.domain.UserRepository
import com.example.whynotkotlin.features.wishlists.domain.Category
import com.example.whynotkotlin.features.wishlists.domain.WishlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val errorMessage: String? = null
) {
    /** The display name of the profile's preferred category, or its raw id. */
    val preferredCategoryName: String
        get() = categories.firstOrNull { it.id == profile?.preferredCategoryId }?.name
            ?: profile?.preferredCategoryId.orEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeSession()
                .flatMapLatest { user ->
                    if (user == null) flowOf(null)
                    else userRepository.observeProfile(user.uid)
                }
                .collect { profile ->
                    _state.value = _state.value.copy(profile = profile, loading = false)
                }
        }

        viewModelScope.launch {
            runCatching { wishlistRepository.getCategories() }
                .onSuccess { categories ->
                    _state.value = _state.value.copy(categories = categories)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        errorMessage = error.readableMessage()
                    )
                }
        }
    }

    fun updateProfile(
        name: String,
        gender: String,
        age: String,
        preferredCategoryId: String,
        onSuccess: () -> Unit
    ) {
        val uid = _state.value.profile?.uid

        if (uid == null) {
            _state.value = _state.value.copy(errorMessage = "No profile loaded yet")
            return
        }

        val parsedAge = age.trim().toIntOrNull()

        val validationError = when {
            name.isBlank() -> "Enter your name"
            gender.isBlank() -> "Select a gender"
            parsedAge == null -> "Enter a valid age"
            parsedAge < AgeRange.MIN || parsedAge > AgeRange.MAX ->
                "Age must be between ${AgeRange.MIN} and ${AgeRange.MAX}"

            preferredCategoryId.isBlank() -> "Select a preferred category"
            else -> null
        }

        if (validationError != null) {
            _state.value = _state.value.copy(errorMessage = validationError)
            return
        }

        _state.value = _state.value.copy(saving = true, errorMessage = null)

        viewModelScope.launch {
            runCatching {
                userRepository.updateProfile(
                    uid = uid,
                    update = UserProfileUpdate(
                        name = name,
                        gender = gender,
                        age = parsedAge,
                        preferredCategoryId = preferredCategoryId
                    )
                )
            }
                .onSuccess {
                    _state.value = _state.value.copy(saving = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        saving = false,
                        errorMessage = error.readableMessage()
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}

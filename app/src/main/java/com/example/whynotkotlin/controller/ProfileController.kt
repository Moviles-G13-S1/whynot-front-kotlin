package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AgeRange
import com.example.whynotkotlin.model.CanonicalCities
import com.example.whynotkotlin.model.Category
import com.example.whynotkotlin.model.UserProfile
import com.example.whynotkotlin.model.UserProfileUpdate
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.UserRepository
import com.example.whynotkotlin.model.repositories.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileController(
    authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
    scope: CoroutineScope
) {

    /**
     * The signed-in user's profile, or a success holding null while signed out.
     *
     * It goes back to null (loading) whenever a different user signs in.
     */
    val profile: StateFlow<Result<UserProfile?>?> =
        authRepository.observeSession()
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(Result.success(null))
                } else {
                    userRepository.observeProfile(user.uid)
                        .asResults()
                        .onStart<Result<UserProfile?>?> { emit(null) }
                }
            }
            .shareIn(scope)

    /** The category catalog, read once per session for the profile screens. */
    val categories: StateFlow<Result<List<Category>>?> =
        authRepository.observeSession()
            .transformLatest<_, Result<List<Category>>?> { user ->
                if (user == null) return@transformLatest

                val result = try {
                    Result.success(wishlistRepository.getCategories())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }

                emit(result)
            }
            .shareIn(scope)

    /** [uid] is the uid of the profile on screen; null when none is loaded. */
    suspend fun updateProfile(
        uid: String?,
        name: String,
        gender: String,
        age: String,
        preferredCategoryId: String
    ) {
        if (uid == null) throw ValidationException("No profile loaded yet")

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

        validationError?.let { throw ValidationException(it) }

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

    /** The display name of the profile's preferred category, or its raw id. */
    fun preferredCategoryName(profile: UserProfile?, categories: List<Category>): String =
        categories.firstOrNull { it.id == profile?.preferredCategoryId }?.name
            ?: profile?.preferredCategoryId.orEmpty()

    /** The display name of the profile's city, as the iOS profile shows it. */
    fun cityName(profile: UserProfile?): String {
        val cityId = profile?.cityId.orEmpty()
        if (cityId.isBlank()) return "Not selected"

        return CanonicalCities.all.firstOrNull { it.id == cityId }?.name
            ?: "City unavailable"
    }
}

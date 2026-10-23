package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AgeRange
import com.example.whynotkotlin.model.Genders
import com.example.whynotkotlin.model.UserProfileUpdate
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.UserRepository

class ProfileUpdateFacade(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend fun save(uid: String?, name: String, gender: String, age: String, categoryId: String) {
        val currentUid = authRepository.currentUser()?.uid
            ?: throw ValidationException("Your session ended. Sign in again.")
        if (uid == null) throw ValidationException("No profile loaded yet")
        if (uid != currentUid) throw ValidationException("Your session changed. Reopen your profile.")
        val parsedAge = age.trim().toIntOrNull()
        val error = when {
            name.isBlank() -> "Enter your name"
            gender !in Genders.all -> "Select a valid gender"
            parsedAge == null -> "Enter a valid age"
            parsedAge !in AgeRange.MIN..AgeRange.MAX ->
                "Age must be between ${AgeRange.MIN} and ${AgeRange.MAX}"
            categoryId.isBlank() -> "Select a preferred category"
            else -> null
        }
        if (error != null) throw ValidationException(error)
        userRepository.updateProfile(
            currentUid,
            UserProfileUpdate(
                name = name.trim(), gender = gender,
                age = parsedAge, preferredCategoryId = categoryId
            )
        )
    }
}

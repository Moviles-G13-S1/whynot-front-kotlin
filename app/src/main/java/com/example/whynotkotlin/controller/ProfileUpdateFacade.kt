package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Genders
import com.example.whynotkotlin.model.InputRules
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
        val error = InputRules.name(name)
            ?: (if (gender !in Genders.all) "Select a valid gender" else null)
            ?: InputRules.age(age)
            ?: (if (categoryId.isBlank()) "Select a preferred category" else null)
        if (error != null) throw ValidationException(error)
        userRepository.updateProfile(
            currentUid,
            UserProfileUpdate(
                name = name.trim(), gender = gender,
                age = age.trim().toInt(), preferredCategoryId = categoryId
            )
        )
    }
}

package com.example.whynotkotlin.features.profile.domain

import kotlinx.coroutines.flow.Flow

/**
 * Typed access to `users/{uid}`.
 *
 * The rules deny collection listing and client deletion, so neither is exposed.
 */
interface UserRepository {

    /** Emits the profile for [uid], or null while it does not exist yet. */
    fun observeProfile(uid: String): Flow<UserProfile?>

    suspend fun getProfile(uid: String): UserProfile?

    /**
     * Creates `users/{uid}`. [email] must be the authenticated email; the rules
     * reject any other value.
     */
    suspend fun createProfile(uid: String, email: String, draft: UserProfileDraft)

    suspend fun updateProfile(uid: String, update: UserProfileUpdate)
}

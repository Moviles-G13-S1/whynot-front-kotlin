package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.AuthUser

import kotlinx.coroutines.flow.Flow

/**
 * Session and credential operations.
 *
 * Authorization comes from the signed Firebase ID token, never from a field in
 * `users/{uid}`, so [currentUser] is the only source of the admin claim.
 */
interface AuthRepository {

    /** Emits the current user, or null while signed out. */
    fun observeSession(): Flow<AuthUser?>

    fun currentUser(): AuthUser?

    suspend fun signIn(email: String, password: String): AuthUser

    suspend fun signUp(email: String, password: String): AuthUser

    suspend fun signOut()

    /** Deletes the signed-in account, to undo a sign-up whose profile could not be created. */
    suspend fun deleteCurrentUser()

    suspend fun changePassword(currentPassword: String, newPassword: String)

    /** Forces a token refresh so a newly granted `admin` claim is visible. */
    suspend fun refreshClaims(): AuthUser?
}

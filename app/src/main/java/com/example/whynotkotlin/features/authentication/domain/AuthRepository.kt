package com.example.whynotkotlin.features.authentication.domain

import kotlinx.coroutines.flow.Flow

/** The signed-in Firebase user, reduced to what the client needs. */
data class AuthUser(
    val uid: String,
    val email: String,
    val isAdmin: Boolean = false
)

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

    suspend fun changePassword(currentPassword: String, newPassword: String)

    /** Forces a token refresh so a newly granted `admin` claim is visible. */
    suspend fun refreshClaims(): AuthUser?
}

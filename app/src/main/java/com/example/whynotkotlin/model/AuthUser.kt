package com.example.whynotkotlin.model

/** The signed-in Firebase user, reduced to what the client needs. */
data class AuthUser(
    val uid: String,
    val email: String,
    val isAdmin: Boolean = false
)

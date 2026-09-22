package com.example.whynotkotlin.features.profile.domain

import java.util.Date

/**
 * A `users/{uid}` document. The document id is always the authenticated UID.
 *
 * `email` is immutable and must equal the authenticated email, so it is not
 * part of [UserProfileUpdate].
 */
data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val gender: String,
    val age: Int,
    val preferredCategoryId: String,
    val createdAt: Date?,
    val updatedAt: Date?
)

/** The fields a client supplies when creating its profile after sign-up. */
data class UserProfileDraft(
    val name: String,
    val gender: String,
    val age: Int,
    val preferredCategoryId: String
)

/** The only fields the rules allow a client to change. */
data class UserProfileUpdate(
    val name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val preferredCategoryId: String? = null
)

/**
 * The values the backend accepts for `users.gender`.
 *
 * The rules reject anything else, so the UI must offer exactly these.
 */
object Genders {
    const val FEMALE = "Female"
    const val MALE = "Male"
    const val OTHER = "Other"

    val all = listOf(FEMALE, MALE, OTHER)
}

/** The inclusive age range the rules accept. */
object AgeRange {
    const val MIN = 13
    const val MAX = 120
}

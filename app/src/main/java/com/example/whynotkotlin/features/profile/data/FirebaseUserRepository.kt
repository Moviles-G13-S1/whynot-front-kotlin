package com.example.whynotkotlin.features.profile.data

import com.example.whynotkotlin.features.profile.domain.UserProfile
import com.example.whynotkotlin.features.profile.domain.UserProfileDraft
import com.example.whynotkotlin.features.profile.domain.UserProfileUpdate
import com.example.whynotkotlin.features.profile.domain.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val USERS = "users"

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun observeProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = firestore.collection(USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.toUserProfile())
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getProfile(uid: String): UserProfile? =
        firestore.collection(USERS).document(uid).get().await().toUserProfile()

    override suspend fun createProfile(
        uid: String,
        email: String,
        draft: UserProfileDraft
    ) {
        // The rules require every field, `email` to equal the authenticated
        // email, and both timestamps to equal the server time of this request.
        // `cityId` became mandatory with the city catalog and must reference an
        // existing `cities` document.
        val data = mapOf(
            "name" to draft.name.trim(),
            "email" to email.trim(),
            "gender" to draft.gender,
            "age" to draft.age,
            "preferredCategoryId" to draft.preferredCategoryId,
            "cityId" to draft.cityId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(USERS).document(uid).set(data).await()
    }

    override suspend fun updateProfile(uid: String, update: UserProfileUpdate) {
        val data = mutableMapOf<String, Any>()

        update.name?.let { data["name"] = it.trim() }
        update.gender?.let { data["gender"] = it }
        update.age?.let { data["age"] = it }
        update.preferredCategoryId?.let { data["preferredCategoryId"] = it }
        update.cityId?.let { data["cityId"] = it }

        if (data.isEmpty()) return

        data["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection(USERS).document(uid).update(data).await()
    }
}

private fun DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null

    return UserProfile(
        uid = id,
        name = getString("name").orEmpty(),
        email = getString("email").orEmpty(),
        gender = getString("gender").orEmpty(),
        age = getLong("age")?.toInt() ?: 0,
        preferredCategoryId = getString("preferredCategoryId").orEmpty(),
        cityId = getString("cityId").orEmpty(),
        createdAt = getTimestamp("createdAt")?.toDate(),
        updatedAt = getTimestamp("updatedAt")?.toDate()
    )
}

package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.OnboardingStep
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val ONBOARDING_EVENTS = "onboardingEvents"

/** Tells Android onboarding apart from iOS in `onboardingEvents`. */
private const val PLATFORM = "android"

class FirebaseOnboardingRepository(
    private val firestore: FirebaseFirestore
) : OnboardingRepository {

    override suspend fun recordStep(userId: String, step: OnboardingStep) {
        // The rules accept exactly these keys, require the id to be
        // `{userId}_{step}` and allow create only, so a step counts once.
        val data = hashMapOf(
            "userId" to userId,
            "platform" to PLATFORM,
            "step" to step.code,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(ONBOARDING_EVENTS)
            .document("${userId}_${step.code}")
            .set(data)
            .await()
    }
}

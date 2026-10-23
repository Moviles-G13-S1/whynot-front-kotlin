package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.OnboardingStep

/** Write-only access to `onboardingEvents`, used for the onboarding funnel. */
interface OnboardingRepository {

    /**
     * Records that [userId] reached [step]. A step is stored once per user, so
     * recording it again is rejected by the backend.
     */
    suspend fun recordStep(userId: String, step: OnboardingStep)
}

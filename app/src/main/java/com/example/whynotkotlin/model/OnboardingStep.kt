package com.example.whynotkotlin.model

/**
 * A milestone of a new user's first steps in the app (BQ Type 2).
 *
 * Each step is recorded once per user in `onboardingEvents`, and the admin
 * analytics turns them into a funnel: sign up, first wishlist, first product.
 * The codes are shared with iOS, so both platforms land in the same funnel.
 */
enum class OnboardingStep(val code: String) {
    SIGN_UP_COMPLETED("sign_up_completed"),
    FIRST_WISHLIST_CREATED("first_wishlist_created")
}

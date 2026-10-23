package com.example.whynotkotlin.model

/** One requirement a new password must meet, with the text shown to the user. */
data class PasswordRule(
    val label: String,
    val errorMessage: String,
    val isMetBy: (String) -> Boolean
)

/**
 * The requirements for every new password, shared by sign-up and change
 * password so both forms show and enforce the same list.
 */
object PasswordPolicy {

    const val MIN_LENGTH = 8

    val rules: List<PasswordRule> = listOf(
        PasswordRule(
            label = "At least $MIN_LENGTH characters",
            errorMessage = "The password needs at least $MIN_LENGTH characters"
        ) { it.length >= MIN_LENGTH },

        PasswordRule(
            label = "One uppercase letter",
            errorMessage = "The password needs at least one uppercase letter"
        ) { password -> password.any { it.isUpperCase() } },

        PasswordRule(
            label = "One lowercase letter",
            errorMessage = "The password needs at least one lowercase letter"
        ) { password -> password.any { it.isLowerCase() } },

        PasswordRule(
            label = "One number",
            errorMessage = "The password needs at least one number"
        ) { password -> password.any { it.isDigit() } },

        PasswordRule(
            label = "No spaces",
            errorMessage = "The password cannot contain spaces"
        ) { password -> password.none { it.isWhitespace() } }
    )

    /** The message of the first requirement [password] misses, or null when it meets all. */
    fun firstError(password: String): String? =
        rules.firstOrNull { !it.isMetBy(password) }?.errorMessage

    fun isValid(password: String): Boolean = firstError(password) == null
}

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

    /** Shorter parts of a name or email, such as "Ana", are too common to forbid. */
    private const val MIN_PERSONAL_WORD_LENGTH = 4

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
        ) { password -> password.none { it.isWhitespace() } },

        PasswordRule(
            label = "No emojis",
            errorMessage = "The password cannot contain emojis"
        ) { password -> !InputRules.containsEmoji(password) }
    )

    /**
     * [rules] plus, once the user's [email] or [name] is known, not including
     * them, so the password cannot be guessed from the account itself.
     */
    fun rulesFor(email: String = "", name: String = ""): List<PasswordRule> {
        val personalWords = personalWords(email, name)
        if (personalWords.isEmpty()) return rules

        return rules + PasswordRule(
            label = "Does not include your name or email",
            errorMessage = "The password cannot include your name or email"
        ) { password -> personalWords.none { password.contains(it, ignoreCase = true) } }
    }

    /** The message of the first requirement [password] misses, or null when it meets all. */
    fun firstError(password: String, email: String = "", name: String = ""): String? =
        rulesFor(email, name).firstOrNull { !it.isMetBy(password) }?.errorMessage

    fun isValid(password: String, email: String = "", name: String = ""): Boolean =
        firstError(password, email, name) == null

    /** "maria.lopez", "maria" and "lopez" from maria.lopez@mail.com, and the name's words. */
    private fun personalWords(email: String, name: String): List<String> {
        val localPart = email.trim().substringBefore('@')

        return (listOf(localPart) + localPart.split('.', '_', '-', '+') +
            name.trim().split(' ', '-', '\'', '.'))
            .filter { it.length >= MIN_PERSONAL_WORD_LENGTH }
            .distinct()
    }
}

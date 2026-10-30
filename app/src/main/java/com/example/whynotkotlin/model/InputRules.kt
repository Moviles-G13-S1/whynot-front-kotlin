package com.example.whynotkotlin.model

/**
 * Validation for the text fields of every form, shared by the controllers and
 * the views: a form shows under each field the same message the controller
 * would throw on save, while the user is still filling the form.
 *
 * Each function returns what is wrong with the value, or null when it is valid.
 *
 * Limits follow the Firestore rules, or the iOS app where it is stricter, so
 * both clients accept the same input.
 */
object InputRules {

    /** Same limit as the iOS app (`UserProfileConstraints.maximumNameLength`). */
    const val NAME_MAX_LENGTH = 15

    /** The rules accept up to 320 characters for `users.email`. */
    const val EMAIL_MAX_LENGTH = 320

    const val AGE_MAX_LENGTH = 3

    /**
     * The rules accept 200 characters for a product name and brand; shorter
     * limits keep product cards readable.
     */
    const val PRODUCT_NAME_MAX_LENGTH = 100
    const val BRAND_MAX_LENGTH = 50

    /** The rules accept up to 2048 characters for image and product links. */
    const val LINK_MAX_LENGTH = 2048

    private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun name(value: String): String? {
        val name = value.trim()

        return when {
            name.isEmpty() -> "Enter your name"
            containsEmoji(name) -> "Your name cannot contain emojis"
            name.length > NAME_MAX_LENGTH ->
                "Your name can have at most $NAME_MAX_LENGTH characters"

            name.none { it.isLetter() } -> "Your name needs at least one letter"
            !name.all { it.isNameCharacter() } ->
                "Use only letters, numbers, spaces, hyphens or apostrophes"

            else -> null
        }
    }

    fun email(value: String): String? {
        val email = value.trim()

        return when {
            email.isEmpty() -> "Enter your email"
            email.length > EMAIL_MAX_LENGTH ->
                "The email can have at most $EMAIL_MAX_LENGTH characters"

            containsEmoji(email) || !EMAIL_PATTERN.matches(email) ->
                "Enter a valid email, such as name@example.com"

            else -> null
        }
    }

    fun age(value: String): String? {
        val age = value.trim()

        return when {
            age.isEmpty() -> "Enter your age"
            !age.all { it in '0'..'9' } -> "Enter your age in whole numbers"
            age.toIntOrNull() !in AgeRange.MIN..AgeRange.MAX ->
                "Age must be between ${AgeRange.MIN} and ${AgeRange.MAX}"

            else -> null
        }
    }

    fun productName(value: String): String? =
        productText(value, "Enter a product name", "The product name", PRODUCT_NAME_MAX_LENGTH)

    fun brand(value: String): String? =
        productText(value, "Enter a brand", "The brand", BRAND_MAX_LENGTH)

    /**
     * An optional picture or store link: empty, or a full web address. [field]
     * names it in the message, such as "Picture link".
     */
    fun optionalLink(value: String, field: String): String? {
        val link = value.trim()
        val subject = "The ${field.lowercase()}"

        return when {
            link.isEmpty() -> null
            link.length > LINK_MAX_LENGTH ->
                "$subject can have at most $LINK_MAX_LENGTH characters"

            !link.startsWith("https://", ignoreCase = true) &&
                !link.startsWith("http://", ignoreCase = true) ->
                "$subject must start with http:// or https://"

            !link.isWebAddress() -> "$subject is not a valid web address"
            else -> null
        }
    }

    /**
     * Whether [value] has an emoji: pictographs, emoticons, flags, dingbats and
     * the characters that turn a symbol into an emoji. Emojis are rejected in
     * names, emails, passwords and product text, as on iOS.
     */
    fun containsEmoji(value: String): Boolean {
        var index = 0

        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            if (codePoint.isEmoji()) return true
            index += Character.charCount(codePoint)
        }

        return false
    }

    private fun productText(
        value: String,
        missingMessage: String,
        subject: String,
        maxLength: Int
    ): String? {
        val text = value.trim()

        return when {
            text.isEmpty() -> missingMessage
            containsEmoji(text) -> "$subject cannot contain emojis"
            text.any { it == '<' || it == '>' } -> "$subject cannot contain < or >"
            text.length > maxLength -> "$subject can have at most $maxLength characters"
            else -> null
        }
    }

    /**
     * Letters (accented ones included), digits ("Demo User 24", as iOS allows),
     * spaces, hyphens, apostrophes and periods.
     */
    private fun Char.isNameCharacter(): Boolean =
        isLetter() || this in '0'..'9' ||
            Character.getType(this) == Character.NON_SPACING_MARK.toInt() ||
            this == ' ' || this == '-' || this == '\'' || this == '’' || this == '.'

    /** A host with a dot after the scheme, no spaces and no emojis. */
    private fun String.isWebAddress(): Boolean {
        val host = substringAfter("://")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')

        return none { it.isWhitespace() } &&
            !containsEmoji(this) &&
            '@' !in host &&
            host.contains('.') &&
            !host.startsWith('.') &&
            !host.endsWith('.')
    }

    private fun Int.isEmoji(): Boolean =
        this in 0x1F000..0x1FAFF || // emoticons, pictographs, flags, transport
            this in 0x2600..0x27BF || // miscellaneous symbols and dingbats
            this in 0x2B00..0x2BFF || // arrows and stars such as ⭐
            this == 0x20E3 || // keycap
            this == 0xFE0F // emoji presentation selector
}

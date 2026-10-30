package com.example.whynotkotlin.model

/**
 * Reads a price typed by the user: a product price or the price paid.
 *
 * `toDoubleOrNull` accepted "NaN", "Infinity" and "1e9", and swapping the
 * comma for a dot read "1.000" as 1. Here a price has digits and separators
 * only. The last separator followed by one or two digits is the decimal mark,
 * and separators between groups of three digits group thousands, so "1.000",
 * "1,000.50", "1.500,50" and "12,5" read as people mean them.
 */
object PriceRules {

    const val MAX_PRICE = 100_000_000.0

    /** Enough for "100.000.000,00" with a currency sign. */
    const val MAX_LENGTH = 16

    /** The amount in [text], or null when it is not a valid price. */
    fun parse(text: String): Double? = (read(text) as? Reading.Amount)?.value

    /** What is wrong with [text] as a price, or null when it is valid. */
    fun error(text: String): String? = (read(text) as? Reading.Problem)?.message

    /** Whether [text] was rejected for being negative (BQ save-failure reason). */
    fun isNegative(text: String): Boolean = read(text) == Reading.Problem(NEGATIVE)

    private sealed interface Reading {
        data class Amount(val value: Double) : Reading
        data class Problem(val message: String) : Reading
    }

    private fun read(text: String): Reading {
        val price = text.trim().removePrefix("$").trim()

        if (price.isEmpty()) return Reading.Problem(MISSING)
        if (price.startsWith("-")) return Reading.Problem(NEGATIVE)
        if (price.none { it in '0'..'9' } ||
            !price.all { it in '0'..'9' || it == '.' || it == ',' }
        ) {
            return Reading.Problem(NOT_A_PRICE)
        }

        val decimalMark = decimalMarkOf(price)
        val whole = if (decimalMark == null) price else price.substringBeforeLast(decimalMark)
        val decimals = if (decimalMark == null) "" else price.substringAfterLast(decimalMark)

        if (decimalMark != null && (decimals.isEmpty() || !decimals.all { it in '0'..'9' })) {
            return Reading.Problem(NOT_A_PRICE)
        }
        if (decimals.length > 2) return Reading.Problem(TOO_MANY_DECIMALS)

        val digits = wholeDigits(whole) ?: return Reading.Problem(NOT_A_PRICE)
        val amount = "$digits.${decimals.ifEmpty { "0" }}".toDouble()

        return when {
            amount <= 0.0 -> Reading.Problem(NOT_POSITIVE)
            amount > MAX_PRICE -> Reading.Problem(TOO_LARGE)
            else -> Reading.Amount(amount)
        }
    }

    /** The separator used as decimal mark in [price], or null for a whole amount. */
    private fun decimalMarkOf(price: String): Char? {
        val lastDot = price.lastIndexOf('.')
        val lastComma = price.lastIndexOf(',')

        if (lastDot >= 0 && lastComma >= 0) return if (lastDot > lastComma) '.' else ','

        val mark = when {
            lastDot >= 0 -> '.'
            lastComma >= 0 -> ','
            else -> return null
        }

        // "1.000.000": the same separator twice only groups thousands.
        if (price.count { it == mark } > 1) return null

        // "1.000" or "12,500": one separator before three digits groups thousands.
        val before = price.substringBefore(mark)
        val digitsAfter = price.length - price.indexOf(mark) - 1
        return if (digitsAfter == 3 && before.length in 1..3 && before != "0") null else mark
    }

    /** The digits of [whole] without thousands separators, or null when misplaced. */
    private fun wholeDigits(whole: String): String? {
        if (whole.isEmpty()) return null

        val separators = whole.filter { it == '.' || it == ',' }.toSet()
        if (separators.isEmpty()) return whole
        if (separators.size > 1) return null

        val groups = whole.split(separators.first())
        val grouped = groups.first().length in 1..3 && groups.drop(1).all { it.length == 3 }

        return if (grouped) groups.joinToString("") else null
    }

    private const val MISSING = "Enter a price"
    private const val NEGATIVE = "The price cannot be negative"
    private const val NOT_A_PRICE = "Enter a valid price, such as 1500 or 1.500,50"
    private const val TOO_MANY_DECIMALS = "Use at most 2 decimals"
    private const val NOT_POSITIVE = "The price must be greater than 0"
    private const val TOO_LARGE = "The price can be at most 100.000.000"
}

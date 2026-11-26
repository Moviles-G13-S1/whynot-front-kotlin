package com.example.whynotkotlin.model

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Sprint 4 feature (Juan Felipe): Monthly Spending Summary.
 *
 * What the user spent is the saved price of every product marked as purchased,
 * grouped by the month of `purchasedAt` (Bogotá time) and by category.
 */

/** One purchased product, as kept for the summary and stored on the phone. */
data class SpendingPurchase(
    val productId: String,
    val yearMonth: Int,
    val categoryId: String,
    val categoryName: String,
    val name: String,
    val brand: String,
    val price: Double,
    val purchasedAtMillis: Long,
    /** Marked as purchased offline and not confirmed by the backend yet. */
    val pending: Boolean
)

/** What was spent in one category during one month. */
data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val total: Double,
    val purchaseCount: Int,
    val pendingCount: Int
)

/** One month of spending, its categories from highest to lowest total. */
data class MonthSpending(
    val yearMonth: Int,
    val total: Double,
    val purchaseCount: Int,
    val categories: List<CategorySpending>
)

/** What the Monthly Spending Summary screen shows. */
data class SpendingSummaryLoad(
    val month: MonthSpending?,
    /** Months with purchases, newest first, as yyyyMM. */
    val availableMonths: List<Int>,
    /** True when shown from the phone because the backend could not be reached. */
    val isStale: Boolean,
    val updatedAtMillis: Long?,
    /** True when no network call or disk read was needed. */
    val fromCache: Boolean = false
)

/** What the Category Spending Detail screen shows. */
data class CategorySpendingDetailLoad(
    val yearMonth: Int,
    val category: CategorySpending?,
    val monthTotal: Double,
    val purchases: List<SpendingPurchase>,
    val isStale: Boolean,
    val updatedAtMillis: Long?
)

/** Months are keyed as yyyyMM integers (202611) in the cache and the database. */
object YearMonth {
    private val bogota: TimeZone = TimeZone.getTimeZone("America/Bogota")

    fun of(date: Date): Int {
        val calendar = Calendar.getInstance(bogota).apply { time = date }
        return calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH) + 1
    }

    fun year(yearMonth: Int): Int = yearMonth / 100

    fun month(yearMonth: Int): Int = yearMonth % 100

    private val names = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun label(yearMonth: Int): String = "${names[month(yearMonth) - 1]} ${year(yearMonth)}"
}

/** Offline and nothing was ever stored for this user. */
class SpendingOfflineException :
    UserFacingException("Your spending summary needs internet the first time. Connect and try again.")

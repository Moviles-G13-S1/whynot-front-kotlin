package com.example.whynotkotlin.model.repositories

import com.example.whynotkotlin.model.CategorySpending
import com.example.whynotkotlin.model.MonthSpending
import com.example.whynotkotlin.model.SpendingPurchase

/**
 * The Monthly Spending Summary saved on the phone (local storage), so it can
 * be shown without internet and after the app is closed.
 *
 * The totals are not stored: they are computed with SQL queries (SUM and GROUP
 * BY) over the stored purchases, so they can never disagree with them.
 */
interface SpendingStore {

    /** Replaces everything stored for [uid] with [purchases], in one transaction. */
    suspend fun replace(uid: String, purchases: List<SpendingPurchase>, updatedAtMillis: Long)

    /** Every month with purchases, newest first, each with its categories. */
    suspend fun months(uid: String): List<MonthSpending>

    /** The categories of one month, highest total first. */
    suspend fun categories(uid: String, yearMonth: Int): List<CategorySpending>

    /** The purchases of one category in one month, newest first. */
    suspend fun purchases(uid: String, yearMonth: Int, categoryId: String): List<SpendingPurchase>

    /** When the stored summary of [uid] was last refreshed, or null if never. */
    suspend fun updatedAt(uid: String): Long?

    /** Removes every user's data, so the next account never sees it. */
    suspend fun clearAll()
}

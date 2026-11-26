package com.example.whynotkotlin.model

import android.util.Log
import androidx.collection.SparseArrayCompat

/**
 * In-memory cache of the Monthly Spending Summary (caching).
 *
 * A [SparseArrayCompat] maps the month as an Int (yyyyMM, e.g. 202611) to its
 * [MonthSpending]. Month keys are plain integers, so a sparse array avoids the
 * boxing and the per-entry objects of a HashMap, and lookups are a binary
 * search over a few dozen months at most.
 *
 * It holds one user at a time: switching user clears it. It is invalidated
 * when a purchase changes (marked, edited or deleted) and on sign-out, so it
 * never shows totals that the backend would contradict.
 */
class SpendingCache(
    private val logger: (String) -> Unit = { Log.d(TAG, it) }
) {
    private var uid: String? = null
    private var updatedAtMillis: Long? = null
    private val months = SparseArrayCompat<MonthSpending>()

    /** Purchases of each month, keyed the same way, for the detail screen. */
    private val purchases = SparseArrayCompat<List<SpendingPurchase>>()

    var hits = 0
        private set
    var misses = 0
        private set

    fun isFilledFor(uid: String): Boolean {
        val filled = this.uid == uid && months.size() > 0
        if (filled) hits++ else misses++
        logger(if (filled) "HIT uid=${uid.take(6)}" else "MISS uid=${uid.take(6)}")
        return filled
    }

    fun month(yearMonth: Int): MonthSpending? = months[yearMonth]

    fun purchasesOf(yearMonth: Int): List<SpendingPurchase> = purchases[yearMonth].orEmpty()

    /** Months with purchases, newest first. */
    fun availableMonths(): List<Int> =
        (0 until months.size()).map { months.keyAt(it) }.sortedDescending()

    fun updatedAt(): Long? = updatedAtMillis

    fun put(
        uid: String,
        monthList: List<MonthSpending>,
        purchaseList: List<SpendingPurchase>,
        updatedAtMillis: Long
    ) {
        clearInternal()
        this.uid = uid
        this.updatedAtMillis = updatedAtMillis
        monthList.forEach { months.put(it.yearMonth, it) }
        purchaseList.groupBy { it.yearMonth }.forEach { (key, list) -> purchases.put(key, list) }
        logger("PUT uid=${uid.take(6)} months=${months.size()}")
    }

    fun invalidate(reason: String) {
        if (months.size() > 0) logger("INVALIDATE reason=$reason")
        clearInternal()
    }

    private fun clearInternal() {
        uid = null
        updatedAtMillis = null
        months.clear()
        purchases.clear()
    }

    private companion object {
        const val TAG = "WhyNotSpendingCache"
    }
}

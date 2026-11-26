package com.example.whynotkotlin.model.repositories

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.whynotkotlin.model.CategorySpending
import com.example.whynotkotlin.model.MonthSpending
import com.example.whynotkotlin.model.SpendingPurchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local relational database (SQLite) for the Monthly Spending Summary.
 *
 * Two tables:
 * - `spending_purchase`: one row per purchased product of each user.
 * - `spending_sync`: when each user's rows were last refreshed.
 *
 * A relational database fits because the screens ask relational questions
 * (totals per month, per category, the purchases of one category in one
 * month), answered with SUM / COUNT / GROUP BY and an index, instead of
 * loading everything and grouping it in Kotlin. SQLiteOpenHelper is used
 * directly, with no annotation processor, so the build stays simple.
 *
 * Every query runs on [Dispatchers.IO], never on the main thread.
 */
class SqliteSpendingStore(context: Context) : SpendingStore {

    private val helper = Helper(context.applicationContext)

    override suspend fun replace(
        uid: String,
        purchases: List<SpendingPurchase>,
        updatedAtMillis: Long
    ) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(PURCHASE, "$UID = ?", arrayOf(uid))
            purchases.forEach { purchase ->
                db.insert(PURCHASE, null, ContentValues().apply {
                    put(UID, uid)
                    put(PRODUCT_ID, purchase.productId)
                    put(YEAR_MONTH, purchase.yearMonth)
                    put(CATEGORY_ID, purchase.categoryId)
                    put(CATEGORY_NAME, purchase.categoryName)
                    put(NAME, purchase.name)
                    put(BRAND, purchase.brand)
                    put(PRICE, purchase.price)
                    put(PURCHASED_AT, purchase.purchasedAtMillis)
                    put(PENDING, if (purchase.pending) 1 else 0)
                })
            }
            db.insertWithOnConflict(SYNC, null, ContentValues().apply {
                put(UID, uid)
                put(UPDATED_AT, updatedAtMillis)
            }, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun months(uid: String): List<MonthSpending> = withContext(Dispatchers.IO) {
        val months = mutableListOf<Pair<Int, Pair<Double, Int>>>()
        helper.readableDatabase.rawQuery(
            "SELECT $YEAR_MONTH, SUM($PRICE), COUNT(*) FROM $PURCHASE " +
                "WHERE $UID = ? GROUP BY $YEAR_MONTH ORDER BY $YEAR_MONTH DESC",
            arrayOf(uid)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                months += cursor.getInt(0) to (cursor.getDouble(1) to cursor.getInt(2))
            }
        }
        months.map { (yearMonth, totals) ->
            MonthSpending(
                yearMonth = yearMonth,
                total = totals.first,
                purchaseCount = totals.second,
                categories = categoriesOf(uid, yearMonth)
            )
        }
    }

    override suspend fun categories(uid: String, yearMonth: Int): List<CategorySpending> =
        withContext(Dispatchers.IO) { categoriesOf(uid, yearMonth) }

    override suspend fun purchases(
        uid: String,
        yearMonth: Int,
        categoryId: String
    ): List<SpendingPurchase> = withContext(Dispatchers.IO) {
        val purchases = mutableListOf<SpendingPurchase>()
        helper.readableDatabase.query(
            PURCHASE,
            arrayOf(PRODUCT_ID, YEAR_MONTH, CATEGORY_ID, CATEGORY_NAME, NAME, BRAND, PRICE, PURCHASED_AT, PENDING),
            "$UID = ? AND $YEAR_MONTH = ? AND $CATEGORY_ID = ?",
            arrayOf(uid, yearMonth.toString(), categoryId),
            null, null,
            "$PURCHASED_AT DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                purchases += SpendingPurchase(
                    productId = cursor.getString(0),
                    yearMonth = cursor.getInt(1),
                    categoryId = cursor.getString(2),
                    categoryName = cursor.getString(3),
                    name = cursor.getString(4),
                    brand = cursor.getString(5),
                    price = cursor.getDouble(6),
                    purchasedAtMillis = cursor.getLong(7),
                    pending = cursor.getInt(8) == 1
                )
            }
        }
        purchases
    }

    override suspend fun updatedAt(uid: String): Long? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            SYNC, arrayOf(UPDATED_AT), "$UID = ?", arrayOf(uid), null, null, null
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else null }
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.delete(PURCHASE, null, null)
        db.delete(SYNC, null, null)
        Unit
    }

    /** Called from inside withContext(Dispatchers.IO). */
    private fun categoriesOf(uid: String, yearMonth: Int): List<CategorySpending> {
        val categories = mutableListOf<CategorySpending>()
        helper.readableDatabase.rawQuery(
            "SELECT $CATEGORY_ID, MAX($CATEGORY_NAME), SUM($PRICE), COUNT(*), SUM($PENDING) " +
                "FROM $PURCHASE WHERE $UID = ? AND $YEAR_MONTH = ? " +
                "GROUP BY $CATEGORY_ID ORDER BY SUM($PRICE) DESC",
            arrayOf(uid, yearMonth.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                categories += CategorySpending(
                    categoryId = cursor.getString(0),
                    categoryName = cursor.getString(1),
                    total = cursor.getDouble(2),
                    purchaseCount = cursor.getInt(3),
                    pendingCount = cursor.getInt(4)
                )
            }
        }
        return categories
    }

    private class Helper(context: Context) :
        SQLiteOpenHelper(context, DATABASE, null, VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $PURCHASE (" +
                    "$UID TEXT NOT NULL, " +
                    "$PRODUCT_ID TEXT NOT NULL, " +
                    "$YEAR_MONTH INTEGER NOT NULL, " +
                    "$CATEGORY_ID TEXT NOT NULL, " +
                    "$CATEGORY_NAME TEXT NOT NULL, " +
                    "$NAME TEXT NOT NULL, " +
                    "$BRAND TEXT NOT NULL, " +
                    "$PRICE REAL NOT NULL, " +
                    "$PURCHASED_AT INTEGER NOT NULL, " +
                    "$PENDING INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY ($UID, $PRODUCT_ID))"
            )
            // The screens always filter by user and month.
            db.execSQL("CREATE INDEX idx_spending_month ON $PURCHASE ($UID, $YEAR_MONTH, $CATEGORY_ID)")
            db.execSQL(
                "CREATE TABLE $SYNC ($UID TEXT PRIMARY KEY NOT NULL, $UPDATED_AT INTEGER NOT NULL)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // The data is a copy of Firestore, so it is safe to rebuild.
            db.execSQL("DROP TABLE IF EXISTS $PURCHASE")
            db.execSQL("DROP TABLE IF EXISTS $SYNC")
            onCreate(db)
        }
    }

    private companion object {
        const val DATABASE = "whynot_spending.db"
        const val VERSION = 1

        const val PURCHASE = "spending_purchase"
        const val SYNC = "spending_sync"

        const val UID = "uid"
        const val PRODUCT_ID = "product_id"
        const val YEAR_MONTH = "year_month"
        const val CATEGORY_ID = "category_id"
        const val CATEGORY_NAME = "category_name"
        const val NAME = "name"
        const val BRAND = "brand"
        const val PRICE = "price"
        const val PURCHASED_AT = "purchased_at"
        const val PENDING = "pending"
        const val UPDATED_AT = "updated_at"
    }
}

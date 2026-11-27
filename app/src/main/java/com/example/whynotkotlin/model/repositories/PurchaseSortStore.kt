package com.example.whynotkotlin.model.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.whynotkotlin.model.PurchaseSort
import kotlinx.coroutines.flow.first

/** Local storage for the order each user chose in Purchases. */
interface PurchaseSortStore {
    suspend fun load(ownerId: String): PurchaseSort?
    suspend fun save(ownerId: String, sort: PurchaseSort)
}

private val Context.purchasesPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "purchases_preferences"
)

/**
 * Local storage (Sprint 3 correction) with Jetpack DataStore Preferences: one
 * key per user (`<uid>.sort`), so two accounts on the phone keep their own
 * order. DataStore reads and writes asynchronously and transactionally, off the
 * main thread.
 */
class DataStorePurchaseSortStore(context: Context) : PurchaseSortStore {

    private val store = context.applicationContext.purchasesPreferences

    override suspend fun load(ownerId: String): PurchaseSort? {
        val saved = store.data.first()[key(ownerId)] ?: return null
        return PurchaseSort.entries.firstOrNull { it.name == saved }
    }

    override suspend fun save(ownerId: String, sort: PurchaseSort) {
        store.edit { preferences -> preferences[key(ownerId)] = sort.name }
    }

    private fun key(ownerId: String) = stringPreferencesKey("$ownerId.sort")
}

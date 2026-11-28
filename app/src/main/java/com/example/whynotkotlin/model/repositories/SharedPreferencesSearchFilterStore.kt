package com.example.whynotkotlin.model.repositories

import android.content.Context
import androidx.core.content.edit
import com.example.whynotkotlin.model.SearchFilters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local storage for the search form with SharedPreferences (key-value).
 *
 * Four short strings per user are a key-value problem, so a database would be
 * overkill. Keys carry the user id (`<uid>.query`, `<uid>.category`...), so two
 * accounts on one phone keep separate filters. The first read loads the XML
 * file from disk, so reads run on [Dispatchers.IO]; writes use `apply()`, which
 * commits to memory at once and writes the file in the background.
 */
class SharedPreferencesSearchFilterStore(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchFilterStore {

    // Opened on first use (inside the IO dispatcher), not when the app starts.
    private val appContext = context.applicationContext
    private val preferences by lazy {
        appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun load(ownerId: String): SearchFilters? = withContext(ioDispatcher) {
        if (!preferences.contains(key(ownerId, QUERY))) return@withContext null

        SearchFilters(
            query = preferences.getString(key(ownerId, QUERY), "").orEmpty(),
            categoryId = preferences.getString(key(ownerId, CATEGORY), "").orEmpty(),
            minPrice = preferences.getString(key(ownerId, MIN_PRICE), "").orEmpty(),
            maxPrice = preferences.getString(key(ownerId, MAX_PRICE), "").orEmpty()
        )
    }

    override suspend fun save(ownerId: String, filters: SearchFilters) = withContext(ioDispatcher) {
        // KTX edit { }: applies asynchronously (apply), like before.
        preferences.edit {
            putString(key(ownerId, QUERY), filters.query)
            putString(key(ownerId, CATEGORY), filters.categoryId)
            putString(key(ownerId, MIN_PRICE), filters.minPrice)
            putString(key(ownerId, MAX_PRICE), filters.maxPrice)
        }
    }

    private fun key(ownerId: String, field: String) = "$ownerId.$field"

    private companion object {
        const val FILE_NAME = "whynot_search_filters"
        const val QUERY = "query"
        const val CATEGORY = "category"
        const val MIN_PRICE = "minPrice"
        const val MAX_PRICE = "maxPrice"
    }
}

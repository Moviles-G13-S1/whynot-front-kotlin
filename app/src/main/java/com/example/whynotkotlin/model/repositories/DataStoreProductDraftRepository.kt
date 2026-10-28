package com.example.whynotkotlin.model.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.whynotkotlin.model.ProductFormDraft
import kotlinx.coroutines.flow.first

/** One file for every user's draft; keys are namespaced by uid. */
private val Context.productDraftStore: DataStore<Preferences> by preferencesDataStore(
    name = "product_drafts"
)

/**
 * Stores product drafts with Jetpack DataStore (Preferences).
 *
 * DataStore was chosen over SharedPreferences because it is asynchronous and
 * transactional: reads and writes run on its own I/O dispatcher, never on the
 * main thread, and a write either lands completely or not at all. A draft is a
 * handful of short strings, so key-value storage is enough and Room would add a
 * schema for no benefit.
 *
 * Each field is stored under "<uid>.<field>", which keeps the drafts of two
 * accounts on the same phone apart.
 */
class DataStoreProductDraftRepository(
    context: Context
) : ProductDraftRepository {

    private val store = context.applicationContext.productDraftStore

    override suspend fun loadDraft(ownerId: String): ProductFormDraft? {
        val preferences = store.data.first()
        val keys = DraftKeys(ownerId)

        val draft = ProductFormDraft(
            wishlistId = preferences[keys.wishlistId].orEmpty(),
            name = preferences[keys.name].orEmpty(),
            brand = preferences[keys.brand].orEmpty(),
            price = preferences[keys.price].orEmpty(),
            imageUrl = preferences[keys.imageUrl].orEmpty(),
            productUrl = preferences[keys.productUrl].orEmpty()
        )

        return draft.takeUnless { it.isBlank() }
    }

    override suspend fun saveDraft(ownerId: String, draft: ProductFormDraft) {
        val keys = DraftKeys(ownerId)

        store.edit { preferences ->
            preferences[keys.wishlistId] = draft.wishlistId
            preferences[keys.name] = draft.name
            preferences[keys.brand] = draft.brand
            preferences[keys.price] = draft.price
            preferences[keys.imageUrl] = draft.imageUrl
            preferences[keys.productUrl] = draft.productUrl
        }
    }

    override suspend fun clearDraft(ownerId: String) {
        val keys = DraftKeys(ownerId)

        store.edit { preferences -> keys.all.forEach(preferences::remove) }
    }
}

private class DraftKeys(ownerId: String) {
    val wishlistId = key(ownerId, "wishlistId")
    val name = key(ownerId, "name")
    val brand = key(ownerId, "brand")
    val price = key(ownerId, "price")
    val imageUrl = key(ownerId, "imageUrl")
    val productUrl = key(ownerId, "productUrl")

    val all = listOf(wishlistId, name, brand, price, imageUrl, productUrl)
}

private fun key(ownerId: String, field: String): Preferences.Key<String> =
    stringPreferencesKey("$ownerId.$field")

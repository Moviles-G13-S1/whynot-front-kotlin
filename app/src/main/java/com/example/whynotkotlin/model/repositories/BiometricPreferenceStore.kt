package com.example.whynotkotlin.model.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/**
 * Whether each account on this phone chose biometric unlock. Only that choice
 * is stored, never a password or token: the Firebase session itself is kept
 * by Firebase, and biometrics only gate opening it.
 */
interface BiometricPreferenceStore {
    suspend fun isEnabled(uid: String): Boolean
    suspend fun setEnabled(uid: String, enabled: Boolean)
}

/** Kept in memory only: used by tests and previews. */
class InMemoryBiometricPreferenceStore : BiometricPreferenceStore {
    private val enabled = mutableSetOf<String>()
    override suspend fun isEnabled(uid: String) = uid in enabled
    override suspend fun setEnabled(uid: String, enabled: Boolean) {
        if (enabled) this.enabled += uid else this.enabled -= uid
    }
}

private val Context.biometricStore: DataStore<Preferences> by preferencesDataStore(
    name = "biometric_unlock"
)

/** DataStore (Preferences): asynchronous, off the main thread, one key per uid. */
class DataStoreBiometricPreferenceStore(context: Context) : BiometricPreferenceStore {

    private val store = context.applicationContext.biometricStore

    private fun key(uid: String) = booleanPreferencesKey("biometric_enabled_$uid")

    override suspend fun isEnabled(uid: String): Boolean =
        store.data.first()[key(uid)] ?: false

    override suspend fun setEnabled(uid: String, enabled: Boolean) {
        store.edit { preferences ->
            if (enabled) preferences[key(uid)] = true else preferences.remove(key(uid))
        }
    }
}

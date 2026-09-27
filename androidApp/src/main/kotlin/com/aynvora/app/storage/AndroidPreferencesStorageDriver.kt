package com.aynvora.app.storage

import android.content.Context
import android.content.SharedPreferences
import com.aynvora.data.storage.StorageDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android persistent storage driver backed by SharedPreferences.
 * Ensures model state, preferences, and session data persist across process restarts.
 */
class AndroidPreferencesStorageDriver(
    context: Context,
    prefsName: String = "aynvora_persistent_storage",
) : StorageDriver {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override suspend fun read(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    override suspend fun write(key: String, content: String): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, content).commit()
    }

    override suspend fun delete(key: String): Unit = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).commit()
    }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.IO) {
        prefs.contains(key)
    }

    override suspend fun keys(): List<String> = withContext(Dispatchers.IO) {
        prefs.all.keys.toList()
    }
}

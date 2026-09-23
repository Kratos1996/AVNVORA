package com.aynvora.data.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Low-level platform-independent storage driver interface.
 *
 * Provides raw key-value / block persistence operations for the storage engine.
 */
interface StorageDriver {
    suspend fun read(key: String): String?
    suspend fun write(key: String, content: String)
    suspend fun delete(key: String)
    suspend fun exists(key: String): Boolean
    suspend fun keys(): List<String>
}

/**
 * Thread-safe in-memory storage driver.
 *
 * Primary driver for automated testing, preview sessions, and ephemeral storage.
 */
class InMemoryStorageDriver : StorageDriver {
    private val mutex = Mutex()
    private val store = mutableMapOf<String, String>()

    override suspend fun read(key: String): String? = mutex.withLock {
        store[key]
    }

    override suspend fun write(key: String, content: String): Unit = mutex.withLock {
        store[key] = content
    }

    override suspend fun delete(key: String): Unit = mutex.withLock {
        store.remove(key)
    }

    override suspend fun exists(key: String): Boolean = mutex.withLock {
        store.containsKey(key)
    }

    override suspend fun keys(): List<String> = mutex.withLock {
        store.keys.toList()
    }
}

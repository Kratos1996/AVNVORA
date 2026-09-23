package com.aynvora.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * File-backed storage driver for JVM / Desktop targets.
 *
 * Executes atomic writes using temporary swap files to prevent file corruption
 * during unexpected process termination or power loss.
 */
class JvmFileStorageDriver(
    private val directory: File,
) : StorageDriver {

    init {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    private fun fileForKey(key: String): File = File(directory, "$key.json")

    override suspend fun read(key: String): String? = withContext(Dispatchers.IO) {
        val file = fileForKey(key)
        if (!file.exists()) return@withContext null
        file.readText()
    }

    override suspend fun write(key: String, content: String): Unit = withContext(Dispatchers.IO) {
        val file = fileForKey(key)
        val tempFile = File(directory, "$key.tmp")
        tempFile.writeText(content)
        if (file.exists()) {
            file.delete()
        }
        tempFile.renameTo(file)
    }

    override suspend fun delete(key: String): Unit = withContext(Dispatchers.IO) {
        val file = fileForKey(key)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.IO) {
        fileForKey(key).exists()
    }

    override suspend fun keys(): List<String> = withContext(Dispatchers.IO) {
        directory.listFiles { f -> f.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
}

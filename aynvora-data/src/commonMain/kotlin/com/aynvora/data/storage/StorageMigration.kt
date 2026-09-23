package com.aynvora.data.storage

/**
 * Contract for an atomic, non-destructive schema migration step.
 */
interface StorageMigration {
    val fromVersion: Int
    val toVersion: Int
    suspend fun migrate(containerJson: String): String
}

/**
 * Migration coordinator managing schema evolution and verification.
 *
 * Enforces non-destructive migration rules per docs/10_DATA_ARCHITECTURE.md.
 */
class MigrationRunner(
    private val migrations: List<StorageMigration> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }

    /**
     * Executes sequential migrations from [currentVersion] up to [targetVersion].
     *
     * @throws IllegalStateException if a downgrade is attempted or an intermediate migration step is missing.
     */
    suspend fun execute(
        rawPayload: String,
        currentVersion: Int,
        targetVersion: Int = CURRENT_SCHEMA_VERSION,
    ): String {
        if (currentVersion == targetVersion) return rawPayload
        if (currentVersion > targetVersion) {
            throw IllegalStateException(
                "Schema downgrade unsupported: stored version ($currentVersion) > engine target ($targetVersion)"
            )
        }

        var runningVersion = currentVersion
        var payload = rawPayload

        while (runningVersion < targetVersion) {
            val step = migrations.find { it.fromVersion == runningVersion && it.toVersion == runningVersion + 1 }
                ?: throw IllegalStateException(
                    "Missing migration path from schema version $runningVersion to ${runningVersion + 1}"
                )
            payload = step.migrate(payload)
            runningVersion = step.toVersion
        }

        return payload
    }
}

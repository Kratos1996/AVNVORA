package com.aynvora.core.repository

import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.SyncResult
import com.aynvora.core.models.SyncStatus
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for accessing offline-first verified knowledge content.
 *
 * Exposes local Room data through Flow and suspend contracts.
 * Direct network calls must not be exposed through this repository.
 */
interface ContentRepository {
    /**
     * Observes approved, published content items for a specific module and language.
     * Guaranteed to emit local database content immediately even when offline.
     */
    fun observeApprovedContent(
        moduleId: ContentModuleId,
        language: String,
    ): Flow<AynvoraResult<List<ContentItem>>>

    /**
     * Observes a single approved content item by its globally unique ID.
     */
    fun observeContentItem(id: String): Flow<AynvoraResult<ContentItem>>

    /**
     * Retrieves a single content item once from local storage.
     */
    suspend fun getContentItem(id: String): AynvoraResult<ContentItem>

    /**
     * Lists currently installed content packs, optionally filtered by module.
     */
    suspend fun getInstalledPacks(moduleId: ContentModuleId? = null): AynvoraResult<List<ContentPack>>
}

/**
 * Domain repository contract for coordinating content synchronization.
 */
interface ContentSyncRepository {
    /**
     * Observes the current synchronization state.
     */
    fun observeSyncStatus(): Flow<SyncStatus>

    /**
     * Triggers a versioned remote sync against local Room storage.
     *
     * @param forceRefresh When true, bypasses local version checks and re-verifies all content packs.
     */
    suspend fun synchronize(forceRefresh: Boolean = false): AynvoraResult<SyncResult>

    /**
     * Cancels an ongoing synchronization operation if active.
     */
    suspend fun cancelSync(): AynvoraResult<Unit>
}

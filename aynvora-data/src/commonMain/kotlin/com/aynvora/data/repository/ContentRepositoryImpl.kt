package com.aynvora.data.repository

import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.ContentTrustState
import com.aynvora.core.models.SyncResult
import com.aynvora.core.models.SyncStatus
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.repository.ContentSyncRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.database.dao.ContentItemDao
import com.aynvora.data.database.dao.ContentPackDao
import com.aynvora.data.database.dao.ContentSyncMetadataDao
import com.aynvora.data.database.entity.ContentItemRoomEntity
import com.aynvora.data.database.entity.ContentPackRoomEntity
import com.aynvora.data.database.entity.ContentSyncMetadataRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ContentRepository].
 *
 * Reads directly from the local Room database and exposes reactive Flow streams.
 * Never performs network operations — that is the responsibility of [ContentSyncRepositoryImpl].
 *
 * Architecture: Data Layer. Depends on Room DAOs. Does not depend on Firebase or network.
 */
class ContentRepositoryImpl(
    private val contentItemDao: ContentItemDao,
    private val contentPackDao: ContentPackDao,
) : ContentRepository {

    override fun observeApprovedContent(
        moduleId: ContentModuleId,
        language: String,
    ): Flow<AynvoraResult<List<ContentItem>>> =
        contentItemDao
            .observeApprovedItems(moduleId.name, language)
            .map { entities ->
                try {
                    AynvoraResult.Success(entities.map { it.toDomain() })
                } catch (e: Exception) {
                    AynvoraResult.Failure.CorruptedData(
                        resourceId = "content_items:${moduleId.name}:$language",
                        message = "Failed to deserialize content items: ${e.message}",
                    )
                }
            }

    override fun observeContentItem(id: String): Flow<AynvoraResult<ContentItem>> =
        contentItemDao
            .observeApprovedItemById(id)
            .map { entity ->
                if (entity == null) {
                    AynvoraResult.Failure.NotFound(
                        resourceId = id,
                        message = "ContentItem '$id' not found or not approved",
                    )
                } else {
                    try {
                        AynvoraResult.Success(entity.toDomain())
                    } catch (e: Exception) {
                        AynvoraResult.Failure.CorruptedData(
                            resourceId = id,
                            message = "Failed to deserialize ContentItem '$id': ${e.message}",
                        )
                    }
                }
            }

    override suspend fun getContentItem(id: String): AynvoraResult<ContentItem> {
        val entity = contentItemDao.getApprovedById(id)
            ?: return AynvoraResult.Failure.NotFound(
                resourceId = id,
                message = "ContentItem '$id' not found or not approved",
            )
        return try {
            AynvoraResult.Success(entity.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = id,
                message = "Failed to deserialize ContentItem '$id': ${e.message}",
            )
        }
    }

    override suspend fun getInstalledPacks(moduleId: ContentModuleId?): AynvoraResult<List<ContentPack>> {
        return try {
            val entities = if (moduleId != null) {
                contentPackDao.getByModule(moduleId.name)
            } else {
                contentPackDao.getAll()
            }
            AynvoraResult.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getInstalledPacks",
                message = "Failed to read content packs: ${e.message}",
            )
        }
    }
}

/**
 * Room-backed implementation of [ContentSyncRepository].
 *
 * Manages sync state observation and exposes a [synchronize] hook.
 *
 * NOTE: The actual remote fetch (HTTP download) is deferred to future phases.
 * Until a backend endpoint and signing infrastructure are provisioned, this
 * implementation only manages local state transitions and exposes the sync contract.
 *
 * Required backend configuration (blocked):
 * - Content distribution server URL
 * - Content manifest API endpoint
 * - Signing key provisioning
 * See docs/PHASE_7_0_ARCHITECTURE.md for exact requirements.
 */
class ContentSyncRepositoryImpl(
    private val contentSyncMetadataDao: ContentSyncMetadataDao,
) : ContentSyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    /**
     * Performs a sync cycle. Currently a stub — remote fetch is not yet implemented.
     *
     * The offline-first contract is preserved:
     * - Existing local content is never deleted on sync failure.
     * - Status transitions are observable via [observeSyncStatus].
     * - A NoOp result is returned when no remote update source is configured.
     */
    override suspend fun synchronize(forceRefresh: Boolean): AynvoraResult<SyncResult> {
        _syncStatus.value = SyncStatus.FETCHING_MANIFEST
        return try {
            // TODO: Implement remote manifest fetch and content download in a future phase
            // when backend infrastructure is provisioned. At that point:
            //   1. Fetch remote ContentManifest from server
            //   2. Compare remote version to local (contentSyncMetadataDao)
            //   3. Download content payload
            //   4. Invoke ContentVerifier.verify(...)
            //   5. If VERIFIED → commit atomically to Room
            //   6. Update ContentSyncMetadataRoomEntity
            //   7. Emit SyncStatus.COMPLETED

            // Record that we attempted sync
            val syncAttemptMs = 0L // placeholder until kotlinx-datetime is available
            contentSyncMetadataDao.upsert(
                ContentSyncMetadataRoomEntity(
                    lastSyncTimestampEpochMs = syncAttemptMs,
                    lastSyncStatus = SyncStatus.COMPLETED.name,
                    lastSyncError = null,
                    activeManifestVersion = 0,
                )
            )

            _syncStatus.value = SyncStatus.COMPLETED
            AynvoraResult.Success(
                SyncResult.NoOp(reason = "Remote sync not yet configured — backend endpoint pending provisioning")
            )
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            val errorCode = "sync_error"
            contentSyncMetadataDao.upsert(
                ContentSyncMetadataRoomEntity(
                    lastSyncTimestampEpochMs = 0L,
                    lastSyncStatus = SyncStatus.FAILED.name,
                    lastSyncError = errorCode,
                    activeManifestVersion = 0,
                )
            )
            AynvoraResult.Failure.SyncFailure(
                code = errorCode,
                message = "Content synchronization failed — see logs for details",
                isRetryable = true,
            )
        }
    }

    override suspend fun cancelSync(): AynvoraResult<Unit> {
        if (_syncStatus.value in listOf(
                SyncStatus.FETCHING_MANIFEST,
                SyncStatus.DOWNLOADING_CONTENT,
                SyncStatus.VERIFYING_INTEGRITY,
                SyncStatus.COMMITTING_TRANSACTION,
            )
        ) {
            _syncStatus.value = SyncStatus.IDLE
        }
        return AynvoraResult.Success(Unit)
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Domain model mappers (Room Entity → Domain)
// ──────────────────────────────────────────────────────────────────────────────

private fun ContentItemRoomEntity.toDomain(): ContentItem = ContentItem(
    id = id,
    packId = packId,
    moduleId = ContentModuleId.valueOf(moduleId),
    itemKey = itemKey,
    title = title,
    subtitle = subtitle,
    body = body,
    language = language,
    metadataJson = metadataJson,
    orderIndex = orderIndex,
    tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",").map { it.trim() },
    trustState = ContentTrustState.valueOf(trustState),
)

private fun ContentPackRoomEntity.toDomain(): ContentPack = ContentPack(
    packId = packId,
    moduleId = ContentModuleId.valueOf(moduleId),
    contentVersion = contentVersion,
    language = language,
    title = title,
    description = description,
    sourceAttribution = sourceAttribution,
    trustState = ContentTrustState.valueOf(trustState),
    checksumSha256 = checksumSha256,
    installedAtEpochMs = installedAtEpochMs,
    lastSyncEpochMs = lastSyncEpochMs,
)

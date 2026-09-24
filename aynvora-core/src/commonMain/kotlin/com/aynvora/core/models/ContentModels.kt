package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Independent knowledge module domains in AYNVORA.
 *
 * Each module maintains independent domain logic and content contracts.
 */
@Serializable
enum class ContentModuleId {
    ASTROLOGY,
    TAROT,
    GARUD_PURAN,
    TANTRA,
}

/**
 * Editorial and cryptographic trust states for offline content packs.
 *
 * Only content in [APPROVED_FOR_PUBLICATION] state is exposed as trusted content to the UI.
 */
@Serializable
enum class ContentTrustState {
    DOWNLOADED_UNVERIFIED,
    VERIFIED,
    APPROVED_FOR_PUBLICATION,
    DEPRECATED,
    REVOKED,
}

/**
 * Cryptographically signed, versioned manifest describing a downloadable content pack.
 */
@Serializable
data class ContentManifest(
    val packId: String,
    val moduleId: ContentModuleId,
    val contentVersion: Int,
    val language: String,
    val schemaVersion: Int,
    val title: String,
    val description: String? = null,
    val sourceAttribution: String,
    val publicationDate: String,
    val lastReviewedDate: String,
    val downloadUrl: String,
    val checksumSha256: String,
    val expectedSizeBytes: Long,
    val signatureKeyId: String,
    val signature: String,
    val compatibilityMinAppVersion: String = "1.0.0",
) {
    init {
        require(packId.isNotBlank()) { "packId cannot be blank" }
        require(contentVersion > 0) { "contentVersion must be positive, got: $contentVersion" }
        require(language.isNotBlank()) { "language cannot be blank" }
        require(schemaVersion > 0) { "schemaVersion must be positive, got: $schemaVersion" }
        require(sourceAttribution.isNotBlank()) { "sourceAttribution cannot be blank" }
        require(checksumSha256.length == 64) { "checksumSha256 must be a 64-char hex string" }
        require(expectedSizeBytes > 0) { "expectedSizeBytes must be > 0" }
    }
}

/**
 * Domain representation of an installed, versioned content pack.
 */
@Serializable
data class ContentPack(
    val packId: String,
    val moduleId: ContentModuleId,
    val contentVersion: Int,
    val language: String,
    val title: String,
    val description: String? = null,
    val sourceAttribution: String,
    val trustState: ContentTrustState,
    val checksumSha256: String,
    val installedAtEpochMs: Long,
    val lastSyncEpochMs: Long,
)

/**
 * Domain representation of an individual structured knowledge or reference item.
 */
@Serializable
data class ContentItem(
    val id: String,
    val packId: String,
    val moduleId: ContentModuleId,
    val itemKey: String,
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val language: String,
    val metadataJson: String? = null,
    val orderIndex: Int = 0,
    val tags: List<String> = emptyList(),
    val trustState: ContentTrustState = ContentTrustState.APPROVED_FOR_PUBLICATION,
)

/**
 * Granular observable state for the synchronization pipeline.
 */
@Serializable
enum class SyncStatus {
    IDLE,
    CHECKING_CONNECTIVITY,
    FETCHING_MANIFEST,
    DOWNLOADING_CONTENT,
    VERIFYING_INTEGRITY,
    COMMITTING_TRANSACTION,
    COMPLETED,
    FAILED,
}

/**
 * Result outcome of a synchronization attempt.
 */
@Serializable
sealed class SyncResult {
    @Serializable
    data class Success(
        val packsUpdated: Int,
        val itemsUpdated: Int,
        val timestampEpochMs: Long,
    ) : SyncResult()

    @Serializable
    data class NoOp(
        val reason: String,
    ) : SyncResult()

    @Serializable
    data class Failure(
        val code: String,
        val message: String,
        val isRetryable: Boolean = true,
    ) : SyncResult()
}

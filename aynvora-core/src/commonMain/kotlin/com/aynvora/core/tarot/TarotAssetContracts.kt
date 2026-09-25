package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult

/**
 * Strict validator for deck manifests, card image integrity, and checksums.
 * Fails closed on missing cards, mismatched counts, corrupt bytes, or invalid checksums.
 */
interface TarotAssetVerifier {
    /**
     * Verifies manifest structural consistency:
     * - exactly 78 cards
     * - 22 Major Arcana + 56 Minor Arcana (14 per suit)
     * - unique non-blank card IDs
     * - valid dimensions
     * - valid license metadata
     */
    fun verifyManifest(manifest: TarotDeckManifest): AynvoraResult<Unit>

    /**
     * Verifies that the provided raw image bytes match the SHA-256 digest in the asset descriptor.
     */
    fun verifyCardAsset(
        card: TarotCardAsset,
        assetBytes: ByteArray,
        isThumbnail: Boolean = false
    ): AynvoraResult<Unit>
}

/**
 * Repository interface for accessing verified local/bundled Tarot card assets and deck manifests.
 */
interface TarotAssetRepository {
    /**
     * Retrieves all locally installed decks with their operational status.
     */
    suspend fun getInstalledDecks(): AynvoraResult<List<TarotDeckDescriptor>>

    /**
     * Loads the validated manifest for a specific deck.
     */
    suspend fun getDeckManifest(deckId: String): AynvoraResult<TarotDeckManifest>

    /**
     * Loads the metadata descriptor for a deck.
     */
    suspend fun getDeckMetadata(deckId: String): AynvoraResult<TarotDeckMetadata>

    /**
     * Loads the license document for a deck.
     */
    suspend fun getDeckLicense(deckId: String): AynvoraResult<TarotDeckLicenseInfo>

    /**
     * Loads raw JPEG bytes for a specific card.
     * Returns typed failure if missing, unverified, or corrupt.
     */
    suspend fun getCardImage(
        deckId: String,
        cardId: String,
        isThumbnail: Boolean = false,
    ): AynvoraResult<ByteArray>

    /**
     * Loads raw JPEG bytes for the deck's card back.
     */
    suspend fun getCardBackImage(
        deckId: String,
        isThumbnail: Boolean = false,
    ): AynvoraResult<ByteArray>

    /**
     * Returns true only if the deck is fully installed, verified, and in [TarotDeckInstallationState.READY] state.
     */
    suspend fun isDeckReady(deckId: String): Boolean
}

/**
 * Downloader contract for optional remote deck packages.
 */
interface TarotAssetDownloader {
    suspend fun downloadDeck(
        deckId: String,
        targetVersion: Int,
        onProgress: (Float) -> Unit = {},
    ): AynvoraResult<TarotStagedDeckPackage>
}

/**
 * Atomic installer and rollback manager for deck packages.
 * Guarantees that a failed update keeps the current valid version and never leaves a partial deck.
 */
interface TarotAssetInstaller {
    suspend fun installDeck(stagedPackage: TarotStagedDeckPackage): AynvoraResult<Unit>
    suspend fun rollback(deckId: String, fallbackVersion: Int): AynvoraResult<Unit>
    suspend fun deleteDeck(deckId: String): AynvoraResult<Unit>
}

/**
 * High-level coordinator managing deck lifecycle, selection, and updates.
 */
interface TarotDeckAssetManager {
    suspend fun listDecks(): AynvoraResult<List<TarotDeckDescriptor>>
    suspend fun getActiveDeckId(): String
    suspend fun selectDeck(deckId: String): AynvoraResult<Unit>
    suspend fun downloadAndInstallDeck(
        deckId: String,
        onProgress: (Float) -> Unit = {},
    ): AynvoraResult<Unit>
}

package com.aynvora.tarot

import kotlinx.serialization.Serializable

/**
 * Pixel dimensions for an image asset.
 */
@Serializable
data class TarotAssetDimensions(
    val width: Int,
    val height: Int,
)

/**
 * Immutable typed metadata and verification descriptor for a single card asset.
 * Preserves full provenance, licensing, dimensions, and cryptographic checksum.
 */
@Serializable
data class TarotCardAsset(
    val deckId: String,
    val cardId: String,
    val canonicalName: String,
    val arcana: String,
    val suit: String? = null,
    val rank: Int,
    val sourceUrl: String,
    val sourceProvider: String,
    val license: String,
    val attribution: String,
    val imageReference: String,
    val thumbnailReference: String? = null,
    val originalDimensions: TarotAssetDimensions,
    val processedDimensions: TarotAssetDimensions,
    val thumbnailDimensions: TarotAssetDimensions? = null,
    val checksum: String,
    val thumbnailChecksum: String? = null,
    val assetVersion: Int = 1,
)

/**
 * Manifest document declaring all validated card assets for a deck.
 */
@Serializable
data class TarotDeckManifest(
    val deckId: String,
    val deckName: String,
    val version: Int,
    val cardCount: Int,
    val majorArcanaCount: Int = 22,
    val minorArcanaCount: Int = 56,
    val backAsset: TarotCardAsset,
    val cards: List<TarotCardAsset>,
    val checksum: String? = null,
)

/**
 * Comprehensive descriptive metadata for a Tarot Deck.
 */
@Serializable
data class TarotDeckMetadata(
    val deckId: String,
    val name: String,
    val alternativeNames: List<String> = emptyList(),
    val artist: String,
    val designer: String,
    val publisher: String,
    val publicationYear: Int,
    val deckType: String,
    val cardCount: Int,
    val majorArcanaCount: Int,
    val minorArcanaCount: Int,
    val suits: List<String>,
    val sourceUrl: String,
    val sourceRestoration: String,
    val license: String,
    val description: String,
)

/**
 * Legal rights and licensing verification document for a Tarot Deck.
 */
@Serializable
data class TarotDeckLicenseInfo(
    val deckId: String,
    val licenseType: String,
    val legalStatus: String,
    val copyrightNotice: String,
    val attributionRequirement: String,
    val commercialUsePermitted: Boolean,
    val modificationPermitted: Boolean,
    val redistributionPermitted: Boolean,
    val verifiedDate: String,
    val verifier: String,
)

/**
 * Installation and operational lifecycle states for Tarot Decks.
 * A deck must NOT be shown as READY or selectable before verification succeeds.
 */
@Serializable
enum class TarotDeckInstallationState {
    INSTALLED,
    DOWNLOAD_AVAILABLE,
    DOWNLOAD_REQUIRED,
    DOWNLOADING,
    VERIFYING,
    INSTALLING,
    READY,
    UPDATE_AVAILABLE,
    FAILED,
    INVALID,
    UNSUPPORTED,
}

/**
 * High-level presentation descriptor of a deck and its operational status.
 */
@Serializable
data class TarotDeckDescriptor(
    val deckId: String,
    val name: String,
    val description: String,
    val cardCount: Int,
    val version: Int,
    val installationState: TarotDeckInstallationState,
    val isBundled: Boolean = false,
    val isCurrent: Boolean = false,
    val downloadProgressPercent: Float = 0f,
    val errorMessage: String? = null,
    val licenseType: String = "Public Domain",
    val attribution: String = "",
)

/**
 * Typed state of a card image during loading, rendering, or failure.
 * Ensures the UI never crashes on missing or corrupted images.
 */
sealed interface TarotCardImageState {
    data class Available(val bytes: ByteArray, val dimensions: TarotAssetDimensions) :
        TarotCardImageState

    data object Loading : TarotCardImageState
    data class Missing(val cardId: String, val reason: String) : TarotCardImageState
    data class Corrupted(val cardId: String, val reason: String) : TarotCardImageState
    data class Unsupported(val cardId: String, val reason: String) : TarotCardImageState
}

/**
 * Staged package used during deck download or update before atomic activation.
 */
data class TarotStagedDeckPackage(
    val deckId: String,
    val version: Int,
    val manifest: TarotDeckManifest,
    val metadata: TarotDeckMetadata,
    val license: TarotDeckLicenseInfo,
    val displayImages: Map<String, ByteArray>,
    val thumbnailImages: Map<String, ByteArray>,
    val backDisplayImage: ByteArray,
    val backThumbnailImage: ByteArray,
)

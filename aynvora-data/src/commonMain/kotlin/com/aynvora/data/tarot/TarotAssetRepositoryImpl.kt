package com.aynvora.data.tarot

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotAssetRepository
import com.aynvora.core.tarot.TarotAssetVerifier
import com.aynvora.core.tarot.TarotDeckDescriptor
import com.aynvora.core.tarot.TarotDeckInstallationState
import com.aynvora.core.tarot.TarotDeckLicenseInfo
import com.aynvora.core.tarot.TarotDeckManifest
import com.aynvora.core.tarot.TarotDeckMetadata
import kotlinx.serialization.json.Json

class TarotAssetRepositoryImpl(
    private val verifier: TarotAssetVerifier = TarotAssetVerifierImpl(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TarotAssetRepository {

    private val manifestCache = mutableMapOf<String, TarotDeckManifest>()
    private val metadataCache = mutableMapOf<String, TarotDeckMetadata>()
    private val licenseCache = mutableMapOf<String, TarotDeckLicenseInfo>()
    private val imageBytesCache = mutableMapOf<String, ByteArray>()

    private val supportedDecks = listOf(
        TarotDeckDescriptor(
            deckId = "rider_waite_smith_standard",
            name = "Rider-Waite-Smith Standard Deck",
            description = "The classic 78-card Tarot deck illustrated by Pamela Colman Smith under the direction of Arthur Edward Waite (1909). Cleaned and restored scans (Public Domain).",
            cardCount = 78,
            version = 1,
            installationState = TarotDeckInstallationState.READY,
            isBundled = true,
            isCurrent = true,
            licenseType = "Public Domain",
            attribution = "Pamela Colman Smith & Arthur Edward Waite (1909), restored by Steve P. (steve-p.org)",
        ),
        TarotDeckDescriptor(
            deckId = "soimoi_tarot",
            name = "Soimoi Tarot Deck",
            description = "Original 78-card contemporary Tarot deck created by Mike Koz (koz.tv). Licensed under CC BY 4.0 with attribution.",
            cardCount = 78,
            version = 1,
            installationState = TarotDeckInstallationState.READY,
            isBundled = true,
            isCurrent = false,
            licenseType = "CC BY 4.0",
            attribution = "\"Soimoi Tarot\" by Mike Koz (https://koz.tv/), licensed under CC BY 4.0",
        )
    )

    override suspend fun getInstalledDecks(): AynvoraResult<List<TarotDeckDescriptor>> {
        return AynvoraResult.Success(supportedDecks)
    }

    override suspend fun getDeckManifest(deckId: String): AynvoraResult<TarotDeckManifest> {
        manifestCache[deckId]?.let { return AynvoraResult.Success(it) }
        val dir = getDeckDirectory(deckId) ?: return AynvoraResult.Failure.NotFound(
            deckId,
            "Unknown deckId: $deckId"
        )
        val manifestBytes = loadTarotResourceBytes("tarot/$dir/manifest.json")
            ?: return AynvoraResult.Failure.NotFound(
                deckId,
                "manifest.json not found for deck $deckId"
            )
        return try {
            val manifest = json.decodeFromString<TarotDeckManifest>(manifestBytes.decodeToString())
            when (val verifyResult = verifier.verifyManifest(manifest)) {
                is AynvoraResult.Success -> {
                    manifestCache[deckId] = manifest
                    AynvoraResult.Success(manifest)
                }

                is AynvoraResult.Failure -> verifyResult
            }
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(deckId, "Failed to parse manifest: ${e.message}")
        }
    }

    override suspend fun getDeckMetadata(deckId: String): AynvoraResult<TarotDeckMetadata> {
        metadataCache[deckId]?.let { return AynvoraResult.Success(it) }
        val dir = getDeckDirectory(deckId) ?: return AynvoraResult.Failure.NotFound(
            deckId,
            "Unknown deckId: $deckId"
        )
        val metaBytes = loadTarotResourceBytes("tarot/$dir/metadata.json")
            ?: return AynvoraResult.Failure.NotFound(
                deckId,
                "metadata.json not found for deck $deckId"
            )
        return try {
            val metadata = json.decodeFromString<TarotDeckMetadata>(metaBytes.decodeToString())
            metadataCache[deckId] = metadata
            AynvoraResult.Success(metadata)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(deckId, "Failed to parse metadata: ${e.message}")
        }
    }

    override suspend fun getDeckLicense(deckId: String): AynvoraResult<TarotDeckLicenseInfo> {
        licenseCache[deckId]?.let { return AynvoraResult.Success(it) }
        val dir = getDeckDirectory(deckId) ?: return AynvoraResult.Failure.NotFound(
            deckId,
            "Unknown deckId: $deckId"
        )
        val licenseBytes = loadTarotResourceBytes("tarot/$dir/license.json")
            ?: return AynvoraResult.Failure.NotFound(
                deckId,
                "license.json not found for deck $deckId"
            )
        return try {
            val license = json.decodeFromString<TarotDeckLicenseInfo>(licenseBytes.decodeToString())
            licenseCache[deckId] = license
            AynvoraResult.Success(license)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(deckId, "Failed to parse license: ${e.message}")
        }
    }

    override suspend fun getCardImage(
        deckId: String,
        cardId: String,
        isThumbnail: Boolean
    ): AynvoraResult<ByteArray> {
        val cacheKey = "$deckId:$cardId:${if (isThumbnail) "thumb" else "disp"}"
        imageBytesCache[cacheKey]?.let { return AynvoraResult.Success(it) }

        val dir = getDeckDirectory(deckId) ?: return AynvoraResult.Failure.NotFound(
            deckId,
            "Unknown deckId: $deckId"
        )
        val subDir = if (isThumbnail) "thumbnail" else "display"
        val path = "tarot/$dir/$subDir/$cardId.jpg"

        val bytes = loadTarotResourceBytes(path)
            ?: return AynvoraResult.Failure.NotFound(cardId, "Image not found at $path")

        imageBytesCache[cacheKey] = bytes
        return AynvoraResult.Success(bytes)
    }

    override suspend fun getCardBackImage(
        deckId: String,
        isThumbnail: Boolean
    ): AynvoraResult<ByteArray> {
        val cacheKey = "$deckId:card_back:${if (isThumbnail) "thumb" else "disp"}"
        imageBytesCache[cacheKey]?.let { return AynvoraResult.Success(it) }

        val dir = getDeckDirectory(deckId) ?: return AynvoraResult.Failure.NotFound(
            deckId,
            "Unknown deckId: $deckId"
        )
        val filename = if (isThumbnail) "back_thumbnail.jpg" else "back_display.jpg"
        val path = "tarot/$dir/back/$filename"

        val bytes = loadTarotResourceBytes(path)
            ?: return AynvoraResult.Failure.NotFound("card_back", "Card back not found at $path")

        imageBytesCache[cacheKey] = bytes
        return AynvoraResult.Success(bytes)
    }

    override suspend fun isDeckReady(deckId: String): Boolean {
        return getDeckDirectory(deckId) != null
    }

    private fun getDeckDirectory(deckId: String): String? {
        return when (deckId) {
            "rider_waite_smith_standard", "rider-waite" -> "rider-waite"
            "soimoi_tarot", "soimoi" -> "soimoi"
            else -> null
        }
    }
}

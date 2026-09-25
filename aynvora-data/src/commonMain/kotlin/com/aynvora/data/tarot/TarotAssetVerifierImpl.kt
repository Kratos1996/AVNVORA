package com.aynvora.data.tarot

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotAssetVerifier
import com.aynvora.core.tarot.TarotCardAsset
import com.aynvora.core.tarot.TarotDeckManifest

class TarotAssetVerifierImpl : TarotAssetVerifier {

    override fun verifyManifest(manifest: TarotDeckManifest): AynvoraResult<Unit> {
        if (manifest.cardCount != 78) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = manifest.deckId,
                message = "Manifest cardCount (${manifest.cardCount}) != 78"
            )
        }
        if (manifest.cards.size != 78) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = manifest.deckId,
                message = "Manifest actual cards count (${manifest.cards.size}) != 78"
            )
        }
        if (manifest.majorArcanaCount != 22) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = manifest.deckId,
                message = "Major arcana count (${manifest.majorArcanaCount}) != 22"
            )
        }
        if (manifest.minorArcanaCount != 56) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = manifest.deckId,
                message = "Minor arcana count (${manifest.minorArcanaCount}) != 56"
            )
        }

        val cardIds = mutableSetOf<String>()
        var majorCount = 0
        val suitCounts = mutableMapOf<String, Int>()

        for (card in manifest.cards) {
            if (card.cardId.isBlank()) {
                return AynvoraResult.Failure.CorruptedData(
                    manifest.deckId,
                    "Empty cardId in manifest"
                )
            }
            if (!cardIds.add(card.cardId)) {
                return AynvoraResult.Failure.CorruptedData(
                    manifest.deckId,
                    "Duplicate cardId: ${card.cardId}"
                )
            }
            if (card.deckId != manifest.deckId) {
                return AynvoraResult.Failure.CorruptedData(
                    manifest.deckId,
                    "Card ${card.cardId} deckId ${card.deckId} != ${manifest.deckId}"
                )
            }
            if (card.arcana == "MAJOR") {
                majorCount++
            } else if (card.arcana == "MINOR") {
                val s = card.suit ?: return AynvoraResult.Failure.CorruptedData(
                    manifest.deckId,
                    "Minor card ${card.cardId} missing suit"
                )
                suitCounts[s] = (suitCounts[s] ?: 0) + 1
            }
        }

        if (majorCount != 22) {
            return AynvoraResult.Failure.CorruptedData(
                manifest.deckId,
                "Verified major arcana count $majorCount != 22"
            )
        }
        val expectedSuits = setOf("WANDS", "CUPS", "SWORDS", "PENTACLES")
        if (suitCounts.keys != expectedSuits) {
            return AynvoraResult.Failure.CorruptedData(
                manifest.deckId,
                "Suits ${suitCounts.keys} != $expectedSuits"
            )
        }
        for ((suit, count) in suitCounts) {
            if (count != 14) {
                return AynvoraResult.Failure.CorruptedData(
                    manifest.deckId,
                    "Suit $suit count $count != 14"
                )
            }
        }

        return AynvoraResult.Success(Unit)
    }

    override fun verifyCardAsset(
        card: TarotCardAsset,
        assetBytes: ByteArray,
        isThumbnail: Boolean
    ): AynvoraResult<Unit> {
        if (assetBytes.isEmpty()) {
            return AynvoraResult.Failure.CorruptedData(card.cardId, "Card asset bytes are empty")
        }
        val expectedChecksum =
            if (isThumbnail) card.thumbnailChecksum ?: card.checksum else card.checksum
        val actualChecksum = GarudaChecksumVerifier.calculateSha256(assetBytes)
        if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
            return AynvoraResult.Failure.CorruptedData(
                card.cardId,
                "Checksum mismatch for ${card.cardId} (isThumbnail=$isThumbnail): expected $expectedChecksum, got $actualChecksum"
            )
        }
        return AynvoraResult.Success(Unit)
    }
}

package com.aynvora.data.tarot

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotAssetDimensions
import com.aynvora.core.tarot.TarotCardAsset
import com.aynvora.core.tarot.TarotDeckManifest
import com.aynvora.core.tarot.TarotStandardDeck
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TarotRealArtworkTest {

    private val repository = TarotAssetRepositoryImpl()
    private val verifier = TarotAssetVerifierImpl()

    @Test
    fun test01_riderWaiteCardCountIsExactly78() = runBlocking {
        val manifestResult = repository.getDeckManifest("rider_waite_smith_standard")
        assertTrue(manifestResult is AynvoraResult.Success)
        val manifest = manifestResult.value
        assertEquals(78, manifest.cards.size)
        assertEquals(78, manifest.cardCount)
        assertEquals("rider_waite_smith_standard", manifest.deckId)
    }

    @Test
    fun test02_everyCardHasRealDisplayAndThumbnailAssets() = runBlocking {
        val manifestResult = repository.getDeckManifest("rider_waite_smith_standard")
        assertTrue(manifestResult is AynvoraResult.Success)
        val manifest = manifestResult.value

        for (card in manifest.cards) {
            val dispResult = repository.getCardImage(
                "rider_waite_smith_standard",
                card.cardId,
                isThumbnail = false
            )
            assertTrue(
                dispResult is AynvoraResult.Success,
                "Display image missing for card: ${card.cardId}"
            )
            assertTrue(
                dispResult.value.isNotEmpty(),
                "Display image empty for card: ${card.cardId}"
            )

            val thumbResult = repository.getCardImage(
                "rider_waite_smith_standard",
                card.cardId,
                isThumbnail = true
            )
            assertTrue(
                thumbResult is AynvoraResult.Success,
                "Thumbnail image missing for card: ${card.cardId}"
            )
            assertTrue(
                thumbResult.value.isNotEmpty(),
                "Thumbnail image empty for card: ${card.cardId}"
            )
        }
    }

    @Test
    fun test03_noDuplicateCardIdsOrAssetFilenames() = runBlocking {
        val manifestResult = repository.getDeckManifest("rider_waite_smith_standard")
        assertTrue(manifestResult is AynvoraResult.Success)
        val manifest = manifestResult.value

        val cardIds = manifest.cards.map { it.cardId }
        val uniqueCardIds = cardIds.toSet()
        assertEquals(78, uniqueCardIds.size, "Found duplicate cardIds in manifest")

        val displayFiles = manifest.cards.map { it.imageReference }
        assertEquals(78, displayFiles.toSet().size, "Found duplicate display filenames")

        val thumbnailFiles = manifest.cards.mapNotNull { it.thumbnailReference }
        assertEquals(78, thumbnailFiles.toSet().size, "Found duplicate thumbnail filenames")
    }

    @Test
    fun test04_cardToImageMappingMatchesCanonicalDeck() = runBlocking {
        val manifestResult = repository.getDeckManifest("rider_waite_smith_standard")
        assertTrue(manifestResult is AynvoraResult.Success)
        val manifest = manifestResult.value
        val manifestMap = manifest.cards.associateBy { it.cardId }

        for (canonicalCard in TarotStandardDeck.AllCards) {
            val mapped = manifestMap[canonicalCard.id]
            assertNotNull(
                mapped,
                "Canonical card ${canonicalCard.id} (${canonicalCard.name}) not found in manifest"
            )
            assertEquals(canonicalCard.id, mapped.cardId)
            assertEquals("display/${canonicalCard.id}.jpg", mapped.imageReference)
            assertEquals("thumbnail/${canonicalCard.id}.jpg", mapped.thumbnailReference)
            assertFalse(mapped.checksum.isBlank())
            assertFalse(mapped.thumbnailChecksum.isNullOrBlank())
        }
    }

    @Test
    fun test05_selectedDeckConsistency() = runBlocking {
        val rwsManifest = repository.getDeckManifest("rider_waite_smith_standard")
        assertTrue(rwsManifest is AynvoraResult.Success)

        val soimoiManifest = repository.getDeckManifest("soimoi_tarot")
        assertTrue(soimoiManifest is AynvoraResult.Success)

        // Verify Rider-Waite images and Soimoi images are distinct
        val rwsFool = repository.getCardImage(
            "rider_waite_smith_standard",
            "major_00_fool",
            isThumbnail = false
        )
        val soimoiFool =
            repository.getCardImage("soimoi_tarot", "major_00_fool", isThumbnail = false)

        assertTrue(rwsFool is AynvoraResult.Success)
        assertTrue(soimoiFool is AynvoraResult.Success)
        assertFalse(
            rwsFool.value.contentEquals(soimoiFool.value),
            "Rider-Waite and Soimoi must have distinct artwork"
        )
    }

    @Test
    fun test06_soimoiDeckHasExactly78Cards() = runBlocking {
        val manifestResult = repository.getDeckManifest("soimoi_tarot")
        assertTrue(manifestResult is AynvoraResult.Success)
        val manifest = manifestResult.value
        assertEquals(78, manifest.cards.size)
        assertEquals(78, manifest.cardCount)
        assertEquals("soimoi_tarot", manifest.deckId)
    }

    @Test
    fun test07_cardBackExistsForBothDecks() = runBlocking {
        val rwsBackDisp =
            repository.getCardBackImage("rider_waite_smith_standard", isThumbnail = false)
        assertTrue(rwsBackDisp is AynvoraResult.Success)
        assertTrue(rwsBackDisp.value.isNotEmpty())

        val rwsBackThumb =
            repository.getCardBackImage("rider_waite_smith_standard", isThumbnail = true)
        assertTrue(rwsBackThumb is AynvoraResult.Success)
        assertTrue(rwsBackThumb.value.isNotEmpty())

        val soimoiBackDisp = repository.getCardBackImage("soimoi_tarot", isThumbnail = false)
        assertTrue(soimoiBackDisp is AynvoraResult.Success)
        assertTrue(soimoiBackDisp.value.isNotEmpty())

        val soimoiBackThumb = repository.getCardBackImage("soimoi_tarot", isThumbnail = true)
        assertTrue(soimoiBackThumb is AynvoraResult.Success)
        assertTrue(soimoiBackThumb.value.isNotEmpty())
    }

    @Test
    fun test08_licenseMetadataExistsForBothDecks() = runBlocking {
        val rwsLicense = repository.getDeckLicense("rider_waite_smith_standard")
        assertTrue(rwsLicense is AynvoraResult.Success)
        assertTrue(rwsLicense.value.licenseType.contains("PUBLIC_DOMAIN", ignoreCase = true))
        assertTrue(rwsLicense.value.commercialUsePermitted)
        assertTrue(rwsLicense.value.attributionRequirement.contains("Pamela Colman Smith"))

        val soimoiLicense = repository.getDeckLicense("soimoi_tarot")
        assertTrue(soimoiLicense is AynvoraResult.Success)
        assertEquals("CC BY 4.0", soimoiLicense.value.licenseType)
        assertTrue(soimoiLicense.value.commercialUsePermitted)
        assertTrue(soimoiLicense.value.attributionRequirement.contains("Mike Koz"))
    }

    @Test
    fun test09_metadataExistsWithAuthorAndProvenance() = runBlocking {
        val rwsMeta = repository.getDeckMetadata("rider_waite_smith_standard")
        assertTrue(rwsMeta is AynvoraResult.Success)
        assertTrue(rwsMeta.value.artist.contains("Pamela Colman Smith"))
        assertTrue(rwsMeta.value.designer.contains("Arthur Edward Waite"))
        assertEquals(1909, rwsMeta.value.publicationYear)

        val soimoiMeta = repository.getDeckMetadata("soimoi_tarot")
        assertTrue(soimoiMeta is AynvoraResult.Success)
        assertTrue(soimoiMeta.value.artist.contains("Mike Koz"))
        assertEquals(2025, soimoiMeta.value.publicationYear)
    }

    @Test
    fun test10_missingCardImageFailsGracefully() = runBlocking {
        val missingResult = repository.getCardImage(
            "rider_waite_smith_standard",
            "non_existent_card_99",
            isThumbnail = false
        )
        assertTrue(missingResult is AynvoraResult.Failure.NotFound)
    }

    @Test
    fun test11_missingDeckFailsGracefully() = runBlocking {
        val missingDeckResult = repository.getDeckManifest("unknown_deck_xyz")
        assertTrue(missingDeckResult is AynvoraResult.Failure.NotFound)
    }

    @Test
    fun test12_verifierManifestValidationFailsOnTamperedCardCount() {
        val dummyBack = TarotCardAsset(
            deckId = "tampered",
            cardId = "card_back",
            canonicalName = "Card Back",
            arcana = "NONE",
            rank = -1,
            sourceUrl = "",
            sourceProvider = "",
            license = "",
            attribution = "",
            imageReference = "back/back_display.jpg",
            originalDimensions = TarotAssetDimensions(360, 600),
            processedDimensions = TarotAssetDimensions(360, 600),
            checksum = "abc",
        )
        val badManifest = TarotDeckManifest(
            deckId = "tampered_deck",
            deckName = "Tampered",
            version = 1,
            cardCount = 77, // Invalid card count
            backAsset = dummyBack,
            cards = emptyList(),
        )
        val result = verifier.verifyManifest(badManifest)
        assertTrue(result is AynvoraResult.Failure.CorruptedData)
    }

    @Test
    fun test13_installedDecksListsBothSupportedDecks() = runBlocking {
        val decksResult = repository.getInstalledDecks()
        assertTrue(decksResult is AynvoraResult.Success)
        val decks = decksResult.value
        assertEquals(2, decks.size)
        assertTrue(decks.any { it.deckId == "rider_waite_smith_standard" && it.isCurrent })
        assertTrue(decks.any { it.deckId == "soimoi_tarot" })
    }
}

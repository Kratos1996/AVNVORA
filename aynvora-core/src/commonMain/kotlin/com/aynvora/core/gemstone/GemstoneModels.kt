package com.aynvora.core.gemstone

import com.aynvora.core.models.CelestialBody
import com.aynvora.core.result.AynvoraResult

/**
 * Vedic Navaratna gemstone types.
 */
enum class GemstoneType(
    val sanskritName: String,
    val primaryPlanet: CelestialBody,
) {
    RUBY("Manikya", CelestialBody.SUN),
    NATURAL_PEARL("Mukta", CelestialBody.MOON),
    RED_CORAL("Moonga", CelestialBody.MARS),
    EMERALD("Marakata", CelestialBody.MERCURY),
    YELLOW_SAPPHIRE("Pukhraj", CelestialBody.JUPITER),
    DIAMOND("Vajra", CelestialBody.VENUS),
    BLUE_SAPPHIRE("Neelam", CelestialBody.SATURN),
    HESSONITE("Gomed", CelestialBody.RAHU),
    CATS_EYE("Vaidurya", CelestialBody.KETU),
}

/**
 * Metal setting for wearing gemstones.
 */
enum class GemstoneMetal {
    GOLD,
    SILVER,
    PANCHADHATU,
    ASHTADHATU,
    COPPER,
    BRASS,
}

/**
 * User-entered or scanned lab certificate metadata for authenticity provenance.
 */
data class GemstoneCertificate(
    val certificateNumber: String,
    val labName: String,
    val issueDateEpochMs: Long? = null,
    val reportedCaratWeight: Double? = null,
    val identifiedSpecies: String? = null,
    val treatmentObservations: String? = null,
    val verificationUrl: String? = null,
)

/**
 * Item representing an active gemstone currently worn or owned by the user.
 * Enables inventory-aware evaluation to prevent planetary conflict.
 */
data class GemstoneInventoryItem(
    val id: String,
    val type: GemstoneType,
    val approximateCaratWeight: Double,
    val metal: GemstoneMetal,
    val fingerOrPlacement: String,
    val isCurrentlyWorn: Boolean,
    val certificate: GemstoneCertificate? = null,
    val notes: String? = null,
)

/**
 * Context of all gemstones currently worn by the user.
 */
data class GemstoneWearingContext(
    val wornItems: List<GemstoneInventoryItem>,
)

/**
 * Astrologically derived gemstone recommendation with ethical guardrails.
 */
data class GemstoneRecommendation(
    val gemstoneType: GemstoneType,
    val associatedPlanet: CelestialBody,
    val rationale: String,
    val recommendedFinger: String,
    val recommendedMetal: GemstoneMetal,
    val compatibleCurrentlyWorn: Boolean,
    val cautionaryNotes: List<String> = emptyList(),
    val ethicalDisclaimer: String = "Gemstones are traditional symbolic and vibrational supports in Jyotish. They are never mandatory to avoid harm and do not guarantee material or medical outcomes.",
)

/**
 * Gemstone domain repository contract.
 */
interface GemstoneRepository {
    suspend fun getInventory(): AynvoraResult<List<GemstoneInventoryItem>>
    suspend fun saveInventoryItem(item: GemstoneInventoryItem): AynvoraResult<Unit>
    suspend fun deleteInventoryItem(id: String): AynvoraResult<Unit>
    suspend fun getRecommendations(wearingContext: GemstoneWearingContext): AynvoraResult<List<GemstoneRecommendation>>
}

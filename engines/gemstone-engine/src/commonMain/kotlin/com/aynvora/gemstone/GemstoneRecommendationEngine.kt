package com.aynvora.gemstone

import com.aynvora.contracts.CelestialBody
import kotlinx.serialization.Serializable

/**
 * Traditional classification of a gemstone recommendation in Jyotisha.
 */
@Serializable
enum class GemstoneRecommendationCategory {
    JEEVAN_RATNA,   // Life Stone (Lagna / 1st House Lord)
    BHAGYA_RATNA,   // Fortune Stone (9th House Lord)
    PUNYA_RATNA,    // Auspicious Wisdom / Intellect Stone (5th House Lord)
    REMEDIAL_CAUTION, // Specifically cautioned or contraindicated
}

/**
 * Structured individual gemstone recommendation item.
 */
@Serializable
data class RecommendedGemstoneItem(
    val category: GemstoneRecommendationCategory,
    val title: String,
    val gemstoneType: GemstoneType,
    val associatedPlanet: CelestialBody,
    val rationale: String,
    val recommendedMetal: GemstoneMetal,
    val recommendedFinger: TraditionalFinger,
    val recommendedDayTime: String,
    val compatibilityWithInventory: GemstoneCompatibilityStatus,
    val inventoryWarnings: List<String> = emptyList(),
)

/**
 * Complete recommendation package generated from birth chart and wearing inventory.
 */
@Serializable
data class GemstoneRecommendationPackage(
    val astroProfile: GemstoneAstrologyProfile,
    val primaryRecommendations: List<RecommendedGemstoneItem>,
    val cautionedGemstones: List<RecommendedGemstoneItem>,
    val contraindicatedGemstones: List<GemstoneType>,
    val inventoryConflictCount: Int,
    val ethicalDisclaimer: String = "Traditional gemstone recommendations are symbolic, astrological disciplines rooted in Vedic Jyotisha. They are not medical treatments, do not guarantee material outcomes, and are never mandatory. You remain the sole decision-maker.",
)

/**
 * Deterministic recommendation engine for Vedic Gemstones.
 */
object GemstoneRecommendationEngine {

    fun generateRecommendations(
        astroProfile: GemstoneAstrologyProfile,
        wearingContext: GemstoneWearingContext? = null,
    ): GemstoneRecommendationPackage {
        val primaryRecs = mutableListOf<RecommendedGemstoneItem>()
        val cautionedRecs = mutableListOf<RecommendedGemstoneItem>()
        val contraindicatedGems = mutableListOf<GemstoneType>()

        var conflictCount = 0

        fun createRecommendation(
            category: GemstoneRecommendationCategory,
            title: String,
            planet: CelestialBody,
            rationalePrefix: String,
        ): RecommendedGemstoneItem? {
            val descriptor = GemstoneCatalog.forPlanet(planet) ?: return null
            val gemType = descriptor.type

            // Evaluate against wearing context
            val compat = GemstoneCompatibilityEngine.evaluate(gemType, astroProfile, wearingContext)
            val inventoryWarnings = compat.conflictingFactors

            if (compat.status == GemstoneCompatibilityStatus.CONFLICT) {
                conflictCount++
                contraindicatedGems.add(gemType)
            }

            return RecommendedGemstoneItem(
                category = category,
                title = title,
                gemstoneType = gemType,
                associatedPlanet = planet,
                rationale = "$rationalePrefix. ${descriptor.traditionalSignificance}",
                recommendedMetal = descriptor.primaryMetal,
                recommendedFinger = descriptor.traditionalFinger,
                recommendedDayTime = descriptor.recommendedDayTime,
                compatibilityWithInventory = compat.status,
                inventoryWarnings = inventoryWarnings,
            )
        }

        // 1. Life Stone (Jeevan Ratna) — 1st House Lord
        createRecommendation(
            category = GemstoneRecommendationCategory.JEEVAN_RATNA,
            title = "Jeevan Ratna (Life Stone)",
            planet = astroProfile.lagnaLord,
            rationalePrefix = "Associated with your Lagna Lord ${astroProfile.lagnaLord.name}. Traditionally worn to support core vitality, physical immunity, and authentic self-expression",
        )?.let { primaryRecs.add(it) }

        // 2. Fortune Stone (Bhagya Ratna) — 9th House Lord
        if (astroProfile.ninthLord != astroProfile.lagnaLord) {
            createRecommendation(
                category = GemstoneRecommendationCategory.BHAGYA_RATNA,
                title = "Bhagya Ratna (Fortune Stone)",
                planet = astroProfile.ninthLord,
                rationalePrefix = "Associated with your 9th House Lord ${astroProfile.ninthLord.name}. Traditionally worn to enhance divine grace, spiritual alignment, and dharma",
            )?.let { primaryRecs.add(it) }
        }

        // 3. Auspicious Intellect Stone (Punya Ratna) — 5th House Lord
        if (astroProfile.fifthLord != astroProfile.lagnaLord && astroProfile.fifthLord != astroProfile.ninthLord) {
            createRecommendation(
                category = GemstoneRecommendationCategory.PUNYA_RATNA,
                title = "Punya Ratna (Intellect & Wisdom Stone)",
                planet = astroProfile.fifthLord,
                rationalePrefix = "Associated with your 5th House Lord ${astroProfile.fifthLord.name}. Traditionally worn to nurture creative wisdom, intellect (Buddhi), and auspicious karma",
            )?.let { primaryRecs.add(it) }
        }

        // 4. Functional Malefic & Cautionary Evaluations (Lords of 6, 8, 12)
        for (maleficPlanet in astroProfile.functionalMalefics) {
            val descriptor = GemstoneCatalog.forPlanet(maleficPlanet)
            if (descriptor != null) {
                cautionedRecs.add(
                    RecommendedGemstoneItem(
                        category = GemstoneRecommendationCategory.REMEDIAL_CAUTION,
                        title = "Caution: ${descriptor.commonName}",
                        gemstoneType = descriptor.type,
                        associatedPlanet = maleficPlanet,
                        rationale = "Rules a Dusthana house (6th, 8th, or 12th) for ${astroProfile.lagnaRashi.name} Lagna. Wearing its gem can inadvertently amplify challenges or disputes.",
                        recommendedMetal = descriptor.primaryMetal,
                        recommendedFinger = descriptor.traditionalFinger,
                        recommendedDayTime = descriptor.recommendedDayTime,
                        compatibilityWithInventory = GemstoneCompatibilityStatus.CAUTION,
                        inventoryWarnings = listOf("Classical Jyotisha contraindicates gems of functional malefic lords without expert consultation."),
                    )
                )
                contraindicatedGems.add(descriptor.type)
            }
        }

        return GemstoneRecommendationPackage(
            astroProfile = astroProfile,
            primaryRecommendations = primaryRecs,
            cautionedGemstones = cautionedRecs,
            contraindicatedGemstones = contraindicatedGems.distinct(),
            inventoryConflictCount = conflictCount,
        )
    }
}

package com.aynvora.gemstone

import com.aynvora.contracts.CelestialBody
import com.aynvora.contracts.Rashi
import kotlinx.serialization.Serializable

/**
 * Structured Vedic astrology profile needed for gemstone evaluation.
 * Extracted deterministically from the birth chart.
 */
@Serializable
data class GemstoneAstrologyProfile(
    val lagnaRashi: Rashi,
    val lagnaLord: CelestialBody,
    val fifthLord: CelestialBody,
    val ninthLord: CelestialBody,
    val moonRashi: Rashi,
    val moonLord: CelestialBody,
    val functionalBenefics: Set<CelestialBody>,
    val functionalMalefics: Set<CelestialBody>,
    val houseOccupancy: Map<CelestialBody, Int> = emptyMap(),
) {
    companion object {
        /**
         * Computes canonical sign lord according to Parashara Jyotisha.
         */
        fun getRashiLord(rashi: Rashi): CelestialBody = when (rashi) {
            Rashi.ARIES, Rashi.SCORPIO -> CelestialBody.MARS
            Rashi.TAURUS, Rashi.LIBRA -> CelestialBody.VENUS
            Rashi.GEMINI, Rashi.VIRGO -> CelestialBody.MERCURY
            Rashi.CANCER -> CelestialBody.MOON
            Rashi.LEO -> CelestialBody.SUN
            Rashi.SAGITTARIUS, Rashi.PISCES -> CelestialBody.JUPITER
            Rashi.CAPRICORN, Rashi.AQUARIUS -> CelestialBody.SATURN
        }

        /**
         * Calculates functional benefic and malefic lords for a given Lagna (Ascendant).
         *
         * Classical Parashara rule:
         * - Lords of 1st, 5th, 9th houses are functional benefics (Trikonadhipatis).
         * - Lords of 6th, 8th, 12th houses are functional malefics (Trikadhipatis),
         *   except when Lagna lord also rules 6th/8th (e.g. Mars for Aries/Scorpio, Venus for Taurus/Libra).
         */
        fun createFromLagna(lagnaRashi: Rashi, moonRashi: Rashi): GemstoneAstrologyProfile {
            val allRashis = Rashi.entries
            fun houseRashi(houseNumber: Int): Rashi {
                val index = (lagnaRashi.ordinal + houseNumber - 1) % 12
                return allRashis[index]
            }

            val lagnaLord = getRashiLord(lagnaRashi)
            val fifthLord = getRashiLord(houseRashi(5))
            val ninthLord = getRashiLord(houseRashi(9))
            val moonLord = getRashiLord(moonRashi)

            val sixthLord = getRashiLord(houseRashi(6))
            val eighthLord = getRashiLord(houseRashi(8))
            val twelfthLord = getRashiLord(houseRashi(12))

            val benefics = setOf(lagnaLord, fifthLord, ninthLord)
            val potentialMalefics = setOf(sixthLord, eighthLord, twelfthLord) - benefics

            return GemstoneAstrologyProfile(
                lagnaRashi = lagnaRashi,
                lagnaLord = lagnaLord,
                fifthLord = fifthLord,
                ninthLord = ninthLord,
                moonRashi = moonRashi,
                moonLord = moonLord,
                functionalBenefics = benefics,
                functionalMalefics = potentialMalefics,
            )
        }
    }
}

/**
 * Result of a gemstone compatibility evaluation.
 */
@Serializable
data class GemstoneCompatibilityResult(
    val gemstoneType: GemstoneType,
    val status: GemstoneCompatibilityStatus,
    val supportingFactors: List<String>,
    val conflictingFactors: List<String>,
    val explanation: String,
    val triggeredRules: List<GemstoneRule>,
    val dataCompletenessConfidence: Double,
)

/**
 * Deterministic engine for evaluating gemstone compatibility against birth chart facts
 * and active worn gemstones inventory.
 */
object GemstoneCompatibilityEngine {

    /**
     * Evaluates compatibility of a target gemstone against the user's astrological profile
     * and their currently worn gemstones.
     */
    fun evaluate(
        gemstoneType: GemstoneType,
        astroProfile: GemstoneAstrologyProfile?,
        wearingContext: GemstoneWearingContext? = null,
    ): GemstoneCompatibilityResult {
        val descriptor = GemstoneCatalog.findByType(gemstoneType)
        val planet = descriptor.primaryPlanet

        val supporting = mutableListOf<String>()
        val conflicting = mutableListOf<String>()
        val triggeredRules = mutableListOf<GemstoneRule>()

        // 1. Check Wearing Inventory Conflicts (Existing Worn Gems)
        val currentlyWorn = wearingContext?.wornItems?.filter { it.isCurrentlyWorn } ?: emptyList()
        for (wornItem in currentlyWorn) {
            val conflictRule = GemstoneRuleRegistry.findConflict(gemstoneType, wornItem.type)
            if (conflictRule != null) {
                conflicting.add(
                    "Contraindicated with currently worn ${wornItem.type.sanskritName} (${wornItem.type.name}): ${conflictRule.rationale}"
                )
                triggeredRules.add(conflictRule)
            }
        }

        // 2. Astrological Profile Evaluation
        if (astroProfile == null) {
            val hasInventoryConflict = conflicting.isNotEmpty()
            return GemstoneCompatibilityResult(
                gemstoneType = gemstoneType,
                status = if (hasInventoryConflict) GemstoneCompatibilityStatus.CONFLICT else GemstoneCompatibilityStatus.INSUFFICIENT_DATA,
                supportingFactors = supporting,
                conflictingFactors = conflicting,
                explanation = if (hasInventoryConflict) {
                    "Conflict detected with currently worn gemstones. Astrological birth chart data was not provided for deeper validation."
                } else {
                    "Astrological birth chart details (Lagna and Moon sign) are required to evaluate personalized compatibility."
                },
                triggeredRules = triggeredRules,
                dataCompletenessConfidence = if (hasInventoryConflict) 0.5 else 0.0,
            )
        }

        // Check Trikona benefic status
        val isLagnaLord = astroProfile.lagnaLord == planet
        val isNinthLord = astroProfile.ninthLord == planet
        val isFifthLord = astroProfile.fifthLord == planet
        val isMoonLord = astroProfile.moonLord == planet

        if (isLagnaLord) {
            supporting.add("Rules the 1st House (Lagna) — acts as the personal Life Stone (Jeevan Ratna), supporting health, vitality, and core identity.")
        }
        if (isNinthLord) {
            supporting.add("Rules the 9th House (Bhagya Sthana) — acts as the Fortune Stone (Bhagya Ratna), supporting auspicious fortune, higher wisdom, and dharma.")
        }
        if (isFifthLord) {
            supporting.add("Rules the 5th House (Punya Sthana) — acts as the Intellect Stone (Punya Ratna), supporting creative intellect, memory, and good karma.")
        }
        if (isMoonLord && !isLagnaLord && !isFifthLord && !isNinthLord) {
            supporting.add("Rules the Moon sign (Chandra Rashi), supporting emotional balance and mental tranquility.")
        }

        // Check Dusthana functional malefic status
        val isFunctionalMalefic = astroProfile.functionalMalefics.contains(planet)
        if (isFunctionalMalefic) {
            conflicting.add(
                "Planet ${planet.name} rules a Dusthana (difficult house: 6th, 8th, or 12th) without Lagna/Trikona ownership for ${astroProfile.lagnaRashi.name} Lagna. Wearing its gem may inadvertently amplify obstacles or debts."
            )
            triggeredRules.add(
                GemstoneRule(
                    ruleId = "dusthana_lord_${planet.name.lowercase()}",
                    ruleType = GemstoneRuleType.DUSTHANA_LORD_WARNING,
                    primaryGemstone = gemstoneType,
                    conditionDescription = "${planet.name} rules 6th/8th/12th house for ${astroProfile.lagnaRashi.name} Lagna",
                    effectStatus = GemstoneCompatibilityStatus.CAUTION,
                    rationale = "Classical rule: gems of functional malefics should not be worn without specific expert astrological remediation.",
                    sourceCitation = GemstoneSourceCitation(
                        sourceId = "bphs_dusthana",
                        title = "Brihat Parashara Hora Shastra",
                        chapterOrSection = "Adhyaya 34, Bhava Lords",
                        quoteOrSummary = "Lords of the 6th, 8th, and 12th houses produce impediments and losses.",
                    ),
                )
            )
        }

        // 3. Synthesize Final Classification
        val status: GemstoneCompatibilityStatus = when {
            conflicting.any { it.startsWith("Contraindicated") } -> GemstoneCompatibilityStatus.CONFLICT
            isFunctionalMalefic && supporting.isEmpty() -> GemstoneCompatibilityStatus.CONFLICT
            isFunctionalMalefic && supporting.isNotEmpty() -> GemstoneCompatibilityStatus.CAUTION
            supporting.isNotEmpty() -> GemstoneCompatibilityStatus.COMPATIBLE
            else -> GemstoneCompatibilityStatus.CAUTION
        }

        val explanation = when (status) {
            GemstoneCompatibilityStatus.COMPATIBLE ->
                "${descriptor.commonName} (${descriptor.sanskritName}) is astrologically supportive for your ${astroProfile.lagnaRashi.name} Lagna, with no conflicting worn stones detected."

            GemstoneCompatibilityStatus.CONFLICT ->
                "${descriptor.commonName} (${descriptor.sanskritName}) has serious classical contraindications based on your chart or currently worn gemstones."

            GemstoneCompatibilityStatus.CAUTION ->
                "${descriptor.commonName} (${descriptor.sanskritName}) has mixed or neutral indications. Traditional guidance advises caution before wearing."

            GemstoneCompatibilityStatus.INSUFFICIENT_DATA ->
                "Additional birth chart facts are needed to determine compatibility."
        }

        return GemstoneCompatibilityResult(
            gemstoneType = gemstoneType,
            status = status,
            supportingFactors = supporting,
            conflictingFactors = conflicting,
            explanation = explanation,
            triggeredRules = triggeredRules,
            dataCompletenessConfidence = 1.0,
        )
    }

    /**
     * Evaluates compatibility directly between two gemstones (e.g. comparing two items).
     */
    fun evaluatePair(gem1: GemstoneType, gem2: GemstoneType): GemstoneCompatibilityResult {
        val conflict = GemstoneRuleRegistry.findConflict(gem1, gem2)
        return if (conflict != null) {
            GemstoneCompatibilityResult(
                gemstoneType = gem1,
                status = GemstoneCompatibilityStatus.CONFLICT,
                supportingFactors = emptyList(),
                conflictingFactors = listOf(conflict.rationale),
                explanation = "Incompatible combination: ${gem1.sanskritName} and ${gem2.sanskritName} have mutual planetary enmity according to classical Jyotisha.",
                triggeredRules = listOf(conflict),
                dataCompletenessConfidence = 1.0,
            )
        } else {
            GemstoneCompatibilityResult(
                gemstoneType = gem1,
                status = GemstoneCompatibilityStatus.COMPATIBLE,
                supportingFactors = listOf("No mutual planetary enmity found between ${gem1.sanskritName} and ${gem2.sanskritName}."),
                conflictingFactors = emptyList(),
                explanation = "These two gemstones are traditionally compatible to be worn together.",
                triggeredRules = emptyList(),
                dataCompletenessConfidence = 1.0,
            )
        }
    }
}

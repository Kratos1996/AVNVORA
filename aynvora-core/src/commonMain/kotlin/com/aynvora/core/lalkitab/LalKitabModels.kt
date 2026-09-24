package com.aynvora.core.lalkitab

import com.aynvora.core.models.CelestialBody
import com.aynvora.core.result.AynvoraResult

/**
 * Traditional source editions of Lal Kitab (1939 to 1952).
 */
data class LalKitabSource(
    val editionYear: Int,
    val title: String,
    val urduOriginalReference: String? = null,
)

/**
 * Categorization of traditional Lal Kitab remedies.
 */
enum class LalKitabRemedyCategory {
    CHARITY_OR_DONATION,
    NATURE_AND_ANIMAL_FEEDING,
    HOUSEHOLD_OBJECT_PLACEMENT,
    WEARING_METAL_OR_THREAD,
    BEHAVIORAL_RESTRAINT,
}

/**
 * Structured remedy condition with ethical framing.
 */
data class LalKitabRemedy(
    val remedyId: String,
    val category: LalKitabRemedyCategory,
    val description: String,
    val prohibitions: List<String> = emptyList(),
    val durationDays: Int? = null,
    val cautionaryNote: String = "Lal Kitab remedies are traditional symbolic folk remedies. They must never involve harm to any living creature or replace practical real-world action.",
)

/**
 * Rule definition in Lal Kitab.
 */
data class LalKitabRule(
    val ruleId: String,
    val planet: CelestialBody,
    val house: Int,
    val conditionDescription: String,
    val traditionalExplanation: String,
    val remedies: List<LalKitabRemedy>,
    val source: LalKitabSource,
)

/**
 * Domain repository contract for Lal Kitab tradition.
 */
interface LalKitabRepository {
    suspend fun getRulesForPlanet(
        planet: CelestialBody,
        house: Int
    ): AynvoraResult<List<LalKitabRule>>

    suspend fun getRemediesForRule(ruleId: String): AynvoraResult<List<LalKitabRemedy>>
}

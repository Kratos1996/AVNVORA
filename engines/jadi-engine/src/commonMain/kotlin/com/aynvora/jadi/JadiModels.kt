package com.aynvora.jadi

import kotlinx.serialization.Serializable

/**
 * AYNVORA Jadi / Roots Domain — Foundation Contracts
 *
 * Phase 8.1: First-class independent core domain registration.
 * STATUS: FOUNDATION_ONLY
 *
 * Jadi (herbal roots) are used in traditional Vedic remedy systems.
 * All recommendations must carry source tradition, planetary association,
 * and strict safety/caution labeling.
 *
 * Rule 74 (Practice Feature Rule): Jadi is a first-class domain with
 * independent source requirements.
 *
 * IMPORTANT: Jadi recommendations must NEVER be presented as medical treatment.
 */

@Serializable
enum class JadiPlanetAssociation {
    SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU,
}

@Serializable
enum class JadiTradition {
    ATHARVA_VEDA,       // Atharvaveda herbal traditions
    LAL_KITAB,          // Lal Kitab folk remedy tradition
    PARASHARA_CLASSICAL, // Classical Parashara Jyotish
    FOLK_RAJASTHAN,     // Regional folk traditions
}

@Serializable
data class RootType(
    val commonName: String,
    val sanskritName: String,
    val botanicalName: String,
    val planetAssociation: JadiPlanetAssociation,
    val tradition: JadiTradition,
    val sourceText: String,
)

@Serializable
data class PracticeGuidance(
    val methodOfUse: String,
    val timingGuidance: String,
    val durationNote: String,
    val cautionNote: String,
)

@Serializable
data class JadiRecommendation(
    val root: RootType,
    val tradition: JadiTradition,
    val evidenceSummary: String,
    val practiceGuidance: PracticeGuidance,
    val disclaimer: String = JADI_DISCLAIMER,
)

const val JADI_DISCLAIMER =
    "Jadi (herbal root) recommendations are traditional Vedic cultural practices. " +
            "They are NOT medical diagnosis or treatment. Do not use as a substitute for " +
            "professional medical care. Always consult a qualified practitioner before use. " +
            "Some roots may have contraindications or cause allergic reactions."

interface JadiRepository {
    /**
     * Returns Jadi recommendations for a planet or planetary weakness.
     * FOUNDATION_ONLY: Returns empty list with FOUNDATION_ONLY status.
     */
    suspend fun getRecommendationsForPlanet(planetName: String): List<JadiRecommendation>
}

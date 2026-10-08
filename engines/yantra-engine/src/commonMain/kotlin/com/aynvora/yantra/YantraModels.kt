package com.aynvora.yantra

import kotlinx.serialization.Serializable

/**
 * AYNVORA Yantra Domain — Foundation Contracts
 *
 * Phase 8.1: First-class independent core domain registration.
 * STATUS: FOUNDATION_ONLY
 *
 * Yantra are geometric diagrams used in traditional Vedic spiritual practice.
 * All recommendations must include source tradition, purpose, and caution labeling.
 *
 * Rule 74 (Practice Feature Rule): Yantra is a first-class domain.
 *
 * IMPORTANT: Yantra must not be presented as guaranteed cure, protection, or result.
 */

@Serializable
enum class YantraType(
    val displayName: String,
    val primaryDiety: String,
    val associatedPlanet: String,
    val primarySource: String,
) {
    SRI_YANTRA("Sri Yantra", "Lakshmi / Tripura Sundari", "Venus / Moon", "Devi Bhagavata Purana"),
    SURYA_YANTRA("Surya Yantra", "Surya (Sun)", "Sun", "Surya Siddhanta / Tantric tradition"),
    CHANDRA_YANTRA("Chandra Yantra", "Chandra (Moon)", "Moon", "Tantric tradition"),
    MANGAL_YANTRA("Mangal Yantra", "Hanuman / Mars", "Mars", "Lal Kitab / Tantric tradition"),
    BUDH_YANTRA("Budh Yantra", "Vishnu / Mercury", "Mercury", "Tantric tradition"),
    GURU_YANTRA("Guru Yantra", "Vishnu / Brihaspati", "Jupiter", "Tantric tradition"),
    SHUKRA_YANTRA("Shukra Yantra", "Lakshmi / Venus", "Venus", "Tantric tradition"),
    SHANI_YANTRA("Shani Yantra", "Shani (Saturn)", "Saturn", "Tantric tradition"),
    RAHU_YANTRA("Rahu Yantra", "Durga / Rahu", "Rahu", "Tantric tradition"),
    KETU_YANTRA("Ketu Yantra", "Ganesha / Ketu", "Ketu", "Tantric tradition"),
    NAVGRAHA_YANTRA("Navagraha Yantra", "Navagraha (All 9 planets)", "All", "Parashara tradition"),
    KUBER_YANTRA(
        "Kuber Yantra",
        "Kubera (Lord of Wealth)",
        "Jupiter / Mercury",
        "Atharva tradition"
    ),
    MAHA_MRITYUNJAYA_YANTRA(
        "Maha Mrityunjaya Yantra",
        "Shiva",
        "Saturn / Moon",
        "Yajurveda / Shiva Purana"
    ),
}

@Serializable
enum class YantraTradition {
    VEDIC,          // Vedic geometric/mathematical tradition
    TANTRIC,        // Tantric Sri Vidya tradition
    FOLK,           // Regional folk practice
    LAL_KITAB,      // Lal Kitab symbolic remedies
}

@Serializable
enum class YantraMaterial {
    COPPER,
    GOLD,
    SILVER,
    BHOJPATRA, // Bark of Himalayan birch tree
    PAPER,
    CRYSTAL,
}

@Serializable
data class YantraPracticeGuidance(
    val installationGuidance: String,
    val worshipGuidance: String,
    val timingGuidance: String,
    val maintainanceNotes: String,
    val cautionNote: String,
)

@Serializable
data class YantraRecommendation(
    val yantraType: YantraType,
    val tradition: YantraTradition,
    val recommendedMaterial: YantraMaterial,
    val sourceText: String,
    val evidenceSummary: String,
    val practiceGuidance: YantraPracticeGuidance,
    val disclaimer: String = YANTRA_DISCLAIMER,
)

const val YANTRA_DISCLAIMER =
    "Yantra are sacred geometric tools used in Vedic and Tantric traditions for " +
            "contemplation, devotion, and spiritual practice. AYNVORA does not guarantee " +
            "any material, financial, health, or spiritual result from Yantra use. " +
            "Efficacy is a matter of personal faith and practice. Always install with " +
            "proper Prana Pratishtha by a qualified priest."

interface YantraRepository {
    /**
     * Returns Yantra recommendations for a planet or life topic.
     * FOUNDATION_ONLY: Returns empty list with FOUNDATION_ONLY status.
     */
    suspend fun getRecommendationsForPlanet(planetName: String): List<YantraRecommendation>
    suspend fun getRecommendationsForLifeTopic(topic: String): List<YantraRecommendation>
}

package com.aynvora.core.rudraksha

import kotlinx.serialization.Serializable

/**
 * AYNVORA Rudraksha Domain — Foundation Contracts
 *
 * Phase 8.1: First-class independent core domain registration.
 * STATUS: FOUNDATION_ONLY
 *
 * Rudraksha recommendations must carry:
 * - Source tradition (Shiva Purana, Padma Purana, etc.)
 * - Mukhi classification (1 to 21)
 * - No claims of guaranteed material benefit, cure, or protection.
 *
 * Rule 74 (Practice Feature Rule): Rudraksha, Jadi, and Yantra are
 * independent first-class domains. No uncorroborated recommendations.
 */

@Serializable
enum class RudrakshaType(
    val mukhi: Int,
    val displayName: String,
    val rulingDiety: String,
    val rulingPlanet: String,
    val primarySourceText: String,
) {
    MUKHI_1(1, "Ek Mukhi", "Shiva / Surya", "Sun", "Shiva Purana, Vidyeshvara Samhita"),
    MUKHI_2(2, "Do Mukhi", "Ardhanarishvara", "Moon", "Padma Purana"),
    MUKHI_3(3, "Teen Mukhi", "Agni / Skanda", "Mars", "Shiva Purana"),
    MUKHI_4(4, "Chaar Mukhi", "Brahma", "Mercury", "Shiva Purana"),
    MUKHI_5(5, "Paanch Mukhi", "Kalagni Rudra", "Jupiter", "Shiva Purana"),
    MUKHI_6(6, "Chhah Mukhi", "Kartikeya", "Venus", "Shiva Purana"),
    MUKHI_7(7, "Saat Mukhi", "Ananta / Lakshmi", "Saturn", "Devi Bhagavata"),
    MUKHI_8(8, "Aath Mukhi", "Ganesha / Vasugi", "Rahu", "Shiva Purana"),
    MUKHI_9(9, "Nau Mukhi", "Durga / Bhairava", "Ketu", "Padma Purana"),
    MUKHI_10(10, "Das Mukhi", "Vishnu", "All planets", "Shiva Purana"),
    MUKHI_11(11, "Gyaarah Mukhi", "Hanuman / Indra", "All planets", "Shiva Purana"),
    MUKHI_12(12, "Baarah Mukhi", "Surya / Vishnu", "Sun", "Shiva Purana"),
    MUKHI_13(13, "Terah Mukhi", "Kamadeva / Vishvedevas", "Venus / Mercury", "Shiva Purana"),
    MUKHI_14(14, "Chaudah Mukhi", "Shiva / Deva Mani", "Saturn", "Shiva Purana"),
}

@Serializable
enum class RudrakshaOrigin {
    NEPAL,   // Larger beads, fewer available naturally
    JAVA,    // Indonesian variety
    INDIA,   // Haridwar / Nashik varieties
    UNKNOWN,
}

@Serializable
enum class RudrakshaTradition {
    SHAIVA,       // Shiva Purana basis
    TANTRIC,      // Tantric usage
    VAISHNAVA,    // Padma Purana basis
}

@Serializable
data class WearingContext(
    val neck: Boolean = true,
    val wrist: Boolean = false,
    val studyTable: Boolean = false,
    val notes: String = "",
)

@Serializable
data class RudrakshaRecommendation(
    val rudrakshaType: RudrakshaType,
    val origin: RudrakshaOrigin,
    val tradition: RudrakshaTradition,
    val sourceText: String,
    val evidenceSummary: String,
    val wearingContext: WearingContext,
    val disclaimer: String = RUDRAKSHA_DISCLAIMER,
)

const val RUDRAKSHA_DISCLAIMER =
    "Rudraksha recommendations are traditional and spiritual in nature. " +
            "They are not medical advice. Efficacy depends on authentic sources, " +
            "proper ritual activation (Prana Pratishtha), and individual practice. " +
            "AYNVORA makes no guarantee of material, health, or spiritual outcomes."

interface RudrakshaRepository {
    /**
     * Returns Rudraksha recommendations for a planet or planetary imbalance.
     * FOUNDATION_ONLY: Returns empty list with FOUNDATION_ONLY status.
     */
    suspend fun getRecommendationsForPlanet(planetName: String): List<RudrakshaRecommendation>
}

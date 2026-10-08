package com.aynvora.numerology

import kotlinx.serialization.Serializable

/**
 * Normalized planetary identifiers recognized across numerological and astrological traditions.
 */
@Serializable
enum class NumerologyPlanetId {
    SUN,
    MOON,
    JUPITER,
    RAHU,
    MERCURY,
    VENUS,
    KETU,
    SATURN,
    MARS,
}

/**
 * Immutable, typed representation of a planetary association for a given numerological root.
 *
 * Ensures planetary associations are source-gated, versioned, and language-neutral in the core engine.
 */
@Serializable
data class NumerologyPlanetaryAssociation(
    val number: Int,
    val planetId: NumerologyPlanetId,
    val sanskritName: String,
    val englishName: String,
    val tradition: String,
    val sourceRule: String,
)

/**
 * Source-backed registry providing typed planetary associations for each supported tradition.
 */
object NumerologyPlanetaryRegistry {

    /**
     * Navagraha planetary rulers in Indian Ank Jyotish.
     * Reference: Pandit Sethuraman (1954), Dr. M. Katakkar (1989).
     * Strictly numbers 1..9 (Navagrahas).
     */
    val INDIAN_NAVAGRAHA_ASSOCIATIONS: Map<Int, NumerologyPlanetaryAssociation> = mapOf(
        1 to NumerologyPlanetaryAssociation(
            number = 1,
            planetId = NumerologyPlanetId.SUN,
            sanskritName = "Surya",
            englishName = "Sun",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_1",
        ),
        2 to NumerologyPlanetaryAssociation(
            number = 2,
            planetId = NumerologyPlanetId.MOON,
            sanskritName = "Chandra",
            englishName = "Moon",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_2",
        ),
        3 to NumerologyPlanetaryAssociation(
            number = 3,
            planetId = NumerologyPlanetId.JUPITER,
            sanskritName = "Guru / Brihaspati",
            englishName = "Jupiter",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_3",
        ),
        4 to NumerologyPlanetaryAssociation(
            number = 4,
            planetId = NumerologyPlanetId.RAHU,
            sanskritName = "Rahu",
            englishName = "Rahu (North Node)",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_4",
        ),
        5 to NumerologyPlanetaryAssociation(
            number = 5,
            planetId = NumerologyPlanetId.MERCURY,
            sanskritName = "Budha",
            englishName = "Mercury",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_5",
        ),
        6 to NumerologyPlanetaryAssociation(
            number = 6,
            planetId = NumerologyPlanetId.VENUS,
            sanskritName = "Shukra",
            englishName = "Venus",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_6",
        ),
        7 to NumerologyPlanetaryAssociation(
            number = 7,
            planetId = NumerologyPlanetId.KETU,
            sanskritName = "Ketu",
            englishName = "Ketu (South Node)",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_7",
        ),
        8 to NumerologyPlanetaryAssociation(
            number = 8,
            planetId = NumerologyPlanetId.SATURN,
            sanskritName = "Shani",
            englishName = "Saturn",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_8",
        ),
        9 to NumerologyPlanetaryAssociation(
            number = 9,
            planetId = NumerologyPlanetId.MARS,
            sanskritName = "Mangal",
            englishName = "Mars",
            tradition = "Indian Ank Jyotish",
            sourceRule = "SETHURAMAN_NAVAGRAHA_9",
        ),
    )

    /**
     * Planetary rulers in Western / Cheiro Numerology.
     * Reference: Cheiro's Book of Numbers (1926).
     */
    val WESTERN_CHEIRO_ASSOCIATIONS: Map<Int, NumerologyPlanetaryAssociation> = mapOf(
        1 to NumerologyPlanetaryAssociation(
            number = 1,
            planetId = NumerologyPlanetId.SUN,
            sanskritName = "Surya",
            englishName = "Sun",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_1",
        ),
        2 to NumerologyPlanetaryAssociation(
            number = 2,
            planetId = NumerologyPlanetId.MOON,
            sanskritName = "Chandra",
            englishName = "Moon",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_2",
        ),
        3 to NumerologyPlanetaryAssociation(
            number = 3,
            planetId = NumerologyPlanetId.JUPITER,
            sanskritName = "Guru",
            englishName = "Jupiter",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_3",
        ),
        4 to NumerologyPlanetaryAssociation(
            number = 4,
            planetId = NumerologyPlanetId.RAHU,
            sanskritName = "Rahu",
            englishName = "Rahu / Uranus",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_4",
        ),
        5 to NumerologyPlanetaryAssociation(
            number = 5,
            planetId = NumerologyPlanetId.MERCURY,
            sanskritName = "Budha",
            englishName = "Mercury",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_5",
        ),
        6 to NumerologyPlanetaryAssociation(
            number = 6,
            planetId = NumerologyPlanetId.VENUS,
            sanskritName = "Shukra",
            englishName = "Venus",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_6",
        ),
        7 to NumerologyPlanetaryAssociation(
            number = 7,
            planetId = NumerologyPlanetId.KETU,
            sanskritName = "Ketu",
            englishName = "Ketu / Neptune",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_7",
        ),
        8 to NumerologyPlanetaryAssociation(
            number = 8,
            planetId = NumerologyPlanetId.SATURN,
            sanskritName = "Shani",
            englishName = "Saturn",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_8",
        ),
        9 to NumerologyPlanetaryAssociation(
            number = 9,
            planetId = NumerologyPlanetId.MARS,
            sanskritName = "Mangal",
            englishName = "Mars",
            tradition = "Chaldean / Cheiro",
            sourceRule = "CHEIRO_PLANET_9",
        ),
    )

    fun getAssociation(number: Int, rulesetId: String): NumerologyPlanetaryAssociation? {
        val root = NumerologyReductionEngine.reduceToRoot(number)
        return when (rulesetId) {
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id -> INDIAN_NAVAGRAHA_ASSOCIATIONS[root]
            else -> WESTERN_CHEIRO_ASSOCIATIONS[root]
        }
    }
}

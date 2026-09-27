package com.aynvora.core.numerology

import kotlinx.serialization.Serializable

/**
 * AYNVORA Numerology Domain — Foundation Contracts
 *
 * Phase 8.1: Registration and contract definitions only.
 * STATUS: FOUNDATION_ONLY
 *
 * Numerology is a first-class independent core domain (Rule 72 — Numerology Core Rule).
 * Formulas are NOT implemented until independently documented source rules are confirmed.
 *
 * Reference values for JKR-117480 (test/engineering vector only):
 * - Radical (Moolank): 11 (date = 11)
 * - Destiny (Bhagyank): 7  (11 + 7 + 1996 => digit sum)
 * - Name Number: 8
 * - Pinnacles: 9, 9, 9, 5
 *
 * DO NOT invent formulas. DO NOT claim implementation until verified source exists.
 */

// ============================================================================
// NUMEROLOGY DOMAIN MODELS
// ============================================================================

/**
 * The Radical (Moolank) is derived from the day-of-birth digit root.
 * Reference: Cheiro's Book of Numbers, Lo Shu Grid, Pythagorean tradition.
 * Master numbers (11, 22, 33) are preserved without further reduction.
 */
@Serializable
data class RadicalNumber(
    val rawDayOfBirth: Int,
    val radicalValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "CHEIRO_MOOLANK",
)

/**
 * The Destiny Number (Bhagyank) is derived from the complete date of birth.
 * Digit sum of DD + MM + YYYY, reduced to single digit or master number.
 */
@Serializable
data class DestinyNumber(
    val rawSum: Int,
    val destinyValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "CHEIRO_BHAGYANK",
)

/**
 * Name Number derived from Chaldean or Pythagorean letter mapping of full birth name.
 * System must be declared explicitly — Chaldean and Pythagorean differ.
 */
@Serializable
data class NameNumber(
    val fullName: String,
    val nameValue: Int,
    val system: NameNumberSystem,
    val rulingPlanet: String,
    val sourceRule: String,
)

@Serializable
enum class NameNumberSystem {
    CHALDEAN,
    PYTHAGOREAN,
    AGRIPPAN,
    HEBREW_GEMATRIA,
    ARABIC_ABJAD,
    KATAPAYADI,
}

/**
 * Vowel-only letter-sum number representing the soul's inner motivation / Heart's Desire.
 * Supported in Pythagorean Western tradition.
 */
@Serializable
data class SoulUrgeNumber(
    val fullName: String,
    val soulUrgeValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "PYTHAGOREAN_SOUL_URGE",
)

/**
 * Consonant-only letter-sum number representing the outer persona / presentation.
 * Supported in Pythagorean Western tradition.
 */
@Serializable
data class PersonalityNumber(
    val fullName: String,
    val personalityValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "PYTHAGOREAN_PERSONALITY",
)

/**
 * Structured request for a deterministic Numerology profile calculation.
 */
@Serializable
data class NumerologyRequest(
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val fullName: String? = null,
    val targetYear: Int? = null,
    val targetMonth: Int? = null,
    val rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
)

/**
 * Personal Year cycle number calculated from birth day/month and a target calendar year.
 */
@Serializable
data class PersonalYear(
    val targetYear: Int,
    val personalYearValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String,
)

/**
 * Personal Month cycle number calculated from personal year and target month.
 */
@Serializable
data class PersonalMonth(
    val targetYear: Int,
    val targetMonth: Int,
    val personalMonthValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String,
)

/**
 * Life cycle Pinnacle period representing major developmental phases and challenges.
 */
@Serializable
data class Pinnacle(
    val pinnacleOrder: Int,
    val pinnacleValue: Int,
    val isMasterNumber: Boolean,
    val startAge: Int,
    val endAge: Int?,
    val sourceRule: String,
)

/**
 * Relationship dynamic between Radical (Moolank) and Destiny (Bhagyank) numbers.
 */
@Serializable
data class NumerologyCombination(
    val radicalValue: Int,
    val destinyValue: Int,
    val relationshipType: NumerologyRelationshipType,
    val planetaryRelationship: String,
    val sourceRule: String,
)

/**
 * Typed relationship between two numbers in a specified numerological tradition.
 */
@Serializable
data class NumerologyRelationship(
    val subjectNumber: Int,
    val targetNumber: Int,
    val relationshipType: NumerologyRelationshipType,
    val tradition: String,
    val sourceRule: String,
)

@Serializable
enum class NumerologyRelationshipType {
    FRIENDLY,
    NEUTRAL,
    CHALLENGING,
}

/**
 * Result of a Classical Hebrew Gematria calculation.
 *
 * Authorities: Sefer Yetzirah; Pardes Rimonim (Moses Cordovero, 1591).
 */
@Serializable
data class GematriaResult(
    val rawText: String,
    val normalizedHebrew: String,
    val absoluteValue: Int,       // Mispar Hechrachi / Mispar Ragil (1..400) or Mispar Gadol (1..900)
    val reducedValue: Int,        // Mispar Katan (Small reduction to single digit 1..9)
    val letterValues: List<Pair<String, Int>>,
    val rulesetId: String,
    val variant: String = "MISPAR_RAGIL",
    val sourceRule: String = "SEFER_YETZIRAH_MISPAR_HECHRACHI",
)

/**
 * Result of an Arabic Hisab al-Jummal calculation.
 *
 * Authorities: Ibn Khaldun, The Muqaddimah (1377 CE), Ch. 6, Sec. 28.
 */
@Serializable
data class AbjadResult(
    val rawText: String,
    val normalizedArabic: String,
    val kabirValue: Int,          // Great sum (Jummal Kabir, 1..1000 scale)
    val saghirValue: Int,         // Small sum (Jummal Saghir, single digit root 1..9)
    val letterValues: List<Pair<String, Int>>,
    val rulesetId: String,
    val variant: String = "MASHRIQI",
    val sourceRule: String = "IBN_KHALDUN_JUMMAL_MASHRIQI",
) {
    val jummalKabir: Int get() = kabirValue
    val jummalSaghir: Int get() = saghirValue
}

/**
 * Individual parsed phoneme/akshara in Indian Katapayadi system.
 */
@Serializable
data class KatapayadiPhoneme(
    val akshara: String,
    val mappedConsonant: String,
    val vargaGroup: String,
    val digit: Int,
)

/**
 * Result of an Indian Katapayadi numerical encoding calculation.
 *
 * Authority: Sadratnamala (Sankara Varman, 1819 CE); Grahacaranibandhana (Haridatta, 683 CE).
 */
@Serializable
data class KatapayadiResult(
    val rawText: String,
    val normalizedText: String,
    val phonemes: List<KatapayadiPhoneme>,
    val extractedDigits: List<Int>,
    val reversedNumber: Long,     // Ankanam vamato gatih (digits read in reverse order)
    val rulesetId: String,
    val sourceRule: String = "SADRATNAMALA_KATAPAYADI",
) {
    val digitSequence: String get() = extractedDigits.joinToString("")
    val finalNumber: String get() = reversedNumber.toString()
    val isReversed: Boolean get() = true
}

/**
 * Classical Chinese Nine Star Ki Star representation.
 */
@Serializable
data class NineStar(
    val starNumber: Int,          // 1..9
    val starName: String,          // e.g. "1 White Water", "4 Green Wood"
    val trigram: String,           // e.g. "Kan", "Xun"
    val element: String,           // e.g. "Water", "Wood", "Earth", "Metal", "Fire"
    val direction: String,         // e.g. "North", "Southeast"
) {
    val number: Int get() = starNumber
    val englishName: String get() = starName
    val chineseName: String get() = trigram
}

/**
 * Result of a Chinese Nine Star Ki calculation.
 *
 * Authority: Xuan Kong Fei Xing; I Ching Solar Calendar (Li Chun 315° solar longitude).
 */
@Serializable
data class NineStarKiResult(
    val birthDateDisplay: String,
    val birthTimestampUtc: Long,
    val solarYear: Int,
    val liChunInstantUtc: Long,
    val isBeforeLiChun: Boolean,
    val principalStar: NineStar,
    val rulesetId: String,
    val sourceRule: String = "XUAN_KONG_NINE_STAR_KI",
) {
    val liChunIsoTimestamp: String get() = liChunInstantUtc.toString()
}

/**
 * Result of a Tarot Birth Card calculation.
 *
 * Authority: Mary K. Greer, 'Tarot for Your Self' (1984), Ch. 2; Angeles Arrien (1987).
 */
@Serializable
data class TarotBirthCardResult(
    val birthDateDisplay: String,
    val rawSum: Int,
    val personalityCardNumber: Int,
    val personalityCardName: String,
    val soulCardNumber: Int,
    val soulCardName: String,
    val isPair: Boolean,
    val shadowCardNumber: Int? = null,
    val shadowCardName: String? = null,
    val rulesetId: String,
    val sourceRule: String = "GREER_1984_TAROT_BIRTH_CARDS",
) {
    val firstReduction: Int get() = personalityCardNumber
    val finalReduction: Int get() = soulCardNumber
}

/**
 * Complete Numerology profile for a birth data input.
 */
@Serializable
data class NumerologyProfile(
    val birthDateDisplay: String,           // "11-07-1996"
    val radical: RadicalNumber?,
    val destiny: DestinyNumber?,
    val nameNumber: NameNumber?,
    val soulUrge: SoulUrgeNumber? = null,
    val personality: PersonalityNumber? = null,
    val personalYears: List<PersonalYear> = emptyList(),
    val personalMonths: List<PersonalMonth> = emptyList(),
    val pinnacles: List<Pinnacle> = emptyList(),
    val combinations: List<NumerologyCombination> = emptyList(),
    val loShu: LoShuGrid? = null,
    val gematria: GematriaResult? = null,
    val abjad: AbjadResult? = null,
    val katapayadi: KatapayadiResult? = null,
    val nineStarKi: NineStarKiResult? = null,
    val tarotBirthCard: TarotBirthCardResult? = null,
    val planetaryAssociation: NumerologyPlanetaryAssociation? = null,
    val rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
    val calculationTraces: Map<NumerologyCalculationType, NumerologyCalculationTrace> = emptyMap(),
    val engineStatus: NumerologyEngineStatus = NumerologyEngineStatus.IMPLEMENTED,
)

/**
 * Numerology Result returned from a complete analysis query.
 */
@Serializable
data class NumerologyResult(
    val profile: NumerologyProfile,
    val loShuResult: LoShuResult? = null,
    val gematriaResult: GematriaResult? = null,
    val abjadResult: AbjadResult? = null,
    val katapayadiResult: KatapayadiResult? = null,
    val nineStarKiResult: NineStarKiResult? = null,
    val tarotBirthCardResult: TarotBirthCardResult? = null,
    val disclaimer: String = NUMEROLOGY_DISCLAIMER,
)

/**
 * Engine implementation status.
 */
@Serializable
enum class NumerologyEngineStatus {
    /** Contracts defined; formulas not yet implemented. */
    FOUNDATION_ONLY,

    /** Radical and Destiny Number implemented and verified. */
    PARTIAL,

    /** All numerology calculations implemented and validated. */
    IMPLEMENTED,
}

const val NUMEROLOGY_DISCLAIMER =
    "Numerology is a contemplative tradition. Numbers and their influences " +
            "are a matter of cultural belief. AYNVORA presents them as interpretive " +
            "tools for self-reflection, not as deterministic prediction of events or " +
            "guaranteed outcomes."

// ============================================================================
// REPOSITORY CONTRACT
// ============================================================================

/**
 * Repository contract for Numerology domain.
 * Implementations live in the data layer (:aynvora-data).
 */
interface NumerologyRepository {
    /**
     * Calculates and returns the Numerology profile for a given birth date, name, and ruleset.
     */
    suspend fun getProfile(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        fullName: String? = null,
        rulesetId: String = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
    ): com.aynvora.core.result.AynvoraResult<NumerologyResult>

    /**
     * Calculates profile from a structured [NumerologyRequest].
     */
    suspend fun calculate(
        request: NumerologyRequest,
    ): com.aynvora.core.result.AynvoraResult<NumerologyResult>
}

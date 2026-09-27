package com.aynvora.core.numerology

import kotlinx.serialization.Serializable

/**
 * Types of numerological calculations supported in the AYNVORA Numerology domain.
 */
@Serializable
enum class NumerologyCalculationType {
    /** Day-of-birth number (Moolank in Indian tradition, Birth Day in Pythagorean). */
    RADICAL_NUMBER,

    /** Full date-of-birth number (Bhagyank in Indian tradition, Life Path in Pythagorean). */
    DESTINY_NUMBER,

    /** Full name letter-sum number (Namank / Expression Number). */
    NAME_NUMBER,

    /** Vowel-only letter-sum number (Soul Urge / Heart's Desire). */
    SOUL_URGE_NUMBER,

    /** Consonant-only letter-sum number (Personality Number). */
    PERSONALITY_NUMBER,

    /** Current / target calendar year cycle (Birthday month/day + target year). */
    PERSONAL_YEAR,

    /** Current / target calendar month cycle within a Personal Year. */
    PERSONAL_MONTH,

    /** 4 major life cycle blocks and age milestones derived from birth date components. */
    PINNACLE_CYCLES,

    /** 3x3 Magic Square digit extraction and frequency grid (Classical Chinese Luoshu tradition). */
    LO_SHU_GRID,

    /** 8 Planes of Arrows (Strength and Weakness) derived from Lo Shu digit frequencies. */
    LO_SHU_ARROWS,

    /** Sacred Hebrew Gematria absolute numerical sum (Mispar Hechrachi / Ragil, 1..400 scale). */
    GEMATRIA_ABSOLUTE_VALUE,

    /** Sacred Hebrew Gematria small reduction root (Mispar Katan, 1..9 scale). */
    GEMATRIA_REDUCED_VALUE,

    /** Arabic Hisab al-Jummal Great Sum (Jummal Kabir, 1..1000 scale). */
    ABJAD_KABIR_VALUE,

    /** Arabic Hisab al-Jummal Small Sum root reduction (Jummal Saghir, 1..9 scale). */
    ABJAD_SAGHIR_VALUE,

    /** Indian Katapayadi phonetic consonant-group numerical coding (Sadratnamala). */
    KATAPAYADI_CODING,

    /** Chinese Nine Star Ki principal star calculation based on solar Li Chun transition. */
    NINE_STAR_KI_PRINCIPAL,

    /** Tarot Major Arcana birth card and pair card reduction (Mary K. Greer 1984). */
    TAROT_BIRTH_CARD,

    /** Relationship and compatibility dynamic between numbers (e.g. Mitra, Sama, Shatru). */
    COMBINATION_RELATIONSHIP,
}

/**
 * Policies governing whether compound master numbers are preserved or reduced to single digits.
 */
@Serializable
enum class MasterNumberPolicy {
    /** Preserves 11, 22, and 33 without further single-digit reduction. Standard modern Pythagorean. */
    PRESERVE_11_22_33,

    /** Preserves 11 and 22 without further single-digit reduction. Classical Cheiro tradition. */
    PRESERVE_11_22,

    /** Reduces all numbers strictly to single digits (1..9). Pure root reduction. */
    REDUCE_ALL_TO_SINGLE_DIGIT,
}

/**
 * Methods for reducing multidigit numbers and calendar dates.
 */
@Serializable
enum class ReductionMethod {
    /** Sums individual digits iteratively until reaching a single digit or recognized master number. */
    DIGIT_SUM,

    /** Reduces day, month, and year separately to their roots/masters, then sums and reduces. */
    COMPONENT_THEN_SUM,

    /** Sums day + month + year as whole integers, then reduces the total sum by digit addition. */
    FULL_INTEGER_SUM,
}

/**
 * Formal source-gated specification of a Numerology tradition and its mathematical contracts.
 */
@Serializable
data class NumerologyRuleset(
    val id: String,
    val name: String,
    val version: String,
    val authorityDescription: String,
    val primarySourceReference: String,
    val nameNumberSystem: NameNumberSystem,
    val masterNumberPolicy: MasterNumberPolicy,
    val dateReductionMethod: ReductionMethod,
    val supportedCalculations: Set<NumerologyCalculationType>,
    val unsupportedCalculations: Set<NumerologyCalculationType>,
) {
    companion object {
        /**
         * Classical Chaldean / Cheiro Numerology ruleset.
         *
         * Authorities:
         * - Cheiro (Count Louis Hamon), "Cheiro's Book of Numbers" (1926)
         * - Pandit Sethuraman, "Adhristavijnanam" / Indian Ank Jyotish tradition
         *
         * Characteristics:
         * - Letter values 1–8 (9 is sacred and excluded from single alphabet assignments).
         * - Day of birth = Moolank (Radical).
         * - Full date digit sum = Bhagyank (Destiny).
         * - Master numbers 11 and 22 recognized.
         */
        val CHALDEAN_CHEIRO_V1 = NumerologyRuleset(
            id = "CHALDEAN_CHEIRO_V1",
            name = "Chaldean / Cheiro Classical Numerology",
            version = "1.0.0",
            authorityDescription = "Traditional Chaldean sound-vibration mapping with Indian Ank Jyotish day/destiny roots",
            primarySourceReference = "Cheiro's Book of Numbers (Count Louis Hamon, 1926), Ch. 1–5",
            nameNumberSystem = NameNumberSystem.CHALDEAN,
            masterNumberPolicy = MasterNumberPolicy.PRESERVE_11_22,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
            ),
        )

        /**
         * Modern Western Pythagorean Numerology ruleset.
         *
         * Authorities:
         * - Matthew Oliver Goodwin, "Numerology: The Complete Guide" (1981)
         * - Florence Campbell, "Your Days Are Numbered" (1931)
         * - Hans Decoz, "Numerology: Key to Your Inner Self" (1994)
         *
         * Characteristics:
         * - Sequential 1–9 letter mapping across Latin alphabet (A–Z).
         * - Master numbers 11, 22, 33 preserved in core calculations.
         * - Component date reduction for Life Path.
         * - Full suite: Life Path, Expression, Soul Urge (vowels), Personality (consonants), Pinnacles.
         */
        val PYTHAGOREAN_WESTERN_V1 = NumerologyRuleset(
            id = "PYTHAGOREAN_WESTERN_V1",
            name = "Pythagorean Western Numerology",
            version = "1.0.0",
            authorityDescription = "Western Pythagorean system with component date reduction and vowel/consonant analysis",
            primarySourceReference = "Goodwin, Matthew Oliver. Numerology: The Complete Guide (1981), Vol 1, Ch. 2–8",
            nameNumberSystem = NameNumberSystem.PYTHAGOREAN,
            masterNumberPolicy = MasterNumberPolicy.PRESERVE_11_22_33,
            dateReductionMethod = ReductionMethod.COMPONENT_THEN_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
            ),
        )

        /**
         * Indian / Vedic Ank Jyotish ruleset.
         *
         * Authorities:
         * - Pandit Sethuraman, "Science of Fortune" (1954)
         * - Dr. M. Katakkar, "Miracles of Numerology" (1989)
         *
         * Characteristics:
         * - Day of birth = Moolank (Driver/Radical), strictly reduced to 1..9 (Navagraha).
         * - Complete date digit sum = Bhagyank (Destiny/Conductor), strictly reduced to 1..9.
         * - Master numbers (11, 22, 33) are REDUCED to single-digit roots (2, 4, 6) because only 9 Navagrahas exist.
         * - Name number (Namank) calculated using Cheiro-Sethuraman sound-vibration table (1–8) for Latin-transliterated names.
         * - Strict Panchadha Maitri relationship matrix (Mitra / Sama / Shatru).
         */
        val INDIAN_ANK_JYOTISH_V1 = NumerologyRuleset(
            id = "INDIAN_ANK_JYOTISH_V1",
            name = "Indian / Vedic Ank Jyotish",
            version = "1.0.0",
            authorityDescription = "Traditional Indian Ank Jyotish based on 9 Navagrahas, Moolank, Bhagyank, and Panchadha Maitri",
            primarySourceReference = "Pandit Sethuraman, 'Science of Fortune' (1954); Dr. M. Katakkar, 'Miracles of Numerology' (1989)",
            nameNumberSystem = NameNumberSystem.CHALDEAN,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
            ),
        )

        /**
         * Classical Chinese Lo Shu 3x3 Magic Square ruleset.
         *
         * Authorities:
         * - I Ching (Book of Changes, Luoshu River Scroll, Zhou Dynasty)
         * - Dr. David A. Phillips, "The Complete Book of Numerology" (1992), Ch. 5
         *
         * Characteristics:
         * - 3x3 Magic Square grid layout (Row 1: 4-9-2, Row 2: 3-5-7, Row 3: 8-1-6).
         * - All non-zero digits extracted from Gregorian date of birth (DD-MM-YYYY).
         * - Zero ('0') is excluded from placement in the grid (void/unmanifest).
         * - Evaluates digit frequencies, present digits, missing digits.
         * - Evaluates 8 Planes: 3 Horizontal (Mind, Soul, Practical), 3 Vertical (Thought, Will, Action),
         *   2 Diagonal (Determination, Compassion).
         * - Classifies each plane into Arrow of Strength (all 3 present) or Arrow of Weakness (all 3 absent).
         * - Does NOT perform single-digit life path reductions or alphabet name calculations.
         */
        val LO_SHU_CLASSICAL_V1 = NumerologyRuleset(
            id = "LO_SHU_CLASSICAL_V1",
            name = "Classical Lo Shu Magic Square",
            version = "1.0.0",
            authorityDescription = "Ancient Chinese 3x3 Magic Square digit extraction, frequency grid, and 8 Planes of Arrows",
            primarySourceReference = "I Ching (Luoshu Scroll); Dr. David A. Phillips, 'The Complete Book of Numerology' (1992), Ch. 5",
            nameNumberSystem = NameNumberSystem.CHALDEAN,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
            ),
        )

        /**
         * Classical Hebrew Gematria ruleset (Mispar Hechrachi & Mispar Katan).
         *
         * Authorities:
         * - Sefer Yetzirah (Book of Creation, 2nd–6th c. CE)
         * - Talmud Bavli (Sanhedrin 22a)
         * - Rabbi Moses Cordovero, "Pardes Rimonim" (1591), Gate 30
         *
         * Characteristics:
         * - Sacred alphanumeric mapping across 22 Hebrew consonants (Alef to Tav).
         * - Mispar Hechrachi (Ragil): Standard absolute values (1..400).
         * - Final letters (sofit): Standard consonant values in Ragil (Kaf=20, Mem=40, Nun=50, Pe=80, Tsadi=90).
         * - Mispar Katan: Sum reduced to single digit root 1..9.
         * - Strictly requires Hebrew script input; rejects non-Hebrew scripts.
         */
        val HEBREW_GEMATRIA_CLASSICAL_V1 = NumerologyRuleset(
            id = "HEBREW_GEMATRIA_CLASSICAL_V1",
            name = "Classical Hebrew Gematria",
            version = "1.0.0",
            authorityDescription = "Hebrew sacred alphanumeric equivalence, Mispar Hechrachi (Absolute 1..400) and Mispar Katan (Root 1..9)",
            primarySourceReference = "Sefer Yetzirah (Book of Creation); Pardes Rimonim (Moses Cordovero, 1591), Gate 30",
            nameNumberSystem = NameNumberSystem.HEBREW_GEMATRIA,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
            ),
        )

        /**
         * Arabic Hisab al-Jummal (Eastern Mashriqi Standard Abjad).
         *
         * Authorities:
         * - Ibn Khaldun, "The Muqaddimah" (1377 CE), Ch. 6, Section 28 (Ilm al-Huruf)
         * - Ahmad al-Buni, "Shams al-Ma'arif al-Kubra" (c. 1225 CE)
         *
         * Characteristics:
         * - Classical Eastern Mashriqi Abjad letter values across 28 Arabic letters (1..1000).
         * - Ta Marbuta (ة) assigned 400 (grammatical equivalent of Taa ت).
         * - Tashkeel (harakat) stripped; hamzas normalized deterministically.
         * - Jummal Kabir: Great sum of letter values.
         * - Jummal Saghir: Small sum reduced to single digit root 1..9.
         * - Strictly requires Arabic script input; rejects non-Arabic scripts.
         */
        val ARABIC_ABJAD_MASHRIQI_V1 = NumerologyRuleset(
            id = "ARABIC_ABJAD_MASHRIQI_V1",
            name = "Arabic Hisab al-Jummal (Mashriqi Abjad)",
            version = "1.0.0",
            authorityDescription = "Traditional Islamic/Semitic alphanumeric calculation, Jummal Kabir (Great Sum 1..1000) and Jummal Saghir (Root 1..9)",
            primarySourceReference = "Ibn Khaldun, 'The Muqaddimah' (1377 CE), Ch. 6, Section 28 (Ilm al-Huruf)",
            nameNumberSystem = NameNumberSystem.ARABIC_ABJAD,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
            ),
        )

        /**
         * Renaissance Agrippan Arithmancy ruleset.
         *
         * Authorities:
         * - Heinrich Cornelius Agrippa, "De Occulta Philosophia libri tres" (1533), Book II, Ch. 20–34
         *
         * Characteristics:
         * - Latin alphanumeric scale (A=1..I/J=9, K=10..S=90, T=100..Z=500).
         * - Classical Roman alphabet equivalences: I=J=9, U=V=W=200.
         * - Calculates total Agrippan letter sum and reduced single-digit root 1..9.
         */
        val AGRIPPAN_OCCULT_V1 = NumerologyRuleset(
            id = "AGRIPPAN_OCCULT_V1",
            name = "Renaissance Agrippan Arithmancy",
            version = "1.0.0",
            authorityDescription = "Renaissance Latin alphanumeric occult scale (1..500 base) with classical Roman letter equivalences",
            primarySourceReference = "Heinrich Cornelius Agrippa, 'De Occulta Philosophia libri tres' (1533), Book II, Ch. 20–34",
            nameNumberSystem = NameNumberSystem.AGRIPPAN,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.NAME_NUMBER,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
                NumerologyCalculationType.KATAPAYADI_CODING,
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
        )

        /**
         * Classical Hebrew Gematria — Mispar Gadol Variant.
         * Authority: Rabbi Moses Cordovero, 'Pardes Rimonim' (1591), Gate 30; Sefer HaBahir.
         * - Assigns distinct large values to the 5 final sofit consonants:
         *   Final Kaf (ך)=500, Final Mem (ם)=600, Final Nun (ן)=700, Final Pe (ף)=800, Final Tsadi (ץ)=900.
         */
        val HEBREW_MISPAR_GADOL_V1 = NumerologyRuleset(
            id = "HEBREW_MISPAR_GADOL_V1",
            name = "Hebrew Gematria — Mispar Gadol",
            version = "1.0.0",
            authorityDescription = "Classical Kabbalistic large numerical values for final letters (500..900 scale)",
            primarySourceReference = "Rabbi Moses Cordovero, 'Pardes Rimonim' (1591), Gate 30",
            nameNumberSystem = NameNumberSystem.HEBREW_GEMATRIA,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
                NumerologyCalculationType.KATAPAYADI_CODING,
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
        )

        /**
         * Arabic Hisab al-Jummal — Maghribi Order Variant.
         * Authority: Ibn Khaldun, 'The Muqaddimah' (1377 CE), Chapter 6, Section 28 (Ilm al-Huruf).
         * - North African & Andalusian classical Abjad order (Sa'fadh, Qarast, Thakhadh, Zaghsh).
         * - Swaps values: Saad=60, Daad=90, Seen=300, Zhaa=800, Ghayn=900, Sheen=1000.
         */
        val ARABIC_ABJAD_MAGHRIBI_V1 = NumerologyRuleset(
            id = "ARABIC_ABJAD_MAGHRIBI_V1",
            name = "Arabic Hisab al-Jummal — Maghribi Order",
            version = "1.0.0",
            authorityDescription = "North African & Andalusian classical Abjad order (Sa'fadh, Qarast, Thakhadh, Zaghsh)",
            primarySourceReference = "Ibn Khaldun, 'The Muqaddimah' (1377 CE), Chapter 6, Section 28",
            nameNumberSystem = NameNumberSystem.ARABIC_ABJAD,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
                NumerologyCalculationType.KATAPAYADI_CODING,
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
        )

        /**
         * Indian Katapayadi Phonetic Alphanumeric System.
         * Authority: Sadratnamala (Sankara Varman, 1819 CE); Grahacaranibandhana (Haridatta, 683 CE).
         * - Ka-Ta-Pa-Ya consonant groups assigned 1..9 and 0.
         * - In conjuncts, last consonant takes value. Vowels alone count as 0/void.
         * - Digits read in reverse order: 'ankanam vamato gatih'.
         */
        val INDIAN_KATAPAYADI_V1 = NumerologyRuleset(
            id = "INDIAN_KATAPAYADI_V1",
            name = "Indian Katapayadi System",
            version = "1.0.0",
            authorityDescription = "Classical Sanskrit/Indic phonetic letter-group encoding system with right-to-left numeral composition",
            primarySourceReference = "Sadratnamala (Sankara Varman, 1819 CE); Grahacaranibandhana (Haridatta, 683 CE)",
            nameNumberSystem = NameNumberSystem.KATAPAYADI,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.KATAPAYADI_CODING,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
        )

        /**
         * Classical Chinese Nine Star Ki.
         * Authority: Xuan Kong Fei Xing; I Ching Solar Calendar treatises.
         * - Determines solar year via astronomical Li Chun transition (solar longitude 315°).
         * - Principal Star cyclic mod-9 reduction.
         */
        val CHINESE_NINE_STAR_KI_V1 = NumerologyRuleset(
            id = "CHINESE_NINE_STAR_KI_V1",
            name = "Chinese Nine Star Ki",
            version = "1.0.0",
            authorityDescription = "Solar year Li Chun transition (315° solar longitude) and 9-Star principal cycle",
            primarySourceReference = "Xuan Kong Fei Xing; I Ching Solar Calendar treatises",
            nameNumberSystem = NameNumberSystem.CHALDEAN,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.DIGIT_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
                NumerologyCalculationType.KATAPAYADI_CODING,
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
        )

        /**
         * Tarot Birth Cards.
         * Authority: Mary K. Greer, 'Tarot for Your Self' (1984), Ch. 2; Angeles Arrien (1987).
         * - Sums MM + DD + YYYY and reduces to Major Arcana 1..22.
         * - Derives Personality and Soul card pairs.
         */
        val TAROT_BIRTH_CARD_V1 = NumerologyRuleset(
            id = "TAROT_BIRTH_CARD_V1",
            name = "Tarot Birth Cards",
            version = "1.0.0",
            authorityDescription = "Mary K. Greer (1984) Major Arcana 1..22 birth reduction and pair-card archetypes",
            primarySourceReference = "Mary K. Greer, 'Tarot for Your Self' (1984), Ch. 2; Angeles Arrien (1987)",
            nameNumberSystem = NameNumberSystem.PYTHAGOREAN,
            masterNumberPolicy = MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT,
            dateReductionMethod = ReductionMethod.FULL_INTEGER_SUM,
            supportedCalculations = setOf(
                NumerologyCalculationType.TAROT_BIRTH_CARD,
            ),
            unsupportedCalculations = setOf(
                NumerologyCalculationType.RADICAL_NUMBER,
                NumerologyCalculationType.DESTINY_NUMBER,
                NumerologyCalculationType.NAME_NUMBER,
                NumerologyCalculationType.SOUL_URGE_NUMBER,
                NumerologyCalculationType.PERSONALITY_NUMBER,
                NumerologyCalculationType.PERSONAL_YEAR,
                NumerologyCalculationType.PERSONAL_MONTH,
                NumerologyCalculationType.PINNACLE_CYCLES,
                NumerologyCalculationType.LO_SHU_GRID,
                NumerologyCalculationType.LO_SHU_ARROWS,
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
                NumerologyCalculationType.GEMATRIA_REDUCED_VALUE,
                NumerologyCalculationType.ABJAD_KABIR_VALUE,
                NumerologyCalculationType.ABJAD_SAGHIR_VALUE,
                NumerologyCalculationType.KATAPAYADI_CODING,
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
            ),
        )

        val ALL_RULESETS: List<NumerologyRuleset> = listOf(
            CHALDEAN_CHEIRO_V1,
            PYTHAGOREAN_WESTERN_V1,
            INDIAN_ANK_JYOTISH_V1,
            LO_SHU_CLASSICAL_V1,
            HEBREW_GEMATRIA_CLASSICAL_V1,
            HEBREW_MISPAR_GADOL_V1,
            ARABIC_ABJAD_MASHRIQI_V1,
            ARABIC_ABJAD_MAGHRIBI_V1,
            AGRIPPAN_OCCULT_V1,
            INDIAN_KATAPAYADI_V1,
            CHINESE_NINE_STAR_KI_V1,
            TAROT_BIRTH_CARD_V1,
        )

        fun fromId(rulesetId: String): NumerologyRuleset? {
            val normalized = rulesetId.trim()
            val canonicalId = when (normalized.uppercase()) {
                "HEBREW_GEMATRIA_STANDARD_V1" -> HEBREW_GEMATRIA_CLASSICAL_V1.id
                "HEBREW_GEMATRIA_MISPAR_GADOL_V1" -> HEBREW_MISPAR_GADOL_V1.id
                "AGRIPPAN_LATIN_V1" -> AGRIPPAN_OCCULT_V1.id
                "INDIAN_KATAPAYADI_VARARUCHI_V1" -> INDIAN_KATAPAYADI_V1.id
                "TAMIL_VEDIC_V1" -> INDIAN_ANK_JYOTISH_V1.id
                else -> normalized
            }
            return ALL_RULESETS.firstOrNull { it.id.equals(canonicalId, ignoreCase = true) }
        }
    }
}

/**
 * Global registry helper for discovering and querying available numerology rulesets.
 */
object NumerologyRulesetRegistry {
    fun find(rulesetId: String): NumerologyRuleset? = NumerologyRuleset.fromId(rulesetId)
    fun all(): List<NumerologyRuleset> = NumerologyRuleset.ALL_RULESETS
}

package com.aynvora.numerology

import kotlinx.serialization.Serializable

/**
 * Fundamental discipline classification distinguishing distinct numerical and esoteric traditions.
 *
 * Prevents conflating gematria, abjad arithmetic, magic squares, and number symbolism
 * into a single generic "numerology" label.
 */
@Serializable
enum class NumericalDisciplineType {
    /** Birth date and name calculation into roots, cycles, and life paths. */
    NUMEROLOGY,

    /** 3x3 Magic Square coordinate placement, digit frequencies, and arrow planes. */
    MAGIC_SQUARE,

    /** Sacred Hebrew alphanumeric equivalence, sums (Ragil), and reduction (Mispar Katan). */
    GEMATRIA,

    /** Semitic / Islamic alphanumeric computation (Hisab al-Jummal Kabir & Saghir). */
    ABJAD_ARITHMETIC,

    /** Cultural, homophonic, or synchronistic number meanings without calculation algorithms. */
    NUMBER_SYMBOLISM,
}

/**
 * Quality gate and implementation maturity status for candidate traditions.
 */
@Serializable
enum class TraditionImplementationStatus {
    /** Primary source documented, exact formulas codified, golden test suite passing. */
    DOCUMENTED_AND_IMPLEMENTED,

    /** Documented in recognized sources, but deferred to a dedicated future phase. */
    DOCUMENTED_PENDING_IMPLEMENTATION,

    /** Multiple distinct conflicting historical variants exist and must remain isolated. */
    MULTIPLE_VARIANTS,

    /** Tradition represents symbolic/cultural attribution only; no mathematical algorithm. */
    SYMBOLIC_ONLY,

    /** Popular internet claims lacking primary historical or authoritative documentation. */
    INSUFFICIENT_SOURCE,

    /** Arbitrary internet mixes or inaccurate composite calculators explicitly rejected. */
    DEPRECATED,
}

/**
 * Authoritative specification of a numerical tradition in the AYNVORA Global Registry.
 */
@Serializable
data class NumerologyTraditionDefinition(
    val traditionId: String,
    val canonicalName: String,
    val discipline: NumericalDisciplineType,
    val culturalRegion: String,
    val historicalEra: String,
    val primarySources: List<String>,
    val acceptedScript: String,
    val implementationStatus: TraditionImplementationStatus,
    val rulesetId: String? = null,
    val supportedCalculations: List<String> = emptyList(),
    val variantPolicy: String,
    val reasonForStatus: String,
)

/**
 * Authoritative living registry of global numerological and alphanumeric traditions.
 */
object NumerologyTraditionRegistry {

    val ALL_TRADITIONS: List<NumerologyTraditionDefinition> = listOf(
        // ── 1. Classical Chaldean / Cheiro ───────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "CHALDEAN_CHEIRO",
            canonicalName = "Chaldean / Cheiro Classical Numerology",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "Babylonian / Anglo-Indian",
            historicalEra = "Ancient origins; codified 1890–1926",
            primarySources = listOf(
                "Cheiro (Count Louis Hamon), 'Cheiro's Book of Numbers' (1926)",
                "Pandit Sethuraman, 'Adhristavijnanam' (1954)",
            ),
            acceptedScript = "Latin (A–Z transliterated)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
            supportedCalculations = listOf(
                "Radical Number",
                "Destiny Number",
                "Name Number (1–8 Sound)",
                "Personal Year"
            ),
            variantPolicy = "Letter 9 is sacred and excluded from alphabet assignments; preserves master numbers 11 and 22.",
            reasonForStatus = "Fully verified against Cheiro's 1926 published text and golden test vectors.",
        ),

        // ── 2. Western Pythagorean ──────────────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "PYTHAGOREAN_WESTERN",
            canonicalName = "Western Pythagorean Modern Numerology",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "Greco-Western / Modern American",
            historicalEra = "Pythagorean school origins; modern codification 1931–1981",
            primarySources = listOf(
                "Matthew Oliver Goodwin, 'Numerology: The Complete Guide' (1981)",
                "Florence Campbell, 'Your Days Are Numbered' (1931)",
                "Hans Decoz, 'Numerology: Key to Your Inner Self' (1994)",
            ),
            acceptedScript = "Latin (A–Z sequential)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            supportedCalculations = listOf(
                "Life Path",
                "Birth Day",
                "Expression",
                "Soul Urge",
                "Personality",
                "Personal Year",
                "Personal Month",
                "4 Pinnacles"
            ),
            variantPolicy = "Preserves master numbers 11, 22, and 33; uses component date reduction; strict vowel/consonant partition.",
            reasonForStatus = "Fully verified against Goodwin's 1981 authoritative manual and mathematical invariants.",
        ),

        // ── 3. Indian / Vedic Ank Jyotish ────────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "INDIAN_ANK_JYOTISH",
            canonicalName = "Indian / Vedic Ank Jyotish (Sethuraman / Katakkar)",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "India (Vedic / Jyotish)",
            historicalEra = "Classical Graha association; codified modern English 1954–1989",
            primarySources = listOf(
                "Pandit Sethuraman, 'Science of Fortune' (1954)",
                "Dr. M. Katakkar, 'Miracles of Numerology' (1989)",
            ),
            acceptedScript = "Latin (A–Z transliterated; Devanagari/Indic scripts strictly rejected)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
            supportedCalculations = listOf(
                "Moolank (Driver)",
                "Bhagyank (Conductor)",
                "Namank",
                "Navagraha Planetary Rulers",
                "Panchadha Maitri Matrix",
                "Personal Year"
            ),
            variantPolicy = "Reduces all numbers strictly to 1..9 roots (no master numbers in 9-Graha framework); Panchadha Maitri relationship matrix.",
            reasonForStatus = "Fully verified against Sethuraman and Katakkar published tables and Navagraha correspondence.",
        ),

        // ── 4. Classical Lo Shu Magic Square ─────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "LO_SHU_CLASSICAL",
            canonicalName = "Classical Lo Shu 3x3 Magic Square",
            discipline = NumericalDisciplineType.MAGIC_SQUARE,
            culturalRegion = "Ancient China",
            historicalEra = "Zhou Dynasty (c. 1000 BCE) / Han Dynasty",
            primarySources = listOf(
                "I Ching (Book of Changes, Luoshu River Scroll)",
                "Dr. David A. Phillips, 'The Complete Book of Numerology' (1992), Ch. 5",
            ),
            acceptedScript = "Gregorian Calendar Date (DD-MM-YYYY)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
            supportedCalculations = listOf(
                "3x3 Frequency Grid",
                "9 Canonical Cells",
                "Present & Missing Digits",
                "8 Planes of Arrows",
                "Arrows of Strength & Weakness"
            ),
            variantPolicy = "Direct digit extraction without life path reduction; zero ('0') excluded as void/unmanifest.",
            reasonForStatus = "Fully verified against classical Luoshu coordinate geometry and Phillips' arrow classifications.",
        ),

        // ── 5. Classical Hebrew Gematria ────────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "HEBREW_GEMATRIA_CLASSICAL",
            canonicalName = "Classical Hebrew Gematria (Mispar Hechrachi & Mispar Katan)",
            discipline = NumericalDisciplineType.GEMATRIA,
            culturalRegion = "Judea / Ancient Near East",
            historicalEra = "Talmudic period (c. 200–500 CE) / Kabbalistic (13th–16th c.)",
            primarySources = listOf(
                "Sefer Yetzirah (Book of Creation, 2nd–6th c. CE)",
                "Talmud Bavli (Tractate Sanhedrin 22a)",
                "Rabbi Moses Cordovero, 'Pardes Rimonim' (1591), Gate 30",
            ),
            acceptedScript = "Hebrew (Alef-Bet א–ת including final letters ך, ם, ן, ף, ץ)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = "HEBREW_GEMATRIA_CLASSICAL_V1",
            supportedCalculations = listOf(
                "Mispar Hechrachi / Ragil (Absolute Value 1..400)",
                "Mispar Katan (Small Root Reduction 1..9)",
                "Letter Value Breakdown"
            ),
            variantPolicy = "Mispar Hechrachi assigns standard values 1..400; final letters take standard consonant values; Mispar Katan reduces total sum to 1..9.",
            reasonForStatus = "Fully documented in classical rabbinic and kabbalistic literature; implemented as dedicated gematria discipline.",
        ),

        // ── 6. Arabic Hisab al-Jummal (Mashriqi Standard) ───────────────────
        NumerologyTraditionDefinition(
            traditionId = "ARABIC_ABJAD_MASHRIQI",
            canonicalName = "Arabic Hisab al-Jummal (Mashriqi Abjad Standard)",
            discipline = NumericalDisciplineType.ABJAD_ARITHMETIC,
            culturalRegion = "Islamic Middle East / Levant",
            historicalEra = "Pre-Islamic Semitic order; codified early Islamic era (8th–14th c.)",
            primarySources = listOf(
                "Ibn Khaldun, 'The Muqaddimah' (1377 CE), Ch. 6, Section 28 (Ilm al-Huruf)",
                "Ahmad al-Buni, 'Shams al-Ma'arif al-Kubra' (c. 1225 CE)",
            ),
            acceptedScript = "Arabic (28 consonants ا–غ)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = "ARABIC_ABJAD_MASHRIQI_V1",
            supportedCalculations = listOf(
                "Jummal Kabir (Great Sum 1..1000)",
                "Jummal Saghir (Small Reduction 1..9)",
                "Tashkeel Stripping",
                "Ta Marbuta Resolution"
            ),
            variantPolicy = "Uses standard Eastern (Mashriqi) Abjad order (Abjad, Hawwaz, Hutti, Kalaman, Sa'fas, Qarashat, Thakhadh, Dazagh). Ta Marbuta = 400.",
            reasonForStatus = "Authoritative classical alphanumeric calculation codified by Ibn Khaldun; implemented as dedicated abjad discipline.",
        ),

        // ── 7. Renaissance Arithmancy (Heinrich Cornelius Agrippa) ───────────
        NumerologyTraditionDefinition(
            traditionId = "AGRIPPAN_OCCULT",
            canonicalName = "Renaissance Agrippan Arithmancy",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "Renaissance Europe (Germany / France)",
            historicalEra = "1533 CE",
            primarySources = listOf(
                "Heinrich Cornelius Agrippa von Nettesheim, 'De Occulta Philosophia libri tres' (1533), Book II, Ch. 20–34",
            ),
            acceptedScript = "Latin (A–Z with classical I/J=9 and U/V/W=200)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = "AGRIPPAN_OCCULT_V1",
            supportedCalculations = listOf(
                "Agrippan Letter Sum (1..500 base)",
                "Agrippan Root Reduction (1..9)"
            ),
            variantPolicy = "Tens and hundreds letter scale (A=1..I=9, K=10..S=90, T=100..Z=500); classical Roman letter equivalences.",
            reasonForStatus = "Published primary Renaissance manual with complete explicit numerical letter tables.",
        ),

        // ── 8. Hebrew Mispar Gadol Variant ──────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "HEBREW_MISPAR_GADOL",
            canonicalName = "Hebrew Gematria — Mispar Gadol Variant",
            discipline = NumericalDisciplineType.GEMATRIA,
            culturalRegion = "Jewish Kabbalah",
            historicalEra = "Medieval Kabbalistic",
            primarySources = listOf("Rabbi Moses Cordovero, 'Pardes Rimonim' (1591), Gate 30"),
            acceptedScript = "Hebrew (Alef-Bet א–ת including final letters ך, ם, ן, ף, ץ)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
            supportedCalculations = listOf(
                "Mispar Gadol (Hundreds 500..900 for Sofiyot)",
                "Mispar Katan (Small Root Reduction 1..9)",
                "Letter Value Breakdown"
            ),
            variantPolicy = "Final letters assigned distinct hundreds values: Kaf Sofit=500, Mem Sofit=600, Nun Sofit=700, Pe Sofit=800, Tsadi Sofit=900.",
            reasonForStatus = "Fully verified against Pardes Rimonim (Gate 30) kabbalistic final-letter traditions and golden test vectors.",
        ),

        // ── 9. Maghribi Abjad Order Variant ─────────────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "ARABIC_ABJAD_MAGHRIBI",
            canonicalName = "Arabic Hisab al-Jummal — Maghribi Order Variant",
            discipline = NumericalDisciplineType.ABJAD_ARITHMETIC,
            culturalRegion = "North Africa / Islamic Spain (Al-Andalus)",
            historicalEra = "Medieval Islamic (8th–14th c.)",
            primarySources = listOf("Ibn Khaldun, 'The Muqaddimah' (1377 CE), Ch. 6, Section 28"),
            acceptedScript = "Arabic (28 consonants ا–غ)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
            supportedCalculations = listOf(
                "Jummal Kabir (Great Sum Maghribi)",
                "Jummal Saghir (Small Reduction 1..9)",
                "Tashkeel Stripping",
                "Ta Marbuta Resolution"
            ),
            variantPolicy = "Swaps values of Sa' (ص=60), Sin (س=300), Da (ض=90), and Zha (ظ=800) according to North African scribal tradition.",
            reasonForStatus = "Fully verified against Ibn Khaldun's Muqaddimah Maghribi order and golden test vectors.",
        ),

        // ── 10. Indian Katapayadi Numerical Mnemonic System ──────────────────
        NumerologyTraditionDefinition(
            traditionId = "INDIAN_KATAPAYADI",
            canonicalName = "Indian Katapayadi Numerical Mnemonic System",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "South India (Kerala / Tamil Nadu / Sanskrit Astronomers)",
            historicalEra = "Classical Sanskrit / Aryabhata tradition (codified in Sadratnamala 1819)",
            primarySources = listOf(
                "Sankaravarman, 'Sadratnamala' (1819 CE), Prakarana 1, Verse 3–5",
                "Aryabhatiya commentaries (Suryadevayajvan, Nilakantha Somayaji)",
            ),
            acceptedScript = "Sanskrit / Devanagari phonemes (ka-ta-pa-ya vargas)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
            supportedCalculations = listOf(
                "Phoneme Segmentation",
                "Consonant Varga Mapping (1..9, 0)",
                "Samyuktakshara Conjunct Rule",
                "Ankānām Vāmato Gatiḥ (Digit Reversal)"
            ),
            variantPolicy = "Ka-Ta-Pa-Ya consonant groups assigned 1..9 and 0; vowels standalone = 0; conjunct takes last consonant; final number reversed.",
            reasonForStatus = "Fully verified with native Devanagari phoneme-aware parser, Sadratnamala verses, and historical chronogram vectors.",
        ),

        // ── 11. Chinese Nine Star Ki (Feng Shui Flying Stars) ───────────────
        NumerologyTraditionDefinition(
            traditionId = "CHINESE_NINE_STAR_KI",
            canonicalName = "Chinese Nine Star Ki (Flying Star Feng Shui)",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "China / Japan",
            historicalEra = "Tang/Song Dynasty origins; modern codification (Takashi Yoshikawa 1981)",
            primarySources = listOf(
                "Takashi Yoshikawa, 'The Ki: The Japanese Art of Divination' (1981)",
                "Jean Meeus, 'Astronomical Algorithms' (for 315.0° Li Chun Solar Longitude)",
                "I Ching (Luoshu 9-Star Sequence)",
            ),
            acceptedScript = "Gregorian Calendar Timestamp with Timezone (converted to Solar Longitude)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
            supportedCalculations = listOf(
                "Astronomical Li Chun Solar Term (315° Solar Longitude)",
                "Solar Year Determination",
                "Principal Star (1..9)",
                "Trigram & Element Association"
            ),
            variantPolicy = "Year boundary determined strictly by astronomical Li Chun (not Jan 1 or hardcoded Feb 4); principal star = 11 - (sum of digits % 9).",
            reasonForStatus = "Fully verified with isolated astronomical SolarTermEngine (Meeus algorithm) and golden test vectors.",
        ),

        // ── 12. Chinese Homophonic Number Symbolism ─────────────────────────
        NumerologyTraditionDefinition(
            traditionId = "CHINESE_HOMOPHONIC_SYMBOLISM",
            canonicalName = "Chinese Homophonic Number Symbolism (Cantonese / Mandarin)",
            discipline = NumericalDisciplineType.NUMBER_SYMBOLISM,
            culturalRegion = "China / Hong Kong / Diaspora",
            historicalEra = "Modern / Folk Tradition",
            primarySources = listOf("Folk linguistic homophony (e.g. 8 = Fa / Prosperity; 4 = Si / Death; 9 = Jiu / Longevity)"),
            acceptedScript = "Spoken Cantonese/Mandarin numerals",
            implementationStatus = TraditionImplementationStatus.SYMBOLIC_ONLY,
            variantPolicy = "Cultural phonetic superstition; does not compute life paths, compound roots, or deterministic cycles.",
            reasonForStatus = "Classified strictly as NUMBER_SYMBOLISM. Must not be disguised as a predictive calculation ruleset.",
        ),

        // ── 13. Western Angel Numbers (Doreen Virtue) ───────────────────────
        NumerologyTraditionDefinition(
            traditionId = "WESTERN_ANGEL_NUMBERS",
            canonicalName = "Modern Angel Numbers (Triple / Quadruple Sequences)",
            discipline = NumericalDisciplineType.NUMBER_SYMBOLISM,
            culturalRegion = "Modern North America / Global New Age",
            historicalEra = "2005 CE",
            primarySources = listOf("Doreen Virtue, 'Angel Numbers 101' (2008)"),
            acceptedScript = "Digit repetitions (111, 222, 333, etc.)",
            implementationStatus = TraditionImplementationStatus.SYMBOLIC_ONLY,
            variantPolicy = "Assigns subjective channeled messages to repeating clock/odometer numbers; no birth date or name algorithm.",
            reasonForStatus = "Classified strictly as NUMBER_SYMBOLISM / Divination. No reproducible biographical calculation exists.",
        ),

        // ── 14. Modern Tarot Numerology (Major Arcana Life Cards) ───────────
        NumerologyTraditionDefinition(
            traditionId = "TAROT_NUMEROLOGY",
            canonicalName = "Modern Tarot Numerology (Major Arcana Birth Cards)",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "Western Esoteric / Modern Tarot",
            historicalEra = "Late 20th Century (1984–1987)",
            primarySources = listOf(
                "Mary K. Greer, 'Tarot for Your Self: A Workbook for the Inward Journey' (1984), Ch. 2",
                "Angeles Arrien, 'The Tarot Handbook' (1987)",
            ),
            acceptedScript = "Gregorian Calendar Date (DD-MM-YYYY)",
            implementationStatus = TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
            supportedCalculations = listOf(
                "Date Calendar Sum",
                "Major Arcana Reduction (1..22)",
                "Personality Card",
                "Soul Card",
                "Special Handling for #19 and #22"
            ),
            variantPolicy = "Sum MM + DD + YYYY; reduce to <= 22 for Personality Card; reduce digits again for Soul Card; 19 yields 19/10/1 triad; 22 maps to The Fool.",
            reasonForStatus = "Fully verified against Mary K. Greer's 1984 reduction algorithm and golden reference test vectors.",
        ),

        // ── 15. Internet Composite / Popular Online Calculators ──────────────
        NumerologyTraditionDefinition(
            traditionId = "INTERNET_COMPOSITE_POPULAR",
            canonicalName = "Arbitrary Internet Composite Numerology",
            discipline = NumericalDisciplineType.NUMEROLOGY,
            culturalRegion = "Internet Blogs / SEO Calculators",
            historicalEra = "c. 2000–present",
            primarySources = listOf("Anonymous web calculators combining Pythagorean letters with Cheiro compound meanings"),
            acceptedScript = "Any",
            implementationStatus = TraditionImplementationStatus.DEPRECATED,
            variantPolicy = "Arbitrary silent mixing of incompatible traditions without attribution or mathematical justification.",
            reasonForStatus = "Explicitly REJECTED by AYNVORA Rule 72 and Phase 10.2 Quality Gate. Silent merging is strictly forbidden.",
        ),
    )

    fun getTradition(traditionId: String): NumerologyTraditionDefinition? =
        ALL_TRADITIONS.firstOrNull { it.traditionId.equals(traditionId, ignoreCase = true) }

    fun getImplementedTraditions(): List<NumerologyTraditionDefinition> =
        ALL_TRADITIONS.filter { it.implementationStatus == TraditionImplementationStatus.DOCUMENTED_AND_IMPLEMENTED }

    fun getTraditionsByDiscipline(discipline: NumericalDisciplineType): List<NumerologyTraditionDefinition> =
        ALL_TRADITIONS.filter { it.discipline == discipline }

    fun getTraditionsByStatus(status: TraditionImplementationStatus): List<NumerologyTraditionDefinition> =
        ALL_TRADITIONS.filter { it.implementationStatus == status }

    fun getAllTraditionIds(): List<String> = ALL_TRADITIONS.map { it.traditionId }
}

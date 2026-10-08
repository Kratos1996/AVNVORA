package com.aynvora.numerology

/**
 * Pure, deterministic calculation engine for the classical Indian Katapayadi system.
 *
 * Authorities:
 * - Sadratnamala (Sankara Varman, 1819 CE)
 * - Grahacaranibandhana (Haridatta, 683 CE)
 * - Aryabhatiya astronomical chronograms & commentaries
 *
 * Fundamental Shloka:
 * "ka-ṭa-pa-yādayo varṇā dhiśūnyam svarāstvakṣaram"
 * "aṅkānām vāmato gatiḥ"
 *
 * Rules:
 * 1. Ka-varga, Ta-varga, Pa-varga, Ya-varga assign values 1..9 and 0.
 * 2. Vowel signs (matras) carry no numeric value; independent vowels without consonant count as 0.
 * 3. In conjunct consonants (Samyuktakshara), only the LAST consonant takes value.
 * 4. "Ankanam vamato gatih": Multi-digit numbers are composed by reading the extracted digits in reverse.
 */
object KatapayadiEngine {

    /**
     * Consonant mapping table according to the classical Katapayadi verse.
     */
    val CONSONANT_MAP: Map<Char, Pair<Int, String>> = mapOf(
        // Ka-varga (1..9, 0)
        'क' to Pair(1, "Ka-varga"),
        'ख' to Pair(2, "Ka-varga"),
        'ग' to Pair(3, "Ka-varga"),
        'घ' to Pair(4, "Ka-varga"),
        'ङ' to Pair(5, "Ka-varga"),
        'च' to Pair(6, "Ka-varga"),
        'छ' to Pair(7, "Ka-varga"),
        'ज' to Pair(8, "Ka-varga"),
        'झ' to Pair(9, "Ka-varga"),
        'ञ' to Pair(0, "Ka-varga"),

        // Ta-varga (retroflex 1..5) & dental (6..9, 0)
        'ट' to Pair(1, "Ta-varga"),
        'ठ' to Pair(2, "Ta-varga"),
        'ड' to Pair(3, "Ta-varga"),
        'ढ' to Pair(4, "Ta-varga"),
        'ण' to Pair(5, "Ta-varga"),
        'त' to Pair(6, "Ta-varga"),
        'थ' to Pair(7, "Ta-varga"),
        'द' to Pair(8, "Ta-varga"),
        'ध' to Pair(9, "Ta-varga"),
        'न' to Pair(0, "Ta-varga"),

        // Pa-varga (1..5)
        'प' to Pair(1, "Pa-varga"),
        'फ' to Pair(2, "Pa-varga"),
        'ब' to Pair(3, "Pa-varga"),
        'भ' to Pair(4, "Pa-varga"),
        'म' to Pair(5, "Pa-varga"),

        // Ya-varga (1..9)
        'य' to Pair(1, "Ya-varga"),
        'र' to Pair(2, "Ya-varga"),
        'ल' to Pair(3, "Ya-varga"),
        'व' to Pair(4, "Ya-varga"),
        'श' to Pair(5, "Ya-varga"),
        'ष' to Pair(6, "Ya-varga"),
        'स' to Pair(7, "Ya-varga"),
        'ह' to Pair(8, "Ya-varga"),
        'ळ' to Pair(9, "Ya-varga"),
    )

    private val INDEPENDENT_VOWELS = setOf(
        'अ', 'आ', 'इ', 'ई', 'उ', 'ऊ', 'ऋ', 'ॠ', 'ऌ', 'ॡ', 'ए', 'ऐ', 'ओ', 'औ'
    )

    private const val VIRAMA = '\u094D' // ् (Halant)

    /**
     * Validates that the input text contains valid Devanagari characters and no unsupported scripts.
     */
    fun validateDevanagariInput(rawText: String): String? {
        if (rawText.isBlank()) {
            return "Katapayadi input text cannot be blank."
        }

        var devanagariCount = 0
        for (ch in rawText) {
            if (ch.isWhitespace() || ch in "-'’.,।॥") continue

            val code = ch.code
            // Devanagari Unicode block is 0900..097F
            if (code in 0x0900..0x097F) {
                devanagariCount++
            } else {
                val hex = code.toString(16).padStart(4, '0').uppercase()
                return "Unsupported character or script '$ch' (code: \\u$hex). " +
                        "Indian Katapayadi system requires Devanagari script (native क–ह). " +
                        "Latin, Arabic, and Hebrew scripts are not accepted in the Katapayadi ruleset."
            }
        }

        if (devanagariCount == 0) {
            return "Katapayadi input text must contain at least one Devanagari character (received: '$rawText')."
        }

        return null
    }

    /**
     * Normalizes Devanagari text by removing punctuation, spaces, and formatting characters.
     */
    fun normalizeDevanagari(rawText: String): String = buildString {
        for (ch in rawText) {
            val code = ch.code
            if (code in 0x0900..0x097F && ch !in "।॥") {
                append(ch)
            }
        }
    }

    /**
     * Parses Devanagari text into aksharas/phonemes and maps each to its Katapayadi digit.
     *
     * In a conjunct consonant (e.g. क् + य = क्य), only the last consonant ('य') takes value.
     * Independent vowels standing alone take 0 (dhisunyam svarastvaksaram).
     */
    fun parsePhonemes(normalizedText: String): List<KatapayadiPhoneme> {
        val result = mutableListOf<KatapayadiPhoneme>()
        var i = 0
        val len = normalizedText.length

        while (i < len) {
            val ch = normalizedText[i]

            if (INDEPENDENT_VOWELS.contains(ch)) {
                // Standalone independent vowel -> 0
                result.add(
                    KatapayadiPhoneme(
                        akshara = ch.toString(),
                        mappedConsonant = ch.toString(),
                        vargaGroup = "Svara (Vowel)",
                        digit = 0,
                    )
                )
                i++
            } else if (CONSONANT_MAP.containsKey(ch)) {
                // We have a consonant. Check if it is part of a conjunct cluster (followed by Virama)
                var lastConsonant = ch
                val clusterBuilder = StringBuilder().append(ch)
                var j = i + 1

                while (j < len && normalizedText[j] == VIRAMA) {
                    clusterBuilder.append(VIRAMA)
                    j++
                    if (j < len && CONSONANT_MAP.containsKey(normalizedText[j])) {
                        lastConsonant = normalizedText[j]
                        clusterBuilder.append(lastConsonant)
                        j++
                    } else {
                        break
                    }
                }

                // Check for subsequent vowel signs (matras), anusvara, visarga
                while (j < len && isVowelSignOrDiacritic(normalizedText[j])) {
                    clusterBuilder.append(normalizedText[j])
                    j++
                }

                // In conjunct consonants, the last consonant in the conjunct takes value
                val mapping = CONSONANT_MAP[lastConsonant] ?: Pair(0, "Unknown")
                result.add(
                    KatapayadiPhoneme(
                        akshara = clusterBuilder.toString(),
                        mappedConsonant = lastConsonant.toString(),
                        vargaGroup = mapping.second,
                        digit = mapping.first,
                    )
                )

                i = j
            } else {
                // Skip combining diacritics that were handled or standalone
                i++
            }
        }

        return result
    }

    private fun isVowelSignOrDiacritic(ch: Char): Boolean {
        val code = ch.code
        // Vowel signs: 093E..094C, Anusvara: 0902, Visarga: 0903, Candrabindu: 0901, Nukta: 093C
        return (code in 0x093E..0x094C) || code == 0x0901 || code == 0x0902 || code == 0x0903 || code == 0x093C
    }

    /**
     * Calculates Katapayadi digit sequence and reverse number composition.
     */
    fun calculate(
        rawText: String,
        rulesetId: String = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
    ): Pair<KatapayadiResult, NumerologyCalculationTrace> {
        val normalized = normalizeDevanagari(rawText)
        val phonemes = parsePhonemes(normalized)
        val extractedDigits = phonemes.map { it.digit }

        // "aṅkānām vāmato gatiḥ" - digits read in reverse order (units, tens, hundreds...)
        val reversedDigits = extractedDigits.reversed()
        val reversedNumber = if (reversedDigits.isNotEmpty()) {
            reversedDigits.joinToString("").toLongOrNull() ?: 0L
        } else {
            0L
        }

        val equation = "${phonemes.joinToString(" + ") { "${it.akshara}(${it.digit})" }} -> [${
            extractedDigits.joinToString(", ")
        }] (vamato gatih: $reversedNumber)"

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.KATAPAYADI_CODING,
            rulesetId = rulesetId,
            rawInput = rawText,
            normalizedInput = normalized,
            letterValues = extractedDigits,
            compoundSum = if (reversedNumber <= Int.MAX_VALUE) reversedNumber.toInt() else 0,
            reductionSteps = listOf(
                ReductionStep(
                    stepNumber = 1,
                    startingValue = extractedDigits.size,
                    digits = extractedDigits,
                    reducedSum = if (reversedNumber <= Int.MAX_VALUE) reversedNumber.toInt() else 0,
                    equation = equation,
                )
            ),
            finalValue = if (reversedNumber <= Int.MAX_VALUE) reversedNumber.toInt() else 0,
            isMasterNumber = false,
        )

        val result = KatapayadiResult(
            rawText = rawText,
            normalizedText = normalized,
            phonemes = phonemes,
            extractedDigits = extractedDigits,
            reversedNumber = reversedNumber,
            rulesetId = rulesetId,
            sourceRule = "SADRATNAMALA_KATAPAYADI",
        )

        return Pair(result, trace)
    }
}

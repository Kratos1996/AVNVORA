package com.aynvora.numerology

/**
 * Immutable mapping tables and deterministic normalization utilities for numerological alphabets.
 */
object NumerologyAlphabet {

    private val VOWELS = setOf('A', 'E', 'I', 'O', 'U')

    /**
     * Chaldean letter mapping table (1–8).
     * Reference: Cheiro's Book of Numbers (1926).
     * Note: 9 is sacred and excluded from individual letter assignments in the Chaldean system.
     */
    val CHALDEAN_MAP: Map<Char, Int> = mapOf(
        'A' to 1, 'I' to 1, 'J' to 1, 'Q' to 1, 'Y' to 1,
        'B' to 2, 'K' to 2, 'R' to 2,
        'C' to 3, 'G' to 3, 'L' to 3, 'S' to 3,
        'D' to 4, 'M' to 4, 'T' to 4,
        'E' to 5, 'H' to 5, 'N' to 5, 'X' to 5,
        'U' to 6, 'V' to 6, 'W' to 6,
        'O' to 7, 'Z' to 7,
        'F' to 8, 'P' to 8,
    )

    /**
     * Pythagorean sequential letter mapping table (1–9).
     * Reference: Goodwin, Matthew Oliver (1981).
     */
    val PYTHAGOREAN_MAP: Map<Char, Int> = mapOf(
        'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6, 'G' to 7, 'H' to 8, 'I' to 9,
        'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4, 'N' to 5, 'O' to 6, 'P' to 7, 'Q' to 8, 'R' to 9,
        'S' to 1, 'T' to 2, 'U' to 3, 'V' to 4, 'W' to 5, 'X' to 6, 'Y' to 7, 'Z' to 8,
    )

    /**
     * Renaissance Agrippan letter mapping table (1..500 scale).
     * Reference: Heinrich Cornelius Agrippa, "De Occulta Philosophia" (1533), Book II, Ch. 20–34.
     * Note: I and J are equivalent (9); U, V, and W are equivalent (200).
     */
    val AGRIPPAN_MAP: Map<Char, Int> = mapOf(
        'A' to 1,
        'B' to 2,
        'C' to 3,
        'D' to 4,
        'E' to 5,
        'F' to 6,
        'G' to 7,
        'H' to 8,
        'I' to 9,
        'J' to 9,
        'K' to 10,
        'L' to 20,
        'M' to 30,
        'N' to 40,
        'O' to 50,
        'P' to 60,
        'Q' to 70,
        'R' to 80,
        'S' to 90,
        'T' to 100,
        'U' to 200,
        'V' to 200,
        'W' to 200,
        'X' to 300,
        'Y' to 400,
        'Z' to 500,
    )

    fun getChaldeanValue(ch: Char): Int = CHALDEAN_MAP[ch.uppercaseChar()] ?: 0
    fun getPythagoreanValue(ch: Char): Int = PYTHAGOREAN_MAP[ch.uppercaseChar()] ?: 0
    fun getAgrippanValue(ch: Char): Int = AGRIPPAN_MAP[ch.uppercaseChar()] ?: 0

    /**
     * Normalizes a raw name for numerological calculation:
     * - Folds common Latin diacritics to basic ASCII equivalents.
     * - Converts to uppercase.
     * - Strips all non-A-Z characters (spaces, punctuation, digits, symbols).
     */
    fun normalizeName(rawName: String): String {
        return buildString {
            for (ch in rawName) {
                val folded = foldDiacritic(ch)
                val upper = folded.uppercaseChar()
                if (upper in 'A'..'Z') {
                    append(upper)
                }
            }
        }
    }

    /**
     * Validates that a raw name contains only supported characters for the specified numerological ruleset:
     * - For Hebrew Gematria: delegates to [GematriaEngine.validateHebrewInput].
     * - For Arabic Abjad: delegates to [AbjadEngine.validateArabicInput].
     * - For Lo Shu: rejects name inputs (names are unsupported in the classical Lo Shu Magic Square).
     * - For Latin-based systems (Chaldean, Pythagorean, Indian, Agrippan): strictly requires Latin alphabet
     *   (including diacritics foldable to A-Z) and rejects non-Latin scripts (Devanagari, Arabic, Hebrew, etc.).
     */
    fun validateNameInput(rawName: String, rulesetId: String = ""): String? {
        if (rawName.isBlank()) return null

        when (rulesetId) {
            NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
            NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> {
                return GematriaEngine.validateHebrewInput(rawName)
            }

            NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
            NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> {
                return AbjadEngine.validateArabicInput(rawName)
            }

            NumerologyRuleset.INDIAN_KATAPAYADI_V1.id -> {
                return KatapayadiEngine.validateDevanagariInput(rawName)
            }

            NumerologyRuleset.LO_SHU_CLASSICAL_V1.id -> {
                return "The Classical Lo Shu Magic Square tradition calculates from birth dates only; name calculations are unsupported."
            }

            NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id -> {
                return "Chinese Nine Star Ki tradition calculates from solar birth date/time only; name calculations are unsupported."
            }

            NumerologyRuleset.TAROT_BIRTH_CARD_V1.id -> {
                return "Tarot Birth Card tradition calculates from birth date only; name calculations are unsupported."
            }

            else -> {
                // Latin-based rulesets (Chaldean, Pythagorean, Indian, Agrippan)
                for (ch in rawName) {
                    if (ch.isWhitespace() || ch in "-'’.,") continue
                    val folded = foldDiacritic(ch)
                    val upper = folded.uppercaseChar()
                    if (upper !in 'A'..'Z') {
                        val hex = ch.code.toString(16).padStart(4, '0').uppercase()
                        return "Unsupported character or script '$ch' (code: \\u$hex). " +
                                "Numerology letter calculation for ruleset '${rulesetId.ifBlank { "LATIN" }}' requires Latin-transliterated characters (A–Z). " +
                                "Indic/Devanagari, Arabic, and Hebrew scripts are not silently converted without authoritative phonetic mapping."
                    }
                }

                val normalized = normalizeName(rawName)
                if (normalized.isEmpty()) {
                    return "Name must contain at least one valid Latin alphabet letter (received: '$rawName')"
                }
                return null
            }
        }
    }

    /**
     * Checks if a character is a primary vowel (A, E, I, O, U).
     */
    fun isVowel(ch: Char): Boolean = ch.uppercaseChar() in VOWELS

    /**
     * Folds common European accented characters to standard ASCII uppercase equivalents.
     */
    private fun foldDiacritic(c: Char): Char = when (c) {
        'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'à', 'á', 'â', 'ã', 'ä', 'å' -> 'A'
        'È', 'É', 'Ê', 'Ë', 'è', 'é', 'ê', 'ë' -> 'E'
        'Ì', 'Í', 'Î', 'Ï', 'ì', 'í', 'î', 'ï' -> 'I'
        'Ò', 'Ó', 'Ô', 'Õ', 'Ö', 'Ø', 'ò', 'ó', 'ô', 'õ', 'ö', 'ø' -> 'O'
        'Ù', 'Ú', 'Û', 'Ü', 'ù', 'ú', 'û', 'ü' -> 'U'
        'Ý', 'ý', 'ÿ' -> 'Y'
        'Ñ', 'ñ' -> 'N'
        'Ç', 'ç' -> 'C'
        'Š', 'š' -> 'S'
        'Ž', 'ž' -> 'Z'
        else -> c
    }
}

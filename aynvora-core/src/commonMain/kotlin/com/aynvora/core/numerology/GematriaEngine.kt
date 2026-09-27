package com.aynvora.core.numerology

/**
 * Pure, deterministic calculation engine for Classical Hebrew Gematria.
 *
 * Authorities:
 * - Sefer Yetzirah (Book of Creation, 2nd–6th c. CE)
 * - Talmud Bavli (Tractate Sanhedrin 22a)
 * - Rabbi Moses Cordovero, "Pardes Rimonim" (1591), Gate 30
 *
 * Calculations supported:
 * 1. Mispar Hechrachi / Mispar Ragil (Absolute numerical value, 1..400 scale)
 * 2. Mispar Katan (Small reduction root, 1..9 scale)
 */
object GematriaEngine {

    /**
     * Standard Mispar Hechrachi (Ragil) consonant mapping (1..400).
     * Final forms (sofit) retain standard consonant base values in classical Ragil.
     */
    val HEBREW_RAGIL_MAP: Map<Char, Int> = mapOf(
        'א' to 1,
        'ב' to 2,
        'ג' to 3,
        'ד' to 4,
        'ה' to 5,
        'ו' to 6,
        'ז' to 7,
        'ח' to 8,
        'ט' to 9,
        'י' to 10,
        'כ' to 20,
        'ך' to 20, // Final Kaf
        'ל' to 30,
        'מ' to 40,
        'ם' to 40, // Final Mem
        'נ' to 50,
        'ן' to 50, // Final Nun
        'ס' to 60,
        'ע' to 70,
        'פ' to 80,
        'ף' to 80, // Final Pe
        'צ' to 90,
        'ץ' to 90, // Final Tsadi
        'ק' to 100,
        'ר' to 200,
        'ש' to 300,
        'ת' to 400,
    )

    val HEBREW_GADOL_MAP: Map<Char, Int> = HEBREW_RAGIL_MAP.toMutableMap().apply {
        put('ך', 500) // Final Kaf
        put('ם', 600) // Final Mem
        put('ן', 700) // Final Nun
        put('ף', 800) // Final Pe
        put('ץ', 900) // Final Tsadi
    }

    fun getHebrewLetterValue(ch: Char, rulesetId: String = ""): Int {
        val map =
            if (rulesetId == NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id) HEBREW_GADOL_MAP else HEBREW_RAGIL_MAP
        return map[ch] ?: 0
    }

    /**
     * Validates that the input contains valid Hebrew consonants and no foreign scripts.
     */
    fun validateHebrewInput(rawText: String): String? {
        if (rawText.isBlank()) {
            return "Hebrew input text cannot be blank."
        }

        var hebrewConsonantCount = 0
        for (ch in rawText) {
            if (ch.isWhitespace() || ch in "-'’.,־׀") continue
            // Allow Hebrew niqqud / cantillation marks in input, which will be stripped
            if (ch in '\u0591'..'\u05C7') continue

            if (HEBREW_RAGIL_MAP.containsKey(ch)) {
                hebrewConsonantCount++
            } else {
                val hex = ch.code.toString(16).padStart(4, '0').uppercase()
                return "Unsupported character or script '$ch' (code: \\u$hex). " +
                        "Classical Hebrew Gematria strictly requires Hebrew script (א–ת). " +
                        "Latin, Arabic, and Indic scripts are not accepted in the Hebrew Gematria ruleset."
            }
        }

        if (hebrewConsonantCount == 0) {
            return "Hebrew input text must contain at least one Hebrew consonant (received: '$rawText')."
        }

        return null
    }

    /**
     * Normalizes Hebrew text by stripping vowels (niqqud), cantillation marks, spaces, and punctuation.
     */
    fun normalizeHebrew(rawText: String): String = buildString {
        for (ch in rawText) {
            if (HEBREW_RAGIL_MAP.containsKey(ch)) {
                append(ch)
            }
        }
    }

    /**
     * Calculates Mispar Hechrachi/Gadol (Absolute Value) and Mispar Katan (Small Root Reduction).
     */
    fun calculate(
        rawText: String,
        rulesetId: String = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
    ): Pair<GematriaResult, NumerologyCalculationTrace> {
        val normalized = normalizeHebrew(rawText)
        val isGadol = rulesetId == NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id
        val mapping = if (isGadol) HEBREW_GADOL_MAP else HEBREW_RAGIL_MAP
        val variantName = if (isGadol) "MISPAR_GADOL" else "MISPAR_RAGIL"
        val ruleId =
            if (isGadol) "CORDOVERO_PARDES_RIMONIM_MISPAR_GADOL" else "SEFER_YETZIRAH_MISPAR_HECHRACHI"

        val letterList = mutableListOf<Pair<String, Int>>()
        var absoluteSum = 0

        for (ch in normalized) {
            val v = mapping[ch] ?: 0
            letterList.add(Pair(ch.toString(), v))
            absoluteSum += v
        }

        // Mispar Katan (small root reduction to single digit 1..9)
        val reduction = NumerologyReductionEngine.reduce(
            if (absoluteSum > 0) absoluteSum else 1,
            MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
        )

        val steps = mutableListOf<ReductionStep>()
        steps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = absoluteSum,
                digits = letterList.map { it.second },
                reducedSum = absoluteSum,
                equation = "${letterList.joinToString(" + ") { "${it.first}(${it.second})" }} = $absoluteSum (${if (isGadol) "Mispar Gadol" else "Mispar Hechrachi"})",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            steps.add(s.copy(stepNumber = idx + 2))
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE,
            rulesetId = rulesetId,
            rawInput = rawText,
            normalizedInput = normalized,
            letterValues = letterList.map { it.second },
            compoundSum = absoluteSum,
            reductionSteps = steps,
            finalValue = absoluteSum,
            isMasterNumber = false,
        )

        val result = GematriaResult(
            rawText = rawText,
            normalizedHebrew = normalized,
            absoluteValue = absoluteSum,
            reducedValue = reduction.finalValue,
            letterValues = letterList,
            rulesetId = rulesetId,
            variant = variantName,
            sourceRule = ruleId,
        )

        return Pair(result, trace)
    }
}

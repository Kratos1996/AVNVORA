package com.aynvora.core.numerology

/**
 * Pure, deterministic calculation engine for Arabic Hisab al-Jummal (Eastern Mashriqi Standard).
 *
 * Authorities:
 * - Ibn Khaldun, "The Muqaddimah" (1377 CE), Chapter 6, Section 28 (Ilm al-Huruf)
 * - Ahmad al-Buni, "Shams al-Ma'arif al-Kubra" (c. 1225 CE)
 *
 * Calculations supported:
 * 1. Jummal Kabir (Great Sum, 1..1000 scale)
 * 2. Jummal Saghir (Small reduction root, 1..9 scale)
 */
object AbjadEngine {

    /**
     * Standard Eastern (Mashriqi) Abjad letter values (1..1000).
     */
    val ARABIC_MASHRIQI_MAP: Map<Char, Int> = mapOf(
        // Abjad (1..4)
        'ا' to 1,
        'ب' to 2,
        'ج' to 3,
        'د' to 4,
        // Hawwaz (5..7)
        'ه' to 5,
        'و' to 6,
        'ز' to 7,
        // Hutti (8..10)
        'ح' to 8,
        'ط' to 9,
        'ي' to 10,
        // Kalaman (20..50)
        'ك' to 20,
        'ل' to 30,
        'م' to 40,
        'ن' to 50,
        // Sa'fas (60..90)
        'س' to 60,
        'ع' to 70,
        'ف' to 80,
        'ص' to 90,
        // Qarashat (100..400)
        'ق' to 100,
        'ر' to 200,
        'ش' to 300,
        'ت' to 400,
        'ة' to 400, // Ta Marbuta (grammatical variant of Taa ت in classical Jummal)
        // Thakhadh (500..700)
        'ث' to 500,
        'خ' to 600,
        'ذ' to 700,
        // Dazagh (800..1000)
        'ض' to 800,
        'ظ' to 900,
        'غ' to 1000,
    )

    /**
     * Classical Western (Maghribi / Andalusian) Abjad letter values.
     * Authority: Ibn Khaldun, The Muqaddimah (1377 CE), Ch. 6, Sec. 28.
     * Swaps values according to the Maghribi mnemonic:
     * Sa'fadh (Saad=60, Ayn=70, Faa=80, Daad=90)
     * Qarast (Qaaf=100, Raa=200, Seen=300, Taa=400)
     * Thakhadh (Thaa=500, Khaa=600, Dhaal=700)
     * Zaghsh (Zhaa=800, Ghayn=900, Sheen=1000)
     */
    val ARABIC_MAGHRIBI_MAP: Map<Char, Int> = ARABIC_MASHRIQI_MAP.toMutableMap().apply {
        put('ص', 60)   // Saad
        put('ض', 90)   // Daad
        put('س', 300)  // Seen
        put('ظ', 800)  // Zhaa
        put('غ', 900)  // Ghayn
        put('ش', 1000) // Sheen
    }

    /**
     * Maps Arabic orthographic variants (hamzas, alif maksura) to base Abjad consonants.
     */
    fun canonicalizeChar(ch: Char): Char? = when (ch) {
        'ء', 'أ', 'إ', 'آ', 'ٱ', 'ا' -> 'ا'
        'ؤ' -> 'و'
        'ئ', 'ى', 'ي' -> 'ي'
        'ة' -> 'ة'
        else -> if (ARABIC_MASHRIQI_MAP.containsKey(ch)) ch else null
    }

    fun getArabicLetterValue(ch: Char, rulesetId: String = ""): Int {
        val canon = canonicalizeChar(ch) ?: return 0
        val map =
            if (rulesetId == NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id) ARABIC_MAGHRIBI_MAP else ARABIC_MASHRIQI_MAP
        return map[canon] ?: 0
    }

    /**
     * Validates that the input contains valid Arabic consonants and no foreign scripts.
     */
    fun validateArabicInput(rawText: String): String? {
        if (rawText.isBlank()) {
            return "Arabic input text cannot be blank."
        }

        var arabicConsonantCount = 0
        for (ch in rawText) {
            if (ch.isWhitespace() || ch in "-'’.,،؛؟") continue
            // Allow Arabic diacritics (harakat / tashkeel / dagger alif) in input
            if (ch in '\u064B'..'\u065F' || ch == '\u0670' || ch == '\u0640') continue

            val canon = canonicalizeChar(ch)
            if (canon != null) {
                arabicConsonantCount++
            } else {
                val hex = ch.code.toString(16).padStart(4, '0').uppercase()
                return "Unsupported character or script '$ch' (code: \\u$hex). " +
                        "Arabic Hisab al-Jummal strictly requires Arabic script (ا–غ). " +
                        "Latin, Hebrew, and Indic scripts are not accepted in the Arabic Abjad ruleset."
            }
        }

        if (arabicConsonantCount == 0) {
            return "Arabic input text must contain at least one Arabic consonant (received: '$rawText')."
        }

        return null
    }

    /**
     * Normalizes Arabic text by canonicalizing consonant variants, stripping tashkeel, tatweel, and whitespace.
     */
    fun normalizeArabic(rawText: String): String = buildString {
        for (ch in rawText) {
            val canon = canonicalizeChar(ch)
            if (canon != null) {
                append(canon)
            }
        }
    }

    /**
     * Calculates Jummal Kabir (Great Sum) and Jummal Saghir (Small Root Reduction).
     */
    fun calculate(
        rawText: String,
        rulesetId: String = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
    ): Pair<AbjadResult, NumerologyCalculationTrace> {
        val normalized = normalizeArabic(rawText)
        val isMaghribi = rulesetId == NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id
        val mapping = if (isMaghribi) ARABIC_MAGHRIBI_MAP else ARABIC_MASHRIQI_MAP
        val variantName = if (isMaghribi) "MAGHRIBI" else "MASHRIQI"
        val ruleId =
            if (isMaghribi) "IBN_KHALDUN_JUMMAL_MAGHRIBI" else "IBN_KHALDUN_JUMMAL_MASHRIQI"

        val letterList = mutableListOf<Pair<String, Int>>()
        var kabirSum = 0

        for (ch in normalized) {
            val v = mapping[ch] ?: 0
            letterList.add(Pair(ch.toString(), v))
            kabirSum += v
        }

        // Jummal Saghir (small root reduction to single digit 1..9)
        val reduction = NumerologyReductionEngine.reduce(
            if (kabirSum > 0) kabirSum else 1,
            MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
        )

        val steps = mutableListOf<ReductionStep>()
        steps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = kabirSum,
                digits = letterList.map { it.second },
                reducedSum = kabirSum,
                equation = "${letterList.joinToString(" + ") { "${it.first}(${it.second})" }} = $kabirSum (${if (isMaghribi) "Jummal Kabir Maghribi" else "Jummal Kabir Mashriqi"})",
            )
        )
        reduction.steps.forEachIndexed { idx, s ->
            steps.add(s.copy(stepNumber = idx + 2))
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.ABJAD_KABIR_VALUE,
            rulesetId = rulesetId,
            rawInput = rawText,
            normalizedInput = normalized,
            letterValues = letterList.map { it.second },
            compoundSum = kabirSum,
            reductionSteps = steps,
            finalValue = kabirSum,
            isMasterNumber = false,
        )

        val result = AbjadResult(
            rawText = rawText,
            normalizedArabic = normalized,
            kabirValue = kabirSum,
            saghirValue = reduction.finalValue,
            letterValues = letterList,
            rulesetId = rulesetId,
            variant = variantName,
            sourceRule = ruleId,
        )

        return Pair(result, trace)
    }
}

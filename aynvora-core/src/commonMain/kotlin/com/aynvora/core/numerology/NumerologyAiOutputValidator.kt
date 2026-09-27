package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult

/**
 * Output validator ensuring on-device SLM output satisfies strict Numerology domain rules,
 * ruleset fidelity, number preservation, and safety policies.
 */
class NumerologyAiOutputValidator {

    /**
     * Validates generated raw text against the immutable [NumerologyAiGroundingContext].
     *
     * Returns [AynvoraResult.Success] with sanitized text if valid,
     * or [AynvoraResult.Failure] describing the exact violation so the caller can fall back.
     */
    fun validateOutput(
        rawText: String,
        context: NumerologyAiGroundingContext
    ): AynvoraResult<String> {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return AynvoraResult.Failure.InternalFailure("AI generation produced empty content")
        }

        val lower = trimmed.lowercase()

        // 1. Fatalistic prediction rejection
        val fatalisticKeywords = listOf(
            "will die", "you will die", "death is certain", "death is guaranteed",
            "accident is certain", "will get cancer", "guaranteed future", "definitely happen",
            "guarantees that", "definite future", "promotion will happen", "guaranteed to happen",
            "निधन निश्चित है", "मृत्यु होगी", "मौत निश्चित", "भविष्य तय है"
        )
        for (badWord in fatalisticKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting fatalistic future claims: '$badWord'"
                )
            }
        }

        // 2. Medical claim rejection
        val medicalKeywords = listOf(
            "will cure", "cures cancer", "stop taking medicine", "stop your medication",
            "cure any disease", "medical diagnosis", "disease treatment",
            "बीमारी ठीक हो जाएगी", "दवा बंद कर दें", "इलाज की जरूरत नहीं"
        )
        for (badWord in medicalKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting medical claims: '$badWord'"
                )
            }
        }

        // 3. Financial guarantee rejection
        val financialKeywords = listOf(
            "guaranteed wealth", "surely win lottery", "100% profit guaranteed",
            "guaranteed jackpot", "you will be rich", "guaranteed success",
            "धन लाभ निश्चित है", "लॉटरी जीतेंगे", "करोड़पति बनना तय"
        )
        for (badWord in financialKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting financial guarantees: '$badWord'"
                )
            }
        }

        // 4. Scientific claim rejection
        val scientificKeywords = listOf(
            "scientifically proven", "scientific law", "scientific proof",
            "proven by science", "वैज्ञानिक प्रमाण", "वैज्ञानिक नियम"
        )
        for (badWord in scientificKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by claiming numerology is scientific fact: '$badWord'"
                )
            }
        }

        // 5. Non-personality system violation rejection
        if (!context.isPersonalityInterpretation) {
            val personalityViolations = listOf(
                "your personality is",
                "your emotional character",
                "your love compatibility",
                "your marriage life",
                "you are naturally introverted",
                "you are naturally extroverted",
                "आपका व्यक्तित्व",
                "आपका वैवाहिक जीवन"
            )
            for (pViolation in personalityViolations) {
                if (lower.contains(pViolation)) {
                    return AynvoraResult.Failure.InternalFailure(
                        "Tradition '${context.rulesetName}' is a non-personality system; output violated contract by generating personality analysis: '$pViolation'"
                    )
                }
            }
        }

        // 6. Recalculation and AI Numerical Authority rejection
        val recalculationClaims = listOf(
            "i calculated", "i recalculated", "calculated it myself",
            "recalculated as", "recalculated to", "new formula",
            "calculated by ai", "overriding the calculation",
            "disregard the number", "aynvora is wrong", "your number is actually",
            "the correct number is"
        )
        for (claim in recalculationClaims) {
            if (lower.contains(claim)) {
                val hasContradiction = context.calculatedValues.values.none { expected ->
                    lower.contains("$claim $expected")
                }
                if (hasContradiction) {
                    return AynvoraResult.Failure.InternalFailure(
                        "Output attempted unauthorized calculation override: '$claim'"
                    )
                }
            }
        }

        // 7. Invented Source and Unsupported Authority rejection
        val inventedSourceMarkers = listOf(
            "according to the secret book", "invented source", "secret manuscript",
            "unpublished revelation", "hidden ancient scroll", "unsupported authority",
            "according to an invented book", "unverified modern channel"
        )
        for (marker in inventedSourceMarkers) {
            if (lower.contains(marker)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output cited unsupported or invented source: '$marker'"
                )
            }
        }

        // 8. Number Preservation check
        // If user question attempts to alter numbers (e.g. "I am 8, rewrite it"), output must not adopt the wrong number as the calculated result
        context.calculatedValues.forEach { (type, expectedVal) ->
            val expectedInt = expectedVal.toIntOrNull()
            if (expectedInt != null && expectedInt in 1..33) {
                val conflictingStatements = listOf(
                    "radical number is",
                    "moolank is",
                    "destiny number is",
                    "life path is",
                    "life path number is"
                )
                for (stmt in conflictingStatements) {
                    val stmtIdx = lower.indexOf(stmt)
                    if (stmtIdx != -1) {
                        val snippet =
                            lower.substring(stmtIdx, (stmtIdx + 40).coerceAtMost(lower.length))
                        val foundDigits =
                            Regex("""\b\d+\b""").findAll(snippet).map { it.value.toInt() }.toList()
                        if (foundDigits.isNotEmpty() && !foundDigits.contains(expectedInt)) {
                            if (type == "DESTINY_NUMBER" && (stmt.contains("life path") || stmt.contains(
                                    "destiny"
                                ))
                            ) {
                                return AynvoraResult.Failure.InternalFailure(
                                    "Output contradicted calculated value: claimed '$snippet' instead of calculated $expectedVal"
                                )
                            }
                            if (type == "RADICAL_NUMBER" && (stmt.contains("radical") || stmt.contains(
                                    "moolank"
                                ))
                            ) {
                                return AynvoraResult.Failure.InternalFailure(
                                    "Output contradicted calculated value: claimed '$snippet' instead of calculated $expectedVal"
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Ruleset isolation check (when NOT cross-tradition comparison)
        if (context.questionCategory != NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON) {
            when (context.rulesetId) {
                NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id -> {
                    if (lower.contains("cheiro's method dictates") || lower.contains("navagraha planetary ruler") || lower.contains(
                            "lo shu magic square"
                        )
                    ) {
                        return AynvoraResult.Failure.InternalFailure("Pythagorean explanation leaked unauthorized foreign tradition mechanics")
                    }
                }

                NumerologyRuleset.CHALDEAN_CHEIRO_V1.id -> {
                    if (lower.contains("pythagorean sequential 1 to 9 reduction") || lower.contains(
                            "lo shu magic square"
                        )
                    ) {
                        return AynvoraResult.Failure.InternalFailure("Chaldean explanation leaked unauthorized foreign tradition mechanics")
                    }
                }

                NumerologyRuleset.LO_SHU_CLASSICAL_V1.id -> {
                    if (lower.contains("life path reduction") || lower.contains("navagraha planetary ruler")) {
                        return AynvoraResult.Failure.InternalFailure("Lo Shu explanation leaked unauthorized Western or Vedic mechanics")
                    }
                }

                NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id -> {
                    if (lower.contains("pythagorean sequential 1 to 9 reduction") || lower.contains(
                            "lo shu magic square"
                        )
                    ) {
                        return AynvoraResult.Failure.InternalFailure("Indian Ank Jyotish explanation leaked unauthorized Western or Lo Shu mechanics")
                    }
                }

                NumerologyRuleset.TAROT_BIRTH_CARD_V1.id -> {
                    if (lower.contains("navagraha planetary ruler") || lower.contains("lo shu arrows")) {
                        return AynvoraResult.Failure.InternalFailure("Tarot explanation leaked unauthorized Vedic or Lo Shu mechanics")
                    }
                }
            }
        }

        return AynvoraResult.Success(trimmed)
    }
}

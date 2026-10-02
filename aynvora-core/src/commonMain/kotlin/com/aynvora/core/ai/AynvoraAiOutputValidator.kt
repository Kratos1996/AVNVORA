package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult

/**
 * Result of the output safety and boundary audit.
 */
sealed class OutputValidationResult {
    data class Valid(val sanitizedText: String) : OutputValidationResult()
    data class Invalid(val reason: String, val violation: AynvoraSafetyConstraint) :
        OutputValidationResult()
}

/**
 * Centralized on-device AI output validator enforcing strict source grounding,
 * numeric preservation, language integrity, and ethical safety boundaries.
 */
class AynvoraAiOutputValidator {

    private val fatalisticCertaintyKeywords = listOf(
        "you are doomed",
        "guaranteed to happen",
        "inevitable death",
        "you will die on",
        "100% destined without choice",
        "inescapable curse",
        "cannot change your fate",
    )

    private val medicalDiagnosisKeywords = listOf(
        "you have cancer",
        "diagnosed with",
        "cure your disease",
        "stop taking medication",
        "substitute for medical treatment",
        "cures diabetes",
        "medical diagnosis",
    )

    private val financialGuaranteeKeywords = listOf(
        "guaranteed profit",
        "buy this stock",
        "lottery winning numbers",
        "sure financial windfall",
        "guaranteed riches",
        "invest all your money",
    )

    private val pseudoscienceKeywords = listOf(
        "nasa officially confirmed",
        "quantum physics proves astrology",
        "scientifically proven by physics that your sign",
        "quantum wavelength of gemstones alters dna",
        "harvard medical study proves horoscope",
    )

    /**
     * Validates raw AI output against request constraints, numbers, source provenance,
     * and safety invariants.
     */
    fun validate(
        rawOutput: String,
        request: AynvoraAiRequest,
    ): OutputValidationResult {
        val trimmed = rawOutput.trim()
        if (trimmed.isBlank()) {
            return OutputValidationResult.Invalid(
                reason = "AI output is blank",
                violation = AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
            )
        }

        val lower = trimmed.lowercase()

        // 1. Check for fatalistic certainty
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY)) {
            for (keyword in fatalisticCertaintyKeywords) {
                if (lower.contains(keyword)) {
                    return OutputValidationResult.Invalid(
                        reason = "Output contains fatalistic certainty: '$keyword'",
                        violation = AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
                    )
                }
            }
        }

        // 2. Check for medical diagnosis
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS)) {
            for (keyword in medicalDiagnosisKeywords) {
                if (lower.contains(keyword)) {
                    return OutputValidationResult.Invalid(
                        reason = "Output contains medical diagnosis or prescriptive claims: '$keyword'",
                        violation = AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS,
                    )
                }
            }
        }

        // 3. Check for financial guarantee
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE)) {
            for (keyword in financialGuaranteeKeywords) {
                if (lower.contains(keyword)) {
                    return OutputValidationResult.Invalid(
                        reason = "Output contains financial guarantee or speculative advice: '$keyword'",
                        violation = AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE,
                    )
                }
            }
        }

        // 4. Check for unsupported scientific claims
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.NO_UNSUPPORTED_SCIENTIFIC_CLAIMS)) {
            for (keyword in pseudoscienceKeywords) {
                if (lower.contains(keyword)) {
                    return OutputValidationResult.Invalid(
                        reason = "Output manufactures false scientific authority: '$keyword'",
                        violation = AynvoraSafetyConstraint.NO_UNSUPPORTED_SCIENTIFIC_CLAIMS,
                    )
                }
            }
        }

        // 5. Cross-feature isolation check
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE)) {
            val allowedDomains = request.contributingDomains
            if (!allowedDomains.contains(CoreFeatureId.TAROT)) {
                if (lower.contains("tarot") || lower.contains("major arcana") || lower.contains("three of cups") || lower.contains("the fool card")) {
                    return OutputValidationResult.Invalid(
                        reason = "Cross-feature leakage detected: Tarot mentioned in non-Tarot context",
                        violation = AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE,
                    )
                }
            }
            if (!allowedDomains.contains(CoreFeatureId.PALMISTRY)) {
                if (lower.contains("palmar crease") || lower.contains("mount of jupiter on your hand") || lower.contains("heart line on your palm")) {
                    return OutputValidationResult.Invalid(
                        reason = "Cross-feature leakage detected: Palmistry mentioned in non-Palmistry context",
                        violation = AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE,
                    )
                }
            }
            if (!allowedDomains.contains(CoreFeatureId.NUMEROLOGY)) {
                if (lower.contains("life path number") || lower.contains("pythagorean vibration")) {
                    return OutputValidationResult.Invalid(
                        reason = "Cross-feature leakage detected: Numerology mentioned in non-Numerology context",
                        violation = AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE,
                    )
                }
            }
        }

        // 6. Number & Astrological Fact Preservation check
        if (request.safetyConstraints.contains(AynvoraSafetyConstraint.PRESERVE_NUMERICAL_FACTS) ||
            request.safetyConstraints.contains(AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING)) {
            val allSigns = listOf(
                "aries" to "मेष", "taurus" to "वृषभ", "gemini" to "मिथुन", "cancer" to "कर्क",
                "leo" to "सिंह", "virgo" to "कन्या", "libra" to "तुला", "scorpio" to "वृश्चिक",
                "sagittarius" to "धनु", "capricorn" to "मकर", "aquarius" to "कुम्भ", "pisces" to "मीन"
            )

            // Check if Muntha sign in evidence is contradicted in output
            val munthaEvidence = request.evidence.firstOrNull {
                it.summary.contains("Muntha", ignoreCase = true) || it.summary.contains("मुन्था")
            }
            if (munthaEvidence != null) {
                val munthaSummary = munthaEvidence.summary.lowercase()
                for ((enSign, hiSign) in allSigns) {
                    if (munthaSummary.contains(enSign) || munthaSummary.contains(hiSign)) {
                        // Found factual sign. Ensure output doesn't claim Muntha is another sign
                        for ((otherEn, otherHi) in allSigns) {
                            if (otherEn != enSign && otherHi != hiSign) {
                                if (lower.contains("muntha in $otherEn") ||
                                    lower.contains("muntha is $otherEn") ||
                                    lower.contains("मुन्था $otherHi में") ||
                                    lower.contains("मुन्था $otherHi राशि")) {
                                    return OutputValidationResult.Invalid(
                                        reason = "Output contradicts deterministic Muntha sign: expected $enSign but claimed $otherEn",
                                        violation = AynvoraSafetyConstraint.PRESERVE_NUMERICAL_FACTS,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Unsupported Feature & Hallucination Boundary Check (Phase 10.27)
        val hasKpEvidence = request.evidence.any { it.domain.name == "KP" || it.summary.contains("KP", ignoreCase = true) }
        if (!hasKpEvidence && (lower.contains("calculate chart without tools") || lower.contains("without using tools") || lower.contains("invent a sanskrit source"))) {
            return OutputValidationResult.Invalid(
                reason = "Model generated ungrounded astrological calculations without tool execution",
                violation = AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
            )
        }

        // Lal Kitab is strictly RESEARCH_ONLY in Phase 10.27
        if (request.question.contains("Lal Kitab", ignoreCase = true) && request.question.contains("calculate", ignoreCase = true)) {
            val acknowledgesResearch = lower.contains("research-only") || lower.contains("research only") || lower.contains("not supported") || lower.contains("not available")
            if (!acknowledgesResearch && (lower.contains("pukka ghar calculated") || lower.contains("exact remedy calculated"))) {
                return OutputValidationResult.Invalid(
                    reason = "Model pretended to produce deterministic calculations for research-only Lal Kitab",
                    violation = AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
                )
            }
        }

        // 8. Language check
        if (request.locale.startsWith("hi")) {
            val hasDevanagari = trimmed.any { it in '\u0900'..'\u097F' }
            if (!hasDevanagari && trimmed.length > 50 && !lower.contains("gita")) {
                return OutputValidationResult.Invalid(
                    reason = "Requested locale 'hi' but generated output lacks Devanagari script",
                    violation = AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
                )
            }
        }

        return OutputValidationResult.Valid(trimmed)
    }
}

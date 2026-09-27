package com.aynvora.core.numerology

import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.FallbackLocalizationProvider
import com.aynvora.core.localization.LocalizationProvider
import com.aynvora.core.localization.RawLocalizationKey
import com.aynvora.core.result.AynvoraResult

/**
 * Pure deterministic, source-grounded explanation engine.
 *
 * Used as the robust fallback when:
 * - On-device SLM is uninstalled or downloading.
 * - Hardware memory is constrained.
 * - Inference times out or is cancelled.
 * - SLM output fails domain validation or safety checks.
 * - The requested locale requires verified, handcrafted localized phrasing.
 *
 * Invariant: Completely offline, zero probabilistic hallucination, 100% source-grounded.
 */
class DeterministicNumerologyExplanationEngine(
    private val localizationProvider: LocalizationProvider? = null,
) : NumerologyExplanationEngine {

    override suspend fun explain(context: NumerologyAiGroundingContext): AynvoraResult<NumerologyAiResponse> {
        val locale = AynvoraLocale.fromId(context.requestedLocale)
        val provider = localizationProvider ?: FallbackLocalizationProvider(locale)

        val answerText = buildString {
            // 1. Non-Personality System Banner (if applicable)
            if (!context.isPersonalityInterpretation && context.nonPersonalityNoticeKey != null) {
                val rawNotice = provider.get(RawLocalizationKey(context.nonPersonalityNoticeKey))
                val notice = if (rawNotice == context.nonPersonalityNoticeKey) {
                    when (context.nonPersonalityNoticeKey) {
                        "numerology.interp.notice.alphanumeric" ->
                            "Non-Personality System Notice: This tradition operates as an alphanumeric calculation system, not a personality horoscope."

                        "numerology.interp.notice.mnemonic" ->
                            "Non-Personality System Notice: Katapayadi is a mnemonic numerical encoding system; AYNVORA does not treat it as a personality horoscope."

                        else ->
                            "Non-Personality System Notice: This tradition is an alphanumeric system, not a personality horoscope."
                    }
                } else rawNotice
                appendLine("[$notice]")
                appendLine()
            }

            when (context.questionCategory) {
                NumerologyAiQuestionCategory.EXPLAIN_RESULT -> {
                    appendLine(explainResult(context, provider))
                }

                NumerologyAiQuestionCategory.EXPLAIN_CALCULATION -> {
                    appendLine(explainCalculation(context, provider))
                }

                NumerologyAiQuestionCategory.EXPLAIN_TRADITION -> {
                    appendLine(explainTradition(context, provider))
                }

                NumerologyAiQuestionCategory.COMPARE_METHODS -> {
                    appendLine(explainMethodsComparison(context, provider))
                }

                NumerologyAiQuestionCategory.EXPLAIN_INTERPRETATION -> {
                    appendLine(explainInterpretation(context, provider))
                }

                NumerologyAiQuestionCategory.CLARIFY_SOURCE -> {
                    appendLine(explainSource(context, provider))
                }

                NumerologyAiQuestionCategory.REFLECTIVE_QUESTION -> {
                    appendLine(explainReflective(context, provider))
                }

                NumerologyAiQuestionCategory.REPORT_SUMMARY -> {
                    appendLine(explainReportSummary(context, provider))
                }

                NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON -> {
                    appendLine(explainCrossTraditionComparison(context, provider))
                }
            }
        }.trim()

        val referencedEvidenceIds = context.evidenceItems.map { it.evidenceId }
        val citedSources =
            context.sourceReferences.ifEmpty { listOf(context.primarySourceReference) }

        return AynvoraResult.Success(
            NumerologyAiResponse(
                answer = answerText,
                questionCategory = context.questionCategory,
                referencedRulesetId = context.rulesetId,
                referencedEvidenceIds = referencedEvidenceIds,
                citedSources = citedSources,
                locale = context.requestedLocale,
                safetyStatus = NumerologyAiSafetyStatus.DETERMINISTIC_DIRECT,
                isFallback = true,
                fallbackReason = "DETERMINISTIC_SOURCE_GROUNDING",
                modelMetadata = "aynvora-deterministic-numerology-v1",
                isOfflineExecution = true,
            )
        )
    }

    private fun explainResult(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Verified Calculated Values:")
            context.calculatedValues.forEach { (k, v) ->
                appendLine("• $k: $v")
            }
            appendLine()

            val primary = context.primaryInterpretation
            if (primary != null) {
                val title = provider.get(RawLocalizationKey(primary.titleKey))
                val summary = provider.get(RawLocalizationKey(primary.summaryKey))
                val reflection = provider.get(RawLocalizationKey(primary.reflectionKey))

                appendLine("• $title")
                appendLine(summary)
                appendLine()
                appendLine("Contemplative Reflection: $reflection")
            } else {
                appendLine("Calculated values for ${context.rulesetName}:")
                context.calculatedValues.forEach { (k, v) ->
                    appendLine("- $k: $v")
                }
            }

            if (context.secondaryInterpretations.isNotEmpty()) {
                appendLine()
                appendLine("Additional Dimensions:")
                context.secondaryInterpretations.take(3).forEach { sec ->
                    val secTitle = provider.get(RawLocalizationKey(sec.titleKey))
                    val secSummary = provider.get(RawLocalizationKey(sec.summaryKey))
                    appendLine("- $secTitle: $secSummary")
                }
            }
        }
    }

    private fun explainCalculation(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Calculation Trace and Mathematical Derivation (${context.rulesetName}):")
            appendLine("Birth Date: ${context.birthDateDisplay}")
            if (!context.fullNameNormalized.isNullOrBlank()) {
                appendLine("Input Text (Normalized): ${context.fullNameNormalized}")
            }
            appendLine()
            if (context.calculationTraces.isNotEmpty()) {
                context.calculationTraces.forEach { (type, trace) ->
                    appendLine("[$type Calculation]")
                    appendLine(trace)
                    appendLine()
                }
            } else {
                appendLine("Calculations executed deterministically under ruleset ${context.rulesetId}.")
            }
            appendLine("Note: AYNVORA calculations follow exact historical reduction formulas without probabilistic estimates.")
        }
    }

    private fun explainTradition(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        val ruleset = NumerologyRuleset.fromId(context.rulesetId)
        return buildString {
            appendLine("Tradition Profile: ${context.rulesetName}")
            appendLine("Authority Framework: ${ruleset?.authorityDescription ?: "Traditional numerological authority"}")
            appendLine("Historical Source: ${ruleset?.primarySourceReference ?: context.primarySourceReference}")
            appendLine("Letter System: ${ruleset?.nameNumberSystem ?: "N/A"}")
            appendLine("Master Number Policy: ${ruleset?.masterNumberPolicy ?: "N/A"}")
            appendLine("Date Reduction Method: ${ruleset?.dateReductionMethod ?: "N/A"}")
            appendLine()
            appendLine("This tradition operates as a symbolic cultural framework for personal reflection, not a predictive or scientific mechanism.")
        }
    }

    private fun explainMethodsComparison(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Methodology Comparison (${context.rulesetName}):")
            appendLine(
                "• The active ruleset '${context.rulesetName}' uses date reduction method '${
                    NumerologyRuleset.fromId(
                        context.rulesetId
                    )?.dateReductionMethod
                }'."
            )
            appendLine(
                "• In contrast to other systems, master numbers (11, 22, 33) are handled per the '${
                    NumerologyRuleset.fromId(
                        context.rulesetId
                    )?.masterNumberPolicy
                }' rule."
            )
            appendLine("• Letter values follow the '${NumerologyRuleset.fromId(context.rulesetId)?.nameNumberSystem}' alphabet mapping table.")
            appendLine("• Each system maintains separate historical roots and should be evaluated within its own traditional context.")
        }
    }

    private fun explainInterpretation(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        val primary = context.primaryInterpretation
        return if (primary != null) {
            val title = provider.get(RawLocalizationKey(primary.titleKey))
            val summary = provider.get(RawLocalizationKey(primary.summaryKey))
            val reflection = provider.get(RawLocalizationKey(primary.reflectionKey))
            """
            Archetype Deep Dive: $title
            
            Core Meaning:
            $summary
            
            Traditional Reflection:
            $reflection
            
            Historical Authority:
            ${primary.sourceReferences.joinToString("; ")}
            """.trimIndent()
        } else {
            "No direct single archetype interpretation found for the current configuration. Please refer to individual calculated values."
        }
    }

    private fun explainSource(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Historical Source Citations and Source Provenance:")
            val sources =
                context.sourceReferences.ifEmpty { listOf(context.primarySourceReference) }
            sources.forEachIndexed { i, src ->
                appendLine("${i + 1}. $src")
            }
            appendLine()
            appendLine("Ruleset: ${context.rulesetName} (${context.rulesetId}, Version ${context.rulesetVersion})")
            appendLine("AYNVORA preserves historical text citations without artificial additions or synthetic attributions.")
        }
    }

    private fun explainReflective(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        val primary = context.primaryInterpretation
        val reflPrompt = if (primary != null) {
            provider.get(RawLocalizationKey(primary.reflectionKey))
        } else {
            "Consider how the qualities of your calculated numbers manifest in your daily contemplation and personal growth."
        }
        return """
        Contemplative Reflection and Reflective Contemplation:
        
        $reflPrompt
        
        Inquiry Guidance:
        • How does this archetype invite you to observe your habits and aspirations?
        • What areas of balance or growth does this traditional symbol suggest?
        
        Reminder: Numerological symbols are contemplative mirrors for self-awareness, never deterministic prophecies.
        """.trimIndent()
    }

    private fun explainReportSummary(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Numerology Profile Summary (${context.rulesetName}):")
            appendLine("Birth Date: ${context.birthDateDisplay}")
            appendLine()
            appendLine("Key Calculations:")
            context.calculatedValues.forEach { (type, value) ->
                appendLine("• $type: $value")
            }
            context.primaryInterpretation?.let { p ->
                appendLine()
                val title = provider.get(RawLocalizationKey(p.titleKey))
                val summary = provider.get(RawLocalizationKey(p.summaryKey))
                appendLine("Primary Focus: $title")
                appendLine(summary)
            }
            appendLine()
            appendLine("Sources: ${context.sourceReferences.joinToString("; ")}")
        }
    }

    private fun explainCrossTraditionComparison(
        context: NumerologyAiGroundingContext,
        provider: LocalizationProvider
    ): String {
        return buildString {
            appendLine("Cross-Tradition Comparative Analysis:")
            appendLine()
            appendLine("1. Primary Active Tradition: ${context.rulesetName}")
            context.calculatedValues.forEach { (k, v) ->
                appendLine("   - $k: $v")
            }
            context.primaryInterpretation?.let { p ->
                val title = provider.get(RawLocalizationKey(p.titleKey))
                appendLine("   - Archetype: $title")
            }
            appendLine()

            if (context.comparisonProfiles.isNotEmpty()) {
                appendLine("2. Compared Traditions (Separately Evaluated):")
                context.comparisonProfiles.forEach { (rId, values) ->
                    val rName = NumerologyRuleset.fromId(rId)?.name ?: rId
                    appendLine("• $rName ($rId):")
                    values.forEach { (k, v) ->
                        appendLine("   - $k: $v")
                    }
                    context.comparisonInterpretations[rId]?.firstOrNull()?.let { cInterp ->
                        val cTitle = provider.get(RawLocalizationKey(cInterp.titleKey))
                        appendLine("   - Archetype: $cTitle")
                    }
                    appendLine()
                }
            } else {
                appendLine("No secondary verified result was provided for direct comparison.")
            }
            appendLine("Note: AYNVORA maintains strict tradition isolation and does NOT blend distinct numerological traditions into a singular composite meaning.")
        }
    }
}

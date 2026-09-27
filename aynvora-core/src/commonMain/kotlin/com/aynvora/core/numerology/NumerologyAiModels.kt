package com.aynvora.core.numerology

import com.aynvora.core.ai.AiEvidence
import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.Serializable

/**
 * Typed categories of user questions supported by the Numerology conversational AI layer.
 */
@Serializable
enum class NumerologyAiQuestionCategory {
    /** Explaining what a specific calculated number (e.g. Life Path, Moolank) traditionally represents. */
    EXPLAIN_RESULT,

    /** Explaining how the number was step-by-step derived from birth date or name letters. */
    EXPLAIN_CALCULATION,

    /** Explaining the cultural origin, historical texts, and methodology of the selected ruleset. */
    EXPLAIN_TRADITION,

    /** Explaining the difference between calculation methods (e.g. Pythagorean vs Chaldean). */
    COMPARE_METHODS,

    /** In-depth explanation of the traditional symbolic archetype and psychological meaning. */
    EXPLAIN_INTERPRETATION,

    /** Clarifying the primary textual authority, author, publishing year, and source citation. */
    CLARIFY_SOURCE,

    /** Reflective, introspective, self-inquiry contemplation without fatalistic claims. */
    REFLECTIVE_QUESTION,

    /** High-level summary of the entire calculated profile. */
    REPORT_SUMMARY,

    /** Cross-tradition comparison when multiple verified results are present in context. */
    CROSS_TRADITION_COMPARISON,
}

/**
 * Safety and validation status of a generated or fallback explanation.
 */
@Serializable
enum class NumerologyAiSafetyStatus {
    /** Output passed all ruleset fidelity, number preservation, and safety guardrails. */
    VALIDATED,

    /** Output was rejected by safety validator and replaced by deterministic fallback. */
    FALLBACK_APPLIED,

    /** Engine operated directly in deterministic fallback mode (e.g., model uninstalled). */
    DETERMINISTIC_DIRECT,
}

/**
 * High-level AI capability contract for Numerology.
 */
@Serializable
data class NumerologyAiCapability(
    val supportedRulesets: Set<String> = NumerologyRuleset.ALL_RULESETS.map { it.id }.toSet(),
    val supportedQuestionTypes: Set<NumerologyAiQuestionCategory> = NumerologyAiQuestionCategory.values()
        .toSet(),
    val supportedLocales: Set<String> = setOf(
        "en",
        "hi",
        "ar",
        "bn",
        "gu",
        "mr",
        "pa",
        "ta",
        "te",
        "kn",
        "ml"
    ),
    val minimumModelCapability: String = "0.5B-Instruct",
    val offlineOnly: Boolean = true,
    val requiresEvidenceGraph: Boolean = true,
    val requiresInterpretationPackage: Boolean = true,
)

/**
 * Immutable structured grounding context passed to the on-device SLM or fallback generator.
 *
 * Invariant: Contains ONLY verified, source-backed facts from [NumerologyResult] and [NumerologyInterpretationBundle].
 * The SLM never has access to room databases, DAOs, or network services.
 */
@Serializable
data class NumerologyAiGroundingContext(
    val rulesetId: String,
    val rulesetName: String,
    val rulesetVersion: String,
    val primarySourceReference: String,
    val birthDateDisplay: String,
    val fullNameNormalized: String?,
    val calculatedValues: Map<String, String>,
    val calculationTraces: Map<String, String>,
    val primaryInterpretation: NumerologyInterpretation?,
    val secondaryInterpretations: List<NumerologyInterpretation> = emptyList(),
    val sourceReferences: List<String> = emptyList(),
    val isPersonalityInterpretation: Boolean = true,
    val nonPersonalityNoticeKey: String? = null,
    val evidenceItems: List<AiEvidence> = emptyList(),
    val userQuestion: String? = null,
    val questionCategory: NumerologyAiQuestionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
    val requestedLocale: String = "en",
    val comparisonProfiles: Map<String, Map<String, String>> = emptyMap(),
    val comparisonInterpretations: Map<String, List<NumerologyInterpretation>> = emptyMap(),
    val privacyClass: AiPrivacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
)

/**
 * Validated, typed response from the Numerology AI explanation layer.
 */
@Serializable
data class NumerologyAiResponse(
    val answer: String,
    val questionCategory: NumerologyAiQuestionCategory,
    val referencedRulesetId: String,
    val referencedEvidenceIds: List<String> = emptyList(),
    val citedSources: List<String> = emptyList(),
    val locale: String,
    val safetyStatus: NumerologyAiSafetyStatus,
    val isFallback: Boolean = false,
    val fallbackReason: String? = null,
    val modelMetadata: String? = null,
    val isOfflineExecution: Boolean = true,
)

/**
 * A single message in a Numerology AI conversational exchange.
 */
@Serializable
data class NumerologyChatMessage(
    val messageId: String,
    val isUser: Boolean,
    val text: String,
    val timestampEpochMs: Long,
    val questionCategory: NumerologyAiQuestionCategory? = null,
    val citedSources: List<String> = emptyList(),
    val referencedEvidenceIds: List<String> = emptyList(),
    val isFallback: Boolean = false,
)

/**
 * Bounded conversation state for interactive follow-up questions within a session.
 */
@Serializable
data class NumerologyConversationState(
    val activeResultId: String,
    val rulesetId: String,
    val conversationId: String,
    val messages: List<NumerologyChatMessage> = emptyList(),
    val relevantEvidenceIds: Set<String> = emptySet(),
    val locale: String = "en",
    val lastQuestion: String? = null,
    val lastAnswer: String? = null,
    val clarificationCount: Int = 0,
    val maxRetainedMessages: Int = 10,
) {
    /**
     * Appends a message while maintaining strict memory bounds.
     */
    fun withMessage(msg: NumerologyChatMessage): NumerologyConversationState {
        val updated = (messages + msg).takeLast(maxRetainedMessages)
        val updatedEvidence = relevantEvidenceIds + msg.referencedEvidenceIds
        return copy(
            messages = updated,
            relevantEvidenceIds = updatedEvidence,
            lastQuestion = if (msg.isUser) msg.text else lastQuestion,
            lastAnswer = if (!msg.isUser) msg.text else lastAnswer,
        )
    }
}

/**
 * Contract for executing Numerology AI explanation requests.
 */
interface NumerologyExplanationEngine {
    suspend fun explain(context: NumerologyAiGroundingContext): AynvoraResult<NumerologyAiResponse>
}

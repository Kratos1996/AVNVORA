package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceItem
import kotlinx.serialization.Serializable

/**
 * High-level mode of response synthesis.
 */
@Serializable
enum class AynvoraResponseMode {
    /** Reflective synthesis connecting source wisdom to user context without fatalistic claims. */
    REFLECTIVE,

    /** Direct technical or textual explanation grounded strictly in evidence. */
    DIRECT_EXPLANATION,

    /** Controlled multi-domain reflection distinguishing separate traditions clearly. */
    CROSS_FEATURE_REFLECTION,
}

/**
 * Outcome status of output safety and schema validation.
 */
@Serializable
enum class AynvoraValidationStatus {
    /** Output passed all safety, numeric, and source preservation checks. */
    VALIDATED,

    /** Output violated safety constraints or corrupted calculations; rejected. */
    REJECTED,

    /** Output was rejected or unavailable; deterministic fallback was transparently applied. */
    FALLBACK_APPLIED,
}

/**
 * Verified provenance and attribution for a scripture, classical treatise, or ruleset.
 */
@Serializable
data class AynvoraSourceReference(
    val sourceName: String,
    val rulesetOrEdition: String,
    val canonicalReference: String? = null,
    val authorOrTranslator: String? = null,
    val provenancePolicy: String? = null,
)

/**
 * Safety and factual constraints enforced on AI synthesis.
 */
@Serializable
enum class AynvoraSafetyConstraint {
    NO_FATALISTIC_CERTAINTY,
    NO_MEDICAL_DIAGNOSIS,
    NO_FINANCIAL_GUARANTEE,
    NO_INVENTED_CITATIONS,
    NO_INVENTED_SCRIPTURE,
    NO_UNSUPPORTED_SCIENTIFIC_CLAIMS,
    NO_CROSS_FEATURE_LEAKAGE,
    STRICT_SOURCE_GROUNDING,
    PRESERVE_NUMERICAL_FACTS,
    NON_AUTHORITATIVE_REFLECTIVE_FRAMING,
}

/**
 * Explicit user context provided directly by the user.
 *
 * PRIVACY GUARANTEE:
 * This model contains ONLY information explicitly provided or selected by the user.
 * The system NEVER infers mental state, health conditions, financial conditions,
 * personality diagnoses, or hidden personal facts.
 */
@Serializable
data class AynvoraUserContext(
    val question: String,
    val statedGoal: String? = null,
    val statedSituation: String? = null,
    val selectedAreaOfReflection: String? = null,
    val explicitlyProvidedContext: Map<String, String> = emptyMap(),
)

/**
 * Verified interpretation record from an approved domain knowledge catalog.
 */
@Serializable
data class AynvoraInterpretationRecord(
    val recordId: String,
    val domain: CoreFeatureId,
    val primarySymbolOrKey: String,
    val contextCondition: String? = null,
    val verifiedMeaning: String,
    val sourceReference: AynvoraSourceReference,
)

/**
 * Canonical rule or aphorism belonging to a knowledge pack.
 */
@Serializable
data class AynvoraKnowledgeRule(
    val ruleId: String,
    val domain: CoreFeatureId,
    val description: String,
    val sourceReference: AynvoraSourceReference,
    val priority: Int = 0,
)

/**
 * Unified request contract for all feature AI interactions.
 */
@Serializable
data class AynvoraAiRequest(
    val requestId: String,
    val featureId: CoreFeatureId,
    val knowledgePackId: String,
    val rulesetId: String? = null,
    val evidence: List<EvidenceItem> = emptyList(),
    val evidenceGraph: EvidenceGraph? = null,
    val userContext: AynvoraUserContext,
    val question: String,
    val maxTokens: Int = 512,
    val locale: String = "en",
    val responseMode: AynvoraResponseMode = AynvoraResponseMode.REFLECTIVE,
    val safetyConstraints: List<AynvoraSafetyConstraint> = listOf(
        AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY,
        AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS,
        AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE,
        AynvoraSafetyConstraint.NO_INVENTED_CITATIONS,
        AynvoraSafetyConstraint.NO_INVENTED_SCRIPTURE,
        AynvoraSafetyConstraint.NO_UNSUPPORTED_SCIENTIFIC_CLAIMS,
        AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE,
        AynvoraSafetyConstraint.STRICT_SOURCE_GROUNDING,
        AynvoraSafetyConstraint.PRESERVE_NUMERICAL_FACTS,
        AynvoraSafetyConstraint.NON_AUTHORITATIVE_REFLECTIVE_FRAMING,
    ),
    val contributingDomains: Set<CoreFeatureId> = setOf(featureId),
)

/**
 * Unified response contract returned from the local intelligence runtime.
 */
@Serializable
data class AynvoraAiResponse(
    val requestId: String,
    val responseText: String,
    val executionMode: AiExecutionMode,
    val featureId: CoreFeatureId,
    val knowledgePackId: String,
    val sourceReferences: List<AynvoraSourceReference> = emptyList(),
    val validationStatus: AynvoraValidationStatus = AynvoraValidationStatus.VALIDATED,
    val fallbackUsed: Boolean = false,
    val fallbackReason: String? = null,
    val latencyMs: Long = 0L,
    val modelMetadata: String? = null,
    val provenance: List<AiProvenance> = emptyList(),
    val reflectiveSynthesis: String? = null,
    val domainSections: Map<String, String> = emptyMap(),
    val generatedTokenCount: Int = 0,
    val promptDiagnostics: AynvoraPromptDiagnostics? = null,
)

package com.aynvora.core.intelligence

import com.aynvora.core.feature.CoreFeatureId
import kotlinx.serialization.Serializable

/**
 * Standard tradition profiles supported or planned in AYNVORA.
 * Traditions must remain explicit and never silently merged.
 */
@Serializable
enum class TraditionProfile {
    PARASHARA_CLASSICAL_V1,
    JAIMINI_UPADESHA_V1,
    KP_SYSTEM_V1,
    LAL_KITAB_CLASSICAL,
    TAROT_CONTEMPLATIVE,
    HASTREKHA_CLASSICAL,
    GITA_VEDANTA,
    GARUDA_PURAN_DHARMA,
}

/**
 * Status categorization for an orchestrated guidance or analysis statement.
 * Future-looking results remain traditional interpretations, never guaranteed facts.
 */
@Serializable
enum class GuidanceStatus {
    CALCULATED_FACT,
    TRADITIONAL_INTERPRETATION,
    FUTURE_TIMING_WINDOW,
    CONDITIONAL_GUIDANCE,
    INSUFFICIENT_DATA,
    CONFLICTING_RULES,
    UNAVAILABLE_CAPABILITY,
}

/**
 * Preservation status for multi-source/multi-rule evaluation.
 * Conflicting factors are preserved explicitly without silent suppression.
 */
@Serializable
enum class ConflictStatus {
    SUPPORTING,
    CONTRADICTING,
    MODIFIER,
    INCONCLUSIVE,
    INSUFFICIENT_DATA,
}

/**
 * High-level intent of an intelligence query.
 */
enum class IntelligenceIntentType {
    ASTROLOGY_CHART,
    GITA_PHILOSOPHICAL_REFLECTION,
    GEMSTONE_EVALUATION,
    PALMISTRY_FINDINGS,
    TAROT_CONTEMPLATION,
    DAILY_GUIDANCE_SYNTHESIS,
    WALLPAPER_GENERATION,
    CROSS_DOMAIN_SYNTHESIS,
    GENERAL_KNOWLEDGE,
}

/**
 * Temporal context for queries requiring timing or calendar alignment.
 */
data class TimeContext(
    val referenceTimestampEpochMs: Long,
    val timezoneId: String,
    val targetTimeRangeStartEpochMs: Long? = null,
    val targetTimeRangeEndEpochMs: Long? = null,
)

/**
 * User context sanitized for local intelligence processing.
 * Sensitive data remains strictly local.
 */
data class UserContext(
    val birthDateIso: String? = null,
    val birthTimeIso: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezoneId: String? = null,
    val selectedTradition: TraditionProfile = TraditionProfile.PARASHARA_CLASSICAL_V1,
    val activeGemstones: List<String> = emptyList(),
    val focusGoal: String? = null,
)

/**
 * Query context specifying parameters, intent, and time boundaries.
 */
data class QueryContext(
    val queryId: String,
    val intentType: IntelligenceIntentType,
    val requestedDomains: Set<CoreFeatureId>,
    val timeContext: TimeContext,
    val locale: String = "en",
)

/**
 * Typed top-level request sent to the Intelligence orchestration engine.
 */
data class IntelligenceRequest(
    val queryContext: QueryContext,
    val userContext: UserContext,
    val allowCrossDomainSynthesis: Boolean = true,
)

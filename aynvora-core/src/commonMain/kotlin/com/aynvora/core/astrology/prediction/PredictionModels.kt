package com.aynvora.core.astrology.prediction

import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.TraditionProfile
import kotlinx.serialization.Serializable

/**
 * Standard life topics for astrological analysis and prediction.
 * Strictly non-medical, non-deterministic life areas.
 */
@Serializable
enum class PredictionTopic(val displayName: String, val sanskritHouseReference: Int) {
    CAREER("Career & Profession", 10),
    BUSINESS("Business & Partnerships", 7),
    FINANCE("Wealth & Prosperity", 2),
    EDUCATION("Knowledge & Learning", 5),
    RELATIONSHIPS("Relationships & Marriage", 7),
    FAMILY("Family & Lineage", 4),
    WELLBEING("Vitality & Wellbeing", 1), // Non-medical vitality reflection
    TRAVEL("Travel & Pilgrimage", 9),
    PROPERTY("Real Estate & Assets", 4),
    SPIRITUALITY("Dharma & Spiritual Evolution", 9),
    DAILY_FOCUS("Daily Focus & Action", 1),
}

/**
 * Quality / confidence tier of the input birth time.
 */
@Serializable
enum class BirthTimePrecision {
    EXACT,        // Official birth certificate or recorded hospital time
    ROUNDED,      // Approximate to 5-15 minutes (e.g. 2:30, 2:45)
    APPROXIMATE,  // Approximate to 30-60 minutes (e.g. "morning", "around 3pm")
    UNKNOWN,      // Date known, time unknown (forces Moon-chart / Chandra Kundali fallback)
}

/**
 * Evaluation match status for a predictive astrological rule.
 */
@Serializable
enum class RuleEvaluationStatus {
    MATCHED,
    NOT_MATCHED,
    BLOCKED_BY_EXCEPTION,
    CANCELLED,
    INSUFFICIENT_DATA,
    CONFLICTING,
}

/**
 * Classical predictive or yoga rule definition.
 * Reference: Brihat Parashara Hora Shastra, Phaladeepika, Saravali.
 */
@Serializable
data class AstrologicalRule(
    val ruleId: String,
    val tradition: TraditionProfile = TraditionProfile.PARASHARA_CLASSICAL_V1,
    val topic: PredictionTopic,
    val sourceTitle: String,
    val sourceChapterOrVerse: String,
    val description: String,
    val priority: Int = 10,
)

/**
 * Match result when a rule is evaluated against natal, transit, and dasha factors.
 */
@Serializable
data class RuleMatchResult(
    val rule: AstrologicalRule,
    val status: RuleEvaluationStatus,
    val contributingFactors: List<String>,
    val exceptionsOrCancellations: List<String> = emptyList(),
    val resultingInterpretation: String,
)

/**
 * Continuous date window where astrological factors converge.
 */
@Serializable
data class TimingWindow(
    val windowId: String,
    val topic: PredictionTopic,
    val startJulianDay: Double,
    val endJulianDay: Double,
    val startIsoDate: String,
    val endIsoDate: String,
    val primaryMahaLord: String,
    val primaryAntarLord: String,
    val transitingTriggers: List<String>,
    val supportingRuleIds: List<String>,
    val confidenceGrade: String = "PRIMARY", // PRIMARY, SECONDARY, MODIFIER
)

/**
 * Complete structured prediction result.
 */
@Serializable
data class PredictionResult(
    val predictionId: String,
    val topic: PredictionTopic,
    val issuedTimestampEpochMs: Long,
    val birthTimePrecision: BirthTimePrecision,
    val ruleMatches: List<RuleMatchResult>,
    val timingWindows: List<TimingWindow>,
    val synthesisSummary: String,
    val evidenceGraph: EvidenceGraph,
)

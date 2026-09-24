package com.aynvora.core.analytics

import com.aynvora.core.garudapuran.GarudaPuranTopicId

private val BUILT_IN_REPORT_ANALYTICS_IDS = setOf(
    "kundali",
    "gemstone",
    "numerology",
    "rudraksha",
    "jadi",
    "yantra",
    "palmistry",
    "tarot",
    "gita",
    "lal_kitab",
    "garuda_puran",
    "daily_guidance",
)

private fun safeReportTypeId(id: String): String =
    id.takeIf { it in BUILT_IN_REPORT_ANALYTICS_IDS } ?: "custom"

private fun safeReportErrorCode(code: String): String =
    code.takeIf { it.matches(Regex("[a-z][a-z_]{0,63}")) } ?: "unknown_error"

/**
 * Sealed hierarchy of all analytics events emitted by AYNVORA.
 *
 * Rules:
 * - Events MUST NOT contain PII (name, birth date/time, coordinates, or raw chart results).
 * - Every event carries only the minimum context needed to measure feature adoption,
 *   error rates, and flow completion — never personal astrological data.
 * - Enum-style string constants keep event and param names stable across platform
 *   implementations and prevent ad-hoc magic strings at call sites.
 */
sealed class AnalyticsEvent(
    /** Stable snake_case event name sent to the analytics backend. */
    val name: String,
    /** Optional structured parameters — string, long, double values only. */
    val params: Map<String, Any> = emptyMap(),
) {

    // ──────────────────────────────────────────────────────────────────────────
    // App Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    /** Fired once when the app launches on a given session. */
    object AppOpened : AnalyticsEvent("app_opened")

    // Reports: stable type ids only. Never attach report text, birth data, or chart values.
    class ReportOpened(reportTypeId: String) :
        AnalyticsEvent("report_opened", mapOf("report_type" to safeReportTypeId(reportTypeId)))

    class ReportGenerated(reportTypeId: String) :
        AnalyticsEvent("report_generated", mapOf("report_type" to safeReportTypeId(reportTypeId)))

    class ReportPdfGenerated(reportTypeId: String) : AnalyticsEvent(
        "report_pdf_generated",
        mapOf("report_type" to safeReportTypeId(reportTypeId))
    )

    class ReportShared(reportTypeId: String) :
        AnalyticsEvent("report_shared", mapOf("report_type" to safeReportTypeId(reportTypeId)))

    class ReportPdfFailed(reportTypeId: String, errorCode: String) : AnalyticsEvent(
        "report_pdf_failed",
        mapOf(
            "report_type" to safeReportTypeId(reportTypeId),
            "error_code" to safeReportErrorCode(errorCode)
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Chart Calculation
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * User requested a natal chart calculation.
     * @param rulesetId The PARASHARA ruleset identifier (e.g. "PARASHARA_CLASSICAL_V1").
     */
    class ChartCalculationRequested(rulesetId: String) : AnalyticsEvent(
        name = "chart_calculation_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    /**
     * Natal chart calculation completed successfully.
     * @param rulesetId The PARASHARA ruleset identifier.
     * @param durationMs Wall-clock time in milliseconds (no personal data).
     */
    class ChartCalculationSucceeded(rulesetId: String, durationMs: Long) : AnalyticsEvent(
        name = "chart_calculation_succeeded",
        params = mapOf(
            Param.RULESET_ID to rulesetId,
            Param.DURATION_MS to durationMs,
        ),
    )

    /**
     * Natal chart calculation failed.
     * @param errorCode Structured error category — never a raw exception message.
     */
    class ChartCalculationFailed(errorCode: String) : AnalyticsEvent(
        name = "chart_calculation_failed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Divisional Charts
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * User requested one or more divisional charts.
     * @param chartDivision The divisional chart identifier (e.g. "D9", "D10").
     */
    class DivisionalChartRequested(chartDivision: String) : AnalyticsEvent(
        name = "divisional_chart_requested",
        params = mapOf(Param.CHART_DIVISION to chartDivision),
    )

    /**
     * Divisional chart calculation completed.
     * @param chartDivision The divisional chart identifier.
     * @param durationMs Wall-clock duration in milliseconds.
     */
    class DivisionalChartSucceeded(chartDivision: String, durationMs: Long) : AnalyticsEvent(
        name = "divisional_chart_succeeded",
        params = mapOf(
            Param.CHART_DIVISION to chartDivision,
            Param.DURATION_MS to durationMs,
        ),
    )

    /** Divisional chart calculation failed. */
    class DivisionalChartFailed(chartDivision: String, errorCode: String) : AnalyticsEvent(
        name = "divisional_chart_failed",
        params = mapOf(
            Param.CHART_DIVISION to chartDivision,
            Param.ERROR_CODE to errorCode,
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Shadbala
    // ──────────────────────────────────────────────────────────────────────────

    class ShadbalaCalculationRequested(rulesetId: String) : AnalyticsEvent(
        name = "shadbala_calculation_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class ShadbalaCalculationSucceeded(rulesetId: String, durationMs: Long) : AnalyticsEvent(
        name = "shadbala_calculation_succeeded",
        params = mapOf(
            Param.RULESET_ID to rulesetId,
            Param.DURATION_MS to durationMs,
        ),
    )

    class ShadbalaCalculationFailed(errorCode: String) : AnalyticsEvent(
        name = "shadbala_calculation_failed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Ashtakavarga
    // ──────────────────────────────────────────────────────────────────────────

    class AshtakavargaCalculationRequested(rulesetId: String) : AnalyticsEvent(
        name = "ashtakavarga_calculation_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class AshtakavargaCalculationSucceeded(rulesetId: String, durationMs: Long) : AnalyticsEvent(
        name = "ashtakavarga_calculation_succeeded",
        params = mapOf(
            Param.RULESET_ID to rulesetId,
            Param.DURATION_MS to durationMs,
        ),
    )

    class AshtakavargaCalculationFailed(errorCode: String) : AnalyticsEvent(
        name = "ashtakavarga_calculation_failed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Shodhana
    // ──────────────────────────────────────────────────────────────────────────

    class ShodhanaCalculationRequested(rulesetId: String) : AnalyticsEvent(
        name = "shodhana_calculation_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class ShodhanaCalculationSucceeded(rulesetId: String, durationMs: Long) : AnalyticsEvent(
        name = "shodhana_calculation_succeeded",
        params = mapOf(
            Param.RULESET_ID to rulesetId,
            Param.DURATION_MS to durationMs,
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Pinda
    // ──────────────────────────────────────────────────────────────────────────

    class PindaCalculationRequested(rulesetId: String) : AnalyticsEvent(
        name = "pinda_calculation_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class PindaCalculationSucceeded(rulesetId: String, durationMs: Long) : AnalyticsEvent(
        name = "pinda_calculation_succeeded",
        params = mapOf(
            Param.RULESET_ID to rulesetId,
            Param.DURATION_MS to durationMs,
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Birth Profile
    // ──────────────────────────────────────────────────────────────────────────

    /** User saved a birth profile (no birth data content — only the action). */
    object BirthProfileSaved : AnalyticsEvent("birth_profile_saved")

    /** User deleted a birth profile. */
    object BirthProfileDeleted : AnalyticsEvent("birth_profile_deleted")

    // ──────────────────────────────────────────────────────────────────────────
    // Saved Charts
    // ──────────────────────────────────────────────────────────────────────────

    /** User saved a calculated chart for later reference. */
    object ChartSaved : AnalyticsEvent("chart_saved")

    /** User deleted a saved chart. */
    object ChartDeleted : AnalyticsEvent("chart_deleted")

    // ──────────────────────────────────────────────────────────────────────────
    // Preferences
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * User changed a preference setting.
     * @param preferenceKey The name of the preference (e.g. "ayanamsa", "house_system").
     *   Must never include the value if it could be identifying.
     */
    class PreferenceChanged(preferenceKey: String) : AnalyticsEvent(
        name = "preference_changed",
        params = mapOf(Param.PREFERENCE_KEY to preferenceKey),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Theme
    // ──────────────────────────────────────────────────────────────────────────

    /** User toggled the app theme. */
    class ThemeToggled(isDark: Boolean) : AnalyticsEvent(
        name = "theme_toggled",
        params = mapOf(Param.IS_DARK to isDark.toString()),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // SDK / Engine
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Unhandled SDK error captured by the analytics boundary.
     * @param errorCode Sanitized error category — never raw stack trace or user data.
     */
    class SdkErrorObserved(errorCode: String) : AnalyticsEvent(
        name = "sdk_error_observed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Language / Localization
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * User changed the app language.
     * @param languageCode The selected locale ID (e.g. "en", "hi"). Not PII.
     */
    class LanguageChanged(languageCode: String) : AnalyticsEvent(
        name = "language_changed",
        params = mapOf(Param.LANGUAGE_CODE to languageCode),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Content / Knowledge Library
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * User opened a content item from a knowledge module.
     * @param moduleId The module domain (e.g. "ASTROLOGY"). Not user-specific.
     */
    class ContentItemOpened(moduleId: String) : AnalyticsEvent(
        name = "content_item_opened",
        params = mapOf(Param.MODULE_ID to moduleId),
    )

    /**
     * Offline content was accessed (network unavailable but local data served).
     * @param moduleId The content module that was accessed offline.
     */
    class OfflineContentAccessed(moduleId: String) : AnalyticsEvent(
        name = "offline_content_accessed",
        params = mapOf(Param.MODULE_ID to moduleId),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Content Sync
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Content synchronization cycle started.
     * @param reason "manual" or "automatic" — never a URL or user identifier.
     */
    class ContentSyncStarted(reason: String) : AnalyticsEvent(
        name = "content_sync_started",
        params = mapOf(Param.SYNC_REASON to reason),
    )

    /** Content synchronization completed successfully. */
    object ContentSyncCompleted : AnalyticsEvent("content_sync_completed")

    /**
     * Content synchronization failed.
     * @param errorCode Sanitized failure code — never a URL, server message, or user data.
     */
    class ContentSyncFailed(errorCode: String) : AnalyticsEvent(
        name = "content_sync_failed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    /**
     * SDK initialized successfully.
     * @param analyticsEnabled Whether analytics is currently enabled (consent granted).
     */
    class SdkInitialized(analyticsEnabled: Boolean) : AnalyticsEvent(
        name = "sdk_initialized",
        params = mapOf("analytics_enabled" to analyticsEnabled.toString()),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Tarot Feature Events
    // ──────────────────────────────────────────────────────────────────────────

    /** User entered the Tarot feature screen. */
    object TarotOpened : AnalyticsEvent("tarot_opened")

    /** User viewed the Tarot ethical reflection / non-predictive disclaimer. */
    object TarotDisclaimerViewed : AnalyticsEvent("tarot_disclaimer_viewed")

    /**
     * User selected a spread for a reading.
     * @param spreadId Spread identifier (e.g. "single_card", "three_card_timeline").
     */
    class TarotSpreadSelected(spreadId: String) : AnalyticsEvent(
        name = "tarot_spread_selected",
        params = mapOf(Param.SPREAD_ID to spreadId),
    )

    /**
     * Tarot reading draw started.
     * @param spreadId Spread identifier.
     * @param cardCount Total cards to be drawn.
     */
    class TarotReadingStarted(spreadId: String, cardCount: Int) : AnalyticsEvent(
        name = "tarot_reading_started",
        params = mapOf(
            Param.SPREAD_ID to spreadId,
            Param.CARD_COUNT to cardCount.toLong(),
        ),
    )

    /**
     * A single card was drawn.
     * @param spreadId Spread identifier.
     * @param cardId Identifier of drawn card (e.g. "major_00_fool").
     * @param orientation "UPRIGHT" or "REVERSED".
     */
    class TarotCardDrawn(
        spreadId: String,
        cardId: String,
        orientation: String,
    ) : AnalyticsEvent(
        name = "tarot_card_drawn",
        params = mapOf(
            Param.SPREAD_ID to spreadId,
            Param.CARD_ID to cardId,
            Param.ORIENTATION to orientation,
        ),
    )

    /**
     * Tarot reading completed successfully.
     * @param spreadId Spread identifier.
     * @param cardCount Total cards drawn.
     */
    class TarotReadingCompleted(spreadId: String, cardCount: Int) : AnalyticsEvent(
        name = "tarot_reading_completed",
        params = mapOf(
            Param.SPREAD_ID to spreadId,
            Param.CARD_COUNT to cardCount.toLong(),
        ),
    )

    /**
     * Tarot reading failed during execution.
     * @param spreadId Spread identifier.
     * @param errorCode Sanitized error code (no PII).
     */
    class TarotReadingFailed(spreadId: String, errorCode: String) : AnalyticsEvent(
        name = "tarot_reading_failed",
        params = mapOf(
            Param.SPREAD_ID to spreadId,
            Param.ERROR_CODE to errorCode,
        ),
    )

    /**
     * User inspected detailed card content / reflective meaning.
     * @param cardId Card identifier.
     * @param languageCode Locale code ("en", "hi").
     */
    class TarotContentOpened(cardId: String, languageCode: String) : AnalyticsEvent(
        name = "tarot_content_opened",
        params = mapOf(
            Param.CARD_ID to cardId,
            Param.LANGUAGE_CODE to languageCode,
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Core Product Features (Phase 7.4 Foundation)
    // ──────────────────────────────────────────────────────────────────────────

    /** Feature accessed from Navigation or Home dashboard. */
    class FeatureOpened(featureId: String) : AnalyticsEvent(
        name = "feature_opened",
        params = mapOf(Param.FEATURE_ID to featureId),
    )

    /** Palmistry session initiated. Zero image data sent. */
    class PalmScanStarted(handType: String) : AnalyticsEvent(
        name = "palm_scan_started",
        params = mapOf(Param.HAND_TYPE to handType),
    )

    /** Gemstone evaluation opened. Zero PII. */
    class GemstoneEvaluationStarted(gemstoneType: String) : AnalyticsEvent(
        name = "gemstone_evaluation_started",
        params = mapOf(Param.GEMSTONE_TYPE to gemstoneType),
    )

    /** Gita verse read. */
    class GitaVerseOpened(chapter: Int, verse: Int) : AnalyticsEvent(
        name = "gita_verse_opened",
        params = mapOf(Param.CHAPTER to chapter, Param.VERSE to verse),
    )

    /** Garuda Puran section viewed. */
    class GarudaContentOpened(chapter: Int) : AnalyticsEvent(
        name = "garuda_content_opened",
        params = mapOf(Param.CHAPTER to chapter),
    )

    /** Feature usage only; source text, references, and user-entered content are excluded. */
    object GarudaPuranOpened : AnalyticsEvent("garuda_puran_opened")

    class GarudaPuranTopicOpened(topicId: GarudaPuranTopicId) : AnalyticsEvent(
        name = "garuda_puran_topic_opened",
        params = mapOf("topic_id" to topicId.wireId),
    )

    object GarudaPuranReportGenerated : AnalyticsEvent("garuda_puran_report_generated")
    object GarudaPuranPdfGenerated : AnalyticsEvent("garuda_puran_pdf_generated")
    object GarudaPuranShared : AnalyticsEvent("garuda_puran_shared")

    /** Lal Kitab rule explored. */
    class LalKitabRuleOpened(planet: String, house: Int) : AnalyticsEvent(
        name = "lal_kitab_rule_opened",
        params = mapOf(Param.PLANET to planet, Param.HOUSE to house),
    )

    /** On-Device AI conversation session started. Zero chat content sent. */
    class AiChatStarted(modelId: String) : AnalyticsEvent(
        name = "ai_chat_started",
        params = mapOf(Param.MODEL_ID to modelId),
    )

    /** Structured tool called by AI assistant. Zero argument PII. */
    class AiToolUsed(toolName: String, isSuccess: Boolean) : AnalyticsEvent(
        name = "ai_tool_used",
        params = mapOf(Param.TOOL_NAME to toolName, Param.IS_SUCCESS to isSuccess),
    )

    /** Daily guidance viewed. */
    class DailyGuidanceOpened(timeOfDay: String) : AnalyticsEvent(
        name = "daily_guidance_opened",
        params = mapOf(Param.TIME_OF_DAY to timeOfDay),
    )

    /** Wallpaper prompt constructed. Zero user intention text sent. */
    class WallpaperPromptGenerated(theme: String, deviceProfile: String) : AnalyticsEvent(
        name = "wallpaper_prompt_generated",
        params = mapOf(Param.THEME to theme, Param.DEVICE_PROFILE to deviceProfile),
    )

    /** Wallpaper prompt shared or copied to external generator. */
    class WallpaperPromptShared(destination: String) : AnalyticsEvent(
        name = "wallpaper_prompt_shared",
        params = mapOf(Param.DESTINATION to destination),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Core Intelligence & Orchestration (Phase 7.5)
    // ──────────────────────────────────────────────────────────────────────────

    /** Intelligence query initiated. Zero question text or PII. */
    class IntelligenceQueryStarted(queryId: String, intentType: String, domainCount: Int) :
        AnalyticsEvent(
            name = "intelligence_query_started",
            params = mapOf(
                Param.QUERY_ID to queryId,
                Param.INTENT_TYPE to intentType,
                Param.DOMAIN_COUNT to domainCount.toLong(),
            ),
        )

    /** Specific domain tool selected by orchestrator or AI router. */
    class IntelligenceToolSelected(queryId: String, domain: String) : AnalyticsEvent(
        name = "intelligence_tool_selected",
        params = mapOf(
            Param.QUERY_ID to queryId,
            Param.DOMAIN to domain,
        ),
    )

    /** Intelligence query completed successfully. */
    class IntelligenceQueryCompleted(queryId: String, status: String, evidenceCount: Int) :
        AnalyticsEvent(
            name = "intelligence_query_completed",
            params = mapOf(
                Param.QUERY_ID to queryId,
                Param.STATUS to status,
                Param.EVIDENCE_COUNT to evidenceCount.toLong(),
            ),
        )

    /** Conflicting rules or traditions detected during multi-domain synthesis. */
    class ConflictingEvidenceDetected(queryId: String, conflictCount: Int) : AnalyticsEvent(
        name = "conflicting_evidence_detected",
        params = mapOf(
            Param.QUERY_ID to queryId,
            Param.CONFLICT_COUNT to conflictCount.toLong(),
        ),
    )

    /** Required data missing for one or more requested domains. Zero PII. */
    class InsufficientDataReturned(queryId: String, missingCount: Int) : AnalyticsEvent(
        name = "insufficient_data_returned",
        params = mapOf(
            Param.QUERY_ID to queryId,
            Param.MISSING_COUNT to missingCount.toLong(),
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Astrology Prediction & Timing Engine (Phase 8.0)
    // ──────────────────────────────────────────────────────────────────────────

    /** Astrology prediction requested for a topic. Zero personal birth data. */
    class PredictionRequested(predictionId: String, topic: String) : AnalyticsEvent(
        name = "prediction_requested",
        params = mapOf(
            Param.QUERY_ID to predictionId,
            Param.TOPIC to topic,
        ),
    )

    /** Astrology prediction completed successfully. Zero prediction text. */
    class PredictionCompleted(predictionId: String, topic: String, windowCount: Int) :
        AnalyticsEvent(
            name = "prediction_completed",
            params = mapOf(
                Param.QUERY_ID to predictionId,
                Param.TOPIC to topic,
                Param.EVIDENCE_COUNT to windowCount.toLong(),
            ),
        )

    /** Prediction evaluation failed. */
    class PredictionFailed(predictionId: String, errorCode: String) : AnalyticsEvent(
        name = "prediction_failed",
        params = mapOf(
            Param.QUERY_ID to predictionId,
            Param.ERROR_CODE to errorCode,
        ),
    )

    /** Timing window generated. Zero personal date content. */
    class TimingWindowGenerated(predictionId: String, topic: String, grade: String) :
        AnalyticsEvent(
            name = "timing_window_generated",
            params = mapOf(
                Param.QUERY_ID to predictionId,
                Param.TOPIC to topic,
                Param.STATUS to grade,
            ),
        )

    // ──────────────────────────────────────────────────────────────────────────
    // Stable parameter key constants
    // ──────────────────────────────────────────────────────────────────────────

    object Param {
        const val RULESET_ID = "ruleset_id"
        const val CHART_DIVISION = "chart_division"
        const val DURATION_MS = "duration_ms"
        const val ERROR_CODE = "error_code"
        const val PREFERENCE_KEY = "preference_key"
        const val IS_DARK = "is_dark"
        const val LANGUAGE_CODE = "language_code"
        const val MODULE_ID = "module_id"
        const val SYNC_REASON = "sync_reason"
        const val SPREAD_ID = "spread_id"
        const val CARD_COUNT = "card_count"
        const val CARD_ID = "card_id"
        const val ORIENTATION = "orientation"
        const val FEATURE_ID = "feature_id"
        const val HAND_TYPE = "hand_type"
        const val GEMSTONE_TYPE = "gemstone_type"
        const val CHAPTER = "chapter"
        const val VERSE = "verse"
        const val PLANET = "planet"
        const val HOUSE = "house"
        const val MODEL_ID = "model_id"
        const val TOOL_NAME = "tool_name"
        const val IS_SUCCESS = "is_success"
        const val TIME_OF_DAY = "time_of_day"
        const val THEME = "theme"
        const val DEVICE_PROFILE = "device_profile"
        const val DESTINATION = "destination"
        const val QUERY_ID = "query_id"
        const val INTENT_TYPE = "intent_type"
        const val DOMAIN_COUNT = "domain_count"
        const val DOMAIN = "domain"
        const val STATUS = "status"
        const val EVIDENCE_COUNT = "evidence_count"
        const val CONFLICT_COUNT = "conflict_count"
        const val MISSING_COUNT = "missing_count"
        const val TOPIC = "topic"

        // Phase 8.1 additions
        const val RULE_ID = "rule_id"
        const val MATCH_STATUS = "match_status"
        const val NUMEROLOGY_SYSTEM = "numerology_system"
        const val REPORT_TYPE = "report_type"
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Reference Validation (Engineering only — no user-facing tracking)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Engineering event: reference validation run completed.
     * Only emitted in debug/QA builds. MUST NOT fire in production.
     * @param referenceId The reference case identifier (e.g. "JKR-117480").
     * @param passCount Number of passing validations.
     * @param failCount Number of failing validations.
     */
    class ReferenceValidationRun(referenceId: String, passCount: Int, failCount: Int) :
        AnalyticsEvent(
            name = "reference_validation_run",
            params = mapOf(
                "reference_id" to referenceId,
                "pass_count" to passCount.toLong(),
                "fail_count" to failCount.toLong(),
            ),
        )

    /**
     * A classical prediction rule was evaluated.
     * @param ruleId The rule ID (e.g. "RULE_BPHS_YOGAKARAKA").
     * @param matchStatus The evaluation status enum name.
     */
    class PredictionRuleEvaluated(ruleId: String, matchStatus: String) : AnalyticsEvent(
        name = "prediction_rule_evaluated",
        params = mapOf(
            Param.RULE_ID to ruleId,
            Param.MATCH_STATUS to matchStatus,
        ),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Numerology Feature Events
    // ──────────────────────────────────────────────────────────────────────────

    /** User opened the Numerology feature. */
    object NumerologyOpened : AnalyticsEvent("numerology_opened")

    /**
     * User triggered a Numerology report.
     * @param system The numerology system used (e.g. "CHALDEAN", "PYTHAGOREAN").
     */
    class NumerologyReportGenerated(system: String) : AnalyticsEvent(
        name = "numerology_report_generated",
        params = mapOf(Param.NUMEROLOGY_SYSTEM to system),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Practice Domain Events (Rudraksha, Jadi, Yantra)
    // ──────────────────────────────────────────────────────────────────────────

    /** User opened the Rudraksha feature. */
    object RudrakshaOpened : AnalyticsEvent("rudraksha_opened")

    /** User opened the Jadi / Sacred Roots feature. */
    object JadiOpened : AnalyticsEvent("jadi_opened")

    /** User opened the Yantra feature. */
    object YantraOpened : AnalyticsEvent("yantra_opened")
}

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
) : AynvoraAnalyticsEvent {
    override val eventName: String get() = name
    override val parameters: Map<String, Any> get() = params

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
    class TarotReadingStarted(
        spreadId: String,
        cardCount: Int,
        language: String = "en",
    ) : AnalyticsEvent(
        name = "tarot_reading_started",
        params = mapOf(
            Param.SPREAD_ID to spreadId,
            Param.CARD_COUNT to cardCount.toLong(),
            Param.LANGUAGE_CODE to language,
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

    /** User opened the Tarot reading history screen. */
    object TarotHistoryOpened : AnalyticsEvent("tarot_history_opened")

    /** User opened the Tarot 78-card browser to browse all archetypes. */
    object TarotCardBrowserOpened : AnalyticsEvent("tarot_card_browser_opened")

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

    /** AI model download initiated. */
    class AiDownloadStarted(modelId: String) : AnalyticsEvent(
        name = "ai_download_started",
        params = mapOf(Param.MODEL_ID to modelId),
    )

    /** AI model download completed successfully. */
    class AiDownloadCompleted(modelId: String, durationMs: Long) : AnalyticsEvent(
        name = "ai_download_completed",
        params = mapOf(Param.MODEL_ID to modelId, Param.DURATION_MS to durationMs),
    )

    /** AI model download cancelled by user. */
    class AiDownloadCancelled(modelId: String) : AnalyticsEvent(
        name = "ai_download_cancelled",
        params = mapOf(Param.MODEL_ID to modelId),
    )

    /** AI model deleted by user from local storage. */
    class AiModelDeleted(modelId: String) : AnalyticsEvent(
        name = "ai_model_deleted",
        params = mapOf(Param.MODEL_ID to modelId),
    )

    /** AI model loaded into memory. */
    class AiModelLoaded(modelId: String, durationMs: Long) : AnalyticsEvent(
        name = "ai_model_loaded",
        params = mapOf(Param.MODEL_ID to modelId, Param.DURATION_MS to durationMs),
    )

    /** AI model load failed (e.g. insufficient RAM). */
    class AiModelLoadFailed(modelId: String, failureCode: String) : AnalyticsEvent(
        name = "ai_model_load_failed",
        params = mapOf(Param.MODEL_ID to modelId, Param.FAILURE_CODE to failureCode),
    )

    /** Local on-device inference initiated. Strictly zero prompt or user question sent. */
    class AiInferenceStarted(modelId: String, language: String) : AnalyticsEvent(
        name = "ai_inference_started",
        params = mapOf(Param.MODEL_ID to modelId, Param.LANGUAGE_CODE to language),
    )

    /** Local on-device inference completed. Strictly zero response or output sent. */
    class AiInferenceCompleted(modelId: String, durationBucket: String, tokensGenerated: Int) :
        AnalyticsEvent(
            name = "ai_inference_completed",
            params = mapOf(
                Param.MODEL_ID to modelId,
                Param.DURATION_BUCKET to durationBucket,
                Param.TOKENS_GENERATED to tokensGenerated.toLong(),
            ),
        )

    /** Local on-device inference failed (e.g. timeout, memory). */
    class AiInferenceFailed(modelId: String, failureCode: String) : AnalyticsEvent(
        name = "ai_inference_failed",
        params = mapOf(Param.MODEL_ID to modelId, Param.FAILURE_CODE to failureCode),
    )

    /** Deterministic fallback used when AI was unavailable or output invalid. */
    class AiFallbackUsed(modelId: String, fallbackReason: String) : AnalyticsEvent(
        name = "ai_fallback_used",
        params = mapOf(Param.MODEL_ID to modelId, Param.FALLBACK_REASON to fallbackReason),
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

        // Phase 8.7 additions (AI On-Device Lifecycle)
        const val FAILURE_CODE = "failure_code"
        const val FALLBACK_REASON = "fallback_reason"
        const val DURATION_BUCKET = "duration_bucket"
        const val TOKENS_GENERATED = "tokens_generated"

        // Phase 8.9 additions (Tarot Conversational Experience)
        const val READING_STATE = "reading_state"
        const val QUESTION_SEQ_NUM = "question_sequence_number"
        const val MODEL_VERSION = "model_version"
        const val PROMPT_VERSION = "prompt_version"
        const val FALLBACK_USED = "fallback_used"
        const val FEEDBACK_RATING = "feedback_rating"
        const val FEEDBACK_TYPE = "feedback_type"
        const val TIMELINE_EVENT_TYPE = "timeline_event_type"
        const val ANALYSIS_VERSION = "analysis_version"
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

    /**
     * Numerology calculation started.
     * @param rulesetId The numerology ruleset identifier.
     */
    class NumerologyCalculationStarted(rulesetId: String) : AnalyticsEvent(
        name = "numerology_calculation_started",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    /**
     * Numerology calculation completed successfully.
     * @param rulesetId The numerology ruleset identifier.
     */
    class NumerologyCalculationCompleted(rulesetId: String) : AnalyticsEvent(
        name = "numerology_calculation_completed",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    /**
     * Numerology calculation failed.
     * @param errorCode Sanitized error code (no PII).
     */
    class NumerologyCalculationFailed(errorCode: String) : AnalyticsEvent(
        name = "numerology_calculation_failed",
        params = mapOf(Param.ERROR_CODE to errorCode),
    )

    class NumerologyRulesetSelected(rulesetId: String) : AnalyticsEvent(
        name = "numerology_ruleset_selected",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class NumerologyResultViewed(rulesetId: String) : AnalyticsEvent(
        name = "numerology_result_viewed",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class NumerologyTraceViewed(rulesetId: String) : AnalyticsEvent(
        name = "numerology_trace_viewed",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    object NumerologyMethodCompared : AnalyticsEvent("numerology_method_compared")

    class NumerologyReportRequested(rulesetId: String) : AnalyticsEvent(
        name = "numerology_report_requested",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    object NumerologyHistoryOpened : AnalyticsEvent("numerology_history_opened")

    // ──────────────────────────────────────────────────────────────────────────
    // Numerology Conversational AI Events (Phase 10.6) - Zero PII
    // ──────────────────────────────────────────────────────────────────────────

    class NumerologyAiOpened(rulesetId: String) : AnalyticsEvent(
        name = "numerology_ai_opened",
        params = mapOf(Param.RULESET_ID to rulesetId),
    )

    class NumerologyAiQuestionSubmitted(rulesetId: String, category: String) : AnalyticsEvent(
        name = "numerology_ai_question_submitted",
        params = mapOf(Param.RULESET_ID to rulesetId, "question_category" to category),
    )

    class NumerologyAiExplanationStarted(rulesetId: String, category: String) : AnalyticsEvent(
        name = "numerology_ai_explanation_started",
        params = mapOf(Param.RULESET_ID to rulesetId, "question_category" to category),
    )

    class NumerologyAiExplanationCompleted(
        rulesetId: String,
        category: String,
        fallbackUsed: Boolean,
        modelId: String? = null,
    ) : AnalyticsEvent(
        name = "numerology_ai_explanation_completed",
        params = buildMap {
            put(Param.RULESET_ID, rulesetId)
            put("question_category", category)
            put("fallback_used", fallbackUsed.toString())
            if (modelId != null) put("model_id", modelId)
        },
    )

    class NumerologyAiExplanationFailed(rulesetId: String, errorCode: String) : AnalyticsEvent(
        name = "numerology_ai_explanation_failed",
        params = mapOf(Param.RULESET_ID to rulesetId, Param.ERROR_CODE to errorCode),
    )

    class NumerologyAiFallbackUsed(rulesetId: String, reason: String) : AnalyticsEvent(
        name = "numerology_ai_fallback_used",
        params = mapOf(Param.RULESET_ID to rulesetId, "fallback_reason" to reason),
    )

    class NumerologyAiClarificationRequested(rulesetId: String, category: String) : AnalyticsEvent(
        name = "numerology_ai_clarification_requested",
        params = mapOf(Param.RULESET_ID to rulesetId, "question_category" to category),
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

    // ──────────────────────────────────────────────────────────────────────────
    // Tarot Conversational Experience Events (Phase 8.9)
    // ──────────────────────────────────────────────────────────────────────────

    /** Reading lock dialog shown to user (one reading per 24 hours). */
    class TarotReadingLockShown(readingState: String) : AnalyticsEvent(
        name = "tarot_reading_lock_shown",
        params = mapOf(Param.READING_STATE to readingState),
    )

    /** User submitted a follow-up question in the active reading timeline. */
    class TarotQuestionSubmitted(sequenceNumber: Int, language: String) : AnalyticsEvent(
        name = "tarot_question_submitted",
        params = mapOf(
            Param.QUESTION_SEQ_NUM to sequenceNumber,
            Param.LANGUAGE_CODE to language,
        ),
    )

    /** AI generated an answer for a question. */
    class TarotAiAnswerGenerated(
        modelId: String?,
        modelVersion: String?,
        promptVersion: String,
        fallbackUsed: Boolean,
        language: String,
    ) : AnalyticsEvent(
        name = "tarot_ai_answer_generated",
        params = buildMap {
            modelId?.let { put(Param.MODEL_ID, it) }
            modelVersion?.let { put(Param.MODEL_VERSION, it) }
            put(Param.PROMPT_VERSION, promptVersion)
            put(Param.FALLBACK_USED, fallbackUsed)
            put(Param.LANGUAGE_CODE, language)
        },
    )

    /** AI answer generation failed. */
    class TarotAiAnswerFailed(errorCode: String, language: String) : AnalyticsEvent(
        name = "tarot_ai_answer_failed",
        params = mapOf(
            Param.ERROR_CODE to errorCode,
            Param.LANGUAGE_CODE to language,
        ),
    )

    /** AI recommended drawing a clarification card. */
    object TarotClarificationRecommended : AnalyticsEvent("tarot_clarification_recommended")

    /** User requested a clarification card. */
    object TarotClarificationRequested : AnalyticsEvent("tarot_clarification_requested")

    /** User accepted drawing a clarification card. */
    object TarotClarificationAccepted : AnalyticsEvent("tarot_clarification_accepted")

    /** User revealed drawn card. */
    object TarotCardRevealed : AnalyticsEvent("tarot_card_revealed")

    /** Clarification card drawn deterministically by TarotDrawEngine. */
    class TarotClarificationDrawn(cardId: String, orientation: String) : AnalyticsEvent(
        name = "tarot_clarification_drawn",
        params = mapOf(
            Param.CARD_ID to cardId,
            Param.ORIENTATION to orientation,
        ),
    )

    /** User marked reading as satisfied. */
    object TarotReadingSatisfied : AnalyticsEvent("tarot_reading_satisfied")

    /** Session-level star feedback submitted. */
    class TarotFeedbackSubmitted(starRating: Int, language: String) : AnalyticsEvent(
        name = "tarot_feedback_submitted",
        params = mapOf(
            Param.FEEDBACK_RATING to starRating,
            Param.LANGUAGE_CODE to language,
        ),
    )

    /** Answer-level feedback submitted. */
    class TarotAnswerFeedbackSubmitted(starRating: Int, language: String) : AnalyticsEvent(
        name = "tarot_answer_feedback_submitted",
        params = mapOf(
            Param.FEEDBACK_RATING to starRating,
            Param.LANGUAGE_CODE to language,
        ),
    )

    /** Card-level swipe/category feedback submitted. */
    class TarotCardFeedbackSubmitted(cardId: String, feedbackType: String) : AnalyticsEvent(
        name = "tarot_card_feedback_submitted",
        params = mapOf(
            Param.CARD_ID to cardId,
            Param.FEEDBACK_TYPE to feedbackType,
        ),
    )

    /** User switched language within Tarot experience. */
    class TarotLanguageChanged(language: String) : AnalyticsEvent(
        name = "tarot_language_changed",
        params = mapOf(Param.LANGUAGE_CODE to language),
    )

    /** User inspected a timeline event in history. */
    class TarotTimelineEventOpened(eventType: String) : AnalyticsEvent(
        name = "tarot_timeline_event_opened",
        params = mapOf(Param.TIMELINE_EVENT_TYPE to eventType),
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 8.10: Palmistry / Hastrekha Production Analytics (Strictly Zero PII)
    // ──────────────────────────────────────────────────────────────────────────

    /** Palmistry ethical disclaimer viewed. */
    object PalmistryDisclaimerViewed : AnalyticsEvent("palmistry_disclaimer_viewed")

    /** Palmistry experience entered. */
    object PalmistryOpened : AnalyticsEvent("palmistry_opened")

    /** User chose left or right hand. */
    class PalmistryHandSelected(hand: String) : AnalyticsEvent(
        name = "palmistry_hand_selected",
        params = mapOf(Param.HAND_TYPE to hand),
    )

    /** User captured or selected a palm image. Zero image data sent. */
    object PalmistryImageSelected : AnalyticsEvent("palmistry_image_selected")

    /** Local on-device palm analysis started. */
    class PalmistryAnalysisStarted(hand: String) : AnalyticsEvent(
        name = "palmistry_analysis_started",
        params = mapOf(Param.HAND_TYPE to hand),
    )

    /** Local on-device palm analysis succeeded. */
    class PalmistryAnalysisCompleted(hand: String, analysisVersion: String) : AnalyticsEvent(
        name = "palmistry_analysis_completed",
        params = mapOf(
            Param.HAND_TYPE to hand,
            Param.ANALYSIS_VERSION to analysisVersion,
        ),
    )

    /** Palm analysis failed or image quality insufficient. */
    class PalmistryAnalysisFailed(failureCode: String) : AnalyticsEvent(
        name = "palmistry_analysis_failed",
        params = mapOf(Param.FAILURE_CODE to failureCode),
    )

    /** Follow-up question submitted. Zero question text sent. */
    class PalmistryQuestionSubmitted(language: String) : AnalyticsEvent(
        name = "palmistry_question_submitted",
        params = mapOf(Param.LANGUAGE_CODE to language),
    )

    /** AI explanation generated for palmistry. */
    class PalmistryAiAnswerGenerated(modelId: String, fallbackUsed: Boolean) : AnalyticsEvent(
        name = "palmistry_ai_answer_generated",
        params = mapOf(
            Param.MODEL_ID to modelId,
            Param.FALLBACK_USED to fallbackUsed,
        ),
    )

    /** Deterministic fallback used for palm explanation. */
    class PalmistryAiFallbackUsed(reason: String) : AnalyticsEvent(
        name = "palmistry_ai_fallback_used",
        params = mapOf(Param.FALLBACK_REASON to reason),
    )

    /** User submitted 1-5 star feedback. */
    class PalmistryFeedbackSubmitted(stars: Int) : AnalyticsEvent(
        name = "palmistry_feedback_submitted",
        params = mapOf(Param.FEEDBACK_RATING to stars),
    )

    /** Palmistry report document produced. */
    class PalmistryReportGenerated(language: String) : AnalyticsEvent(
        name = "palmistry_report_generated",
        params = mapOf(Param.LANGUAGE_CODE to language),
    )

    /** Palmistry PDF export triggered. */
    object PalmistryPdfGenerated : AnalyticsEvent("palmistry_pdf_generated")

    /** User switched language within Palmistry experience. */
    class PalmistryLanguageChanged(language: String) : AnalyticsEvent(
        name = "palmistry_language_changed",
        params = mapOf(Param.LANGUAGE_CODE to language),
    )
}

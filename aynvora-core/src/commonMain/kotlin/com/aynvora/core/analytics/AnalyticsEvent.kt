package com.aynvora.core.analytics

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
    }
}

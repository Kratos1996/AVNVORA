package com.aynvora.contracts

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Universal identifiers for all core and extensible product feature engines in AYNVORA.
 * Language-neutral, machine-readable, canonical tokens.
 */
@Serializable
enum class AynvoraFeatureId {
    ASTROLOGY,
    PALMISTRY,
    GEMSTONE,
    NUMEROLOGY,
    RUDRAKSHA,
    JADI,
    YANTRA,
    GITA,
    GARUDA_PURAN,
    LAL_KITAB,
    TAROT,
    AI_ASSISTANT,
    DAILY_GUIDANCE,
    GUIDANCE,
    WALLPAPER,
    REPORT,
    UNKNOWN;

    companion object {
        fun fromString(value: String): AynvoraFeatureId = try {
            valueOf(value.uppercase())
        } catch (_: Exception) {
            UNKNOWN
        }
    }
}

/**
 * High-level operational status of an event processing cycle.
 */
@Serializable
enum class AynvoraStatus {
    SUCCESS,
    FEATURE_NOT_INCLUDED,
    FEATURE_DISABLED,
    FEATURE_LOCKED,
    FEATURE_NOT_READY,
    FEATURE_UNSUPPORTED,
    ENGINE_NOT_REGISTERED,
    INVALID_REQUEST,
    INVALID_JSON,
    SCHEMA_VERSION_UNSUPPORTED,
    ENGINE_ERROR,
    CALCULATION_FAILED,
    ANALYSIS_FAILED,
    AI_UNAVAILABLE,
    REPORT_FAILED,
    PERMISSION_REQUIRED,
    PLATFORM_UNSUPPORTED,
    INTERNAL_ERROR,
}

/**
 * Structured, language-neutral error representation.
 * Excludes arbitrary UI/presentation strings.
 */
@Serializable
data class AynvoraError(
    val code: AynvoraStatus,
    val messageKey: String,
    val details: String = "",
    val errorClass: String = "",
)

/**
 * Feature capabilities exposed by individual engines.
 */
@Serializable
data class AynvoraFeatureCapability(
    val capabilityId: String,
    val description: String,
    val isDeterministic: Boolean = true,
    val isOfflineFirst: Boolean = true,
)

/**
 * Provenance tracking across engine calculations and model executions.
 */
@Serializable
data class AynvoraProvenance(
    val source: String,
    val engineId: String,
    val engineVersion: String,
    val calculationVersion: String = "1.0.0",
    val modelVersion: String? = null,
    val datasetVersion: String? = null,
    val generatedAtEpochMs: Long,
    val isDeterministic: Boolean = true,
    val evidenceReferences: List<String> = emptyList(),
)

/**
 * Request metadata accompanying event dispatch.
 */
@Serializable
data class AynvoraRequestMetadata(
    val requestId: String,
    val correlationId: String = "",
    val timestampEpochMs: Long = 0L,
    val schemaVersion: String = "1.0",
    val callerId: String = "sdk",
    val platform: String = "kmp",
)

/**
 * Response metadata returned with event execution.
 */
@Serializable
data class AynvoraResponseMetadata(
    val requestId: String,
    val timestampEpochMs: Long = 0L,
    val schemaVersion: String = "1.0",
    val durationMs: Long = 0L,
    val provenance: AynvoraProvenance? = null,
)

/**
 * Privacy-safe analytics metadata emitted through event middleware.
 * Must NOT contain biometric vectors, image bytes, prompts, or PII.
 */
@Serializable
data class AynvoraAnalyticsMetadata(
    val eventId: String,
    val featureId: AynvoraFeatureId,
    val eventType: String,
    val durationMs: Long,
    val status: AynvoraStatus,
    val engineVersion: String,
    val platform: String,
    val isOffline: Boolean = true,
)

/**
 * Canonical JSON-first event payload wrapper for transport between Core, Engines, and SDK.
 */
@Serializable
data class AynvoraEvent(
    val eventId: String,
    val featureId: AynvoraFeatureId,
    val eventType: String,
    val version: String = "1.0.0",
    val schemaVersion: String = "1.0",
    val timestampEpochMs: Long,
    val requestId: String,
    val payloadJson: String = "{}",
    val metadata: AynvoraRequestMetadata = AynvoraRequestMetadata(requestId = requestId),
)

/**
 * Outward-facing event request format for raw headless SDK calls.
 */
@Serializable
data class AynvoraEventRequest(
    val featureId: AynvoraFeatureId,
    val eventType: String,
    val payloadJson: String = "{}",
    val requestId: String = "",
    val schemaVersion: String = "1.0",
    val metadata: AynvoraRequestMetadata = AynvoraRequestMetadata(requestId = requestId),
)

/**
 * Canonical JSON-first event response produced by feature engines.
 */
@Serializable
data class AynvoraEventResponse(
    val eventId: String,
    val requestId: String,
    val featureId: AynvoraFeatureId,
    val status: AynvoraStatus,
    val resultJson: String = "{}",
    val error: AynvoraError? = null,
    val metadata: AynvoraResponseMetadata = AynvoraResponseMetadata(requestId = requestId),
) {
    val isSuccess: Boolean get() = status == AynvoraStatus.SUCCESS

    companion object {
        fun success(
            eventId: String,
            requestId: String,
            featureId: AynvoraFeatureId,
            resultJson: String,
            durationMs: Long = 0L,
            provenance: AynvoraProvenance? = null,
        ): AynvoraEventResponse = AynvoraEventResponse(
            eventId = eventId,
            requestId = requestId,
            featureId = featureId,
            status = AynvoraStatus.SUCCESS,
            resultJson = resultJson,
            metadata = AynvoraResponseMetadata(
                requestId = requestId,
                timestampEpochMs = System.currentTimeMillis(),
                durationMs = durationMs,
                provenance = provenance,
            ),
        )

        fun failure(
            eventId: String,
            requestId: String,
            featureId: AynvoraFeatureId,
            status: AynvoraStatus,
            messageKey: String,
            details: String = "",
            durationMs: Long = 0L,
        ): AynvoraEventResponse = AynvoraEventResponse(
            eventId = eventId,
            requestId = requestId,
            featureId = featureId,
            status = status,
            resultJson = "{}",
            error = AynvoraError(
                code = status,
                messageKey = messageKey,
                details = details,
            ),
            metadata = AynvoraResponseMetadata(
                requestId = requestId,
                timestampEpochMs = System.currentTimeMillis(),
                durationMs = durationMs,
            ),
        )
    }
}

/**
 * Engine metadata registered with the central registry.
 */
@Serializable
data class AynvoraEngineMetadata(
    val engineId: String,
    val featureId: AynvoraFeatureId,
    val version: String,
    val capabilities: List<AynvoraFeatureCapability> = emptyList(),
    val isOfflineCapable: Boolean = true,
)

/**
 * Common Feature Engine Interface.
 * Every independent engine implements this interface to communicate exclusively through events.
 *
 * Engines must NOT import UI, Compose, ViewModel, Context, or another feature engine.
 */
interface AynvoraFeatureEngine {
    val featureId: AynvoraFeatureId
    val version: String
    val capabilities: Set<AynvoraFeatureCapability>

    suspend fun handle(event: AynvoraEvent): AynvoraEventResponse
}

/**
 * Formal operational lifecycle states for individual feature engines.
 */
@Serializable
enum class EngineLifecycleState {
    REGISTERED,
    READY,
    RUNNING,
    IDLE,
    RELEASED,
    FAILED,
}

/**
 * Factory provider for explicit and lazy initialization of a feature engine.
 * Allows consumers to package and instantiate only the engines they require.
 */
interface AynvoraEngineProvider {
    val featureId: AynvoraFeatureId
    fun create(): AynvoraFeatureEngine
}

/**
 * Inline helper to construct an [AynvoraEngineProvider] from a lambda factory.
 */
class LambdaEngineProvider(
    override val featureId: AynvoraFeatureId,
    private val factory: () -> AynvoraFeatureEngine,
) : AynvoraEngineProvider {
    override fun create(): AynvoraFeatureEngine = factory()
}

fun engineProvider(
    featureId: AynvoraFeatureId,
    factory: () -> AynvoraFeatureEngine,
): AynvoraEngineProvider = LambdaEngineProvider(featureId, factory)


/**
 * Universal canonical astronomical/planetary identifier shared across Jyotish, Gemstones, Rudraksha, and Guidance.
 */
@Serializable
enum class CelestialBody {
    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    RAHU,
    KETU,
}

/**
 * 12 Sidereal Zodiac Signs (Rashis), each spanning exactly 30 degrees.
 */
@Serializable
enum class Rashi(val index: Int, val displayName: String, val sanskritName: String) {
    ARIES(0, "Aries", "Mesha"),
    TAURUS(1, "Taurus", "Vrishabha"),
    GEMINI(2, "Gemini", "Mithuna"),
    CANCER(3, "Cancer", "Karka"),
    LEO(4, "Leo", "Simha"),
    VIRGO(5, "Virgo", "Kanya"),
    LIBRA(6, "Libra", "Tula"),
    SCORPIO(7, "Scorpio", "Vrishchika"),
    SAGITTARIUS(8, "Sagittarius", "Dhanu"),
    CAPRICORN(9, "Capricorn", "Makara"),
    AQUARIUS(10, "Aquarius", "Kumbha"),
    PISCES(11, "Pisces", "Meena");

    companion object {
        fun fromIndex(index: Int): Rashi = entries[index.coerceIn(0, 11)]
    }
}

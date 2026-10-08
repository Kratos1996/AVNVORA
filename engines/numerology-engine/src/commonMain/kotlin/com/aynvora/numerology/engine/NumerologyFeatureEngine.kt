package com.aynvora.numerology.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.numerology.NumerologyCalculationEngine
import com.aynvora.numerology.NumerologyRequest
import com.aynvora.numerology.NumerologyResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Independent feature engine implementation for Numerology / Ank Jyotish.
 * Encapsulates Chaldean, Pythagorean, Lo Shu, Gematria, Abjad, and Katapayadi calculations.
 * UI-independent, deterministic, and communicating solely via canonical JSON events.
 */
class NumerologyFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.NUMEROLOGY
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("CHALDEAN_NUMEROLOGY", "Chaldean Cheiro Moolank, Bhagyank, Namank calculations", true, true),
        AynvoraFeatureCapability("PYTHAGOREAN_NUMEROLOGY", "Western Pythagorean Life Path and Pinnacles", true, true),
        AynvoraFeatureCapability("LO_SHU_GRID", "Chinese Lo Shu 3x3 magic square arrows of strength/weakness", true, true),
        AynvoraFeatureCapability("TRADITIONAL_REGISTRIES", "Gematria, Abjad, Katapayadi, Nine Star Ki, Tarot Birth Card", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.numerology.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<NumerologyRequest>(event.payloadJson)
            val result = NumerologyCalculationEngine.calculate(req)
            when (result) {
                is AynvoraResult.Success -> {
                    val durationMs = System.currentTimeMillis() - startTime
                    val provenance = AynvoraProvenance(
                        source = "numerology-engine",
                        engineId = "com.aynvora.numerology",
                        engineVersion = version,
                        calculationVersion = "1.0.0",
                        generatedAtEpochMs = System.currentTimeMillis(),
                        isDeterministic = true,
                    )
                    AynvoraEventResponse.success(
                        eventId = event.eventId,
                        requestId = event.requestId,
                        featureId = featureId,
                        resultJson = json.encodeToString(result.value),
                        durationMs = durationMs,
                        provenance = provenance,
                    )
                }
                is AynvoraResult.Failure -> {
                    val durationMs = System.currentTimeMillis() - startTime
                    AynvoraEventResponse.failure(
                        eventId = event.eventId,
                        requestId = event.requestId,
                        featureId = featureId,
                        status = AynvoraStatus.CALCULATION_FAILED,
                        messageKey = "error.numerology.calculation_failed",
                        details = result.message,
                        durationMs = durationMs,
                    )
                }
            }
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.CALCULATION_FAILED,
                messageKey = "error.numerology.exception",
                details = e.message ?: "Unknown numerology error",
                durationMs = durationMs,
            )
        }
    }
}

package com.aynvora.jadi.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.jadi.JADI_DISCLAIMER
import com.aynvora.jadi.JadiPlanetAssociation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class JadiRequest(
    val action: String = "PLANETS",
    val planet: JadiPlanetAssociation? = null,
)

@Serializable
data class JadiResult(
    val action: String,
    val status: String = "FOUNDATION_ONLY",
    val disclaimer: String = JADI_DISCLAIMER,
)

/**
 * Independent feature engine for herbal roots (Jadi) domain contracts.
 * Free from UI and foreign domain dependencies.
 */
class JadiFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.JADI
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("JADI_CLASSIFICATION", "Vedic herbal roots domain contracts", true, true),
        AynvoraFeatureCapability("JADI_RECOMMENDATIONS", "Verified Atharva/Lal Kitab roots recommendations", false, false),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.jadi.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<JadiRequest>(event.payloadJson)
            val result = JadiResult(
                action = req.action,
                status = "FOUNDATION_ONLY",
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "jadi-engine",
                engineId = "com.aynvora.jadi",
                engineVersion = version,
                calculationVersion = "1.0.0",
                generatedAtEpochMs = System.currentTimeMillis(),
                isDeterministic = true,
            )

            AynvoraEventResponse.success(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                resultJson = json.encodeToString(result),
                durationMs = durationMs,
                provenance = provenance,
            )
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.CALCULATION_FAILED,
                messageKey = "error.jadi.retrieval_failed",
                details = e.message ?: "Unknown jadi error",
                durationMs = durationMs,
            )
        }
    }
}

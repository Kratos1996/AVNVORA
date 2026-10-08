package com.aynvora.rudraksha.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.rudraksha.RUDRAKSHA_DISCLAIMER
import com.aynvora.rudraksha.RudrakshaType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RudrakshaRequest(
    val action: String = "CATALOG",
    val mukhi: Int? = null,
)

@Serializable
data class RudrakshaResult(
    val action: String,
    val types: List<RudrakshaType> = emptyList(),
    val status: String = "FOUNDATION_ONLY",
    val disclaimer: String = RUDRAKSHA_DISCLAIMER,
)

/**
 * Independent feature engine for Rudraksha classification and tradition contracts.
 * Free from UI and foreign domain dependencies.
 */
class RudrakshaFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.RUDRAKSHA
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("RUDRAKSHA_CATALOG", "1 to 14 Mukhi canonical classification", true, true),
        AynvoraFeatureCapability("RUDRAKSHA_RECOMMENDATION", "Planetary weakness recommendations (Foundation)", false, false),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.rudraksha.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<RudrakshaRequest>(event.payloadJson)
            val result = RudrakshaResult(
                action = req.action,
                types = if (req.mukhi != null) {
                    RudrakshaType.entries.filter { it.mukhi == req.mukhi }
                } else {
                    RudrakshaType.entries
                },
                status = "FOUNDATION_ONLY",
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "rudraksha-engine",
                engineId = "com.aynvora.rudraksha",
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
                messageKey = "error.rudraksha.retrieval_failed",
                details = e.message ?: "Unknown rudraksha error",
                durationMs = durationMs,
            )
        }
    }
}

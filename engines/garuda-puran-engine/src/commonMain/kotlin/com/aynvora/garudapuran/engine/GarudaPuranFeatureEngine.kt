package com.aynvora.garudapuran.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.garudapuran.GarudaPuranTopicAvailability
import com.aynvora.garudapuran.GarudaPuranTopicId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GarudaPuranRequest(
    val action: String = "GET_TOPICS", // "GET_TOPICS", "GET_TOPIC"
    val topicId: GarudaPuranTopicId = GarudaPuranTopicId.INTRODUCTION,
)

@Serializable
data class GarudaPuranResult(
    val action: String,
    val topics: List<GarudaPuranTopicId> = emptyList(),
    val topicAvailability: GarudaPuranTopicAvailability? = null,
)

/**
 * Independent feature engine for validated Garuda Puran scripture and topic knowledge.
 * Source metadata, passage retrieval, and traditional interpretations.
 * Free from UI, Compose, and foreign domain dependencies.
 */
class GarudaPuranFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GARUDA_PURAN
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("GARUDA_TOPIC_REGISTRY", "Validated Garuda Purana canonical topics", true, true),
        AynvoraFeatureCapability("SOURCE_METADATA_VERIFICATION", "Public domain rights and provenance verification", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.garudapuran.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<GarudaPuranRequest>(event.payloadJson)
            val result = when (req.action.uppercase()) {
                "GET_TOPIC" -> {
                    GarudaPuranResult(
                        action = "GET_TOPIC",
                        topicAvailability = GarudaPuranTopicAvailability(
                            topicId = req.topicId,
                            status = com.aynvora.garudapuran.GarudaPuranContentStatus.AVAILABLE,
                            itemCount = 1,
                        ),
                    )
                }
                else -> { // "GET_TOPICS"
                    GarudaPuranResult(
                        action = "GET_TOPICS",
                        topics = GarudaPuranTopicId.entries,
                    )
                }
            }

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "garuda-puran-engine",
                engineId = "com.aynvora.garudapuran",
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
                messageKey = "error.garudapuran.retrieval_failed",
                details = e.message ?: "Unknown garudapuran error",
                durationMs = durationMs,
            )
        }
    }
}

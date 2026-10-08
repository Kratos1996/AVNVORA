package com.aynvora.yantra.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.yantra.YANTRA_DISCLAIMER
import com.aynvora.yantra.YantraType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class YantraRequest(
    val action: String = "CATALOG",
    val yantraType: YantraType? = null,
)

@Serializable
data class YantraResult(
    val action: String,
    val yantras: List<YantraType> = emptyList(),
    val status: String = "FOUNDATION_ONLY",
    val disclaimer: String = YANTRA_DISCLAIMER,
)

/**
 * Independent feature engine for sacred geometric diagrams (Yantra) domain contracts.
 * Free from UI, Compose, and foreign domain dependencies.
 */
class YantraFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.YANTRA
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("YANTRA_CATALOG", "13 classical Vedic and Tantric sacred yantras", true, true),
        AynvoraFeatureCapability("YANTRA_GEOMETRY_EVIDENCE", "Authentic geometry and tradition source citations", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.yantra.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<YantraRequest>(event.payloadJson)
            val result = YantraResult(
                action = req.action,
                yantras = if (req.yantraType != null) listOf(req.yantraType) else YantraType.entries,
                status = "FOUNDATION_ONLY",
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "yantra-engine",
                engineId = "com.aynvora.yantra",
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
                messageKey = "error.yantra.retrieval_failed",
                details = e.message ?: "Unknown yantra error",
                durationMs = durationMs,
            )
        }
    }
}

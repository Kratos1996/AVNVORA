package com.aynvora.tarot.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.tarot.DefaultTarotRandomSource
import com.aynvora.tarot.DeterministicTarotRandomSource
import com.aynvora.tarot.TarotDrawEngine
import com.aynvora.tarot.TarotReading
import com.aynvora.tarot.TarotSpread
import com.aynvora.tarot.TarotStandardDeck
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TarotRequest(
    val spreadId: String = "single_card",
    val allowReversed: Boolean = true,
    val seed: Long? = null,
    val timestampEpochMs: Long = 0L,
)

/**
 * Independent feature engine implementation for Tarot contemplation and spread drawing.
 * Manages full 78-card deck, standard spreads, and deterministic seedable random draws.
 * Free from UI, Compose, and foreign domain dependencies.
 */
class TarotFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.TAROT
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("TAROT_SPREAD_DRAW", "Deterministic and RNG spread draws without replacement", true, true),
        AynvoraFeatureCapability("STANDARD_RIDER_WAITE_DECK", "78-card canonical Rider-Waite-Smith deck catalog", true, true),
        AynvoraFeatureCapability("POSITIONAL_SPREAGS", "Single Card, Three Card, and Celtic Cross spreads", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.tarot.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<TarotRequest>(event.payloadJson)
            val spread = TarotSpread.StandardSpreads.find { it.id == req.spreadId }
                ?: TarotSpread.SingleCard

            val randomSource = if (req.seed != null) {
                DeterministicTarotRandomSource(req.seed)
            } else {
                DefaultTarotRandomSource()
            }

            val drawEngine = TarotDrawEngine(randomSource)
            val reading = drawEngine.drawSpread(
                spread = spread,
                deckCards = TarotStandardDeck.AllCards,
                allowReversed = req.allowReversed,
                timestampEpochMs = if (req.timestampEpochMs > 0) req.timestampEpochMs else System.currentTimeMillis(),
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "tarot-engine",
                engineId = "com.aynvora.tarot",
                engineVersion = version,
                calculationVersion = "1.0.0",
                generatedAtEpochMs = System.currentTimeMillis(),
                isDeterministic = req.seed != null,
            )

            AynvoraEventResponse.success(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                resultJson = json.encodeToString(reading),
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
                messageKey = "error.tarot.draw_failed",
                details = e.message ?: "Unknown tarot error",
                durationMs = durationMs,
            )
        }
    }
}

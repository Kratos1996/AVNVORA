package com.aynvora.guidance.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.guidance.DailyGuidance
import com.aynvora.guidance.DailyGuidanceContext
import com.aynvora.guidance.GuidanceActionItem
import com.aynvora.guidance.MorningRoutine
import com.aynvora.guidance.NightRoutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GuidanceRequest(
    val dateIso: String,
    val context: DailyGuidanceContext = DailyGuidanceContext(),
)

@Serializable
data class GuidanceResult(
    val dailyGuidance: DailyGuidance,
)

/**
 * Independent feature engine for daily mindfulness, sunrise routines, and contemplation guidance.
 * Free from UI wording, Compose, and foreign domain dependencies.
 */
class GuidanceFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false; encodeDefaults = true }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GUIDANCE
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("DAILY_GUIDANCE", "Sunrise morning and evening mindful guidance routines", true, true),
        AynvoraFeatureCapability("ACTION_TRACKING", "Practical mindful action recommendations", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId && event.featureId != AynvoraFeatureId.DAILY_GUIDANCE) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.guidance.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<GuidanceRequest>(event.payloadJson)
            val morning = MorningRoutine(
                title = "morning_reflection",
                recommendedTime = "06:00 - 08:00",
                suryaPracticeNote = "surya_namaskar_focus",
                focusTheme = "calm_clarity",
                gitaInspirationCitation = "BG 2.47",
                actionItems = listOf(
                    GuidanceActionItem(
                        id = "act_1",
                        title = "mindful_breath",
                        description = "practice_five_mindful_breaths",
                        category = "MINDFULNESS",
                    ),
                ),
            )
            val night = NightRoutine(
                title = "evening_gratitude",
                recommendedTime = "21:00 - 22:30",
                gratitudePrompt = "reflect_on_daily_gifts",
                relaxationTechnique = "gentle_mindfulness",
                reflectionVerseCitation = "BG 6.5",
            )
            val daily = DailyGuidance(
                dateIso = req.dateIso,
                morning = morning,
                daytimeActions = listOf(
                    GuidanceActionItem(
                        id = "act_mid",
                        title = "midday_pause",
                        description = "pause_and_recenter",
                        category = "FOCUS",
                    ),
                ),
                night = night,
                summaryQuote = "dharma_peace_summary",
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "guidance-engine",
                engineId = "com.aynvora.guidance",
                engineVersion = version,
                calculationVersion = "1.0.0",
                generatedAtEpochMs = System.currentTimeMillis(),
                isDeterministic = true,
            )

            AynvoraEventResponse.success(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                resultJson = json.encodeToString(GuidanceResult(daily)),
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
                messageKey = "error.guidance.generation_failed",
                details = e.message ?: "Unknown guidance error",
                durationMs = durationMs,
            )
        }
    }
}

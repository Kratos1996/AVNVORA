package com.aynvora.gemstone.engine

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.gemstone.GemstoneAstrologyProfile
import com.aynvora.gemstone.GemstoneCatalog
import com.aynvora.gemstone.GemstoneCompatibilityEngine
import com.aynvora.gemstone.GemstoneCompatibilityResult
import com.aynvora.gemstone.GemstoneDescriptor
import com.aynvora.gemstone.GemstoneRecommendationEngine
import com.aynvora.gemstone.GemstoneRecommendationPackage
import com.aynvora.gemstone.GemstoneType
import com.aynvora.gemstone.GemstoneWearingContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GemstoneRequest(
    val action: String = "RECOMMEND", // "RECOMMEND", "CATALOG", "COMPATIBILITY"
    val astroProfile: GemstoneAstrologyProfile? = null,
    val wearingContext: GemstoneWearingContext? = null,
    val candidateGemstone: GemstoneType? = null,
)

@Serializable
data class GemstoneResult(
    val action: String,
    val recommendations: GemstoneRecommendationPackage? = null,
    val catalog: List<GemstoneDescriptor>? = null,
    val compatibility: GemstoneCompatibilityResult? = null,
)

/**
 * Independent feature engine implementation for Vedic Ratna / Gemstones.
 * Owns Navaratna catalog, planetary associations, compatibility matrices, and ethical recommendations.
 * Free from UI, Compose, and foreign domain dependencies.
 */
class GemstoneFeatureEngine(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GEMSTONE
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("NAVARATNA_CATALOG", "Authoritative 9 Vedic gemstone properties and metals", true, true),
        AynvoraFeatureCapability("GEMSTONE_RECOMMENDATION", "Lagna, 5th, and 9th lord recommendations with contraindications", true, true),
        AynvoraFeatureCapability("PLANETARY_COMPATIBILITY", "Planetary friendship and inventory clash avoidance", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.gemstone.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<GemstoneRequest>(event.payloadJson)
            val result = when (req.action.uppercase()) {
                "CATALOG" -> {
                    GemstoneResult(
                        action = "CATALOG",
                        catalog = GemstoneCatalog.NAVARATNA,
                    )
                }
                "COMPATIBILITY" -> {
                    val candidate = req.candidateGemstone ?: GemstoneType.RUBY
                    val compat = GemstoneCompatibilityEngine.evaluate(
                        gemstoneType = candidate,
                        astroProfile = req.astroProfile,
                        wearingContext = req.wearingContext,
                    )
                    GemstoneResult(
                        action = "COMPATIBILITY",
                        compatibility = compat,
                    )
                }
                else -> { // "RECOMMEND"
                    if (req.astroProfile == null) {
                        return AynvoraEventResponse.failure(
                            eventId = event.eventId,
                            requestId = event.requestId,
                            featureId = featureId,
                            status = AynvoraStatus.INVALID_REQUEST,
                            messageKey = "error.gemstone.missing_astro_profile",
                            details = "Gemstone recommendations require an astroProfile",
                        )
                    }
                    val recs = GemstoneRecommendationEngine.generateRecommendations(
                        astroProfile = req.astroProfile,
                        wearingContext = req.wearingContext,
                    )
                    GemstoneResult(
                        action = "RECOMMEND",
                        recommendations = recs,
                    )
                }
            }

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "gemstone-engine",
                engineId = "com.aynvora.gemstone",
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
                messageKey = "error.gemstone.calculation_failed",
                details = e.message ?: "Unknown gemstone error",
                durationMs = durationMs,
            )
        }
    }
}

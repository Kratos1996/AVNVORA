package com.aynvora.sdk.clients

import com.aynvora.ai.engine.AiGroundingRequest
import com.aynvora.ai.engine.AiGroundingResult
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.astro.engine.AstrologyResult
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.core.orchestration.AynvoraEventRouter
import com.aynvora.garudapuran.engine.GarudaPuranRequest
import com.aynvora.garudapuran.engine.GarudaPuranResult
import com.aynvora.gemstone.engine.GemstoneRequest
import com.aynvora.gemstone.engine.GemstoneResult
import com.aynvora.gita.engine.GitaRequest
import com.aynvora.gita.engine.GitaResult
import com.aynvora.guidance.engine.GuidanceRequest
import com.aynvora.guidance.engine.GuidanceResult
import com.aynvora.jadi.engine.JadiRequest
import com.aynvora.jadi.engine.JadiResult
import com.aynvora.numerology.NumerologyRequest
import com.aynvora.numerology.NumerologyResult
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.palmistry.engine.PalmistryResult
import com.aynvora.report.engine.ReportGenerateRequest
import com.aynvora.report.engine.ReportResult
import com.aynvora.rudraksha.engine.RudrakshaRequest
import com.aynvora.rudraksha.engine.RudrakshaResult
import com.aynvora.tarot.TarotReading
import com.aynvora.tarot.engine.TarotRequest
import com.aynvora.yantra.engine.YantraRequest
import com.aynvora.yantra.engine.YantraResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal inline fun <reified T> dispatchAndDecode(
    router: AynvoraEventRouter,
    json: Json,
    featureId: AynvoraFeatureId,
    eventType: String,
    payload: Any,
): AynvoraResult<T> {
    return try {
        val payloadJson = json.encodeToString(payload)
        val request = AynvoraEventRequest(
            featureId = featureId,
            eventType = eventType,
            payloadJson = payloadJson,
        )
        // Note: dispatching synchronously via blocking or suspend
        // Since typed clients are suspendable, we delegate to executeTyped
        throw UnsupportedOperationException("Use executeTyped")
    } catch (e: Exception) {
        AynvoraResult.Failure.InternalError("SDK_CLIENT_ERROR", e.message ?: "Failed dispatch")
    }
}

internal suspend inline fun <reified T> executeTyped(
    router: AynvoraEventRouter,
    json: Json,
    featureId: AynvoraFeatureId,
    eventType: String,
    payloadJson: String,
): AynvoraResult<T> {
    val request = AynvoraEventRequest(
        featureId = featureId,
        eventType = eventType,
        payloadJson = payloadJson,
    )
    val response = router.route(request)
    return if (response.isSuccess) {
        try {
            val decoded = json.decodeFromString<T>(response.resultJson)
            AynvoraResult.Success(decoded, response.metadata)
        } catch (e: Exception) {
            AynvoraResult.Failure.InternalError("DECODING_ERROR", "Failed to decode response: ${e.message}")
        }
    } else {
        val errorMsg = response.error?.let { "${it.code.name}: ${it.messageKey} (${it.details})" }
            ?: "Request failed with status ${response.status}"
        when (response.status) {
            AynvoraStatus.FEATURE_DISABLED,
            AynvoraStatus.FEATURE_LOCKED,
            AynvoraStatus.FEATURE_NOT_READY,
            AynvoraStatus.FEATURE_UNSUPPORTED -> AynvoraResult.Failure.UnsupportedConfiguration(errorMsg)
            AynvoraStatus.INVALID_REQUEST,
            AynvoraStatus.INVALID_JSON -> AynvoraResult.Failure.InvalidInput("payload", errorMsg)
            else -> AynvoraResult.Failure.CalculationFailure(response.status.name, errorMsg)
        }
    }
}

class AynvoraAstrologyClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun calculate(request: AstrologyRequest): AynvoraResult<AstrologyResult> {
        return executeTyped(router, json, AynvoraFeatureId.ASTROLOGY, "CALCULATE_CHART", json.encodeToString(request))
    }
}

class AynvoraPalmistryClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun analyze(request: PalmistryRequest): AynvoraResult<PalmistryResult> {
        return executeTyped(router, json, AynvoraFeatureId.PALMISTRY, "ANALYZE_PALM", json.encodeToString(request))
    }
}

class AynvoraNumerologyClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun calculate(request: NumerologyRequest): AynvoraResult<NumerologyResult> {
        return executeTyped(router, json, AynvoraFeatureId.NUMEROLOGY, "CALCULATE_NUMEROLOGY", json.encodeToString(request))
    }
}

class AynvoraTarotClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun draw(request: TarotRequest): AynvoraResult<TarotReading> {
        return executeTyped(router, json, AynvoraFeatureId.TAROT, "DRAW_SPREAD", json.encodeToString(request))
    }
}

class AynvoraGemstoneClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun recommend(request: GemstoneRequest): AynvoraResult<GemstoneResult> {
        return executeTyped(router, json, AynvoraFeatureId.GEMSTONE, "RECOMMEND_GEMSTONES", json.encodeToString(request))
    }
}

class AynvoraGitaClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun getVerse(request: GitaRequest): AynvoraResult<GitaResult> {
        return executeTyped(router, json, AynvoraFeatureId.GITA, "GET_VERSE", json.encodeToString(request))
    }
}

class AynvoraGarudaPuranClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun getChapter(request: GarudaPuranRequest): AynvoraResult<GarudaPuranResult> {
        return executeTyped(router, json, AynvoraFeatureId.GARUDA_PURAN, "GET_CHAPTER", json.encodeToString(request))
    }
}

class AynvoraRudrakshaClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun lookup(request: RudrakshaRequest): AynvoraResult<RudrakshaResult> {
        return executeTyped(router, json, AynvoraFeatureId.RUDRAKSHA, "LOOKUP_RUDRAKSHA", json.encodeToString(request))
    }
}

class AynvoraJadiClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun lookup(request: JadiRequest): AynvoraResult<JadiResult> {
        return executeTyped(router, json, AynvoraFeatureId.JADI, "LOOKUP_JADI", json.encodeToString(request))
    }
}

class AynvoraYantraClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun lookup(request: YantraRequest): AynvoraResult<YantraResult> {
        return executeTyped(router, json, AynvoraFeatureId.YANTRA, "LOOKUP_YANTRA", json.encodeToString(request))
    }
}

class AynvoraGuidanceClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun getGuidance(request: GuidanceRequest): AynvoraResult<GuidanceResult> {
        return executeTyped(router, json, AynvoraFeatureId.DAILY_GUIDANCE, "GET_DAILY_GUIDANCE", json.encodeToString(request))
    }
}

class AynvoraAiClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun ask(request: AiGroundingRequest): AynvoraResult<AiGroundingResult> {
        return executeTyped(router, json, AynvoraFeatureId.AI_ASSISTANT, "GROUNDED_QUERY", json.encodeToString(request))
    }

    suspend fun ground(request: AiGroundingRequest): AynvoraResult<AiGroundingResult> = ask(request)
}

class AynvoraReportClient(private val router: AynvoraEventRouter, private val json: Json) {
    suspend fun generate(request: ReportGenerateRequest): AynvoraResult<ReportResult> {
        return executeTyped(router, json, AynvoraFeatureId.REPORT, "GENERATE_REPORT", json.encodeToString(request))
    }
}

package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.GroundedSlmTarotExplanationEngine
import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotExplanationRequest
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpreadPosition
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 8.7 Tarot Grounding, Authority Boundary & Output Validation Tests.
 */
class AiTarotAuthorityAndGroundingTest {

    private val hermitCard = TarotCard(
        id = "major_09_hermit",
        number = 9,
        name = "The Hermit",
        arcana = TarotArcana.MAJOR,
    )

    private val position = TarotSpreadPosition(
        id = "single_pos",
        orderIndex = 0,
        name = "Insight",
        description = "Core contemplation",
    )

    private val drawnCard = TarotCardDraw(
        card = hermitCard,
        orientation = TarotCardOrientation.UPRIGHT,
        position = position,
    )

    private val cardContent = TarotCardContent(
        cardId = hermitCard.id,
        language = "en",
        title = "The Hermit",
        shortDescription = "Quiet contemplation and seeking truth.",
        keywords = listOf("Introspection", "Solitude", "Inner Guidance"),
        uprightMeaning = "A period of quiet reflection and seeking inner truth.",
        reversedMeaning = "Isolation, loneliness, or withdrawal from the world.",
        contentVersion = 1,
    )

    private val testRequest = TarotExplanationRequest(
        requestId = "req_tarot_hermit",
        draw = drawnCard,
        spreadPositionContext = "Insight",
        deterministicContent = cardContent,
        language = "en",
        allowSlmInference = true,
    )

    @Test
    fun aiAuthorityBoundary_modelCannotChangeCardIdOrOrientationOrProvenance() = runBlocking {
        val engine = LocalAiInferenceEngine()
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        engine.load(variant, "/models/qwen05b.gguf")

        val tarotEngine = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = engine,
        )

        val result = tarotEngine.explain(testRequest)
        assertTrue(result is AynvoraResult.Success)
        val explanation = (result as AynvoraResult.Success).value

        // Invariant: Card ID and language remain immutable
        assertEquals(hermitCard.id, explanation.cardId)
        assertEquals("en", explanation.language)
        assertEquals(1, explanation.provenance.contentVersion)
        assertEquals("GroundedSlmTarotExplanationEngine", explanation.provenance.producedBy)
        assertFalse(explanation.fallbackUsed)
    }

    @Test
    fun outputValidator_rejectsFatalisticMedicalAndFinancialClaims() {
        val validator = AiOutputValidator()
        val context = AiContext(
            featureId = CoreFeatureId.TAROT,
            evidenceItems = emptyList(),
        )

        // 1. Fatalistic claim
        val fatalisticResult =
            validator.validateOutput("You will die in a tragic accident soon.", context)
        assertTrue(fatalisticResult is AynvoraResult.Failure)
        assertTrue((fatalisticResult as AynvoraResult.Failure.InternalFailure).message.contains("fatalistic"))

        // 2. Medical claim
        val medicalResult = validator.validateOutput(
            "This card shows you will cure cancer and can stop your medication.",
            context
        )
        assertTrue(medicalResult is AynvoraResult.Failure)
        assertTrue((medicalResult as AynvoraResult.Failure.InternalFailure).message.contains("medical"))

        // 3. Financial guarantee
        val financialResult = validator.validateOutput(
            "You have guaranteed wealth and will surely win lottery tomorrow.",
            context
        )
        assertTrue(financialResult is AynvoraResult.Failure)
        assertTrue((financialResult as AynvoraResult.Failure.InternalFailure).message.contains("financial"))

        // 4. Empty/blank output
        val emptyResult = validator.validateOutput("   ", context)
        assertTrue(emptyResult is AynvoraResult.Failure)

        // 5. Safe contemplative reflection
        val safeResult = validator.validateOutput(
            "The Hermit reflects a time of quiet seeking and honoring your inner wisdom.",
            context
        )
        assertTrue(safeResult is AynvoraResult.Success)
    }

    @Test
    fun fallbackTriggeredTransparently_whenValidationFails() = runBlocking {
        // Engine producing unsafe fatalistic output
        val unsafeEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Success(
                    AiGenerationResponse(
                        requestId = request.requestId,
                        text = "You will die soon and tragedy is certain.",
                        tokensGenerated = 10,
                        finishReason = "STOP",
                    )
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel() = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        }

        val tarotEngine = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = unsafeEngine,
        )

        val result = tarotEngine.explain(testRequest)
        assertTrue(result is AynvoraResult.Success)
        val explanation = (result as AynvoraResult.Success).value

        // Invariant: Unsafe output was rejected and transparently routed to deterministic fallback
        assertTrue(explanation.fallbackUsed)
        assertEquals("SLM_OUTPUT_FAILED_VALIDATION", explanation.provenance.fallbackReason)
        assertTrue(explanation.explanationText.contains(cardContent.uprightMeaning))
    }

    @Test
    fun fallbackTriggeredTransparently_whenModelNotReady() = runBlocking {
        val unreadyEngine = LocalAiInferenceEngine() // Status is UNLOADED

        val tarotEngine = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = unreadyEngine,
        )

        val result = tarotEngine.explain(testRequest)
        assertTrue(result is AynvoraResult.Success)
        val explanation = (result as AynvoraResult.Success).value

        assertTrue(explanation.fallbackUsed)
        assertEquals("SLM_NOT_READY", explanation.provenance.fallbackReason)
        assertTrue(explanation.explanationText.contains(cardContent.uprightMeaning))
    }

    @Test
    fun tarotDataConnector_createsEvidenceWithProvenanceAndDisclaimers() {
        val connector = TarotFeatureDataConnector()
        val reading = TarotReading(
            id = "reading_123",
            deckId = "rws_standard",
            spreadId = "single_card",
            draws = listOf(drawnCard),
            timestampEpochMs = 1714000000000L,
        )

        val evidence = connector.extractEvidence(reading)
        assertEquals(1, evidence.size)
        val item = evidence.first()
        assertEquals(CoreFeatureId.TAROT, item.featureId)
        assertEquals(EvidenceCategory.FACT, item.category)
        assertTrue(item.summaryText.contains("The Hermit [Upright]"))
        assertEquals(AiPrivacyClass.STRICT_LOCAL_ONLY, item.privacyClass)
        assertTrue(item.disclaimers.isNotEmpty())
    }
}

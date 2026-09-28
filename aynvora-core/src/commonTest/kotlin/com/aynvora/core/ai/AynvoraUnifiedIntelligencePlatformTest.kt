package com.aynvora.core.ai

import com.aynvora.core.ai.adapters.AstrologyAiAdapter
import com.aynvora.core.ai.adapters.CrossFeatureReflectionAdapter
import com.aynvora.core.ai.adapters.GarudaPuranAiAdapter
import com.aynvora.core.ai.adapters.GemstoneAiAdapter
import com.aynvora.core.ai.adapters.GitaAiAdapter
import com.aynvora.core.ai.adapters.NumerologyAiAdapter
import com.aynvora.core.ai.adapters.PalmistryAiAdapter
import com.aynvora.core.ai.adapters.TarotAiAdapter
import com.aynvora.core.ai.gita.GitaQueryIntent
import com.aynvora.core.ai.gita.GitaReflectionPipeline
import com.aynvora.core.ai.knowledge.GarudaPuranKnowledgePack
import com.aynvora.core.ai.knowledge.GemstoneKnowledgePack
import com.aynvora.core.ai.knowledge.GitaKnowledgePack
import com.aynvora.core.ai.knowledge.NumerologyKnowledgePack
import com.aynvora.core.ai.knowledge.PalmistryKnowledgePack
import com.aynvora.core.ai.knowledge.TarotKnowledgePack
import com.aynvora.core.ai.knowledge.VedicAstrologyKnowledgePack
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AynvoraUnifiedIntelligencePlatformTest {

    private fun createSimulationRuntime(): LocalAiRuntime {
        val simEngine = LocalSimulationEngine()
        return DefaultLocalAiRuntime(
            inferenceEngine = simEngine,
            nativeRuntimeDriver = null,
        )
    }

    private fun createReadyLocalIntelligence(): DefaultAynvoraLocalIntelligence {
        val runtime = createSimulationRuntime()
        val intelligence = DefaultAynvoraLocalIntelligence(
            runtime = runtime,
            outputValidator = AynvoraAiOutputValidator(),
        )
        runBlocking {
            runtime.loadModel(AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M, "/mock/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")
        }
        return intelligence
    }

    @Test
    fun testCentralizedRuntime_RegistersAllCanonicalKnowledgePacks() {
        val intelligence = DefaultAynvoraLocalIntelligence(runtime = createSimulationRuntime())
        val packs = intelligence.getAllKnowledgePacks()

        assertTrue(packs.any { it.featureId == CoreFeatureId.ASTROLOGY })
        assertTrue(packs.any { it.featureId == CoreFeatureId.GITA })
        assertTrue(packs.any { it.featureId == CoreFeatureId.TAROT })
        assertTrue(packs.any { it.featureId == CoreFeatureId.NUMEROLOGY })
        assertTrue(packs.any { it.featureId == CoreFeatureId.PALMISTRY })
        assertTrue(packs.any { it.featureId == CoreFeatureId.GEMSTONE })
        assertTrue(packs.any { it.featureId == CoreFeatureId.GARUDA_PURAN })
    }

    @Test
    fun testModelCatalog_ContainsCompleteModelMetadata() {
        val model = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        assertEquals("qwen2.5-0.5b-instruct-q4_k_m", model.modelId)
        assertEquals(AiModelFamily.QWEN_2_5, model.family)
        assertEquals("0.5B", model.parameterCount)
        assertEquals(AiQuantization.Q4_K_M, model.quantization)
        assertEquals("GGUF", model.format)
        assertEquals("1.0.0", model.version)
        assertEquals(491400032L, model.fileSizeBytes)
        assertEquals("74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db", model.sha256Checksum)
        assertTrue(model.supportedLanguages.contains("en"))
        assertTrue(model.supportedLanguages.contains("hi"))
        assertTrue(model.supportedAbis.contains("arm64-v8a"))
        assertTrue(model.minRamBytes > 0L)
        assertTrue(model.recommendedRamBytes >= model.minRamBytes)
    }

    @Test
    fun testGitaKnowledgePack_LightweightRetrievalSelectsRelevantVerses() {
        val gitaPack = GitaKnowledgePack()

        // Question about career and duty
        val retrieved = gitaPack.retrieveRelevantEvidence(
            question = "I feel stuck deciding between two career options and feel anxious about results.",
            userContext = AynvoraUserContext(
                question = "Career decision",
                statedSituation = "Stuck between two job offers",
                selectedAreaOfReflection = "CAREER",
            ),
        )

        assertTrue(retrieved.isNotEmpty())
        assertTrue(retrieved.size <= 3) // Retrieval bounds verse count
        assertTrue(retrieved.any { it.ruleId == "BG_2_47" || it.ruleId == "BG_3_35" })
    }

    @Test
    fun testGitaReflectionPipeline_ExecutesEndToEnd() {
        runBlocking {
            val intelligence = createReadyLocalIntelligence()
            val pipeline = GitaReflectionPipeline(intelligence)

            val userContext = AynvoraUserContext(
                question = "I feel discouraged about my work results.",
                statedSituation = "Working hard on a project with uncertain recognition.",
                selectedAreaOfReflection = "ACTION",
            )

            val result = pipeline.reflect(userContext)
            assertTrue(result is AynvoraResult.Success)

            val outcome = result.value
            assertEquals(GitaQueryIntent.ACTION_AND_RESULTS, outcome.queryIntent)
            assertTrue(outcome.selectedVerses.isNotEmpty())
            assertNotNull(outcome.reflectiveAnswer)
            assertEquals(CoreFeatureId.GITA, outcome.aiResponse.featureId)
        }
    }

    @Test
    fun testAllFeatureAdapters_DelegateToCentralizedIntelligence() {
        runBlocking {
            val intelligence = createReadyLocalIntelligence()

            val astroAdapter = AstrologyAiAdapter(intelligence)
            val gitaAdapter = GitaAiAdapter(intelligence)
            val tarotAdapter = TarotAiAdapter(intelligence)
            val numAdapter = NumerologyAiAdapter(intelligence)
            val palmAdapter = PalmistryAiAdapter(intelligence)
            val gemAdapter = GemstoneAiAdapter(intelligence)
            val garudaAdapter = GarudaPuranAiAdapter(intelligence)

            val userContext = AynvoraUserContext(question = "General reflection")

            // 1. Astrology
            val astroRes = astroAdapter.explainChart(
                question = "Lagna vitality",
                userContext = userContext,
                chartEvidence = listOf(
                    EvidenceItem(
                        evidenceId = "astro_ev_1",
                        domain = CoreFeatureId.ASTROLOGY,
                        category = EvidenceCategory.FACT,
                        summary = "Lagna is in Mesha at 12°",
                        provenance = EvidenceProvenance(CoreFeatureId.ASTROLOGY, "Astro", "Parashara", "1.0", null, 1000L),
                    )
                ),
            )
            assertTrue(astroRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.ASTROLOGY, astroRes.value.featureId)

            // 2. Gita
            val gitaRes = gitaAdapter.reflect("Duty reflection", userContext)
            assertTrue(gitaRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.GITA, gitaRes.value.featureId)

            // 3. Tarot
            val tarotRes = tarotAdapter.explainSpread("The Fool upright", userContext, emptyList())
            assertTrue(tarotRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.TAROT, tarotRes.value.featureId)

            // 4. Numerology
            val numRes = numAdapter.explainNumberProfile("Life Path 7", userContext, emptyList())
            assertTrue(numRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.NUMEROLOGY, numRes.value.featureId)

            // 5. Palmistry
            val palmRes = palmAdapter.explainLineObservations("Clear head line", userContext, emptyList())
            assertTrue(palmRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.PALMISTRY, palmRes.value.featureId)

            // 6. Gemstone
            val gemRes = gemAdapter.explainCompatibility("Ruby recommendation", userContext, emptyList())
            assertTrue(gemRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.GEMSTONE, gemRes.value.featureId)

            // 7. Garuda Puran
            val garudaRes = garudaAdapter.explainDharmaPassage("Dharma conduct", userContext, emptyList())
            assertTrue(garudaRes is AynvoraResult.Success)
            assertEquals(CoreFeatureId.GARUDA_PURAN, garudaRes.value.featureId)
        }
    }

    @Test
    fun testCrossFeatureReflection_DistinguishesDomainsWithoutConflation() {
        runBlocking {
            val intelligence = createReadyLocalIntelligence()
            val crossAdapter = CrossFeatureReflectionAdapter(intelligence)

            val astroEvidence = listOf(
                EvidenceItem(
                    evidenceId = "astro_1",
                    domain = CoreFeatureId.ASTROLOGY,
                    category = EvidenceCategory.FACT,
                    summary = "10th house has Saturn situated in Capricorn",
                    provenance = EvidenceProvenance(CoreFeatureId.ASTROLOGY, "Astro", "Parashara", "1.0", null, 1000L),
                )
            )

            val gitaEvidence = listOf(
                EvidenceItem(
                    evidenceId = "gita_1",
                    domain = CoreFeatureId.GITA,
                    category = EvidenceCategory.TRADITIONAL_RULE,
                    ruleId = "BG_2_47",
                    summary = "BG 2.47: Focus on duty without attachment to fruit",
                    provenance = EvidenceProvenance(CoreFeatureId.GITA, "Gita", "Corpus", "1.0", null, 1000L),
                )
            )

            val userContext = AynvoraUserContext(
                question = "How should I approach my current workplace struggles?",
                statedSituation = "High responsibility but delayed promotions.",
            )

            val result = crossAdapter.reflect(
                primaryFeature = CoreFeatureId.ASTROLOGY,
                secondaryFeature = CoreFeatureId.GITA,
                question = userContext.question,
                userContext = userContext,
                primaryEvidence = astroEvidence,
                secondaryEvidence = gitaEvidence,
            )

            assertTrue(result is AynvoraResult.Success)
            val response = result.value
            assertEquals(AynvoraValidationStatus.VALIDATED, response.validationStatus)
            assertNotNull(response.responseText)
        }
    }

    @Test
    fun testOutputValidator_CatchesSafetyViolations() {
        val validator = AynvoraAiOutputValidator()
        val request = AynvoraAiRequest(
            requestId = "req_1",
            featureId = CoreFeatureId.ASTROLOGY,
            knowledgePackId = "kp_astro",
            userContext = AynvoraUserContext("Will I be rich?"),
            question = "Will I be rich?",
        )

        // 1. Fatalistic certainty rejection
        val fatalOutput = "You are doomed and have inescapable curse that cannot change your fate."
        val fatalResult = validator.validate(fatalOutput, request)
        assertTrue(fatalResult is OutputValidationResult.Invalid)
        assertEquals(AynvoraSafetyConstraint.NO_FATALISTIC_CERTAINTY, fatalResult.violation)

        // 2. Medical diagnosis rejection
        val medOutput = "You have cancer and this gemstone cures your disease without medication."
        val medResult = validator.validate(medOutput, request)
        assertTrue(medResult is OutputValidationResult.Invalid)
        assertEquals(AynvoraSafetyConstraint.NO_MEDICAL_DIAGNOSIS, medResult.violation)

        // 3. Financial guarantee rejection
        val finOutput = "Buy this stock tomorrow, guaranteed profit and lottery winning numbers."
        val finResult = validator.validate(finOutput, request)
        assertTrue(finResult is OutputValidationResult.Invalid)
        assertEquals(AynvoraSafetyConstraint.NO_FINANCIAL_GUARANTEE, finResult.violation)

        // 4. False science rejection
        val sciOutput = "NASA officially confirmed that your planetary transit alters quantum wavelength."
        val sciResult = validator.validate(sciOutput, request)
        assertTrue(sciResult is OutputValidationResult.Invalid)
        assertEquals(AynvoraSafetyConstraint.NO_UNSUPPORTED_SCIENTIFIC_CLAIMS, sciResult.violation)

        // 5. Cross-feature leakage rejection
        val leakOutput = "Your birth chart shows you drew the Three of Cups tarot card and The Fool."
        val leakResult = validator.validate(leakOutput, request)
        assertTrue(leakResult is OutputValidationResult.Invalid)
        assertEquals(AynvoraSafetyConstraint.NO_CROSS_FEATURE_LEAKAGE, leakResult.violation)
    }

    @Test
    fun testFallback_TriggeredGracefullyWhenModelNotLoaded() {
        runBlocking {
            // Unloaded runtime
            val runtime = createSimulationRuntime()
            val intelligence = DefaultAynvoraLocalIntelligence(runtime = runtime)

            val request = AynvoraAiRequest(
                requestId = "fallback_req",
                featureId = CoreFeatureId.GITA,
                knowledgePackId = "kp_gita",
                userContext = AynvoraUserContext("Explain duty"),
                question = "Explain duty",
            )

            val result = intelligence.synthesize(request)
            assertTrue(result is AynvoraResult.Success)

            val response = result.value
            assertTrue(response.fallbackUsed)
            assertEquals(AiExecutionMode.DETERMINISTIC_FALLBACK, response.executionMode)
            assertEquals(AynvoraValidationStatus.FALLBACK_APPLIED, response.validationStatus)
            assertTrue(response.responseText.contains("BG 2.47") || response.responseText.contains("Gita"))
        }
    }

    @Test
    fun testMultilingualSupport_HindiRequestsProduceOrRequireDevanagari() {
        val validator = AynvoraAiOutputValidator()
        val request = AynvoraAiRequest(
            requestId = "hi_req",
            featureId = CoreFeatureId.NUMEROLOGY,
            knowledgePackId = "kp_num",
            userContext = AynvoraUserContext("कर्म और संख्या"),
            question = "कर्म और संख्या",
            locale = "hi",
        )

        // English-only text masquerading as Hindi
        val bogusHindi = "This is purely English text pretending to be Hindi guidance for the seeker."
        val validation = validator.validate(bogusHindi, request)
        assertTrue(validation is OutputValidationResult.Invalid)

        // Valid Devanagari text
        val validHindi = "यह विश्लेषण अंक ज्योतिष के सिद्धांतों पर आधारित है।"
        val validCheck = validator.validate(validHindi, request)
        assertTrue(validCheck is OutputValidationResult.Valid)
    }

    @Test
    fun testNativeRuntimeStatus_TruthfulReporting() {
        val runtime = createSimulationRuntime()
        assertFalse(runtime.isNativeVerified(), "When libllama.so is not linked, isNativeVerified must be false")
    }
}

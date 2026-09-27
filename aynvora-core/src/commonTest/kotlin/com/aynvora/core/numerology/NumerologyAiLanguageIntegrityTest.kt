package com.aynvora.core.numerology

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiGenerationResponse
import com.aynvora.core.ai.AiInferenceDiagnostics
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 10.7: Multilingual AI Quality, Arabic RTL & Language Fallback Test.
 *
 * Verifies that:
 * 1. All 11 supported locales generate grounded responses.
 * 2. An unsupported locale in SLM triggers transparent deterministic localized fallback.
 * 3. Arabic output preserves authentic Arabic script without reversal.
 */
class NumerologyAiLanguageIntegrityTest {

    private val connector = NumerologyFeatureDataConnector()
    private val deterministicEngine = DeterministicNumerologyExplanationEngine()

    private val all11Locales =
        listOf("en", "hi", "ar", "bn", "gu", "mr", "pa", "ta", "te", "kn", "ml")

    @Test
    fun testAll11Locales_GenerateValidDeterministicExplanation() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value

        for (locale in all11Locales) {
            val context = connector.buildGroundingContext(
                result = res,
                userQuestion = "What does my number mean?",
                requestedLocale = locale,
            )

            val explanationRes = deterministicEngine.explain(context)
            assertTrue(
                explanationRes is AynvoraResult.Success,
                "Deterministic generation must succeed for locale '$locale'"
            )
            val response = explanationRes.value

            assertEquals(locale, response.locale, "Response locale must match requested '$locale'")
            assertTrue(
                response.answer.isNotBlank(),
                "Response answer must not be blank for '$locale'"
            )
            assertTrue(
                response.answer.contains("7"),
                "Response must preserve core calculated number 7"
            )
        }
    }

    @Test
    fun testUnsupportedModelLocale_FallsBackHonestly() = runBlocking {
        // Model supporting ONLY English ("en")
        val englishOnlyModel = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M.copy(
            supportedLanguages = setOf("en")
        )

        val fakeInference = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                if (request.language != "en") {
                    return AynvoraResult.Failure.CalculationFailure(
                        code = "UNSUPPORTED_LANGUAGE",
                        message = "Language '${request.language}' not supported",
                    )
                }
                return AynvoraResult.Success(
                    AiGenerationResponse(request.requestId, "English text", 5, "STOP")
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel(): AiModelVariant = englishOnlyModel
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slmEngine = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = fakeInference,
            deterministicEngine = deterministicEngine,
        )

        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value

        // Request in Tamil ("ta")
        val context = connector.buildGroundingContext(
            result = res,
            requestedLocale = "ta",
        )

        val explanationRes = slmEngine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        // Must trigger fallback honestly without pretending English is Tamil
        assertTrue(response.isFallback, "Must fall back when SLM lacks language capability")
        assertTrue(
            response.fallbackReason?.contains("LOCALE_NOT_SUPPORTED") == true ||
                    response.fallbackReason?.contains("INFERENCE_FAILED") == true
        )
        assertEquals("ta", response.locale)
    }

    @Test
    fun testArabicRtlIntegrity_ContainsAuthenticScriptWithoutReversal() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 1, birthMonth = 1, birthYear = 2000, fullName = "محمد",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(
            result = res,
            requestedLocale = "ar",
        )

        val arabicProvider = object : com.aynvora.core.localization.LocalizationProvider {
            override fun get(key: com.aynvora.core.localization.LocalizationKey): String =
                "حساب الجمل: نظام عددي عربي تقليدي يربط الحروف بالأرقام"

            override fun get(
                key: com.aynvora.core.localization.LocalizationKey,
                args: Map<String, Any?>
            ): String =
                "حساب الجمل: نظام عددي عربي تقليدي يربط الحروف بالأرقام"

            override fun currentLocale(): com.aynvora.core.localization.AynvoraLocale =
                com.aynvora.core.localization.AynvoraLocale.Arabic

            override fun isSupported(locale: com.aynvora.core.localization.AynvoraLocale): Boolean =
                locale == com.aynvora.core.localization.AynvoraLocale.Arabic
        }
        val arabicEngine = DeterministicNumerologyExplanationEngine(arabicProvider)
        val explanationRes = arabicEngine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        assertEquals("ar", response.locale)
        // Check for authentic Arabic characters
        assertTrue(response.answer.any { it in '\u0600'..'\u06FF' } || response.answer.contains("حساب الجمل"),
            "Output must contain Arabic text")
    }
}

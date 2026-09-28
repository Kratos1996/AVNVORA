package com.aynvora.app

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiRuntimeErrorCode
import com.aynvora.core.ai.AynvoraAiOutputValidator
import com.aynvora.core.ai.AynvoraAiRequest
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.ai.AynvoraResponseMode
import com.aynvora.core.ai.AynvoraUserContext
import com.aynvora.core.ai.adapters.AstrologyAiAdapter
import com.aynvora.core.ai.adapters.CrossFeatureReflectionAdapter
import com.aynvora.core.ai.adapters.GarudaPuranAiAdapter
import com.aynvora.core.ai.adapters.GemstoneAiAdapter
import com.aynvora.core.ai.adapters.GitaAiAdapter
import com.aynvora.core.ai.adapters.NumerologyAiAdapter
import com.aynvora.core.ai.adapters.PalmistryAiAdapter
import com.aynvora.core.ai.adapters.TarotAiAdapter
import com.aynvora.core.ai.gita.GitaReflectionPipeline
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import java.io.File

/**
 * Autonomous Native On-Device AI Test Runner for Phase 10.12.
 *
 * Executes real inference on physical hardware (Samsung Galaxy S23 Ultra),
 * captures authentic hardware latency, token speed, and memory telemetry,
 * and persists results to JSON.
 */
class AynvoraNativeAiTestRunner(private val context: Context) {

    companion object {
        private const val TAG = "AynvoraAiTestResult"
    }

    private fun getMemoryMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val info = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(info)
        return info.availMem / (1024 * 1024)
    }

    suspend fun runTests(testFilter: String = "all"): String = withContext(Dispatchers.IO) {
        val koin = GlobalContext.get()
        val intelligence = koin.get<AynvoraLocalIntelligence>()
        val gitaPipeline = koin.get<GitaReflectionPipeline>()
        val astrologyAdapter = koin.get<AstrologyAiAdapter>()
        val gitaAdapter = koin.get<GitaAiAdapter>()
        val tarotAdapter = koin.get<TarotAiAdapter>()
        val numerologyAdapter = koin.get<NumerologyAiAdapter>()
        val palmistryAdapter = koin.get<PalmistryAiAdapter>()
        val gemstoneAdapter = koin.get<GemstoneAiAdapter>()
        val garudaAdapter = koin.get<GarudaPuranAiAdapter>()
        val crossAdapter = koin.get<CrossFeatureReflectionAdapter>()

        val reportObj = JSONObject()
        val resultsArray = JSONArray()

        val memBeforeLoadMb = getMemoryMb()
        reportObj.put("memBeforeLoadMb", memBeforeLoadMb)

        // 1. Ensure Model is Loaded in Native Runtime
        val loadStartTime = System.currentTimeMillis()
        if (!intelligence.isModelLoaded()) {
            val loadRes = intelligence.loadModel()
            Log.i(TAG, "Native model load result: $loadRes")
        }
        val loadDurationMs = System.currentTimeMillis() - loadStartTime
        val memAfterLoadMb = getMemoryMb()
        reportObj.put("loadDurationMs", loadDurationMs)
        reportObj.put("memAfterLoadMb", memAfterLoadMb)
        reportObj.put("nativeLibraryStatus", intelligence.getNativeLibraryStatus())
        reportObj.put("jniStatus", intelligence.getJniStatus())

        // ── TEST 1: SELF TEST (Very small prompt) ───────────────────────────
        if (testFilter == "all" || testFilter == "self_test") {
            val res = runSelfTest(intelligence)
            resultsArray.put(res)
        }

        // ── TEST 2: BHAGAVAD GITA (Step 13) ─────────────────────────────────
        if (testFilter == "all" || testFilter == "gita") {
            val res = runGitaTest(gitaPipeline)
            resultsArray.put(res)
        }

        // ── TEST 3: VEDIC ASTROLOGY (Step 14) ───────────────────────────────
        if (testFilter == "all" || testFilter == "astrology") {
            val res = runAstrologyTest(astrologyAdapter)
            resultsArray.put(res)
        }

        // ── TEST 4: TAROT (Step 14) ─────────────────────────────────────────
        if (testFilter == "all" || testFilter == "tarot") {
            val res = runTarotTest(tarotAdapter)
            resultsArray.put(res)
        }

        // ── TEST 5: NUMEROLOGY (Step 14) ────────────────────────────────────
        if (testFilter == "all" || testFilter == "numerology") {
            val res = runNumerologyTest(numerologyAdapter)
            resultsArray.put(res)
        }

        // ── TEST 6: PALMISTRY (Step 14) ─────────────────────────────────────
        if (testFilter == "all" || testFilter == "palmistry") {
            val res = runPalmistryTest(palmistryAdapter)
            resultsArray.put(res)
        }

        // ── TEST 7: GEMSTONE (Step 14) ──────────────────────────────────────
        if (testFilter == "all" || testFilter == "gemstone") {
            val res = runGemstoneTest(gemstoneAdapter)
            resultsArray.put(res)
        }

        // ── TEST 8: GARUDA PURAN (Step 14) ──────────────────────────────────
        if (testFilter == "all" || testFilter == "garuda") {
            val res = runGarudaTest(garudaAdapter)
            resultsArray.put(res)
        }

        // ── TEST 9: CROSS-FEATURE ASTROLOGY + GITA (Step 15) ────────────────
        if (testFilter == "all" || testFilter == "cross_feature") {
            val res = runCrossFeatureTest(crossAdapter)
            resultsArray.put(res)
        }

        val memAfterAllMb = getMemoryMb()
        reportObj.put("memAfterAllMb", memAfterAllMb)
        reportObj.put("finalExecutionMode", intelligence.getExecutionMode().name)
        reportObj.put("testResults", resultsArray)

        val reportJson = reportObj.toString(2)
        val outFile = File(context.filesDir, "native_ai_test_results.json")
        outFile.writeText(reportJson)
        Log.i(TAG, "All AI tests completed. Report written to: ${outFile.absolutePath}")
        Log.i(TAG, "Report JSON:\n$reportJson")

        reportJson
    }

    private suspend fun runSelfTest(intelligence: AynvoraLocalIntelligence): JSONObject {
        val testName = "SELF_TEST"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val req = AynvoraAiRequest(
            requestId = "selftest_${System.currentTimeMillis()}",
            featureId = CoreFeatureId.GITA,
            knowledgePackId = "kp_gita_canonical_v1",
            rulesetId = "CANONICAL_GITA_TRADITION",
            userContext = AynvoraUserContext(question = "Hello! State your contemplative purpose in 5 words:"),
            question = "Hello! State your contemplative purpose in 5 words:",
            locale = "en",
            responseMode = AynvoraResponseMode.REFLECTIVE,
        )

        val result = intelligence.synthesize(req)
        val duration = System.currentTimeMillis() - start
        val memAfter = getMemoryMb()

        return formatTestResult(testName, result, duration, memBefore, memAfter)
    }

    private suspend fun runGitaTest(pipeline: GitaReflectionPipeline): JSONObject {
        val testName = "BHAGAVAD_GITA_CAREER_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val userContext = AynvoraUserContext(
            question = "Can you help me reflect on this using the Bhagavad Gita?",
            userSituation = "I am confused about my career direction.",
        )

        val result = pipeline.reflect(userContext)
        val duration = System.currentTimeMillis() - start
        val memAfter = getMemoryMb()

        val obj = JSONObject()
        obj.put("testName", testName)
        obj.put("durationMs", duration)
        obj.put("memBeforeMb", memBefore)
        obj.put("memAfterMb", memAfter)

        when (result) {
            is AynvoraResult.Success -> {
                val value = result.value
                val aiRes = value.aiResponse
                val tokens = aiRes.responseText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                val tps = if (duration > 0) (tokens.toFloat() / (duration / 1000f)) else 0f
                obj.put("success", true)
                obj.put("executionMode", aiRes.executionMode.name)
                obj.put("validationStatus", aiRes.validationStatus.name)
                obj.put("fallbackUsed", aiRes.fallbackUsed)
                obj.put("selectedVerses", value.selectedVerses.map { "BG ${it.chapter}.${it.verse}" }.joinToString())
                obj.put("tokenCount", tokens)
                obj.put("tokensPerSec", tps)
                obj.put("responseText", aiRes.responseText)
                Log.i(TAG, "[$testName] SUCCESS: mode=${aiRes.executionMode.name}, tokens=$tokens, speed=${"%.2f".format(tps)} t/s")
                Log.i(TAG, "[$testName] Text: ${aiRes.responseText}")
            }
            is AynvoraResult.Failure -> {
                obj.put("success", false)
                obj.put("error", result.message)
                Log.e(TAG, "[$testName] FAILED: ${result.message}")
            }
        }
        return obj
    }

    private suspend fun runAstrologyTest(adapter: AstrologyAiAdapter): JSONObject {
        val testName = "VEDIC_ASTROLOGY_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "astro_saturn_10",
                domain = CoreFeatureId.ASTROLOGY,
                category = EvidenceCategory.CALCULATED_POSITION,
                ruleId = "PLANET_SATURN_HOUSE_10",
                summary = "Saturn is placed in the 10th House (Karma Bhava) in Capricorn (own sign).",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.ASTROLOGY,
                    sourceName = "Brihat Parasara Hora Sastra",
                    rulesetOrEdition = "Parasari Principles Ch. 24",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "BPHS_10_SATURN",
                ),
            )
        )

        val result = adapter.explainChart(
            question = "What is the reflective guidance for Saturn in the 10th house?",
            userContext = AynvoraUserContext(question = "Reflect on Saturn in 10th house"),
            chartEvidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runTarotTest(adapter: TarotAiAdapter): JSONObject {
        val testName = "TAROT_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "tarot_hermit_upright",
                domain = CoreFeatureId.TAROT,
                category = EvidenceCategory.TRADITIONAL_RULE,
                ruleId = "TAROT_MAJOR_09_HERMIT",
                summary = "The Hermit (IX) Upright: Soul-searching, introspection, inner guidance, solitude.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.TAROT,
                    sourceName = "RWS Archetypal Tradition",
                    rulesetOrEdition = "Pictorial Key to the Tarot",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "RWS_09_HERMIT",
                ),
            )
        )

        val result = adapter.synthesizeExplanation(
            question = "Reflect on The Hermit card for introspective guidance.",
            userContext = AynvoraUserContext(question = "Introspective guidance"),
            evidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runNumerologyTest(adapter: NumerologyAiAdapter): JSONObject {
        val testName = "NUMEROLOGY_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "num_lifepath_7",
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.MATHEMATICAL_RESULT,
                ruleId = "NUM_LP_07",
                summary = "Life Path Number is 7 (derived from 1996-07-11: 1+1+7+1+9+9+6 = 34 -> 7). Analytical, seeker of truth.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.NUMEROLOGY,
                    sourceName = "Pythagorean Western System",
                    rulesetOrEdition = "Pythagorean Matrix Canon",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "PYTH_LP_7",
                ),
            )
        )

        val result = adapter.explainNumbers(
            question = "What is the contemplative significance of Life Path 7?",
            userContext = AynvoraUserContext(question = "Life Path 7"),
            numberEvidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runPalmistryTest(adapter: PalmistryAiAdapter): JSONObject {
        val testName = "PALMISTRY_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "palm_heartline_curved",
                domain = CoreFeatureId.PALMISTRY,
                category = EvidenceCategory.OBSERVED_FEATURE,
                ruleId = "PALM_HEART_JUPITER",
                summary = "Heart line curves smoothly upward ending beneath the Mount of Jupiter.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.PALMISTRY,
                    sourceName = "Classical Cheirology Tradition",
                    rulesetOrEdition = "Cheiro Traditional Hand Analysis",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "CHEIRO_HEART_JUPITER",
                ),
            )
        )

        val result = adapter.explainPalmFeatures(
            question = "Reflect on a heart line extending towards the Mount of Jupiter.",
            userContext = AynvoraUserContext(question = "Heart line to Jupiter"),
            featuresEvidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runGemstoneTest(adapter: GemstoneAiAdapter): JSONObject {
        val testName = "GEMSTONE_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "gem_blue_sapphire",
                domain = CoreFeatureId.GEMSTONE,
                category = EvidenceCategory.TRADITIONAL_RULE,
                ruleId = "GEM_SATURN_NEELAM",
                summary = "Blue Sapphire (Neelam) associated with planetary energy of Shani (Saturn). Requires disciplined contemplation.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.GEMSTONE,
                    sourceName = "Garuda Purana Ratna Pariksha",
                    rulesetOrEdition = "Navaratna Classical Treatise",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "GP_RATNA_NEELAM",
                ),
            )
        )

        val result = adapter.explainRecommendation(
            question = "What is the traditional contemplative significance of Blue Sapphire (Neelam)?",
            userContext = AynvoraUserContext(question = "Blue Sapphire"),
            gemEvidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runGarudaTest(adapter: GarudaPuranAiAdapter): JSONObject {
        val testName = "GARUDA_PURAN_REFLECTION"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val evidence = listOf(
            EvidenceItem(
                evidenceId = "garuda_karma_dharma",
                domain = CoreFeatureId.GARUDA_PURAN,
                category = EvidenceCategory.SCRIPTURAL_QUOTE,
                ruleId = "GP_SARODDHARA_CH02",
                summary = "Every action (Karma) bears natural fruition; righteous conduct (Dharma) protects the soul through transitions.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.GARUDA_PURAN,
                    sourceName = "Garuda Purana Saroddhara",
                    rulesetOrEdition = "Wood & Subrahmanyam Translation",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "GP_SARO_CH02",
                ),
            )
        )

        val result = adapter.explainDharmaPassage(
            question = "What is the philosophical teaching on Karma in Garuda Purana?",
            userContext = AynvoraUserContext(question = "Karma in Garuda Purana"),
            passageEvidence = evidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private suspend fun runCrossFeatureTest(adapter: CrossFeatureReflectionAdapter): JSONObject {
        val testName = "CROSS_FEATURE_ASTROLOGY_GITA"
        val start = System.currentTimeMillis()
        val memBefore = getMemoryMb()

        val astroEvidence = listOf(
            EvidenceItem(
                evidenceId = "cross_astro_saturn",
                domain = CoreFeatureId.ASTROLOGY,
                category = EvidenceCategory.CALCULATED_POSITION,
                ruleId = "PLANET_SATURN_HOUSE_10",
                summary = "Saturn in 10th House indicates responsibility, patience, and diligent labor in one's vocation.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.ASTROLOGY,
                    sourceName = "Brihat Parasara Hora Sastra",
                    rulesetOrEdition = "Parasari Ruleset",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "BPHS_10",
                ),
            )
        )

        val gitaEvidence = listOf(
            EvidenceItem(
                evidenceId = "cross_gita_duty",
                domain = CoreFeatureId.GITA,
                category = EvidenceCategory.SCRIPTURAL_QUOTE,
                ruleId = "BG_02_47",
                summary = "BG 2.47: You have a right to perform your prescribed duty, but you are not entitled to the fruits of action.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.GITA,
                    sourceName = "Srimad Bhagavad Gita",
                    rulesetOrEdition = "Canonical Translation",
                    engineVersion = "1.0.0",
                    timestampEpochMs = System.currentTimeMillis(),
                    referenceId = "BG_2_47",
                ),
            )
        )

        val result = adapter.reflect(
            primaryFeature = CoreFeatureId.ASTROLOGY,
            secondaryFeature = CoreFeatureId.GITA,
            question = "How do astrological Saturn in 10th House and Gita Chapter 2 Verse 47 harmonize in guiding vocational duty?",
            userContext = AynvoraUserContext(question = "Vocational duty in Astrology and Gita"),
            primaryEvidence = astroEvidence,
            secondaryEvidence = gitaEvidence,
        )

        val duration = System.currentTimeMillis() - start
        return formatTestResult(testName, result, duration, memBefore, getMemoryMb())
    }

    private fun formatTestResult(
        testName: String,
        result: AynvoraResult<AynvoraAiResponse>,
        duration: Long,
        memBefore: Long,
        memAfter: Long,
    ): JSONObject {
        val obj = JSONObject()
        obj.put("testName", testName)
        obj.put("durationMs", duration)
        obj.put("memBeforeMb", memBefore)
        obj.put("memAfterMb", memAfter)

        when (result) {
            is AynvoraResult.Success -> {
                val res = result.value
                val tokens = res.responseText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                val tps = if (duration > 0) (tokens.toFloat() / (duration / 1000f)) else 0f
                obj.put("success", true)
                obj.put("executionMode", res.executionMode.name)
                obj.put("validationStatus", res.validationStatus.name)
                obj.put("fallbackUsed", res.fallbackUsed)
                obj.put("tokenCount", tokens)
                obj.put("tokensPerSec", tps)
                obj.put("responseText", res.responseText)
                Log.i(TAG, "[$testName] SUCCESS: mode=${res.executionMode.name}, tokens=$tokens, speed=${"%.2f".format(tps)} t/s")
                Log.i(TAG, "[$testName] Response: ${res.responseText}")
            }
            is AynvoraResult.Failure -> {
                obj.put("success", false)
                obj.put("error", result.message)
                Log.e(TAG, "[$testName] FAILED: ${result.message}")
            }
        }
        return obj
    }
}

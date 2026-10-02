package com.aynvora.core.astrology.knowledge

import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraValidationStatus
import com.aynvora.core.astrology.knowledge.tajika.TajikaKnowledgePack
import com.aynvora.core.astrology.knowledge.tajika.TajikaMunthaRegisteredTool
import com.aynvora.core.astrology.knowledge.tajika.TajikaRegisteredTools
import com.aynvora.core.astrology.prediction.KnowledgeChunk
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Thirty deterministic orchestration scenarios. Planner, generic chart tools, and answer generation
 * are fixtures; these cases exercise dispatch/evidence/validation wiring, not native model quality.
 */
class AstroToolExecutorScenarioTest {
    private data class Scenario(val prompt: String, val tool: String, val args: kotlinx.serialization.json.JsonObject = buildJsonObject {})
    private fun munthaArgs(house: Int) = buildJsonObject {
        put("natalAscendantLongitude", (house - 1) * 30.0 + 1.0)
        put("elapsedSolarReturnCycles", 0)
        put("annualAscendantSignIndex", 0)
    }

    private val scenarios = listOf(
        Scenario("Summarize this Kundali", "getKundali"),
        Scenario("Explain the chart", "getChart"),
        Scenario("Show planetary positions", "getPlanetaryPositions"),
        Scenario("Explain my Lagna", "getLagna"),
        Scenario("Explain the fourth house", "getHouses"),
        Scenario("Explain my Navamsha", "getVarga"),
        Scenario("Explain the current Dasha", "getDasha"),
        Scenario("Explain this transit", "getTransit"),
        Scenario("Explain today's Panchang", "getPanchang"),
        Scenario("Explain Ashtakavarga", "getAshtakavarga"),
        Scenario("Explain Shadbala", "getShadbala"),
        Scenario("Explain the chart events", "getEvents"),
        Scenario("What does this source say about Muntha?", "getMuntha", munthaArgs(3)),
        Scenario("Explain Muntha in house four", "getMuntha", munthaArgs(4)),
        Scenario("Give a short Muntha explanation", "getMuntha", munthaArgs(5)),
        Scenario("Give a deep Muntha explanation", "getMuntha", munthaArgs(6)),
        Scenario("मेरी कुंडली समझाइए", "getKundali"),
        Scenario("Explain my annual chart", "getChart"),
        Scenario("Explain my Varsheshwara", "getMuntha", munthaArgs(3)),
        Scenario("Explain Sahams", "getEvents"),
        Scenario("Explain Mudda Dasha", "getDasha"),
        Scenario("What calculation produced this value?", "getPlanetaryPositions"),
        Scenario("Which source supports this interpretation?", "getMuntha", munthaArgs(5)),
        Scenario("Keep the answer brief", "getChart"),
        Scenario("Give a detailed explanation", "getHouses"),
        Scenario("Explain my second house", "getHouses"),
        Scenario("Explain my Dasha period", "getDasha"),
        Scenario("Explain the Navamsha chart", "getVarga"),
        Scenario("Explain the annual Muntha indication", "getMuntha", munthaArgs(4)),
        Scenario("Explain supported Muntha factors in English", "getMuntha", munthaArgs(5)),
    )

    @Test fun thirtySimulatedQuestionsDispatchFuseSourceEvidenceAndValidate() = runBlocking {
        assertEquals(30, scenarios.size)
        val pack = TajikaKnowledgePack.v1()
        assertTrue(pack.validate().isEmpty(), pack.validate().joinToString())
        val registry = AstroKnowledgeSourceRegistry(pack.sourceRegistry)
        val sourceChunk = pack.chunks.first { it.page == 122 }
        val sourceFact = AstroEvidenceItem(
            evidenceId = "fixture-source-reference",
            kind = AstroEvidenceKind.LOCAL_KNOWLEDGE,
            text = sourceChunk.text,
            sourceId = sourceChunk.sourceId,
            sourceRef = sourceChunk.sourceRef,
            checksum = sourceChunk.checksum,
            traditionId = "TAJIKA",
            metadata = mapOf("sourceTitle" to pack.getSource(sourceChunk.sourceId!!)!!.title, "version" to pack.metadata.version),
        )
        val context = AstroPageContext("varshaphal", "astro.varshaphal", traditionId = "TAJIKA", evidence = listOf(sourceFact))
        val called = mutableListOf<String>()
        val registered = (AstroToolRegistry.all().filter { it.status == AstroToolStatus.AVAILABLE }.map { it.toolId } + "getMuntha").distinct().associateWith { id ->
            if (id == "getMuntha") TajikaRegisteredTools.verifiedSubset().getValue("getMuntha") else AstroRegisteredTool { _, _ ->
                called += id
                AstroRegisteredToolResult(id, "fixture deterministic payload for $id", listOf(AstroEvidenceItem("$id-fixture", AstroEvidenceKind.DETERMINISTIC_CALCULATION, "Fixture result for $id")))
            }
        }
        val planner = AstroFunctionCallPlanner { question, _, _ ->
            val scenario = scenarios.first { it.prompt == question }
            AstroFunctionCall(scenario.tool, scenario.args)
        }
        var generatedCount = 0
        val responseGenerator = AstroGroundedResponseGenerator { question, _, evidence, _, _, _ ->
            generatedCount++
            assertTrue(evidence.items.isNotEmpty(), "No evidence for $question")
            assertTrue(evidence.items.any { it.sourceId == sourceChunk.sourceId && it.sourceRef == sourceChunk.sourceRef }, "Source reference lost for $question")
            AynvoraResult.Success(AynvoraAiResponse(
                requestId = "simulated-${question.hashCode()}", responseText = "Fixture answer grounded in supplied evidence.",
                executionMode = AiExecutionMode.LOCAL_SIMULATION, featureId = CoreFeatureId.ASTROLOGY,
                knowledgePackId = pack.metadata.packId, validationStatus = AynvoraValidationStatus.VALIDATED,
            ))
        }
        val executor = AynvoraAiToolExecutor(planner, registered, responseGenerator, registry, pack.chunks, pack.metadata.rules)

        scenarios.forEachIndexed { index, scenario ->
            val result = executor.ask(scenario.prompt, context, locale = if (index == 16) "hi" else "en", requestTimestampEpochMs = 1_800_000_000_000L)
            val success = assertIs<AynvoraResult.Success<AstroGroundedAiResult>>(result, "Scenario ${index + 1} failed: ${scenario.prompt}")
            assertEquals(scenario.tool, success.value.functionCall.tool, "Wrong selected tool for ${scenario.prompt}")
            assertEquals(scenario.tool, success.value.toolResult.toolId, "Tool not executed for ${scenario.prompt}")
            assertTrue(success.value.evidence.items.isNotEmpty(), "Evidence missing for ${scenario.prompt}")
            assertTrue(success.value.evidence.items.any { it.sourceRef == sourceChunk.sourceRef }, "Citation missing for ${scenario.prompt}")
            assertEquals(AynvoraValidationStatus.VALIDATED, success.value.answer.validationStatus)
            assertEquals(false, success.value.nativeVerified, "Fixtures must not claim native execution")
        }
        assertEquals(30, generatedCount)
        assertTrue(called.containsAll(scenarios.map { it.tool }.filter { it != "getMuntha" }.toSet()), "A scenario's selected fixture tool was not executed")
    }

    @Test fun unavailableResearchToolCannotBeExecutedByModelCall() = runBlocking {
        val executor = AynvoraAiToolExecutor(
            AstroFunctionCallPlanner { _, _, _ -> AstroFunctionCall("getKPResearch", buildJsonObject {}) },
            mapOf("getMuntha" to TajikaMunthaRegisteredTool()),
            AstroGroundedResponseGenerator { _, _, _, _, _, _ -> error("Unsupported research tool must not reach answer generation") },
            AstroKnowledgeSourceRegistry(TajikaKnowledgePack.v1().sourceRegistry),
        )
        val result = executor.ask("Pretend you calculated KP", AstroPageContext("chart", "astro.chart"), requestTimestampEpochMs = 1_800_000_000_000L)
        assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
        Unit
    }
}

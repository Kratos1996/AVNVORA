package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.Aynvora
import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraValidationStatus
import com.aynvora.core.astrology.knowledge.*
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.25 Real Varshaphal Tool Executor Integration Test.
 *
 * Verifies that AynvoraAiToolExecutor dispatches to actual registered classical Tajika calculators
 * without fixture planners or synthetic mock calculators on the acceptance path.
 *
 * Flow:
 * strict JSON -> AynvoraAiToolExecutor -> AstroToolRegistry -> real calculator -> FeatureResult -> EvidenceFusion -> PromptBuilder
 */
class RealVarshaphalToolExecutorIntegrationTest {

    private val pack = TajikaKnowledgePack.v1()
    private val sdk = Aynvora.create()
    private val realTools = TajikaRegisteredTools.verifiedSubset(sdk)
    private val sourceRegistry = AstroKnowledgeSourceRegistry(pack.sourceRegistry)

    @Test
    fun realToolExecutorExecutesAllSevenTajikaToolsWithRealCalculators() = runBlocking {
        val testCalls = listOf(
            // 1. getMuntha
            """{"tool":"getMuntha","arguments":{"natalAscendantLongitude":15.0,"elapsedSolarReturnCycles":2,"annualAscendantSignIndex":1}}""",
            // 2. getMunthaLord
            """{"tool":"getMunthaLord","arguments":{"munthaSignIndex":2,"annualAscendantSignIndex":0}}""",
            // 3. getVarsheshwara
            """{"tool":"getVarsheshwara","arguments":{"natalAscendantLongitude":15.0,"annualAscendantSignIndex":4,"munthaSignIndex":1,"isDay":true}}""",
            // 4. getSahams
            """{"tool":"getSahams","arguments":{"ascendantLongitude":120.0,"sunLongitude":130.0,"moonLongitude":250.0,"marsLongitude":40.0,"mercuryLongitude":160.0,"jupiterLongitude":10.0,"isDay":true}}""",
            // 5. getTajikaAspects
            """{"tool":"getTajikaAspects","arguments":{"ascendantLongitude":0.0,"sunLongitude":10.0,"moonLongitude":68.0,"marsLongitude":192.0}}""",
            // 6. getMuddaDasha
            """{"tool":"getMuddaDasha","arguments":{"returnUtcTimestamp":"2024-01-01 12:00:00 UTC","natalMoonLongitude":10.0,"elapsedCycles":0,"annualLengthDays":365.24219}}""",
            // 7. getVarshaphal
            """{"tool":"getVarshaphal","arguments":{"birthYear":2000,"birthMonth":1,"birthDay":1,"birthHour":12,"birthMinute":0,"latitude":28.6139,"longitude":77.2090,"timezoneId":"Asia/Kolkata","targetYear":2024}}""",
        )

        for (strictJson in testCalls) {
            val plannedCall = AstroFunctionCallParser.parse(strictJson)
            val planner = AstroFunctionCallPlanner { _, _, _ -> plannedCall }

            val responseGenerator = AstroGroundedResponseGenerator { question, context, evidence, locale, answerMode, ts ->
                // PromptBuilder simulation: formats grounded answer from real evidence items
                val summary = evidence.items.joinToString(" | ") { it.text }
                AynvoraResult.Success(
                    AynvoraAiResponse(
                        requestId = "real-tool-${plannedCall.tool}-$ts",
                        responseText = "Grounded response for ${plannedCall.tool}: $summary",
                        executionMode = AiExecutionMode.LOCAL_SIMULATION,
                        featureId = CoreFeatureId.ASTROLOGY,
                        knowledgePackId = pack.metadata.packId,
                        validationStatus = AynvoraValidationStatus.VALIDATED,
                    )
                )
            }

            val executor = AynvoraAiToolExecutor(
                planner = planner,
                tools = realTools,
                responseGenerator = responseGenerator,
                sources = sourceRegistry,
                chunks = pack.chunks,
                rules = pack.metadata.rules,
            )

            val context = AstroPageContext("varshaphal", "astro.varshaphal", traditionId = "TAJIKA")
            val outcome = executor.ask(
                question = "Execute ${plannedCall.tool}",
                context = context,
                requestTimestampEpochMs = 1_800_000_000_000L,
            )

            val success = assertIs<AynvoraResult.Success<AstroGroundedAiResult>>(outcome, "Execution failed for tool ${plannedCall.tool}")
            val trace = success.value

            // 1. Verify toolId
            assertEquals(plannedCall.tool, trace.toolResult.toolId, "Tool ID in trace must match requested tool")

            // 2. Verify arguments
            assertEquals(plannedCall.arguments, trace.functionCall.arguments, "Arguments in trace must match input")

            // 3. Verify real calculation result payload (not empty or mock placeholder)
            assertTrue(trace.toolResult.resultJson.isNotBlank(), "Result payload must not be blank")
            assertTrue(!trace.toolResult.resultJson.contains("fixture deterministic payload"), "Result must be from real engine, not fixture")

            // 4. Verify provenance
            assertTrue(trace.toolResult.evidence.isNotEmpty(), "Tool must produce real evidence items")
            val primaryEvidence = trace.toolResult.evidence.first()
            assertNotNull(primaryEvidence.sourceRef, "Primary source reference must be recorded")
            assertEquals("TAJIKA", primaryEvidence.traditionId, "Tradition must be TAJIKA")

            // 5. Verify EvidenceFusion: tool evidence fused into final evidence graph
            assertTrue(trace.evidence.items.any { it.evidenceId == primaryEvidence.evidenceId }, "Fused evidence must retain tool calculation evidence")

            // 6. Verify validation
            assertEquals(AynvoraValidationStatus.VALIDATED, trace.answer.validationStatus, "Answer must be validated")
        }
    }
}

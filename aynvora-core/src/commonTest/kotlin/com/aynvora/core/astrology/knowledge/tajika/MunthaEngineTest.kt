package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.Rashi
import com.aynvora.core.astrology.knowledge.AstroPageContext
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.aynvora.core.astrology.knowledge.*
import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraValidationStatus
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MunthaEngineTest {
    @Test
    fun advancesOneSignPerElapsedCycleAndPreservesWithinSignLongitude() {
        val zero = MunthaEngine.calculate(15.0, 0)
        val one = MunthaEngine.calculate(15.0, 1)
        val wrap = MunthaEngine.calculate(359.0, 1)
        val fullCycle = MunthaEngine.calculate(15.0, 12)
        assertEquals(Rashi.ARIES, zero.sign)
        assertEquals(15.0, zero.longitude)
        assertEquals(MunthaLord.MARS, zero.lord)
        assertEquals(Rashi.TAURUS, one.sign)
        assertEquals(45.0, one.longitude)
        assertEquals(MunthaLord.VENUS, one.lord)
        assertEquals(Rashi.ARIES, wrap.sign)
        assertEquals(29.0, wrap.longitude)
        assertEquals(Rashi.ARIES, fullCycle.sign)
        assertEquals(15.0, fullCycle.longitude)
    }

    @Test
    fun computesAnnualWholeSignHouseAndLongerOffsets() {
        assertEquals(5, MunthaEngine.calculate(359.0, 1, Rashi.SAGITTARIUS).annualHouse)
        assertEquals(Rashi.PISCES, MunthaEngine.calculate(15.0, 11).sign)
        assertEquals(Rashi.TAURUS, MunthaEngine.calculate(15.0, 13).sign)
    }

    @Test
    fun rejectsInvalidLongitudeAndElapsedCycles() {
        assertFailsWith<IllegalArgumentException> { MunthaEngine.calculate(-0.01, 0) }
        assertFailsWith<IllegalArgumentException> { MunthaEngine.calculate(360.0, 0) }
        assertFailsWith<IllegalArgumentException> { MunthaEngine.calculate(0.0, -1) }
    }

    @Test
    fun verifiedMunthaToolIsRegisteredAndExecutesWithSourceEvidence() = runBlocking {
        val descriptor = com.aynvora.core.astrology.knowledge.AstroToolRegistry.all().first { it.toolId == "getMuntha" }
        assertEquals(com.aynvora.core.astrology.knowledge.AstroToolStatus.AVAILABLE, descriptor.status)
        val pack = TajikaKnowledgePack.v1()
        val executor = AynvoraAiToolExecutor(
            planner = AstroFunctionCallPlanner { _, _, _ ->
                AstroFunctionCallParser.parse("""{"tool":"getMuntha","arguments":{"natalAscendantLongitude":15.0,"elapsedSolarReturnCycles":1,"annualAscendantSignIndex":0}}""")
            },
            tools = TajikaRegisteredTools.verifiedSubset(),
            responseGenerator = AstroGroundedResponseGenerator { _, _, evidence, _, _, _ ->
                assertTrue(evidence.items.any { it.evidenceId == "TN-MUN-01-02" })
                AynvoraResult.Success(AynvoraAiResponse(
                    requestId = "muntha-executor-test",
                    responseText = "Muntha calculation is supported by the cited source.",
                    executionMode = AiExecutionMode.LOCAL_SIMULATION,
                    featureId = CoreFeatureId.ASTROLOGY,
                    knowledgePackId = pack.metadata.packId,
                    validationStatus = AynvoraValidationStatus.VALIDATED,
                ))
            },
            sources = AstroKnowledgeSourceRegistry(pack.sourceRegistry),
        )
        val output = assertIs<AynvoraResult.Success<AstroGroundedAiResult>>(
            executor.ask("Calculate Muntha", AstroPageContext("annual-chart", "astro.varshaphal", traditionId = "TAJIKA"), requestTimestampEpochMs = 1_800_000_000_000L),
        ).value.toolResult
        assertEquals("getMuntha", output.toolId)
        assertEquals("SUPPORTED_PRIMARY_SOURCE_VERIFIED", output.status)
        assertEquals("TAJIKA", output.evidence.single().traditionId)
        assertEquals("astro.varshaphal.muntha", output.evidence.single().featureId)
        assertTrue(output.resultJson.contains("TAURUS"), "Tool result must contain calculated Muntha sign")
        assertTrue(output.resultJson.contains("45.0"), "Tool result must contain calculated Muntha longitude")
    }
}

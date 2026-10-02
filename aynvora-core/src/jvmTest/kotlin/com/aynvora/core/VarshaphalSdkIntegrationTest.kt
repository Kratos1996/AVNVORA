package com.aynvora.core

import com.aynvora.core.models.*
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VarshaphalSdkIntegrationTest {
    @Test
    fun sdkCalculatesSolarReturnAndBuildsAnnualSharedChartDeterministically() = runBlocking {
        val request = ChartRequest(
            BirthData(BirthDate(2000, 1, 1), BirthTime(17, 30),
                BirthPlace("New Delhi", Coordinates(28.6139, 77.2090), "Asia/Kolkata")),
            CalculationConfig(houseSystem = HouseSystem.WHOLE_SIGN),
        )
        val sdk = Aynvora.create()
        val first = sdk.calculateVarshaphal(request, 2024)
        val second = sdk.calculateVarshaphal(request, 2024)
        assertIs<AynvoraResult.Success<com.aynvora.core.astrology.knowledge.tajika.VarshaphalResult>>(first)
        assertIs<AynvoraResult.Success<com.aynvora.core.astrology.knowledge.tajika.VarshaphalResult>>(second)
        val result = first.value
        assertEquals("CALCULATED", result.solarReturn.status.name)
        assertNotNull(result.annualChart)
        assertEquals(12, result.annualChart.houses.size)
        assertEquals("WHOLE_SIGN", result.annualChart.houseSystem)
        assertEquals("PARTIAL", result.status)
        assertEquals(
            Json.encodeToString(result),
            Json.encodeToString(second.value),
            "Repeated calculations should serialize identically.",
        )
        assertTrue(result.diagnostics.any { it.contains("unsupported") || it.contains("not enabled") })
    }
}

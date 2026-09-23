package com.aynvora.astro

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AstroEngineTest {

    @Test
    fun engineCalculatesFoundationBaselineStatus() {
        val engine = AynvoraAstroEngine()
        val birthData = BirthData(
            dateTimeIso = "2000-01-01T12:00:00Z",
            latitude = 28.6139,
            longitude = 77.2090,
            timeZoneId = "Asia/Kolkata",
        )

        val result = runBlocking { engine.calculate(birthData) }
        assertNotNull(result)
        assertEquals("0.2.0", result.engineVersion)
        assertEquals("CALCULATED", result.status)
        assertEquals("MEEUS_VSOP87", result.calculationModel)
        assertEquals(9, result.positions.size)
    }
}

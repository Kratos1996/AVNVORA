package com.aynvora.astro

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        assertEquals("0.3.0", result.engineVersion)
        assertEquals("CALCULATED", result.status)
        assertEquals("MEEUS_VSOP87", result.calculationModel)
        assertEquals(9, result.positions.size)

        // Lagna verification
        assertNotNull(result.lagna)
        assertTrue(result.lagna.tropicalLongitude in 0.0..<360.0)
        assertTrue(result.lagna.siderealLongitude in 0.0..<360.0)

        // Houses verification
        assertEquals(12, result.houses.size)
        assertEquals(9, result.planetHouseOccupancy.size)
        result.planetHouseOccupancy.values.forEach { houseNum ->
            assertTrue(houseNum in 1..12)
        }

        // Aspects & States verification
        assertNotNull(result.aspects)
        assertNotNull(result.planetStates)
        assertEquals(9, result.planetStates.size)
    }
}

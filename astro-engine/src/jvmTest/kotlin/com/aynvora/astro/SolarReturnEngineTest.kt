package com.aynvora.astro

import com.aynvora.astro.varshaphal.SolarReturnEngine
import com.aynvora.astro.varshaphal.SolarReturnStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SolarReturnEngineTest {
    @Test
    fun calculatesReproducibleAnnualSiderealSunReturn() = kotlinx.coroutines.runBlocking {
        val engine = SolarReturnEngine(AynvoraAstroEngine())
        val birth = BirthData(
            dateTimeIso = "2000-01-01T17:30:00",
            latitude = 28.6139,
            longitude = 77.2090,
            timeZoneId = "Asia/Kolkata",
            year = 2000, month = 1, day = 1, hour = 17, minute = 30,
        )

        val first = engine.calculate(birth, 2024)
        val repeated = engine.calculate(birth, 2024)

        assertEquals(SolarReturnStatus.CALCULATED, first.status, first.diagnostics.joinToString())
        assertNotNull(first.julianDayUtc)
        assertNotNull(first.utcTimestamp)
        assertEquals(first.julianDayUtc, repeated.julianDayUtc)
        assertEquals(first.utcTimestamp, repeated.utcTimestamp)
        assertTrue(kotlin.math.abs(first.natalSunLongitude!! - first.returnSunLongitude!!) < 0.001)
        assertTrue(first.diagnostics.any { it.contains("accuracy") })
    }
}

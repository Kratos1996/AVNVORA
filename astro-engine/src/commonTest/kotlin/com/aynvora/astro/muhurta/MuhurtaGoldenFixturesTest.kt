package com.aynvora.astro.muhurta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MuhurtaGoldenFixturesTest {

    @Test
    fun testRahuKalamAndYamagandaAcrossWeekdays() {
        // Sunday (weekday 0): Rahu Kalam is 8th part of day
        val sunRes = MuhurtaEngine.calculate(0, dayFraction = 0.9)
        assertEquals(8, sunRes.rahuKalam.partIndex)
        assertEquals(5, sunRes.yamaganda.partIndex)
        assertEquals(7, sunRes.gulikaKalam.partIndex)

        // Monday (weekday 1): Rahu Kalam is 2nd part of day
        val monRes = MuhurtaEngine.calculate(1, dayFraction = 0.2)
        assertEquals(2, monRes.rahuKalam.partIndex)
        assertEquals(4, monRes.yamaganda.partIndex)
        assertEquals(6, monRes.gulikaKalam.partIndex)

        // Friday (weekday 5): Rahu Kalam is 4th part of day
        val friRes = MuhurtaEngine.calculate(5, dayFraction = 0.45)
        assertEquals(4, friRes.rahuKalam.partIndex)
        assertEquals(7, friRes.yamaganda.partIndex)
        assertEquals(2, friRes.gulikaKalam.partIndex)
    }

    @Test
    fun testAbhijitMuhurtaSpanFraction() {
        val res = MuhurtaEngine.calculate(3, dayFraction = 0.5) // Wednesday midday
        // Abhijit is always exactly the 8th of 15 diurnal muhurtas: [7/15, 8/15]
        assertEquals(7.0 / 15.0, res.abhijitStartFraction)
        assertEquals(8.0 / 15.0, res.abhijitEndFraction)
        assertTrue(res.abhijitStartFraction < res.abhijitEndFraction)
    }

    @Test
    fun testChaldeanHoraSequence() {
        // Sunday sunrise starts with Sun Hora
        val sunDawn = MuhurtaEngine.calculate(0, dayFraction = 0.01)
        assertEquals("Sun", sunDawn.currentHora.lord)

        // Monday sunrise starts with Moon Hora
        val monDawn = MuhurtaEngine.calculate(1, dayFraction = 0.01)
        assertEquals("Moon", monDawn.currentHora.lord)

        // Tuesday sunrise starts with Mars Hora
        val tueDawn = MuhurtaEngine.calculate(2, dayFraction = 0.01)
        assertEquals("Mars", tueDawn.currentHora.lord)
    }
}

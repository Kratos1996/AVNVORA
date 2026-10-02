package com.aynvora.astro.jaimini

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JaiminiEngineTest {

    @Test
    fun testCharaKarakaDescendingDegreeOrdering() {
        val planetLons = mapOf(
            "SUN" to 28.5,       // Aries 28.5° -> Highest -> Atmakaraka (AK)
            "MOON" to 54.0,      // Taurus 24.0° -> 2nd -> Amatyakaraka (AmK)
            "MARS" to 78.2,      // Gemini 18.2° -> 3rd -> Bhratrikaraka (BK)
            "MERCURY" to 105.0,  // Cancer 15.0° -> 4th -> Matrikaraka (MK)
            "JUPITER" to 132.1,  // Leo 12.1° -> 5th -> Putrakaraka (PK)
            "VENUS" to 158.4,    // Virgo 8.4° -> 6th -> Gnatikaraka (GK)
            "SATURN" to 183.0,   // Libra 3.0° -> 7th -> Darakaraka (DK)
        )

        val karakas = JaiminiEngine.calculateCharaKarakas(planetLons)
        assertEquals(7, karakas.size)

        assertEquals(JaiminiKarakaRole.ATMAKARAKA, karakas[0].role)
        assertEquals("SUN", karakas[0].planetName)
        assertEquals(28.5, karakas[0].degreeInSign, 1e-6)

        assertEquals(JaiminiKarakaRole.AMATYAKARAKA, karakas[1].role)
        assertEquals("MOON", karakas[1].planetName)

        assertEquals(JaiminiKarakaRole.DARAKARAKA, karakas[6].role)
        assertEquals("SATURN", karakas[6].planetName)
    }

    @Test
    fun testArudhaPadaExceptionHandling() {
        // If Lagna is Aries (0) and Mars is in Aries (0):
        // Raw Arudha would be in Aries (distance 1 from 1st house).
        // Jaimini exception moves it 10 houses forward -> Capricorn (9).
        val planetRashi = mapOf(
            "MARS" to 0,
            "VENUS" to 1,
            "MERCURY" to 2,
            "MOON" to 3,
            "SUN" to 4,
            "JUPITER" to 8,
            "SATURN" to 9,
        )

        val arudhas = JaiminiEngine.calculateArudhas(0, planetRashi)
        val al = arudhas.first { it.label == "AL" }

        assertTrue(al.appliedException, "Exception must be applied when lord is in 1st house from rashi")
        assertEquals("Capricorn", al.rashiName)
    }

    @Test
    fun testJaiminiRashiAspectRules() {
        val aspects = JaiminiEngine.calculateRashiAspects()
        assertTrue(aspects.isNotEmpty())

        // Aries (Movable) aspects Leo, Scorpio, Aquarius (Fixed) - but NOT Taurus (adjacent Fixed)
        val ariesAspects = aspects.filter { it.sourceRashi == "Aries" }.map { it.targetRashi }
        assertTrue(ariesAspects.contains("Leo"))
        assertTrue(ariesAspects.contains("Scorpio"))
        assertTrue(ariesAspects.contains("Aquarius"))
        assertTrue(!ariesAspects.contains("Taurus"), "Aries must not aspect adjacent Taurus")
    }
}

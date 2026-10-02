package com.aynvora.astro.yogas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YogaDoshaGoldenFixturesTest {

    @Test
    fun testGajakesariYoga_TruePositiveAndNegative() {
        // Moon in 1st house, Jupiter in 4th house (4th from Moon -> Kendra)
        val positive = YogaDoshaEngine.evaluate(
            mapOf("MOON" to 1, "JUPITER" to 4)
        )
        val gajakesariPos = positive.yogas.single { it.id == "YOGA_GAJAKESARI" }
        assertTrue(gajakesariPos.isPresent)
        assertEquals("STRONG", gajakesariPos.strength)

        // Moon in 1st house, Jupiter in 6th house (6th from Moon -> Dusthana, NOT Kendra)
        val negative = YogaDoshaEngine.evaluate(
            mapOf("MOON" to 1, "JUPITER" to 6)
        )
        val gajakesariNeg = negative.yogas.single { it.id == "YOGA_GAJAKESARI" }
        assertFalse(gajakesariNeg.isPresent)
        assertEquals("ABSENT", gajakesariNeg.strength)
    }

    @Test
    fun testKemadrumaYoga_FormationAndCancellation() {
        // Isolated Moon in 5th house (Trine, not Kendra), no planets in 4th or 6th, no planets in Kendra from Lagna or Moon
        val formed = YogaDoshaEngine.evaluate(
            mapOf("MOON" to 5, "SUN" to 9, "SATURN" to 12)
        )
        val kemadrumaFormed = formed.doshas.single { it.id == "DOSHA_KEMADRUMA" }
        assertTrue(kemadrumaFormed.isPresent)
        assertEquals("STRONG", kemadrumaFormed.strength)

        // Moon in 1st house (Kendra from Lagna). Kemadruma Bhanga applies!
        val cancelled = YogaDoshaEngine.evaluate(
            mapOf("MOON" to 1, "SUN" to 9)
        )
        val kemadrumaCancelled = cancelled.doshas.single { it.id == "DOSHA_KEMADRUMA" }
        assertTrue(kemadrumaCancelled.isPresent)
        assertEquals("CANCELLED", kemadrumaCancelled.strength)
    }

    @Test
    fun testManglikDosha_FormationAndExaltationCancellation() {
        // Mars in 7th house in Cancer (Sign 3, Debilitated) -> Uncancelled Manglik
        val formed = YogaDoshaEngine.evaluate(
            planetHouses = mapOf("MARS" to 7),
            planetSigns = mapOf("MARS" to 3) // Cancer
        )
        val manglikFormed = formed.doshas.single { it.id == "DOSHA_MANGLIK" }
        assertTrue(manglikFormed.isPresent)
        assertEquals("MODERATE", manglikFormed.strength)

        // Mars in 7th house in Capricorn (Sign 9, Exalted) -> Kuja Dosha Bhanga!
        val cancelled = YogaDoshaEngine.evaluate(
            planetHouses = mapOf("MARS" to 7),
            planetSigns = mapOf("MARS" to 9) // Capricorn
        )
        val manglikCancelled = cancelled.doshas.single { it.id == "DOSHA_MANGLIK" }
        assertTrue(manglikCancelled.isPresent)
        assertEquals("CANCELLED", manglikCancelled.strength)
    }

    @Test
    fun testKalaSarpaDosha_HemmedVsBroken() {
        // Rahu in 1st house, Ketu in 7th house (opposite axis)
        // All 7 planets in houses 2, 3, 4, 5, 6 (strictly side A)
        val trueKalaSarpa = YogaDoshaEngine.evaluate(
            mapOf(
                "RAHU" to 1,
                "KETU" to 7,
                "SUN" to 2,
                "MOON" to 3,
                "MARS" to 4,
                "MERCURY" to 5,
                "JUPITER" to 6,
                "VENUS" to 2,
                "SATURN" to 4
            )
        )
        val ksTrue = trueKalaSarpa.doshas.single { it.id == "DOSHA_KALA_SARPA" }
        assertTrue(ksTrue.isPresent)
        assertEquals("STRONG", ksTrue.strength)

        // One planet (e.g. Jupiter) moves to house 10 (side B, breaking the hemming)
        val broken = YogaDoshaEngine.evaluate(
            mapOf(
                "RAHU" to 1,
                "KETU" to 7,
                "SUN" to 2,
                "MOON" to 3,
                "MARS" to 4,
                "MERCURY" to 5,
                "JUPITER" to 10, // Across the axis!
                "VENUS" to 2,
                "SATURN" to 4
            )
        )
        val ksBroken = broken.doshas.single { it.id == "DOSHA_KALA_SARPA" }
        assertFalse(ksBroken.isPresent)
        assertEquals("ABSENT", ksBroken.strength)
    }
}

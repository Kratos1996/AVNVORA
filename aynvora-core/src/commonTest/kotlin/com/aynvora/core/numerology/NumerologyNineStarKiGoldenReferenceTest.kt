package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.3: Golden Reference Test for Chinese Nine Star Ki (Feng Shui Flying Stars).
 *
 * Authorities:
 * - Takashi Yoshikawa, "The Ki: The Japanese Art of Divination" (1981)
 * - Jean Meeus, "Astronomical Algorithms" (315.0° Li Chun Solar Longitude)
 * - Classical Luoshu 9-Star Sequence
 *
 * Requirements:
 * - Solar year begins at astronomical Li Chun (c. Feb 3–5, exact 315° crossing).
 * - Dates before Li Chun belong to the preceding solar year.
 * - Dates after Li Chun belong to the current solar year.
 * - Principal Star formula: 11 - (sum of digits % 9).
 */
class NumerologyNineStarKiGoldenReferenceTest {

    @Test
    fun testBeforeLiChunBoundary1996() {
        // February 1, 1996: Preceding solar year 1995
        // 1995: 1+9+9+5 = 24 -> 6 -> 11 - 6 = 5 (5 Yellow Earth)
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 2,
            birthYear = 1996,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val nsk = result.value.profile.nineStarKi
        assertNotNull(nsk)
        assertEquals(1995, nsk.solarYear)
        assertTrue(nsk.isBeforeLiChun)
        assertEquals(5, nsk.principalStar.number)
        assertEquals("5 Yellow Earth", nsk.principalStar.englishName)
        assertEquals("Earth", nsk.principalStar.element)
    }

    @Test
    fun testAfterLiChunBoundary1996() {
        // February 10, 1996: Solar year 1996
        // 1996: 1+9+9+6 = 25 -> 7 -> 11 - 7 = 4 (4 Green Wood)
        val request = NumerologyRequest(
            birthDay = 10,
            birthMonth = 2,
            birthYear = 1996,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val nsk = result.value.profile.nineStarKi
        assertNotNull(nsk)
        assertEquals(1996, nsk.solarYear)
        assertFalse(nsk.isBeforeLiChun)
        assertEquals(4, nsk.principalStar.number)
        assertEquals("4 Green Wood", nsk.principalStar.englishName)
        assertEquals("Wood", nsk.principalStar.element)
        assertEquals("Xun", nsk.principalStar.trigram)
    }

    @Test
    fun testMillenniumYearBoundary2000() {
        // Jan 15, 2000 -> Solar year 1999
        // 1999: 1+9+9+9 = 28 -> 10 -> 1 -> 11 - 1 = 1 (1 White Water / Kan)
        val reqBefore = NumerologyRequest(
            birthDay = 15,
            birthMonth = 1,
            birthYear = 2000,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val resBefore = NumerologyCalculationEngine.calculate(reqBefore)
        assertTrue(resBefore is AynvoraResult.Success)
        val nskBefore = resBefore.value.profile.nineStarKi
        assertNotNull(nskBefore)
        assertEquals(1999, nskBefore.solarYear)
        assertEquals(1, nskBefore.principalStar.number)
        assertEquals("1 White Water", nskBefore.principalStar.englishName)
        assertEquals("Kan", nskBefore.principalStar.trigram)

        // July 15, 2000 -> Solar year 2000
        // 2000: 2+0+0+0 = 2 -> 11 - 2 = 9 (9 Purple Fire / Li)
        val reqAfter = NumerologyRequest(
            birthDay = 15,
            birthMonth = 7,
            birthYear = 2000,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val resAfter = NumerologyCalculationEngine.calculate(reqAfter)
        assertTrue(resAfter is AynvoraResult.Success)
        val nskAfter = resAfter.value.profile.nineStarKi
        assertNotNull(nskAfter)
        assertEquals(2000, nskAfter.solarYear)
        assertEquals(9, nskAfter.principalStar.number)
        assertEquals("9 Purple Fire", nskAfter.principalStar.englishName)
        assertEquals("Li", nskAfter.principalStar.trigram)
    }

    @Test
    fun testModernYear2024() {
        // Dec 1, 2024 -> Solar year 2024
        // 2024: 2+0+2+4 = 8 -> 11 - 8 = 3 (3 Jade Wood / Zhen)
        val req = NumerologyRequest(
            birthDay = 1,
            birthMonth = 12,
            birthYear = 2024,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val res = NumerologyCalculationEngine.calculate(req)
        assertTrue(res is AynvoraResult.Success)
        val nsk = res.value.profile.nineStarKi
        assertNotNull(nsk)
        assertEquals(2024, nsk.solarYear)
        assertEquals(3, nsk.principalStar.number)
        assertEquals("3 Jade Wood", nsk.principalStar.englishName)
        assertEquals("Zhen", nsk.principalStar.trigram)
    }

    @Test
    fun testEvidenceGraphContainsNineStarNodes() {
        val request = NumerologyRequest(
            birthDay = 10,
            birthMonth = 2,
            birthYear = 1996,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val graph = NumerologyEvidenceGraphFactory.create(result.value, request)
        assertTrue(graph.nodes.containsKey("num_fact_solar_year"))
        assertTrue(graph.nodes.containsKey("num_derived_nine_star_principal"))
    }
}

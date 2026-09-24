package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AshtakavargaCalculatorTest {

    // Helper to generate a dummy BodyPosition at a specified rashiIndex
    private fun createDummyBodyPosition(bodyId: BodyId, rashiIndex: Int): BodyPosition {
        return BodyPosition(
            bodyId = bodyId,
            tropicalLongitude = (rashiIndex * 30.0) + 15.0,
            siderealLongitude = (rashiIndex * 30.0) + 15.0,
            rashiIndex = rashiIndex,
            rashiName = "Rashi_$rashiIndex",
            degreeInRashi = 15.0,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = false,
            dailyMotionDegrees = 1.0,
        )
    }

    private fun createDummyLagna(rashiIndex: Int): LagnaPosition {
        return LagnaPosition(
            tropicalLongitude = (rashiIndex * 30.0) + 15.0,
            siderealLongitude = (rashiIndex * 30.0) + 15.0,
            rashiIndex = rashiIndex,
            rashiName = "Rashi_$rashiIndex",
            degreeInRashi = 15.0,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
        )
    }

    // =========================================================================
    // SECTION 1: Independent Reference Tests (BPHS Ch. 66-72 Rules Verification)
    // =========================================================================

    @Test
    fun testSuryaAshtakavargaClassicalContributionRules_ASTRO_R39A() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.SUN]
        assertNotNull(rules)

        // BPHS Ch. 66: Sun BAV contributions from 8 sources
        assertEquals(setOf(1, 2, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(3, 6, 10, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(1, 2, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(3, 5, 6, 9, 10, 11, 12), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(5, 6, 9, 11), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(6, 7, 12), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(1, 2, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(3, 4, 6, 10, 11, 12), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(48, totalRuleBindus, "Surya Ashtakavarga must total 48 Bindus")
    }

    @Test
    fun testChandraAshtakavargaClassicalContributionRules_ASTRO_R39B() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.MOON]
        assertNotNull(rules)

        // BPHS Ch. 67: Moon BAV contributions from 8 sources
        assertEquals(setOf(3, 6, 7, 8, 10, 11), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(1, 3, 6, 7, 10, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(2, 3, 5, 6, 9, 10, 11), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(1, 3, 4, 5, 7, 8, 10, 11), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(1, 4, 7, 8, 10, 11, 12), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(3, 4, 5, 7, 9, 10, 11), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(3, 5, 6, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(3, 6, 10, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(49, totalRuleBindus, "Chandra Ashtakavarga must total 49 Bindus")
    }

    @Test
    fun testMangalaAshtakavargaClassicalContributionRules_ASTRO_R39C() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.MARS]
        assertNotNull(rules)

        // BPHS Ch. 68: Mars BAV contributions from 8 sources
        assertEquals(setOf(3, 5, 6, 10, 11), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(3, 6, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(1, 2, 4, 7, 8, 10, 11), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(3, 5, 6, 11), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(6, 10, 11, 12), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(6, 8, 11, 12), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(1, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(1, 3, 6, 10, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(39, totalRuleBindus, "Mangala Ashtakavarga must total 39 Bindus")
    }

    @Test
    fun testBudhaAshtakavargaClassicalContributionRules_ASTRO_R39D() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.MERCURY]
        assertNotNull(rules)

        // BPHS Ch. 69: Mercury BAV contributions from 8 sources
        assertEquals(setOf(5, 6, 9, 11, 12), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(2, 4, 6, 8, 10, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(1, 2, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(1, 3, 5, 6, 9, 10, 11, 12), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(6, 8, 11, 12), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(1, 2, 3, 4, 5, 8, 9, 11), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(1, 2, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(1, 2, 4, 6, 8, 10, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(54, totalRuleBindus, "Budha Ashtakavarga must total 54 Bindus")
    }

    @Test
    fun testGuruAshtakavargaClassicalContributionRules_ASTRO_R39E() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.JUPITER]
        assertNotNull(rules)

        // BPHS Ch. 70: Jupiter BAV contributions from 8 sources
        assertEquals(setOf(1, 2, 3, 4, 7, 8, 9, 10, 11), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(2, 5, 7, 9, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(1, 2, 4, 7, 8, 10, 11), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(1, 2, 4, 5, 6, 9, 10, 11), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(1, 2, 3, 4, 7, 8, 10, 11), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(2, 5, 6, 9, 10, 11), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(3, 5, 6, 12), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(1, 2, 4, 5, 6, 7, 9, 10, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(56, totalRuleBindus, "Guru Ashtakavarga must total 56 Bindus")
    }

    @Test
    fun testShukraAshtakavargaClassicalContributionRules_ASTRO_R39F() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.VENUS]
        assertNotNull(rules)

        // BPHS Ch. 71: Venus BAV contributions from 8 sources
        assertEquals(setOf(8, 11, 12), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(1, 2, 3, 4, 5, 8, 9, 11, 12), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(3, 5, 6, 9, 11, 12), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(3, 5, 6, 9, 11), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(5, 8, 9, 10, 11), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(1, 2, 3, 4, 5, 8, 9, 10, 11), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(3, 4, 5, 8, 9, 10, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(1, 2, 3, 4, 5, 8, 9, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(52, totalRuleBindus, "Shukra Ashtakavarga must total 52 Bindus")
    }

    @Test
    fun testShaniAshtakavargaClassicalContributionRules_ASTRO_R39G() {
        val rules = AshtakavargaCalculator.BAV_CONTRIBUTIONS[BodyId.SATURN]
        assertNotNull(rules)

        // BPHS Ch. 72: Saturn BAV contributions from 8 sources
        assertEquals(setOf(1, 2, 4, 7, 8, 10, 11), rules[AshtakavargaContributor.SUN])
        assertEquals(setOf(3, 6, 11), rules[AshtakavargaContributor.MOON])
        assertEquals(setOf(3, 5, 6, 10, 11, 12), rules[AshtakavargaContributor.MARS])
        assertEquals(setOf(6, 8, 9, 10, 11, 12), rules[AshtakavargaContributor.MERCURY])
        assertEquals(setOf(5, 6, 11, 12), rules[AshtakavargaContributor.JUPITER])
        assertEquals(setOf(6, 11, 12), rules[AshtakavargaContributor.VENUS])
        assertEquals(setOf(3, 5, 6, 11), rules[AshtakavargaContributor.SATURN])
        assertEquals(setOf(1, 3, 4, 6, 10, 11), rules[AshtakavargaContributor.LAGNA])

        val totalRuleBindus = rules.values.sumOf { it.size }
        assertEquals(39, totalRuleBindus, "Shani Ashtakavarga must total 39 Bindus")
    }

    @Test
    fun testSarvashtakavargaClassicalGrandTotalInvariant_ASTRO_R40() {
        val expectedGrandTotal = AshtakavargaCalculator.EXPECTED_BAV_TOTALS.values.sum()
        assertEquals(337, expectedGrandTotal, "Classical Sarvashtakavarga grand total must be 337")
        assertEquals(337, AshtakavargaCalculator.EXPECTED_SAV_TOTAL)
    }

    // =========================================================================
    // SECTION 2: Internal Consistency & Integration Tests
    // =========================================================================

    @Test
    fun testClassicalHoroscopeCalculationAndInvariants() {
        // Sample classical chart:
        // Sun in Aries (0), Moon in Taurus (1), Mars in Gemini (2), Mercury in Aries (0),
        // Jupiter in Cancer (3), Venus in Pisces (11), Saturn in Libra (6), Lagna in Aries (0).
        val positions = listOf(
            createDummyBodyPosition(BodyId.SUN, 0),
            createDummyBodyPosition(BodyId.MOON, 1),
            createDummyBodyPosition(BodyId.MARS, 2),
            createDummyBodyPosition(BodyId.MERCURY, 0),
            createDummyBodyPosition(BodyId.JUPITER, 3),
            createDummyBodyPosition(BodyId.VENUS, 11),
            createDummyBodyPosition(BodyId.SATURN, 6),
            createDummyBodyPosition(BodyId.RAHU, 4),
            createDummyBodyPosition(BodyId.KETU, 10),
        )
        val lagna = createDummyLagna(0)

        val result = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)

        assertEquals("PARASHARA_CLASSICAL_V1", result.rulesetId)
        assertEquals(AshtakavargaCompleteness.COMPLETE, result.completeness)
        assertEquals(listOf(BodyId.RAHU, BodyId.KETU), result.unsupportedBodies)

        // Verify BAV totals for each classical planet
        val expectedTotals = AshtakavargaCalculator.EXPECTED_BAV_TOTALS
        for ((body, expectedTotal) in expectedTotals) {
            val chart = result.bhinnashtakavarga[body]
            assertNotNull(chart, "Missing BAV chart for $body")
            assertEquals(12, chart.signScores.size)
            assertEquals(expectedTotal, chart.totalBindus, "Mismatch in total Bindus for $body")
            assertEquals(96 - expectedTotal, chart.totalRekhas, "Mismatch in total Rekhas for $body")

            // Check that bindus + rekhas == 8 in every sign
            for (score in chart.signScores) {
                assertEquals(8, score.binduCount + score.rekhaCount)
            }
        }

        // Verify SAV totals and invariants
        val sav = result.sarvashtakavarga
        assertEquals(12, sav.signScores.size)
        assertEquals(337, sav.grandTotalBindus)
        assertEquals(672 - 337, sav.grandTotalRekhas)
        assertTrue(sav.isInvariantValid)

        // Check that SAV sign total equals sum of BAV sign totals
        for (i in 0 until 12) {
            val savSign = sav.signScores[i]
            val sumBavForSign = expectedTotals.keys.sumOf { body ->
                result.bhinnashtakavarga[body]!!.signScores[i].binduCount
            }
            assertEquals(sumBavForSign, savSign.totalBindus)
            assertEquals(56 - savSign.totalBindus, savSign.totalRekhas)
        }
    }

    @Test
    fun testAllPlanetsInSameSignBoundaryCase() {
        // Boundary case: All 7 bodies and Lagna in Capricorn (9)
        val positions = listOf(
            createDummyBodyPosition(BodyId.SUN, 9),
            createDummyBodyPosition(BodyId.MOON, 9),
            createDummyBodyPosition(BodyId.MARS, 9),
            createDummyBodyPosition(BodyId.MERCURY, 9),
            createDummyBodyPosition(BodyId.JUPITER, 9),
            createDummyBodyPosition(BodyId.VENUS, 9),
            createDummyBodyPosition(BodyId.SATURN, 9),
        )
        val lagna = createDummyLagna(9)

        val result = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)

        assertEquals(AshtakavargaCompleteness.COMPLETE, result.completeness)
        assertEquals(337, result.sarvashtakavarga.grandTotalBindus)
        assertTrue(result.sarvashtakavarga.isInvariantValid)

        // All 7 BAV totals must strictly match classical constants
        assertEquals(48, result.bhinnashtakavarga[BodyId.SUN]?.totalBindus)
        assertEquals(49, result.bhinnashtakavarga[BodyId.MOON]?.totalBindus)
        assertEquals(39, result.bhinnashtakavarga[BodyId.MARS]?.totalBindus)
        assertEquals(54, result.bhinnashtakavarga[BodyId.MERCURY]?.totalBindus)
        assertEquals(56, result.bhinnashtakavarga[BodyId.JUPITER]?.totalBindus)
        assertEquals(52, result.bhinnashtakavarga[BodyId.VENUS]?.totalBindus)
        assertEquals(39, result.bhinnashtakavarga[BodyId.SATURN]?.totalBindus)
    }

    @Test
    fun testSignWrappingFromPiscesToAries() {
        // Contributor at sign index 11 (Pisces) contributing at relative house 2 -> index (11 + 1) % 12 = 0 (Aries)
        val contributorPositions = mapOf(
            AshtakavargaContributor.SUN to 11,
            AshtakavargaContributor.MOON to 11,
            AshtakavargaContributor.MARS to 11,
            AshtakavargaContributor.MERCURY to 11,
            AshtakavargaContributor.JUPITER to 11,
            AshtakavargaContributor.VENUS to 11,
            AshtakavargaContributor.SATURN to 11,
            AshtakavargaContributor.LAGNA to 11,
        )

        val sunBav = AshtakavargaCalculator.calculateBhinnashtakavarga(BodyId.SUN, contributorPositions)
        assertNotNull(sunBav)
        assertEquals(48, sunBav.totalBindus)
        assertEquals(12, sunBav.signScores.size)
    }

    @Test
    fun testRahuKetuUnsupportedPolicy_ASTRO_R41() {
        val contributorPositions = mapOf(
            AshtakavargaContributor.SUN to 0,
            AshtakavargaContributor.MOON to 1,
            AshtakavargaContributor.MARS to 2,
            AshtakavargaContributor.MERCURY to 3,
            AshtakavargaContributor.JUPITER to 4,
            AshtakavargaContributor.VENUS to 5,
            AshtakavargaContributor.SATURN to 6,
            AshtakavargaContributor.LAGNA to 0,
        )

        val rahuBav = AshtakavargaCalculator.calculateBhinnashtakavarga(BodyId.RAHU, contributorPositions)
        assertNull(rahuBav, "Rahu BAV must be unsupported and return null")

        val ketuBav = AshtakavargaCalculator.calculateBhinnashtakavarga(BodyId.KETU, contributorPositions)
        assertNull(ketuBav, "Ketu BAV must be unsupported and return null")
    }

    @Test
    fun testDeterminismAcrossRepeatedCalculations() {
        val positions = listOf(
            createDummyBodyPosition(BodyId.SUN, 2),
            createDummyBodyPosition(BodyId.MOON, 5),
            createDummyBodyPosition(BodyId.MARS, 8),
            createDummyBodyPosition(BodyId.MERCURY, 2),
            createDummyBodyPosition(BodyId.JUPITER, 10),
            createDummyBodyPosition(BodyId.VENUS, 1),
            createDummyBodyPosition(BodyId.SATURN, 4),
        )
        val lagna = createDummyLagna(7)

        val run1 = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)
        val run2 = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)

        assertEquals(run1.sarvashtakavarga.grandTotalBindus, run2.sarvashtakavarga.grandTotalBindus)
        assertEquals(run1.sarvashtakavarga.signScores, run2.sarvashtakavarga.signScores)
        assertEquals(run1.bhinnashtakavarga, run2.bhinnashtakavarga)
    }
}

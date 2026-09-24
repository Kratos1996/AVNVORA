package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AshtakavargaPindaTest {

    // -------------------------------------------------------------
    // 1. CONSTANTS VERIFICATION (BPHS Ch. 74/75, Raman Ch. 6)
    // -------------------------------------------------------------

    @Test
    fun testRasiGunakaraConstants_allTwelveSigns() {
        val expected = mapOf(
            0 to 7,   // Mesha
            1 to 10,  // Vrishabha
            2 to 8,   // Mithuna
            3 to 4,   // Karka
            4 to 10,  // Simha
            5 to 5,   // Kanya
            6 to 7,   // Tula
            7 to 8,   // Vrishchika
            8 to 9,   // Dhanus
            9 to 5,   // Makara
            10 to 11, // Kumbha
            11 to 12, // Meena
        )
        assertEquals(expected, AshtakavargaPindaCalculator.RASI_GUNAKARAS)
    }

    @Test
    fun testGrahaGunakaraConstants_allSevenPlanets() {
        val expected = mapOf(
            BodyId.SUN to 5,
            BodyId.MOON to 5,
            BodyId.MARS to 8,
            BodyId.MERCURY to 5,
            BodyId.JUPITER to 10,
            BodyId.VENUS to 7,
            BodyId.SATURN to 5,
        )
        assertEquals(expected, AshtakavargaPindaCalculator.GRAHA_GUNAKARAS)
    }

    // -------------------------------------------------------------
    // 2. INDEPENDENT CLASSICAL REFERENCE TEST (Raman Example Chart)
    // -------------------------------------------------------------

    @Test
    fun testIndependentReference_RamanSunPinda() {
        // Dr. B.V. Raman's *The Ashtakavarga System of Direction*, Ch. 2-6:
        // Lagna: Pisces (11)
        // Sun: Aries (0)
        // Moon: Taurus (1)
        // Mars: Capricorn (9)
        // Mercury: Aries (0)
        // Jupiter: Scorpio (7)
        // Venus: Pisces (11)
        // Saturn: Cancer (3)
        // Rahu: Sagittarius (8) - excluded
        // Ketu: Gemini (2) - excluded
        val lagna = LagnaPosition(
            tropicalLongitude = (11 * 30.0) + 10.0,
            siderealLongitude = (11 * 30.0) + 10.0,
            rashiIndex = 11,
            rashiName = "Pisces",
            degreeInRashi = 10.0,
            nakshatraIndex = 24,
            nakshatraName = "Shatabhisha",
            degreeInNakshatra = 3.0,
            pada = 1,
            obliquityDegrees = 23.44,
        )
        val positions = listOf(
            createBody(BodyId.SUN, 0),
            createBody(BodyId.MOON, 1),
            createBody(BodyId.MARS, 9),
            createBody(BodyId.MERCURY, 0),
            createBody(BodyId.JUPITER, 7),
            createBody(BodyId.VENUS, 11),
            createBody(BodyId.SATURN, 3),
            createBody(BodyId.RAHU, 8),
            createBody(BodyId.KETU, 2),
        )

        // 1. Raw calculation (Phase 6.1)
        val raw = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)

        // 2. Shodhana calculation (Phase 6.2)
        val shodhana = AshtakavargaShodhanaCalculator.calculateShodhana(raw, positions)

        // 3. Pinda calculation (Phase 6.3)
        val pindaResult = AshtakavargaPindaCalculator.calculatePindas(shodhana, positions)

        assertEquals(AshtakavargaCompleteness.COMPLETE, pindaResult.completeness)
        assertEquals(7, pindaResult.planetaryPindas.size)

        // Verify Sun's Pinda values:
        val sunPinda = pindaResult.planetaryPindas[BodyId.SUN]
        assertNotNull(sunPinda, "Sun Pinda must be present")

        // Exact independent classical verification against Raman's worked example:
        // Rasi Pinda for Sun = 114
        // Graha Pinda for Sun = 55
        // Shodhya Pinda for Sun = 114 + 55 = 169
        assertEquals(114, sunPinda.rasiPinda, "Sun Rasi Pinda must equal 114 per Raman Ch. 6")
        assertEquals(55, sunPinda.grahaPinda, "Sun Graha Pinda must equal 55 per Raman Ch. 6")
        assertEquals(169, sunPinda.shodhyaPinda, "Sun Shodhya Pinda must equal 169 per Raman Ch. 6")

        // Detailed Rasi Contributions verification:
        // Aries (0): 1 * 7 = 7
        // Taurus (1): 0 * 10 = 0
        // Gemini (2): 0 * 8 = 0
        // Cancer (3): 3 * 4 = 12
        // Leo (4): 2 * 10 = 20
        // Virgo (5): 1 * 5 = 5
        // Libra (6): 2 * 7 = 14
        // Scorpio (7): 0 * 8 = 0
        // Sagittarius (8): 0 * 9 = 0
        // Capricorn (9): 2 * 5 = 10
        // Aquarius (10): 2 * 11 = 22
        // Pisces (11): 2 * 12 = 24
        val expectedRasiContributions = mapOf(
            0 to 7,
            1 to 0,
            2 to 0,
            3 to 12,
            4 to 20,
            5 to 5,
            6 to 14,
            7 to 0,
            8 to 0,
            9 to 10,
            10 to 22,
            11 to 24,
        )
        assertEquals(expectedRasiContributions, sunPinda.rasiContributions)

        // Detailed Graha Contributions verification:
        // Sun in Aries (0): 1 * 5 = 5
        // Moon in Taurus (1): 0 * 5 = 0
        // Mars in Capricorn (9): 2 * 8 = 16
        // Mercury in Aries (0): 1 * 5 = 5
        // Jupiter in Scorpio (7): 0 * 10 = 0
        // Venus in Pisces (11): 2 * 7 = 14
        // Saturn in Cancer (3): 3 * 5 = 15
        val expectedGrahaContributions = mapOf(
            BodyId.SUN to 5,
            BodyId.MOON to 0,
            BodyId.MARS to 16,
            BodyId.MERCURY to 5,
            BodyId.JUPITER to 0,
            BodyId.VENUS to 14,
            BodyId.SATURN to 15,
        )
        assertEquals(expectedGrahaContributions, sunPinda.grahaContributions)

        // Invariant: sum of rasiContributions == rasiPinda
        assertEquals(sunPinda.rasiPinda, sunPinda.rasiContributions.values.sum())
        // Invariant: sum of grahaContributions == grahaPinda
        assertEquals(sunPinda.grahaPinda, sunPinda.grahaContributions.values.sum())
        // Invariant: shodhyaPinda == rasiPinda + grahaPinda
        assertEquals(sunPinda.shodhyaPinda, sunPinda.rasiPinda + sunPinda.grahaPinda)
    }

    // -------------------------------------------------------------
    // 3. BOUNDARY AND INTEGRITY TESTS
    // -------------------------------------------------------------

    @Test
    fun testZeroReducedBindus_yieldZeroPinda() {
        val dummyChart = ShodhitaBhinnashtakavargaChart(
            targetBody = BodyId.MARS,
            rulesetId = "PARASHARA_CLASSICAL_V1",
            signScores = (0 until 12).map {
                ShodhitaBhinnashtakavargaSignScore(
                    rashiIndex = it,
                    rashiName = "Rashi_$it",
                    rawBindus = 0,
                    trikonaReducedBindus = 0,
                    ekadhipatyaReducedBindus = 0,
                )
            },
            rawTotalBindus = 0,
            trikonaTotalBindus = 0,
            shodhitaTotalBindus = 0,
        )

        val planetPositions = mapOf(
            BodyId.SUN to 0,
            BodyId.MOON to 1,
            BodyId.MARS to 2,
            BodyId.MERCURY to 3,
            BodyId.JUPITER to 4,
            BodyId.VENUS to 5,
            BodyId.SATURN to 6,
        )

        val pinda = AshtakavargaPindaCalculator.calculatePlanetaryPinda(dummyChart, planetPositions)

        assertEquals(0, pinda.rasiPinda)
        assertEquals(0, pinda.grahaPinda)
        assertEquals(0, pinda.shodhyaPinda)
    }

    @Test
    fun testAllSevenPlanetsTotalPindaConsistency() {
        val lagna = LagnaPosition(
            tropicalLongitude = 15.0,
            siderealLongitude = 15.0,
            rashiIndex = 0,
            rashiName = "Aries",
            degreeInRashi = 15.0,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 15.0,
            pada = 1,
            obliquityDegrees = 23.44,
        )
        val positions = listOf(
            createBody(BodyId.SUN, 0),
            createBody(BodyId.MOON, 2),
            createBody(BodyId.MARS, 4),
            createBody(BodyId.MERCURY, 6),
            createBody(BodyId.JUPITER, 8),
            createBody(BodyId.VENUS, 10),
            createBody(BodyId.SATURN, 1),
        )

        val raw = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)
        val shodhana = AshtakavargaShodhanaCalculator.calculateShodhana(raw, positions)
        val pindaResult = AshtakavargaPindaCalculator.calculatePindas(shodhana, positions)

        assertEquals(7, pindaResult.planetaryPindas.size)

        val expectedRasiTotal = pindaResult.planetaryPindas.values.sumOf { it.rasiPinda }
        val expectedGrahaTotal = pindaResult.planetaryPindas.values.sumOf { it.grahaPinda }
        val expectedShodhyaTotal = pindaResult.planetaryPindas.values.sumOf { it.shodhyaPinda }

        assertEquals(expectedRasiTotal, pindaResult.totalRasiPinda)
        assertEquals(expectedGrahaTotal, pindaResult.totalGrahaPinda)
        assertEquals(expectedShodhyaTotal, pindaResult.totalShodhyaPinda)
        assertEquals(pindaResult.totalShodhyaPinda, pindaResult.totalRasiPinda + pindaResult.totalGrahaPinda)

        for ((_, pinda) in pindaResult.planetaryPindas) {
            assertTrue(pinda.rasiPinda >= 0, "Rasi Pinda must be non-negative")
            assertTrue(pinda.grahaPinda >= 0, "Graha Pinda must be non-negative")
            assertTrue(pinda.shodhyaPinda >= 0, "Shodhya Pinda must be non-negative")
            assertEquals(pinda.shodhyaPinda, pinda.rasiPinda + pinda.grahaPinda)
        }
    }

    @Test
    fun testImmutabilityOfShodhanaAndRawData() {
        val lagna = LagnaPosition(
            tropicalLongitude = 15.0,
            siderealLongitude = 15.0,
            rashiIndex = 0,
            rashiName = "Aries",
            degreeInRashi = 15.0,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 15.0,
            pada = 1,
            obliquityDegrees = 23.44,
        )
        val positions = listOf(
            createBody(BodyId.SUN, 0),
            createBody(BodyId.MOON, 1),
            createBody(BodyId.MARS, 2),
            createBody(BodyId.MERCURY, 3),
            createBody(BodyId.JUPITER, 4),
            createBody(BodyId.VENUS, 5),
            createBody(BodyId.SATURN, 6),
        )

        val raw = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)
        val shodhana = AshtakavargaShodhanaCalculator.calculateShodhana(raw, positions)

        val shodhitaBindusBefore = shodhana.shodhitaBhinnashtakavarga.mapValues { (_, c) ->
            c.signScores.map { it.shodhitaBindus }
        }

        val pindaResult = AshtakavargaPindaCalculator.calculatePindas(shodhana, positions)

        val shodhitaBindusAfter = shodhana.shodhitaBhinnashtakavarga.mapValues { (_, c) ->
            c.signScores.map { it.shodhitaBindus }
        }

        assertEquals(shodhitaBindusBefore, shodhitaBindusAfter, "Shodhita BAV must not be mutated by Pinda calculation")
        assertNotNull(pindaResult)
    }

    private fun createBody(bodyId: BodyId, rashiIndex: Int): BodyPosition = BodyPosition(
        bodyId = bodyId,
        tropicalLongitude = (rashiIndex * 30.0),
        siderealLongitude = (rashiIndex * 30.0),
        rashiIndex = rashiIndex,
        rashiName = when (rashiIndex) {
            0 -> "Aries"
            1 -> "Taurus"
            2 -> "Gemini"
            3 -> "Cancer"
            4 -> "Leo"
            5 -> "Virgo"
            6 -> "Libra"
            7 -> "Scorpio"
            8 -> "Sagittarius"
            9 -> "Capricorn"
            10 -> "Aquarius"
            11 -> "Pisces"
            else -> "Aries"
        },
        degreeInRashi = 10.0,
        nakshatraIndex = 0,
        nakshatraName = "Ashwini",
        degreeInNakshatra = 10.0,
        pada = 1,
        isRetrograde = false,
        dailyMotionDegrees = 1.0,
    )
}

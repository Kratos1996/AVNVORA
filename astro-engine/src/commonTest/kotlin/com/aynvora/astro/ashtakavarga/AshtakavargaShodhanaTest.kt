package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AshtakavargaShodhanaTest {

    // -------------------------------------------------------------
    // 1. TRIKONA SHODHANA TESTS (BPHS Ch. 73, Raman Ch. 4)
    // -------------------------------------------------------------

    @Test
    fun testTrikonaShodhana_allThreeEqualBecomeZero() {
        // Fire triplicity: Aries (0), Leo (4), Sagittarius (8) with equal values 4
        val raw = MutableList(12) { 0 }
        raw[0] = 4
        raw[4] = 4
        raw[8] = 4

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(0, reduced[0], "Aries equal value should reduce to 0")
        assertEquals(0, reduced[4], "Leo equal value should reduce to 0")
        assertEquals(0, reduced[8], "Sagittarius equal value should reduce to 0")
    }

    @Test
    fun testTrikonaShodhana_unequalNonZeroSubtractsMinimum() {
        // Earth triplicity: Taurus (1)=5, Virgo (5)=3, Capricorn (9)=2 -> min is 2
        // Expected: 5-2=3, 3-2=1, 2-2=0
        val raw = MutableList(12) { 0 }
        raw[1] = 5
        raw[5] = 3
        raw[9] = 2

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(3, reduced[1], "Taurus should be 5 - 2 = 3")
        assertEquals(1, reduced[5], "Virgo should be 3 - 2 = 1")
        assertEquals(0, reduced[9], "Capricorn should be 2 - 2 = 0")
    }

    @Test
    fun testTrikonaShodhana_twoEqualAndOneSmallerSubtractsMinimum() {
        // Air triplicity: Gemini (2)=4, Libra (6)=4, Aquarius (10)=2 -> min is 2
        // Expected: 4-2=2, 4-2=2, 2-2=0
        val raw = MutableList(12) { 0 }
        raw[2] = 4
        raw[6] = 4
        raw[10] = 2

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(2, reduced[2])
        assertEquals(2, reduced[6])
        assertEquals(0, reduced[10])
    }

    @Test
    fun testTrikonaShodhana_twoEqualAndOneLargerSubtractsMinimum() {
        // Water triplicity: Cancer (3)=5, Scorpio (7)=2, Pisces (11)=2 -> min is 2
        // Expected: 5-2=3, 2-2=0, 2-2=0
        val raw = MutableList(12) { 0 }
        raw[3] = 5
        raw[7] = 2
        raw[11] = 2

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(3, reduced[3])
        assertEquals(0, reduced[7])
        assertEquals(0, reduced[11])
    }

    @Test
    fun testTrikonaShodhana_oneZeroNoReduction() {
        // If exactly 1 sign has 0 bindus, no reduction is made in that triplicity
        // Fire triplicity: Aries (0)=0, Leo (4)=4, Sagittarius (8)=6
        val raw = MutableList(12) { 0 }
        raw[0] = 0
        raw[4] = 4
        raw[8] = 6

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(0, reduced[0], "Aries remains 0")
        assertEquals(4, reduced[4], "Leo remains 4 (no reduction when one is 0)")
        assertEquals(6, reduced[8], "Sagittarius remains 6 (no reduction when one is 0)")
    }

    @Test
    fun testTrikonaShodhana_twoZerosThirdBecomesZero() {
        // If exactly 2 signs have 0 bindus, the 3rd sign is also reduced to 0
        // Earth triplicity: Taurus (1)=0, Virgo (5)=5, Capricorn (9)=0
        val raw = MutableList(12) { 0 }
        raw[1] = 0
        raw[5] = 5
        raw[9] = 0

        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)

        assertEquals(0, reduced[1])
        assertEquals(0, reduced[5], "Virgo must be reduced to 0 because the other two signs have 0")
        assertEquals(0, reduced[9])
    }

    @Test
    fun testTrikonaShodhana_allThreeZerosRemainZero() {
        val raw = List(12) { 0 }
        val reduced = AshtakavargaShodhanaCalculator.applyTrikonaShodhana(raw)
        assertEquals(List(12) { 0 }, reduced)
    }

    // -------------------------------------------------------------
    // 2. EKADHIPATYA SHODHANA TESTS (BPHS Ch. 74, Raman Ch. 5)
    // -------------------------------------------------------------

    @Test
    fun testEkadhipatyaShodhana_zeroExemptionRule() {
        // Mars pair: Aries (0) and Scorpio (7)
        // If either has 0 bindus, no reduction is made
        val trikona = MutableList(12) { 0 }
        trikona[0] = 0
        trikona[7] = 4
        val occupied = emptySet<Int>()

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(0, reduced[0])
        assertEquals(4, reduced[7], "Scorpio retains 4 because Aries is 0")
    }

    @Test
    fun testEkadhipatyaShodhana_bothSignsOccupiedNoReduction() {
        // Venus pair: Taurus (1) and Libra (6)
        // Both occupied -> no reduction regardless of figures
        val trikona = MutableList(12) { 0 }
        trikona[1] = 5
        trikona[6] = 3
        val occupied = setOf(1, 6)

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(5, reduced[1], "Taurus retains 5 because both are occupied")
        assertEquals(3, reduced[6], "Libra retains 3 because both are occupied")
    }

    @Test
    fun testEkadhipatyaShodhana_bothUnoccupiedEqualFiguresBothZero() {
        // Mercury pair: Gemini (2) and Virgo (5)
        // Both unoccupied, both equal (4) -> both become 0
        val trikona = MutableList(12) { 0 }
        trikona[2] = 4
        trikona[5] = 4
        val occupied = emptySet<Int>()

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(0, reduced[2], "Gemini reduces to 0 (both unoccupied and equal)")
        assertEquals(0, reduced[5], "Virgo reduces to 0 (both unoccupied and equal)")
    }

    @Test
    fun testEkadhipatyaShodhana_bothUnoccupiedUnequalFiguresLargerBecomesSmaller() {
        // Jupiter pair: Sagittarius (8) and Pisces (11)
        // Both unoccupied, unequal: 8 has 5, 11 has 2 -> larger becomes smaller (both become 2)
        val trikona = MutableList(12) { 0 }
        trikona[8] = 5
        trikona[11] = 2
        val occupied = emptySet<Int>()

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(2, reduced[8], "Sagittarius reduces to smaller figure 2")
        assertEquals(2, reduced[11], "Pisces retains smaller figure 2")
    }

    @Test
    fun testEkadhipatyaShodhana_oneOccupiedOneUnoccupied_occupiedGreaterOrEqual() {
        // Saturn pair: Capricorn (9) and Aquarius (10)
        // Capricorn (9) is occupied with 4. Aquarius (10) is unoccupied with 3.
        // occ (4) >= unocc (3) -> unocc becomes 0, occ retains 4
        val trikona = MutableList(12) { 0 }
        trikona[9] = 4
        trikona[10] = 3
        val occupied = setOf(9)

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(4, reduced[9], "Occupied Capricorn retains 4")
        assertEquals(0, reduced[10], "Unoccupied Aquarius reduces to 0 since occ >= unocc")
    }

    @Test
    fun testEkadhipatyaShodhana_oneOccupiedOneUnoccupied_occupiedEqualToUnoccupied() {
        // Saturn pair: Capricorn (9) and Aquarius (10)
        // Capricorn (9) occupied with 3. Aquarius (10) unoccupied with 3.
        // occ (3) >= unocc (3) -> unocc becomes 0, occ retains 3
        val trikona = MutableList(12) { 0 }
        trikona[9] = 3
        trikona[10] = 3
        val occupied = setOf(9)

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(3, reduced[9], "Occupied Capricorn retains 3")
        assertEquals(0, reduced[10], "Unoccupied Aquarius reduces to 0 since occ == unocc")
    }

    @Test
    fun testEkadhipatyaShodhana_oneOccupiedOneUnoccupied_occupiedLessThanUnoccupied() {
        // Mars pair: Aries (0) and Scorpio (7)
        // Aries (0) occupied with 2. Scorpio (7) unoccupied with 5.
        // occ (2) < unocc (5) -> unocc is made equal to occ (2), occ retains 2
        val trikona = MutableList(12) { 0 }
        trikona[0] = 2
        trikona[7] = 5
        val occupied = setOf(0)

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(2, reduced[0], "Occupied Aries retains 2")
        assertEquals(2, reduced[7], "Unoccupied Scorpio reduces to occupied figure 2 since occ < unocc")
    }

    @Test
    fun testEkadhipatyaShodhana_cancerAndLeoExemptFromReduction() {
        // Moon (Cancer 3) and Sun (Leo 4) own only one house each and are exempt
        val trikona = MutableList(12) { 0 }
        trikona[3] = 6
        trikona[4] = 5
        val occupied = emptySet<Int>()

        val reduced = AshtakavargaShodhanaCalculator.applyEkadhipatyaShodhana(trikona, occupied)

        assertEquals(6, reduced[3], "Cancer must remain untouched in Ekadhipatya Shodhana")
        assertEquals(5, reduced[4], "Leo must remain untouched in Ekadhipatya Shodhana")
    }

    @Test
    fun testEkadhipatyaShodhana_rahuKetuDoNotCountAsOccupyingPlanets() {
        // Verify that only the 7 classical planets count as occupying planets
        val positions = listOf(
            createBody(BodyId.SUN, 0), // Aries
            createBody(BodyId.RAHU, 1), // Taurus (Rahu should NOT count)
            createBody(BodyId.KETU, 6), // Libra (Ketu should NOT count)
        )
        val occupied = AshtakavargaShodhanaCalculator.extractOccupiedSigns(positions)

        assertEquals(setOf(0), occupied, "Only Sun in Aries should be an occupied sign; Rahu/Ketu do not count")
    }

    // -------------------------------------------------------------
    // 3. CLASSICAL HOROSCOPE INTEGRATED REFERENCE TEST (Raman Example)
    // -------------------------------------------------------------

    @Test
    fun testClassicalHoroscopeReference_RamanExample() {
        // Standard Example Chart from Dr. B.V. Raman's *The Ashtakavarga System of Direction*, Ch. 2-5:
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

        // 1. Raw calculation (Phase 6.1 engine)
        val rawResult = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)
        assertEquals(337, rawResult.sarvashtakavarga.grandTotalBindus)
        assertTrue(rawResult.sarvashtakavarga.isInvariantValid)

        // 2. Shodhana calculation (Phase 6.2 engine)
        val shodhanaResult = AshtakavargaShodhanaCalculator.calculateShodhana(rawResult, positions)

        assertEquals(AshtakavargaCompleteness.COMPLETE, shodhanaResult.completeness)
        assertEquals(7, shodhanaResult.shodhitaBhinnashtakavarga.size)

        // Verify occupied signs in Raman chart: {0, 1, 3, 7, 9, 11}
        val occupied = AshtakavargaShodhanaCalculator.extractOccupiedSigns(positions)
        assertEquals(setOf(0, 1, 3, 7, 9, 11), occupied)

        // Verify Surya BAV reduction in Raman's chart:
        val suryaBav = rawResult.bhinnashtakavarga[BodyId.SUN]!!
        val suryaShodhita = shodhanaResult.shodhitaBhinnashtakavarga[BodyId.SUN]!!

        // Sun's raw bindus total must be 48
        assertEquals(48, suryaShodhita.rawTotalBindus)

        // Exact sign-by-sign verification of Surya BAV:
        // Raw: [4, 3, 2, 5, 5, 4, 4, 2, 3, 5, 7, 4]
        val expectedSuryaRaw = listOf(4, 3, 2, 5, 5, 4, 4, 2, 3, 5, 7, 4)
        assertEquals(expectedSuryaRaw, suryaShodhita.signScores.map { it.rawBindus })

        // Trikona reduced:
        // Fire (0,4,8): [4,5,3] -> min 3 -> [1,2,0]
        // Earth (1,5,9): [3,4,5] -> min 3 -> [0,1,2]
        // Air (2,6,10): [2,4,7] -> min 2 -> [0,2,5]
        // Water (3,7,11): [5,2,4] -> min 2 -> [3,0,2]
        // Result: [1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 5, 2]
        val expectedSuryaTrikona = listOf(1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 5, 2)
        assertEquals(expectedSuryaTrikona, suryaShodhita.signScores.map { it.trikonaReducedBindus })
        assertEquals(18, suryaShodhita.trikonaTotalBindus)

        // Shodhita (after Ekadhipatya):
        // Mars (0,7): Aries=1, Scorpio=0 -> zero exemption -> [1, 0]
        // Venus (1,6): Taurus=0, Libra=2 -> zero exemption -> [0, 2]
        // Mercury (2,5): Gemini=0, Virgo=1 -> zero exemption -> [0, 1]
        // Jupiter (8,11): Sagi=0, Pisces=2 -> zero exemption -> [0, 2]
        // Saturn (9,10): Cap(occ)=2, Aqua(unocc)=5 -> occ < unocc -> Aqua becomes 2 -> [2, 2]
        // Single lords (Cancer 3, Leo 4): exempt -> [3, 2]
        // Final: [1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 2, 2]
        val expectedSuryaShodhita = listOf(1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 2, 2)
        assertEquals(expectedSuryaShodhita, suryaShodhita.signScores.map { it.shodhitaBindus })
        assertEquals(15, suryaShodhita.shodhitaTotalBindus)

        // Monotonic reduction check for all signs in Sun's chart:
        // raw >= trikona >= ekadhipatya >= 0
        for (i in 0 until 12) {
            val score = suryaShodhita.signScores[i]
            assertTrue(
                score.rawBindus >= score.trikonaReducedBindus,
                "Sign $i: raw (${score.rawBindus}) must be >= trikona (${score.trikonaReducedBindus})"
            )
            assertTrue(
                score.trikonaReducedBindus >= score.ekadhipatyaReducedBindus,
                "Sign $i: trikona (${score.trikonaReducedBindus}) must be >= ekadhipatya (${score.ekadhipatyaReducedBindus})"
            )
            assertTrue(
                score.ekadhipatyaReducedBindus >= 0,
                "Sign $i: ekadhipatya must be non-negative"
            )
        }

        // Monotonic reduction check for ALL 7 planets and Shodhita SAV:
        for ((body, shodhitaChart) in shodhanaResult.shodhitaBhinnashtakavarga) {
            assertTrue(
                shodhitaChart.rawTotalBindus >= shodhitaChart.trikonaTotalBindus,
                "$body: raw total (${shodhitaChart.rawTotalBindus}) >= trikona total (${shodhitaChart.trikonaTotalBindus})"
            )
            assertTrue(
                shodhitaChart.trikonaTotalBindus >= shodhitaChart.shodhitaTotalBindus,
                "$body: trikona total (${shodhitaChart.trikonaTotalBindus}) >= shodhita total (${shodhitaChart.shodhitaTotalBindus})"
            )
            assertTrue(
                shodhitaChart.shodhitaTotalBindus >= 0,
                "$body: shodhita total must be non-negative"
            )
        }

        // Shodhita SAV consistency:
        val sav = shodhanaResult.shodhitaSarvashtakavarga
        assertEquals(337, sav.grandTotalRawBindus)
        assertTrue(sav.grandTotalRawBindus >= sav.grandTotalTrikonaBindus)
        assertTrue(sav.grandTotalTrikonaBindus >= sav.grandTotalShodhitaBindus)

        // Sign sum check: for each sign, Shodhita SAV must equal sum of 7 Shodhita BAVs
        for (sign in 0 until 12) {
            val expectedSignSum = shodhanaResult.shodhitaBhinnashtakavarga.values.sumOf {
                it.signScores[sign].shodhitaBindus
            }
            assertEquals(
                expectedSignSum,
                sav.signScores[sign].shodhitaTotalBindus,
                "Sign $sign Shodhita SAV must equal sum of 7 planets' shodhita bindus"
            )
        }
    }

    @Test
    fun testImmutabilityOfRawAshtakavarga() {
        // Ensure that running Shodhana does not mutate any underlying raw BAV data
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
            createBody(BodyId.MOON, 3),
            createBody(BodyId.MARS, 6),
            createBody(BodyId.MERCURY, 9),
            createBody(BodyId.JUPITER, 1),
            createBody(BodyId.VENUS, 4),
            createBody(BodyId.SATURN, 7),
        )

        val rawResult = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna)
        val rawCopyBefore = rawResult.bhinnashtakavarga.mapValues { (_, chart) ->
            chart.signScores.map { it.binduCount }
        }

        val shodhanaResult = AshtakavargaShodhanaCalculator.calculateShodhana(rawResult, positions)

        val rawAfter = rawResult.bhinnashtakavarga.mapValues { (_, chart) ->
            chart.signScores.map { it.binduCount }
        }

        assertEquals(rawCopyBefore, rawAfter, "Raw Ashtakavarga charts must not be mutated by Shodhana")
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

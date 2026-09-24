package com.aynvora.astro.varga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VargaEngineTest {

    private val engine = DefaultVargaEngine()

    private fun dummyBody(bodyId: BodyId, siderealLongitude: Double): BodyPosition {
        val sign = (siderealLongitude / 30.0).toInt().coerceIn(0, 11)
        val degInSign = siderealLongitude - (sign * 30.0)
        return BodyPosition(
            bodyId = bodyId,
            tropicalLongitude = siderealLongitude + 24.0,
            siderealLongitude = siderealLongitude,
            rashiIndex = sign,
            rashiName = "Sign_$sign",
            degreeInRashi = degInSign,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = false,
            dailyMotionDegrees = 1.0,
        )
    }

    private fun dummyLagna(siderealLongitude: Double): LagnaPosition {
        val sign = (siderealLongitude / 30.0).toInt().coerceIn(0, 11)
        val degInSign = siderealLongitude - (sign * 30.0)
        return LagnaPosition(
            tropicalLongitude = siderealLongitude + 24.0,
            siderealLongitude = siderealLongitude,
            rashiIndex = sign,
            rashiName = "Sign_$sign",
            degreeInRashi = degInSign,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
        )
    }


    // ==========================================
    // 1, 2, 3: Varga Registry & Metadata Tests
    // ==========================================

    @Test
    fun testVargaRegistryAllSixteenChartsRegistered() {
        val allMetadata = VargaRegistry.all()
        assertEquals(16, allMetadata.size)

        val charts = allMetadata.map { it.chart }.toSet()
        assertEquals(DivisionalChart.entries.toSet(), charts)

        DivisionalChart.entries.forEach { chart ->
            val meta = VargaRegistry.getMetadata(chart)
            assertEquals(chart, meta.chart)
            assertTrue(meta.divisionNumber > 0)
            assertTrue(meta.traditionalName.isNotBlank())
            assertTrue(meta.ruleId.startsWith("ASTRO-R"))
            assertTrue(meta.referenceSource.isNotBlank())
            assertTrue(meta.isSupported)
        }
    }

    // ==========================================
    // 4: D1 Baseline Rashi Chart
    // ==========================================

    @Test
    fun testD1BaselineRashi() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D1)
        // Sun at 14.5° Aries (sign 0) -> Rashi Aries (0)
        val resAries = strategy.calculate(14.5)
        assertEquals(0, resAries.sourceRashiIndex)
        assertEquals(0, resAries.divisionIndex)
        assertEquals(0, resAries.resultingRashiIndex)
        assertEquals(14.5, resAries.degreeInResultingRashi, 1e-6)
        assertEquals(14.5, resAries.resultingLongitude, 1e-6)

        // Moon at 45.2° (15.2° Taurus, sign 1) -> Rashi Taurus (1)
        val resTaurus = strategy.calculate(45.2)
        assertEquals(1, resTaurus.sourceRashiIndex)
        assertEquals(0, resTaurus.divisionIndex)
        assertEquals(1, resTaurus.resultingRashiIndex)
        assertEquals(15.2, resTaurus.degreeInResultingRashi, 1e-6)
        assertEquals(45.2, resTaurus.resultingLongitude, 1e-6)
    }

    // ==========================================
    // 5: D2 Hora Chart (BPHS Sun/Moon Rule)
    // ==========================================

    @Test
    fun testD2HoraOddAndEvenSigns() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D2)

        // Odd Sign: Aries (0)
        // 0° - 15°: Sun -> Leo (4)
        val oddFirst = strategy.calculate(5.0)
        assertEquals(0, oddFirst.sourceRashiIndex)
        assertEquals(0, oddFirst.divisionIndex)
        assertEquals(4, oddFirst.resultingRashiIndex) // Leo
        assertEquals(10.0, oddFirst.degreeInResultingRashi, 1e-6) // 5.0 * 2

        // 15° - 30°: Moon -> Cancer (3)
        val oddSecond = strategy.calculate(20.0)
        assertEquals(0, oddSecond.sourceRashiIndex)
        assertEquals(1, oddSecond.divisionIndex)
        assertEquals(3, oddSecond.resultingRashiIndex) // Cancer
        assertEquals(10.0, oddSecond.degreeInResultingRashi, 1e-6) // (20-15) * 2

        // Even Sign: Taurus (1)
        // 0° - 15°: Moon -> Cancer (3)
        val evenFirst = strategy.calculate(30.0 + 5.0)
        assertEquals(1, evenFirst.sourceRashiIndex)
        assertEquals(0, evenFirst.divisionIndex)
        assertEquals(3, evenFirst.resultingRashiIndex) // Cancer

        // 15° - 30°: Sun -> Leo (4)
        val evenSecond = strategy.calculate(30.0 + 20.0)
        assertEquals(1, evenSecond.sourceRashiIndex)
        assertEquals(1, evenSecond.divisionIndex)
        assertEquals(4, evenSecond.resultingRashiIndex) // Leo
    }

    // ==========================================
    // 6: D3 Drekkana Chart (BPHS 1st, 5th, 9th)
    // ==========================================

    @Test
    fun testD3DrekkanaAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D3)

        // Aries (0):
        // 0° - 10° -> 1st: Aries (0)
        val p1 = strategy.calculate(5.0)
        assertEquals(0, p1.resultingRashiIndex)
        // 10° - 20° -> 5th: Leo (4)
        val p2 = strategy.calculate(15.0)
        assertEquals(4, p2.resultingRashiIndex)
        // 20° - 30° -> 9th: Sagittarius (8)
        val p3 = strategy.calculate(25.0)
        assertEquals(8, p3.resultingRashiIndex)

        // Taurus (1):
        // 0° - 10° -> Taurus (1)
        val t1 = strategy.calculate(35.0)
        assertEquals(1, t1.resultingRashiIndex)
        // 10° - 20° -> Virgo (5)
        val t2 = strategy.calculate(45.0)
        assertEquals(5, t2.resultingRashiIndex)
        // 20° - 30° -> Capricorn (9)
        val t3 = strategy.calculate(55.0)
        assertEquals(9, t3.resultingRashiIndex)
    }

    // ==========================================
    // 7: D4 Chaturthamsa (BPHS Kendras)
    // ==========================================

    @Test
    fun testD4ChaturthamsaKendraAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D4)
        // Aries (0):
        // [0, 7.5) -> Aries (0)
        assertEquals(0, strategy.calculate(3.0).resultingRashiIndex)
        // [7.5, 15) -> Cancer (3)
        assertEquals(3, strategy.calculate(10.0).resultingRashiIndex)
        // [15, 22.5) -> Libra (6)
        assertEquals(6, strategy.calculate(18.0).resultingRashiIndex)
        // [22.5, 30) -> Capricorn (9)
        assertEquals(9, strategy.calculate(25.0).resultingRashiIndex)
    }

    // ==========================================
    // 8: D7 Saptamsa (BPHS Odd/Even)
    // ==========================================

    @Test
    fun testD7SaptamsaOddAndEvenRules() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D7)
        // Odd Sign (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(1.0).resultingRashiIndex)
        assertEquals(1, strategy.calculate(5.0).resultingRashiIndex) // division 1 -> Taurus

        // Even Sign (Taurus 1): starts 7th from it = Scorpio (7)
        assertEquals(7, strategy.calculate(31.0).resultingRashiIndex)
        assertEquals(8, strategy.calculate(35.0).resultingRashiIndex) // division 1 -> Sagittarius
    }

    // ==========================================
    // 9: D9 Navamsa (BPHS Triplicity)
    // ==========================================

    @Test
    fun testD9NavamsaTriplicityAllocationsAndContinuity() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D9)

        // Fiery: Aries (0) starts Aries (0)
        assertEquals(0, strategy.calculate(0.5).resultingRashiIndex)
        assertEquals(8, strategy.calculate(28.0).resultingRashiIndex) // 9th navamsa of Aries -> Sagittarius (8)

        // Earthy: Taurus (1) starts Capricorn (9)
        assertEquals(9, strategy.calculate(30.5).resultingRashiIndex)
        assertEquals(5, strategy.calculate(58.0).resultingRashiIndex) // 9th navamsa of Taurus -> Virgo (5)

        // Airy: Gemini (2) starts Libra (6)
        assertEquals(6, strategy.calculate(60.5).resultingRashiIndex)

        // Watery: Cancer (3) starts Cancer (3)
        assertEquals(3, strategy.calculate(90.5).resultingRashiIndex)

        // Navamsa cycle continuity: 108 continuous divisions
        for (i in 0 until 108) {
            val longitude = i * (40.0 / 12.0) + 0.1
            val res = strategy.calculate(longitude)
            assertEquals(i % 12, res.resultingRashiIndex, "Continuous navamsa mismatch at step $i")
        }
    }

    // ==========================================
    // 10: D10 Dasamsa (BPHS Odd/Even)
    // ==========================================

    @Test
    fun testD10DasamsaOddAndEvenRules() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D10)
        // Odd sign: Aries (0) starts Aries (0)
        assertEquals(0, strategy.calculate(1.5).resultingRashiIndex)
        assertEquals(1, strategy.calculate(4.5).resultingRashiIndex)

        // Even sign: Taurus (1) starts 9th from Taurus = Capricorn (9)
        assertEquals(9, strategy.calculate(31.5).resultingRashiIndex)
        assertEquals(10, strategy.calculate(34.5).resultingRashiIndex)
    }

    // ==========================================
    // 11: D12 Dwadasamsa (BPHS Direct Progression)
    // ==========================================

    @Test
    fun testD12DwadasamsaDirectProgression() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D12)
        // Aries (0):
        for (k in 0 until 12) {
            val deg = k * 2.5 + 0.5
            val res = strategy.calculate(deg)
            assertEquals(k, res.resultingRashiIndex)
        }
        // Taurus (1):
        for (k in 0 until 12) {
            val deg = 30.0 + k * 2.5 + 0.5
            val res = strategy.calculate(deg)
            assertEquals((1 + k) % 12, res.resultingRashiIndex)
        }
    }

    // ==========================================
    // 12: D16 Shodasamsa (BPHS Mobility)
    // ==========================================

    @Test
    fun testD16ShodasamsaMobilityAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D16)
        // Movable (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(0.5).resultingRashiIndex)
        // Fixed (Taurus 1): starts Leo (4)
        assertEquals(4, strategy.calculate(30.5).resultingRashiIndex)
        // Dual (Gemini 2): starts Sagittarius (8)
        assertEquals(8, strategy.calculate(60.5).resultingRashiIndex)
    }

    // ==========================================
    // 13: D20 Vimsamsa (BPHS Mobility)
    // ==========================================

    @Test
    fun testD20VimsamsaMobilityAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D20)
        // Movable (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(0.5).resultingRashiIndex)
        // Fixed (Taurus 1): starts Sagittarius (8)
        assertEquals(8, strategy.calculate(30.5).resultingRashiIndex)
        // Dual (Gemini 2): starts Leo (4)
        assertEquals(4, strategy.calculate(60.5).resultingRashiIndex)
    }

    // ==========================================
    // 14: D24 Chaturvimsamsa (BPHS Odd/Even)
    // ==========================================

    @Test
    fun testD24ChaturvimsamsaOddAndEvenRules() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D24)
        // Odd sign (Aries 0): starts Leo (4)
        assertEquals(4, strategy.calculate(0.5).resultingRashiIndex)
        // Even sign (Taurus 1): starts Cancer (3)
        assertEquals(3, strategy.calculate(30.5).resultingRashiIndex)
    }

    // ==========================================
    // 15: D27 Bhamsa (BPHS Triplicity)
    // ==========================================

    @Test
    fun testD27BhamsaTriplicityAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D27)
        // Fiery (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(0.5).resultingRashiIndex)
        // Earthy (Taurus 1): starts Cancer (3)
        assertEquals(3, strategy.calculate(30.5).resultingRashiIndex)
        // Airy (Gemini 2): starts Libra (6)
        assertEquals(6, strategy.calculate(60.5).resultingRashiIndex)
        // Watery (Cancer 3): starts Capricorn (9)
        assertEquals(9, strategy.calculate(90.5).resultingRashiIndex)
    }

    // ==========================================
    // 16: D30 Trimsamsa (BPHS Unequal Portions)
    // ==========================================

    @Test
    fun testD30TrimsamsaUnequalPlanetaryPortions() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D30)

        // Odd sign: Aries (0)
        // [0, 5) -> Mars / Aries (0)
        val o1 = strategy.calculate(3.0)
        assertEquals(0, o1.resultingRashiIndex)
        assertEquals(0, o1.divisionIndex)

        // [5, 10) -> Saturn / Aquarius (10)
        val o2 = strategy.calculate(7.0)
        assertEquals(10, o2.resultingRashiIndex)
        assertEquals(1, o2.divisionIndex)

        // [10, 18) -> Jupiter / Sagittarius (8)
        val o3 = strategy.calculate(14.0)
        assertEquals(8, o3.resultingRashiIndex)
        assertEquals(2, o3.divisionIndex)

        // [18, 25) -> Mercury / Gemini (2)
        val o4 = strategy.calculate(21.0)
        assertEquals(2, o4.resultingRashiIndex)
        assertEquals(3, o4.divisionIndex)

        // [25, 30) -> Venus / Libra (6)
        val o5 = strategy.calculate(28.0)
        assertEquals(6, o5.resultingRashiIndex)
        assertEquals(4, o5.divisionIndex)

        // Even sign: Taurus (1)
        // [0, 5) -> Venus / Taurus (1)
        val e1 = strategy.calculate(33.0)
        assertEquals(1, e1.resultingRashiIndex)
        assertEquals(0, e1.divisionIndex)

        // [5, 12) -> Mercury / Virgo (5)
        val e2 = strategy.calculate(38.0)
        assertEquals(5, e2.resultingRashiIndex)
        assertEquals(1, e2.divisionIndex)

        // [12, 20) -> Jupiter / Pisces (11)
        val e3 = strategy.calculate(46.0)
        assertEquals(11, e3.resultingRashiIndex)
        assertEquals(2, e3.divisionIndex)

        // [20, 25) -> Saturn / Capricorn (9)
        val e4 = strategy.calculate(52.0)
        assertEquals(9, e4.resultingRashiIndex)
        assertEquals(3, e4.divisionIndex)

        // [25, 30) -> Mars / Scorpio (7)
        val e5 = strategy.calculate(58.0)
        assertEquals(7, e5.resultingRashiIndex)
        assertEquals(4, e5.divisionIndex)
    }

    // ==========================================
    // 17: D40 Khavedamsa (BPHS Odd/Even)
    // ==========================================

    @Test
    fun testD40KhavedamsaOddAndEvenRules() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D40)
        // Odd (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(0.2).resultingRashiIndex)
        // Even (Taurus 1): starts Libra (6)
        assertEquals(6, strategy.calculate(30.2).resultingRashiIndex)
    }

    // ==========================================
    // 18: D45 Akshavedamsa (BPHS Mobility)
    // ==========================================

    @Test
    fun testD45AkshavedamsaMobilityAllocations() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D45)
        // Movable (Aries 0): starts Aries (0)
        assertEquals(0, strategy.calculate(0.2).resultingRashiIndex)
        // Fixed (Taurus 1): starts Leo (4)
        assertEquals(4, strategy.calculate(30.2).resultingRashiIndex)
        // Dual (Gemini 2): starts Sagittarius (8)
        assertEquals(8, strategy.calculate(60.2).resultingRashiIndex)
    }

    // ==========================================
    // 19: D60 Shashtiamsa (High Precision Boundary Sensitivity)
    // ==========================================

    @Test
    fun testD60ShashtiamsaHighBoundarySensitivity() {
        val strategy = VargaStrategyRegistry.getStrategy(DivisionalChart.D60)

        // 60 parts of 0.5° each in Aries (0)
        val firstPart = strategy.calculate(0.25)
        assertEquals(0, firstPart.sourceRashiIndex)
        assertEquals(0, firstPart.divisionIndex)
        assertEquals(0, firstPart.resultingRashiIndex)

        val secondPart = strategy.calculate(0.75)
        assertEquals(0, secondPart.sourceRashiIndex)
        assertEquals(1, secondPart.divisionIndex)
        assertEquals(1, secondPart.resultingRashiIndex)

        val lastPart = strategy.calculate(29.75)
        assertEquals(0, lastPart.sourceRashiIndex)
        assertEquals(59, lastPart.divisionIndex)
        assertEquals(11, lastPart.resultingRashiIndex) // (0 + 59) % 12 = 11 (Pisces)

        // In Taurus (1)
        val taurusFirst = strategy.calculate(30.25)
        assertEquals(1, taurusFirst.sourceRashiIndex)
        assertEquals(0, taurusFirst.divisionIndex)
        assertEquals(1, taurusFirst.resultingRashiIndex) // (1 + 0) % 12 = 1 (Taurus)
    }

    // ==========================================
    // 20-27: Boundary Handling & Precision
    // ==========================================

    @Test
    fun testBoundaryHandlingZeroDegrees() {
        DivisionalChart.entries.forEach { chart ->
            val strategy = VargaStrategyRegistry.getStrategy(chart)
            val res = strategy.calculate(0.0)
            assertEquals(0, res.sourceRashiIndex, "Failed 0° source sign for $chart")
            assertEquals(0, res.divisionIndex, "Failed 0° division index for $chart")
            assertTrue(res.resultingRashiIndex in 0..11, "Failed 0° resulting sign range for $chart")
            assertEquals(0.0, res.degreeInResultingRashi, 1e-6, "Failed 0° degree in resulting sign for $chart")
        }
    }

    @Test
    fun testBoundaryHandlingExactDivisionBoundaries() {
        val d9 = VargaStrategyRegistry.getStrategy(DivisionalChart.D9)
        // Navamsa span = 3° 20' = 10.0 / 3.0 = 3.3333333333333335
        val exactBoundary = 10.0 / 3.0
        val res = d9.calculate(exactBoundary)
        // At exact boundary, [start, end) convention advances to division 1
        assertEquals(1, res.divisionIndex)
        assertEquals(1, res.resultingRashiIndex) // Taurus
    }

    @Test
    fun testBoundaryHandlingImmediatelyBeforeAndAfter() {
        val d3 = VargaStrategyRegistry.getStrategy(DivisionalChart.D3)
        // D3 boundary is at 10.0°
        val before = d3.calculate(9.999999)
        assertEquals(0, before.divisionIndex)
        assertEquals(0, before.resultingRashiIndex)

        val after = d3.calculate(10.000001)
        assertEquals(1, after.divisionIndex)
        assertEquals(4, after.resultingRashiIndex) // 5th from Aries = Leo (4)
    }

    @Test
    fun testBoundaryHandlingNearSignBoundary29_999999() {
        DivisionalChart.entries.forEach { chart ->
            val strategy = VargaStrategyRegistry.getStrategy(chart)
            val res = strategy.calculate(29.999999)
            assertEquals(0, res.sourceRashiIndex, "Failed near-30° source sign for $chart")
            val meta = VargaRegistry.getMetadata(chart)
            val expectedMaxDiv = if (chart == DivisionalChart.D30) 4 else meta.divisionNumber - 1
            assertEquals(expectedMaxDiv, res.divisionIndex, "Failed near-30° division index for $chart")
            assertTrue(res.resultingRashiIndex in 0..11, "Failed resulting sign for $chart")
        }
    }

    @Test
    fun testBoundaryHandling360DegreeNormalizationAndNegative() {
        val d9 = VargaStrategyRegistry.getStrategy(DivisionalChart.D9)
        val res360 = d9.calculate(360.0)
        assertEquals(0, res360.sourceRashiIndex)
        assertEquals(0, res360.resultingRashiIndex)

        val res720Plus15 = d9.calculate(720.0 + 15.0)
        val res15 = d9.calculate(15.0)
        assertEquals(res15.resultingRashiIndex, res720Plus15.resultingRashiIndex)
        assertEquals(res15.resultingLongitude, res720Plus15.resultingLongitude, 1e-6)

        val resNeg = d9.calculate(-345.0) // -345° == +15°
        assertEquals(res15.resultingRashiIndex, resNeg.resultingRashiIndex)
    }

    // ==========================================
    // 28: Rahu / Ketu Behavior
    // ==========================================

    @Test
    fun testRahuKetuOppositionsInVargas() {
        val rahuLong = 45.0 // 15° Taurus
        val ketuLong = 225.0 // 15° Scorpio (exact 180° opposition)

        val bodies = listOf(
            dummyBody(BodyId.RAHU, rahuLong),
            dummyBody(BodyId.KETU, ketuLong),
        )

        // In D1, Rahu is Taurus (1) and Ketu is Scorpio (7) -> 6 signs apart (180°)
        val d1Result = engine.calculate(bodies, null, DivisionalChart.D1)
        val rahuD1 = d1Result.positions.first { it.bodyId == BodyId.RAHU }
        val ketuD1 = d1Result.positions.first { it.bodyId == BodyId.KETU }
        assertEquals(1, rahuD1.resultingRashiIndex)
        assertEquals(7, ketuD1.resultingRashiIndex)
        assertEquals(6, abs(rahuD1.resultingRashiIndex - ketuD1.resultingRashiIndex))

        // In D9 (Navamsa): 180° in D1 corresponds to exactly 180° in D9 (6 signs apart)
        val d9Result = engine.calculate(bodies, null, DivisionalChart.D9)
        val rahuD9 = d9Result.positions.first { it.bodyId == BodyId.RAHU }
        val ketuD9 = d9Result.positions.first { it.bodyId == BodyId.KETU }
        val signDiff = abs(rahuD9.resultingRashiIndex - ketuD9.resultingRashiIndex)
        assertEquals(6, signDiff, "Rahu and Ketu must maintain 180° (6 sign) separation in Navamsa")
    }

    // ==========================================
    // 29: Lagna Divisional Position
    // ==========================================

    @Test
    fun testLagnaDivisionalPositionCalculation() {
        val lagna = dummyLagna(10.0) // 10° Aries
        val bodies = listOf(dummyBody(BodyId.SUN, 5.0))

        val d9Result = engine.calculate(bodies, lagna, DivisionalChart.D9)
        assertNotNull(d9Result.lagnaPosition)
        assertTrue(d9Result.lagnaPosition.isLagna)
        assertNull(d9Result.lagnaPosition.bodyId)
        assertEquals(10.0, d9Result.lagnaPosition.sourceLongitude)
        // 10.0° Aries is exact boundary of Navamsa 3 (4th part) -> Cancer (3)
        assertEquals(3, d9Result.lagnaPosition.resultingRashiIndex)
    }

    // ==========================================
    // 30: Deterministic Repeatability
    // ==========================================

    @Test
    fun testDeterministicRepeatability() {
        val bodies = listOf(
            dummyBody(BodyId.SUN, 23.456),
            dummyBody(BodyId.MOON, 187.654),
            dummyBody(BodyId.MARS, 305.123),
        )
        val lagna = dummyLagna(72.89)

        val run1 = engine.calculateMultiple(bodies, lagna, DivisionalChart.entries.toSet())
        val run2 = engine.calculateMultiple(bodies, lagna, DivisionalChart.entries.toSet())

        assertEquals(run1.size, run2.size)
        run1.forEach { (chart, chartResult1) ->
            val chartResult2 = run2[chart]
            assertNotNull(chartResult2)
            assertEquals(chartResult1.rulesetId, chartResult2.rulesetId)
            assertEquals(chartResult1.lagnaPosition, chartResult2.lagnaPosition)
            assertEquals(chartResult1.positions, chartResult2.positions)
        }
    }

    // ==========================================
    // 32: Unsupported Rule & Profile Behavior
    // ==========================================

    @Test
    fun testUnsupportedChartOrProfileRejection() {
        val customProfile = VargaProfile(
            rulesetId = "CUSTOM_RESTRICTED",
            supportedCharts = setOf(DivisionalChart.D1, DivisionalChart.D9),
        )

        val bodies = listOf(dummyBody(BodyId.SUN, 10.0))

        // D1 and D9 are supported
        val d9Res = engine.calculate(bodies, null, DivisionalChart.D9, customProfile)
        assertEquals(DivisionalChart.D9, d9Res.chart)

        // D60 is unsupported in this profile
        val ex = assertFailsWith<UnsupportedOperationException> {
            engine.calculate(bodies, null, DivisionalChart.D60, customProfile)
        }
        assertTrue(ex.message!!.contains("Divisional chart D60 is not supported"))
    }

    // ==========================================
    // Property Invariant Tests
    // ==========================================

    @Test
    fun testPropertyResultingRashiAlwaysValidRange() {
        val testLongitudes = listOf(0.0, 0.0001, 15.0, 29.9999, 30.0, 89.99, 180.0, 270.0, 359.999)
        DivisionalChart.entries.forEach { chart ->
            val strategy = VargaStrategyRegistry.getStrategy(chart)
            testLongitudes.forEach { lon ->
                val res = strategy.calculate(lon)
                assertTrue(res.resultingRashiIndex in 0..11, "Sign out of range for $chart at $lon")
                assertTrue(res.degreeInResultingRashi in 0.0..30.0, "Degree out of range for $chart at $lon")
                assertTrue(res.resultingLongitude in 0.0..<360.0, "Longitude out of range for $chart at $lon")
            }
        }
    }

    @Test
    fun testMultipleVargaCalculationEfficiency() {
        val bodies = listOf(dummyBody(BodyId.SUN, 12.0), dummyBody(BodyId.MOON, 88.0))
        val lagna = dummyLagna(45.0)

        // Calculate a subset of 3 charts
        val subset = setOf(DivisionalChart.D1, DivisionalChart.D9, DivisionalChart.D10)
        val result = engine.calculateMultiple(bodies, lagna, subset)

        assertEquals(3, result.size)
        assertTrue(result.containsKey(DivisionalChart.D1))
        assertTrue(result.containsKey(DivisionalChart.D9))
        assertTrue(result.containsKey(DivisionalChart.D10))
    }
}

package com.aynvora.astro.shadbala

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.varga.DefaultVargaEngine
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShadbalaCalculatorTest {

    private fun dummyBody(
        bodyId: BodyId,
        rashiIndex: Int,
        degreeInRashi: Double,
        isRetrograde: Boolean = false,
        dailyMotion: Double = 1.0,
    ): BodyPosition {
        val sidereal = rashiIndex * 30.0 + degreeInRashi
        return BodyPosition(
            bodyId = bodyId,
            tropicalLongitude = (sidereal + 24.0) % 360.0,
            siderealLongitude = sidereal,
            rashiIndex = rashiIndex,
            rashiName = "Sign_$rashiIndex",
            degreeInRashi = degreeInRashi,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = isRetrograde,
            dailyMotionDegrees = dailyMotion,
        )
    }

    private fun defaultCuspsMap(): Map<Int, Double> {
        // Equal houses from 0.0 (Lagna = 0.0)
        return (1..12).associateWith { (it - 1) * 30.0 }
    }

    // =========================================================================
    // 1. NAISARGIKA BALA (ASTRO-R32, BPHS Ch. 28, slokas 13-14) - Reference Tests
    // =========================================================================

    @Test
    fun testNaisargikaBala_classicalReferenceValuesAndRanking() {
        val expected = mapOf(
            BodyId.SUN to Pair(60.0, 1),
            BodyId.MOON to Pair(60.0 * 6.0 / 7.0, 2),
            BodyId.VENUS to Pair(60.0 * 5.0 / 7.0, 3),
            BodyId.JUPITER to Pair(60.0 * 4.0 / 7.0, 4),
            BodyId.MERCURY to Pair(60.0 * 3.0 / 7.0, 5),
            BodyId.MARS to Pair(60.0 * 2.0 / 7.0, 6),
            BodyId.SATURN to Pair(60.0 * 1.0 / 7.0, 7),
        )

        var sumVirupas = 0.0
        for ((bodyId, pair) in expected) {
            val (expectedVirupas, expectedRank) = pair
            val result = ShadbalaCalculator.calculateNaisargikaBala(bodyId)

            assertTrue(result.isEvaluated)
            assertEquals(expectedRank, result.rank, "Rank mismatch for $bodyId")
            assertTrue(
                abs(result.virupas - expectedVirupas) < 1e-9,
                "Virupas mismatch for $bodyId: expected $expectedVirupas, got ${result.virupas}"
            )
            assertTrue(
                abs(result.rupas - (expectedVirupas / 60.0)) < 1e-9,
                "Rupas mismatch for $bodyId"
            )
            sumVirupas += result.virupas
        }

        assertEquals(240.0, sumVirupas, 1e-9)
    }

    @Test
    fun testNaisargikaBala_unsupportedBodies() {
        val rahu = ShadbalaCalculator.calculateNaisargikaBala(BodyId.RAHU)
        assertFalse(rahu.isEvaluated)
        assertEquals(0.0, rahu.virupas)
        assertEquals(0.0, rahu.rupas)
        assertEquals(0, rahu.rank)

        val ketu = ShadbalaCalculator.calculateNaisargikaBala(BodyId.KETU)
        assertFalse(ketu.isEvaluated)
        assertEquals(0.0, ketu.virupas)
        assertEquals(0.0, ketu.rupas)
        assertEquals(0, ketu.rank)
    }

    // =========================================================================
    // 2. DIG BALA (ASTRO-R33, BPHS Ch. 28, slokas 7-8) - Reference & Edge Tests
    // =========================================================================

    @Test
    fun testDigBala_exactPowerfulPointsYieldMaxStrength() {
        val cusps = (1..12).associateWith { ((it - 1) * 30.0 + 15.0) % 360.0 }
        val cusp1 = cusps[1]!!   // 15.0 - East (Jupiter, Mercury)
        val cusp4 = cusps[4]!!   // 105.0 - North (Venus, Moon)
        val cusp7 = cusps[7]!!   // 195.0 - West (Saturn)
        val cusp10 = cusps[10]!! // 285.0 - South (Sun, Mars)

        val cases = listOf(
            Pair(BodyId.MERCURY, cusp1),
            Pair(BodyId.JUPITER, cusp1),
            Pair(BodyId.SUN, cusp10),
            Pair(BodyId.MARS, cusp10),
            Pair(BodyId.SATURN, cusp7),
            Pair(BodyId.MOON, cusp4),
            Pair(BodyId.VENUS, cusp4),
        )

        for ((bodyId, longitude) in cases) {
            val result = ShadbalaCalculator.calculateDigBala(bodyId, longitude, cusps)
            assertTrue(result.isEvaluated)
            assertEquals(180.0, result.arcDegrees, 1e-9, "Arc from zero point must be 180 deg")
            assertEquals(60.0, result.virupas, 1e-9, "Dig Bala at powerful point must be 60 Virupas for $bodyId")
            assertEquals(1.0, result.rupas, 1e-9, "Dig Bala at powerful point must be 1 Rupa for $bodyId")
        }
    }

    @Test
    fun testDigBala_exactZeroPointsYieldZeroStrength() {
        val cusps = (1..12).associateWith { (it - 1) * 30.0 }
        val zeroCases = listOf(
            Pair(BodyId.MERCURY, 180.0),
            Pair(BodyId.JUPITER, 180.0),
            Pair(BodyId.SUN, 90.0),
            Pair(BodyId.MARS, 90.0),
            Pair(BodyId.SATURN, 0.0),
            Pair(BodyId.MOON, 270.0),
            Pair(BodyId.VENUS, 270.0),
        )

        for ((bodyId, longitude) in zeroCases) {
            val result = ShadbalaCalculator.calculateDigBala(bodyId, longitude, cusps)
            assertTrue(result.isEvaluated)
            assertEquals(0.0, result.arcDegrees, 1e-9, "Arc from zero point must be 0 deg")
            assertEquals(0.0, result.virupas, 1e-9, "Dig Bala at zero point must be 0 Virupas for $bodyId")
            assertEquals(0.0, result.rupas, 1e-9, "Dig Bala at zero point must be 0 Rupas for $bodyId")
        }
    }

    // =========================================================================
    // 3. UCHCHA BALA (ASTRO-R34A, BPHS Ch. 28, sloka 2) - Reference Tests
    // =========================================================================

    @Test
    fun testUchchaBala_atParamaUchchaYieldsSixtyVirupas() {
        val deepExaltationDegrees = mapOf(
            BodyId.SUN to 10.0,
            BodyId.MOON to 33.0,
            BodyId.MARS to 298.0,
            BodyId.MERCURY to 165.0,
            BodyId.JUPITER to 95.0,
            BodyId.VENUS to 357.0,
            BodyId.SATURN to 200.0,
        )

        for ((bodyId, deg) in deepExaltationDegrees) {
            val virupas = ShadbalaCalculator.calculateUchchaBala(bodyId, deg)
            assertEquals(60.0, virupas, 1e-9, "Uchcha Bala at deep exaltation must be 60.0 for $bodyId")
        }
    }

    @Test
    fun testUchchaBala_atParamaNeechaYieldsZeroVirupas() {
        val deepDebilitationDegrees = mapOf(
            BodyId.SUN to 190.0,
            BodyId.MOON to 213.0,
            BodyId.MARS to 118.0,
            BodyId.MERCURY to 345.0,
            BodyId.JUPITER to 275.0,
            BodyId.VENUS to 177.0,
            BodyId.SATURN to 20.0,
        )

        for ((bodyId, deg) in deepDebilitationDegrees) {
            val virupas = ShadbalaCalculator.calculateUchchaBala(bodyId, deg)
            assertEquals(0.0, virupas, 1e-9, "Uchcha Bala at deep debilitation must be 0.0 for $bodyId")
        }
    }

    // =========================================================================
    // 4. KALA BALA SUBCOMPONENTS (ASTRO-R36, BPHS Ch. 28, slokas 14-18)
    // =========================================================================

    @Test
    fun testNathonnathaBala_middayAndMidnightExtremes() {
        val fourthCusp = 90.0 // Nadir / Midnight
        // At midday: Sun is at 270.0 (180° away from midnight)
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.SUN, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.JUPITER, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.VENUS, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MOON, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MARS, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.SATURN, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MERCURY, sunLongitude = 270.0, fourthHouseCusp = fourthCusp))

        // At midnight: Sun is at 90.0 (exact 4th cusp)
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.SUN, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.JUPITER, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(0.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.VENUS, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MOON, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MARS, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.SATURN, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
        assertEquals(60.0, ShadbalaCalculator.calculateNathonnathaBala(BodyId.MERCURY, sunLongitude = 90.0, fourthHouseCusp = fourthCusp))
    }

    @Test
    fun testTribhagaBala_allPortions() {
        // Jupiter always gets 60.0
        for (h in 1..12) {
            assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.JUPITER, h))
        }

        // Day portions:
        // H12 (morning) -> Mercury gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.MERCURY, 12))
        assertEquals(0.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.SUN, 12))

        // H10 (midday) -> Sun gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.SUN, 10))
        assertEquals(0.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.MERCURY, 10))

        // H8 (afternoon) -> Saturn gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.SATURN, 8))
        assertEquals(0.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.SUN, 8))

        // Night portions:
        // H6 (evening) -> Moon gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.MOON, 6))

        // H4 (midnight) -> Venus gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.VENUS, 4))

        // H2 (pre-dawn) -> Mars gets 60
        assertEquals(60.0, ShadbalaCalculator.calculateTribhagaBala(BodyId.MARS, 2))
    }

    @Test
    fun testVaraBala_weekdayRulers() {
        // J2000.0 is Jan 1, 2000 (Saturday) -> JD = 2451545.0
        // (floor(2451545.0 + 1.5) % 7) = 2451546 % 7 = 6 (Saturday -> Saturn)
        assertEquals(45.0, ShadbalaCalculator.calculateVaraBala(BodyId.SATURN, 2451545.0))
        assertEquals(0.0, ShadbalaCalculator.calculateVaraBala(BodyId.SUN, 2451545.0))
        assertEquals(0.0, ShadbalaCalculator.calculateVaraBala(BodyId.MOON, 2451545.0))

        // Sunday (JD + 1 day) = 2451546.0 -> Sun gets 45.0
        assertEquals(45.0, ShadbalaCalculator.calculateVaraBala(BodyId.SUN, 2451546.0))
        assertEquals(0.0, ShadbalaCalculator.calculateVaraBala(BodyId.SATURN, 2451546.0))
    }

    @Test
    fun testHoraBala_chaldeanOrder() {
        // On Saturday (Saturn day), 0th hora is Saturn (gets 60.0)
        assertEquals(60.0, ShadbalaCalculator.calculateHoraBala(BodyId.SATURN, 2451545.0, 0))
        assertEquals(0.0, ShadbalaCalculator.calculateHoraBala(BodyId.JUPITER, 2451545.0, 0))

        // 1st hora is Jupiter (gets 60.0)
        assertEquals(60.0, ShadbalaCalculator.calculateHoraBala(BodyId.JUPITER, 2451545.0, 1))

        // 2nd hora is Mars (gets 60.0)
        assertEquals(60.0, ShadbalaCalculator.calculateHoraBala(BodyId.MARS, 2451545.0, 2))

        // 3rd hora is Sun (gets 60.0)
        assertEquals(60.0, ShadbalaCalculator.calculateHoraBala(BodyId.SUN, 2451545.0, 3))
    }

    @Test
    fun testAyanaBala_solsticesAndEquinoxes() {
        val obliquity = 23.44

        // Equinox (tropical 0° -> declination = 0°)
        // All planets get (24 + 0) * 1.25 = 30.0
        assertEquals(30.0, ShadbalaCalculator.calculateAyanaBala(BodyId.SUN, 0.0, obliquity), 1e-6)
        assertEquals(30.0, ShadbalaCalculator.calculateAyanaBala(BodyId.MOON, 0.0, obliquity), 1e-6)
        assertEquals(30.0, ShadbalaCalculator.calculateAyanaBala(BodyId.SATURN, 0.0, obliquity), 1e-6)

        // Summer Solstice (tropical 90° -> declination = +23.44°)
        // Sun gets (24 + 23.44) * 1.25 = 59.3
        val sunSummer = ShadbalaCalculator.calculateAyanaBala(BodyId.SUN, 90.0, obliquity)
        assertTrue(sunSummer > 59.0, "Sun summer Ayana Bala should be near 60.0, got $sunSummer")

        // Saturn gets (24 - 23.44) * 1.25 = 0.7
        val saturnSummer = ShadbalaCalculator.calculateAyanaBala(BodyId.SATURN, 90.0, obliquity)
        assertTrue(saturnSummer < 1.0, "Saturn summer Ayana Bala should be near 0.0, got $saturnSummer")
    }

    // =========================================================================
    // 5. CHESTA BALA (ASTRO-R35, BPHS Ch. 28, slokas 19-21)
    // =========================================================================

    @Test
    fun testChestaBala_sunAndMoonClassicalEquivalences() {
        val sun = dummyBody(BodyId.SUN, 0, 10.0)
        val moon = dummyBody(BodyId.MOON, 1, 15.0)

        val sunChesta = ShadbalaCalculator.calculateChestaBala(
            bodyId = BodyId.SUN,
            pos = sun,
            sunPos = sun,
            ayanaBala = 45.0,
            pakshaBala = 30.0,
        )
        // Sun Chesta Bala equals its Ayana Bala (BPHS Ch. 28, Sloka 21)
        assertEquals(45.0, sunChesta.virupas)
        assertEquals(45.0 / 60.0, sunChesta.rupas)

        val moonChesta = ShadbalaCalculator.calculateChestaBala(
            bodyId = BodyId.MOON,
            pos = moon,
            sunPos = sun,
            ayanaBala = 45.0,
            pakshaBala = 35.0,
        )
        // Moon Chesta Bala equals its Paksha Bala (BPHS Ch. 28, Sloka 21)
        assertEquals(35.0, moonChesta.virupas)
    }

    @Test
    fun testChestaBala_retrogradeTruePlanetsGetSixtyVirupas() {
        val mars = dummyBody(BodyId.MARS, 5, 10.0, isRetrograde = true, dailyMotion = -0.2)
        val sun = dummyBody(BodyId.SUN, 0, 10.0)

        val chesta = ShadbalaCalculator.calculateChestaBala(
            bodyId = BodyId.MARS,
            pos = mars,
            sunPos = sun,
            ayanaBala = 30.0,
            pakshaBala = 30.0,
        )

        assertTrue(chesta.isRetrograde)
        assertEquals(60.0, chesta.virupas)
        assertEquals(1.0, chesta.rupas)
        assertEquals("VAKRA", chesta.motionCategory)
    }

    // =========================================================================
    // 6. DRIK BALA (ASTRO-R37, BPHS Ch. 28, slokas 22-24)
    // =========================================================================

    @Test
    fun testDrikBala_beneficAspectAddsStrength() {
        // Jupiter at 0° (Aries 0°), Moon at 180° (Libra 0°)
        // Full opposition aspect (180°) = 60.0 Drishti
        val jupiter = dummyBody(BodyId.JUPITER, 0, 0.0)
        val moon = dummyBody(BodyId.MOON, 6, 0.0)
        val sun = dummyBody(BodyId.SUN, 0, 15.0)

        val drikMoon = ShadbalaCalculator.calculateDrikBala(
            targetBodyId = BodyId.MOON,
            positions = listOf(jupiter, moon, sun),
            sunPos = sun,
        )

        assertTrue(drikMoon.isEvaluated)
        assertTrue(drikMoon.beneficAspectVirupas > 0.0)
        // Drik Bala is non-zero
        assertTrue(abs(drikMoon.virupas) > 0.0)
    }

    // =========================================================================
    // 7. COMPLETENESS GATE & TOTAL SHADBALA (ASTRO-R38)
    // =========================================================================

    @Test
    fun testCompletenessGate_producesCompleteScoreForClassicalPlanets() {
        val planets = listOf(
            dummyBody(BodyId.SUN, rashiIndex = 0, degreeInRashi = 10.0),
            dummyBody(BodyId.MOON, rashiIndex = 1, degreeInRashi = 3.0),
            dummyBody(BodyId.MARS, rashiIndex = 9, degreeInRashi = 28.0),
            dummyBody(BodyId.MERCURY, rashiIndex = 5, degreeInRashi = 15.0),
            dummyBody(BodyId.JUPITER, rashiIndex = 3, degreeInRashi = 5.0),
            dummyBody(BodyId.VENUS, rashiIndex = 11, degreeInRashi = 27.0),
            dummyBody(BodyId.SATURN, rashiIndex = 6, degreeInRashi = 20.0),
            dummyBody(BodyId.RAHU, rashiIndex = 2, degreeInRashi = 10.0),
            dummyBody(BodyId.KETU, rashiIndex = 8, degreeInRashi = 10.0),
        )
        val cusps = defaultCuspsMap()
        val houseMap = planets.associate { it.bodyId to (it.rashiIndex + 1) }

        val results = ShadbalaCalculator.calculateShadbala(
            positions = planets,
            lagnaLongitude = 0.0,
            houseCusps = cusps,
            planetHouseOccupancy = houseMap,
            vargas = emptyMap(),
            julianDay = 2451545.0,
            birthHour = 12,
            obliquityDeg = 23.44,
        )

        assertEquals(9, results.size)

        val classicalBodies = listOf(
            BodyId.SUN, BodyId.MOON, BodyId.MARS, BodyId.MERCURY,
            BodyId.JUPITER, BodyId.VENUS, BodyId.SATURN
        )
        for (bodyId in classicalBodies) {
            val shadbala = results.first { it.bodyId == bodyId }

            // MUST be marked COMPLETE in Phase 5.6
            assertEquals(ShadbalaCompleteness.COMPLETE, shadbala.completeness)
            assertTrue(shadbala.isComplete, "isComplete must be true")
            assertTrue(shadbala.deferredComponents.isEmpty())

            // MUST produce non-null total scores
            assertNotNull(shadbala.totalVirupas)
            assertNotNull(shadbala.totalRupas)
            assertTrue(shadbala.totalVirupas > 0.0)

            // Verify all 6 component Balas are evaluated
            assertTrue(shadbala.sthanaBala.isEvaluated)
            assertTrue(shadbala.digBala.isEvaluated)
            assertTrue(shadbala.naisargikaBala.isEvaluated)
            assertTrue(shadbala.kalaBala.isEvaluated)
            assertTrue(shadbala.chestaBala.isEvaluated)
            assertTrue(shadbala.drikBala.isEvaluated)

            // Verify Total equals exact algebraic sum of 6 components
            val expectedTotal = shadbala.sthanaBala.totalVirupas +
                shadbala.digBala.virupas +
                shadbala.kalaBala.totalVirupas +
                shadbala.chestaBala.virupas +
                shadbala.naisargikaBala.virupas +
                shadbala.drikBala.virupas

            assertEquals(expectedTotal, shadbala.totalVirupas, 1e-9)
            assertEquals(expectedTotal / 60.0, shadbala.totalRupas, 1e-9)
        }

        // Verify Rahu and Ketu remain UNSUPPORTED
        for (shadowBody in listOf(BodyId.RAHU, BodyId.KETU)) {
            val shadbala = results.first { it.bodyId == shadowBody }
            assertEquals(ShadbalaCompleteness.UNSUPPORTED, shadbala.completeness)
            assertFalse(shadbala.isComplete)
            assertNull(shadbala.totalVirupas)
            assertNull(shadbala.totalRupas)
        }
    }

    @Test
    fun testDeterminismAndRepeatability() {
        val planets = listOf(
            dummyBody(BodyId.SUN, rashiIndex = 0, degreeInRashi = 10.0),
            dummyBody(BodyId.MOON, rashiIndex = 1, degreeInRashi = 3.0),
        )
        val cusps = defaultCuspsMap()
        val houseMap = mapOf(BodyId.SUN to 1, BodyId.MOON to 2)

        val run1 = ShadbalaCalculator.calculateShadbala(planets, 0.0, cusps, houseMap, emptyMap())
        val run2 = ShadbalaCalculator.calculateShadbala(planets, 0.0, cusps, houseMap, emptyMap())

        assertEquals(run1.size, run2.size)
        for (i in run1.indices) {
            val p1 = run1[i]
            val p2 = run2[i]
            assertEquals(p1.bodyId, p2.bodyId)
            assertEquals(p1.totalVirupas, p2.totalVirupas)
            assertEquals(p1.completeness, p2.completeness)
            assertEquals(p1.isComplete, p2.isComplete)
        }
    }
}

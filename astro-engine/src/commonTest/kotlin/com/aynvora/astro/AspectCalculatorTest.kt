package com.aynvora.astro

import com.aynvora.astro.aspects.AspectCalculator
import com.aynvora.astro.aspects.AspectDefinition
import com.aynvora.astro.aspects.AspectProfile
import com.aynvora.astro.aspects.AspectType
import com.aynvora.astro.math.AstroMath.angularSeparation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AspectCalculatorTest {

    private fun createBody(
        bodyId: BodyId,
        siderealLongitude: Double,
        isRetrograde: Boolean = false,
    ): BodyPosition {
        return BodyPosition(
            bodyId = bodyId,
            tropicalLongitude = (siderealLongitude + 24.0) % 360.0,
            siderealLongitude = siderealLongitude,
            rashiIndex = (siderealLongitude / 30.0).toInt().coerceIn(0, 11),
            rashiName = "Rashi_${(siderealLongitude / 30.0).toInt()}",
            degreeInRashi = siderealLongitude % 30.0,
            nakshatraIndex = (siderealLongitude / (40.0 / 3.0)).toInt().coerceIn(0, 26),
            nakshatraName = "Nakshatra",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = isRetrograde,
            dailyMotionDegrees = if (isRetrograde) -0.5 else 1.0,
        )
    }

    @Test
    fun testAngularSeparationBasicGeometry() {
        assertEquals(0.0, angularSeparation(0.0, 0.0), 1e-9)
        assertEquals(180.0, angularSeparation(0.0, 180.0), 1e-9)
        assertEquals(2.0, angularSeparation(359.0, 1.0), 1e-9)
        assertEquals(2.0, angularSeparation(1.0, 359.0), 1e-9)
        assertEquals(20.0, angularSeparation(10.0, 350.0), 1e-9)
        assertEquals(90.0, angularSeparation(45.0, 135.0), 1e-9)
        assertEquals(60.0, angularSeparation(720.0, 60.0), 1e-9)
        assertEquals(20.0, angularSeparation(-10.0, 10.0), 1e-9)
    }

    @Test
    fun testAngularSeparationSymmetryAndBoundsProperty() {
        for (a in 0..360 step 15) {
            for (b in 0..360 step 15) {
                val sepAB = angularSeparation(a.toDouble(), b.toDouble())
                val sepBA = angularSeparation(b.toDouble(), a.toDouble())
                assertEquals(sepAB, sepBA, 1e-9, "Symmetry failed for $a and $b")
                assertTrue(sepAB in 0.0..180.0, "Bounds failed: $sepAB not in [0, 180]")
            }
        }
    }

    @Test
    fun testConjunctionDetection() {
        val sun = createBody(BodyId.SUN, 100.0)
        val moon = createBody(BodyId.MOON, 102.5) // separation = 2.5° (within 8.0° orb)
        val mars = createBody(BodyId.MARS, 120.0) // separation = 20.0° (outside 8.0° orb)

        val aspects = AspectCalculator.calculate(listOf(sun, moon, mars))
        val conj = aspects.firstOrNull { it.type == AspectType.CONJUNCTION }

        assertNotNull(conj)
        assertEquals(BodyId.SUN, conj.firstBody)
        assertEquals(BodyId.MOON, conj.secondBody)
        assertEquals(2.5, conj.actualSeparation, 1e-9)
        assertEquals(2.5, conj.orb, 1e-9)
    }

    @Test
    fun testMajorAspectTypesDetection() {
        val sun = createBody(BodyId.SUN, 0.0)
        val sextilePlanet = createBody(BodyId.MOON, 60.5) // Sextile, orb 0.5°
        val squarePlanet = createBody(BodyId.MERCURY, 89.0) // Square, orb 1.0°
        val trinePlanet = createBody(BodyId.VENUS, 121.2) // Trine, orb 1.2°
        val oppPlanet = createBody(BodyId.MARS, 180.0) // Opposition, orb 0.0°

        val aspects = AspectCalculator.calculate(listOf(sun, sextilePlanet, squarePlanet, trinePlanet, oppPlanet))

        val sextile = aspects.firstOrNull { it.firstBody == BodyId.SUN && it.secondBody == BodyId.MOON }
        assertNotNull(sextile)
        assertEquals(AspectType.SEXTILE, sextile.type)
        assertEquals(0.5, sextile.orb, 1e-9)

        val square = aspects.firstOrNull { it.firstBody == BodyId.SUN && it.secondBody == BodyId.MERCURY }
        assertNotNull(square)
        assertEquals(AspectType.SQUARE, square.type)
        assertEquals(1.0, square.orb, 1e-9)

        val trine = aspects.firstOrNull { it.firstBody == BodyId.SUN && it.secondBody == BodyId.VENUS }
        assertNotNull(trine)
        assertEquals(AspectType.TRINE, trine.type)
        assertEquals(1.2, trine.orb, 1e-9)

        val opp = aspects.firstOrNull { it.firstBody == BodyId.SUN && it.secondBody == BodyId.MARS }
        assertNotNull(opp)
        assertEquals(AspectType.OPPOSITION, opp.type)
        assertEquals(0.0, opp.orb, 1e-9)
    }

    @Test
    fun testExactOrbBoundaryBehavior() {
        val customProfile = AspectProfile(
            definitions = listOf(
                AspectDefinition(AspectType.TRINE, 120.0, allowedOrb = 5.0),
            ),
        )

        val sun = createBody(BodyId.SUN, 0.0)
        val planetInside = createBody(BodyId.MOON, 124.999) // orb = 4.999° -> inside
        val planetExact = createBody(BodyId.MERCURY, 125.0) // orb = 5.0° -> inside
        val planetOutside = createBody(BodyId.VENUS, 125.001) // orb = 5.001° -> outside

        val aspects = AspectCalculator.calculate(listOf(sun, planetInside, planetExact, planetOutside), customProfile)

        assertTrue(aspects.any { it.firstBody == BodyId.SUN && it.secondBody == BodyId.MOON })
        assertTrue(aspects.any { it.firstBody == BodyId.SUN && it.secondBody == BodyId.MERCURY })
        assertFalse(aspects.any { it.firstBody == BodyId.SUN && it.secondBody == BodyId.VENUS })
    }

    @Test
    fun testNoSelfPairsAndNoDuplicatePairs() {
        val bodies = listOf(
            createBody(BodyId.SUN, 0.0),
            createBody(BodyId.MOON, 0.0),
            createBody(BodyId.MARS, 0.0),
        )

        val aspects = AspectCalculator.calculate(bodies)

        // There should be exactly 3 conjunctions: (SUN, MOON), (SUN, MARS), (MOON, MARS)
        assertEquals(3, aspects.size)
        aspects.forEach {
            assertTrue(it.firstBody != it.secondBody, "Self-pair found: ${it.firstBody}")
            assertTrue(it.firstBody.ordinal < it.secondBody.ordinal, "Ordering violation: ${it.firstBody} -> ${it.secondBody}")
        }
    }

    @Test
    fun testRahuKetuInclusionAndExclusion() {
        val sun = createBody(BodyId.SUN, 0.0)
        val rahu = createBody(BodyId.RAHU, 180.0)

        val profileIncluded = AspectProfile(includeLunarNodes = true)
        val aspectsIncluded = AspectCalculator.calculate(listOf(sun, rahu), profileIncluded)
        assertEquals(1, aspectsIncluded.size)
        assertEquals(AspectType.OPPOSITION, aspectsIncluded[0].type)

        val profileExcluded = AspectProfile(includeLunarNodes = false)
        val aspectsExcluded = AspectCalculator.calculate(listOf(sun, rahu), profileExcluded)
        assertEquals(0, aspectsExcluded.size)
    }

    @Test
    fun testDeterministicOrdering() {
        val bodies = listOf(
            createBody(BodyId.SATURN, 120.0),
            createBody(BodyId.SUN, 0.0),
            createBody(BodyId.MOON, 60.0),
            createBody(BodyId.MARS, 180.0),
        )

        val aspects1 = AspectCalculator.calculate(bodies)
        val aspects2 = AspectCalculator.calculate(bodies.reversed())

        assertEquals(aspects1.size, aspects2.size)
        for (i in aspects1.indices) {
            assertEquals(aspects1[i].firstBody, aspects2[i].firstBody)
            assertEquals(aspects1[i].secondBody, aspects2[i].secondBody)
            assertEquals(aspects1[i].type, aspects2[i].type)
            assertEquals(aspects1[i].orb, aspects2[i].orb)
        }
    }
}

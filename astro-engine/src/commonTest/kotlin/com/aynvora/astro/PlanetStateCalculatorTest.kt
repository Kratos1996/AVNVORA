package com.aynvora.astro

import com.aynvora.astro.states.CombustionState
import com.aynvora.astro.states.PlanetMotionState
import com.aynvora.astro.states.PlanetStateCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlanetStateCalculatorTest {

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
            rashiName = "Rashi",
            degreeInRashi = siderealLongitude % 30.0,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
            isRetrograde = isRetrograde,
            dailyMotionDegrees = if (isRetrograde) -0.5 else 1.0,
        )
    }

    @Test
    fun testMotionStateMapping() {
        val directMars = createBody(BodyId.MARS, 50.0, isRetrograde = false)
        val retroJupiter = createBody(BodyId.JUPITER, 100.0, isRetrograde = true)

        val states = PlanetStateCalculator.calculate(listOf(directMars, retroJupiter))
        val marsState = states.first { it.bodyId == BodyId.MARS }
        val jupState = states.first { it.bodyId == BodyId.JUPITER }

        assertEquals(PlanetMotionState.DIRECT, marsState.motionState)
        assertEquals(PlanetMotionState.RETROGRADE, jupState.motionState)
    }

    @Test
    fun testSunAndNodesCombustionNotApplicable() {
        val sun = createBody(BodyId.SUN, 100.0)
        val rahu = createBody(BodyId.RAHU, 100.0, isRetrograde = true) // Conjunction with Sun
        val ketu = createBody(BodyId.KETU, 280.0, isRetrograde = true)

        val states = PlanetStateCalculator.calculate(listOf(sun, rahu, ketu))
        val sunState = states.first { it.bodyId == BodyId.SUN }
        val rahuState = states.first { it.bodyId == BodyId.RAHU }
        val ketuState = states.first { it.bodyId == BodyId.KETU }

        assertEquals(CombustionState.NOT_APPLICABLE, sunState.combustionState)
        assertEquals(CombustionState.NOT_APPLICABLE, rahuState.combustionState)
        assertEquals(CombustionState.NOT_APPLICABLE, ketuState.combustionState)
        assertNull(sunState.separationFromSun)
        assertNull(rahuState.separationFromSun)
    }

    @Test
    fun testClassicalCombustionThresholds() {
        val sun = createBody(BodyId.SUN, 100.0)

        // Moon: 12° threshold
        val combustMoon = createBody(BodyId.MOON, 110.0) // 10° separation -> COMBUST
        val normalMoon = createBody(BodyId.MOON, 113.0) // 13° separation -> NORMAL

        // Mars: 17° threshold
        val combustMars = createBody(BodyId.MARS, 116.0) // 16° separation -> COMBUST
        val normalMars = createBody(BodyId.MARS, 118.0) // 18° separation -> NORMAL

        // Jupiter: 11° threshold
        val combustJupiter = createBody(BodyId.JUPITER, 108.0) // 8° separation -> COMBUST
        val normalJupiter = createBody(BodyId.JUPITER, 112.0) // 12° separation -> NORMAL

        // Saturn: 15° threshold
        val combustSaturn = createBody(BodyId.SATURN, 114.0) // 14° separation -> COMBUST
        val normalSaturn = createBody(BodyId.SATURN, 116.0) // 16° separation -> NORMAL

        val statesCombust = PlanetStateCalculator.calculate(
            listOf(sun, combustMoon, combustMars, combustJupiter, combustSaturn),
        )
        assertEquals(CombustionState.COMBUST, statesCombust.first { it.bodyId == BodyId.MOON }.combustionState)
        assertEquals(CombustionState.COMBUST, statesCombust.first { it.bodyId == BodyId.MARS }.combustionState)
        assertEquals(CombustionState.COMBUST, statesCombust.first { it.bodyId == BodyId.JUPITER }.combustionState)
        assertEquals(CombustionState.COMBUST, statesCombust.first { it.bodyId == BodyId.SATURN }.combustionState)

        val statesNormal = PlanetStateCalculator.calculate(
            listOf(sun, normalMoon, normalMars, normalJupiter, normalSaturn),
        )
        assertEquals(CombustionState.NORMAL, statesNormal.first { it.bodyId == BodyId.MOON }.combustionState)
        assertEquals(CombustionState.NORMAL, statesNormal.first { it.bodyId == BodyId.MARS }.combustionState)
        assertEquals(CombustionState.NORMAL, statesNormal.first { it.bodyId == BodyId.JUPITER }.combustionState)
        assertEquals(CombustionState.NORMAL, statesNormal.first { it.bodyId == BodyId.SATURN }.combustionState)
    }

    @Test
    fun testMercuryAndVenusDirectVsRetrogradeCombustionThresholds() {
        val sun = createBody(BodyId.SUN, 100.0)

        // Mercury: 14° (direct), 12° (retrograde)
        val mercDirectCombust = createBody(BodyId.MERCURY, 113.0, isRetrograde = false) // 13° <= 14° -> COMBUST
        val mercDirectNormal = createBody(BodyId.MERCURY, 115.0, isRetrograde = false) // 15° > 14° -> NORMAL
        val mercRetroCombust = createBody(BodyId.MERCURY, 111.0, isRetrograde = true) // 11° <= 12° -> COMBUST
        val mercRetroNormal = createBody(BodyId.MERCURY, 113.0, isRetrograde = true) // 13° > 12° -> NORMAL

        // Venus: 10° (direct), 8° (retrograde)
        val venusDirectCombust = createBody(BodyId.VENUS, 109.0, isRetrograde = false) // 9° <= 10° -> COMBUST
        val venusDirectNormal = createBody(BodyId.VENUS, 111.0, isRetrograde = false) // 11° > 10° -> NORMAL
        val venusRetroCombust = createBody(BodyId.VENUS, 107.0, isRetrograde = true) // 7° <= 8° -> COMBUST
        val venusRetroNormal = createBody(BodyId.VENUS, 109.0, isRetrograde = true) // 9° > 8° -> NORMAL

        val states1 = PlanetStateCalculator.calculate(listOf(sun, mercDirectCombust, mercRetroNormal, venusDirectCombust, venusRetroNormal))
        assertEquals(CombustionState.COMBUST, states1.first { it.bodyId == BodyId.MERCURY && it.motionState == PlanetMotionState.DIRECT }.combustionState)
        assertEquals(CombustionState.NORMAL, states1.first { it.bodyId == BodyId.MERCURY && it.motionState == PlanetMotionState.RETROGRADE }.combustionState)
        assertEquals(CombustionState.COMBUST, states1.first { it.bodyId == BodyId.VENUS && it.motionState == PlanetMotionState.DIRECT }.combustionState)
        assertEquals(CombustionState.NORMAL, states1.first { it.bodyId == BodyId.VENUS && it.motionState == PlanetMotionState.RETROGRADE }.combustionState)

        val states2 = PlanetStateCalculator.calculate(listOf(sun, mercDirectNormal, mercRetroCombust, venusDirectNormal, venusRetroCombust))
        assertEquals(CombustionState.NORMAL, states2.first { it.bodyId == BodyId.MERCURY && it.motionState == PlanetMotionState.DIRECT }.combustionState)
        assertEquals(CombustionState.COMBUST, states2.first { it.bodyId == BodyId.MERCURY && it.motionState == PlanetMotionState.RETROGRADE }.combustionState)
        assertEquals(CombustionState.NORMAL, states2.first { it.bodyId == BodyId.VENUS && it.motionState == PlanetMotionState.DIRECT }.combustionState)
        assertEquals(CombustionState.COMBUST, states2.first { it.bodyId == BodyId.VENUS && it.motionState == PlanetMotionState.RETROGRADE }.combustionState)
    }
}

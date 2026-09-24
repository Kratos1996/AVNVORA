package com.aynvora.astro

import com.aynvora.astro.dasha.DashaPlanet
import com.aynvora.astro.dasha.VimshottariDashaCalculator
import com.aynvora.astro.panchang.PanchangCalculator
import com.aynvora.astro.panchang.Vara
import com.aynvora.astro.transit.TransitCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DashaTransitPanchangTest {

    // J2000.0 epoch: 2000-01-01 12:00:00 UT -> JD 2451545.0
    private val j2000Jd = 2451545.0

    @Test
    fun testVimshottariDasha_AshwiniMoon_StartsKetu() {
        // Ashwini spans 0° to 13° 20' (13.333333°). If Moon is at 6.666666° (mid-Ashwini),
        // starting lord is Ketu (index 0). Half traversed => 3.5 years balance of Ketu Dasha remaining at birth.
        val moonLong = 6.666666666666667
        val timeline = VimshottariDashaCalculator.calculate(
            birthJd = j2000Jd,
            moonSiderealLongitude = moonLong,
            calculateAntardashas = true,
        )

        assertEquals(DashaPlanet.KETU, timeline.startingLord)
        assertTrue(timeline.balanceYearsAtBirth in 3.49..3.51)
        assertEquals(9, timeline.mahadashas.size)

        // First Mahadasha is Ketu
        val firstMaha = timeline.mahadashas[0]
        assertEquals(DashaPlanet.KETU, firstMaha.planet)
        assertEquals(9, firstMaha.subPeriods.size)

        // Second Mahadasha is Venus (20 years)
        val secondMaha = timeline.mahadashas[1]
        assertEquals(DashaPlanet.VENUS, secondMaha.planet)

        // Test active periods resolution
        val (activeMaha, activeAntar) = timeline.findActivePeriodsAt(j2000Jd + 100.0) // 100 days after birth
        assertNotNull(activeMaha)
        assertEquals(DashaPlanet.KETU, activeMaha.planet)
        assertNotNull(activeAntar)
    }

    @Test
    fun testTransitCalculator_J2000Deterministic() {
        val snapshot = TransitCalculator.calculateSnapshot(j2000Jd)
        assertEquals(j2000Jd, snapshot.julianDay)
        assertEquals(9, snapshot.planetaryPositions.size)

        val sun = snapshot.findBody(BodyId.SUN)
        assertNotNull(sun)
        // Around Jan 1, Sun sidereal longitude in Lahiri is in Sagittarius (Dhanu) around 255°
        assertEquals("Sagittarius", sun.rashiName)
        assertEquals(8, sun.rashiIndex)

        // Range calculation
        val timeline = TransitCalculator.calculateTimeline(
            startJd = j2000Jd,
            endJd = j2000Jd + 10.0,
            stepDays = 2.0,
        )
        assertEquals(6, timeline.snapshots.size) // 0, 2, 4, 6, 8, 10
    }

    @Test
    fun testPanchangCalculator_J2000KnownValues() {
        val panchang = PanchangCalculator.calculate(j2000Jd)
        // 2000-01-01 was a Saturday (Shanivara)
        assertEquals(Vara.SATURDAY, panchang.vara)

        // Tithi, Nakshatra, Yoga, and Karana check
        assertNotNull(panchang.tithi)
        assertNotNull(panchang.yoga)
        assertNotNull(panchang.karana)
        assertTrue(panchang.nakshatraIndex in 0..26)
    }
}

package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.*
import com.aynvora.astro.varshaphal.SolarReturnMoment
import com.aynvora.astro.varshaphal.SolarReturnStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.25 Golden Fixtures and Differential Tests for Classical Tajika Astrology.
 *
 * Primary Source: Tajika Neelakanthi (1907 edition, Khemraj Shri Venkateshwar Steam Press, Bombay)
 * SHA-256: a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989
 */
class VarshaphalGoldenFixturesTest {

    private fun createPlanet(id: String, house: Int, sign: Rashi, longitude: Double): PlanetInHouse {
        val degInSign = (longitude % 30.0 + 30.0) % 30.0
        return PlanetInHouse(
            planetId = id,
            displayKey = id.lowercase(),
            degreeInSign = degInSign,
            degrees = degInSign.toInt(),
            longitude = longitude,
            minutes = 0,
            seconds = 0.0,
            nakshatra = null,
            pada = null,
            retrograde = false,
            combust = false,
            exalted = null,
            debilitated = null,
            vargottama = null,
            dignity = null,
            state = null,
            houseNumber = house,
            sign = sign,
            provenance = "TEST",
        )
    }

    // -------------------------------------------------------------------------
    // 1. Muntha Golden Fixtures (Offsets 0, 1, 11, 12, 13, 24, wraparound)
    // Primary Source: Varsha Tantra, Muntha Adhyaya, v. 1 (PDF p. 120 / printed p. 112)
    // -------------------------------------------------------------------------

    @Test
    fun munthaGoldenProgressionOffsetsAndWraparound() {
        // Offset 0 (birth year): retains exact natal sign and degree
        val m0 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 0)
        assertEquals(Rashi.ARIES, m0.sign)
        assertEquals(15.0, m0.longitude)
        assertEquals(MunthaLord.MARS, m0.lord)

        // Offset 1: progresses 1 sign (Taurus)
        val m1 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 1)
        assertEquals(Rashi.TAURUS, m1.sign)
        assertEquals(45.0, m1.longitude)
        assertEquals(MunthaLord.VENUS, m1.lord)

        // Offset 11: progresses 11 signs (Pisces)
        val m11 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 11)
        assertEquals(Rashi.PISCES, m11.sign)
        assertEquals(345.0, m11.longitude)
        assertEquals(MunthaLord.JUPITER, m11.lord)

        // Offset 12: completes full 12-sign cycle, returns to Aries
        val m12 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 12)
        assertEquals(Rashi.ARIES, m12.sign)
        assertEquals(15.0, m12.longitude)
        assertEquals(MunthaLord.MARS, m12.lord)

        // Offset 13: 13 % 12 = 1 (Taurus)
        val m13 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 13)
        assertEquals(Rashi.TAURUS, m13.sign)
        assertEquals(45.0, m13.longitude)
        assertEquals(MunthaLord.VENUS, m13.lord)

        // Offset 24: 24 % 12 = 0 (2 full cycles back to Aries)
        val m24 = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 24)
        assertEquals(Rashi.ARIES, m24.sign)
        assertEquals(15.0, m24.longitude)
        assertEquals(MunthaLord.MARS, m24.lord)

        // Wraparound test from Pisces (359.0°) + 1 cycle -> Aries (29.0°)
        val mWrap = MunthaEngine.calculate(natalAscendantLongitude = 359.0, elapsedSolarReturnCycles = 1)
        assertEquals(Rashi.ARIES, mWrap.sign)
        assertEquals(29.0, mWrap.longitude)
        assertEquals(MunthaLord.MARS, mWrap.lord)
    }

    // -------------------------------------------------------------------------
    // 2. Muntha Lord & Annual House Golden Fixtures
    // Primary Source: Varsha Tantra, Muntha Adhyaya, v. 3 (PDF p. 121 / printed p. 113)
    // -------------------------------------------------------------------------

    @Test
    fun munthaLordAndAnnualHouseCalculation() {
        val muntha = MunthaEngine.calculate(
            natalAscendantLongitude = 15.0,
            elapsedSolarReturnCycles = 4, // Aries -> Leo (sign index 4)
            annualAscendantSign = Rashi.GEMINI, // Gemini = House 1, Cancer = 2, Leo = 3
        )
        assertEquals(Rashi.LEO, muntha.sign)
        assertEquals(MunthaLord.SUN, muntha.lord)
        assertEquals(3, muntha.annualHouse)

        val result = MunthaLordEngine.calculate(muntha, annualChart = null)
        assertEquals(Rashi.LEO, result.munthaSign)
        assertEquals(MunthaLord.SUN, result.lord)
        assertEquals(3, result.annualHouse)
        assertTrue(result.provenance.any { it.contains("primary-source") })
    }

    // -------------------------------------------------------------------------
    // 3. Varsheshwara 5 Office-Bearers & Panchavargiya Bala Fixture
    // Primary Source: Varsha Tantra, vv. 5-8 (PDF pp. 110-111 / printed pp. 102-103)
    // -------------------------------------------------------------------------

    @Test
    fun varsheshwaraOfficeBearersAndAspectEligibility() {
        // Build mock annual chart with Leo ascendant (day return)
        val planets = listOf(
            createPlanet("SUN", 1, Rashi.LEO, 130.0),
            createPlanet("MOON", 5, Rashi.SAGITTARIUS, 250.0),
            createPlanet("MARS", 10, Rashi.TAURUS, 40.0),
            createPlanet("MERCURY", 2, Rashi.VIRGO, 160.0), // House 2 = Ineligible (Adrishti)
            createPlanet("JUPITER", 9, Rashi.ARIES, 10.0),
            createPlanet("VENUS", 11, Rashi.GEMINI, 70.0),
            createPlanet("SATURN", 6, Rashi.CAPRICORN, 280.0), // House 6 = Ineligible (Adrishti)
        )
        val houses = (1..12).map { h ->
            AstroChartHouseData(
                houseNumber = h,
                sign = Rashi.fromIndex((3 + h) % 12),
                cusp = ((3 + h) % 12) * 30.0,
                planets = planets.filter { it.houseNumber == h },
                ascendant = h == 1,
            )
        }
        val chart = AstroChart(
            chartId = "ANNUAL_TEST",
            chartType = "D1",
            titleKey = "chart.annual",
            zodiacMode = "SIDEREAL",
            houseSystem = "WHOLE_SIGN",
            ascendant = AstroChartAscendant(Rashi.LEO, 120.0),
            houses = houses,
            metadata = CalculationMetadata(
                calculationProfileId = "STANDARD_VEDIC",
                calculationModel = "TEST",
                engineVersion = "0.1.0",
            ),
            provenance = "TEST",
            status = CalculationAvailability.AVAILABLE,
        )

        val muntha = MunthaEngine.calculate(natalAscendantLongitude = 15.0, elapsedSolarReturnCycles = 1) // Taurus, Venus
        val varsheshwara = VarsheshwaraEngine.calculate(
            natalAscendantLongitude = 15.0, // Aries -> Janma Lagnesha Mars
            annualChart = chart,
            muntha = muntha,
        )

        // 5 Office-bearers: Janma Lagnesha (Mars), Varsha Lagnesha (Sun), Munthesha (Venus), Trirashipati (Jupiter for Leo day), Dina-pati (Sun)
        val candidateNames = varsheshwara.candidates.map { it.planet }
        assertTrue(candidateNames.contains("MARS"), "Janma Lagnesha must be candidate")
        assertTrue(candidateNames.contains("SUN"), "Varsha Lagnesha must be candidate")
        assertTrue(candidateNames.contains("VENUS"), "Munthesha must be candidate")
        assertTrue(candidateNames.contains("JUPITER"), "Trirashipati must be candidate")

        // In Tajika, planets in houses 2, 6, 8, 12 cannot aspect Lagna (Adrishti).
        // Mars is in H10 (eligible), Sun in H1 (eligible), Jupiter in H9 (eligible), Venus in H11 (eligible).
        // Mercury in H2 and Saturn in H6 are ineligible.
        val marsCand = varsheshwara.candidates.first { it.planet == "MARS" }
        val sunCand = varsheshwara.candidates.first { it.planet == "SUN" }
        assertTrue(marsCand.eligible == true)
        assertTrue(sunCand.eligible == true)

        // Selected planet must be one of the eligible candidates with highest Panchavargiya score
        assertNotNull(varsheshwara.selectedPlanet)
        assertTrue(varsheshwara.candidates.first { it.planet == varsheshwara.selectedPlanet }.eligible == true)
        assertTrue(varsheshwara.provenance.any { it.contains("Five candidates") })
    }

    // -------------------------------------------------------------------------
    // 4. Sahams with Classical Saika-bham (+30°) Arc Correction
    // Primary Source: Samjna Tantra, Saham chapter, vv. 5, 6, 12 (PDF pp. 87-89 / printed pp. 79-81)
    // -------------------------------------------------------------------------

    @Test
    fun sahamPrimaryFormulasAndSaikabhamArcCorrection() {
        // Test primary 1907 commentary example:
        // Point A (Moon) = 6s 12°10' = 192.166667°
        // Point B (Sun) = 4s 8°10' = 128.166667°
        // Point C (Lagna) = 8s 10°10' = 250.166667°
        // Forward arc from B (128.166667°) to A (192.166667°) is 64°.
        // Forward arc from B (128.166667°) to Lagna (250.166667°) is 122° (> 64°).
        // Therefore Lagna is outside the arc! Classical rule prescribes adding 30° (+1 rashi / Saika-bham).
        // Base = 250.166667 + 192.166667 - 128.166667 = 314.166667° (10s 14°10').
        // With +30° correction = 344.166667° (11s 14°10' = Meena 14°10').
        val (longWithCorrection, correctionApplied) = SahamEngine.calculateSahamLongitude(
            pointA = 192.166667,
            pointB = 128.166667,
            lagna = 250.166667,
        )
        assertTrue(correctionApplied, "Lagna is outside arc between Sun and Moon; +30° must be applied per v. 5")
        assertEquals(344.166667, longWithCorrection, 0.001)

        // Case when Lagna IS inside the arc:
        // Sun = 100°, Moon = 160°, Lagna = 130° (between 100° and 160°)
        // Base = 130 + 160 - 100 = 190°. No +30° correction.
        val (longInside, corrInside) = SahamEngine.calculateSahamLongitude(
            pointA = 160.0,
            pointB = 100.0,
            lagna = 130.0,
        )
        assertFalse(corrInside, "Lagna is inside arc; no +30° correction")
        assertEquals(190.0, longInside, 0.001)
    }

    // -------------------------------------------------------------------------
    // 5. Tajika Aspects, Deeptamsha Orbs & Itthashala / Ishrafa Yogas
    // Primary Source: Samjna Tantra Ch. 2 vv. 13-14 (PDF p. 49) & Ch. 3 vv. 1-10 (PDF pp. 50-55)
    // -------------------------------------------------------------------------

    @Test
    fun tajikaAspectsDeeptamshaAndYogaFormation() {
        val sunDeeptamsha = TajikaAspectEngine.deeptamshaOf("SUN")
        val moonDeeptamsha = TajikaAspectEngine.deeptamshaOf("MOON")
        val marsDeeptamsha = TajikaAspectEngine.deeptamshaOf("MARS")
        assertEquals(15.0, sunDeeptamsha)
        assertEquals(12.0, moonDeeptamsha)
        assertEquals(8.0, marsDeeptamsha)

        val chartPlacements = listOf(
            createPlanet("SUN", 1, Rashi.ARIES, 14.0),
            createPlanet("MOON", 3, Rashi.GEMINI, 68.0), // Moon at 8° Gemini applying to Sun at 14° Aries
            createPlanet("MARS", 7, Rashi.LIBRA, 190.0), // Mars at 10° Libra separating from Sun at 14° Aries
        )
        val houses = (1..12).map { h ->
            AstroChartHouseData(
                houseNumber = h,
                sign = Rashi.fromIndex(h - 1),
                cusp = (h - 1) * 30.0,
                planets = chartPlacements.filter { it.houseNumber == h },
                ascendant = h == 1,
            )
        }
        val chart = AstroChart(
            chartId = "ASPECT_TEST",
            chartType = "D1",
            titleKey = "chart.aspect",
            zodiacMode = "SIDEREAL",
            houseSystem = "WHOLE_SIGN",
            ascendant = AstroChartAscendant(Rashi.ARIES, 0.0),
            houses = houses,
            metadata = CalculationMetadata(
                calculationProfileId = "STANDARD_VEDIC",
                calculationModel = "TEST",
                engineVersion = "0.1.0",
            ),
            provenance = "TEST",
            status = CalculationAvailability.AVAILABLE,
        )

        val aspects = TajikaAspectEngine.calculateAspects(chart)
        assertTrue(aspects.isNotEmpty(), "Tajika aspects should be formed within orb")

        val sunMoon = aspects.firstOrNull {
            (it.planet1 == "MOON" && it.planet2 == "SUN") || (it.planet1 == "SUN" && it.planet2 == "MOON")
        }
        assertNotNull(sunMoon, "Sun-Moon sextile within 13.5° orb must be detected")
        assertEquals(true, sunMoon.applying, "Moon at 8° applying to Sun at 14° sextile")
        assertEquals(true, sunMoon.itthashala, "Applying aspect within orb constitutes Itthashala Yoga")

        val sunMars = aspects.firstOrNull {
            (it.planet1 == "MARS" && it.planet2 == "SUN") || (it.planet1 == "SUN" && it.planet2 == "MARS")
        }
        assertNotNull(sunMars, "Sun-Mars opposition within orb must be detected")
        assertEquals(true, sunMars.separating, "Sun at 14° past Mars at 10° opposition is separating")
        assertEquals(true, sunMars.ishrafa, "Separating aspect within orb constitutes Ishrafa Yoga")
    }

    // -------------------------------------------------------------------------
    // 6. Mudda Dasha Golden Fixtures: Continuous Coverage, Zero Gap, Zero Overlap
    // Primary Source: Varsha Tantra, p. 192 vv. 14-15 & Tajika Muktavali
    // -------------------------------------------------------------------------

    @Test
    fun muddaDashaContinuousCoverageAndDeterminism() {
        val solarMoment = SolarReturnMoment(
            targetYear = 2024,
            julianDayUtc = 2460310.5,
            utcTimestamp = "2024-01-01 12:00:00 UTC",
            natalSunLongitude = 255.0,
            returnSunLongitude = 255.0,
            ayanamsa = "LAHIRI",
            status = SolarReturnStatus.CALCULATED,
        )

        val periods = MuddaDashaEngine.calculate(
            solarReturn = solarMoment,
            natalMoonLongitude = 10.0, // Ashwini nakshatra (Ketu lord)
            elapsedCycles = 0,
            annualLengthDays = 365.24219,
        )

        assertEquals(9, periods.size, "Mudda Dasha must have 9 planetary sub-periods")
        assertEquals("KETU", periods.first().planet, "Starting lord for Ashwini Moon must be Ketu")

        // Sum of all 9 durations must equal the annual solar return interval
        val totalDuration = periods.sumOf { it.durationDays }
        assertEquals(365.24219, totalDuration, 0.0001, "Total duration must match solar return interval exactly")

        // Zero-gap, zero-overlap check: each period's start must match previous period's end
        for (i in 0 until periods.size - 1) {
            assertEquals(periods[i].end, periods[i + 1].start, "Period $i end must match period ${i + 1} start")
        }

        // Starts at solar return timestamp
        assertEquals("2024-01-01 12:00:00 UTC", periods.first().start)
    }
}

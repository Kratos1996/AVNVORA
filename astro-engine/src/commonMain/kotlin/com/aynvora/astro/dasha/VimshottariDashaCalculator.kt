package com.aynvora.astro.dasha

import com.aynvora.astro.zodiac.ZodiacCalculator
import com.aynvora.astro.provenance.CalculationMetadata
import kotlinx.serialization.Serializable

/**
 * Astrological planetary lord in Vimshottari Dasha system.
 */
@Serializable
enum class DashaPlanet(
    val index: Int,
    val displayName: String,
    val standardYears: Double,
) {
    KETU(0, "Ketu", 7.0),
    VENUS(1, "Venus", 20.0),
    SUN(2, "Sun", 6.0),
    MOON(3, "Moon", 10.0),
    MARS(4, "Mars", 7.0),
    RAHU(5, "Rahu", 18.0),
    JUPITER(6, "Jupiter", 16.0),
    SATURN(7, "Saturn", 19.0),
    MERCURY(8, "Mercury", 17.0);

    companion object {
        const val TOTAL_VIMSHOTTARI_YEARS = 120.0

        // Julian year in days (canonical 365.25 days per solar year)
        const val DAYS_PER_YEAR = 365.25

        fun fromIndex(index: Int): DashaPlanet = entries[index % entries.size]

        /**
         * Resolves the Dasha ruler for a given Nakshatra index (0..26).
         * Nakshatras repeat in cycle of 9 planets: Ketu (0, 9, 18), Venus (1, 10, 19), etc.
         */
        fun fromNakshatraIndex(nakshatraIndex: Int): DashaPlanet =
            entries[nakshatraIndex % 9]
    }
}

/**
 * Single Dasha period (Mahadasha, Antardasha, or Pratyantardasha).
 */
@Serializable
data class DashaPeriod(
    val planet: DashaPlanet,
    val level: Int, // 1 = Mahadasha, 2 = Antardasha, 3 = Pratyantardasha
    val startJulianDay: Double,
    val endJulianDay: Double,
    val subPeriods: List<DashaPeriod> = emptyList(),
) {
    fun contains(jd: Double): Boolean = jd >= startJulianDay && jd < endJulianDay
}

/**
 * Complete Vimshottari Dasha timeline for a birth chart.
 */
@Serializable
data class VimshottariDashaTimeline(
    val rulesetId: String,
    val birthJulianDay: Double,
    val moonSiderealLongitude: Double,
    val startingLord: DashaPlanet,
    val balanceYearsAtBirth: Double,
    val mahadashas: List<DashaPeriod>,
    val calculationMetadata: CalculationMetadata = CalculationMetadata(
        calculationProfileId = rulesetId,
        calculationModel = "VIMSHOTTARI_DASHA",
        ephemerisSourceId = "PARASHARA_VIMSHOTTARI_RULES_V1",
        conventions = mapOf("days_per_dasha_year" to DashaPlanet.DAYS_PER_YEAR.toString()),
    ),
) {
    /**
     * Finds the active Mahadasha and Antardasha for a target Julian Day.
     */
    fun findActivePeriodsAt(targetJd: Double): Pair<DashaPeriod?, DashaPeriod?> {
        val activeMaha = mahadashas.find { it.contains(targetJd) } ?: return Pair(null, null)
        val activeAntar = activeMaha.subPeriods.find { it.contains(targetJd) }
        return Pair(activeMaha, activeAntar)
    }
}

/**
 * Deterministic calculator for the classical Vimshottari Dasha system (120-year cycle).
 * Reference: Brihat Parashara Hora Shastra (BPHS), Chapter 46.
 */
object VimshottariDashaCalculator {

    const val DEFAULT_RULESET_ID = "PARASHARA_VIMSHOTTARI_120_V1"

    /**
     * Calculates the full 120-year Vimshottari Dasha tree from birth Julian Day and Moon's sidereal longitude.
     *
     * @param birthJd The birth Julian Day.
     * @param moonSiderealLongitude The sidereal longitude of Moon (0°..360°).
     * @param calculateAntardashas Whether to compute level-2 Antardashas.
     * @param calculatePratyantardashas Whether to compute level-3 Pratyantardashas.
     */
    fun calculate(
        birthJd: Double,
        moonSiderealLongitude: Double,
        calculateAntardashas: Boolean = true,
        calculatePratyantardashas: Boolean = false,
        rulesetId: String = DEFAULT_RULESET_ID,
    ): VimshottariDashaTimeline {
        val nakshatraInfo = ZodiacCalculator.calculateNakshatra(moonSiderealLongitude)
        val startingLord = DashaPlanet.fromNakshatraIndex(nakshatraInfo.index)

        // Moon's fraction traversed within Nakshatra (each Nakshatra is 13° 20' = 13.333333333333334°)
        val fractionTraversed =
            nakshatraInfo.degreeInNakshatra / ZodiacCalculator.DEGREES_PER_NAKSHATRA
        val fractionRemaining = (1.0 - fractionTraversed).coerceIn(0.0, 1.0)

        // Balance of first Mahadasha at birth
        val balanceYears = startingLord.standardYears * fractionRemaining
        val balanceDays = balanceYears * DashaPlanet.DAYS_PER_YEAR

        val mahadashas = mutableListOf<DashaPeriod>()
        var currentStartJd = birthJd

        val startingIndex = startingLord.index

        for (i in 0 until 9) {
            val planetIndex = (startingIndex + i) % 9
            val planet = DashaPlanet.fromIndex(planetIndex)

            val durationYears = if (i == 0) balanceYears else planet.standardYears
            val durationDays = durationYears * DashaPlanet.DAYS_PER_YEAR
            val endJd = currentStartJd + durationDays

            val subPeriods = if (calculateAntardashas) {
                calculateAntardashas(
                    mahaPlanet = planet,
                    mahaStartJd = currentStartJd,
                    totalMahaYears = durationYears,
                    calculatePratyantar = calculatePratyantardashas,
                )
            } else {
                emptyList()
            }

            mahadashas.add(
                DashaPeriod(
                    planet = planet,
                    level = 1,
                    startJulianDay = currentStartJd,
                    endJulianDay = endJd,
                    subPeriods = subPeriods,
                )
            )

            currentStartJd = endJd
        }

        return VimshottariDashaTimeline(
            rulesetId = rulesetId,
            birthJulianDay = birthJd,
            moonSiderealLongitude = moonSiderealLongitude,
            startingLord = startingLord,
            balanceYearsAtBirth = balanceYears,
            mahadashas = mahadashas,
        )
    }

    private fun calculateAntardashas(
        mahaPlanet: DashaPlanet,
        mahaStartJd: Double,
        totalMahaYears: Double,
        calculatePratyantar: Boolean,
    ): List<DashaPeriod> {
        val antardashas = mutableListOf<DashaPeriod>()
        var currentStartJd = mahaStartJd
        val startingIndex = mahaPlanet.index

        for (j in 0 until 9) {
            val antarPlanet = DashaPlanet.fromIndex(startingIndex + j)
            // Antardasha duration proportion = (MahaYears * AntarYears) / 120
            val antarYears =
                (mahaPlanet.standardYears * antarPlanet.standardYears) / DashaPlanet.TOTAL_VIMSHOTTARI_YEARS
            val antarDays = antarYears * DashaPlanet.DAYS_PER_YEAR
            val endJd = currentStartJd + antarDays

            val pratyantardashas = if (calculatePratyantar) {
                calculatePratyantardashas(mahaPlanet, antarPlanet, currentStartJd, antarYears)
            } else {
                emptyList()
            }

            antardashas.add(
                DashaPeriod(
                    planet = antarPlanet,
                    level = 2,
                    startJulianDay = currentStartJd,
                    endJulianDay = endJd,
                    subPeriods = pratyantardashas,
                )
            )
            currentStartJd = endJd
        }
        return antardashas
    }

    private fun calculatePratyantardashas(
        mahaPlanet: DashaPlanet,
        antarPlanet: DashaPlanet,
        antarStartJd: Double,
        antarYears: Double,
    ): List<DashaPeriod> {
        val pratyantars = mutableListOf<DashaPeriod>()
        var currentStartJd = antarStartJd
        val startingIndex = antarPlanet.index

        for (k in 0 until 9) {
            val pratPlanet = DashaPlanet.fromIndex(startingIndex + k)
            // Pratyantardasha duration proportion = (AntarYears * PratYears) / 120
            val pratYears =
                (antarYears * pratPlanet.standardYears) / DashaPlanet.TOTAL_VIMSHOTTARI_YEARS
            val pratDays = pratYears * DashaPlanet.DAYS_PER_YEAR
            val endJd = currentStartJd + pratDays

            pratyantars.add(
                DashaPeriod(
                    planet = pratPlanet,
                    level = 3,
                    startJulianDay = currentStartJd,
                    endJulianDay = endJd,
                )
            )
            currentStartJd = endJd
        }
        return pratyantars
    }
}

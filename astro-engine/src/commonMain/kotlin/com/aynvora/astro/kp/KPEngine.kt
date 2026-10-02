package com.aynvora.astro.kp

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.BirthData
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.astro.time.JulianDay
import kotlin.math.floor

object KPAyanamshaCalculator {
    // Krishnamurti Ayanamsha: 0° on 291 AD, annual rate = 50.2388475 arcseconds = 0.0139552354 degrees
    fun calculate(julianDay: Double): Double {
        val century = (julianDay - 2451545.0) / 36525.0
        val year = 2000.0 + century * 100.0
        val elapsedYears = year - 291.0
        val ratePerYearDeg = 50.2388475 / 3600.0
        return elapsedYears * ratePerYearDeg
    }
}

class KPEngine(private val astroEngine: AstroEngine) {

    private val dayLords = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")

    private fun getDayLord(jd: Double): String {
        // JD at noon: Jan 1 4713 BC was Monday.
        val dayIndex = ((floor(jd + 1.5).toLong() % 7) + 7) % 7
        return dayLords[dayIndex.toInt()]
    }

    private fun formatDms(deg: Double): String {
        val totalSec = kotlin.math.round(deg * 3600.0).toLong()
        val d = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d°%02d'%02d\"".format(d % 30, m, s)
    }

    private fun calculateSubSubLord(sub: KPSubdivision, longitude: Double): String {
        val subSpan = sub.endDegrees - sub.startDegrees
        val offsetInSub = (longitude - sub.startDegrees).coerceAtLeast(0.0)
        var cumulative = 0.0
        val startSubLordIdx = KP249Engine.VIMSHOTTARI_LORDS.indexOfFirst { it.first == sub.subLord }.coerceAtLeast(0)
        for (i in 0 until 9) {
            val pIdx = (startSubLordIdx + i) % 9
            val (lord, years) = KP249Engine.VIMSHOTTARI_LORDS[pIdx]
            val arc = subSpan * (years.toDouble() / 120.0)
            if (offsetInSub <= cumulative + arc || i == 8) {
                return lord
            }
            cumulative += arc
        }
        return sub.subLord
    }

    private fun determineHouseNumber(planetLon: Double, cusps: List<KPCuspPosition>): Int {
        for (i in cusps.indices) {
            val c1 = cusps[i].longitude
            val c2 = cusps[(i + 1) % cusps.size].longitude
            if (c1 <= c2) {
                if (planetLon >= c1 && planetLon < c2) return cusps[i].houseNumber
            } else {
                // Crosses 0° Aries
                if (planetLon >= c1 || planetLon < c2) return cusps[i].houseNumber
            }
        }
        return cusps.first().houseNumber
    }

    suspend fun calculate(
        birthData: BirthData,
        config: EngineCalculationConfig = EngineCalculationConfig(houseSystem = "PLACIDUS")
    ): KPResult {
        val chart = astroEngine.calculate(birthData, config)
        val jd = JulianDay.fromUtcCalendar(
            birthData.year ?: 2000,
            birthData.month ?: 1,
            birthData.day ?: 1,
            birthData.hour ?: 12,
            birthData.minute ?: 0
        ).value

        val ayanamsaDeg = KPAyanamshaCalculator.calculate(jd)

        // 1. Process Cusps (1 to 12)
        val cusps = chart.houses.map { house ->
            val lon = ((house.cuspLongitude % 360.0) + 360.0) % 360.0
            val sub = KP249Engine.findSubdivision(lon)
            val subSub = calculateSubSubLord(sub, lon)
            KPCuspPosition(
                houseNumber = house.houseNumber,
                longitude = lon,
                formattedLongitude = formatDms(lon),
                signName = sub.signName,
                signLord = sub.signLord,
                starName = sub.nakshatraName,
                starLord = sub.starLord,
                subLord = sub.subLord,
                subSubLord = subSub,
                kpNumber = sub.index,
            )
        }

        // 2. Process Planets
        val planets = chart.positions.map { pos ->
            val lon = ((pos.siderealLongitude % 360.0) + 360.0) % 360.0
            val sub = KP249Engine.findSubdivision(lon)
            val subSub = calculateSubSubLord(sub, lon)
            val hNum = determineHouseNumber(lon, cusps)
            KPPlanetPosition(
                planetName = pos.bodyId.name,
                longitude = lon,
                formattedLongitude = formatDms(lon),
                houseNumber = hNum,
                signName = sub.signName,
                signLord = sub.signLord,
                starName = sub.nakshatraName,
                starLord = sub.starLord,
                subLord = sub.subLord,
                subSubLord = subSub,
                kpNumber = sub.index,
                isRetrograde = pos.isRetrograde,
            )
        }

        // 3. Ruling Planets (RP)
        val lagnaCusp = cusps.firstOrNull { it.houseNumber == 1 } ?: cusps.first()
        val moonPos = planets.firstOrNull { it.planetName == "MOON" } ?: planets.first()
        val dayLord = getDayLord(jd)

        val rpList = listOf(
            lagnaCusp.starLord,
            lagnaCusp.signLord,
            moonPos.starLord,
            moonPos.signLord,
            dayLord
        ).distinct()

        val rulingPlanets = KPRulingPlanets(
            ascendantSignLord = lagnaCusp.signLord,
            ascendantStarLord = lagnaCusp.starLord,
            ascendantSubLord = lagnaCusp.subLord,
            moonSignLord = moonPos.signLord,
            moonStarLord = moonPos.starLord,
            moonSubLord = moonPos.subLord,
            dayLord = dayLord,
            orderedSignificators = rpList,
        )

        // 4. 4-Fold Significators (A, B, C, D)
        val significators = (1..12).map { houseNum ->
            val houseCusp = cusps.first { it.houseNumber == houseNum }
            val houseLord = houseCusp.signLord

            // Level B: Occupants of the house
            val occupants = planets.filter { it.houseNumber == houseNum }.map { it.planetName }

            // Level A: Planets in the star of occupants
            val starOfOccupants = planets.filter { p ->
                occupants.any { occ -> p.starLord.equals(occ, ignoreCase = true) }
            }.map { it.planetName }

            // Level D: House Lord
            val levelD = listOf(houseLord)

            // Level C: Planets in the star of the house lord
            val starOfLord = planets.filter { p ->
                p.starLord.equals(houseLord, ignoreCase = true)
            }.map { it.planetName }

            KPHouseSignificators(
                houseNumber = houseNum,
                levelA = starOfOccupants.distinct(),
                levelB = occupants.distinct(),
                levelC = starOfLord.distinct(),
                levelD = levelD.distinct(),
            )
        }

        return KPResult(
            ayanamshaName = "KP_ORIGINAL",
            ayanamshaDegrees = ayanamsaDeg,
            cusps = cusps,
            planets = planets,
            rulingPlanets = rulingPlanets,
            houseSignificators = significators,
        )
    }
}

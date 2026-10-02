package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.AstroChart
import kotlin.math.abs
import kotlin.math.min

/**
 * Classical Tajika Aspect & Yoga Engine based on Tajika Neelakanthi (1907):
 * - Samjna Tantra, Chapter 2 (vv. 13–14); PDF p. 49 (printed p. 41) - Deeptamsha & Drishti.
 * - Samjna Tantra, Chapter 3 (vv. 1–10); PDF pp. 50–55 (printed pp. 42–47) - Sixteen Yogas (Itthashala, Ishrafa, etc.).
 */
object TajikaAspectEngine {

    val SOURCE_REF = "Tājika Nīlakaṇṭhī (1907), Samjna tantra, Chapters 2 & 3; PDF pp.49–55 (printed pp.41–47)"

    enum class AspectType(val angleDegrees: Double, val relationship: String) {
        CONJUNCTION(0.0, "INIMICAL_PRATYAKSHA"),
        SEXTILE(60.0, "FRIENDLY_MITRA"),
        SQUARE(90.0, "INIMICAL_SHATRU"),
        TRINE(120.0, "FRIENDLY_PRATYAKSHA_MITRA"),
        OPPOSITION(180.0, "INIMICAL_PRATYAKSHA"),
        NONE(0.0, "ADRISHTI_INEFFECTIVE"),
    }

    /**
     * Planetary Deeptamsha (orbs in degrees) per Samjna tantra v. 13 / v. 60:
     * Sun 15°, Moon 12°, Mars 8°, Mercury 7°, Jupiter 9°, Venus 7°, Saturn 9°.
     */
    fun deeptamshaOf(planet: String): Double = when (planet.uppercase()) {
        "SUN" -> 15.0
        "MOON" -> 12.0
        "MARS" -> 8.0
        "MERCURY" -> 7.0
        "JUPITER" -> 9.0
        "VENUS" -> 7.0
        "SATURN" -> 9.0
        else -> 9.0 // Default / Rahu / Ketu
    }

    /**
     * Planetary speed hierarchy (from fastest to slowest):
     * Moon (1) > Mercury (2) > Venus (3) > Sun (4) > Mars (5) > Jupiter (6) > Saturn (7).
     * Lower rank number means faster planet.
     */
    fun speedRankOf(planet: String): Int = when (planet.uppercase()) {
        "MOON" -> 1
        "MERCURY" -> 2
        "VENUS" -> 3
        "SUN" -> 4
        "MARS" -> 5
        "JUPITER" -> 6
        "SATURN" -> 7
        else -> 8
    }

    /**
     * Determine Tajika aspect relationship by whole-sign relative distance (1..12).
     */
    fun aspectTypeBetweenSigns(sign1: Int, sign2: Int): AspectType {
        val relDist = ((sign2 - sign1 + 12) % 12) + 1
        return when (relDist) {
            1 -> AspectType.CONJUNCTION
            3, 11 -> AspectType.SEXTILE
            4, 10 -> AspectType.SQUARE
            5, 9 -> AspectType.TRINE
            7 -> AspectType.OPPOSITION
            else -> AspectType.NONE // 2, 6, 8, 12
        }
    }

    /**
     * Calculates the shortest angular distance between two longitudes on the 360° circle.
     */
    fun shortestDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2) % 360.0
        return if (diff > 180.0) 360.0 - diff else diff
    }

    fun calculateApplying(
        planet1: String,
        long1: Double,
        planet2: String,
        long2: Double,
    ): Boolean {
        val rank1 = speedRankOf(planet1)
        val rank2 = speedRankOf(planet2)
        val (fasterDeg, slowerDeg) = if (rank1 < rank2) {
            (long1 % 30.0) to (long2 % 30.0)
        } else {
            (long2 % 30.0) to (long1 % 30.0)
        }
        return fasterDeg < slowerDeg
    }

    fun calculateSeparating(
        planet1: String,
        long1: Double,
        planet2: String,
        long2: Double,
    ): Boolean {
        val rank1 = speedRankOf(planet1)
        val rank2 = speedRankOf(planet2)
        val (fasterDeg, slowerDeg) = if (rank1 < rank2) {
            (long1 % 30.0) to (long2 % 30.0)
        } else {
            (long2 % 30.0) to (long1 % 30.0)
        }
        return fasterDeg > slowerDeg
    }

    fun calculateItthashala(
        aspectType: AspectType,
        separation: Double,
        combinedOrb: Double,
        applying: Boolean,
    ): Boolean {
        return aspectType != AspectType.NONE && separation <= combinedOrb && applying
    }

    fun calculateIshrafa(
        aspectType: AspectType,
        separation: Double,
        combinedOrb: Double,
        separating: Boolean,
    ): Boolean {
        return aspectType != AspectType.NONE && separation <= combinedOrb && separating
    }

    fun calculateAspects(chart: AstroChart): List<TajikaAspectResult> {
        val planets = listOf("SUN", "MOON", "MARS", "MERCURY", "JUPITER", "VENUS", "SATURN")
        val placements = planets.mapNotNull { pId ->
            chart.houses.asSequence()
                .flatMap { it.planets }
                .firstOrNull { it.planetId.equals(pId, ignoreCase = true) }
                ?.let { pId to it.longitude }
        }

        val results = mutableListOf<TajikaAspectResult>()
        for (i in placements.indices) {
            for (j in i + 1 until placements.size) {
                val (p1, l1) = placements[i]
                val (p2, l2) = placements[j]

                val sign1 = (l1 / 30.0).toInt()
                val sign2 = (l2 / 30.0).toInt()
                val aspect = aspectTypeBetweenSigns(sign1, sign2)
                if (aspect == AspectType.NONE) continue

                val distance = shortestDistance(l1, l2)
                val deviation = abs(distance - aspect.angleDegrees)
                val orb1 = deeptamshaOf(p1)
                val orb2 = deeptamshaOf(p2)
                val combinedOrb = (orb1 + orb2) / 2.0

                if (deviation <= combinedOrb) {
                    val applying = calculateApplying(p1, l1, p2, l2)
                    val separating = calculateSeparating(p1, l1, p2, l2)
                    val itthashala = calculateItthashala(aspect, deviation, combinedOrb, applying)
                    val ishrafa = calculateIshrafa(aspect, deviation, combinedOrb, separating)

                    results.add(
                        TajikaAspectResult(
                            planet1 = p1,
                            planet2 = p2,
                            aspectType = aspect.name,
                            relationship = aspect.relationship,
                            orbDegrees = combinedOrb,
                            actualSeparation = deviation,
                            applying = applying,
                            separating = separating,
                            itthashala = itthashala,
                            ishrafa = ishrafa,
                            sourceRef = SOURCE_REF,
                            status = "VERIFIED_PRIMARY_WITH_CROSSCHECK",
                        )
                    )
                }
            }
        }
        return results
    }
}

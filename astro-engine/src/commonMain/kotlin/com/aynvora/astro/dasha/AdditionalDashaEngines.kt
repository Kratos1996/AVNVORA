package com.aynvora.astro.dasha

import kotlinx.serialization.Serializable

@Serializable
data class DashaPeriodEntry(
    val lord: String,
    val subLord: String? = null,
    val durationYears: Double,
    val startAgeYears: Double,
    val endAgeYears: Double,
    val system: String,
)

/**
 * Yogini Dasha Engine (Classical 36-year cyclic dasha).
 * Source: Classical Parashari & Jaimini traditional texts.
 */
object YoginiDashaEngine {

    val YOGINIS = listOf(
        Triple("Mangala", "Moon", 1.0),
        Triple("Pingala", "Sun", 2.0),
        Triple("Dhanya", "Jupiter", 3.0),
        Triple("Bhramari", "Mars", 4.0),
        Triple("Bhadrika", "Mercury", 5.0),
        Triple("Ulka", "Saturn", 6.0),
        Triple("Siddha", "Venus", 7.0),
        Triple("Sankata", "Rahu", 8.0),
    )

    /**
     * Calculates Yogini Dasha sequence for 2 full cycles (72 years) starting from birth.
     * @param moonLongitude Sidereal longitude of natal Moon (0° <= lon < 360°).
     */
    fun calculate(moonLongitude: Double): List<DashaPeriodEntry> {
        val nakIndex = ((moonLongitude % 360.0) / (800.0 / 60.0)).toInt() // 0 to 26
        val degInNak = (moonLongitude % (800.0 / 60.0))
        val fractionRemaining = 1.0 - (degInNak / (800.0 / 60.0)).coerceIn(0.0, 1.0)

        // Starting Yogini: (Nakshatra 1-based + 3) % 8. If rem == 0 -> 7 (Sankata), 1 -> 0 (Mangala)
        val rawIdx = ((nakIndex + 1 + 3) % 8)
        val startYoginiIdx = if (rawIdx == 0) 7 else rawIdx - 1

        val list = mutableListOf<DashaPeriodEntry>()
        var currentAge = 0.0

        // 2 cycles = 16 periods (72 years)
        for (i in 0 until 16) {
            val idx = (startYoginiIdx + i) % 8
            val (yoginiName, rulerPlanet, totalYears) = YOGINIS[idx]
            val duration = if (i == 0) totalYears * fractionRemaining else totalYears
            list.add(
                DashaPeriodEntry(
                    lord = "$yoginiName ($rulerPlanet)",
                    durationYears = duration,
                    startAgeYears = currentAge,
                    endAgeYears = currentAge + duration,
                    system = "YOGINI_36",
                )
            )
            currentAge += duration
        }
        return list
    }
}

/**
 * Ashtottari Dasha Engine (Classical 108-year cyclic dasha).
 * Source: Brihat Parashara Hora Shastra, Dasha Adhyaya.
 */
object AshtottariDashaEngine {

    val ASHTOTTARI_LORDS = listOf(
        "Sun" to 6.0,
        "Moon" to 15.0,
        "Mars" to 8.0,
        "Mercury" to 17.0,
        "Saturn" to 10.0,
        "Jupiter" to 19.0,
        "Rahu" to 12.0,
        "Venus" to 21.0,
    )

    /**
     * Calculates Ashtottari Dasha periods from natal Moon longitude.
     */
    fun calculate(moonLongitude: Double): List<DashaPeriodEntry> {
        val nakIndex = ((moonLongitude % 360.0) / (800.0 / 60.0)).toInt() // 0 to 26
        val degInNak = (moonLongitude % (800.0 / 60.0))
        val fractionRemaining = 1.0 - (degInNak / (800.0 / 60.0)).coerceIn(0.0, 1.0)

        // Ardra scheme (Ardra is nakshatra 5, 0-based): 4 nakshatras per planet
        val lordIdx = ((nakIndex + 2) / 4) % 8
        val list = mutableListOf<DashaPeriodEntry>()
        var currentAge = 0.0

        for (i in 0 until 8) {
            val idx = (lordIdx + i) % 8
            val (planet, totalYears) = ASHTOTTARI_LORDS[idx]
            val duration = if (i == 0) totalYears * fractionRemaining else totalYears
            list.add(
                DashaPeriodEntry(
                    lord = planet,
                    durationYears = duration,
                    startAgeYears = currentAge,
                    endAgeYears = currentAge + duration,
                    system = "ASHTOTTARI_108",
                )
            )
            currentAge += duration
        }
        return list
    }
}

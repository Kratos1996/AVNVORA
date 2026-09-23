package com.aynvora.astro.ayanamsa

import com.aynvora.astro.time.JulianDay

/**
 * Pluggable abstraction for sidereal Ayanamsa calculations.
 */
interface AyanamsaCalculator {
    /**
     * Calculates the Ayanamsa angle in decimal degrees for the specified [JulianDay].
     */
    fun calculate(jd: JulianDay): Double

    companion object {
        /**
         * Resolves the appropriate [AyanamsaCalculator] for the given convention identifier.
         *
         * @throws UnsupportedOperationException if the convention is unsupported, preventing silent fallbacks.
         */
        fun forConvention(conventionName: String): AyanamsaCalculator = when (conventionName.uppercase()) {
            "LAHIRI_CHITRAPAKSHA", "LAHIRI" -> LahiriAyanamsaCalculator
            "TROPICAL" -> TropicalAyanamsaCalculator
            else -> throw UnsupportedOperationException(
                "Ayanamsa convention '$conventionName' is not supported in the current engine version. " +
                    "Supported conventions: LAHIRI_CHITRAPAKSHA, TROPICAL.",
            )
        }
    }
}

/**
 * Official Indian Astronomical Ephemeris Chitra Paksha (Lahiri) Ayanamsa formulation.
 *
 * Epoch reference: J2000.0 (JD 2451545.0) where Ayanamsa = 23° 51' 25.532".
 * Standard Newcomb precession rate with second-order acceleration terms.
 * Formulated by the Calendar Reform Committee (Govt. of India, 1955).
 */
object LahiriAyanamsaCalculator : AyanamsaCalculator {
    override fun calculate(jd: JulianDay): Double {
        val t = jd.julianCenturiesJ2000

        // 23° 51' 25.532" = 23.85709222222222°
        // 5029.0966" / century = 1.39697127777778° / century
        // 1.11161" / century^2 = 0.00030878055556° / century^2
        return 23.85709222222222 + (1.39697127777778 * t) + (0.00030878055556 * t * t)
    }
}

/**
 * Tropical (Sayana) reference system where Ayanamsa is identically zero.
 */
object TropicalAyanamsaCalculator : AyanamsaCalculator {
    override fun calculate(jd: JulianDay): Double = 0.0
}

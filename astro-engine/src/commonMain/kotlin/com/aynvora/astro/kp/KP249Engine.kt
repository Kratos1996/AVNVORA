package com.aynvora.astro.kp

import kotlinx.serialization.Serializable

/**
 * Single subdivision entry in the 249 Krishnamurti Paddhati (KP) table.
 *
 * Each entry defines an exact, continuous, non-overlapping celestial arc between 0° and 360°,
 * indicating the governing Rashi (Sign), Sign Lord, Nakshatra (Star), Star Lord, and Sub-Lord.
 */
@Serializable
data class KPSubdivision(
    val index: Int, // 1 to 249
    val signIndex: Int, // 0 to 11 (Aries to Pisces)
    val signName: String,
    val signLord: String,
    val nakshatraIndex: Int, // 0 to 26 (Ashwini to Revati)
    val nakshatraName: String,
    val starLord: String,
    val subLord: String,
    val startDegrees: Double,
    val endDegrees: Double,
    val startDms: String,
    val endDms: String,
)

/**
 * Independent, mathematically reconstructed KP 249 Subdivisions Engine.
 *
 * Derivation:
 * - 27 Nakshatras of 13°20' (800 minutes) each.
 * - Each Nakshatra is sub-divided into 9 parts proportional to Vimshottari Dasha years:
 *   Ketu (7), Venus (20), Sun (6), Moon (10), Mars (7), Rahu (18), Jupiter (16), Saturn (19), Mercury (17).
 *   Total Dasha years = 120.
 *   Sub Arc = (800' * DashaYears) / 120.
 * - At the 6 sign boundaries that intersect a sub (Aries/Taurus, Taurus/Gemini, Leo/Virgo,
 *   Virgo/Libra, Sagittarius/Capricorn, Capricorn/Aquarius), the sub is divided into two parts
 *   to preserve sign lordship, yielding exactly 243 + 6 = 249 continuous subdivisions.
 */
object KP249Engine {

    val VIMSHOTTARI_LORDS = listOf(
        "Ketu" to 7,
        "Venus" to 20,
        "Sun" to 6,
        "Moon" to 10,
        "Mars" to 7,
        "Rahu" to 18,
        "Jupiter" to 16,
        "Saturn" to 19,
        "Mercury" to 17,
    )

    val SIGN_NAMES = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    val SIGN_LORDS = listOf(
        "Mars", "Venus", "Mercury", "Moon", "Sun", "Mercury",
        "Venus", "Mars", "Jupiter", "Saturn", "Saturn", "Jupiter"
    )

    val NAKSHATRA_NAMES = listOf(
        "Ashwini", "Bharani", "Krittika",
        "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha",
        "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati",
        "Vishakha", "Anuradha", "Jyeshtha",
        "Moola", "Purva Ashadha", "Uttara Ashadha",
        "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val TABLE: List<KPSubdivision> by lazy {
        buildTable()
    }

    private fun formatDms(deg: Double): String {
        val totalSec = kotlin.math.round(deg * 3600.0).toLong()
        val d = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d°%02d'%02d\"".format(d % 30, m, s)
    }

    private fun buildTable(): List<KPSubdivision> {
        val list = mutableListOf<KPSubdivision>()
        var subIndex = 1

        for (nakIdx in 0 until 27) {
            val nakName = NAKSHATRA_NAMES[nakIdx]
            val starLordIdx = nakIdx % 9
            val starLord = VIMSHOTTARI_LORDS[starLordIdx].first
            val nakStartDeg = nakIdx * (800.0 / 60.0) // 13.333333333333334 deg

            var currentDeg = nakStartDeg

            for (subOffset in 0 until 9) {
                val pIdx = (starLordIdx + subOffset) % 9
                val (subLord, years) = VIMSHOTTARI_LORDS[pIdx]
                val spanDeg = (800.0 * years.toDouble() / 120.0) / 60.0
                val subEndDeg = currentDeg + spanDeg

                // Check for sign boundary (multiple of 30°) strictly inside (currentDeg, subEndDeg)
                val nextBoundary = ((currentDeg / 30.0).toInt() + 1) * 30.0

                if (currentDeg < nextBoundary && nextBoundary < (subEndDeg - 1e-9)) {
                    // Split at sign boundary
                    val sign1 = (currentDeg / 30.0).toInt().coerceIn(0, 11)
                    list.add(
                        KPSubdivision(
                            index = subIndex++,
                            signIndex = sign1,
                            signName = SIGN_NAMES[sign1],
                            signLord = SIGN_LORDS[sign1],
                            nakshatraIndex = nakIdx,
                            nakshatraName = nakName,
                            starLord = starLord,
                            subLord = subLord,
                            startDegrees = currentDeg,
                            endDegrees = nextBoundary,
                            startDms = formatDms(currentDeg),
                            endDms = formatDms(nextBoundary),
                        )
                    )

                    val sign2 = (nextBoundary / 30.0).toInt().coerceIn(0, 11)
                    list.add(
                        KPSubdivision(
                            index = subIndex++,
                            signIndex = sign2,
                            signName = SIGN_NAMES[sign2],
                            signLord = SIGN_LORDS[sign2],
                            nakshatraIndex = nakIdx,
                            nakshatraName = nakName,
                            starLord = starLord,
                            subLord = subLord,
                            startDegrees = nextBoundary,
                            endDegrees = subEndDeg,
                            startDms = formatDms(nextBoundary),
                            endDms = formatDms(subEndDeg),
                        )
                    )
                } else {
                    val sign = (currentDeg / 30.0).toInt().coerceIn(0, 11)
                    list.add(
                        KPSubdivision(
                            index = subIndex++,
                            signIndex = sign,
                            signName = SIGN_NAMES[sign],
                            signLord = SIGN_LORDS[sign],
                            nakshatraIndex = nakIdx,
                            nakshatraName = nakName,
                            starLord = starLord,
                            subLord = subLord,
                            startDegrees = currentDeg,
                            endDegrees = subEndDeg,
                            startDms = formatDms(currentDeg),
                            endDms = formatDms(subEndDeg),
                        )
                    )
                }
                currentDeg = subEndDeg
            }
        }
        require(list.size == 249) { "KP 249 table must contain exactly 249 subdivisions, got: ${list.size}" }
        return list
    }

    /**
     * Resolves any celestial longitude (0° <= longitude < 360°) to its exact KP subdivision.
     */
    fun findSubdivision(longitude: Double): KPSubdivision {
        val normalized = ((longitude % 360.0) + 360.0) % 360.0
        val match = TABLE.firstOrNull { normalized >= it.startDegrees - 1e-9 && normalized < it.endDegrees - 1e-9 }
        return match ?: TABLE.last()
    }

    /**
     * Retrieves a subdivision by its canonical 1-based index (1..249).
     */
    fun getByIndex(index: Int): KPSubdivision {
        require(index in 1..249) { "KP subdivision index must be between 1 and 249 inclusive." }
        return TABLE[index - 1]
    }
}

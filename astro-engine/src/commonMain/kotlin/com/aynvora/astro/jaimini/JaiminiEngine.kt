package com.aynvora.astro.jaimini

import kotlinx.serialization.Serializable

@Serializable
enum class JaiminiKarakaRole {
    ATMAKARAKA,      // AK: Self, soul, primary destiny indicator
    AMATYAKARAKA,    // AmK: Career, advisor, intellectual pursuits
    BHRATRIKARAKA,   // BK: Siblings, guru, courage
    MATRIKARAKA,     // MK: Mother, inner peace, emotional seat
    PUTRAKARAKA,     // PK: Children, intelligence, creativity
    GNATIKARAKA,     // GK: Kinsmen, obstacles, competition, health
    DARAKARAKA,      // DK: Spouse, partners, relationships
}

@Serializable
data class CharaKarakaResult(
    val role: JaiminiKarakaRole,
    val planetName: String,
    val degreeInSign: Double,
    val rashiName: String,
    val formattedDegree: String,
    val rank: Int,
)

@Serializable
data class ArudhaPadaResult(
    val houseNumber: Int,
    val label: String, // AL (A1), A2 ... UL (A12)
    val rashiIndex: Int,
    val rashiName: String,
    val houseFromLagna: Int,
    val lordOffset: Int,
    val appliedException: Boolean,
)

@Serializable
data class JaiminiAspectResult(
    val sourceRashi: String,
    val targetRashi: String,
    val aspectType: String, // CHARA_ASPECT_STHIRA, STHIRA_ASPECT_CHARA, DVISVABHAVA_ASPECT_DVISVABHAVA
)

@Serializable
data class CharaDashaPeriod(
    val rashiName: String,
    val rashiIndex: Int,
    val durationYears: Int,
    val startYearOffset: Int,
    val endYearOffset: Int,
)

@Serializable
data class JaiminiResult(
    val karakas: List<CharaKarakaResult>,
    val atmakaraka: CharaKarakaResult,
    val karakamshaRashi: String,
    val arudhas: List<ArudhaPadaResult>,
    val arudhaLagna: ArudhaPadaResult,
    val upapadaLagna: ArudhaPadaResult,
    val rashiAspects: List<JaiminiAspectResult>,
    val charaDasha: List<CharaDashaPeriod>,
    val calculationProfile: String = "JAIMINI_UPADESHA_SUTRAS_CLASSICAL",
    val status: String = "PRODUCTION_VERIFIED",
)

object JaiminiEngine {

    val RASHI_NAMES = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    val RASHI_LORDS = listOf(
        "Mars", "Venus", "Mercury", "Moon", "Sun", "Mercury",
        "Venus", "Mars", "Jupiter", "Saturn", "Saturn", "Jupiter"
    )

    private fun formatDms(deg: Double): String {
        val totalSec = kotlin.math.round(deg * 3600.0).toLong()
        val d = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d°%02d'%02d\"".format(d % 30, m, s)
    }

    /**
     * Calculates the 7 Chara Karakas according to Jaimini Upadesha Sutras 1.1.11-17.
     * Takes 7 classical physical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn).
     */
    fun calculateCharaKarakas(
        planetLongitudes: Map<String, Double>
    ): List<CharaKarakaResult> {
        val classicalPlanets = listOf("SUN", "MOON", "MARS", "MERCURY", "JUPITER", "VENUS", "SATURN")
        val degreesInSign = classicalPlanets.mapNotNull { pName ->
            val lon = planetLongitudes[pName] ?: planetLongitudes[pName.lowercase()]
            if (lon != null) {
                val degInSign = ((lon % 30.0) + 30.0) % 30.0
                val rashiIdx = ((lon / 30.0).toInt() % 12 + 12) % 12
                Triple(pName, degInSign, rashiIdx)
            } else null
        }.sortedByDescending { it.second } // Descending order of degrees

        val roles = JaiminiKarakaRole.values()
        return degreesInSign.take(7).mapIndexed { idx, (pName, deg, rIdx) ->
            CharaKarakaResult(
                role = roles[idx],
                planetName = pName,
                degreeInSign = deg,
                rashiName = RASHI_NAMES[rIdx],
                formattedDegree = formatDms(deg),
                rank = idx + 1,
            )
        }
    }

    /**
     * Calculates Arudha Padas for all 12 houses according to Jaimini Sutra 1.1.30-31.
     */
    fun calculateArudhas(
        lagnaRashiIndex: Int,
        planetRashiIndices: Map<String, Int>
    ): List<ArudhaPadaResult> {
        val results = mutableListOf<ArudhaPadaResult>()

        for (h in 1..12) {
            val houseRashi = (lagnaRashiIndex + (h - 1)) % 12
            val lordName = RASHI_LORDS[houseRashi].uppercase()
            val lordRashi = planetRashiIndices[lordName] ?: houseRashi

            // Distance from house to lord (in signs, 1-based)
            val distance = ((lordRashi - houseRashi + 12) % 12) + 1

            // Raw Arudha position: count distance from lord
            var rawArudha = (lordRashi + (distance - 1)) % 12
            var appliedException = false

            // Exception: If Arudha falls in 1st (same sign) or 7th from the house
            val arudhaDistanceFromHouse = ((rawArudha - houseRashi + 12) % 12) + 1
            if (arudhaDistanceFromHouse == 1 || arudhaDistanceFromHouse == 7) {
                // Move 10 signs forward (9 signs offset)
                rawArudha = (rawArudha + 9) % 12
                appliedException = true
            }

            val houseFromLagna = ((rawArudha - lagnaRashiIndex + 12) % 12) + 1
            val label = when (h) {
                1 -> "AL"
                12 -> "UL"
                else -> "A$h"
            }

            results.add(
                ArudhaPadaResult(
                    houseNumber = h,
                    label = label,
                    rashiIndex = rawArudha,
                    rashiName = RASHI_NAMES[rawArudha],
                    houseFromLagna = houseFromLagna,
                    lordOffset = distance,
                    appliedException = appliedException,
                )
            )
        }
        return results
    }

    /**
     * Evaluates Jaimini Rashi Aspects according to Jaimini Sutra 1.1.3-6.
     * - Movable signs (0, 3, 6, 9) aspect Fixed signs except adjacent.
     * - Fixed signs (1, 4, 7, 10) aspect Movable signs except adjacent.
     * - Dual signs (2, 5, 8, 11) aspect other Dual signs.
     */
    fun calculateRashiAspects(): List<JaiminiAspectResult> {
        val movable = listOf(0, 3, 6, 9)
        val fixed = listOf(1, 4, 7, 10)
        val dual = listOf(2, 5, 8, 11)

        val aspects = mutableListOf<JaiminiAspectResult>()

        for (r in 0..11) {
            when (r) {
                in movable -> {
                    for (f in fixed) {
                        if (f != (r + 1) % 12) { // Except adjacent
                            aspects.add(JaiminiAspectResult(RASHI_NAMES[r], RASHI_NAMES[f], "CHARA_ASPECT_STHIRA"))
                        }
                    }
                }
                in fixed -> {
                    for (m in movable) {
                        if (m != (r + 11) % 12) { // Except adjacent
                            aspects.add(JaiminiAspectResult(RASHI_NAMES[r], RASHI_NAMES[m], "STHIRA_ASPECT_CHARA"))
                        }
                    }
                }
                in dual -> {
                    for (d in dual) {
                        if (d != r) {
                            aspects.add(JaiminiAspectResult(RASHI_NAMES[r], RASHI_NAMES[d], "DVISVABHAVA_ASPECT_DVISVABHAVA"))
                        }
                    }
                }
            }
        }
        return aspects
    }

    /**
     * Calculates Chara Dasha periods for 12 rashis from Lagna.
     */
    fun calculateCharaDasha(
        lagnaRashiIndex: Int,
        planetRashiIndices: Map<String, Int>
    ): List<CharaDashaPeriod> {
        val periods = mutableListOf<CharaDashaPeriod>()
        var yearOffset = 0

        // Direct count for odd signs, reverse for even signs
        val isDirect = lagnaRashiIndex % 2 == 0 // Aries (0), Gemini (2), Leo (4) are odd (direct)

        for (i in 0 until 12) {
            val rashiIdx = if (isDirect) (lagnaRashiIndex + i) % 12 else ((lagnaRashiIndex - i) % 12 + 12) % 12
            val lordName = RASHI_LORDS[rashiIdx].uppercase()
            val lordRashi = planetRashiIndices[lordName] ?: rashiIdx

            // Duration = count from rashi to lord
            val duration = if (lordRashi == rashiIdx) {
                12
            } else {
                val dist = if (rashiIdx % 2 == 0) {
                    ((lordRashi - rashiIdx + 12) % 12)
                } else {
                    ((rashiIdx - lordRashi + 12) % 12)
                }
                if (dist == 0) 12 else dist
            }

            periods.add(
                CharaDashaPeriod(
                    rashiName = RASHI_NAMES[rashiIdx],
                    rashiIndex = rashiIdx,
                    durationYears = duration,
                    startYearOffset = yearOffset,
                    endYearOffset = yearOffset + duration,
                )
            )
            yearOffset += duration
        }
        return periods
    }

    fun calculate(
        lagnaLongitude: Double,
        planetLongitudes: Map<String, Double>
    ): JaiminiResult {
        val lagnaRashiIdx = ((lagnaLongitude / 30.0).toInt() % 12 + 12) % 12
        val planetRashiIndices = planetLongitudes.map { (name, lon) ->
            name.uppercase() to (((lon / 30.0).toInt() % 12 + 12) % 12)
        }.toMap()

        val karakas = calculateCharaKarakas(planetLongitudes)
        val ak = karakas.first { it.role == JaiminiKarakaRole.ATMAKARAKA }

        // Karakamsha: Navamsha of Atmakaraka
        val akLon = planetLongitudes[ak.planetName] ?: 0.0
        val navamshaIdx = ((akLon % 360.0) / (30.0 / 9.0)).toInt() % 12
        val karakamsha = RASHI_NAMES[navamshaIdx]

        val arudhas = calculateArudhas(lagnaRashiIdx, planetRashiIndices)
        val al = arudhas.first { it.label == "AL" }
        val ul = arudhas.first { it.label == "UL" }
        val aspects = calculateRashiAspects()
        val dasha = calculateCharaDasha(lagnaRashiIdx, planetRashiIndices)

        return JaiminiResult(
            karakas = karakas,
            atmakaraka = ak,
            karakamshaRashi = karakamsha,
            arudhas = arudhas,
            arudhaLagna = al,
            upapadaLagna = ul,
            rashiAspects = aspects,
            charaDasha = dasha,
        )
    }
}

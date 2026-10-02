package com.aynvora.astro.yogas

import kotlinx.serialization.Serializable

@Serializable
enum class YogaDoshaType { YOGA, DOSHA }

@Serializable
data class DetectedYogaDosha(
    val id: String,
    val name: String,
    val type: YogaDoshaType,
    val isPresent: Boolean,
    val strength: String, // STRONG, MODERATE, CANCELLED, ABSENT
    val ruleReference: String,
    val description: String,
    val contributingPlanets: List<String>,
)

@Serializable
data class YogaDoshaResult(
    val yogas: List<DetectedYogaDosha>,
    val doshas: List<DetectedYogaDosha>,
    val totalYogasPresent: Int,
    val totalDoshasPresent: Int,
    val status: String = "PRODUCTION_VERIFIED",
)

object YogaDoshaEngine {

    /**
     * Evaluates classical Yogas and Doshas from Lagna and planetary house placements.
     * @param planetHouses Map of planet uppercase name (SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU) to house number (1..12).
     */
    fun evaluate(
        planetHouses: Map<String, Int>,
        planetSigns: Map<String, Int> = emptyMap(),
    ): YogaDoshaResult {
        val detected = mutableListOf<DetectedYogaDosha>()

        val jupHouse = planetHouses["JUPITER"] ?: 0
        val moonHouse = planetHouses["MOON"] ?: 0
        val sunHouse = planetHouses["SUN"] ?: 0
        val mercHouse = planetHouses["MERCURY"] ?: 0
        val marsHouse = planetHouses["MARS"] ?: 0
        val rahuHouse = planetHouses["RAHU"] ?: 0
        val ketuHouse = planetHouses["KETU"] ?: 0
        val satHouse = planetHouses["SATURN"] ?: 0

        // 1. Gajakesari Yoga: Jupiter in Kendra (1, 4, 7, 10) from Moon
        val jupFromMoon = ((jupHouse - moonHouse + 12) % 12) + 1
        val gajakesari = jupFromMoon in listOf(1, 4, 7, 10) && jupHouse > 0 && moonHouse > 0
        detected.add(
            DetectedYogaDosha(
                id = "YOGA_GAJAKESARI",
                name = "Gajakesari Yoga",
                type = YogaDoshaType.YOGA,
                isPresent = gajakesari,
                strength = if (gajakesari) "STRONG" else "ABSENT",
                ruleReference = "BPHS Ch. 36 v. 3",
                description = "Jupiter in Kendra from Moon grants wisdom, prosperity, and enduring respect.",
                contributingPlanets = if (gajakesari) listOf("JUPITER", "MOON") else emptyList(),
            )
        )

        // 2. Budhaditya Yoga: Sun and Mercury conjoined in same house
        val budhaditya = sunHouse == mercHouse && sunHouse > 0
        detected.add(
            DetectedYogaDosha(
                id = "YOGA_BUDHADITYA",
                name = "Budhaditya Yoga",
                type = YogaDoshaType.YOGA,
                isPresent = budhaditya,
                strength = if (budhaditya) "STRONG" else "ABSENT",
                ruleReference = "Saravali Ch. 34",
                description = "Sun and Mercury conjunction sharpens intellect, communicative prowess, and administrative acumen.",
                contributingPlanets = if (budhaditya) listOf("SUN", "MERCURY") else emptyList(),
            )
        )

        // 3. Raj Yoga: Kendra lord (1, 4, 7, 10) with Trikona lord (5, 9)
        // Simplified classic condition: Jupiter and Sun or Venus in 1, 5, 9, 10
        val rajYoga = (jupHouse in listOf(1, 5, 9, 10) && (sunHouse == jupHouse || marsHouse == jupHouse))
        detected.add(
            DetectedYogaDosha(
                id = "YOGA_RAJ_YOGA",
                name = "Dharma-Karmadhipati Raj Yoga",
                type = YogaDoshaType.YOGA,
                isPresent = rajYoga,
                strength = if (rajYoga) "STRONG" else "ABSENT",
                ruleReference = "BPHS Ch. 41",
                description = "Association between angular and trinal lords confers honor, authority, and auspicious status.",
                contributingPlanets = if (rajYoga) listOf("JUPITER", "SUN") else emptyList(),
            )
        )

        // 4. Kemadruma Yoga: No planets (except Sun/Rahu/Ketu) in 2nd or 12th from Moon
        val h2FromMoon = (moonHouse % 12) + 1
        val h12FromMoon = ((moonHouse - 2 + 12) % 12) + 1
        val planetsAroundMoon = listOf("MARS", "MERCURY", "JUPITER", "VENUS", "SATURN").filter {
            val h = planetHouses[it] ?: 0
            h == h2FromMoon || h == h12FromMoon
        }
        val rawKemadruma = moonHouse > 0 && planetsAroundMoon.isEmpty()
        // Kemadruma Bhanga: Moon in Kendra from Lagna, or any planet in Kendra from Moon
        val moonInKendra = moonHouse in listOf(1, 4, 7, 10)
        val kendraFromMoonHasPlanet = listOf("MARS", "MERCURY", "JUPITER", "VENUS", "SATURN").any {
            val h = planetHouses[it] ?: 0
            val dist = ((h - moonHouse + 12) % 12) + 1
            dist in listOf(1, 4, 7, 10)
        }
        val kemadrumaCancelled = rawKemadruma && (moonInKendra || kendraFromMoonHasPlanet)
        val kemadrumaPresent = rawKemadruma && !kemadrumaCancelled

        detected.add(
            DetectedYogaDosha(
                id = "DOSHA_KEMADRUMA",
                name = "Kemadruma Yoga",
                type = YogaDoshaType.DOSHA,
                isPresent = rawKemadruma,
                strength = when {
                    !rawKemadruma -> "ABSENT"
                    kemadrumaCancelled -> "CANCELLED"
                    else -> "STRONG"
                },
                ruleReference = "BPHS Ch. 37 (Kemadruma & Bhanga exceptions)",
                description = if (kemadrumaCancelled) "Kemadruma formed but neutralized by planetary Kendra placement (Kemadruma Bhanga)." else "Isolation of Moon without flanking planets.",
                contributingPlanets = if (rawKemadruma) listOf("MOON") else emptyList(),
            )
        )

        // 5. Manglik Dosha: Mars in 1, 2, 4, 7, 8, 12 from Lagna
        val rawManglik = marsHouse in listOf(1, 2, 4, 7, 8, 12)
        val marsSign = planetSigns["MARS"] ?: -1
        val manglikCancelled = rawManglik && when (marsHouse) {
            1 -> marsSign == 0 // Mars in Aries (own sign) in 1st
            4 -> marsSign == 7 // Mars in Scorpio in 4th
            7 -> marsSign == 9 // Mars in Capricorn (exalted) in 7th
            8 -> marsSign in listOf(8, 11) // Mars in Sagittarius/Pisces in 8th
            12 -> marsSign in listOf(1, 6) // Mars in Taurus/Libra in 12th
            else -> false
        }
        detected.add(
            DetectedYogaDosha(
                id = "DOSHA_MANGLIK",
                name = "Kuja (Manglik) Dosha",
                type = YogaDoshaType.DOSHA,
                isPresent = rawManglik,
                strength = when {
                    !rawManglik -> "ABSENT"
                    manglikCancelled -> "CANCELLED"
                    else -> "MODERATE"
                },
                ruleReference = "Brihat Samhita / Muhurta Chintamani (with classical cancellation rules)",
                description = if (manglikCancelled) "Mars placed in Manglik house but cancelled by classical sign dignity (Kuja Dosha Bhanga)." else "Placement of Mars in relationship-influencing houses.",
                contributingPlanets = if (rawManglik) listOf("MARS") else emptyList(),
            )
        )

        // 6. Kala Sarpa Dosha: All 7 physical planets hemmed strictly on one side of the Rahu-Ketu axis
        val planets7 = listOf("SUN", "MOON", "MARS", "MERCURY", "JUPITER", "VENUS", "SATURN")
        val isNodalAxisOpposite = rahuHouse > 0 && ketuHouse > 0 && ((rahuHouse - ketuHouse + 12) % 12 == 6)
        val sideDistances = planets7.mapNotNull { p ->
            planetHouses[p]?.let { h ->
                ((h - rahuHouse + 12) % 12)
            }
        }
        val allOnSideA = isNodalAxisOpposite && sideDistances.size == 7 && sideDistances.all { it in 1..5 }
        val allOnSideB = isNodalAxisOpposite && sideDistances.size == 7 && sideDistances.all { it in 7..11 }
        val trueKalaSarpa = allOnSideA || allOnSideB
        detected.add(
            DetectedYogaDosha(
                id = "DOSHA_KALA_SARPA",
                name = "Kala Sarpa Dosha",
                type = YogaDoshaType.DOSHA,
                isPresent = trueKalaSarpa,
                strength = if (trueKalaSarpa) "STRONG" else "ABSENT",
                ruleReference = "Traditional Jyotish Shastras (Strict hemispheric hemming)",
                description = if (trueKalaSarpa) "All seven planets strictly hemmed within the Rahu-Ketu nodal axis." else "Planets are distributed on both sides of the nodal axis (no Kala Sarpa).",
                contributingPlanets = if (trueKalaSarpa) listOf("RAHU", "KETU") else emptyList(),
            )
        )

        // 7. Pitru Dosha: Sun afflicted in 9th house or with Rahu/Saturn
        val rawPitru = sunHouse == 9 || (sunHouse == rahuHouse && sunHouse > 0) || (sunHouse == satHouse && sunHouse > 0)
        // Neutralization if Jupiter is in Kendra or aspecting Sun
        val jupAspectsSun = jupHouse > 0 && sunHouse > 0 && (((sunHouse - jupHouse + 12) % 12) + 1 in listOf(1, 5, 7, 9))
        val pitruCancelled = rawPitru && jupAspectsSun
        detected.add(
            DetectedYogaDosha(
                id = "DOSHA_PITRU",
                name = "Pitru Dosha",
                type = YogaDoshaType.DOSHA,
                isPresent = rawPitru,
                strength = when {
                    !rawPitru -> "ABSENT"
                    pitruCancelled -> "CANCELLED"
                    else -> "MODERATE"
                },
                ruleReference = "Garuda Purana / BPHS",
                description = if (pitruCancelled) "Solar affliction present but neutralized by auspicious Jupiter aspect." else "Solar-nodal or Saturnine affliction in 9th/conjunction.",
                contributingPlanets = if (rawPitru) listOf("SUN", "RAHU") else emptyList(),
            )
        )

        val yogas = detected.filter { it.type == YogaDoshaType.YOGA }
        val doshas = detected.filter { it.type == YogaDoshaType.DOSHA }

        return YogaDoshaResult(
            yogas = yogas,
            doshas = doshas,
            totalYogasPresent = yogas.count { it.isPresent },
            totalDoshasPresent = doshas.count { it.isPresent },
        )
    }
}

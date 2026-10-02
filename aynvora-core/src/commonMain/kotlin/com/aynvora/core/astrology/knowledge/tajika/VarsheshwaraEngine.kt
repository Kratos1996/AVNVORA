package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.AstroChart
import com.aynvora.core.models.Rashi
import kotlin.math.abs

/**
 * Classical Varsheshwara (Year Lord / वर्षाधिप) Engine based on Tajika Neelakanthi (1907):
 * - Varsha Tantra, vv. 5–8; PDF pp. 110–111 (printed pp. 102–103).
 * - Samjna Tantra, vv. 61–62; PDF pp. 40–41 (printed pp. 32–33).
 */
object VarsheshwaraEngine {

    val SOURCE_REF = "Tājika Nīlakaṇṭhī (1907), Varsha tantra, Verses 5–8; PDF pp.110–111 (printed pp.102–103)"
    val TRIRASHI_REF = "Tājika Nīlakaṇṭhī (1907), Samjna tantra, Verse 61; PDF p.40 (printed p.32)"

    enum class CandidateRole {
        JANMA_LAGNESHA,
        VARSHA_LAGNESHA,
        MUNTHESHA,
        TRIRASHIPATI,
        DINA_RATRI_PATI,
    }

    /**
     * Samjna tantra verse 61 Trirashipati table:
     * Aries..Cancer:
     *   Day: Sun, Venus, Saturn, Venus
     *   Night: Jupiter, Moon, Mercury, Mars
     * Leo..Scorpio (inverted):
     *   Day: Jupiter, Moon, Mercury, Mars
     *   Night: Sun, Venus, Saturn, Venus
     * Sagittarius..Pisces (fixed for both day and night):
     *   Day & Night: Saturn, Mars, Jupiter, Moon
     */
    fun trirashipatiOf(sign: Rashi, isDay: Boolean): String {
        return when (sign) {
            Rashi.ARIES -> if (isDay) "SUN" else "JUPITER"
            Rashi.TAURUS -> if (isDay) "VENUS" else "MOON"
            Rashi.GEMINI -> if (isDay) "SATURN" else "MERCURY"
            Rashi.CANCER -> if (isDay) "VENUS" else "MARS"
            Rashi.LEO -> if (isDay) "JUPITER" else "SUN"
            Rashi.VIRGO -> if (isDay) "MOON" else "VENUS"
            Rashi.LIBRA -> if (isDay) "MERCURY" else "SATURN"
            Rashi.SCORPIO -> if (isDay) "MARS" else "VENUS"
            Rashi.SAGITTARIUS -> "SATURN"
            Rashi.CAPRICORN -> "MARS"
            Rashi.AQUARIUS -> "JUPITER"
            Rashi.PISCES -> "MOON"
        }
    }

    fun lordOf(sign: Rashi): String = when (sign) {
        Rashi.ARIES, Rashi.SCORPIO -> "MARS"
        Rashi.TAURUS, Rashi.LIBRA -> "VENUS"
        Rashi.GEMINI, Rashi.VIRGO -> "MERCURY"
        Rashi.CANCER -> "MOON"
        Rashi.LEO -> "SUN"
        Rashi.SAGITTARIUS, Rashi.PISCES -> "JUPITER"
        Rashi.CAPRICORN, Rashi.AQUARIUS -> "SATURN"
    }

    fun isDayReturn(annualChart: AstroChart): Boolean {
        val sunPlacement = annualChart.houses.asSequence().flatMap { house ->
            house.planets.asSequence().filter { it.planetId.equals("SUN", ignoreCase = true) }.map { house to it }
        }.firstOrNull()
        val houseNum = sunPlacement?.first?.houseNumber ?: 1
        // In whole sign / bhava, houses 7..12 are above horizon (Day)
        return houseNum in 7..12
    }

    /**
     * Checks if a planet casts a Tajika aspect to annual Lagna.
     * Houses 1 (conjunction), 3, 11 (sextile), 4, 10 (square), 5, 9 (trine), 7 (opposition) see Lagna.
     * Houses 2, 6, 8, 12 do NOT see Lagna (Adrishti / Aprakashya).
     */
    fun aspectsAnnualLagna(houseNumber: Int): Boolean {
        return houseNumber in setOf(1, 3, 4, 5, 7, 9, 10, 11)
    }

    /**
     * Calculates classical Tajika Panchavargiya Bala (5-fold dignity strength):
     * 1. Kshetra (Own/Friend/Neutral/Enemy sign) - max 30.0 virupas
     * 2. Uccha (Exaltation distance) - max 20.0 virupas
     * 3. Hadda (Term) - max 15.0 virupas
     * 4. Drekkana (Decan) - max 10.0 virupas
     * 5. Navamsha - max 5.0 virupas
     * Total score out of 80.0 virupas (20 vishwas).
     */
    fun calculatePanchavargiyaBala(planetId: String, longitude: Double): Pair<Double, Map<String, String>> {
        val normLong = ((longitude % 360.0) + 360.0) % 360.0
        val signIndex = (normLong / 30.0).toInt()
        val degInSign = normLong % 30.0
        val sign = Rashi.fromIndex(signIndex)

        // 1. Kshetra Bala
        val ownLord = lordOf(sign)
        val kshetraScore = if (ownLord.equals(planetId, ignoreCase = true)) {
            30.0
        } else {
            // Friend / Neutral / Enemy approximation
            val friends = mapOf(
                "SUN" to setOf("MOON", "MARS", "JUPITER"),
                "MOON" to setOf("SUN", "MERCURY"),
                "MARS" to setOf("SUN", "MOON", "JUPITER"),
                "MERCURY" to setOf("SUN", "VENUS"),
                "JUPITER" to setOf("SUN", "MOON", "MARS"),
                "VENUS" to setOf("MERCURY", "SATURN"),
                "SATURN" to setOf("MERCURY", "VENUS")
            )
            val enemies = mapOf(
                "SUN" to setOf("VENUS", "SATURN"),
                "MOON" to setOf<String>(),
                "MARS" to setOf("MERCURY"),
                "MERCURY" to setOf("MOON"),
                "JUPITER" to setOf("MERCURY", "VENUS"),
                "VENUS" to setOf("SUN", "MOON"),
                "SATURN" to setOf("SUN", "MOON", "MARS")
            )
            val pFriends = friends[planetId.uppercase()].orEmpty()
            val pEnemies = enemies[planetId.uppercase()].orEmpty()
            when {
                ownLord in pFriends -> 22.5
                ownLord in pEnemies -> 7.5
                else -> 15.0
            }
        }

        // 2. Uccha Bala (Deep exaltation degrees: Sun 10 Aries, Moon 3 Taurus, Mars 28 Cap, Merc 15 Vir, Jup 5 Can, Ven 27 Pis, Sat 20 Lib)
        val ucchaPoints = mapOf(
            "SUN" to 10.0,
            "MOON" to 33.0,
            "MARS" to 298.0,
            "MERCURY" to 165.0,
            "JUPITER" to 95.0,
            "VENUS" to 357.0,
            "SATURN" to 200.0
        )
        val deepExaltation = ucchaPoints[planetId.uppercase()] ?: 0.0
        val deepDebilitation = (deepExaltation + 180.0) % 360.0
        val distDebil = ((normLong - deepDebilitation + 360.0) % 360.0).let { if (it > 180.0) 360.0 - it else it }
        val ucchaScore = (distDebil / 180.0) * 20.0

        // 3. Hadda Bala (approximate hadda division based on degree 0-6, 6-12, 12-18, 18-24, 24-30)
        val haddaSegment = (degInSign / 6.0).toInt().coerceIn(0, 4)
        val haddaScore = if (kshetraScore >= 30.0 || haddaSegment in 1..2) 15.0 else 11.25

        // 4. Drekkana Bala (0-10 own sign, 10-20 5th sign, 20-30 9th sign)
        val drekkanaSign = when ((degInSign / 10.0).toInt().coerceIn(0, 2)) {
            0 -> sign
            1 -> Rashi.fromIndex((signIndex + 4) % 12)
            else -> Rashi.fromIndex((signIndex + 8) % 12)
        }
        val drekkanaLord = lordOf(drekkanaSign)
        val drekkanaScore = if (drekkanaLord.equals(planetId, ignoreCase = true)) 10.0 else 7.5

        // 5. Navamsha Bala
        val navamshaOffset = (degInSign / (30.0 / 9.0)).toInt().coerceIn(0, 8)
        val navamshaStart = when (signIndex % 4) {
            0 -> 0 // Fire: Aries
            1 -> 9 // Earth: Capricorn
            2 -> 6 // Air: Libra
            else -> 3 // Water: Cancer
        }
        val navamshaSign = Rashi.fromIndex((navamshaStart + navamshaOffset) % 12)
        val navamshaLord = lordOf(navamshaSign)
        val navamshaScore = if (navamshaLord.equals(planetId, ignoreCase = true)) 5.0 else 3.75

        val total = kshetraScore + ucchaScore + haddaScore + drekkanaScore + navamshaScore
        val breakdown = mapOf(
            "kshetraBala" to "${kshetraScore} virupas",
            "ucchaBala" to "${(ucchaScore * 10.0).toInt() / 10.0} virupas",
            "haddaBala" to "${haddaScore} virupas",
            "drekkanaBala" to "${drekkanaScore} virupas",
            "navamshaBala" to "${navamshaScore} virupas",
            "totalPanchavargiyaBala" to "${(total * 10.0).toInt() / 10.0} virupas"
        )
        return total to breakdown
    }

    fun calculate(
        natalAscendantLongitude: Double,
        annualChart: AstroChart,
        muntha: MunthaCalculation,
    ): VarsheshwaraResult {
        val isDay = isDayReturn(annualChart)
        val natalLagnaSign = Rashi.fromIndex(((natalAscendantLongitude % 360.0 + 360.0) % 360.0 / 30.0).toInt())
        val annualLagnaSign = annualChart.ascendant?.sign ?: Rashi.ARIES

        val janmaLagnesha = lordOf(natalLagnaSign)
        val varshaLagnesha = lordOf(annualLagnaSign)
        val munthesha = muntha.lord.name
        val trirashipati = trirashipatiOf(annualLagnaSign, isDay)

        // Dina/Ratri-pati: Sun sign lord by day, Moon sign lord by night in annual chart
        val sunPlacement = annualChart.houses.asSequence().flatMap { it.planets }.firstOrNull { it.planetId.equals("SUN", ignoreCase = true) }
        val moonPlacement = annualChart.houses.asSequence().flatMap { it.planets }.firstOrNull { it.planetId.equals("MOON", ignoreCase = true) }
        val dinaRatriPati = if (isDay) {
            sunPlacement?.sign?.let(::lordOf) ?: "SUN"
        } else {
            moonPlacement?.sign?.let(::lordOf) ?: "MOON"
        }

        val rawCandidates = listOf(
            CandidateRole.JANMA_LAGNESHA to janmaLagnesha,
            CandidateRole.VARSHA_LAGNESHA to varshaLagnesha,
            CandidateRole.MUNTHESHA to munthesha,
            CandidateRole.TRIRASHIPATI to trirashipati,
            CandidateRole.DINA_RATRI_PATI to dinaRatriPati,
        )

        // Find planet placements in annual chart to check Lagna aspect & Panchavargiya strength
        val candidateResults = rawCandidates.map { (role, planet) ->
            val placement = annualChart.houses.asSequence().flatMap { house ->
                house.planets.asSequence().filter { it.planetId.equals(planet, ignoreCase = true) }.map { house to it }
            }.firstOrNull()

            val houseNum = placement?.first?.houseNumber ?: 0
            val longitude = placement?.second?.longitude ?: 0.0
            val eligible = if (placement != null) aspectsAnnualLagna(houseNum) else false
            val (strength, breakdown) = if (placement != null) {
                calculatePanchavargiyaBala(planet, longitude)
            } else {
                0.0 to mapOf("totalPanchavargiyaBala" to "0.0 virupas")
            }

            val roleDesc = when (role) {
                CandidateRole.JANMA_LAGNESHA -> "Janma Lagnesha (Natal Lagna Lord)"
                CandidateRole.VARSHA_LAGNESHA -> "Varsha Lagnesha (Annual Lagna Lord)"
                CandidateRole.MUNTHESHA -> "Munthesha (Muntha Lord)"
                CandidateRole.TRIRASHIPATI -> "Trirashipati (${if (isDay) "Day" else "Night"} Lord of Annual Lagna)"
                CandidateRole.DINA_RATRI_PATI -> if (isDay) "Dina-pati (Sun Sign Lord)" else "Ratri-pati (Moon Sign Lord)"
            }

            VarsheshwaraCandidateResult(
                planet = planet,
                eligible = eligible,
                strengthBreakdown = breakdown + mapOf(
                    "role" to roleDesc,
                    "annualHouse" to houseNum.toString(),
                    "aspectsAnnualLagna" to eligible.toString(),
                    "numericalStrength" to strength.toString(),
                ),
                ruleRefs = listOf(SOURCE_REF, TRIRASHI_REF),
            )
        }

        // Selection algorithm per Varsha Tantra vv. 6-7:
        // 1. Filter candidates where eligible == true
        val eligibleCandidates = candidateResults.filter { it.eligible == true }
        val pool = if (eligibleCandidates.isNotEmpty()) eligibleCandidates else candidateResults

        // Sort by numerical strength descending
        val sorted = pool.sortedByDescending {
            it.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0
        }

        val topScore = sorted.firstOrNull()?.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0
        val topCandidates = sorted.filter {
            abs((it.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0) - topScore) < 0.001
        }

        val (selected, tieBreakMsg) = when {
            topCandidates.size == 1 -> topCandidates.first().planet to null
            topCandidates.any { it.planet.equals(munthesha, ignoreCase = true) } -> {
                munthesha to "Tie broken in favor of Munthesha per Varsha tantra verse 7 ('दृगादिसाम्येऽप्यथ निर्बलत्वे वर्षाधिपः स्यान्मुंथहेश्वरस्तु')."
            }
            topCandidates.any { it.planet.equals(dinaRatriPati, ignoreCase = true) } -> {
                dinaRatriPati to "Tie broken in favor of Dina/Ratri-pati per Varsha tantra verse 8."
            }
            else -> {
                topCandidates.first().planet to "Tie resolved by candidate precedence order."
            }
        }

        return VarsheshwaraResult(
            candidates = candidateResults,
            selectedPlanet = selected,
            tieBreak = tieBreakMsg,
            ruleRefs = listOf(SOURCE_REF, TRIRASHI_REF),
            provenance = listOf(
                "Five candidates evaluated: Janma Lagnesha, Varsha Lagnesha, Munthesha, Trirashipati, and Dina/Ratri-pati.",
                "Trirashipati derived per Samjna tantra v.61 based on ${if (isDay) "Day" else "Night"} ingress.",
                if (eligibleCandidates.isNotEmpty()) "Selected highest Panchavargiya strength candidate aspecting the annual Lagna."
                else "No candidate aspected the annual Lagna; selected highest strength candidate overall per verse 7.",
            ),
            status = VarshaphalComponentStatus.CALCULATED,
        )
    }

    fun calculateFromPlacements(
        natalAscendantLongitude: Double,
        annualAscendantLongitude: Double,
        elapsedSolarReturnCycles: Int,
        planetLongitudes: Map<String, Double>,
        isDay: Boolean = true,
    ): VarsheshwaraResult {
        val natalLagnaSign = Rashi.fromIndex(((natalAscendantLongitude % 360.0 + 360.0) % 360.0 / 30.0).toInt())
        val annualLagnaSign = Rashi.fromIndex(((annualAscendantLongitude % 360.0 + 360.0) % 360.0 / 30.0).toInt())
        val muntha = MunthaEngine.calculate(natalAscendantLongitude, elapsedSolarReturnCycles, annualLagnaSign)

        val janmaLagnesha = lordOf(natalLagnaSign)
        val varshaLagnesha = lordOf(annualLagnaSign)
        val munthesha = muntha.lord.name
        val trirashipati = trirashipatiOf(annualLagnaSign, isDay)

        val sunLong = planetLongitudes.entries.firstOrNull { it.key.equals("SUN", ignoreCase = true) }?.value ?: 0.0
        val moonLong = planetLongitudes.entries.firstOrNull { it.key.equals("MOON", ignoreCase = true) }?.value ?: 0.0
        val dinaRatriPati = if (isDay) lordOf(Rashi.fromIndex((((sunLong % 360.0 + 360.0) % 360.0) / 30.0).toInt()))
                           else lordOf(Rashi.fromIndex((((moonLong % 360.0 + 360.0) % 360.0) / 30.0).toInt()))

        val rawCandidates = listOf(
            CandidateRole.JANMA_LAGNESHA to janmaLagnesha,
            CandidateRole.VARSHA_LAGNESHA to varshaLagnesha,
            CandidateRole.MUNTHESHA to munthesha,
            CandidateRole.TRIRASHIPATI to trirashipati,
            CandidateRole.DINA_RATRI_PATI to dinaRatriPati,
        )

        val candidateResults = rawCandidates.map { (role, planet) ->
            val pLong = planetLongitudes.entries.firstOrNull { it.key.equals(planet, ignoreCase = true) }?.value
            val pSign = pLong?.let { (((it % 360.0 + 360.0) % 360.0) / 30.0).toInt() } ?: 0
            val houseNum = ((pSign - annualLagnaSign.index + 12) % 12) + 1
            val eligible = if (pLong != null) aspectsAnnualLagna(houseNum) else false
            val (strength, breakdown) = if (pLong != null) {
                calculatePanchavargiyaBala(planet, pLong)
            } else {
                0.0 to mapOf("totalPanchavargiyaBala" to "0.0 virupas")
            }

            val roleDesc = when (role) {
                CandidateRole.JANMA_LAGNESHA -> "Janma Lagnesha (Natal Lagna Lord)"
                CandidateRole.VARSHA_LAGNESHA -> "Varsha Lagnesha (Annual Lagna Lord)"
                CandidateRole.MUNTHESHA -> "Munthesha (Muntha Lord)"
                CandidateRole.TRIRASHIPATI -> "Trirashipati (${if (isDay) "Day" else "Night"} Lord of Annual Lagna)"
                CandidateRole.DINA_RATRI_PATI -> if (isDay) "Dina-pati (Sun Sign Lord)" else "Ratri-pati (Moon Sign Lord)"
            }

            VarsheshwaraCandidateResult(
                planet = planet,
                eligible = eligible,
                strengthBreakdown = breakdown + mapOf(
                    "role" to roleDesc,
                    "annualHouse" to houseNum.toString(),
                    "aspectsAnnualLagna" to eligible.toString(),
                    "numericalStrength" to strength.toString(),
                ),
                ruleRefs = listOf(SOURCE_REF, TRIRASHI_REF),
            )
        }

        val eligibleCandidates = candidateResults.filter { it.eligible == true }
        val pool = if (eligibleCandidates.isNotEmpty()) eligibleCandidates else candidateResults

        val sorted = pool.sortedByDescending {
            it.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0
        }

        val topScore = sorted.firstOrNull()?.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0
        val topCandidates = sorted.filter {
            abs((it.strengthBreakdown["numericalStrength"]?.toDoubleOrNull() ?: 0.0) - topScore) < 0.001
        }

        val (selected, tieBreakMsg) = when {
            topCandidates.size == 1 -> topCandidates.first().planet to null
            topCandidates.any { it.planet.equals(munthesha, ignoreCase = true) } -> {
                munthesha to "Tie broken in favor of Munthesha per Varsha tantra verse 7 ('दृगादिसाम्येऽप्यथ निर्बलत्वे वर्षाधिपः स्यान्मुंथहेश्वरस्तु')."
            }
            topCandidates.any { it.planet.equals(dinaRatriPati, ignoreCase = true) } -> {
                dinaRatriPati to "Tie broken in favor of Dina/Ratri-pati per Varsha tantra verse 8."
            }
            else -> {
                topCandidates.first().planet to "Tie resolved by candidate precedence order."
            }
        }

        return VarsheshwaraResult(
            candidates = candidateResults,
            selectedPlanet = selected,
            tieBreak = tieBreakMsg,
            ruleRefs = listOf(SOURCE_REF, TRIRASHI_REF),
            provenance = listOf(
                "Five candidates evaluated: Janma Lagnesha, Varsha Lagnesha, Munthesha, Trirashipati, and Dina/Ratri-pati.",
                "Trirashipati derived per Samjna tantra v.61 based on ${if (isDay) "Day" else "Night"} ingress.",
                if (eligibleCandidates.isNotEmpty()) "Selected highest Panchavargiya strength candidate aspecting the annual Lagna."
                else "No candidate aspected the annual Lagna; selected highest strength candidate overall per verse 7.",
            ),
            status = VarshaphalComponentStatus.CALCULATED,
        )
    }
}

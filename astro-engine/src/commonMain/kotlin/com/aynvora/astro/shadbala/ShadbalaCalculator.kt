package com.aynvora.astro.shadbala

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.dignity.DignityType
import com.aynvora.astro.dignity.PlanetaryDignityCalculator
import com.aynvora.astro.math.AstroMath
import com.aynvora.astro.varga.DefaultVargaEngine
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import com.aynvora.astro.varga.VargaEngine
import com.aynvora.astro.varga.VargaProfile
import kotlin.math.abs
import kotlin.math.floor

/**
 * Deterministic facts-only calculator for classical Shadbala strength calculations
 * according to Brihat Parashara Hora Shastra, Chapters 27-28 (PARASHARA_CLASSICAL_V1).
 */
object ShadbalaCalculator {

    val CLASSICAL_PLANETS: List<BodyId> = listOf(
        BodyId.SUN,
        BodyId.MOON,
        BodyId.MARS,
        BodyId.MERCURY,
        BodyId.JUPITER,
        BodyId.VENUS,
        BodyId.SATURN,
    )

    /**
     * Saptavargas required for Saptavargaja Bala (ASTRO-R34B).
     */
    val SAPTAVARGA_CHARTS: Set<DivisionalChart> = setOf(
        DivisionalChart.D1,
        DivisionalChart.D2,
        DivisionalChart.D3,
        DivisionalChart.D7,
        DivisionalChart.D9,
        DivisionalChart.D12,
        DivisionalChart.D30,
    )

    /**
     * Parama Neecha (deep debilitation) longitudes for the 7 classical planets (ASTRO-R26).
     */
    val DEEP_DEBILITATION_LONGITUDES: Map<BodyId, Double> = mapOf(
        BodyId.SUN to 190.0, // Libra 10°
        BodyId.MOON to 213.0, // Scorpio 3°
        BodyId.MARS to 118.0, // Cancer 28°
        BodyId.MERCURY to 345.0, // Pisces 15°
        BodyId.JUPITER to 275.0, // Capricorn 5°
        BodyId.VENUS to 177.0, // Virgo 27°
        BodyId.SATURN to 20.0, // Aries 20°
    )

    /**
     * Calculates Naisargika Bala (Natural Strength) according to BPHS Ch. 28, Slokas 13-14 (ASTRO-R32).
     * Proportional to intrinsic luminosity:
     * Sun=60, Moon=51.4286, Venus=42.8571, Jupiter=34.2857, Mercury=25.7143, Mars=17.1429, Saturn=8.5714.
     */
    fun calculateNaisargikaBala(bodyId: BodyId): NaisargikaBalaPosition {
        return when (bodyId) {
            BodyId.SUN -> NaisargikaBalaPosition(
                virupas = 60.0,
                rupas = 1.0,
                rank = 1,
            )
            BodyId.MOON -> NaisargikaBalaPosition(
                virupas = 60.0 * 6.0 / 7.0,
                rupas = (60.0 * 6.0 / 7.0) / 60.0,
                rank = 2,
            )
            BodyId.VENUS -> NaisargikaBalaPosition(
                virupas = 60.0 * 5.0 / 7.0,
                rupas = (60.0 * 5.0 / 7.0) / 60.0,
                rank = 3,
            )
            BodyId.JUPITER -> NaisargikaBalaPosition(
                virupas = 60.0 * 4.0 / 7.0,
                rupas = (60.0 * 4.0 / 7.0) / 60.0,
                rank = 4,
            )
            BodyId.MERCURY -> NaisargikaBalaPosition(
                virupas = 60.0 * 3.0 / 7.0,
                rupas = (60.0 * 3.0 / 7.0) / 60.0,
                rank = 5,
            )
            BodyId.MARS -> NaisargikaBalaPosition(
                virupas = 60.0 * 2.0 / 7.0,
                rupas = (60.0 * 2.0 / 7.0) / 60.0,
                rank = 6,
            )
            BodyId.SATURN -> NaisargikaBalaPosition(
                virupas = 60.0 * 1.0 / 7.0,
                rupas = (60.0 * 1.0 / 7.0) / 60.0,
                rank = 7,
            )
            BodyId.RAHU, BodyId.KETU -> NaisargikaBalaPosition(
                virupas = 0.0,
                rupas = 0.0,
                rank = 0,
                isEvaluated = false,
            )
        }
    }

    /**
     * Calculates Dig Bala (Directional Strength) according to BPHS Ch. 28, Slokas 7-8 (ASTRO-R33).
     * - Jupiter & Mercury: 1st house cusp (Lagna / East)
     * - Sun & Mars: 10th house cusp (Midheaven / South)
     * - Saturn: 7th house cusp (Descendant / West)
     * - Moon & Venus: 4th house cusp (Nadir / North)
     */
    fun calculateDigBala(
        bodyId: BodyId,
        planetLongitude: Double,
        houseCusps: Map<Int, Double>,
    ): DigBalaPosition {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
            return DigBalaPosition(
                powerfulPointDegrees = 0.0,
                zeroPointDegrees = 0.0,
                arcDegrees = 0.0,
                virupas = 0.0,
                rupas = 0.0,
                isEvaluated = false,
            )
        }

        val lagnaCusp = houseCusps[1] ?: 0.0
        val fourthCusp = houseCusps[4] ?: ((lagnaCusp + 90.0) % 360.0)
        val seventhCusp = houseCusps[7] ?: ((lagnaCusp + 180.0) % 360.0)
        val tenthCusp = houseCusps[10] ?: ((lagnaCusp + 270.0) % 360.0)

        val powerfulPoint = when (bodyId) {
            BodyId.MERCURY, BodyId.JUPITER -> lagnaCusp
            BodyId.SUN, BodyId.MARS -> tenthCusp
            BodyId.SATURN -> seventhCusp
            BodyId.MOON, BodyId.VENUS -> fourthCusp
            BodyId.RAHU, BodyId.KETU -> 0.0
        }

        val zeroPoint = (powerfulPoint + 180.0) % 360.0
        var arc = abs(planetLongitude - zeroPoint) % 360.0
        if (arc > 180.0) {
            arc = 360.0 - arc
        }

        val virupas = arc / 3.0
        val rupas = virupas / 60.0

        return DigBalaPosition(
            powerfulPointDegrees = powerfulPoint,
            zeroPointDegrees = zeroPoint,
            arcDegrees = arc,
            virupas = virupas,
            rupas = rupas,
            isEvaluated = true,
        )
    }

    /**
     * Calculates Uchcha Bala (Exaltation Strength) according to BPHS Ch. 28, Slokas 2-3 (ASTRO-R34A).
     */
    fun calculateUchchaBala(bodyId: BodyId, planetLongitude: Double): Double {
        val neechaPoint = DEEP_DEBILITATION_LONGITUDES[bodyId] ?: return 0.0
        var diff = abs(planetLongitude - neechaPoint) % 360.0
        if (diff > 180.0) {
            diff = 360.0 - diff
        }
        return diff / 3.0
    }

    /**
     * Calculates Saptavargaja Bala according to BPHS Ch. 28, Slokas 4-6 (ASTRO-R34B).
     */
    fun calculateSaptavargajaBala(
        bodyId: BodyId,
        vargas: Map<DivisionalChart, VargaChartResult>,
    ): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0

        var totalVirupas = 0.0

        for (chart in SAPTAVARGA_CHARTS) {
            val vargaResult = vargas[chart] ?: continue
            val planetPos = vargaResult.positions.firstOrNull { it.bodyId == bodyId && !it.isLagna } ?: continue

            val signMap = vargaResult.positions
                .filter { it.bodyId != null && !it.isLagna }
                .associate { it.bodyId!! to it.resultingRashiIndex }

            val dignity = PlanetaryDignityCalculator.evaluateDignity(
                bodyId = bodyId,
                rashiIndex = planetPos.resultingRashiIndex,
                degreeInSign = planetPos.degreeInResultingRashi,
                signMap = signMap,
                chart = chart,
            )

            val virupas = when (dignity.dignityType) {
                DignityType.MOOLATRIKONA, DignityType.EXALTATION -> 45.0
                DignityType.OWN_SIGN -> 30.0
                DignityType.GREAT_FRIEND_SIGN -> 20.0
                DignityType.FRIEND_SIGN -> 15.0
                DignityType.NEUTRAL_SIGN -> 10.0
                DignityType.ENEMY_SIGN -> 4.0
                DignityType.GREAT_ENEMY_SIGN -> 2.0
                DignityType.DEBILITATION -> 0.0
                DignityType.NOT_APPLICABLE -> 0.0
            }
            totalVirupas += virupas
        }

        return totalVirupas
    }

    /**
     * Calculates Ojhayugmarasyamsa Bala according to BPHS Ch. 28, Sloka 9 (ASTRO-R34C).
     * Female planets (Moon, Venus) in even signs/navamsas: 15 Virupas each.
     * Male & neutral planets (Sun, Mars, Jupiter, Mercury, Saturn) in odd signs/navamsas: 15 Virupas each.
     */
    fun calculateOjhayugmarasyamsaBala(
        bodyId: BodyId,
        d1RashiIndex: Int,
        d9RashiIndex: Int,
    ): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0

        val isD1Odd = (d1RashiIndex % 2 == 0)
        val isD1Even = !isD1Odd
        val isD9Odd = (d9RashiIndex % 2 == 0)
        val isD9Even = !isD9Odd

        return when (bodyId) {
            BodyId.MOON, BodyId.VENUS -> {
                val d1Score = if (isD1Even) 15.0 else 0.0
                val d9Score = if (isD9Even) 15.0 else 0.0
                d1Score + d9Score
            }
            BodyId.SUN, BodyId.MARS, BodyId.JUPITER, BodyId.MERCURY, BodyId.SATURN -> {
                val d1Score = if (isD1Odd) 15.0 else 0.0
                val d9Score = if (isD9Odd) 15.0 else 0.0
                d1Score + d9Score
            }
            BodyId.RAHU, BodyId.KETU -> 0.0
        }
    }

    /**
     * Calculates Kendra Bala according to BPHS Ch. 28, Sloka 10 (ASTRO-R34D).
     * Kendra (1, 4, 7, 10): 60 Virupas.
     * Panaphara (2, 5, 8, 11): 30 Virupas.
     * Apoklima (3, 6, 9, 12): 15 Virupas.
     */
    fun calculateKendraBala(bodyId: BodyId, houseNumber: Int): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        return when (houseNumber) {
            1, 4, 7, 10 -> 60.0
            2, 5, 8, 11 -> 30.0
            3, 6, 9, 12 -> 15.0
            else -> 0.0
        }
    }

    /**
     * Calculates Drekkana Bala according to BPHS Ch. 28, Sloka 11 (ASTRO-R34E).
     * 1st Drekkana [0°, 10°): Male planets (Sun, Mars, Jupiter) get 15.0 Virupas.
     * 2nd Drekkana [10°, 20°): Neutral planets (Mercury, Saturn) get 15.0 Virupas.
     * 3rd Drekkana [20°, 30°): Female planets (Moon, Venus) get 15.0 Virupas.
     */
    fun calculateDrekkanaBala(bodyId: BodyId, degreeInSign: Double): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        return when {
            degreeInSign in 0.0..<10.0 -> when (bodyId) {
                BodyId.SUN, BodyId.MARS, BodyId.JUPITER -> 15.0
                else -> 0.0
            }
            degreeInSign in 10.0..<20.0 -> when (bodyId) {
                BodyId.MERCURY, BodyId.SATURN -> 15.0
                else -> 0.0
            }
            degreeInSign in 20.0..30.0 -> when (bodyId) {
                BodyId.MOON, BodyId.VENUS -> 15.0
                else -> 0.0
            }
            else -> 0.0
        }
    }

    /**
     * Calculates full Sthana Bala (Positional Strength) summing the 5 classical subcomponents.
     */
    fun calculateSthanaBala(
        bodyId: BodyId,
        planetLongitude: Double,
        degreeInSign: Double,
        d1RashiIndex: Int,
        d9RashiIndex: Int,
        houseNumber: Int,
        vargas: Map<DivisionalChart, VargaChartResult>,
    ): SthanaBalaPosition {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
            return SthanaBalaPosition(
                uchchaBalaVirupas = 0.0,
                saptavargajaBalaVirupas = 0.0,
                ojhayugmarasyamsaBalaVirupas = 0.0,
                kendraBalaVirupas = 0.0,
                drekkanaBalaVirupas = 0.0,
                totalVirupas = 0.0,
                totalRupas = 0.0,
                isEvaluated = false,
            )
        }

        val uchcha = calculateUchchaBala(bodyId, planetLongitude)
        val saptavarga = calculateSaptavargajaBala(bodyId, vargas)
        val ojha = calculateOjhayugmarasyamsaBala(bodyId, d1RashiIndex, d9RashiIndex)
        val kendra = calculateKendraBala(bodyId, houseNumber)
        val drekkana = calculateDrekkanaBala(bodyId, degreeInSign)

        val totalVirupas = uchcha + saptavarga + ojha + kendra + drekkana
        val totalRupas = totalVirupas / 60.0

        return SthanaBalaPosition(
            uchchaBalaVirupas = uchcha,
            saptavargajaBalaVirupas = saptavarga,
            ojhayugmarasyamsaBalaVirupas = ojha,
            kendraBalaVirupas = kendra,
            drekkanaBalaVirupas = drekkana,
            totalVirupas = totalVirupas,
            totalRupas = totalRupas,
            isEvaluated = true,
        )
    }

    /**
     * Calculates Paksha Bala under Kala Bala (ASTRO-R36).
     */
    fun calculatePakshaBala(
        bodyId: BodyId,
        sunLongitude: Double,
        moonLongitude: Double,
    ): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0

        val diff = (moonLongitude - sunLongitude + 360.0) % 360.0
        val beneficBala = if (diff <= 180.0) diff / 3.0 else (360.0 - diff) / 3.0
        val maleficBala = 60.0 - beneficBala

        return when (bodyId) {
            BodyId.JUPITER, BodyId.VENUS -> beneficBala
            BodyId.SUN, BodyId.MARS, BodyId.SATURN -> maleficBala
            BodyId.MOON -> (beneficBala * 2.0).coerceAtMost(60.0)
            BodyId.MERCURY -> beneficBala
            BodyId.RAHU, BodyId.KETU -> 0.0
        }
    }

    /**
     * Calculates Nathonnatha Bala (Diurnal / Nocturnal Strength) according to BPHS Ch. 28, Slokas 14-15 (ASTRO-R36).
     */
    fun calculateNathonnathaBala(
        bodyId: BodyId,
        sunLongitude: Double,
        fourthHouseCusp: Double,
    ): Double {
        var diff = abs(sunLongitude - fourthHouseCusp) % 360.0
        if (diff > 180.0) diff = 360.0 - diff
        val arcFromMidnight = diff

        return when (bodyId) {
            BodyId.MERCURY -> 60.0
            BodyId.SUN, BodyId.JUPITER, BodyId.VENUS -> arcFromMidnight / 3.0
            BodyId.MOON, BodyId.MARS, BodyId.SATURN -> (180.0 - arcFromMidnight) / 3.0
            BodyId.RAHU, BodyId.KETU -> 0.0
        }
    }

    /**
     * Calculates Tribhaga Bala (Three Parts of Day & Night) according to BPHS Ch. 28, Sloka 17 (ASTRO-R36).
     */
    fun calculateTribhagaBala(bodyId: BodyId, sunHouseNumber: Int): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        if (bodyId == BodyId.JUPITER) return 60.0

        val isDay = sunHouseNumber in 7..12
        val portionLord = if (isDay) {
            when (sunHouseNumber) {
                11, 12 -> BodyId.MERCURY
                9, 10 -> BodyId.SUN
                7, 8 -> BodyId.SATURN
                else -> BodyId.SUN
            }
        } else {
            when (sunHouseNumber) {
                5, 6 -> BodyId.MOON
                3, 4 -> BodyId.VENUS
                1, 2 -> BodyId.MARS
                else -> BodyId.MOON
            }
        }
        return if (bodyId == portionLord) 60.0 else 0.0
    }

    /**
     * Calculates Vara Bala (Lord of the Day) according to BPHS Ch. 28, Sloka 17 (ASTRO-R36).
     */
    fun calculateVaraBala(bodyId: BodyId, julianDay: Double): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        val dayIndex = ((floor(julianDay + 1.5).toLong() % 7L) + 7L) % 7L
        val dayLord = when (dayIndex) {
            0L -> BodyId.SUN
            1L -> BodyId.MOON
            2L -> BodyId.MARS
            3L -> BodyId.MERCURY
            4L -> BodyId.JUPITER
            5L -> BodyId.VENUS
            6L -> BodyId.SATURN
            else -> BodyId.SUN
        }
        return if (bodyId == dayLord) 45.0 else 0.0
    }

    /**
     * Calculates Hora Bala (Lord of the Hour) according to BPHS Ch. 28, Sloka 17 (ASTRO-R36).
     */
    fun calculateHoraBala(bodyId: BodyId, julianDay: Double, birthHour: Int): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        val dayIndex = ((floor(julianDay + 1.5).toLong() % 7L) + 7L) % 7L
        val dayLordChaldean = when (dayIndex) {
            0L -> 3 // Sun
            1L -> 6 // Moon
            2L -> 2 // Mars
            3L -> 5 // Mercury
            4L -> 1 // Jupiter
            5L -> 4 // Venus
            6L -> 0 // Saturn
            else -> 3
        }
        val horaIndex = (dayLordChaldean + (birthHour % 24)) % 7
        val horaLord = when (horaIndex) {
            0 -> BodyId.SATURN
            1 -> BodyId.JUPITER
            2 -> BodyId.MARS
            3 -> BodyId.SUN
            4 -> BodyId.VENUS
            5 -> BodyId.MERCURY
            6 -> BodyId.MOON
            else -> BodyId.SUN
        }
        return if (bodyId == horaLord) 60.0 else 0.0
    }

    /**
     * Calculates Masa Bala (Lord of the Month) according to BPHS Ch. 28, Sloka 17 (ASTRO-R36).
     */
    fun calculateMasaBala(bodyId: BodyId, sunRashiIndex: Int): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        val monthLord = when (sunRashiIndex) {
            0, 7 -> BodyId.MARS
            1, 6 -> BodyId.VENUS
            2, 5 -> BodyId.MERCURY
            3 -> BodyId.MOON
            4 -> BodyId.SUN
            8, 11 -> BodyId.JUPITER
            9, 10 -> BodyId.SATURN
            else -> BodyId.SUN
        }
        return if (bodyId == monthLord) 30.0 else 0.0
    }

    /**
     * Calculates Varsha Bala (Lord of the Year) according to BPHS Ch. 28, Sloka 17 (ASTRO-R36).
     */
    fun calculateVarshaBala(bodyId: BodyId, julianDay: Double): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        val yearFraction = (julianDay - 2451545.0) / 365.25
        val yearNumber = floor(yearFraction).toInt()
        val yearLordIndex = ((yearNumber * 3) % 7 + 7) % 7
        val yearLord = when (yearLordIndex) {
            0 -> BodyId.SUN
            1 -> BodyId.MOON
            2 -> BodyId.MARS
            3 -> BodyId.MERCURY
            4 -> BodyId.JUPITER
            5 -> BodyId.VENUS
            6 -> BodyId.SATURN
            else -> BodyId.SUN
        }
        return if (bodyId == yearLord) 15.0 else 0.0
    }

    /**
     * Calculates equatorial declination from tropical longitude and obliquity of ecliptic.
     */
    fun calculateDeclination(tropicalLongitudeDeg: Double, obliquityDeg: Double): Double {
        val sinDelta = AstroMath.sinDeg(obliquityDeg) * AstroMath.sinDeg(tropicalLongitudeDeg)
        return AstroMath.asinDeg(sinDelta)
    }

    /**
     * Calculates Ayana Bala (Equinoctial / Declination Strength) according to BPHS Ch. 28, Slokas 18-19 (ASTRO-R36).
     */
    fun calculateAyanaBala(
        bodyId: BodyId,
        tropicalLongitude: Double,
        obliquityDeg: Double,
    ): Double {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) return 0.0
        val delta = calculateDeclination(tropicalLongitude, obliquityDeg)
        val virupas = when (bodyId) {
            BodyId.SUN, BodyId.MARS, BodyId.JUPITER, BodyId.VENUS -> (24.0 + delta) * 1.25
            BodyId.MOON, BodyId.SATURN -> (24.0 - delta) * 1.25
            BodyId.MERCURY -> (24.0 + delta) * 1.25
            BodyId.RAHU, BodyId.KETU -> 0.0
        }
        return virupas.coerceIn(0.0, 60.0)
    }

    /**
     * Calculates Yuddha Bala (Planetary War) according to BPHS Ch. 28, Sloka 20 (ASTRO-R36).
     */
    fun calculateYuddhaBala(bodyId: BodyId, positions: List<BodyPosition>): Double {
        if (bodyId !in listOf(BodyId.MARS, BodyId.MERCURY, BodyId.JUPITER, BodyId.VENUS, BodyId.SATURN)) return 0.0
        val myPos = positions.firstOrNull { it.bodyId == bodyId } ?: return 0.0
        for (other in positions) {
            if (other.bodyId != bodyId && other.bodyId in listOf(BodyId.MARS, BodyId.MERCURY, BodyId.JUPITER, BodyId.VENUS, BodyId.SATURN)) {
                var diff = abs(myPos.siderealLongitude - other.siderealLongitude) % 360.0
                if (diff > 180.0) diff = 360.0 - diff
                if (diff < 1.0) {
                    val isVictor = myPos.siderealLongitude >= other.siderealLongitude
                    val deltaBala = (1.0 - diff) * 10.0
                    return if (isVictor) deltaBala else -deltaBala
                }
            }
        }
        return 0.0
    }

    /**
     * Evaluates comprehensive Kala Bala (Temporal Strength) summing all 8 classical subcomponents.
     */
    fun calculateKalaBala(
        bodyId: BodyId,
        sunLongitude: Double,
        moonLongitude: Double,
        tropicalLongitude: Double,
        obliquityDeg: Double,
        fourthHouseCusp: Double,
        sunHouseNumber: Int,
        sunRashiIndex: Int,
        julianDay: Double,
        birthHour: Int,
        positions: List<BodyPosition>,
    ): KalaBalaPosition {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
            return KalaBalaPosition(
                isEvaluated = false,
                deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES"),
            )
        }

        val nathonnatha = calculateNathonnathaBala(bodyId, sunLongitude, fourthHouseCusp)
        val paksha = calculatePakshaBala(bodyId, sunLongitude, moonLongitude)
        val tribhaga = calculateTribhagaBala(bodyId, sunHouseNumber)
        val vara = calculateVaraBala(bodyId, julianDay)
        val hora = calculateHoraBala(bodyId, julianDay, birthHour)
        val masa = calculateMasaBala(bodyId, sunRashiIndex)
        val varsha = calculateVarshaBala(bodyId, julianDay)
        val ayana = calculateAyanaBala(bodyId, tropicalLongitude, obliquityDeg)
        val yuddha = calculateYuddhaBala(bodyId, positions)

        val totalVirupas = nathonnatha + paksha + tribhaga + vara + hora + masa + varsha + ayana + yuddha
        val totalRupas = totalVirupas / 60.0

        return KalaBalaPosition(
            nathonnathaBalaVirupas = nathonnatha,
            pakshaBalaVirupas = paksha,
            tribhagaBalaVirupas = tribhaga,
            varaBalaVirupas = vara,
            horaBalaVirupas = hora,
            masaBalaVirupas = masa,
            varshaBalaVirupas = varsha,
            ayanaBalaVirupas = ayana,
            yuddhaBalaVirupas = yuddha,
            totalVirupas = totalVirupas,
            totalRupas = totalRupas,
            isEvaluated = true,
            deferredSubcomponents = emptyList(),
        )
    }

    /**
     * Calculates Chesta Bala (Motional Strength) according to BPHS Ch. 28, Slokas 19-21 (ASTRO-R35).
     */
    fun calculateChestaBala(
        bodyId: BodyId,
        pos: BodyPosition,
        sunPos: BodyPosition?,
        ayanaBala: Double,
        pakshaBala: Double,
    ): ChestaBalaPosition {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
            return ChestaBalaPosition(
                isRetrograde = false,
                dailyMotionDegrees = 0.0,
                virupas = 0.0,
                rupas = 0.0,
                isEvaluated = false,
                deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES"),
            )
        }

        // BPHS Ch. 28, Sloka 21: Sun = Ayana Bala, Moon = Paksha Bala
        if (bodyId == BodyId.SUN) {
            return ChestaBalaPosition(
                isRetrograde = false,
                dailyMotionDegrees = pos.dailyMotionDegrees,
                chestaKendraDegrees = 0.0,
                virupas = ayanaBala,
                rupas = ayanaBala / 60.0,
                motionCategory = "DIRECT",
                isEvaluated = true,
            )
        }

        if (bodyId == BodyId.MOON) {
            return ChestaBalaPosition(
                isRetrograde = false,
                dailyMotionDegrees = pos.dailyMotionDegrees,
                chestaKendraDegrees = 0.0,
                virupas = pakshaBala,
                rupas = pakshaBala / 60.0,
                motionCategory = "DIRECT",
                isEvaluated = true,
            )
        }

        val sunLong = sunPos?.siderealLongitude ?: 0.0
        var diff = abs(pos.siderealLongitude - sunLong) % 360.0
        if (diff > 180.0) diff = 360.0 - diff
        val chestaKendra = diff

        val isRetro = pos.isRetrograde
        val virupas = if (isRetro) {
            60.0 // BPHS Sloka 19: Vakra motion gets 60 Virupas
        } else {
            chestaKendra / 3.0
        }.coerceIn(0.0, 60.0)

        val motionCat = when {
            isRetro -> "VAKRA"
            abs(pos.dailyMotionDegrees) < 0.05 -> "VIKALA"
            pos.dailyMotionDegrees > 1.0 -> "CHARA"
            pos.dailyMotionDegrees < 0.5 -> "MANDA"
            else -> "SAMA"
        }

        return ChestaBalaPosition(
            isRetrograde = isRetro,
            dailyMotionDegrees = pos.dailyMotionDegrees,
            chestaKendraDegrees = chestaKendra,
            virupas = virupas,
            rupas = virupas / 60.0,
            motionCategory = motionCat,
            isEvaluated = true,
            deferredSubcomponents = emptyList(),
        )
    }

    /**
     * Calculates Drik Bala (Aspectual Strength) according to BPHS Ch. 28, Slokas 22-24 (ASTRO-R37).
     */
    fun calculateDrikBala(
        targetBodyId: BodyId,
        positions: List<BodyPosition>,
        sunPos: BodyPosition?,
    ): DrikBalaPosition {
        if (targetBodyId == BodyId.RAHU || targetBodyId == BodyId.KETU) {
            return DrikBalaPosition(
                virupas = 0.0,
                rupas = 0.0,
                isEvaluated = false,
                deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES"),
            )
        }

        val targetPos = positions.firstOrNull { it.bodyId == targetBodyId } ?: return DrikBalaPosition(isEvaluated = false)
        val sunLong = sunPos?.siderealLongitude ?: 0.0

        var beneficAspectSum = 0.0
        var maleficAspectSum = 0.0

        for (asp in positions) {
            val aspBody = asp.bodyId
            if (aspBody == targetBodyId || aspBody == BodyId.RAHU || aspBody == BodyId.KETU) continue

            val theta = (targetPos.siderealLongitude - asp.siderealLongitude + 360.0) % 360.0

            // Base aspect curve
            var drishti = when {
                theta < 30.0 -> 0.0
                theta < 60.0 -> (theta - 30.0) / 2.0
                theta < 90.0 -> 15.0 + (theta - 60.0)
                theta < 120.0 -> 45.0 - (theta - 90.0) / 2.0
                theta < 150.0 -> 30.0 - (theta - 120.0)
                theta < 180.0 -> (theta - 150.0) * 2.0
                theta < 300.0 -> (300.0 - theta) / 2.0
                else -> 0.0
            }

            // Special classical aspects
            when (aspBody) {
                BodyId.MARS -> {
                    if (theta in 75.0..105.0) drishti = 60.0 // 4th house full aspect
                    if (theta in 195.0..225.0) drishti = 60.0 // 8th house full aspect
                }
                BodyId.JUPITER -> {
                    if (theta in 105.0..135.0) drishti = 60.0 // 5th house full aspect
                    if (theta in 225.0..255.0) drishti = 60.0 // 9th house full aspect
                }
                BodyId.SATURN -> {
                    if (theta in 45.0..75.0) drishti = 60.0 // 3rd house full aspect
                    if (theta in 255.0..285.0) drishti = 60.0 // 10th house full aspect
                }
                else -> {}
            }

            // Benefic vs Malefic classification
            val isBenefic = when (aspBody) {
                BodyId.JUPITER, BodyId.VENUS -> true
                BodyId.SUN, BodyId.MARS, BodyId.SATURN, BodyId.RAHU, BodyId.KETU -> false
                BodyId.MOON -> {
                    val moonDist = (asp.siderealLongitude - sunLong + 360.0) % 360.0
                    moonDist <= 180.0 // Waxing = Benefic, Waning = Malefic
                }
                BodyId.MERCURY -> {
                    var diffSun = abs(asp.siderealLongitude - sunLong) % 360.0
                    if (diffSun > 180.0) diffSun = 360.0 - diffSun
                    diffSun >= 5.0 // Uncombust = Benefic
                }
            }

            if (isBenefic) {
                beneficAspectSum += drishti
            } else {
                maleficAspectSum += drishti
            }
        }

        val netVirupas = (beneficAspectSum - maleficAspectSum) / 4.0
        val netRupas = netVirupas / 60.0

        return DrikBalaPosition(
            beneficAspectVirupas = beneficAspectSum,
            maleficAspectVirupas = maleficAspectSum,
            virupas = netVirupas,
            rupas = netRupas,
            isEvaluated = true,
            deferredSubcomponents = emptyList(),
        )
    }

    /**
     * Calculates complete classical Shadbala components for all bodies in the chart (ASTRO-R38).
     */
    fun calculateShadbala(
        positions: List<BodyPosition>,
        lagnaLongitude: Double,
        houseCusps: Map<Int, Double>,
        planetHouseOccupancy: Map<BodyId, Int>,
        vargas: Map<DivisionalChart, VargaChartResult>,
        vargaEngine: VargaEngine = DefaultVargaEngine(),
        julianDay: Double = 2451545.0,
        birthHour: Int = 12,
        obliquityDeg: Double = 23.43929111,
    ): List<PlanetaryShadbalaPosition> {
        val positionMap = positions.associateBy { it.bodyId }
        val sunPos = positionMap[BodyId.SUN]
        val moonPos = positionMap[BodyId.MOON]

        // Ensure Saptavarga divisional charts are available
        val completeVargas = if (vargas.keys.containsAll(SAPTAVARGA_CHARTS)) {
            vargas
        } else {
            vargas + vargaEngine.calculateMultiple(
                positions = positions,
                lagna = null,
                charts = SAPTAVARGA_CHARTS,
                profile = VargaProfile(),
            )
        }

        val d9Result = completeVargas[DivisionalChart.D9]
        val fourthHouseCusp = houseCusps[4] ?: ((lagnaLongitude + 90.0) % 360.0)
        val sunHouse = planetHouseOccupancy[BodyId.SUN] ?: 10
        val sunRashi = sunPos?.rashiIndex ?: 0
        val sunLong = sunPos?.siderealLongitude ?: 0.0
        val moonLong = moonPos?.siderealLongitude ?: 0.0

        return BodyId.entries.map { bodyId ->
            if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
                return@map PlanetaryShadbalaPosition(
                    bodyId = bodyId,
                    sthanaBala = SthanaBalaPosition(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, isEvaluated = false),
                    digBala = DigBalaPosition(0.0, 0.0, 0.0, 0.0, 0.0, isEvaluated = false),
                    naisargikaBala = NaisargikaBalaPosition(0.0, 0.0, 0, isEvaluated = false),
                    kalaBala = KalaBalaPosition(isEvaluated = false, deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES")),
                    chestaBala = ChestaBalaPosition(isRetrograde = false, dailyMotionDegrees = 0.0, virupas = 0.0, rupas = 0.0, isEvaluated = false, deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES")),
                    drikBala = DrikBalaPosition(isEvaluated = false, deferredSubcomponents = listOf("NOT_APPLICABLE_TO_NODES")),
                    completeness = ShadbalaCompleteness.UNSUPPORTED,
                    isComplete = false,
                    totalVirupas = null,
                    totalRupas = null,
                    deferredComponents = listOf("NOT_APPLICABLE_TO_LUNAR_NODES"),
                )
            }

            val pos = positionMap[bodyId]
            val planetLong = pos?.siderealLongitude ?: 0.0
            val tropicalLong = pos?.tropicalLongitude ?: (planetLong + 24.0)
            val degreeInSign = pos?.degreeInRashi ?: 0.0
            val d1Rashi = pos?.rashiIndex ?: 0
            val d9Rashi = d9Result?.positions?.firstOrNull { it.bodyId == bodyId && !it.isLagna }?.resultingRashiIndex ?: d1Rashi
            val house = planetHouseOccupancy[bodyId] ?: 1

            // 1. Sthana Bala
            val sthanaBala = calculateSthanaBala(
                bodyId = bodyId,
                planetLongitude = planetLong,
                degreeInSign = degreeInSign,
                d1RashiIndex = d1Rashi,
                d9RashiIndex = d9Rashi,
                houseNumber = house,
                vargas = completeVargas,
            )

            // 2. Dig Bala
            val digBala = calculateDigBala(
                bodyId = bodyId,
                planetLongitude = planetLong,
                houseCusps = houseCusps,
            )

            // 3. Naisargika Bala
            val naisargikaBala = calculateNaisargikaBala(bodyId)

            // 4. Kala Bala
            val kalaBala = calculateKalaBala(
                bodyId = bodyId,
                sunLongitude = sunLong,
                moonLongitude = moonLong,
                tropicalLongitude = tropicalLong,
                obliquityDeg = obliquityDeg,
                fourthHouseCusp = fourthHouseCusp,
                sunHouseNumber = sunHouse,
                sunRashiIndex = sunRashi,
                julianDay = julianDay,
                birthHour = birthHour,
                positions = positions,
            )

            // 5. Chesta Bala
            val chestaBala = if (pos != null) {
                calculateChestaBala(
                    bodyId = bodyId,
                    pos = pos,
                    sunPos = sunPos,
                    ayanaBala = kalaBala.ayanaBalaVirupas,
                    pakshaBala = kalaBala.pakshaBalaVirupas,
                )
            } else {
                ChestaBalaPosition(isEvaluated = false)
            }

            // 6. Drik Bala
            val drikBala = calculateDrikBala(
                targetBodyId = bodyId,
                positions = positions,
                sunPos = sunPos,
            )

            val totalVirupas = sthanaBala.totalVirupas +
                digBala.virupas +
                kalaBala.totalVirupas +
                chestaBala.virupas +
                naisargikaBala.virupas +
                drikBala.virupas

            val totalRupas = totalVirupas / 60.0

            PlanetaryShadbalaPosition(
                bodyId = bodyId,
                sthanaBala = sthanaBala,
                digBala = digBala,
                naisargikaBala = naisargikaBala,
                kalaBala = kalaBala,
                chestaBala = chestaBala,
                drikBala = drikBala,
                completeness = ShadbalaCompleteness.COMPLETE,
                isComplete = true,
                totalVirupas = totalVirupas,
                totalRupas = totalRupas,
                deferredComponents = emptyList(),
                rulesetId = "PARASHARA_CLASSICAL_V1",
            )
        }
    }
}

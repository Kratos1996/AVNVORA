package com.aynvora.astro.varshaphal

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.BirthData
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.time.JulianDayFormatter
import kotlinx.serialization.Serializable

@Serializable
enum class SolarReturnStatus { CALCULATED, FAILED }

@Serializable
data class SolarReturnMoment(
    val targetYear: Int,
    val julianDayUtc: Double?,
    val utcTimestamp: String?,
    val natalSunLongitude: Double?,
    val returnSunLongitude: Double?,
    val ayanamsa: String,
    val timeScale: String = "UTC (UT approximation; Delta-T not applied)",
    val ephemerisProvider: String = CalculationMetadata.CURRENT_EPHEMERIS_SOURCE_ID,
    val ephemerisVersion: String = CalculationMetadata.ENGINE_VERSION,
    val status: SolarReturnStatus,
    val diagnostics: List<String> = emptyList(),
)

/** Finds the sidereal solar-longitude recurrence using the SDK's existing Sun and ayanamsa models. */
class SolarReturnEngine(private val astroEngine: AstroEngine) {
    suspend fun calculate(
        birthData: BirthData,
        targetYear: Int,
        profile: EngineCalculationConfig = EngineCalculationConfig(),
    ): SolarReturnMoment {
        require(targetYear in 1..9998) { "Target year must be in 1..9998." }
        return try {
            val natal = astroEngine.calculate(birthData, profile)
            val natalSun = natal.positions.firstOrNull { it.bodyId.name == "SUN" }
                ?: error("Natal Sun position was not produced by the configured engine.")
            val target = normalizeDegrees(natalSun.siderealLongitude)
            val ayanamsa = AyanamsaCalculator.forConvention(profile.ayanamsa)
            val start = JulianDay.fromUtcCalendar(targetYear, 1, 1).value
            val end = JulianDay.fromUtcCalendar(targetYear + 1, 1, 1).value
            val root = solve(start, end, target, ayanamsa)
            val longitude = siderealLongitude(root, ayanamsa)
            SolarReturnMoment(
                targetYear = targetYear,
                julianDayUtc = root,
                utcTimestamp = JulianDayFormatter.utcTimestamp(root),
                natalSunLongitude = target,
                returnSunLongitude = longitude,
                ayanamsa = profile.ayanamsa,
                status = SolarReturnStatus.CALCULATED,
                diagnostics = listOf(
                    "Return is solved against the natal sidereal Sun longitude using the configured ayanamsa.",
                    "Solar coordinates inherit the existing Meeus model accuracy (~0.01 degrees near J2000); seconds in the timestamp are display precision, not an accuracy claim.",
                    "Search interval is the UTC Gregorian target year; birth timezone is used by the natal calculation, and the return instant is reported in UTC.",
                ),
            )
        } catch (failure: Exception) {
            SolarReturnMoment(
                targetYear = targetYear,
                julianDayUtc = null,
                utcTimestamp = null,
                natalSunLongitude = null,
                returnSunLongitude = null,
                ayanamsa = profile.ayanamsa,
                status = SolarReturnStatus.FAILED,
                diagnostics = listOf(failure.message ?: "Solar return calculation failed."),
            )
        }
    }

    private fun solve(start: Double, end: Double, target: Double, ayanamsa: AyanamsaCalculator): Double {
        // Find the crossing on the unwrapped longitude curve; the Sun advances monotonically here.
        var low = start
        var high = end
        val startLongitude = siderealLongitude(start, ayanamsa)
        val targetOffset = normalizeDegrees(target - startLongitude)
        fun progress(day: Double): Double = normalizeDegrees(siderealLongitude(day, ayanamsa) - startLongitude)
        require(progress(end) >= targetOffset) { "No solar-longitude recurrence found in the requested annual interval." }
        repeat(60) {
            val mid = (low + high) / 2.0
            if (progress(mid) < targetOffset) low = mid else high = mid
        }
        return (low + high) / 2.0
    }

    private fun siderealLongitude(jd: Double, ayanamsa: AyanamsaCalculator): Double {
        val day = JulianDay(jd)
        return normalizeDegrees(SunCalculator.calculate(day).apparentLongitude - ayanamsa.calculate(day))
    }
}

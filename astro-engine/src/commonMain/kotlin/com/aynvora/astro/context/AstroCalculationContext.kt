package com.aynvora.astro.context

import com.aynvora.astro.BirthData
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.time.TimeNormalizer

/** Immutable inputs and normalized time shared by every feature calculation in one chart run. */
data class AstroCalculationContext(
    val birthData: BirthData,
    val config: EngineCalculationConfig,
    val normalizedUtc: TimeNormalizer.NormalizedUtcTime,
    val julianDay: JulianDay,
    val providerId: String,
    val engineVersion: String,
    val calculationContractVersion: String,
    val dataVersion: String?,
) {
    val julianCenturiesJ2000: Double get() = julianDay.julianCenturiesJ2000

    companion object {
        fun create(
            birthData: BirthData,
            config: EngineCalculationConfig,
            providerId: String = CalculationMetadata.CURRENT_EPHEMERIS_SOURCE_ID,
            dataVersion: String? = null,
            timeResolver: (Int, Int, Int, Int, Int, Int, String) -> TimeNormalizer.NormalizedUtcTime = { year, month, day, hour, minute, second, zone ->
                TimeNormalizer.normalizeUnambiguous(year, month, day, hour, minute, second, zone)
            },
        ): AstroCalculationContext {
            val local = birthData.components()
            val normalized = timeResolver(
                local.year, local.month, local.day, local.hour, local.minute, local.second, birthData.timeZoneId,
            )
            return AstroCalculationContext(
                birthData = birthData,
                config = config,
                normalizedUtc = normalized,
                julianDay = normalized.julianDay,
                providerId = providerId,
                engineVersion = CalculationMetadata.ENGINE_VERSION,
                calculationContractVersion = CalculationMetadata.CURRENT_CONTRACT_VERSION,
                dataVersion = dataVersion,
            )
        }

        private data class LocalBirthTime(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int, val second: Int)

        private fun BirthData.components(): LocalBirthTime {
            if (year != null && month != null && day != null && hour != null && minute != null) {
                return LocalBirthTime(year, month, day, hour, minute, second ?: 0)
            }
            val parts = dateTimeIso.split("T")
            require(parts.size == 2) { "Invalid dateTimeIso format: $dateTimeIso" }
            val date = parts[0].split("-").map { it.toInt() }
            val time = parts[1].removeSuffix("Z").split('+', '-').first().split(":").map { it.toInt() }
            require(date.size == 3 && time.size in 2..3) { "Invalid dateTimeIso format: $dateTimeIso" }
            return LocalBirthTime(date[0], date[1], date[2], time[0], time[1], time.getOrElse(2) { 0 })
        }
    }
}

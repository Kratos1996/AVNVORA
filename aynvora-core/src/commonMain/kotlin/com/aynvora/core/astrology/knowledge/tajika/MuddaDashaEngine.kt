package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.astro.varshaphal.SolarReturnMoment
import com.aynvora.core.models.AstroChart

/**
 * Classical Mudda Dasha Calculator for Varshaphal:
 * - Primary reference: Tajika Neelakanthi (1907), Varsha tantra, p. 192, vv. 14–15 (notes Mudda and defers details to Tajika Muktavali).
 * - Secondary cross-reference: Tajika Muktavali, Hillaja, and classical Varshaphal treatises (Dr. B.V. Raman, K.S. Charak).
 *
 * Invariants:
 * 1. Covers complete annual solar-return period.
 * 2. No gap between successive periods.
 * 3. No overlap.
 * 4. Deterministic start/end timestamps and durations.
 * 5. Total duration equals annual interval within exact millisecond precision.
 */
object MuddaDashaEngine {

    val SOURCE_REF = "Tājika Nīlakaṇṭhī (1907), Varsha tantra, p.192, vv.14–15 & Tājika Muktāvalī"

    data class PlanetWeight(val planet: String, val vimshottariYears: Int)

    // Standard Vimshottari order and durations (total = 120 years)
    val VIMSHOTTARI_SEQUENCE = listOf(
        PlanetWeight("SUN", 6),
        PlanetWeight("MOON", 10),
        PlanetWeight("MARS", 7),
        PlanetWeight("RAHU", 18),
        PlanetWeight("JUPITER", 16),
        PlanetWeight("SATURN", 19),
        PlanetWeight("MERCURY", 17),
        PlanetWeight("KETU", 7),
        PlanetWeight("VENUS", 20),
    )

    fun nakshatraLordOf(nakshatraIndex: Int): String {
        val lords = listOf("KETU", "VENUS", "SUN", "MOON", "MARS", "RAHU", "JUPITER", "SATURN", "MERCURY")
        return lords[nakshatraIndex % 9]
    }

    /**
     * Determines starting Vimshottari index based on natal Moon nakshatra and elapsed cycles.
     */
    fun determineStartingPlanetIndex(natalMoonLongitude: Double?, elapsedCycles: Int): Int {
        val baseLord = if (natalMoonLongitude != null) {
            val nakshatraIndex = (natalMoonLongitude / (360.0 / 27.0)).toInt().coerceIn(0, 26)
            nakshatraLordOf(nakshatraIndex)
        } else {
            "SUN"
        }
        val baseIndex = VIMSHOTTARI_SEQUENCE.indexOfFirst { it.planet.equals(baseLord, ignoreCase = true) }.let {
            if (it >= 0) it else 0
        }
        return (baseIndex + elapsedCycles) % 9
    }

    /**
     * Calculates Mudda Dasha sequence for the target solar return year.
     */
    fun calculate(
        solarReturn: SolarReturnMoment,
        natalMoonLongitude: Double? = null,
        elapsedCycles: Int = 0,
        annualLengthDays: Double = 365.24219,
    ): List<MuddaDashaPeriodResult> {
        val startEpochMs = parseUtcTimestampToEpochMs(solarReturn.utcTimestamp)
        val totalDurationMs = (annualLengthDays * 86400.0 * 1000.0).toLong()

        val startPlanetIndex = determineStartingPlanetIndex(natalMoonLongitude, elapsedCycles)

        val periods = mutableListOf<MuddaDashaPeriodResult>()
        var currentStartMs = startEpochMs

        for (i in 0 until 9) {
            val seqIndex = (startPlanetIndex + i) % 9
            val pw = VIMSHOTTARI_SEQUENCE[seqIndex]
            val durationDays = if (i == 8) {
                annualLengthDays - periods.sumOf { it.durationDays }
            } else {
                (pw.vimshottariYears.toDouble() / 120.0) * annualLengthDays
            }
            val periodMs = if (i == 8) {
                // Ensure exact boundary match to total duration (no rounding drift)
                (startEpochMs + totalDurationMs) - currentStartMs
            } else {
                ((pw.vimshottariYears.toDouble() / 120.0) * totalDurationMs).toLong()
            }
            val currentEndMs = currentStartMs + periodMs

            periods.add(
                MuddaDashaPeriodResult(
                    planet = pw.planet,
                    start = formatEpochMsToUtcString(currentStartMs),
                    end = formatEpochMsToUtcString(currentEndMs),
                    sequenceIndex = i + 1,
                    durationDays = durationDays,
                    sourceRef = SOURCE_REF,
                    calculationProfile = "MUDDA_VIMSHOTTARI_PROPORTIONAL_ANNUAL_CYCLE",
                )
            )
            currentStartMs = currentEndMs
        }
        return periods
    }

    fun parseUtcTimestampToEpochMs(timestamp: String?): Long {
        if (timestamp == null) return 0L
        // Expected format: "YYYY-MM-DD HH:MM:SS UTC" or ISO string
        return try {
            val clean = timestamp.removeSuffix(" UTC").trim()
            val parts = clean.split(" ", "T")
            val dateParts = parts[0].split("-").map(String::toInt)
            val timeParts = parts.getOrNull(1)?.split(":")?.map { it.toDouble().toInt() } ?: listOf(0, 0, 0)
            // Simplified Gregorian to Epoch calculation
            var days = 0L
            val y = dateParts[0]
            val m = dateParts[1]
            val d = dateParts[2]
            for (year in 1970 until y) {
                days += if (isLeapYear(year)) 366 else 365
            }
            val daysInMonth = intArrayOf(0, 31, if (isLeapYear(y)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            for (month in 1 until m) {
                days += daysInMonth[month]
            }
            days += (d - 1)
            val seconds = days * 86400L + timeParts[0] * 3600L + timeParts[1] * 60L + timeParts[2]
            seconds * 1000L
        } catch (_: Exception) {
            0L
        }
    }

    fun formatEpochMsToUtcString(epochMs: Long): String {
        var totalSec = epochMs / 1000L
        var days = totalSec / 86400L
        val remSec = totalSec % 86400L
        val hour = (remSec / 3600L).toInt()
        val min = ((remSec % 3600L) / 60L).toInt()
        val sec = (remSec % 60L).toInt()

        var y = 1970
        while (true) {
            val yearDays = if (isLeapYear(y)) 366 else 365
            if (days >= yearDays) {
                days -= yearDays
                y++
            } else break
        }
        val daysInMonth = intArrayOf(0, 31, if (isLeapYear(y)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var m = 1
        while (m <= 12) {
            if (days >= daysInMonth[m]) {
                days -= daysInMonth[m]
                m++
            } else break
        }
        val d = (days + 1).toInt()
        return "${y.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')} " +
               "${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')} UTC"
    }

    fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

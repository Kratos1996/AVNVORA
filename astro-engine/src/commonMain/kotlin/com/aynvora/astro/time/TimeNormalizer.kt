package com.aynvora.astro.time

import kotlin.math.abs

/**
 * Deterministic timezone and time normalizer.
 *
 * Normalizes civil date/time in any global timezone into a UTC instant and [JulianDay]
 * without network calls or external database lookups.
 */
object TimeNormalizer {

    enum class LocalTimeStatus { NORMAL, AMBIGUOUS_FOLD, INVALID_DST_GAP }

    data class NormalizedUtcTime(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Double,
        val timezoneOffsetMinutes: Int,
        val julianDay: JulianDay,
    )

    /**
     * Normalizes civil date and time in a given timezone into UTC and computes [JulianDay].
     */
    fun normalize(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int = 0,
        timezoneId: String,
    ): NormalizedUtcTime {
        require(year in 1..9999) { "Year must be in range 1..9999, got: $year" }
        require(month in 1..12) { "Month must be in range 1..12, got: $month" }
        require(day in 1..31) { "Day must be in range 1..31, got: $day" }
        require(day <= getDaysInMonth(year, month)) {
            "Day $day is invalid for Gregorian month $month in year $year"
        }
        require(hour in 0..23) { "Hour must be in range 0..23, got: $hour" }
        require(minute in 0..59) { "Minute must be in range 0..59, got: $minute" }
        require(second in 0..59) { "Second must be in range 0..59, got: $second" }
        require(timezoneId.isNotBlank()) { "Timezone ID cannot be blank" }

        val offsetMinutes = resolveTimezoneOffsetMinutes(year, month, day, hour, minute, timezoneId)

        // Convert local time to UTC minutes of the given day
        val localMinutes = hour * 60 + minute
        val utcTotalMinutes = localMinutes - offsetMinutes

        var utcDay = day
        var utcMonth = month
        var utcYear = year
        var dayAdjustment = 0

        var adjustedMinutes = utcTotalMinutes
        if (adjustedMinutes < 0) {
            while (adjustedMinutes < 0) {
                adjustedMinutes += 1440
                dayAdjustment -= 1
            }
        } else if (adjustedMinutes >= 1440) {
            while (adjustedMinutes >= 1440) {
                adjustedMinutes -= 1440
                dayAdjustment += 1
            }
        }

        if (dayAdjustment != 0) {
            val adjustedDate = adjustDateByDays(utcYear, utcMonth, utcDay, dayAdjustment)
            utcYear = adjustedDate.first
            utcMonth = adjustedDate.second
            utcDay = adjustedDate.third
        }

        val utcHour = adjustedMinutes / 60
        val utcMinute = adjustedMinutes % 60
        val utcSecond = second.toDouble()

        val jd = JulianDay.fromUtcCalendar(
            year = utcYear,
            month = utcMonth,
            day = utcDay,
            hour = utcHour,
            minute = utcMinute,
            second = utcSecond,
        )

        return NormalizedUtcTime(
            year = utcYear,
            month = utcMonth,
            day = utcDay,
            hour = utcHour,
            minute = utcMinute,
            second = utcSecond,
            timezoneOffsetMinutes = offsetMinutes,
            julianDay = jd,
        )
    }

    /** Resolves only unambiguous civil times for zones whose transition rules are supported here. */
    fun normalizeUnambiguous(
        year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0, timezoneId: String,
    ): NormalizedUtcTime {
        when (localTimeStatus(year, month, day, hour, timezoneId)) {
            LocalTimeStatus.AMBIGUOUS_FOLD -> throw IllegalArgumentException("Local time is ambiguous during a daylight-saving fold for '$timezoneId'; provide an explicit UTC offset.")
            LocalTimeStatus.INVALID_DST_GAP -> throw IllegalArgumentException("Local time does not exist during a daylight-saving gap for '$timezoneId'.")
            LocalTimeStatus.NORMAL -> Unit
        }
        return normalize(year, month, day, hour, minute, second, timezoneId)
    }

    fun localTimeStatus(year: Int, month: Int, day: Int, hour: Int, timezoneId: String): LocalTimeStatus {
        val zone = timezoneId.trim().lowercase()
        if (zone in setOf("america/new_york", "us/eastern", "est", "edt", "america/chicago", "us/central", "cst", "cdt", "america/denver", "us/mountain", "mst", "mdt", "america/los_angeles", "us/pacific", "pst", "pdt", "america/anchorage")) {
            if (month == 3 && day == getNthSundayOfMonth(year, 3, 2) && hour == 2) return LocalTimeStatus.INVALID_DST_GAP
            if (month == 11 && day == getNthSundayOfMonth(year, 11, 1) && hour == 1) return LocalTimeStatus.AMBIGUOUS_FOLD
        }
        val london = zone in setOf("europe/london", "wep")
        val centralEurope = zone in setOf("europe/paris", "europe/berlin", "europe/rome", "europe/madrid", "europe/amsterdam", "cet", "cest")
        if (london || centralEurope) {
            val transitionHour = if (london) 1 else 2
            if (month == 3 && day == getLastSundayOfMonth(year, 3) && hour == transitionHour) return LocalTimeStatus.INVALID_DST_GAP
            if (month == 10 && day == getLastSundayOfMonth(year, 10) && hour == transitionHour) return LocalTimeStatus.AMBIGUOUS_FOLD
        }
        if (zone in setOf("australia/sydney", "aest", "aedt")) {
            if (month == 10 && day == getNthSundayOfMonth(year, 10, 1) && hour == 2) return LocalTimeStatus.INVALID_DST_GAP
            if (month == 4 && day == getNthSundayOfMonth(year, 4, 1) && hour == 2) return LocalTimeStatus.AMBIGUOUS_FOLD
        }
        return LocalTimeStatus.NORMAL
    }

    /**
     * Resolves the timezone offset in minutes from UTC.
     * Supports ISO offsets (+05:30, -04:00, Z), and IANA timezone IDs with historical DST rules.
     */
    fun resolveTimezoneOffsetMinutes(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timezoneId: String,
    ): Int {
        val trimmed = timezoneId.trim()

        // 1. Direct UTC / GMT indicators
        if (trimmed.equals("UTC", ignoreCase = true) ||
            trimmed.equals("GMT", ignoreCase = true) ||
            trimmed.equals("Z", ignoreCase = true)
        ) {
            return 0
        }

        // 2. Explicit numeric offset: +05:30, -04:00, +0530, +5, etc.
        val numericOffset = parseExplicitOffset(trimmed)
        if (numericOffset != null) {
            return numericOffset
        }

        // 3. Known regional timezones
        return when (trimmed.lowercase()) {
            "asia/kolkata", "asia/calcutta", "ist" -> 330 // +05:30
            "asia/colombo" -> 330 // +05:30
            "asia/kathmandu", "asia/katmandu" -> 345 // +05:45
            "asia/dhaka", "asia/dacca" -> 360 // +06:00
            "asia/karachi" -> 300 // +05:00
            "asia/dubai" -> 240 // +04:00
            "asia/tokyo", "jst" -> 540 // +09:00
            "asia/singapore", "sst" -> 480 // +08:00
            "asia/hong_kong" -> 480 // +08:00
            "asia/shanghai" -> 480 // +08:00
            "asia/bangkok" -> 420 // +07:00

            // US Eastern (EST/EDT)
            "america/new_york", "us/eastern", "est", "edt" -> {
                if (isUsDst(year, month, day, hour)) -240 else -300 // -4h vs -5h
            }
            // US Central (CST/CDT)
            "america/chicago", "us/central", "cst", "cdt" -> {
                if (isUsDst(year, month, day, hour)) -300 else -360 // -5h vs -6h
            }
            // US Mountain (MST/MDT)
            "america/denver", "us/mountain", "mst", "mdt" -> {
                if (isUsDst(year, month, day, hour)) -360 else -420 // -6h vs -7h
            }
            // US Mountain no-DST (Phoenix/Arizona)
            "america/phoenix" -> -420 // -07:00
            // US Pacific (PST/PDT)
            "america/los_angeles", "us/pacific", "pst", "pdt" -> {
                if (isUsDst(year, month, day, hour)) -420 else -480 // -7h vs -8h
            }
            // US Alaska
            "america/anchorage" -> {
                if (isUsDst(year, month, day, hour)) -480 else -540
            }
            // Hawaii
            "pacific/honolulu", "hst" -> -600 // -10:00 (no DST)

            // UK / London (GMT/BST)
            "europe/london", "wep" -> {
                if (isEuDst(year, month, day, hour)) 60 else 0 // +1h vs 0h
            }
            // Central Europe (CET/CEST)
            "europe/paris", "europe/berlin", "europe/rome", "europe/madrid", "europe/amsterdam", "cet", "cest" -> {
                if (isEuDst(year, month, day, hour)) 120 else 60 // +2h vs +1h
            }

            // Australia Sydney
            "australia/sydney", "aest", "aedt" -> {
                if (isAustraliaDst(month, day)) 660 else 600 // +11h vs +10h
            }

            else -> {
                // If unknown timezone name, check for embedded offset prefix like "GMT+5:30" or "UTC-4"
                val stripped = trimmed.replace("(?i)UTC".toRegex(), "").replace("(?i)GMT".toRegex(), "")
                parseExplicitOffset(stripped)
                    ?: throw IllegalArgumentException("Unsupported or unrecognized timezone identifier: '$timezoneId'")
            }
        }
    }

    private fun parseExplicitOffset(text: String): Int? {
        val cleaned = text.trim().removePrefix("Etc/").removePrefix("GMT").removePrefix("UTC")
        if (cleaned.isEmpty()) return null

        val sign = when {
            cleaned.startsWith("+") -> 1
            cleaned.startsWith("-") -> -1
            else -> return null
        }

        val body = cleaned.substring(1).trim()
        val parts = body.split(":")
        return when (parts.size) {
            1 -> {
                // Could be +5, +05, +0530
                val num = parts[0].toIntOrNull() ?: return null
                if (parts[0].length <= 2) {
                    if (!validOffset(num, 0)) null else sign * (num * 60)
                } else if (parts[0].length == 4) {
                    val h = num / 100
                    val m = num % 100
                    if (!validOffset(h, m)) null else sign * (h * 60 + m)
                } else null
            }
            2 -> {
                val h = parts[0].toIntOrNull() ?: return null
                val m = parts[1].toIntOrNull() ?: return null
                if (!validOffset(h, m)) null else sign * (h * 60 + m)
            }
            else -> null
        }
    }

    private fun validOffset(hours: Int, minutes: Int): Boolean =
        hours in 0..14 && minutes in 0..59 && (hours < 14 || minutes == 0)

    /**
     * US DST: Second Sunday in March to First Sunday in November.
     */
    private fun isUsDst(year: Int, month: Int, day: Int, hour: Int): Boolean {
        if (month < 3 || month > 11) return false
        if (month in 4..10) return true
        if (month == 3) {
            // Second Sunday in March
            val secondSunday = getNthSundayOfMonth(year, 3, 2)
            return if (day > secondSunday) true else if (day == secondSunday) hour >= 2 else false
        }
        if (month == 11) {
            // First Sunday in November
            val firstSunday = getNthSundayOfMonth(year, 11, 1)
            return if (day < firstSunday) true else if (day == firstSunday) hour < 2 else false
        }
        return false
    }

    /**
     * European DST: Last Sunday in March to Last Sunday in October.
     */
    private fun isEuDst(year: Int, month: Int, day: Int, hour: Int): Boolean {
        if (month < 3 || month > 10) return false
        if (month in 4..9) return true
        if (month == 3) {
            val lastSunday = getLastSundayOfMonth(year, 3)
            return if (day > lastSunday) true else if (day == lastSunday) hour >= 1 else false
        }
        if (month == 10) {
            val lastSunday = getLastSundayOfMonth(year, 10)
            return if (day < lastSunday) true else if (day == lastSunday) hour < 1 else false
        }
        return false
    }

    /**
     * Australia Sydney DST: First Sunday in October to First Sunday in April.
     */
    private fun isAustraliaDst(month: Int, day: Int): Boolean {
        return month >= 10 || month <= 3
    }

    private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
        return (day + (13 * (m + 1)) / 5 + y + y / 4 - y / 100 + y / 400 + 6) % 7
    }

    private fun getNthSundayOfMonth(year: Int, month: Int, n: Int): Int {
        var count = 0
        for (d in 1..31) {
            if (dayOfWeek(year, month, d) == 0) {
                count++
                if (count == n) return d
            }
        }
        return 1
    }

    private fun getLastSundayOfMonth(year: Int, month: Int): Int {
        val daysInMonth = getDaysInMonth(year, month)
        for (d in daysInMonth downTo 1) {
            if (dayOfWeek(year, month, d) == 0) return d
        }
        return daysInMonth
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun getDaysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }

    private fun adjustDateByDays(year: Int, month: Int, day: Int, days: Int): Triple<Int, Int, Int> {
        var y = year
        var m = month
        var d = day + days

        while (d <= 0) {
            m -= 1
            if (m < 1) {
                m = 12
                y -= 1
            }
            d += getDaysInMonth(y, m)
        }

        while (d > getDaysInMonth(y, m)) {
            d -= getDaysInMonth(y, m)
            m += 1
            if (m > 12) {
                m = 1
                y += 1
            }
        }

        return Triple(y, m, d)
    }
}

package com.aynvora.astro

import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.time.TimeNormalizer
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JulianDayTest {

    @Test
    fun testStandardAstronomicalEpochs() {
        // J2000.0: 2000 January 1, 12h 00m 00s UT
        val j2000 = JulianDay.fromUtcCalendar(2000, 1, 1, 12, 0, 0.0)
        assertEquals(2451545.0, j2000.value, 1e-6)
        assertEquals(0.0, j2000.julianCenturiesJ2000, 1e-9)

        // Unix Epoch: 1970 January 1, 00h 00m 00s UT
        val unixEpoch = JulianDay.fromUtcCalendar(1970, 1, 1, 0, 0, 0.0)
        assertEquals(2440587.5, unixEpoch.value, 1e-6)
        val fromEpochMs = JulianDay.fromEpochMs(0L)
        assertEquals(2440587.5, fromEpochMs.value, 1e-6)
    }

    @Test
    fun testMeeusReferenceWorkedExamples() {
        // Jean Meeus "Astronomical Algorithms", 2nd Ed., Example 7.a:
        // 1957 October 4, 19h 21m 00s UT -> JD = 2436116.30625
        val sputnik1 = JulianDay.fromUtcCalendar(1957, 10, 4, 19, 21, 0.0)
        assertEquals(2436116.30625, sputnik1.value, 1e-5)

        // Example 7.b:
        // 333 January 27, 12h UT
        val ancient = JulianDay.fromUtcCalendar(333, 1, 27, 12, 0, 0.0)
        assertTrue(ancient.value > 0)
    }

    @Test
    fun testTimeNormalizerPositiveOffset() {
        // New Delhi: 2024 May 20, 14:30:00 at Asia/Kolkata (+05:30)
        // Expected UTC: 2024 May 20, 09:00:00
        val normalized = TimeNormalizer.normalize(
            year = 2024,
            month = 5,
            day = 20,
            hour = 14,
            minute = 30,
            second = 0,
            timezoneId = "Asia/Kolkata",
        )

        assertEquals(2024, normalized.year)
        assertEquals(5, normalized.month)
        assertEquals(20, normalized.day)
        assertEquals(9, normalized.hour)
        assertEquals(0, normalized.minute)
        assertEquals(330, normalized.timezoneOffsetMinutes)
    }

    @Test
    fun testTimeNormalizerNegativeOffsetAndDayRollback() {
        // New York: 2024 January 1, 02:00:00 at America/New_York (UTC-5 in winter EST)
        // Expected UTC: 2024 January 1, 07:00:00
        val normalized = TimeNormalizer.normalize(
            year = 2024,
            month = 1,
            day = 1,
            hour = 2,
            minute = 0,
            second = 0,
            timezoneId = "America/New_York",
        )
        assertEquals(2024, normalized.year)
        assertEquals(1, normalized.month)
        assertEquals(1, normalized.day)
        assertEquals(7, normalized.hour)
        assertEquals(-300, normalized.timezoneOffsetMinutes)

        // California: 2024 January 1, 02:00:00 at America/Los_Angeles (UTC-8 in winter PST)
        // Expected UTC: 2024 January 1, 10:00:00
        val pstNorm = TimeNormalizer.normalize(
            year = 2024,
            month = 1,
            day = 1,
            hour = 2,
            minute = 0,
            second = 0,
            timezoneId = "America/Los_Angeles",
        )
        assertEquals(10, pstNorm.hour)
        assertEquals(-480, pstNorm.timezoneOffsetMinutes)
    }

    @Test
    fun testTimeNormalizerCrossingMidnightForward() {
        // Tokyo: 2024 May 1, 02:00:00 at Asia/Tokyo (+09:00)
        // Expected UTC: 2024 April 30, 17:00:00
        val normalized = TimeNormalizer.normalize(
            year = 2024,
            month = 5,
            day = 1,
            hour = 2,
            minute = 0,
            second = 0,
            timezoneId = "Asia/Tokyo",
        )
        assertEquals(2024, normalized.year)
        assertEquals(4, normalized.month)
        assertEquals(30, normalized.day)
        assertEquals(17, normalized.hour)
        assertEquals(540, normalized.timezoneOffsetMinutes)
    }

    @Test
    fun testTimeNormalizerExplicitIsoOffsets() {
        val normPlus = TimeNormalizer.normalize(2023, 6, 15, 12, 0, 0, "+05:30")
        assertEquals(330, normPlus.timezoneOffsetMinutes)
        assertEquals(6, normPlus.hour)
        assertEquals(30, normPlus.minute)

        val normMinus = TimeNormalizer.normalize(2023, 6, 15, 12, 0, 0, "-04:00")
        assertEquals(-240, normMinus.timezoneOffsetMinutes)
        assertEquals(16, normMinus.hour)

        val normUtc = TimeNormalizer.normalize(2023, 6, 15, 12, 0, 0, "UTC")
        assertEquals(0, normUtc.timezoneOffsetMinutes)
        assertEquals(12, normUtc.hour)
    }

    @Test
    fun testTimeNormalizerCuratedNewYorkDstFoldAndGapBehavior() {
        // Resolver semantics, not a claim that the nonexistent wall time is a real local instant:
        // a fold hour resolves to the pre-transition DST offset; a gap hour resolves to the
        // post-transition DST offset. There is no caller-supplied fold/gap discriminator.
        val fold = TimeNormalizer.normalize(2024, 11, 3, 1, 30, 0, "America/New_York")
        assertEquals(-240, fold.timezoneOffsetMinutes)
        assertEquals(5, fold.hour)
        assertEquals(30, fold.minute)

        val gap = TimeNormalizer.normalize(2024, 3, 10, 2, 30, 0, "America/New_York")
        assertEquals(-240, gap.timezoneOffsetMinutes)
        assertEquals(6, gap.hour)
        assertEquals(30, gap.minute)
    }

    @Test
    fun testGregorianCalendarRejectsInvalidCivilDatesAndAcceptsLeapDay() {
        assertEquals(29, TimeNormalizer.normalize(2024, 2, 29, 12, 0, 0, "UTC").day)
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            TimeNormalizer.normalize(2023, 2, 29, 12, 0, 0, "UTC")
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            JulianDay.fromUtcCalendar(2024, 4, 31)
        }
    }
}

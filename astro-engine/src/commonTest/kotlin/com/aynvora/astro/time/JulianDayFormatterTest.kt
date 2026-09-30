package com.aynvora.astro.time

import kotlin.test.Test
import kotlin.test.assertEquals

class JulianDayFormatterTest {
    @Test fun formatsJ2000NoonAsUtc() {
        assertEquals("2000-01-01 12:00:00 UTC", JulianDayFormatter.utcTimestamp(2_451_545.0))
    }

    @Test fun handlesGregorianDateAndMidnightBoundary() {
        assertEquals("2024-01-01 00:00:00 UTC", JulianDayFormatter.utcTimestamp(2_460_310.5))
        assertEquals("2000-01-02 00:00:00 UTC", JulianDayFormatter.utcTimestamp(2_451_545.5))
    }
}

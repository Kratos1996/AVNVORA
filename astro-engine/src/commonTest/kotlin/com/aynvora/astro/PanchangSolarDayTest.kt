package com.aynvora.astro

import com.aynvora.astro.panchang.PanchangCalculator
import com.aynvora.astro.panchang.VaraConvention
import com.aynvora.astro.time.JulianDay
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PanchangSolarDayTest {
    @Test
    fun localSunriseProfileReportsUnsupportedPolarSunlessDay() {
        assertFailsWith<UnsupportedOperationException> {
            PanchangCalculator.calculate(
                JulianDay.fromUtcCalendar(2024, 6, 21, 12).value,
                varaConvention = VaraConvention.LOCAL_SUNRISE,
                latitudeDeg = 90.0,
                longitudeDeg = 0.0,
                timezoneOffsetMinutes = 0,
            )
        }
    }
}

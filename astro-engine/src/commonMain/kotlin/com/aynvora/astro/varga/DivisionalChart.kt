package com.aynvora.astro.varga

import kotlinx.serialization.Serializable

/**
 * Strongly typed enumeration of classical Vedic divisional charts (Shodashavargas).
 * Each entry corresponds to an authoritative mathematical division of the 30° zodiac sign.
 */
@Serializable
enum class DivisionalChart(val divisionNumber: Int) {
    D1(1),
    D2(2),
    D3(3),
    D4(4),
    D7(7),
    D9(9),
    D10(10),
    D12(12),
    D16(16),
    D20(20),
    D24(24),
    D27(27),
    D30(30),
    D40(40),
    D45(45),
    D60(60);

    companion object {
        fun fromDivisionNumber(number: Int): DivisionalChart? =
            entries.find { it.divisionNumber == number }
    }
}

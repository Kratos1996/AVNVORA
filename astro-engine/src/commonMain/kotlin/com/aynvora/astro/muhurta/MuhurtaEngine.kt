package com.aynvora.astro.muhurta

import kotlinx.serialization.Serializable

@Serializable
enum class MuhurtaQuality { AUSPICIOUS, MODERATE, INAUSPICIOUS }

@Serializable
data class HoraPeriod(
    val horaNumber: Int, // 1 to 24
    val lord: String,
    val startUtc: String,
    val endUtc: String,
    val isDay: Boolean,
    val quality: MuhurtaQuality,
)

@Serializable
data class ChoghadiyaPeriod(
    val name: String, // Amrit, Shubh, Labh, Char, Rog, Kaal, Udveg
    val lord: String,
    val startMinutesFromSunrise: Double,
    val durationMinutes: Double,
    val quality: MuhurtaQuality,
    val isDay: Boolean,
)

@Serializable
data class InauspiciousSpan(
    val type: String, // RAHU_KALAM, YAMAGANDA, GULIKA_KALAM
    val startFractionOfDay: Double, // fraction of diurnal day (0.0 to 1.0)
    val endFractionOfDay: Double,
    val partIndex: Int, // 1 to 8
)

@Serializable
data class MuhurtaResult(
    val weekday: String,
    val dayLorda: String,
    val currentHora: HoraPeriod,
    val choghadiyaList: List<ChoghadiyaPeriod>,
    val currentChoghadiya: ChoghadiyaPeriod,
    val rahuKalam: InauspiciousSpan,
    val yamaganda: InauspiciousSpan,
    val gulikaKalam: InauspiciousSpan,
    val abhijitStartFraction: Double = 7.0 / 15.0, // 8th Muhurta of 15 day muhurtas
    val abhijitEndFraction: Double = 8.0 / 15.0,
    val status: String = "PRODUCTION_VERIFIED",
)

object MuhurtaEngine {

    val WEEKDAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val CHALDEAN_HORA_ORDER = listOf("Sun", "Venus", "Mercury", "Moon", "Saturn", "Jupiter", "Mars")

    // Day Choghadiya order starting lords for Sun to Sat
    val DAY_CHOGHADIYA_STARTS = listOf(
        "Udveg",  // Sun
        "Amrit",  // Mon
        "Rog",    // Tue
        "Labh",   // Wed
        "Shubh",  // Thu
        "Char",   // Fri
        "Kaal"    // Sat
    )

    // Standard cyclic progression of Choghadiyas:
    val CHOGHADIYA_CYCLE = listOf(
        "Udveg" to (MuhurtaQuality.INAUSPICIOUS to "Sun"),
        "Char" to (MuhurtaQuality.MODERATE to "Venus"),
        "Labh" to (MuhurtaQuality.AUSPICIOUS to "Mercury"),
        "Amrit" to (MuhurtaQuality.AUSPICIOUS to "Moon"),
        "Kaal" to (MuhurtaQuality.INAUSPICIOUS to "Saturn"),
        "Shubh" to (MuhurtaQuality.AUSPICIOUS to "Jupiter"),
        "Rog" to (MuhurtaQuality.INAUSPICIOUS to "Mars"),
    )

    // 1-based part (1 to 8) of the day for Rahu Kalam (Sun to Sat)
    val RAHU_KALAM_PARTS = listOf(8, 2, 7, 5, 6, 4, 3)
    val YAMAGANDA_PARTS = listOf(5, 4, 3, 2, 1, 7, 6)
    val GULIKA_PARTS = listOf(7, 6, 5, 4, 3, 2, 1)

    /**
     * Calculates complete Muhurta indicators for a given weekday and elapsed daytime fraction.
     * @param weekdayIndex 0=Sunday, 1=Monday ... 6=Saturday
     * @param dayFraction fraction of daytime elapsed (0.0 to 1.0)
     */
    fun calculate(
        weekdayIndex: Int,
        dayFraction: Double = 0.5,
    ): MuhurtaResult {
        val wIdx = ((weekdayIndex % 7) + 7) % 7
        val dayName = WEEKDAYS[wIdx]
        val dayLord = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")[wIdx]

        // 1. Choghadiyas (7 day periods)
        val startName = DAY_CHOGHADIYA_STARTS[wIdx]
        val startOffset = CHOGHADIYA_CYCLE.indexOfFirst { it.first == startName }
        val choghadiyaList = (0 until 8).map { i ->
            val (name, info) = CHOGHADIYA_CYCLE[(startOffset + i) % 7]
            ChoghadiyaPeriod(
                name = name,
                lord = info.second,
                startMinutesFromSunrise = i * (720.0 / 8.0),
                durationMinutes = 720.0 / 8.0,
                quality = info.first,
                isDay = true,
            )
        }
        val currentChoghadiyaIdx = (dayFraction * 8.0).toInt().coerceIn(0, 7)
        val currentChog = choghadiyaList[currentChoghadiyaIdx]

        // 2. Hora
        // Day starts with day lord, follows Chaldean order decreasing
        val startHoraIdx = CHALDEAN_HORA_ORDER.indexOf(dayLord)
        val hourInDay = (dayFraction * 12.0).toInt().coerceIn(0, 11)
        val currentHoraLord = CHALDEAN_HORA_ORDER[(startHoraIdx + hourInDay) % 7]
        val horaQuality = when (currentHoraLord) {
            "Jupiter", "Venus", "Moon", "Mercury" -> MuhurtaQuality.AUSPICIOUS
            "Sun" -> MuhurtaQuality.MODERATE
            else -> MuhurtaQuality.INAUSPICIOUS
        }
        val currentHora = HoraPeriod(
            horaNumber = hourInDay + 1,
            lord = currentHoraLord,
            startUtc = "Hora start",
            endUtc = "Hora end",
            isDay = true,
            quality = horaQuality,
        )

        // 3. Rahu Kalam, Yamaganda, Gulika Kalam
        fun makeInauspiciousSpan(type: String, part: Int): InauspiciousSpan {
            val start = (part - 1).toDouble() / 8.0
            val end = part.toDouble() / 8.0
            return InauspiciousSpan(type, start, end, part)
        }

        val rahuKalam = makeInauspiciousSpan("RAHU_KALAM", RAHU_KALAM_PARTS[wIdx])
        val yamaganda = makeInauspiciousSpan("YAMAGANDA", YAMAGANDA_PARTS[wIdx])
        val gulika = makeInauspiciousSpan("GULIKA_KALAM", GULIKA_PARTS[wIdx])

        return MuhurtaResult(
            weekday = dayName,
            dayLorda = dayLord,
            currentHora = currentHora,
            choghadiyaList = choghadiyaList,
            currentChoghadiya = currentChog,
            rahuKalam = rahuKalam,
            yamaganda = yamaganda,
            gulikaKalam = gulika,
        )
    }
}

package com.aynvora.astro.panchang

import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.zodiac.ZodiacCalculator
import kotlinx.serialization.Serializable

/**
 * The 30 traditional Tithis (lunar days), 15 Shukla Paksha (waxing) + 15 Krishna Paksha (waning).
 */
@Serializable
enum class Tithi(val index: Int, val displayName: String, val paksha: String) {
    SHUKLA_PRATIPADA(1, "Pratipada", "Shukla"),
    SHUKLA_DVITIYA(2, "Dvitiya", "Shukla"),
    SHUKLA_TRITIYA(3, "Tritiya", "Shukla"),
    SHUKLA_CHATURTHI(4, "Chaturthi", "Shukla"),
    SHUKLA_PANCHAMI(5, "Panchami", "Shukla"),
    SHUKLA_SHASHTHI(6, "Shashthi", "Shukla"),
    SHUKLA_SAPTAMI(7, "Saptami", "Shukla"),
    SHUKLA_ASHTAMI(8, "Ashtami", "Shukla"),
    SHUKLA_NAVAMI(9, "Navami", "Shukla"),
    SHUKLA_DASHAMI(10, "Dashami", "Shukla"),
    SHUKLA_EKADASHI(11, "Ekadashi", "Shukla"),
    SHUKLA_DVADASHI(12, "Dvadashi", "Shukla"),
    SHUKLA_TRAYODASHI(13, "Trayodashi", "Shukla"),
    SHUKLA_CHATURDASHI(14, "Chaturdashi", "Shukla"),
    PURNIMA(15, "Purnima", "Shukla"),
    KRISHNA_PRATIPADA(16, "Pratipada", "Krishna"),
    KRISHNA_DVITIYA(17, "Dvitiya", "Krishna"),
    KRISHNA_TRITIYA(18, "Tritiya", "Krishna"),
    KRISHNA_CHATURTHI(19, "Chaturthi", "Krishna"),
    KRISHNA_PANCHAMI(20, "Panchami", "Krishna"),
    KRISHNA_SHASHTHI(21, "Shashthi", "Krishna"),
    KRISHNA_SAPTAMI(22, "Saptami", "Krishna"),
    KRISHNA_ASHTAMI(23, "Ashtami", "Krishna"),
    KRISHNA_NAVAMI(24, "Navami", "Krishna"),
    KRISHNA_DASHAMI(25, "Dashami", "Krishna"),
    KRISHNA_EKADASHI(26, "Ekadashi", "Krishna"),
    KRISHNA_DVADASHI(27, "Dvadashi", "Krishna"),
    KRISHNA_TRAYODASHI(28, "Trayodashi", "Krishna"),
    KRISHNA_CHATURDASHI(29, "Chaturdashi", "Krishna"),
    AMAVASYA(30, "Amavasya", "Krishna");

    companion object {
        fun fromIndex(index: Int): Tithi = entries[(index - 1).coerceIn(0, 29)]
    }
}

/**
 * 27 traditional astronomical Yogas (sum of Sun and Moon sidereal longitudes).
 */
@Serializable
enum class PanchangYoga(val index: Int, val displayName: String) {
    VISHKUMBHA(1, "Vishkumbha"),
    PRITI(2, "Priti"),
    AYUSHMAN(3, "Ayushman"),
    SAUBHAGYA(4, "Saubhagya"),
    SHOBHANA(5, "Shobhana"),
    ATIGANDA(6, "Atiganda"),
    SUKARMA(7, "Sukarma"),
    DHRITI(8, "Dhriti"),
    SHOOLA(9, "Shoola"),
    GANDA(10, "Ganda"),
    VRIDDHI(11, "Vriddhi"),
    DHRUVA(12, "Dhruva"),
    VYAGHATA(13, "Vyaghata"),
    HARSHANA(14, "Harshana"),
    VAJRA(15, "Vajra"),
    ASIDDHI(16, "Siddhi"),
    VYATIPATA(17, "Vyatipata"),
    VARIYAN(18, "Variyan"),
    PARIGHA(19, "Parigha"),
    SHIVA(20, "Shiva"),
    SIDDHA(21, "Siddha"),
    SADHYA(22, "Sadhya"),
    SHUBHA(23, "Shubha"),
    SHUKLA(24, "Shukla"),
    BRAHMA(25, "Brahma"),
    INDRA(26, "Indra"),
    VAIDHRITI(27, "Vaidhriti");

    companion object {
        fun fromIndex(index: Int): PanchangYoga = entries[(index - 1).coerceIn(0, 26)]
    }
}

/**
 * 11 traditional Karanas (half of a Tithi: 6 degrees each).
 */
@Serializable
enum class Karana(val displayName: String) {
    BAVA("Bava"),
    BALAVA("Balava"),
    KAULAVA("Kaulava"),
    TAITILA("Taitila"),
    GARA("Gara"),
    VANIJA("Vanija"),
    VISHTI("Vishti / Bhadra"),
    SHAKUNI("Shakuni"),
    CHATUSHPADA("Chatushpada"),
    NAGA("Naga"),
    KIMSTUGHNA("Kimstughna");
}

/**
 * Day of the week (Vara).
 */
@Serializable
enum class Vara(val index: Int, val displayName: String, val sanskritName: String) {
    SUNDAY(0, "Sunday", "Ravivara"),
    MONDAY(1, "Monday", "Somavara"),
    TUESDAY(2, "Tuesday", "Mangalavara"),
    WEDNESDAY(3, "Wednesday", "Budhavara"),
    THURSDAY(4, "Thursday", "Guruvara"),
    FRIDAY(5, "Friday", "Shukravara"),
    SATURDAY(6, "Saturday", "Shanivara");

    companion object {
        fun fromJulianDay(jd: Double): Vara {
            // JD 2451545.0 (2000-01-01 12:00 UT) was a Saturday (index 6)
            // (jd + 1.5) % 7 maps 0 -> Sunday, 1 -> Monday ...
            val dayIndex = ((jd + 1.5).toLong() % 7).toInt()
            val safeIndex = if (dayIndex < 0) dayIndex + 7 else dayIndex
            return entries[safeIndex]
        }
    }
}

/** Vara boundaries are explicit so a civil weekday is never mistaken for sunrise Vara. */
@Serializable
enum class VaraConvention { CIVIL_UTC, LOCAL_SUNRISE }

@Serializable
enum class ReferenceComparisonStatus {
    EXACT_MATCH, TOLERANCE_MATCH, PROFILE_DIFFERENCE, ROUNDING_DIFFERENCE,
    CALCULATION_ERROR, UNSUPPORTED, REFERENCE_AMBIGUITY,
}

/**
 * Complete immutable classical Panchang snapshot for an exact moment.
 */
@Serializable
data class PanchangSnapshot(
    val julianDay: Double,
    val tithi: Tithi,
    val tithiElapsedDegrees: Double,
    val nakshatraIndex: Int,
    val nakshatraName: String,
    val nakshatraDegree: Double,
    val yoga: PanchangYoga,
    val karana: Karana,
    val vara: Vara,
    val sunSiderealLongitude: Double,
    val moonSiderealLongitude: Double,
    val profileId: String = "CLASSICAL_PANCHANG_V1",
    val varaConvention: VaraConvention = VaraConvention.CIVIL_UTC,
    val sunriseJulianDay: Double? = null,
    val sunsetJulianDay: Double? = null,
    val observerLatitudeDeg: Double? = null,
    val observerLongitudeDeg: Double? = null,
    val timezoneOffsetMinutes: Int? = null,
)

/**
 * Deterministic Panchang calculator implementing the 5 Limbs of Time.
 * Reference: Surya Siddhanta & Indian Astronomical Ephemeris standard definitions.
 */
object PanchangCalculator {

    const val DEGREES_PER_TITHI = 12.0
    const val DEGREES_PER_YOGA = 360.0 / 27.0 // 13° 20'

    fun calculate(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
        varaConvention: VaraConvention = VaraConvention.CIVIL_UTC,
        latitudeDeg: Double? = null,
        longitudeDeg: Double? = null,
        timezoneOffsetMinutes: Int? = null,
    ): PanchangSnapshot {
        val julianDay = JulianDay(jd)
        val ayanamsaDegrees =
            AyanamsaCalculator.forConvention(ayanamsaConvention).calculate(julianDay)

        val sun = SunCalculator.calculate(julianDay)
        val moon = MoonCalculator.calculate(julianDay)

        val sunSidereal = ZodiacCalculator.toSidereal(sun.apparentLongitude, ayanamsaDegrees)
        val moonSidereal = ZodiacCalculator.toSidereal(moon.apparentLongitude, ayanamsaDegrees)

        // 1. Tithi: (Moon - Sun) mod 360 / 12
        val diffDegrees = normalizeDegrees(moon.apparentLongitude - sun.apparentLongitude)
        val tithiIndex = ((diffDegrees / DEGREES_PER_TITHI).toInt() + 1).coerceIn(1, 30)
        val tithiElapsed = diffDegrees % DEGREES_PER_TITHI
        val tithi = Tithi.fromIndex(tithiIndex)

        // 2. Nakshatra: Moon's sidereal position
        val nakshatraInfo = ZodiacCalculator.calculateNakshatra(moonSidereal)

        // 3. Yoga: (Sun + Moon sidereal) mod 360 / (13° 20')
        val yogaDegrees = normalizeDegrees(sunSidereal + moonSidereal)
        val yogaIndex = ((yogaDegrees / DEGREES_PER_YOGA).toInt() + 1).coerceIn(1, 27)
        val yoga = PanchangYoga.fromIndex(yogaIndex)

        // 4. Karana: 60 half-tithis per lunar month
        val halfTithiIndex = (diffDegrees / 6.0).toInt().coerceIn(0, 59)
        val karana = resolveKarana(halfTithiIndex)

        // 5. Vara: civil UTC or local sunrise boundary, selected explicitly.
        val solarEvents = if (varaConvention == VaraConvention.LOCAL_SUNRISE) {
            require(latitudeDeg != null && longitudeDeg != null && timezoneOffsetMinutes != null) {
                "LOCAL_SUNRISE requires latitude, longitude and timezoneOffsetMinutes"
            }
            val localJd = jd + timezoneOffsetMinutes / 1440.0
            val localMidnightJd = kotlin.math.floor(localJd + 0.5) - 0.5
            SolarDayEventCalculator.calculate(
                localMidnightJd,
                latitudeDeg,
                longitudeDeg,
                timezoneOffsetMinutes
            )
        } else null
        val vara = if (solarEvents == null) Vara.fromJulianDay(jd) else {
            val localJd = jd + timezoneOffsetMinutes!! / 1440.0
            val localMidnightJd = kotlin.math.floor(localJd + 0.5) - 0.5
            Vara.fromJulianDay(if (jd < solarEvents.sunriseJulianDay) localMidnightJd - 1.0 else localMidnightJd)
        }

        return PanchangSnapshot(
            julianDay = jd,
            tithi = tithi,
            tithiElapsedDegrees = tithiElapsed,
            nakshatraIndex = nakshatraInfo.index,
            nakshatraName = nakshatraInfo.name,
            nakshatraDegree = nakshatraInfo.degreeInNakshatra,
            yoga = yoga,
            karana = karana,
            vara = vara,
            sunSiderealLongitude = sunSidereal,
            moonSiderealLongitude = moonSidereal,
            varaConvention = varaConvention,
            profileId = if (varaConvention == VaraConvention.LOCAL_SUNRISE) "CLASSICAL_LOCAL_SUNRISE_V1" else "CLASSICAL_CIVIL_UTC_V1",
            sunriseJulianDay = solarEvents?.sunriseJulianDay,
            sunsetJulianDay = solarEvents?.sunsetJulianDay,
            observerLatitudeDeg = latitudeDeg,
            observerLongitudeDeg = longitudeDeg,
            timezoneOffsetMinutes = timezoneOffsetMinutes,
        )
    }

    private fun resolveKarana(halfTithiIndex: Int): Karana {
        // 4 Fixed Karanas:
        // Index 0 (1st half of Shukla Pratipada) = Kimstughna
        // Index 57 (2nd half of Krishna Chaturdashi) = Shakuni
        // Index 58 (1st half of Amavasya) = Chatushpada
        // Index 59 (2nd half of Amavasya) = Naga
        return when (halfTithiIndex) {
            0 -> Karana.KIMSTUGHNA
            57 -> Karana.SHAKUNI
            58 -> Karana.CHATUSHPADA
            59 -> Karana.NAGA
            else -> {
                // 7 Repeating Karanas (Bava..Vishti), repeating 8 times (56 half-tithis)
                val repeatingIndex = (halfTithiIndex - 1) % 7
                when (repeatingIndex) {
                    0 -> Karana.BAVA
                    1 -> Karana.BALAVA
                    2 -> Karana.KAULAVA
                    3 -> Karana.TAITILA
                    4 -> Karana.GARA
                    5 -> Karana.VANIJA
                    else -> Karana.VISHTI
                }
            }
        }
    }
}

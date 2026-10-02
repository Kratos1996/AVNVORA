package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.models.Rashi
import kotlinx.serialization.Serializable

/** Graha named by the 1907 source's sign-lord rule. */
@Serializable
enum class MunthaLord { SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN }

@Serializable
data class MunthaCalculation(
    val elapsedSolarReturnCycles: Int,
    val natalAscendantLongitude: Double,
    val sign: Rashi,
    val longitude: Double,
    val lord: MunthaLord,
    val annualHouse: Int?,
    val sourceRef: String = "Tājika Nīlakaṇṭhī (1907), Varsha tantra, Muntha chapter, verses 1 and 3; PDF pp.120–121 (printed pp.112–113)",
    val status: String = "CALCULATED_PRIMARY_SOURCE_VERIFIED",
)

@Serializable
data class MunthaLordPlanetState(
    val houseNumber: Int,
    val sign: Rashi,
    val longitude: Double,
    val retrograde: Boolean,
    val dignity: String?,
    val state: String?,
)

@Serializable
data class MunthaLordResult(
    val munthaSign: Rashi,
    val lord: MunthaLord,
    val annualSign: Rashi?,
    val annualHouse: Int?,
    val planetState: MunthaLordPlanetState?,
    val ruleRefs: List<String>,
    val provenance: List<String>,
)

/** Implements the source's one-sign-per-elapsed-year Muntha progression and sign-lord definition. */
object MunthaEngine {
    fun calculate(
        natalAscendantLongitude: Double,
        elapsedSolarReturnCycles: Int,
        annualAscendantSign: Rashi? = null,
    ): MunthaCalculation {
        require(natalAscendantLongitude.isFinite() && natalAscendantLongitude >= 0.0 && natalAscendantLongitude < 360.0) {
            "Natal ascendant longitude must be in [0, 360)."
        }
        require(elapsedSolarReturnCycles >= 0) { "Elapsed solar-return cycles cannot be negative." }
        val natalSign = (natalAscendantLongitude / 30.0).toInt()
        val sign = Rashi.fromIndex((natalSign + elapsedSolarReturnCycles) % 12)
        val longitude = sign.index * 30.0 + natalAscendantLongitude % 30.0
        val house = annualAscendantSign?.let { ((sign.index - it.index + 12) % 12) + 1 }
        return MunthaCalculation(
            elapsedSolarReturnCycles = elapsedSolarReturnCycles,
            natalAscendantLongitude = natalAscendantLongitude,
            sign = sign,
            longitude = longitude,
            lord = lordOf(sign),
            annualHouse = house,
        )
    }

    fun lordOf(sign: Rashi): MunthaLord = when (sign) {
        Rashi.ARIES, Rashi.SCORPIO -> MunthaLord.MARS
        Rashi.TAURUS, Rashi.LIBRA -> MunthaLord.VENUS
        Rashi.GEMINI, Rashi.VIRGO -> MunthaLord.MERCURY
        Rashi.CANCER -> MunthaLord.MOON
        Rashi.LEO -> MunthaLord.SUN
        Rashi.SAGITTARIUS, Rashi.PISCES -> MunthaLord.JUPITER
        Rashi.CAPRICORN, Rashi.AQUARIUS -> MunthaLord.SATURN
    }
}

/** Combines the verified Muntha sign-lord rule with observable facts from the annual chart. */
object MunthaLordEngine {
    fun calculate(muntha: MunthaCalculation, annualChart: com.aynvora.core.models.AstroChart?): MunthaLordResult {
        val lordId = muntha.lord.name
        val placement = annualChart?.houses?.asSequence()?.flatMap { house ->
            house.planets.asSequence().filter { it.planetId.equals(lordId, ignoreCase = true) }
                .map { house to it }
        }?.firstOrNull()
        val (house, planet) = placement ?: (null to null)
        val state = if (house != null && planet != null) MunthaLordPlanetState(
            houseNumber = house.houseNumber,
            sign = planet.sign,
            longitude = planet.longitude,
            retrograde = planet.retrograde,
            dignity = planet.dignity,
            state = planet.state,
        ) else null
        return MunthaLordResult(
            munthaSign = muntha.sign,
            lord = muntha.lord,
            annualSign = annualChart?.ascendant?.sign,
            annualHouse = muntha.annualHouse,
            planetState = state,
            ruleRefs = listOf(muntha.sourceRef),
            provenance = listOf("Muntha sign and lord calculated from the primary-source progression and sign-lord rule.") +
                listOfNotNull(if (annualChart == null) "Annual chart unavailable; lord placement state not calculated." else if (state == null) "Muntha lord placement was not present in the annual chart model." else "Lord placement state taken from the calculated annual AstroChart."),
        )
    }
}

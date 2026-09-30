package com.aynvora.core.models

import kotlinx.serialization.Serializable

/** Headless, house-owned chart contract. A house is the sole owner of its placements. */
@Serializable
data class AstroChart(
    val chartId: String,
    val chartType: String,
    val titleKey: String,
    val zodiacMode: String,
    val houseSystem: String,
    val ascendant: AstroChartAscendant?,
    val houses: List<AstroChartHouseData>,
    val metadata: CalculationMetadata,
    val provenance: String,
    val status: CalculationAvailability,
)

@Serializable
data class AstroChartAscendant(val sign: Rashi, val longitude: Double? = null)

@Serializable
data class AstroChartHouseData(
    val houseNumber: Int,
    val sign: Rashi,
    val signIndex: Int = sign.index,
    val cusp: Double? = null,
    val houseStart: Double? = null,
    val houseMiddle: Double? = null,
    val houseEnd: Double? = null,
    val planets: List<PlanetInHouse> = emptyList(),
    val ascendant: Boolean = false,
    val annotations: List<String> = emptyList(),
    val markers: List<String> = emptyList(),
    val status: CalculationAvailability = CalculationAvailability.AVAILABLE,
)

@Serializable
data class PlanetInHouse(
    val planetId: String,
    val displayKey: String,
    val degreeInSign: Double,
    val degrees: Int,
    val longitude: Double,
    val minutes: Int,
    val seconds: Double,
    val nakshatra: String?,
    val pada: Int?,
    val retrograde: Boolean,
    val combust: Boolean,
    val exalted: Boolean?,
    val debilitated: Boolean?,
    val vargottama: Boolean?,
    val dignity: String?,
    val state: String?,
    val houseNumber: Int,
    val sign: Rashi,
    val evidence: String? = null,
    val provenance: String,
)

@Serializable
data class AstroChartDiagnostic(val code: String, val message: String, val houseNumber: Int? = null, val planetId: String? = null)

sealed interface AstroChartBuildResult {
    data class Valid(val chart: AstroChart) : AstroChartBuildResult
    data class Invalid(val code: String = "CHART_DATA_INVALID", val diagnostics: List<AstroChartDiagnostic>) : AstroChartBuildResult
}

/** Assigns placements to house objects once and rejects incomplete or ambiguous source data. */
object AstroChartBuilder {
    fun fromSnapshot(snapshot: KundaliSnapshot, chartId: String): AstroChartBuildResult {
        val source = snapshot.charts.firstOrNull { it.chartId == chartId }
            ?: return AstroChartBuildResult.Invalid(diagnostics = listOf(AstroChartDiagnostic("CHART_NOT_FOUND", "Chart $chartId is not present in the Kundali snapshot")))
        return fromSnapshot(source)
    }

    fun fromSnapshot(source: AstroChartSnapshot): AstroChartBuildResult {
        val issues = mutableListOf<AstroChartDiagnostic>()
        if (source.status == CalculationAvailability.UNSUPPORTED || source.status == CalculationAvailability.COMING_SOON) {
            return AstroChartBuildResult.Invalid(diagnostics = listOf(AstroChartDiagnostic("UNSUPPORTED", "Chart ${source.chartId} has no calculated data")))
        }
        val houseNumbers = source.houses.map { it.houseNumber }
        if (houseNumbers.size != 12 || houseNumbers.toSet() != (1..12).toSet()) {
            issues += AstroChartDiagnostic("INVALID_HOUSES", "Expected exactly one house numbered 1 through 12")
        }
        source.houses.forEach { house ->
            if (house.signIndex !in 0..11) issues += AstroChartDiagnostic("INVALID_SIGN", "Invalid sign index ${house.signIndex}", house.houseNumber)
        }
        val duplicateIds = source.placements.groupBy { it.bodyId }.filterValues { it.size > 1 }.keys
        duplicateIds.forEach { id -> issues += AstroChartDiagnostic("DUPLICATE_PLANET", "Planet $id occurs more than once", planetId = id) }
        source.placements.forEach { p ->
            if (p.houseNumber !in 1..12) issues += AstroChartDiagnostic("ORPHAN_PLANET", "Planet ${p.bodyId} has no valid house", planetId = p.bodyId)
            if (p.signIndex !in 0..11 || !p.degreeInSign.isFinite() || p.degreeInSign !in 0.0..<30.0 || !p.longitude.isFinite()) {
                issues += AstroChartDiagnostic("INVALID_PLANET_DATA", "Planet ${p.bodyId} has invalid sign or degree", p.houseNumber, p.bodyId)
            }
        }
        if (issues.isNotEmpty()) return AstroChartBuildResult.Invalid(diagnostics = issues)
        val grouped = source.placements.groupBy { it.houseNumber!! }
        val houses = source.houses.sortedBy { it.houseNumber }.map { h ->
            val sign = Rashi.fromIndex(h.signIndex!!)
            val planets = grouped[h.houseNumber].orEmpty().sortedBy { it.bodyId }.map { p ->
                val whole = p.degreeInSign.toInt()
                val minuteRaw = (p.degreeInSign - whole) * 60.0
                val minute = minuteRaw.toInt()
                PlanetInHouse(
                    planetId = p.bodyId, displayKey = "astrology.body.${p.bodyId.lowercase()}",
                    degreeInSign = p.degreeInSign, degrees = whole, longitude = p.longitude,
                    minutes = minute, seconds = (minuteRaw - minute) * 60.0,
                    nakshatra = p.nakshatraId, pada = p.pada, retrograde = p.retrograde,
                    combust = p.combust, exalted = p.exalted, debilitated = p.debilitated,
                    vargottama = p.vargottama, dignity = p.dignityId, state = null,
                    houseNumber = h.houseNumber, sign = Rashi.fromIndex(p.signIndex),
                    provenance = p.sourceId,
                )
            }
            AstroChartHouseData(
                houseNumber = h.houseNumber, sign = sign, cusp = h.cuspLongitude,
                houseStart = h.startLongitude, houseMiddle = h.cuspLongitude, houseEnd = h.endLongitude,
                planets = planets, ascendant = h.houseNumber == 1 && source.ascendantSignIndex == h.signIndex,
            )
        }
        val chart = AstroChart(
            source.chartId, source.chartTypeId, source.titleKey, source.zodiacModeId,
            source.houseSystemId, source.ascendantSignIndex?.let { idx -> AstroChartAscendant(Rashi.fromIndex(idx)) },
            houses, source.sourceMetadata, source.sourceMetadata.ephemerisSourceId, source.status,
        )
        val validation = AstroChartValidator.validate(chart)
        return if (validation.isEmpty()) AstroChartBuildResult.Valid(chart)
        else AstroChartBuildResult.Invalid(diagnostics = validation)
    }
}

object AstroChartValidator {
    fun validate(chart: AstroChart): List<AstroChartDiagnostic> = buildList {
        val numbers = chart.houses.map { it.houseNumber }
        if (numbers.size != 12 || numbers.toSet() != (1..12).toSet()) {
            add(AstroChartDiagnostic("INVALID_HOUSES", "Expected exactly one house numbered 1 through 12"))
        }
        val ids = mutableSetOf<String>()
        chart.houses.forEach { house ->
            if (house.signIndex !in 0..11 || house.signIndex != house.sign.index) {
                add(AstroChartDiagnostic("INVALID_SIGN", "House ${house.houseNumber} has inconsistent sign data", house.houseNumber))
            }
            house.planets.forEach { planet ->
                if (planet.houseNumber != house.houseNumber) add(AstroChartDiagnostic("PLANET_HOUSE_MISMATCH", "${planet.planetId} is stored in a different house than it claims", house.houseNumber, planet.planetId))
                if (planet.planetId in ids) add(AstroChartDiagnostic("DUPLICATE_PLANET", "${planet.planetId} occurs more than once", house.houseNumber, planet.planetId))
                ids += planet.planetId
                if (planet.degreeInSign !in 0.0..<30.0 || !planet.degreeInSign.isFinite() || !planet.longitude.isFinite()) {
                    add(AstroChartDiagnostic("INVALID_PLANET_DATA", "${planet.planetId} has an invalid degree or longitude", house.houseNumber, planet.planetId))
                }
                if (planet.degrees !in 0..29 || planet.minutes !in 0..59 || !planet.seconds.isFinite() || planet.seconds !in 0.0..<60.0) {
                    add(AstroChartDiagnostic("INVALID_DMS", "${planet.planetId} has invalid degree components", house.houseNumber, planet.planetId))
                }
            }
        }
        if (chart.ascendant != null && chart.houses.none { it.houseNumber == 1 && it.sign == chart.ascendant.sign }) {
            add(AstroChartDiagnostic("ASCENDANT_MISMATCH", "Ascendant sign does not match House 1", 1))
        }
    }
}

object AstroChartJson {
    private val json = kotlinx.serialization.json.Json { encodeDefaults = true; explicitNulls = true; ignoreUnknownKeys = false }
    fun encode(chart: AstroChart): String = json.encodeToString(AstroChart.serializer(), chart)
    fun decode(payload: String): AstroChart = json.decodeFromString(AstroChart.serializer(), payload)
}

package com.aynvora.core.models

import com.aynvora.astro.time.TimeNormalizer
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.json.Json

/** Stable JSON boundary. Data-class and list declaration order is part of schema v1. */
object KundaliSnapshotJson {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = false
        prettyPrint = false
    }

    fun encode(snapshot: KundaliSnapshot): String = json.encodeToString(KundaliSnapshot.serializer(), snapshot)
    fun decode(payload: String): KundaliSnapshot = json.decodeFromString(KundaliSnapshot.serializer(), payload)
}

/** Catalog entries are metadata only; a section is available only when snapshot data backs it. */
object AstrologySectionCatalog {
    val sectionIds: List<String> = listOf(
        "home", "lagna", "navamsha", "chandra", "chalit", "dasha", "phaladesh", "kp",
        "shodashavarga", "lal_kitab", "varshaphal", "cloud", "graha_sthiti",
        "graha_sthiti_all", "chalit_table", "janam_vivaran", "panchang", "ashtakavarga",
        "karakansha", "swansha", "gochar", "shadbala", "pindabala", "prastara_ashtakavarga",
        "shodhita_ashtakavarga", "avakahada", "personal_details", "pdf_report", "ask_question",
        "relationships", "yogas", "reports",
    )

    fun forSnapshot(snapshot: KundaliSnapshot): List<AstrologySectionAvailability> {
        val natal = snapshot.natalChart
        val hasD9 = snapshot.charts.any { it.chartId == "D9" && it.status == CalculationAvailability.AVAILABLE }
        val hasMoon = natal.planetaryPositions.any { it.body == CelestialBody.MOON }
        val hasAv = natal.ashtakavarga != null
        val hasShodhana = natal.shodhitaAshtakavarga != null
        val hasPinda = natal.ashtakavargaPinda != null
        val hasStrength = natal.shadbala.isNotEmpty()
        val hasVargas = snapshot.charts.any { it.chartId.startsWith("D") && it.chartId != "D1" && it.status != CalculationAvailability.UNSUPPORTED }
        val states = natal.planetStates.isNotEmpty()
        val statuses = mapOf(
            "home" to CalculationAvailability.AVAILABLE,
            "dasha" to (if (snapshot.dasha != null) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "phaladesh" to CalculationAvailability.UNSUPPORTED,
            "kp" to CalculationAvailability.UNSUPPORTED,
            "shodashavarga" to (if (hasVargas) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "lal_kitab" to CalculationAvailability.UNSUPPORTED,
            "varshaphal" to CalculationAvailability.UNSUPPORTED,
            "lagna" to (if (natal.lagna != null) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "navamsha" to (if (hasD9) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "cloud" to CalculationAvailability.UNSUPPORTED,
            "chandra" to (if (hasMoon) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "chalit" to (if (natal.houses.size == 12) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL),
            "graha_sthiti" to (if (natal.planetaryPositions.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "graha_sthiti_all" to (if (states) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL),
            "chalit_table" to (if (natal.houses.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "janam_vivaran" to CalculationAvailability.AVAILABLE,
            "panchang" to (if (snapshot.panchang != null) CalculationAvailability.PARTIAL else CalculationAvailability.UNSUPPORTED),
            "ashtakavarga" to (if (hasAv) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "karakansha" to CalculationAvailability.UNSUPPORTED,
            "swansha" to CalculationAvailability.UNSUPPORTED,
            "gochar" to (if (snapshot.transitAtBirth != null) CalculationAvailability.PARTIAL else CalculationAvailability.UNSUPPORTED),
            "shadbala" to (if (hasStrength) CalculationAvailability.PARTIAL else CalculationAvailability.UNSUPPORTED),
            "pindabala" to (if (hasPinda) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "prastara_ashtakavarga" to (if (hasAv) CalculationAvailability.PARTIAL else CalculationAvailability.UNSUPPORTED),
            "shodhita_ashtakavarga" to (if (hasShodhana) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "avakahada" to (if (hasMoon && natal.lagna != null) CalculationAvailability.PARTIAL else CalculationAvailability.UNSUPPORTED),
            "personal_details" to CalculationAvailability.AVAILABLE,
            "pdf_report" to CalculationAvailability.PARTIAL,
            "ask_question" to CalculationAvailability.UNSUPPORTED,
            "relationships" to (if (natal.aspects.isNotEmpty() || natal.planetaryRelationships.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "yogas" to CalculationAvailability.UNSUPPORTED,
            "reports" to CalculationAvailability.PARTIAL,
        )
        return sectionIds.map { id ->
            AstrologySectionAvailability(id, "astrology.section.$id", statuses[id] ?: CalculationAvailability.COMING_SOON, sectionDataRef(id, statuses[id]))
        }
    }

    private fun sectionDataRef(id: String, status: CalculationAvailability?): String? = when (status) {
        CalculationAvailability.AVAILABLE, CalculationAvailability.PARTIAL -> when (id) {
            "dasha" -> "dasha"
            "panchang" -> "panchang"
            "gochar" -> "transitAtBirth"
            "ashtakavarga", "prastara_ashtakavarga" -> "natalChart.ashtakavarga"
            "pindabala" -> "natalChart.ashtakavargaPinda"
            "shadbala" -> "natalChart.shadbala"
            "navamsha", "shodashavarga" -> "charts"
            "chalit", "chalit_table" -> "natalChart.houses"
            "janam_vivaran" -> "birth"
            "graha_sthiti" -> "natalChart.planetaryPositions"
            "graha_sthiti_all" -> "natalChart.planetStates"
            "relationships" -> "natalChart.aspects"
            "shodhita_ashtakavarga" -> "natalChart.shodhitaAshtakavarga"
            "avakahada" -> "birth"
            else -> "natalChart"
        }
        else -> null
    }
}

/** Build all screen data from one chart result; this adapter calls existing SDK/engine operations only. */
suspend fun AynvoraSdk.generateKundaliSnapshot(
    request: ChartRequest,
    profileId: String,
    profileName: String,
    genderId: String? = null,
): AynvoraResult<KundaliSnapshot> {
    require(profileId.isNotBlank())
    require(profileName.isNotBlank())
    return when (val calculated = calculateChart(request)) {
        is AynvoraResult.Failure -> calculated
        is AynvoraResult.Success -> {
            val chart = calculated.value
            val moon = chart.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }
            val dasha = moon?.let { calculateDasha(chart.julianDay, it.siderealLongitude, true, true) }
            val transit = runCatching { calculateTransit(chart.julianDay, request.config.ayanamsa.name) }.getOrNull()
            val panchang = runCatching { calculatePanchang(chart.julianDay, request.config.ayanamsa.name) }.getOrNull()
            val birth = request.birthData
            val local = TimeNormalizer.normalize(
                birth.date.year, birth.date.month, birth.date.day,
                birth.time.hour, birth.time.minute, birth.time.second,
                birth.place.timezoneId,
            )
            val snapshot = KundaliSnapshot(
                profileId = profileId,
                profileName = profileName,
                genderId = genderId,
                birth = KundaliBirthDetails(
                    profileId = profileId,
                    profileName = profileName,
                    genderId = genderId,
                    localDate = birth.date.toIsoDateString(),
                    localTime = birth.time.toIsoTimeString(),
                    timezoneId = birth.place.timezoneId,
                    timezoneOffsetMinutes = local.timezoneOffsetMinutes,
                    utcTimestamp = "%04d-%02d-%02dT%02d:%02d:%02dZ".format(local.year, local.month, local.day, local.hour, local.minute, local.second.toInt()),
                    julianDay = chart.julianDay,
                    latitude = birth.place.coordinates.latitude,
                    longitude = birth.place.coordinates.longitude,
                    country = birth.place.country,
                    state = birth.place.stateName,
                    city = birth.place.cityName ?: birth.place.name,
                    ayanamsaId = request.config.ayanamsa.name,
                    houseSystemId = request.config.houseSystem.name,
                    calculationProfileId = request.config.profile.name,
                    nodeConventionId = chart.calculationMetadata.conventions["node_profile"] ?: "UNSPECIFIED",
                ),
                calculation = chart.calculationMetadata,
                natalChart = chart,
                charts = buildCharts(chart),
                dasha = dasha,
                transitAtBirth = transit,
                panchang = panchang,
                tables = buildTables(chart),
                availability = emptyList(),
            ).let { it.copy(availability = AstrologySectionCatalog.forSnapshot(it)) }
            AynvoraResult.Success(snapshot, calculated.metadata, chart.calculationMetadata)
        }
    }
}

private fun buildCharts(chart: ChartResult): List<AstroChartSnapshot> {
    val dignity = chart.planetaryDignities.filter { it.chart == DivisionalChart.D1 }.associateBy { it.body }
    val natalPlacements = chart.planetaryPositions.sortedBy { it.body.ordinal }.map { p ->
        val d = dignity[p.body]
        AstroChartPlacement(
            bodyId = p.body.name, houseNumber = p.houseNumber,
            signIndex = p.rashiPosition.rashi.index, longitude = p.siderealLongitude,
            degreeInSign = p.rashiPosition.degreeInSign,
            nakshatraId = p.nakshatraPosition.nakshatra.name,
            nakshatraIndex = p.nakshatraPosition.nakshatra.index,
            pada = p.nakshatraPosition.pada,
            retrograde = p.isRetrograde, combust = p.combustionState == CombustionState.COMBUST,
            exalted = d?.isExalted, debilitated = d?.isDebilitated,
            // The current engine does not expose a Vargottama evaluation, so preserve as unknown.
            vargottama = null,
            dignityId = d?.dignityType?.name,
            sourceId = chart.calculationMetadata.ephemerisSourceId,
        )
    }
    val houses = chart.houses.sortedBy { it.houseNumber }.map {
        AstroChartHouse(it.houseNumber, it.rashiPosition.rashi.index, it.startLongitude, it.cuspLongitude, it.endLongitude)
    }
    val base = AstroChartSnapshot(
        chartId = "D1", chartTypeId = "RASHI", titleKey = "astrology.chart.d1",
        zodiacModeId = "SIDEREAL", houseSystemId = chart.config.houseSystem.name,
        status = if (chart.lagna != null && houses.size == 12) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL,
        ascendantSignIndex = chart.lagna?.rashiPosition?.rashi?.index,
        houses = houses, placements = natalPlacements, sourceMetadata = chart.calculationMetadata,
    )
    val vargas = chart.divisionalCharts.entries.filter { it.key != DivisionalChart.D1 }.sortedBy { it.key.divisionNumber }.map { (type, result) ->
        val placements = result.positions.filter { !it.isLagna && it.body != null }.sortedBy { it.body!!.ordinal }.map { p ->
            val body = p.body!!
            AstroChartPlacement(
                bodyId = body.name, houseNumber = result.lagnaPosition?.let { ((p.resultingRashi.index - it.resultingRashi.index + 12) % 12) + 1 },
                signIndex = p.resultingRashi.index, longitude = p.resultingLongitude,
                degreeInSign = p.degreeInResultingRashi, nakshatraId = null, nakshatraIndex = null,
                pada = null, retrograde = false, combust = false,
                sourceId = chart.calculationMetadata.ephemerisSourceId,
            )
        }
        AstroChartSnapshot(
            chartId = "D${type.divisionNumber}", chartTypeId = type.name,
            titleKey = "astrology.chart.d${type.divisionNumber}", zodiacModeId = "SIDEREAL",
            houseSystemId = chart.config.houseSystem.name,
            status = if (result.isSupported) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED,
            ascendantSignIndex = result.lagnaPosition?.resultingRashi?.index,
            houses = (1..12).map { n -> AstroChartHouse(n, result.lagnaPosition?.resultingRashi?.index?.let { (it + n - 1) % 12 }) },
            placements = placements, sourceMetadata = chart.calculationMetadata,
        )
    }
    val moonSign = chart.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }?.rashiPosition?.rashi?.index
    val moonChart = if (moonSign != null) base.copy(
        chartId = "MOON", chartTypeId = "MOON_REFERENCE", titleKey = "astrology.chart.moon",
        ascendantSignIndex = moonSign,
        houses = (1..12).map { n -> AstroChartHouse(n, (moonSign + n - 1) % 12) },
        placements = base.placements.map { p -> p.copy(houseNumber = ((p.signIndex - moonSign + 12) % 12) + 1) },
    ) else null
    return listOfNotNull(base, moonChart) + vargas
}

private fun buildTables(chart: ChartResult): List<AstroTableSnapshot> {
    val planetColumns = listOf(
        AstroTableColumn("body", "astrology.column.body", AstroValueType.BODY),
        AstroTableColumn("sign", "astrology.column.sign", AstroValueType.SIGN),
        AstroTableColumn("longitude", "astrology.column.longitude", AstroValueType.ANGLE),
        AstroTableColumn("house", "astrology.column.house", AstroValueType.INTEGER),
        AstroTableColumn("nakshatra", "astrology.column.nakshatra", AstroValueType.NAKSHATRA),
        AstroTableColumn("pada", "astrology.column.pada", AstroValueType.INTEGER),
        AstroTableColumn("retrograde", "astrology.column.retrograde", AstroValueType.BOOLEAN),
        AstroTableColumn("combust", "astrology.column.combust", AstroValueType.BOOLEAN),
    )
    val planets = AstroTableSnapshot("graha_sthiti", "astrology.section.graha_sthiti", planetColumns,
        chart.planetaryPositions.sortedBy { it.body.ordinal }.map { p -> AstroTableRow(p.body.name, listOf(
            AstroTableCell(AstroValueType.BODY, canonicalId = p.body.name),
            AstroTableCell(AstroValueType.SIGN, canonicalId = p.rashiPosition.rashi.name),
            AstroTableCell(AstroValueType.ANGLE, numericValue = p.siderealLongitude),
            AstroTableCell(AstroValueType.INTEGER, integerValue = p.houseNumber),
            AstroTableCell(AstroValueType.NAKSHATRA, canonicalId = p.nakshatraPosition.nakshatra.name),
            AstroTableCell(AstroValueType.INTEGER, integerValue = p.nakshatraPosition.pada),
            AstroTableCell(AstroValueType.BOOLEAN, canonicalId = p.isRetrograde.toString()),
            AstroTableCell(AstroValueType.BOOLEAN, canonicalId = (p.combustionState == CombustionState.COMBUST).toString()),
        )) }, if (chart.planetaryPositions.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED)
    val houseColumns = listOf(
        AstroTableColumn("house", "astrology.column.house", AstroValueType.INTEGER),
        AstroTableColumn("sign", "astrology.column.sign", AstroValueType.SIGN),
        AstroTableColumn("start", "astrology.column.start", AstroValueType.ANGLE),
        AstroTableColumn("cusp", "astrology.column.cusp", AstroValueType.ANGLE),
        AstroTableColumn("end", "astrology.column.end", AstroValueType.ANGLE),
    )
    val houses = AstroTableSnapshot("chalit_table", "astrology.section.chalit_table", houseColumns,
        chart.houses.sortedBy { it.houseNumber }.map { h -> AstroTableRow("house_${h.houseNumber}", listOf(
            AstroTableCell(AstroValueType.INTEGER, integerValue = h.houseNumber),
            AstroTableCell(AstroValueType.SIGN, canonicalId = h.rashiPosition.rashi.name),
            AstroTableCell(AstroValueType.ANGLE, numericValue = h.startLongitude),
            AstroTableCell(AstroValueType.ANGLE, numericValue = h.cuspLongitude),
            AstroTableCell(AstroValueType.ANGLE, numericValue = h.endLongitude),
        )) }, if (chart.houses.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED)
    return listOf(planets, houses)
}

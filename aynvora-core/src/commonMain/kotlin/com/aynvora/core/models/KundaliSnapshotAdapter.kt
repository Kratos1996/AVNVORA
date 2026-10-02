package com.aynvora.core.models

import com.aynvora.core.AynvoraSdk
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Stable JSON boundary. Data-class and list declaration order is part of schema v1. */
object KundaliSnapshotJson {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = false
        prettyPrint = false
    }

    fun encode(snapshot: KundaliSnapshot): String = json.encodeToString(KundaliSnapshot.serializer(), snapshot)
    fun decode(payload: String): KundaliSnapshot {
        val root = Json.parseToJsonElement(payload).jsonObject
        val version = root["schemaVersion"]?.jsonPrimitive?.content
        return when (version) {
            KundaliSnapshot.CURRENT_SCHEMA_VERSION -> json.decodeFromJsonElement(KundaliSnapshot.serializer(), root)
            "1" -> {
                // Schema 2 only adds optional event/evidence fields, so v1 snapshots migrate losslessly.
                val migrated = JsonObject(root + ("schemaVersion" to JsonPrimitive(KundaliSnapshot.CURRENT_SCHEMA_VERSION)))
                json.decodeFromJsonElement(KundaliSnapshot.serializer(), migrated)
            }
            else -> error("Unsupported Kundali snapshot schema '$version'.")
        }
    }
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
            "chalit" to (when (snapshot.featureResults["chalit"]?.status) {
                AstroFeatureStatus.AMBIGUOUS -> CalculationAvailability.AMBIGUOUS
                AstroFeatureStatus.UNSUPPORTED, AstroFeatureStatus.FAILED -> CalculationAvailability.UNSUPPORTED
                AstroFeatureStatus.SUPPORTED -> if (natal.houses.size == 12) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL
                AstroFeatureStatus.PARTIAL -> CalculationAvailability.PARTIAL
                // Old snapshots without a Chalit feature record must not inherit the natal chart as a substitute.
                null, AstroFeatureStatus.NOT_VERIFIED -> CalculationAvailability.AMBIGUOUS
            }),
            "graha_sthiti" to (if (natal.planetaryPositions.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED),
            "graha_sthiti_all" to (if (states) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL),
            "chalit_table" to (when (snapshot.featureResults["chalit"]?.status) {
                AstroFeatureStatus.SUPPORTED -> if (natal.houses.size == 12) CalculationAvailability.AVAILABLE else CalculationAvailability.PARTIAL
                AstroFeatureStatus.UNSUPPORTED, AstroFeatureStatus.FAILED -> CalculationAvailability.UNSUPPORTED
                else -> CalculationAvailability.AMBIGUOUS
            }),
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
            // Execution timings are diagnostic data, not stable chart content. Persisting them
            // makes otherwise identical snapshots and their JSON change from run to run.
            val snapshotChart = chart.copy(executionTrace = null)
            val dasha = chart.dashaTimeline
            val transit = runCatching { calculateTransit(chart.julianDay, request.config.ayanamsa.name) }.getOrNull()
            val panchang = runCatching { calculatePanchang(chart.julianDay, request.config.ayanamsa.name) }.getOrNull()
            val birth = request.birthData
            val utcTimestamp = chart.utcTimestamp
                ?: return AynvoraResult.Failure.CalculationFailure("MISSING_NORMALIZED_INSTANT", "Engine did not return its normalized UTC instant.")
            val timezoneOffsetMinutes = chart.timezoneOffsetMinutes
                ?: return AynvoraResult.Failure.CalculationFailure("MISSING_TIMEZONE_OFFSET", "Engine did not return its resolved timezone offset.")
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
                    timezoneOffsetMinutes = timezoneOffsetMinutes,
                    utcTimestamp = utcTimestamp,
                    julianDay = chart.julianDay,
                    latitude = birth.place.coordinates.latitude,
                    longitude = birth.place.coordinates.longitude,
                    country = birth.place.country,
                    countryCode = birth.place.countryCode,
                    state = birth.place.stateName,
                    stateCode = birth.place.stateCode,
                    city = birth.place.cityName ?: birth.place.name,
                    cityId = birth.place.id,
                    locationSource = if (birth.place.locationDatasetVersion != null) "OFFLINE_CANONICAL_CATALOG" else "CALLER_SUPPLIED",
                    locationResolutionStatus = if (birth.place.locationDatasetVersion != null) AstroFeatureStatus.PARTIAL else AstroFeatureStatus.NOT_VERIFIED,
                    locationDatasetVersion = birth.place.locationDatasetVersion,
                    locationProvenance = birth.place.locationProvenance,
                    timezoneDataVersion = if (com.aynvora.astro.time.TimeNormalizer.supportsTimezoneId(birth.place.timezoneId)) com.aynvora.astro.time.TimeNormalizer.TIMEZONE_DATA_VERSION else null,
                    ayanamsaId = request.config.ayanamsa.name,
                    houseSystemId = request.config.houseSystem.name,
                    calculationProfileId = request.config.profile.name,
                    nodeConventionId = chart.calculationMetadata.conventions["node_profile"] ?: "UNSPECIFIED",
                ),
                calculation = chart.calculationMetadata,
                natalChart = snapshotChart,
                charts = buildCharts(snapshotChart),
                dasha = dasha,
                transitAtBirth = transit,
                panchang = panchang,
                tables = buildTables(snapshotChart),
                availability = emptyList(),
                featureResults = buildFeatureRecords(snapshotChart, birth.place, dasha != null, transit != null, panchang != null),
            ).let { it.copy(availability = AstrologySectionCatalog.forSnapshot(it)) }
            AynvoraResult.Success(snapshot, calculated.metadata, chart.calculationMetadata)
        }
    }
}

/** Headless SDK alias for callers that use the Kundali domain term. */
suspend fun AynvoraSdk.calculateKundali(
    request: ChartRequest,
    profileId: String,
    profileName: String,
    genderId: String? = null,
): AynvoraResult<KundaliSnapshot> = generateKundaliSnapshot(request, profileId, profileName, genderId)

/** Returns the already-calculated common chart by canonical chart ID without recalculation. */
fun KundaliSnapshot.getChart(chartId: String): AstroChartSnapshot? = charts.firstOrNull { it.chartId == chartId }

/** Stable JSON export of the complete, persisted snapshot contract. */
fun KundaliSnapshot.exportJson(): String = KundaliSnapshotJson.encode(this)

private fun buildFeatureRecords(
    chart: ChartResult,
    place: BirthPlace,
    hasDasha: Boolean,
    hasTransit: Boolean,
    hasPanchang: Boolean,
): Map<String, AstroFeatureRecord> {
    val metadata = chart.calculationMetadata
    fun record(
        id: String,
        ref: String?,
        dependencies: List<String> = emptyList(),
        status: AstroFeatureStatus = AstroFeatureStatus.SUPPORTED,
        warnings: List<String>? = null,
    ) =
        AstroFeatureRecord(
            id, "1", status, ref, dependencies, metadata,
            warnings = warnings ?: if (status == AstroFeatureStatus.NOT_VERIFIED) listOf("Canonical location or timezone database resolution is not configured; source values are caller supplied.") else emptyList(),
        )
    return listOf(
        record("location", "birth", status = if (place.locationDatasetVersion != null) AstroFeatureStatus.PARTIAL else AstroFeatureStatus.NOT_VERIFIED),
        record(
            "time", "birth", listOf("location"),
            status = if (com.aynvora.astro.time.TimeNormalizer.supportsTimezoneId(place.timezoneId)) AstroFeatureStatus.PARTIAL else AstroFeatureStatus.NOT_VERIFIED,
        ),
        record("ayanamsa", "calculation", listOf("time")),
        record("ephemeris", "natalChart.planetaryPositions", listOf("time")),
        record("planetary_positions", "natalChart.planetaryPositions", listOf("ephemeris", "ayanamsa")),
        record("lagna", "natalChart.lagna", listOf("time", "ayanamsa"), if (chart.lagna != null) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.PARTIAL),
        record("houses", "natalChart.houses", listOf("lagna", "planetary_positions"), if (chart.houses.size == 12) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.PARTIAL),
        record("varga", "charts", listOf("planetary_positions", "lagna"), if (chart.divisionalCharts.isNotEmpty()) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.PARTIAL),
        record("dignity", "natalChart.planetaryDignities", listOf("planetary_positions")),
        record("relationships", "natalChart.planetaryRelationships", listOf("planetary_positions")),
        record("aspects", "natalChart.aspects", listOf("planetary_positions")),
        record("planet_states", "natalChart.planetaryPositions", listOf("planetary_positions")),
        record("dasha", "dasha", listOf("planetary_positions"), if (hasDasha) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.UNSUPPORTED),
        record("transit_at_birth", "transitAtBirth", listOf("time"), if (hasTransit) AstroFeatureStatus.PARTIAL else AstroFeatureStatus.UNSUPPORTED),
        record("panchang_at_birth", "panchang", listOf("time"), if (hasPanchang) AstroFeatureStatus.PARTIAL else AstroFeatureStatus.UNSUPPORTED),
        record("shadbala", "natalChart.shadbala", listOf("planetary_positions")),
        record("ashtakavarga", "natalChart.ashtakavarga", listOf("planetary_positions")),
        record("shodhana", "natalChart.shodhitaAshtakavarga", listOf("ashtakavarga")),
        record("pinda", "natalChart.ashtakavargaPinda", listOf("shodhana")),
        record("chart", "commonChart", listOf("planetary_positions", "lagna", "houses"), if (chart.commonChart != null) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.UNSUPPORTED),
        record("grah_sthiti", "tables.grah_sthiti", listOf("chart", "planet_states", "dignity"), if (chart.grahSthiti?.rows?.size == chart.planetaryPositions.size) AstroFeatureStatus.SUPPORTED else AstroFeatureStatus.UNSUPPORTED),
        record("kp_analysis", null, listOf("core.time", "core.location", "core.ephemeris", "core.planetary_positions", "vedic.houses"), AstroFeatureStatus.NOT_VERIFIED,
            listOf("Verified KP ayanamsha, Placidus convention, 249 subdivision data, and source-backed rules are unavailable.")),
        record("lal_kitab_analysis", null, listOf("chart", "knowledge.rules"), AstroFeatureStatus.NOT_VERIFIED,
            listOf("An approved source edition and rights-cleared normalized rule pack are unavailable.")),
        record("varshaphal", null, listOf("core.time", "core.location", "core.ephemeris", "vedic.houses"), AstroFeatureStatus.NOT_VERIFIED,
            listOf("Verified solar return, annual chart, Tajika, and Mudda Dasha conventions are unavailable.")),
        record("phaladesh", null, listOf("vedic.dasha", "vedic.transit", "vedic.vargas", "astro.events", "knowledge.rules"), AstroFeatureStatus.UNSUPPORTED,
            listOf("Monthly processing requires supported event and tradition rule packs.")),
        record("astro_events", null, listOf("feature_evidence"), AstroFeatureStatus.NOT_VERIFIED,
            listOf("No event rules are bundled by default; occurrences require explicit source-backed definitions and calculated evidence.")),
        record("knowledge_rules", null, listOf("knowledge.pack"), AstroFeatureStatus.NOT_VERIFIED,
            listOf("No advanced tradition knowledge pack is installed.")),
        record("chalit", "tables.chalit_table", listOf("houses"), when (chart.chalit?.ruleStatus) {
            "SRIPATI_CHALIT_V1" -> AstroFeatureStatus.PARTIAL
            else -> AstroFeatureStatus.AMBIGUOUS
        }),
    ).associateBy { it.featureId }
}

internal fun buildCharts(chart: ChartResult): List<AstroChartSnapshot> {
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
    val grahRows = chart.grahSthiti?.rows.orEmpty()
    val planets = AstroTableSnapshot("graha_sthiti", "astrology.section.graha_sthiti", planetColumns,
        grahRows.map { row -> AstroTableRow(row.bodyId.name, listOf(
            AstroTableCell(AstroValueType.BODY, canonicalId = row.bodyId.name),
            AstroTableCell(AstroValueType.SIGN, canonicalId = Rashi.fromIndex(row.signIndex).name),
            AstroTableCell(AstroValueType.ANGLE, numericValue = row.signIndex * 30.0 + row.degreeInSign),
            AstroTableCell(AstroValueType.INTEGER, integerValue = row.houseNumber),
            AstroTableCell(AstroValueType.NAKSHATRA, canonicalId = Nakshatra.fromIndex(row.nakshatraIndex).name),
            AstroTableCell(AstroValueType.INTEGER, integerValue = row.pada),
            AstroTableCell(AstroValueType.BOOLEAN, canonicalId = row.retrograde.toString()),
            AstroTableCell(AstroValueType.BOOLEAN, canonicalId = (row.combustionState == com.aynvora.astro.states.CombustionState.COMBUST).toString()),
        )) }, if (grahRows.size == chart.planetaryPositions.size && grahRows.isNotEmpty()) CalculationAvailability.AVAILABLE else CalculationAvailability.UNSUPPORTED)
    val chalitFeature = chart.chalit
    val chalitAvailable = chalitFeature?.ruleStatus == "SRIPATI_CHALIT_V1" && chalitFeature.boundaries.size == 12
    val houses = if (!chalitAvailable) {
        // Do not substitute the selected natal house system for Chalit.
        AstroTableSnapshot("chalit_table", "astrology.section.chalit_table", emptyList(), emptyList(), CalculationAvailability.AMBIGUOUS)
    } else {
        val columns = listOf(
            AstroTableColumn("house", "astrology.column.house", AstroValueType.INTEGER),
            AstroTableColumn("sign", "astrology.column.sign", AstroValueType.SIGN),
            AstroTableColumn("cusp", "astrology.column.cusp", AstroValueType.ANGLE),
            AstroTableColumn("planets", "astrology.column.planets", AstroValueType.TEXT),
        )
        val rows = (1..12).map { house ->
            val cusp = chalitFeature.boundaries[house - 1]
            val signIndex = (cusp / 30.0).toInt().coerceIn(0, 11)
            val planetIds = chalitFeature.planetHouseOccupancy.filterValues { it == house }.keys.sortedBy { it.ordinal }.joinToString(",") { it.name }
            AstroTableRow(house.toString(), listOf(
                AstroTableCell(AstroValueType.INTEGER, integerValue = house),
                AstroTableCell(AstroValueType.SIGN, canonicalId = Rashi.fromIndex(signIndex).name),
                AstroTableCell(AstroValueType.ANGLE, numericValue = cusp),
                AstroTableCell(AstroValueType.TEXT, textValue = planetIds),
            ))
        }
        AstroTableSnapshot("chalit_table", "astrology.section.chalit_table", columns, rows, CalculationAvailability.PARTIAL)
    }
    return listOf(planets, houses)
}

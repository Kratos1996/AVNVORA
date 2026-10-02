package com.aynvora.astro.pipeline

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.context.AstroObservationContext
import com.aynvora.astro.aspects.AspectCalculator
import com.aynvora.astro.aspects.AspectPosition
import com.aynvora.astro.ashtakavarga.AshtakavargaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaShodhanaCalculator
import com.aynvora.astro.ashtakavarga.ShodhitaAshtakavargaResult
import com.aynvora.astro.dignity.PlanetaryDignityCalculator
import com.aynvora.astro.dignity.PlanetaryDignityPosition
import com.aynvora.astro.houses.HouseCalculationInput
import com.aynvora.astro.houses.HouseCalculationResult
import com.aynvora.astro.houses.HouseSystemRegistry
import com.aynvora.astro.lagna.LagnaCalculator
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.PlanetaryCalculator
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipPosition
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.provenance.EphemerisProviderMetadata
import com.aynvora.astro.shadbala.PlanetaryShadbalaPosition
import com.aynvora.astro.shadbala.ShadbalaCalculator
import com.aynvora.astro.states.PlanetStateCalculator
import com.aynvora.astro.states.PlanetStatePosition
import com.aynvora.astro.time.TimeNormalizer
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import com.aynvora.astro.varga.VargaEngine
import com.aynvora.astro.varga.VargaProfile

object CoreFeatureKeys {
    val Location = FeatureKey<ResolvedLocationResult>("core.location")
    val Time = FeatureKey<TimeNormalizer.NormalizedUtcTime>("core.time")
    val Ayanamsa = FeatureKey<Double>("core.ayanamsa")
    val Ephemeris = FeatureKey<EphemerisResult>("core.ephemeris")
    val PlanetaryPositions = FeatureKey<List<BodyPosition>>("core.planetary_positions")
    val Lagna = FeatureKey<LagnaPosition>("vedic.lagna")
    val Houses = FeatureKey<HouseCalculationResult>("vedic.houses")
    val Vargas = FeatureKey<Map<DivisionalChart, VargaChartResult>>("vedic.vargas")
    val Aspects = FeatureKey<List<AspectPosition>>("vedic.aspects")
    val PlanetStates = FeatureKey<List<PlanetStatePosition>>("vedic.planet_states")
    val Dignities = FeatureKey<List<PlanetaryDignityPosition>>("vedic.dignities")
    val Relationships = FeatureKey<List<PlanetaryRelationshipPosition>>("vedic.relationships")
    val Shadbala = FeatureKey<List<PlanetaryShadbalaPosition>>("vedic.shadbala")
    val Ashtakavarga = FeatureKey<AshtakavargaResult>("vedic.ashtakavarga")
    val Shodhana = FeatureKey<ShodhitaAshtakavargaResult>("vedic.shodhana")
    val Pinda = FeatureKey<AshtakavargaPindaResult>("vedic.pinda")
    val Dasha = FeatureKey<com.aynvora.astro.dasha.VimshottariDashaTimeline>("vedic.dasha")
    val Chalit = FeatureKey<ChalitFeatureResult>("vedic.chalit")
    val Chart = FeatureKey<AstroChartFeatureResult>("vedic.chart")
    val GrahSthiti = FeatureKey<GrahSthitiResult>("vedic.grah_sthiti")
    val TransitObservation = FeatureKey<AstroObservationContext>("input.transit_observation")
    val PanchangObservation = FeatureKey<AstroObservationContext>("input.panchang_observation")
    val Transit = FeatureKey<TransitPipelineResult>("vedic.transit")
    val Panchang = FeatureKey<PanchangPipelineResult>("vedic.panchang")
}

@kotlinx.serialization.Serializable
data class ChalitFeatureResult(
    val ruleStatus: String,
    val boundaries: List<Double> = emptyList(),
    val houseSystem: String? = null,
    val planetHouseOccupancy: Map<BodyId, Int> = emptyMap(),
)

/** Engine-layer chart payload; it stays independent of UI and core SDK modules. */
@kotlinx.serialization.Serializable
data class AstroChartFeatureResult(
    val chartId: String,
    val zodiacMode: String,
    val houseSystem: String,
    val ascendant: ChartAscendantFeature,
    val houses: List<ChartHouseFeature>,
    val provenance: CalculationMetadata,
)

@kotlinx.serialization.Serializable
data class ChartAscendantFeature(val longitude: Double, val signIndex: Int, val degreeInSign: Double, val nakshatraIndex: Int, val pada: Int)

@kotlinx.serialization.Serializable
data class ChartHouseFeature(
    val houseNumber: Int,
    val signIndex: Int,
    val startLongitude: Double,
    val cuspLongitude: Double,
    val endLongitude: Double,
    val planets: List<BodyPosition>,
)

/** Rows are projected from chart and state outputs and never run ephemeris calculations. */
@kotlinx.serialization.Serializable
data class GrahSthitiResult(val rows: List<GrahSthitiRow>, val provenance: CalculationMetadata)

@kotlinx.serialization.Serializable
data class GrahSthitiRow(
    val bodyId: BodyId,
    val signIndex: Int,
    val degreeInSign: Double,
    val houseNumber: Int,
    val nakshatraIndex: Int,
    val pada: Int,
    val retrograde: Boolean,
    val combustionState: com.aynvora.astro.states.CombustionState,
    val dignityId: String?,
)

/** Place facts passed through the canonical catalog or explicitly supplied by the caller. */
@kotlinx.serialization.Serializable
data class ResolvedLocationResult(
    val countryCode: String?, val countryName: String?, val stateCode: String?, val stateName: String?,
    val cityId: String?, val cityName: String?, val latitude: Double, val longitude: Double,
    val timezoneId: String, val timezoneDataVersion: String?, val source: String,
    val locationDatasetVersion: String? = null, val locationProvenance: String? = null,
)

@kotlinx.serialization.Serializable
data class EphemerisBodyValue(
    val bodyId: BodyId, val tropicalLongitude: Double, val dailyMotionDegrees: Double,
    val isRetrograde: Boolean,
)

@kotlinx.serialization.Serializable
data class EphemerisResult(
    val providerId: String, val providerVersion: String, val source: String,
    val epochJulianDay: Double, val supportedBodies: List<BodyId>,
    val rawValues: List<EphemerisBodyValue>, val status: String,
    val provenance: CalculationMetadata,
    val providerMetadata: EphemerisProviderMetadata,
)

/** Registry of deterministic core engines; specialized chart traditions stay outside this set. */
object CoreAstroFeatureRegistry {
    fun engines(vargaEngine: VargaEngine): List<AstroFeatureEngine<*>> = listOf(
        LocationFeatureEngine, TimeFeatureEngine, AyanamsaFeatureEngine, EphemerisFeatureEngine, PlanetaryPositionFeatureEngine,
        LagnaFeatureEngine, HouseFeatureEngine, VargaFeatureEngine(vargaEngine),
        AspectFeatureEngine, PlanetStateFeatureEngine, DignityFeatureEngine,
        RelationshipFeatureEngine, ShadbalaFeatureEngine(vargaEngine),
        AshtakavargaFeatureEngine, ShodhanaFeatureEngine, PindaFeatureEngine, DashaFeatureEngine, ChalitFeatureEngine,
        ChartFeatureEngine, GrahSthitiFeatureEngine, TransitFeatureEngine, PanchangFeatureEngine,
    )

    val featureIds: Set<String> = setOf(
        CoreFeatureKeys.Location.id, CoreFeatureKeys.Time.id, CoreFeatureKeys.Ayanamsa.id,
        CoreFeatureKeys.Ephemeris.id, CoreFeatureKeys.PlanetaryPositions.id,
        CoreFeatureKeys.Lagna.id, CoreFeatureKeys.Houses.id, CoreFeatureKeys.Vargas.id,
        CoreFeatureKeys.Aspects.id, CoreFeatureKeys.PlanetStates.id, CoreFeatureKeys.Dignities.id,
        CoreFeatureKeys.Relationships.id, CoreFeatureKeys.Shadbala.id, CoreFeatureKeys.Ashtakavarga.id,
        CoreFeatureKeys.Shodhana.id, CoreFeatureKeys.Pinda.id, CoreFeatureKeys.Dasha.id, CoreFeatureKeys.Chalit.id,
        CoreFeatureKeys.Chart.id, CoreFeatureKeys.GrahSthiti.id, CoreFeatureKeys.Transit.id, CoreFeatureKeys.Panchang.id,
    )

    val natalFeatureIds: Set<String> = featureIds - setOf(CoreFeatureKeys.Transit.id, CoreFeatureKeys.Panchang.id)

    fun descriptors(vargaEngine: VargaEngine): List<AstroFeatureDescriptor> = engines(vargaEngine).map { engine ->
        AstroFeatureDescriptor(
            featureId = engine.featureId,
            version = engine.featureVersion,
            dependencies = engine.dependencies.map { it.id }.sorted(),
            supportedProfiles = engine.supportedCalculationProfiles,
            status = when (engine) {
                ChalitFeatureEngine -> FeatureStatus.AMBIGUOUS
                TransitFeatureEngine, PanchangFeatureEngine -> FeatureStatus.PARTIAL
                else -> FeatureStatus.SUPPORTED
            },
        )
    }
}

@kotlinx.serialization.Serializable
data class AstroFeatureDescriptor(
    val featureId: String,
    val version: String,
    val dependencies: List<String>,
    val supportedProfiles: Set<String>,
    val status: FeatureStatus,
)

/** Uses the already normalized birth instant and Moon output from the shared feature set. */
object DashaFeatureEngine : AstroFeatureEngine<com.aynvora.astro.dasha.VimshottariDashaTimeline> {
    override val output = CoreFeatureKeys.Dasha
    override val dependencies = setOf(CoreFeatureKeys.Time, CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<com.aynvora.astro.dasha.VimshottariDashaTimeline> {
        val time = inputs.get(CoreFeatureKeys.Time).value ?: error("Normalized time is unavailable")
        val moon = inputs.get(CoreFeatureKeys.PlanetaryPositions).value?.firstOrNull { it.bodyId == BodyId.MOON }
            ?: error("Moon position is unavailable")
        val value = com.aynvora.astro.dasha.VimshottariDashaCalculator.calculate(time.julianDay.value, moon.siderealLongitude, true, true)
        return FeatureOutput(value, FeatureStatus.SUPPORTED, context.provenance("vimshottari_dasha"))
    }
}

/** Chalit stays explicitly ambiguous until its named cusp convention has a verified implementation. */
object ChalitFeatureEngine : AstroFeatureEngine<ChalitFeatureResult> {
    override val output = CoreFeatureKeys.Chalit
    override val dependencies = setOf(CoreFeatureKeys.Time, CoreFeatureKeys.Lagna, CoreFeatureKeys.Houses, CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<ChalitFeatureResult> {
        val houses = inputs.get(CoreFeatureKeys.Houses).value ?: error("House results are unavailable")
        return if (houses.houseSystem == "SRIPATI_CHALIT_V1") {
            FeatureOutput(
                ChalitFeatureResult("SRIPATI_CHALIT_V1", houses.houses.map { it.cuspLongitude }, houses.houseSystem, houses.planetHouseOccupancy),
                FeatureStatus.PARTIAL,
                context.provenance("chalit").copy(conventions = context.provenance("chalit").conventions + ("chalit_ruleset" to "SRIPATI_CHALIT_V1")),
                listOf(FeatureWarning("CHALIT_RULESET_PARTIAL", "Sripati cusps and occupancy are calculated; traditional interpretation conventions remain unverified.")),
            )
        } else FeatureOutput(
            ChalitFeatureResult("AMBIGUOUS"), FeatureStatus.AMBIGUOUS,
            context.provenance("chalit"),
            listOf(FeatureWarning("CHALIT_RULESET_AMBIGUOUS", "The configured house system does not identify a verified Chalit cusp and occupancy convention.")),
        )
    }
}

/** Common chart projection built from shared Lagna, house, and planetary outputs. */
object ChartFeatureEngine : AstroFeatureEngine<AstroChartFeatureResult> {
    override val output = CoreFeatureKeys.Chart
    override val dependencies = setOf(CoreFeatureKeys.Lagna, CoreFeatureKeys.Houses, CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<AstroChartFeatureResult> {
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Lagna is unavailable")
        val houseResult = inputs.get(CoreFeatureKeys.Houses).value ?: error("House results are unavailable")
        val planets = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val houses = houseResult.houses.sortedBy { it.houseNumber }.map { house ->
            ChartHouseFeature(
                house.houseNumber, house.rashiIndex, house.startLongitude, house.cuspLongitude, house.endLongitude,
                planets.filter { houseResult.planetHouseOccupancy[it.bodyId] == house.houseNumber }.sortedBy { it.bodyId.ordinal },
            )
        }
        require(houses.size == 12 && houses.map { it.houseNumber } == (1..12).toList()) { "Chart requires houses 1 through 12" }
        val provenance = context.provenance("chart")
        return FeatureOutput(
            AstroChartFeatureResult("D1", "SIDEREAL", houseResult.houseSystem,
                ChartAscendantFeature(lagna.siderealLongitude, lagna.rashiIndex, lagna.degreeInRashi, lagna.nakshatraIndex, lagna.pada),
                houses, provenance),
            FeatureStatus.SUPPORTED, provenance,
        )
    }
}

/** Grah Sthiti is a stable table projection over already calculated engine outputs. */
object GrahSthitiFeatureEngine : AstroFeatureEngine<GrahSthitiResult> {
    override val output = CoreFeatureKeys.GrahSthiti
    override val dependencies = setOf(CoreFeatureKeys.Chart, CoreFeatureKeys.PlanetStates, CoreFeatureKeys.Dignities)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<GrahSthitiResult> {
        val chart = inputs.get(CoreFeatureKeys.Chart).value ?: error("Chart is unavailable")
        val states = inputs.get(CoreFeatureKeys.PlanetStates).value.orEmpty().associateBy { it.bodyId }
        val dignities = inputs.get(CoreFeatureKeys.Dignities).value.orEmpty().associateBy { it.bodyId }
        val rows = chart.houses.flatMap { house -> house.planets.map { planet ->
            val state = states[planet.bodyId]
            GrahSthitiRow(
                planet.bodyId, planet.rashiIndex, planet.degreeInRashi, house.houseNumber,
                planet.nakshatraIndex, planet.pada, planet.isRetrograde,
                state?.combustionState ?: com.aynvora.astro.states.CombustionState.NOT_APPLICABLE,
                dignities[planet.bodyId]?.dignityType?.name,
            )
        } }.sortedBy { it.bodyId.ordinal }
        val provenance = context.provenance("grah_sthiti")
        return FeatureOutput(GrahSthitiResult(rows, provenance), FeatureStatus.SUPPORTED, provenance)
    }
}

object LocationFeatureEngine : AstroFeatureEngine<ResolvedLocationResult> {
    override val output = CoreFeatureKeys.Location
    override val dependencies = emptySet<FeatureKey<*>>()
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs) = FeatureOutput(
        ResolvedLocationResult(
            context.birthData.countryCode, context.birthData.countryName,
            context.birthData.stateCode, context.birthData.stateName,
            context.birthData.cityId, context.birthData.cityName,
            context.birthData.latitude, context.birthData.longitude, context.birthData.timeZoneId,
            timezoneDataVersion = if (TimeNormalizer.supportsTimezoneId(context.birthData.timeZoneId)) TimeNormalizer.TIMEZONE_DATA_VERSION else null,
            source = if (context.birthData.locationDatasetVersion != null) "offline_canonical_catalog" else "caller_supplied",
            locationDatasetVersion = context.birthData.locationDatasetVersion,
            locationProvenance = context.birthData.locationProvenance,
        ),
        if (context.birthData.locationDatasetVersion != null) FeatureStatus.PARTIAL else FeatureStatus.NOT_VERIFIED,
        context.provenance("location"),
        if (context.birthData.locationDatasetVersion != null) emptyList() else listOf(FeatureWarning(
            "LOCATION_CATALOG_UNVERIFIED", "Place identity and timezone mapping were supplied by the caller without canonical catalog metadata.",
        )),
    )
}

object TimeFeatureEngine : AstroFeatureEngine<TimeNormalizer.NormalizedUtcTime> {
    override val output = CoreFeatureKeys.Time
    override val dependencies = setOf(CoreFeatureKeys.Location)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<TimeNormalizer.NormalizedUtcTime> {
        val location = inputs.get(CoreFeatureKeys.Location).value ?: error("Location input is unavailable")
        val zoneRulesVerified = TimeNormalizer.supportsTimezoneId(location.timezoneId)
        return FeatureOutput(
            context.normalizedUtc,
            if (zoneRulesVerified) FeatureStatus.PARTIAL else FeatureStatus.NOT_VERIFIED,
            context.provenance("time_normalization"),
            if (zoneRulesVerified) listOf(FeatureWarning(
                "CURATED_TIMEZONE_RULES", "Timezone normalization used ${TimeNormalizer.TIMEZONE_DATA_VERSION}; historical IANA coverage is not complete.",
            )) else listOf(FeatureWarning(
                "TIMEZONE_DATA_VERSION_UNAVAILABLE",
                "Timezone normalization completed, but this zone is outside the versioned curated rule set.",
            )),
        )
    }
}

object AyanamsaFeatureEngine : AstroFeatureEngine<Double> {
    override val output = CoreFeatureKeys.Ayanamsa
    override val dependencies = setOf(CoreFeatureKeys.Time)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<Double> {
        val time = inputs.get(CoreFeatureKeys.Time).value ?: error("Normalized time is unavailable")
        val value = AyanamsaCalculator.forConvention(context.config.ayanamsa).calculate(time.julianDay)
        return FeatureOutput(value, FeatureStatus.SUPPORTED, context.provenance("ayanamsa"))
    }
}

object EphemerisFeatureEngine : AstroFeatureEngine<EphemerisResult> {
    override val output = CoreFeatureKeys.Ephemeris
    override val dependencies = setOf(CoreFeatureKeys.Time)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<EphemerisResult> {
        val jd = inputs.get(CoreFeatureKeys.Time).value?.julianDay ?: error("Normalized time is unavailable")
        val values = buildList {
            val sun = SunCalculator.calculate(jd)
            add(EphemerisBodyValue(BodyId.SUN, sun.apparentLongitude, sun.dailyMotionDegrees, false))
            val moon = MoonCalculator.calculate(jd)
            add(EphemerisBodyValue(BodyId.MOON, moon.apparentLongitude, moon.dailyMotionDegrees, false))
            PlanetaryCalculator.Planet.entries.forEach { planet ->
                val position = PlanetaryCalculator.calculate(planet, jd)
                val body = when (planet) {
                    PlanetaryCalculator.Planet.MERCURY -> BodyId.MERCURY
                    PlanetaryCalculator.Planet.VENUS -> BodyId.VENUS
                    PlanetaryCalculator.Planet.MARS -> BodyId.MARS
                    PlanetaryCalculator.Planet.JUPITER -> BodyId.JUPITER
                    PlanetaryCalculator.Planet.SATURN -> BodyId.SATURN
                }
                add(EphemerisBodyValue(body, position.apparentLongitude, position.dailyMotionDegrees, position.isRetrograde))
            }
            LunarNodesCalculator.calculate(jd).let { nodes ->
                add(EphemerisBodyValue(BodyId.RAHU, nodes.rahu.apparentLongitude, nodes.rahu.dailyMotionDegrees, true))
                add(EphemerisBodyValue(BodyId.KETU, nodes.ketu.apparentLongitude, nodes.ketu.dailyMotionDegrees, true))
            }
        }
        val provenance = context.provenance("ephemeris")
        return FeatureOutput(
            EphemerisResult(context.providerId, context.engineVersion, provenance.ephemerisSourceId,
                jd.value, values.map { it.bodyId }, values, "SUPPORTED", provenance,
                EphemerisProviderMetadata.AnalyticalMeeusSimon.copy(
                    providerId = context.providerId,
                    version = context.engineVersion,
                    source = provenance.ephemerisSourceId,
                )),
            FeatureStatus.SUPPORTED, provenance,
        )
    }
}

object PlanetaryPositionFeatureEngine : AstroFeatureEngine<List<BodyPosition>> {
    override val output = CoreFeatureKeys.PlanetaryPositions
    override val dependencies = setOf(CoreFeatureKeys.Ephemeris, CoreFeatureKeys.Ayanamsa)

    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<BodyPosition>> {
        val ephemeris = inputs.get(CoreFeatureKeys.Ephemeris).value ?: error("Ephemeris output is unavailable")
        val ayanamsa = inputs.get(CoreFeatureKeys.Ayanamsa).value ?: error("Ayanamsa is unavailable")
        val positions = ephemeris.rawValues.map { raw ->
            AynvoraPositionFactory.create(raw.bodyId, raw.tropicalLongitude, ayanamsa, raw.isRetrograde, raw.dailyMotionDegrees)
        }
        return FeatureOutput(positions, FeatureStatus.SUPPORTED, context.provenance("planetary_positions"))
    }
}

object LagnaFeatureEngine : AstroFeatureEngine<LagnaPosition> {
    override val output = CoreFeatureKeys.Lagna
    override val dependencies = setOf(CoreFeatureKeys.Time, CoreFeatureKeys.Ayanamsa)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<LagnaPosition> {
        val time = inputs.get(CoreFeatureKeys.Time).value ?: error("Normalized time is unavailable")
        val ayanamsa = inputs.get(CoreFeatureKeys.Ayanamsa).value ?: error("Ayanamsa is unavailable")
        val lagna = LagnaCalculator.calculate(time.julianDay, context.birthData.latitude, context.birthData.longitude, ayanamsa)
        return FeatureOutput(lagna, FeatureStatus.SUPPORTED, context.provenance("lagna"))
    }
}

object HouseFeatureEngine : AstroFeatureEngine<HouseCalculationResult> {
    override val output = CoreFeatureKeys.Houses
    override val dependencies = setOf(CoreFeatureKeys.Lagna, CoreFeatureKeys.PlanetaryPositions, CoreFeatureKeys.Ayanamsa)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<HouseCalculationResult> {
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Lagna is unavailable")
        val planets = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val ayanamsa = inputs.get(CoreFeatureKeys.Ayanamsa).value ?: error("Ayanamsa is unavailable")
        val houseResult = HouseSystemRegistry.forName(context.config.houseSystem).calculate(
            HouseCalculationInput(lagna, context.birthData.latitude, context.birthData.longitude, ayanamsa,
                planets.associate { it.bodyId to it.siderealLongitude }),
        )
        return FeatureOutput(houseResult, FeatureStatus.SUPPORTED, context.provenance("houses"))
    }
}

class VargaFeatureEngine(private val engine: VargaEngine) : AstroFeatureEngine<Map<DivisionalChart, VargaChartResult>> {
    override val output = CoreFeatureKeys.Vargas
    override val dependencies = setOf(CoreFeatureKeys.Lagna, CoreFeatureKeys.PlanetaryPositions, CoreFeatureKeys.Ayanamsa)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<Map<DivisionalChart, VargaChartResult>> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Lagna is unavailable")
        val charts = context.config.requestedDivisionalCharts
        val results = if (charts.isEmpty()) emptyMap() else {
            if (context.config.vargaRulesetId != VargaProfile.DEFAULT_RULESET_ID) {
                throw UnsupportedOperationException(
                    "Divisional chart ruleset '${context.config.vargaRulesetId}' is unsupported.",
                )
            }
            engine.calculateMultiple(positions, lagna, charts, VargaProfile(rulesetId = context.config.vargaRulesetId))
        }
        return FeatureOutput(results, FeatureStatus.SUPPORTED, context.provenance("vargas"))
    }
}

object AspectFeatureEngine : AstroFeatureEngine<List<AspectPosition>> {
    override val output = CoreFeatureKeys.Aspects
    override val dependencies = setOf(CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<AspectPosition>> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        return FeatureOutput(AspectCalculator.calculate(positions), FeatureStatus.SUPPORTED, context.provenance("aspects"))
    }
}

object PlanetStateFeatureEngine : AstroFeatureEngine<List<PlanetStatePosition>> {
    override val output = CoreFeatureKeys.PlanetStates
    override val dependencies = setOf(CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<PlanetStatePosition>> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        return FeatureOutput(PlanetStateCalculator.calculate(positions), FeatureStatus.SUPPORTED, context.provenance("planet_states"))
    }
}

object DignityFeatureEngine : AstroFeatureEngine<List<PlanetaryDignityPosition>> {
    override val output = CoreFeatureKeys.Dignities
    override val dependencies = setOf(CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<PlanetaryDignityPosition>> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        return FeatureOutput(PlanetaryDignityCalculator.calculateDignities(positions), FeatureStatus.SUPPORTED, context.provenance("dignities"))
    }
}

object RelationshipFeatureEngine : AstroFeatureEngine<List<PlanetaryRelationshipPosition>> {
    override val output = CoreFeatureKeys.Relationships
    override val dependencies = setOf(CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<PlanetaryRelationshipPosition>> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        return FeatureOutput(PlanetaryRelationshipCalculator.calculateRelationships(positions), FeatureStatus.SUPPORTED, context.provenance("relationships"))
    }
}

class ShadbalaFeatureEngine(private val vargaEngine: VargaEngine) : AstroFeatureEngine<List<PlanetaryShadbalaPosition>> {
    override val output = CoreFeatureKeys.Shadbala
    override val dependencies = setOf(CoreFeatureKeys.Time, CoreFeatureKeys.Lagna, CoreFeatureKeys.Houses, CoreFeatureKeys.PlanetaryPositions, CoreFeatureKeys.Vargas)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<PlanetaryShadbalaPosition>> {
        val time = inputs.get(CoreFeatureKeys.Time).value ?: error("Normalized time is unavailable")
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Lagna is unavailable")
        val houses = inputs.get(CoreFeatureKeys.Houses).value ?: error("House results are unavailable")
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val vargas = inputs.get(CoreFeatureKeys.Vargas).value ?: error("Varga results are unavailable")
        val result = ShadbalaCalculator.calculateShadbala(
            positions = positions,
            lagnaLongitude = lagna.siderealLongitude,
            houseCusps = houses.houses.associate { it.houseNumber to it.cuspLongitude },
            planetHouseOccupancy = houses.planetHouseOccupancy,
            vargas = vargas,
            vargaEngine = vargaEngine,
            julianDay = time.julianDay.value,
            birthHour = context.birthData.hour ?: 12,
            obliquityDeg = lagna.obliquityDegrees,
        )
        return FeatureOutput(result, FeatureStatus.SUPPORTED, context.provenance("shadbala"))
    }
}

object AshtakavargaFeatureEngine : AstroFeatureEngine<AshtakavargaResult> {
    override val output = CoreFeatureKeys.Ashtakavarga
    override val dependencies = setOf(CoreFeatureKeys.PlanetaryPositions, CoreFeatureKeys.Lagna)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<AshtakavargaResult> {
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Lagna is unavailable")
        val result = AshtakavargaCalculator.calculateAshtakavarga(positions, lagna, context.config.ashtakavargaRulesetId)
        return FeatureOutput(result, FeatureStatus.SUPPORTED, context.provenance("ashtakavarga"))
    }
}

object ShodhanaFeatureEngine : AstroFeatureEngine<ShodhitaAshtakavargaResult> {
    override val output = CoreFeatureKeys.Shodhana
    override val dependencies = setOf(CoreFeatureKeys.Ashtakavarga, CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<ShodhitaAshtakavargaResult> {
        val ashtakavarga = inputs.get(CoreFeatureKeys.Ashtakavarga).value ?: error("Ashtakavarga is unavailable")
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val result = AshtakavargaShodhanaCalculator.calculateShodhana(ashtakavarga, positions)
        return FeatureOutput(result, FeatureStatus.SUPPORTED, context.provenance("shodhana"))
    }
}

object PindaFeatureEngine : AstroFeatureEngine<AshtakavargaPindaResult> {
    override val output = CoreFeatureKeys.Pinda
    override val dependencies = setOf(CoreFeatureKeys.Shodhana, CoreFeatureKeys.PlanetaryPositions)
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<AshtakavargaPindaResult> {
        val shodhana = inputs.get(CoreFeatureKeys.Shodhana).value ?: error("Shodhana is unavailable")
        val positions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value ?: error("Planetary positions are unavailable")
        val result = AshtakavargaPindaCalculator.calculatePindas(shodhana, positions)
        return FeatureOutput(result, FeatureStatus.SUPPORTED, context.provenance("pinda"))
    }
}

fun AstroCalculationContext.provenance(featureId: String) = CalculationMetadata(
    contractVersion = calculationContractVersion,
    calculationProfileId = config.profile,
    engineVersion = engineVersion,
    calculationModel = "MEEUS_VSOP87",
    ephemerisSourceId = providerId,
    ephemerisDataVersion = dataVersion,
    conventions = mapOf(
        "feature_id" to featureId,
        "ayanamsa" to config.ayanamsa,
        "house_system" to config.houseSystem,
    ),
)

private object AynvoraPositionFactory {
    fun create(body: BodyId, longitude: Double, ayanamsa: Double, retrograde: Boolean, motion: Double): BodyPosition =
        com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(body, longitude, ayanamsa, retrograde, motion)
}

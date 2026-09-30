package com.aynvora.astro.pipeline

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.context.AstroCalculationContext
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
    val Time = FeatureKey<TimeNormalizer.NormalizedUtcTime>("core.time")
    val Ayanamsa = FeatureKey<Double>("core.ayanamsa")
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
}

data class ChalitFeatureResult(val ruleStatus: String, val boundaries: List<Double> = emptyList())

/** Registry of deterministic core engines; specialized chart traditions stay outside this set. */
object CoreAstroFeatureRegistry {
    fun engines(vargaEngine: VargaEngine): List<AstroFeatureEngine<*>> = listOf(
        TimeFeatureEngine, AyanamsaFeatureEngine, PlanetaryPositionFeatureEngine,
        LagnaFeatureEngine, HouseFeatureEngine, VargaFeatureEngine(vargaEngine),
        AspectFeatureEngine, PlanetStateFeatureEngine, DignityFeatureEngine,
        RelationshipFeatureEngine, ShadbalaFeatureEngine(vargaEngine),
        AshtakavargaFeatureEngine, ShodhanaFeatureEngine, PindaFeatureEngine, DashaFeatureEngine, ChalitFeatureEngine,
    )

    val featureIds: Set<String> = setOf(
        CoreFeatureKeys.Time.id, CoreFeatureKeys.Ayanamsa.id, CoreFeatureKeys.PlanetaryPositions.id,
        CoreFeatureKeys.Lagna.id, CoreFeatureKeys.Houses.id, CoreFeatureKeys.Vargas.id,
        CoreFeatureKeys.Aspects.id, CoreFeatureKeys.PlanetStates.id, CoreFeatureKeys.Dignities.id,
        CoreFeatureKeys.Relationships.id, CoreFeatureKeys.Shadbala.id, CoreFeatureKeys.Ashtakavarga.id,
        CoreFeatureKeys.Shodhana.id, CoreFeatureKeys.Pinda.id, CoreFeatureKeys.Dasha.id, CoreFeatureKeys.Chalit.id,
    )
}

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
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs) = FeatureOutput(
        ChalitFeatureResult("AMBIGUOUS"), FeatureStatus.AMBIGUOUS,
        context.provenance("chalit"),
        listOf(FeatureWarning("CHALIT_RULESET_AMBIGUOUS", "The requested Chalit cusp and occupancy convention is not verified.")),
    )
}

object TimeFeatureEngine : AstroFeatureEngine<TimeNormalizer.NormalizedUtcTime> {
    override val output = CoreFeatureKeys.Time
    override val dependencies = emptySet<FeatureKey<*>>()
    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs) =
        FeatureOutput(context.normalizedUtc, FeatureStatus.SUPPORTED, context.provenance("time_normalization"))
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

object PlanetaryPositionFeatureEngine : AstroFeatureEngine<List<BodyPosition>> {
    override val output = CoreFeatureKeys.PlanetaryPositions
    override val dependencies = setOf(CoreFeatureKeys.Time, CoreFeatureKeys.Ayanamsa)

    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<List<BodyPosition>> {
        val time = inputs.get(CoreFeatureKeys.Time).value ?: error("Normalized time is unavailable")
        val ayanamsa = inputs.get(CoreFeatureKeys.Ayanamsa).value ?: error("Ayanamsa is unavailable")
        val jd = time.julianDay
        val positions = buildList {
            val sun = SunCalculator.calculate(jd)
            add(AynvoraPositionFactory.create(BodyId.SUN, sun.apparentLongitude, ayanamsa, false, sun.dailyMotionDegrees))
            val moon = MoonCalculator.calculate(jd)
            add(AynvoraPositionFactory.create(BodyId.MOON, moon.apparentLongitude, ayanamsa, false, moon.dailyMotionDegrees))
            PlanetaryCalculator.Planet.entries.forEach { planet ->
                val calculated = PlanetaryCalculator.calculate(planet, jd)
                val body = when (planet) {
                    PlanetaryCalculator.Planet.MERCURY -> BodyId.MERCURY
                    PlanetaryCalculator.Planet.VENUS -> BodyId.VENUS
                    PlanetaryCalculator.Planet.MARS -> BodyId.MARS
                    PlanetaryCalculator.Planet.JUPITER -> BodyId.JUPITER
                    PlanetaryCalculator.Planet.SATURN -> BodyId.SATURN
                }
                add(AynvoraPositionFactory.create(body, calculated.apparentLongitude, ayanamsa, calculated.isRetrograde, calculated.dailyMotionDegrees))
            }
            val nodes = LunarNodesCalculator.calculate(jd)
            add(AynvoraPositionFactory.create(BodyId.RAHU, nodes.rahu.apparentLongitude, ayanamsa, true, nodes.rahu.dailyMotionDegrees))
            add(AynvoraPositionFactory.create(BodyId.KETU, nodes.ketu.apparentLongitude, ayanamsa, true, nodes.ketu.dailyMotionDegrees))
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

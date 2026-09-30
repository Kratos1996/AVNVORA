package com.aynvora.astro

import com.aynvora.astro.ashtakavarga.AshtakavargaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaCalculator
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaResult
import com.aynvora.astro.ashtakavarga.AshtakavargaShodhanaCalculator
import com.aynvora.astro.ashtakavarga.ShodhitaAshtakavargaResult
import com.aynvora.astro.aspects.AspectCalculator
import com.aynvora.astro.aspects.AspectPosition
import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.dignity.PlanetaryDignityCalculator
import com.aynvora.astro.dignity.PlanetaryDignityPosition
import com.aynvora.astro.houses.HouseCalculationInput
import com.aynvora.astro.houses.HousePosition
import com.aynvora.astro.houses.HouseSystemRegistry
import com.aynvora.astro.lagna.LagnaCalculator
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.PlanetaryCalculator
import com.aynvora.astro.planets.PlanetaryCalculator.Planet
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipPosition
import com.aynvora.astro.shadbala.PlanetaryShadbalaPosition
import com.aynvora.astro.shadbala.ShadbalaCalculator
import com.aynvora.astro.states.PlanetStateCalculator
import com.aynvora.astro.states.PlanetStatePosition
import com.aynvora.astro.time.TimeNormalizer
import com.aynvora.astro.varga.DefaultVargaEngine
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import com.aynvora.astro.varga.VargaEngine
import com.aynvora.astro.varga.VargaProfile
import com.aynvora.astro.zodiac.ZodiacCalculator
import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.pipeline.AstroFeaturePipeline
import com.aynvora.astro.pipeline.CoreAstroFeatureRegistry
import com.aynvora.astro.pipeline.AyanamsaFeatureEngine
import com.aynvora.astro.pipeline.CoreFeatureKeys
import com.aynvora.astro.pipeline.PlanetaryPositionFeatureEngine
import com.aynvora.astro.pipeline.TimeFeatureEngine
import com.aynvora.astro.pipeline.LagnaFeatureEngine
import com.aynvora.astro.pipeline.HouseFeatureEngine
import com.aynvora.astro.pipeline.VargaFeatureEngine
import com.aynvora.astro.pipeline.AspectFeatureEngine
import com.aynvora.astro.pipeline.PlanetStateFeatureEngine
import com.aynvora.astro.pipeline.DignityFeatureEngine
import com.aynvora.astro.pipeline.RelationshipFeatureEngine
import com.aynvora.astro.pipeline.ShadbalaFeatureEngine
import com.aynvora.astro.pipeline.AshtakavargaFeatureEngine
import com.aynvora.astro.pipeline.ShodhanaFeatureEngine
import com.aynvora.astro.pipeline.PindaFeatureEngine
import kotlinx.serialization.Serializable

@Serializable
enum class BodyId {
    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    RAHU,
    KETU,
}

@Serializable
data class BodyPosition(
    val bodyId: BodyId,
    val tropicalLongitude: Double,
    val siderealLongitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInRashi: Double,
    val nakshatraIndex: Int,
    val nakshatraName: String,
    val degreeInNakshatra: Double,
    val pada: Int,
    val isRetrograde: Boolean,
    val dailyMotionDegrees: Double,
)

@Serializable
data class EngineCalculationConfig(
    val ayanamsa: String = "LAHIRI_CHITRAPAKSHA",
    val houseSystem: String = "EQUAL_HOUSE",
    val profile: String = "STANDARD_VEDIC",
    val vargaRulesetId: String = "PARASHARA_CLASSICAL_V1",
    val requestedDivisionalCharts: Set<DivisionalChart> = emptySet(),
    val ashtakavargaRulesetId: String = "PARASHARA_CLASSICAL_V1",
)

@Serializable
data class BirthData(
    val dateTimeIso: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null,
)

@Serializable
data class CalculationResult(
    val engineVersion: String,
    val status: String,
    val calculationModel: String = "MEEUS_VSOP87",
    val julianDay: Double = 0.0,
    /** UTC instant produced by the single normalization performed in AstroCalculationContext. */
    val utcTimestamp: String? = null,
    val timezoneOffsetMinutes: Int? = null,
    val julianCenturies: Double = 0.0,
    val ayanamsaDegrees: Double = 0.0,
    val ayanamsaName: String = "LAHIRI_CHITRAPAKSHA",
    val lagna: LagnaPosition? = null,
    val houses: List<HousePosition> = emptyList(),
    val planetHouseOccupancy: Map<BodyId, Int> = emptyMap(),
    val aspects: List<AspectPosition> = emptyList(),
    val planetStates: List<PlanetStatePosition> = emptyList(),
    val positions: List<BodyPosition> = emptyList(),
    val divisionalCharts: Map<DivisionalChart, VargaChartResult> = emptyMap(),
    val planetaryDignities: List<PlanetaryDignityPosition> = emptyList(),
    val planetaryRelationships: List<PlanetaryRelationshipPosition> = emptyList(),
    val shadbala: List<PlanetaryShadbalaPosition> = emptyList(),
    val ashtakavarga: AshtakavargaResult? = null,
    val shodhitaAshtakavarga: ShodhitaAshtakavargaResult? = null,
    val ashtakavargaPinda: AshtakavargaPindaResult? = null,
    val dasha: com.aynvora.astro.dasha.VimshottariDashaTimeline? = null,
)

interface AstroEngine {
    suspend fun calculate(
        birthData: BirthData,
        config: EngineCalculationConfig = EngineCalculationConfig(),
    ): CalculationResult
}

class AynvoraAstroEngine(
    private val vargaEngine: VargaEngine = DefaultVargaEngine(),
    private val timeResolver: (Int, Int, Int, Int, Int, Int, String) -> TimeNormalizer.NormalizedUtcTime = { year, month, day, hour, minute, second, zone ->
        TimeNormalizer.normalize(year, month, day, hour, minute, second, zone)
    },
) : AstroEngine {

    suspend fun calculateFeatures(
        birthData: BirthData,
        config: EngineCalculationConfig,
        featureIds: Set<String>,
    ): com.aynvora.astro.pipeline.FeatureSetCalculation {
        val context = AstroCalculationContext.create(birthData, config, timeResolver = timeResolver)
        val engines = CoreAstroFeatureRegistry.engines(vargaEngine)
        val requested = featureIds.map { id ->
            require(engines.any { it.output.id == id }) { "No engine registered for requested feature '$id'" }
            com.aynvora.astro.pipeline.FeatureKey<Any?>(id)
        }.toSet()
        return AstroFeaturePipeline(engines).calculateFeatures(context, requested)
    }

    override suspend fun calculate(
        birthData: BirthData,
        config: EngineCalculationConfig,
    ): CalculationResult {
        // Shared context normalizes the birth time once; feature outputs are reused downstream.
        val context = AstroCalculationContext.create(birthData, config, timeResolver = timeResolver)
        val features = AstroFeaturePipeline(CoreAstroFeatureRegistry.engines(vargaEngine)).calculate(context)
        val normalizedTime = context.normalizedUtc
        val jd = context.julianDay
        val ayanamsaDegrees = features.get(CoreFeatureKeys.Ayanamsa).value
            ?: error("Ayanamsa feature returned no value")
        val positions = features.get(CoreFeatureKeys.PlanetaryPositions).value
            ?: error("Planetary position feature returned no value")
        val lagna = features.get(CoreFeatureKeys.Lagna).value ?: error("Lagna feature returned no value")
        val houseResult = features.get(CoreFeatureKeys.Houses).value ?: error("House feature returned no value")
        val divisionalCharts = features.get(CoreFeatureKeys.Vargas).value ?: error("Varga feature returned no value")

        val aspects = features.get(CoreFeatureKeys.Aspects).value ?: error("Aspects feature returned no value")
        val planetStates = features.get(CoreFeatureKeys.PlanetStates).value ?: error("Planet states feature returned no value")
        val planetaryDignities = features.get(CoreFeatureKeys.Dignities).value ?: error("Dignities feature returned no value")
        val planetaryRelationships = features.get(CoreFeatureKeys.Relationships).value ?: error("Relationships feature returned no value")
        val shadbala = features.get(CoreFeatureKeys.Shadbala).value ?: error("Shadbala feature returned no value")
        val rawAshtakavarga = features.get(CoreFeatureKeys.Ashtakavarga).value ?: error("Ashtakavarga feature returned no value")
        val shodhitaAshtakavarga = features.get(CoreFeatureKeys.Shodhana).value ?: error("Shodhana feature returned no value")
        val ashtakavargaPinda = features.get(CoreFeatureKeys.Pinda).value ?: error("Pinda feature returned no value")
        val dasha = features.get(CoreFeatureKeys.Dasha).value ?: error("Dasha feature returned no value")

        val ashtakavarga = rawAshtakavarga.copy(
            shodhana = shodhitaAshtakavarga,
            pinda = ashtakavargaPinda,
        )

        return CalculationResult(
            engineVersion = "0.3.0",
            status = "CALCULATED",
            calculationModel = "MEEUS_VSOP87",
            julianDay = jd.value,
            utcTimestamp = "%04d-%02d-%02dT%02d:%02d:%02dZ".format(
                normalizedTime.year, normalizedTime.month, normalizedTime.day,
                normalizedTime.hour, normalizedTime.minute, normalizedTime.second.toInt(),
            ),
            timezoneOffsetMinutes = normalizedTime.timezoneOffsetMinutes,
            julianCenturies = jd.julianCenturiesJ2000,
            ayanamsaDegrees = ayanamsaDegrees,
            ayanamsaName = config.ayanamsa,
            lagna = lagna,
            houses = houseResult.houses,
            planetHouseOccupancy = houseResult.planetHouseOccupancy,
            aspects = aspects,
            planetStates = planetStates,
            positions = positions,
            divisionalCharts = divisionalCharts,
            planetaryDignities = planetaryDignities,
            planetaryRelationships = planetaryRelationships,
            shadbala = shadbala,
            ashtakavarga = ashtakavarga,
            shodhitaAshtakavarga = shodhitaAshtakavarga,
            ashtakavargaPinda = ashtakavargaPinda,
            dasha = dasha,
        )
    }

    companion object {
        internal fun createBodyPosition(
            bodyId: BodyId,
            tropicalLongitude: Double,
            ayanamsaDegrees: Double,
            isRetrograde: Boolean,
            dailyMotion: Double,
        ): BodyPosition {
            val sidereal = ZodiacCalculator.toSidereal(tropicalLongitude, ayanamsaDegrees)
            val rashi = ZodiacCalculator.calculateRashi(sidereal)
            val nakshatra = ZodiacCalculator.calculateNakshatra(sidereal)

            return BodyPosition(
                bodyId = bodyId,
                tropicalLongitude = tropicalLongitude,
                siderealLongitude = sidereal,
                rashiIndex = rashi.index,
                rashiName = rashi.name,
                degreeInRashi = rashi.degreeInRashi,
                nakshatraIndex = nakshatra.index,
                nakshatraName = nakshatra.name,
                degreeInNakshatra = nakshatra.degreeInNakshatra,
                pada = nakshatra.pada,
                isRetrograde = isRetrograde,
                dailyMotionDegrees = dailyMotion,
            )
        }
    }
}

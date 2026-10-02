package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Public calculation result returned by the AYNVORA SDK.
 * Decoupled from internal Astro Engine data structures.
 */
@Serializable
data class ChartResult(
    val engineVersion: String,
    val calculationStatus: String,
    val birthData: BirthData,
    val config: CalculationConfig,
    val calculationModel: String = "MEEUS_VSOP87",
    val julianDay: Double = 0.0,
    val utcTimestamp: String? = null,
    val timezoneOffsetMinutes: Int? = null,
    val ayanamsaDegrees: Double = 0.0,
    val lagna: LagnaDetails? = null,
    val houses: List<HouseDetails> = emptyList(),
    val aspects: List<Aspect> = emptyList(),
    val planetStates: List<PlanetState> = emptyList(),
    val planetaryPositions: List<PlanetaryPosition> = emptyList(),
    val divisionalCharts: Map<DivisionalChart, DivisionalChartResult> = emptyMap(),
    val planetaryDignities: List<PlanetaryDignity> = emptyList(),
    val planetaryRelationships: List<PlanetaryRelationship> = emptyList(),
    val shadbala: List<PlanetaryShadbala> = emptyList(),
    val ashtakavarga: AshtakavargaResult? = null,
    val shodhitaAshtakavarga: ShodhitaAshtakavargaResult? = null,
    val ashtakavargaPinda: AshtakavargaPinda? = null,
    val dashaTimeline: com.aynvora.astro.dasha.VimshottariDashaTimeline? = null,
    val commonChart: com.aynvora.astro.pipeline.AstroChartFeatureResult? = null,
    val grahSthiti: com.aynvora.astro.pipeline.GrahSthitiResult? = null,
    val chalit: com.aynvora.astro.pipeline.ChalitFeatureResult? = null,
    val executionTrace: com.aynvora.astro.pipeline.FeatureExecutionTrace? = null,
    val calculationMetadata: CalculationMetadata = CalculationMetadata(
        calculationProfileId = config.profile.name,
        engineVersion = engineVersion,
        calculationModel = calculationModel,
    ),
)

/** Result of selective execution through the registered core astrology feature pipeline. */
data class AstrologyFeatureCalculation(
    val outputs: Map<String, com.aynvora.astro.pipeline.FeatureOutput<*>>,
    val trace: com.aynvora.astro.pipeline.FeatureExecutionTrace,
)

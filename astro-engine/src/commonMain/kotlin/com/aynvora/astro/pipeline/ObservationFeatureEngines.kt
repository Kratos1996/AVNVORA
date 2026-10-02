package com.aynvora.astro.pipeline

import com.aynvora.astro.BodyId
import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.context.AstroObservationContext
import com.aynvora.astro.houses.HouseCalculationResult
import com.aynvora.astro.panchang.PanchangCalculator
import com.aynvora.astro.panchang.PanchangSnapshot
import com.aynvora.astro.panchang.VaraConvention
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.transit.TransitCalculator
import com.aynvora.astro.transit.TransitInteraction
import com.aynvora.astro.transit.TransitSnapshot
import kotlinx.serialization.Serializable

@Serializable
data class TransitPipelineResult(
    val observation: AstroObservationContext,
    val snapshot: TransitSnapshot,
    val natalHouseByBody: Map<BodyId, Int>,
    val interactionsFromLagna: List<TransitInteraction>,
    val status: FeatureStatus,
    val provenance: CalculationMetadata,
)

@Serializable
data class PanchangPipelineResult(
    val observation: AstroObservationContext,
    val snapshot: PanchangSnapshot,
    val dayDurationMinutes: Int?,
    val status: FeatureStatus,
    val provenance: CalculationMetadata,
)

object TransitFeatureEngine : AstroFeatureEngine<TransitPipelineResult> {
    override val output = CoreFeatureKeys.Transit
    override val supportedCalculationProfiles = setOf("STANDARD_VEDIC")
    override val dependencies = setOf(
        CoreFeatureKeys.TransitObservation,
        CoreFeatureKeys.Lagna,
        CoreFeatureKeys.Houses,
        CoreFeatureKeys.PlanetaryPositions,
    )

    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<TransitPipelineResult> {
        val observation = inputs.get(CoreFeatureKeys.TransitObservation).value
            ?: error("Transit requires an explicit observation context")
        val lagna = inputs.get(CoreFeatureKeys.Lagna).value ?: error("Natal Lagna is unavailable")
        val houses = inputs.get(CoreFeatureKeys.Houses).value ?: error("Natal houses are unavailable")
        val natalPositions = inputs.get(CoreFeatureKeys.PlanetaryPositions).value
            ?: error("Natal planetary positions are unavailable")
        val metadata = context.provenance("transit").copy(conventions = context.provenance("transit").conventions + mapOf(
            "observation_timezone" to observation.timeZoneId,
            "observation_julian_day" to observation.julianDay.toString(),
            "natal_house_system" to houses.houseSystem,
        ))
        val snapshot = TransitCalculator.calculateSnapshot(observation.julianDay, context.config.ayanamsa)
            .copy(calculationMetadata = metadata)
        val natalHouseByBody = snapshot.planetaryPositions.mapNotNull { transitBody ->
            houseForLongitude(transitBody.siderealLongitude, houses)?.let { transitBody.bodyId to it }
        }.toMap()
        val interactions = TransitCalculator.evaluateInteractions(
            snapshot,
            lagna.rashiIndex,
            natalPositions.associate { it.bodyId to it.rashiIndex },
        )
        val result = TransitPipelineResult(observation, snapshot, natalHouseByBody, interactions, FeatureStatus.PARTIAL, metadata)
        return FeatureOutput(
            result,
            FeatureStatus.PARTIAL,
            metadata,
            listOf(FeatureWarning(
                "TRANSIT_TO_NATAL_ASPECTS_UNSUPPORTED",
                "Transit positions and house/sign relationships are calculated. Transit-to-natal aspect rules are not implemented.",
            )),
        )
    }

    private fun houseForLongitude(longitude: Double, houses: HouseCalculationResult): Int? =
        houses.houses.firstOrNull { house ->
            val width = (house.endLongitude - house.startLongitude + 360.0) % 360.0
            val offset = (longitude - house.startLongitude + 360.0) % 360.0
            if (width == 0.0) offset == 0.0 else offset < width
        }?.houseNumber
}

object PanchangFeatureEngine : AstroFeatureEngine<PanchangPipelineResult> {
    override val output = CoreFeatureKeys.Panchang
    override val supportedCalculationProfiles = setOf("STANDARD_VEDIC")
    override val dependencies = setOf(CoreFeatureKeys.PanchangObservation)

    override suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<PanchangPipelineResult> {
        val observation = inputs.get(CoreFeatureKeys.PanchangObservation).value
            ?: error("Panchang requires an explicit observation context")
        val metadata = context.provenance("panchang").copy(conventions = context.provenance("panchang").conventions + mapOf(
            "observation_timezone" to observation.timeZoneId,
            "observation_local_date" to observation.localDate,
            "observation_local_time" to observation.localTime,
        ))
        val snapshot = PanchangCalculator.calculate(
            jd = observation.julianDay,
            ayanamsaConvention = context.config.ayanamsa,
            varaConvention = observation.panchangVaraConvention?.let(VaraConvention::valueOf) ?: VaraConvention.LOCAL_SUNRISE,
            latitudeDeg = observation.latitude,
            longitudeDeg = observation.longitude,
            timezoneOffsetMinutes = observation.timezoneOffsetMinutes,
        ).copy(calculationMetadata = metadata)
        val duration = if (snapshot.sunriseJulianDay != null && snapshot.sunsetJulianDay != null) {
            ((snapshot.sunsetJulianDay - snapshot.sunriseJulianDay) * 1440.0).toInt()
        } else null
        val status = if (duration == null || observation.timezoneDataVersion == null) FeatureStatus.PARTIAL else FeatureStatus.SUPPORTED
        val result = PanchangPipelineResult(observation, snapshot, duration, status, metadata)
        val warnings = buildList {
            if (duration == null) add(FeatureWarning("SOLAR_DAY_UNAVAILABLE", "Sunrise, sunset, or day duration is unavailable for this observation."))
            if (observation.timezoneDataVersion == null) add(FeatureWarning("TIMEZONE_RULES_NOT_VERIFIED", "The observation timezone is outside the curated, versioned timezone rule set."))
        }
        return FeatureOutput(result, status, metadata, warnings)
    }
}

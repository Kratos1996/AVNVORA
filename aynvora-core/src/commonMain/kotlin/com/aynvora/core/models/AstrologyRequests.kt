package com.aynvora.core.models

import com.aynvora.astro.panchang.PanchangSnapshot
import com.aynvora.astro.panchang.VaraConvention
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.transit.TransitSnapshot
import kotlinx.serialization.Serializable
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.result.AynvoraResult
import com.aynvora.astro.time.TimeNormalizer
import com.aynvora.astro.dasha.VimshottariDashaTimeline

/** Dasha is derived from the same calculated natal chart and its unrounded sidereal Moon. */
@Serializable
data class DashaRequest(
    val chartRequest: ChartRequest,
    val targetJulianDay: Double? = null,
    val includeAntardasha: Boolean = true,
    val includePratyantardasha: Boolean = false,
    val rulesetId: String = com.aynvora.astro.dasha.VimshottariDashaCalculator.DEFAULT_RULESET_ID,
)

@Serializable
data class DashaFeatureResult(
    val timeline: VimshottariDashaTimeline,
    val currentMahadasha: com.aynvora.astro.dasha.DashaPeriod?,
    val currentAntardasha: com.aynvora.astro.dasha.DashaPeriod?,
    val currentPratyantardasha: com.aynvora.astro.dasha.DashaPeriod?,
    val status: AstroFeatureStatus,
    val provenance: CalculationMetadata,
)

/** Uses the core chart calculation as the sole source of the birth time and Moon longitude. */
suspend fun AynvoraSdk.calculateDashaRequest(request: DashaRequest): AynvoraResult<DashaFeatureResult> =
    if (request.rulesetId != com.aynvora.astro.dasha.VimshottariDashaCalculator.DEFAULT_RULESET_ID) {
        AynvoraResult.Failure.UnsupportedConfiguration("Unsupported Dasha ruleset '${request.rulesetId}'.")
    } else if (request.targetJulianDay != null && !request.targetJulianDay.isFinite()) {
        AynvoraResult.Failure.InvalidInput("target_julian_day", "Target Julian Day must be finite.")
    } else when (val calculated = calculateChart(request.chartRequest)) {
        is AynvoraResult.Failure -> calculated
        is AynvoraResult.Success -> runCatching {
            val chart = calculated.value
            val moon = chart.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }
                ?: error("Natal chart has no Moon position; Dasha cannot be calculated")
            val timeline = com.aynvora.astro.dasha.VimshottariDashaCalculator.calculate(
                birthJd = chart.julianDay,
                moonSiderealLongitude = moon.siderealLongitude,
                calculateAntardashas = request.includeAntardasha,
                calculatePratyantardashas = request.includePratyantardasha,
                rulesetId = request.rulesetId,
            )
            val target = request.targetJulianDay ?: chart.julianDay
            val (maha, antar) = timeline.findActivePeriodsAt(target)
            val pratyantar = antar?.subPeriods?.firstOrNull { it.contains(target) }
            DashaFeatureResult(timeline, maha, antar, pratyantar, AstroFeatureStatus.SUPPORTED, timeline.calculationMetadata)
        }.fold(
            onSuccess = { AynvoraResult.Success(it, calculationMetadata = it.provenance) },
            onFailure = { AynvoraResult.Failure.CalculationFailure("DASHA_CALCULATION_FAILED", it.message ?: "Dasha calculation failed") },
        )
    }

/** A requested transit instant and observer place. No current-date default is supplied. */
@Serializable
data class TransitRequest(
    val date: BirthDate,
    val time: BirthTime,
    val location: BirthPlace,
    val config: CalculationConfig = CalculationConfig(),
    val natalChart: ChartResult? = null,
    val natalContext: ChartRequest? = null,
)

/** A local civil date and observer place for Panchang. The observation instant is local noon. */
@Serializable
data class PanchangRequest(
    val date: BirthDate,
    val location: BirthPlace,
    val config: CalculationConfig = CalculationConfig(),
    val varaConvention: VaraConvention = VaraConvention.LOCAL_SUNRISE,
)

@Serializable
data class ResolvedAstroInstant(
    val localDate: String,
    val localTime: String,
    val utcTimestamp: String,
    val timezoneId: String,
    val timezoneOffsetMinutes: Int,
    val julianDay: Double,
    val timezoneDataVersion: String? = null,
    val resolutionStatus: AstroResolutionStatus = AstroResolutionStatus.RESOLVED,
)

@Serializable
enum class AstroResolutionStatus { RESOLVED, NOT_VERIFIED, AMBIGUOUS, INVALID_LOCAL_TIME, UNSUPPORTED_TIMEZONE }

@Serializable
enum class AstroFeatureStatus { SUPPORTED, PARTIAL, UNSUPPORTED, NOT_VERIFIED, AMBIGUOUS, FAILED }

@Serializable
data class TransitFeatureResult(
    val instant: ResolvedAstroInstant,
    val snapshot: TransitSnapshot,
    val natalHouseByBody: Map<String, Int> = emptyMap(),
    val status: AstroFeatureStatus = AstroFeatureStatus.PARTIAL,
    val unsupportedFields: List<String> = listOf("natal_aspect_references"),
    val provenance: CalculationMetadata = snapshot.calculationMetadata,
    val interactions: List<com.aynvora.astro.transit.TransitInteraction> = emptyList(),
    val executionTrace: com.aynvora.astro.pipeline.FeatureExecutionTrace? = null,
)

@Serializable
data class PanchangFeatureResult(
    val localDate: String,
    val evaluatedLocalTime: String,
    val location: BirthPlace,
    val snapshot: PanchangSnapshot,
    val dayDurationMinutes: Int? = null,
    val status: AstroFeatureStatus = AstroFeatureStatus.PARTIAL,
    val unsupportedFields: List<String> = emptyList(),
    val provenance: CalculationMetadata = snapshot.calculationMetadata,
    val executionTrace: com.aynvora.astro.pipeline.FeatureExecutionTrace? = null,
)

/** Computes transits for the requested local instant (which may differ from natal birth time). */
suspend fun AynvoraSdk.calculateTransitRequest(request: TransitRequest): AynvoraResult<TransitFeatureResult> =
    runCatching {
        val natalContext = request.natalContext
            ?: request.natalChart?.let { ChartRequest(it.birthData, it.config) }
            ?: error("Transit requires an explicit natal ChartRequest")
        val local = TimeNormalizer.normalizeUnambiguous(
            request.date.year, request.date.month, request.date.day,
            request.time.hour, request.time.minute, request.time.second,
            request.location.timezoneId,
        )
        val observation = createObservationContext(
            request.date.toIsoDateString(), request.time.toIsoTimeString(),
            local.year, local.month, local.day, local.hour, local.minute, local.second,
            local.timezoneOffsetMinutes, request.location, local.julianDay.value,
        )
        val inputs = com.aynvora.astro.pipeline.FeatureOutputs.Empty.withInput(
            com.aynvora.astro.pipeline.CoreFeatureKeys.TransitObservation,
            observation,
            observationInputMetadata(request.config.profile.name, request.config.ayanamsa.name, "transit"),
        )
        when (val calculation = calculateFeaturesWithInputs(
            natalContext.copy(config = request.config),
            setOf(com.aynvora.astro.pipeline.CoreFeatureKeys.Transit.id),
            inputs,
        )) {
            is AynvoraResult.Failure -> error(calculation.message)
            is AynvoraResult.Success -> {
                val feature = calculation.value.outputs[com.aynvora.astro.pipeline.CoreFeatureKeys.Transit.id]
                    ?: error("Transit feature returned no output")
                val value = feature.value as? com.aynvora.astro.pipeline.TransitPipelineResult
                    ?: error("Transit feature returned an incompatible output")
                TransitFeatureResult(
                    instant = observation.toResolvedAstroInstant(),
                    snapshot = value.snapshot,
                    natalHouseByBody = value.natalHouseByBody.mapKeys { it.key.name },
                    status = feature.status.toPublicFeatureStatus(),
                    unsupportedFields = if (feature.warnings.isEmpty()) emptyList() else listOf("transit_to_natal_aspects"),
                    provenance = feature.provenance,
                    interactions = value.interactionsFromLagna,
                    executionTrace = calculation.value.trace,
                )
            }
        }
    }.fold(
        onSuccess = { AynvoraResult.Success(it, calculationMetadata = it.provenance) },
        onFailure = { AynvoraResult.Failure.InvalidInput("date_time_timezone", it.message ?: "Transit instant could not be resolved.") },
    )

/** Evaluates the requested local civil date at noon and also supplies location for sunrise/sunset. */
suspend fun AynvoraSdk.calculatePanchangRequest(request: PanchangRequest): AynvoraResult<PanchangFeatureResult> =
    runCatching {
        val local = TimeNormalizer.normalizeUnambiguous(
            request.date.year, request.date.month, request.date.day, 12, 0, 0,
            request.location.timezoneId,
        )
        val observation = createObservationContext(
            request.date.toIsoDateString(), "12:00:00",
            local.year, local.month, local.day, local.hour, local.minute, local.second,
            local.timezoneOffsetMinutes, request.location, local.julianDay.value,
            request.varaConvention.name,
        )
        val inputs = com.aynvora.astro.pipeline.FeatureOutputs.Empty.withInput(
            com.aynvora.astro.pipeline.CoreFeatureKeys.PanchangObservation,
            observation,
            observationInputMetadata(request.config.profile.name, request.config.ayanamsa.name, "panchang"),
        )
        // The anchor carries calculation settings; the Panchang engine reads only this explicit
        // observation input and has no natal feature dependencies.
        val anchor = ChartRequest(BirthData(request.date, BirthTime(12, 0, 0), request.location), request.config)
        when (val calculation = calculateFeaturesWithInputs(
            anchor, setOf(com.aynvora.astro.pipeline.CoreFeatureKeys.Panchang.id), inputs,
        )) {
            is AynvoraResult.Failure -> error(calculation.message)
            is AynvoraResult.Success -> {
                val feature = calculation.value.outputs[com.aynvora.astro.pipeline.CoreFeatureKeys.Panchang.id]
                    ?: error("Panchang feature returned no output")
                val value = feature.value as? com.aynvora.astro.pipeline.PanchangPipelineResult
                    ?: error("Panchang feature returned an incompatible output")
                PanchangFeatureResult(
                    request.date.toIsoDateString(), "12:00:00", request.location, value.snapshot,
                    value.dayDurationMinutes, feature.status.toPublicFeatureStatus(),
                    unsupportedFields = buildList {
                        if (value.snapshot.sunriseJulianDay == null) add("sunrise")
                        if (value.snapshot.sunsetJulianDay == null) add("sunset")
                        if (value.dayDurationMinutes == null) add("day_duration_minutes")
                        if (timezoneResolutionStatus(request.location.timezoneId) != AstroResolutionStatus.RESOLVED) add("timezone_data_version")
                    },
                    provenance = feature.provenance,
                    executionTrace = calculation.value.trace,
                )
            }
        }
    }.fold(
        onSuccess = { AynvoraResult.Success(it, calculationMetadata = it.provenance) },
        onFailure = { AynvoraResult.Failure.InvalidInput("date_timezone_location", it.message ?: "Panchang request could not be resolved.") },
    )

private fun timezoneResolutionStatus(timezoneId: String): AstroResolutionStatus =
    if (com.aynvora.astro.time.TimeNormalizer.supportsTimezoneId(timezoneId)) AstroResolutionStatus.RESOLVED
    else AstroResolutionStatus.UNSUPPORTED_TIMEZONE

private fun timezoneDataVersion(timezoneId: String): String? =
    if (com.aynvora.astro.time.TimeNormalizer.supportsTimezoneId(timezoneId)) com.aynvora.astro.time.TimeNormalizer.TIMEZONE_DATA_VERSION else null

private fun createObservationContext(
    localDate: String,
    localTime: String,
    utcYear: Int,
    utcMonth: Int,
    utcDay: Int,
    utcHour: Int,
    utcMinute: Int,
    utcSecond: Double,
    offsetMinutes: Int,
    location: BirthPlace,
    julianDay: Double,
    varaConvention: String? = null,
) = com.aynvora.astro.context.AstroObservationContext(
    localDate, localTime,
    "%04d-%02d-%02dT%02d:%02d:%02dZ".format(utcYear, utcMonth, utcDay, utcHour, utcMinute, utcSecond.toInt()),
    location.timezoneId, offsetMinutes, julianDay,
    location.coordinates.latitude, location.coordinates.longitude,
    location.id, location.cityName?.takeIf { it.isNotBlank() } ?: location.name,
    timezoneDataVersion(location.timezoneId), varaConvention,
)

private fun observationInputMetadata(profile: String, ayanamsa: String, feature: String) = CalculationMetadata(
    calculationProfileId = profile,
    calculationModel = "EXPLICIT_OBSERVATION_CONTEXT",
    conventions = mapOf("feature_id" to "input.$feature", "ayanamsa" to ayanamsa),
)

private fun com.aynvora.astro.context.AstroObservationContext.toResolvedAstroInstant() = ResolvedAstroInstant(
    localDate, localTime, utcTimestamp, timeZoneId, timezoneOffsetMinutes, julianDay,
    timezoneDataVersion,
    if (timezoneDataVersion != null) AstroResolutionStatus.RESOLVED else AstroResolutionStatus.UNSUPPORTED_TIMEZONE,
)

private fun com.aynvora.astro.pipeline.FeatureStatus.toPublicFeatureStatus() = when (this) {
    com.aynvora.astro.pipeline.FeatureStatus.SUPPORTED -> AstroFeatureStatus.SUPPORTED
    com.aynvora.astro.pipeline.FeatureStatus.PARTIAL -> AstroFeatureStatus.PARTIAL
    com.aynvora.astro.pipeline.FeatureStatus.UNSUPPORTED -> AstroFeatureStatus.UNSUPPORTED
    com.aynvora.astro.pipeline.FeatureStatus.NOT_VERIFIED -> AstroFeatureStatus.NOT_VERIFIED
    com.aynvora.astro.pipeline.FeatureStatus.AMBIGUOUS -> AstroFeatureStatus.AMBIGUOUS
    com.aynvora.astro.pipeline.FeatureStatus.FAILED -> AstroFeatureStatus.FAILED
}

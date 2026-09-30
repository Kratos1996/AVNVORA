package com.aynvora.core.models

import com.aynvora.astro.panchang.PanchangSnapshot
import com.aynvora.astro.panchang.VaraConvention
import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.transit.TransitSnapshot
import kotlinx.serialization.Serializable
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.result.AynvoraResult
import com.aynvora.astro.time.TimeNormalizer

/** A requested transit instant and observer place. No current-date default is supplied. */
@Serializable
data class TransitRequest(
    val date: BirthDate,
    val time: BirthTime,
    val location: BirthPlace,
    val config: CalculationConfig = CalculationConfig(),
    val natalChart: ChartResult? = null,
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
)

/** Computes transits for the requested local instant (which may differ from natal birth time). */
suspend fun AynvoraSdk.calculateTransitRequest(request: TransitRequest): AynvoraResult<TransitFeatureResult> =
    runCatching {
        val local = TimeNormalizer.normalizeUnambiguous(
            request.date.year, request.date.month, request.date.day,
            request.time.hour, request.time.minute, request.time.second,
            request.location.timezoneId,
        )
        val jd = local.julianDay.value
        val snapshot = calculateTransit(jd, request.config.ayanamsa.name)
        val instant = ResolvedAstroInstant(
            request.date.toIsoDateString(), request.time.toIsoTimeString(),
            "%04d-%02d-%02dT%02d:%02d:%02dZ".format(local.year, local.month, local.day, local.hour, local.minute, local.second.toInt()),
            request.location.timezoneId, local.timezoneOffsetMinutes, jd,
            resolutionStatus = timezoneResolutionStatus(request.location.timezoneId),
        )
        val houses = request.natalChart?.planetaryPositions?.associate { it.body.name to it.houseNumber }.orEmpty()
        TransitFeatureResult(instant, snapshot, houses)
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
        val snapshot = com.aynvora.astro.panchang.PanchangCalculator.calculate(
            local.julianDay.value,
            request.config.ayanamsa.name,
            request.varaConvention,
            request.location.coordinates.latitude,
            request.location.coordinates.longitude,
            local.timezoneOffsetMinutes,
        )
        val sunrise = snapshot.sunriseJulianDay
        val sunset = snapshot.sunsetJulianDay
        val duration = if (sunrise != null && sunset != null) {
            ((sunset - sunrise) * 1440.0).toInt()
        } else null
        PanchangFeatureResult(
            request.date.toIsoDateString(), "12:00:00", request.location, snapshot, duration,
            status = AstroFeatureStatus.PARTIAL,
            unsupportedFields = buildList {
                if (duration == null) add("day_duration_minutes")
                if (timezoneResolutionStatus(request.location.timezoneId) == AstroResolutionStatus.NOT_VERIFIED) add("timezone_data_version")
            },
        )
    }.fold(
        onSuccess = { AynvoraResult.Success(it, calculationMetadata = it.provenance) },
        onFailure = { AynvoraResult.Failure.InvalidInput("date_timezone_location", it.message ?: "Panchang request could not be resolved.") },
    )

private fun timezoneResolutionStatus(timezoneId: String): AstroResolutionStatus =
    if (timezoneId.equals("UTC", true) || timezoneId.equals("GMT", true) || timezoneId == "Z" ||
        timezoneId.startsWith("+") || timezoneId.startsWith("-") ||
        timezoneId.startsWith("UTC+", true) || timezoneId.startsWith("UTC-", true) ||
        timezoneId.startsWith("GMT+", true) || timezoneId.startsWith("GMT-", true)) {
        AstroResolutionStatus.RESOLVED
    } else AstroResolutionStatus.NOT_VERIFIED

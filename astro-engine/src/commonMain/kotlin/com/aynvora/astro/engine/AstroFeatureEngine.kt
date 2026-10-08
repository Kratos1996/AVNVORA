package com.aynvora.astro.engine

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraError
import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraProvenance
import com.aynvora.contracts.AynvoraStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AstrologyLocation(
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val latitude: Double,
    val longitude: Double,
    val timezone: Double = 5.5,
    val timezoneId: String = "Asia/Kolkata",
)

@Serializable
data class AstrologyRequest(
    val name: String = "",
    val dateOfBirth: String, // YYYY-MM-DD
    val timeOfBirth: String, // HH:mm:ss
    val location: AstrologyLocation,
    val gender: String = "UNSPECIFIED",
)

@Serializable
data class PlanetResult(
    val name: String,
    val longitude: Double,
    val sign: String,
    val nakshatra: String,
    val pada: Int,
    val isRetrograde: Boolean = false,
)

@Serializable
data class AstrologyResult(
    val julianDay: Double,
    val lagnaLongitude: Double,
    val lagnaSign: String,
    val planets: List<PlanetResult> = emptyList(),
    val ayanamsaDegrees: Double = 0.0,
    val ayanamsaName: String = "LAHIRI",
    val calculatedAtEpochMs: Long = 0L,
)

/**
 * Independent feature engine implementation for Vedic Astrology.
 * Communicates exclusively through typed events and JSON contracts.
 * Free from UI, Compose, Context, and cross-engine dependencies.
 */
class AstroFeatureEngine(
    private val astroEngine: AstroEngine = AynvoraAstroEngine(),
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false },
) : AynvoraFeatureEngine {

    override val featureId: AynvoraFeatureId = AynvoraFeatureId.ASTROLOGY
    override val version: String = "1.0.0"

    override val capabilities: Set<AynvoraFeatureCapability> = setOf(
        AynvoraFeatureCapability("CALCULATE_CHART", "Deterministic Vedic birth chart calculation", true, true),
        AynvoraFeatureCapability("CALCULATE_PLANETS", "High-precision planetary and lunar nodes calculations", true, true),
        AynvoraFeatureCapability("CALCULATE_VARGA", "16 Divisional harmonic varga charts", true, true),
        AynvoraFeatureCapability("CALCULATE_SHADBALA", "Sixfold planetary potency evaluations", true, true),
        AynvoraFeatureCapability("CALCULATE_ASHTAKAVARGA", "Bindu and Shodhana Ashtakavarga matrices", true, true),
    )

    override suspend fun handle(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()
        if (event.featureId != featureId) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.INVALID_REQUEST,
                messageKey = "error.astrology.mismatched_feature",
            )
        }

        return try {
            val req = json.decodeFromString<AstrologyRequest>(event.payloadJson)
            val dateParts = req.dateOfBirth.split("-").mapNotNull { it.toIntOrNull() }
            val timeParts = req.timeOfBirth.split(":").mapNotNull { it.toIntOrNull() }

            if (dateParts.size != 3 || timeParts.isEmpty()) {
                return AynvoraEventResponse.failure(
                    eventId = event.eventId,
                    requestId = event.requestId,
                    featureId = featureId,
                    status = AynvoraStatus.INVALID_REQUEST,
                    messageKey = "error.astrology.invalid_datetime_format",
                    details = "Expected date YYYY-MM-DD and time HH:mm:ss",
                )
            }

            val year = dateParts[0]
            val month = dateParts[1]
            val day = dateParts[2]
            val hour = timeParts.getOrElse(0) { 12 }
            val minute = timeParts.getOrElse(1) { 0 }
            val second = timeParts.getOrElse(2) { 0 }

            val pad = { n: Int -> if (n < 10) "0$n" else "$n" }
            val iso = "$year-${pad(month)}-${pad(day)}T${pad(hour)}:${pad(minute)}:${pad(second)}"
            val tzId = if (req.location.timezoneId.isNotBlank()) req.location.timezoneId else "Asia/Kolkata"

            val birthData = BirthData(
                dateTimeIso = iso,
                latitude = req.location.latitude,
                longitude = req.location.longitude,
                timeZoneId = tzId,
                year = year,
                month = month,
                day = day,
                hour = hour,
                minute = minute,
                second = second,
                cityName = req.location.city,
                stateName = req.location.state,
                countryName = req.location.country,
            )

            val chart = astroEngine.calculate(birthData, EngineCalculationConfig())

            val planetResults = chart.positions.map { pos ->
                PlanetResult(
                    name = pos.bodyId.name,
                    longitude = pos.siderealLongitude,
                    sign = pos.rashiName,
                    nakshatra = pos.nakshatraName,
                    pada = pos.pada,
                    isRetrograde = pos.isRetrograde,
                )
            }

            val result = AstrologyResult(
                julianDay = chart.julianDay,
                lagnaLongitude = chart.lagna?.siderealLongitude ?: 0.0,
                lagnaSign = chart.lagna?.rashiName ?: "Aries",
                planets = planetResults,
                ayanamsaDegrees = chart.ayanamsaDegrees,
                ayanamsaName = chart.ayanamsaName,
                calculatedAtEpochMs = System.currentTimeMillis(),
            )

            val durationMs = System.currentTimeMillis() - startTime
            val provenance = AynvoraProvenance(
                source = "astro-engine",
                engineId = "com.aynvora.astro",
                engineVersion = version,
                calculationVersion = "1.0.0",
                generatedAtEpochMs = System.currentTimeMillis(),
                isDeterministic = true,
            )

            AynvoraEventResponse.success(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                resultJson = json.encodeToString(result),
                durationMs = durationMs,
                provenance = provenance,
            )
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = featureId,
                status = AynvoraStatus.CALCULATION_FAILED,
                messageKey = "error.astrology.calculation_failed",
                details = e.message ?: "Unknown calculation error",
                durationMs = durationMs,
            )
        }
    }
}

package com.aynvora.core.internal

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData as InternalBirthData
import com.aynvora.astro.BodyId
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.EngineMetadata
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.NakshatraPosition
import com.aynvora.core.models.PlanetaryPosition
import com.aynvora.core.models.Rashi
import com.aynvora.core.models.RashiPosition
import com.aynvora.core.result.AynvoraResult

/**
 * Internal adapter isolating Astro Engine implementation details from the public SDK API.
 */
internal class AstroEngineAdapter(
    private val engine: AstroEngine = AynvoraAstroEngine(),
) {
    private val metadata = EngineMetadata(
        engineVersion = "0.2.0",
        buildNumber = "astro-b2",
        isDeterministic = true,
        supportedDomains = listOf(
            "PLANETARY_POSITIONS",
            "ASTRONOMICAL_TIME",
            "AYANAMSA",
            "RASHI",
            "NAKSHATRA",
            "PADA",
            "RETROGRADE",
        ),
    )

    fun getMetadata(): EngineMetadata = metadata

    suspend fun execute(request: ChartRequest): AynvoraResult<ChartResult> {
        val validationError = validate(request.birthData)
        if (validationError != null) {
            return validationError
        }

        return try {
            val date = request.birthData.date
            val time = request.birthData.time
            val coords = request.birthData.place.coordinates

            val internalBirthData = InternalBirthData(
                dateTimeIso = request.birthData.toIsoDateTimeString(),
                latitude = coords.latitude,
                longitude = coords.longitude,
                timeZoneId = request.birthData.place.timezoneId,
                year = date.year,
                month = date.month,
                day = date.day,
                hour = time.hour,
                minute = time.minute,
                second = time.second,
            )

            val engineConfig = EngineCalculationConfig(
                ayanamsa = request.config.ayanamsa.name,
                houseSystem = request.config.houseSystem.name,
                profile = request.config.profile.name,
            )

            val rawResult = engine.calculate(internalBirthData, engineConfig)

            val publicPositions = rawResult.positions.map { raw ->
                val body = when (raw.bodyId) {
                    BodyId.SUN -> CelestialBody.SUN
                    BodyId.MOON -> CelestialBody.MOON
                    BodyId.MERCURY -> CelestialBody.MERCURY
                    BodyId.VENUS -> CelestialBody.VENUS
                    BodyId.MARS -> CelestialBody.MARS
                    BodyId.JUPITER -> CelestialBody.JUPITER
                    BodyId.SATURN -> CelestialBody.SATURN
                    BodyId.RAHU -> CelestialBody.RAHU
                    BodyId.KETU -> CelestialBody.KETU
                }

                val rashi = Rashi.fromIndex(raw.rashiIndex)
                val nakshatra = Nakshatra.fromIndex(raw.nakshatraIndex)

                PlanetaryPosition(
                    body = body,
                    tropicalLongitude = raw.tropicalLongitude,
                    siderealLongitude = raw.siderealLongitude,
                    rashiPosition = RashiPosition(
                        rashi = rashi,
                        degreeInSign = raw.degreeInRashi,
                        totalSiderealLongitude = raw.siderealLongitude,
                    ),
                    nakshatraPosition = NakshatraPosition(
                        nakshatra = nakshatra,
                        degreeInNakshatra = raw.degreeInNakshatra,
                        pada = raw.pada,
                    ),
                    isRetrograde = raw.isRetrograde,
                    dailyMotionDegrees = raw.dailyMotionDegrees,
                )
            }

            AynvoraResult.Success(
                value = ChartResult(
                    engineVersion = rawResult.engineVersion,
                    calculationStatus = rawResult.status,
                    birthData = request.birthData,
                    config = request.config,
                    calculationModel = rawResult.calculationModel,
                    julianDay = rawResult.julianDay,
                    ayanamsaDegrees = rawResult.ayanamsaDegrees,
                    planetaryPositions = publicPositions,
                ),
                metadata = metadata.copy(engineVersion = rawResult.engineVersion),
            )
        } catch (e: UnsupportedOperationException) {
            AynvoraResult.Failure.UnsupportedConfiguration(
                message = e.message ?: "The requested astrological configuration is unsupported.",
            )
        } catch (e: Exception) {
            AynvoraResult.Failure.InternalFailure(
                message = e.message ?: "Calculation terminated due to an unexpected internal error.",
            )
        }
    }

    private fun validate(birthData: BirthData): AynvoraResult.Failure.InvalidInput? {
        val date = birthData.date
        val time = birthData.time
        val coords = birthData.place.coordinates

        return when {
            date.year !in 1..9999 -> AynvoraResult.Failure.InvalidInput("year", "Year must be between 1 and 9999.")
            date.month !in 1..12 -> AynvoraResult.Failure.InvalidInput("month", "Month must be between 1 and 12.")
            date.day !in 1..31 -> AynvoraResult.Failure.InvalidInput("day", "Day must be between 1 and 31.")
            time.hour !in 0..23 -> AynvoraResult.Failure.InvalidInput("hour", "Hour must be between 0 and 23.")
            time.minute !in 0..59 -> AynvoraResult.Failure.InvalidInput("minute", "Minute must be between 0 and 59.")
            time.second !in 0..59 -> AynvoraResult.Failure.InvalidInput("second", "Second must be between 0 and 59.")
            coords.latitude !in -90.0..90.0 -> AynvoraResult.Failure.InvalidInput("latitude", "Latitude must be between -90.0 and 90.0.")
            coords.longitude !in -180.0..180.0 -> AynvoraResult.Failure.InvalidInput("longitude", "Longitude must be between -180.0 and 180.0.")
            birthData.place.timezoneId.isBlank() -> AynvoraResult.Failure.InvalidInput("timezoneId", "Timezone cannot be blank.")
            else -> null
        }
    }
}

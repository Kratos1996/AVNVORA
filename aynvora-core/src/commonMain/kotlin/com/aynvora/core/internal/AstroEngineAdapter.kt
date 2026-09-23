package com.aynvora.core.internal

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData as InternalBirthData
import com.aynvora.astro.BodyId
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.core.models.Aspect
import com.aynvora.core.models.AspectType
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.CombustionState
import com.aynvora.core.models.EngineMetadata
import com.aynvora.core.models.HouseDetails
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.LagnaDetails
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.NakshatraPosition
import com.aynvora.core.models.PlanetMotionState
import com.aynvora.core.models.PlanetState
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
        engineVersion = "0.3.0",
        buildNumber = "astro-b3",
        isDeterministic = true,
        supportedDomains = listOf(
            "PLANETARY_POSITIONS",
            "ASTRONOMICAL_TIME",
            "AYANAMSA",
            "RASHI",
            "NAKSHATRA",
            "PADA",
            "RETROGRADE",
            "ASCENDANT_LAGNA",
            "HOUSES_BHAVAS",
            "HOUSE_OCCUPANCY",
            "PLANETARY_ASPECTS",
            "CONJUNCTIONS",
            "COMBUSTION",
            "PLANET_STATES",
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

            val planetStatesMap = rawResult.planetStates.associateBy { it.bodyId }

            val publicPositions = rawResult.positions.map { raw ->
                val body = mapBodyId(raw.bodyId)
                val rashi = Rashi.fromIndex(raw.rashiIndex)
                val nakshatra = Nakshatra.fromIndex(raw.nakshatraIndex)
                val houseNumber = rawResult.planetHouseOccupancy[raw.bodyId] ?: 1

                val statePos = planetStatesMap[raw.bodyId]
                val motionState = if (statePos != null) {
                    mapMotionState(statePos.motionState)
                } else if (raw.isRetrograde) {
                    PlanetMotionState.RETROGRADE
                } else {
                    PlanetMotionState.DIRECT
                }

                val combustionState = if (statePos != null) {
                    mapCombustionState(statePos.combustionState)
                } else {
                    CombustionState.NORMAL
                }

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
                    houseNumber = houseNumber,
                    motionState = motionState,
                    combustionState = combustionState,
                    isRetrograde = raw.isRetrograde,
                    dailyMotionDegrees = raw.dailyMotionDegrees,
                )
            }

            val publicLagna = rawResult.lagna?.let { rawLagna ->
                val rashi = Rashi.fromIndex(rawLagna.rashiIndex)
                val nakshatra = Nakshatra.fromIndex(rawLagna.nakshatraIndex)

                LagnaDetails(
                    tropicalLongitude = rawLagna.tropicalLongitude,
                    siderealLongitude = rawLagna.siderealLongitude,
                    rashiPosition = RashiPosition(
                        rashi = rashi,
                        degreeInSign = rawLagna.degreeInRashi,
                        totalSiderealLongitude = rawLagna.siderealLongitude,
                    ),
                    nakshatraPosition = NakshatraPosition(
                        nakshatra = nakshatra,
                        degreeInNakshatra = rawLagna.degreeInNakshatra,
                        pada = rawLagna.pada,
                    ),
                    localSiderealTimeDegrees = rawLagna.localSiderealTimeDegrees,
                    obliquityDegrees = rawLagna.obliquityDegrees,
                    midheavenTropicalLongitude = rawLagna.midheavenTropicalLongitude,
                    midheavenSiderealLongitude = rawLagna.midheavenSiderealLongitude,
                )
            }

            val publicHouses = rawResult.houses.map { rawHouse ->
                val houseSystem = when (rawHouse.houseSystem.uppercase()) {
                    "WHOLE_SIGN" -> HouseSystem.WHOLE_SIGN
                    "EQUAL_HOUSE" -> HouseSystem.EQUAL_HOUSE
                    "PLACIDUS" -> HouseSystem.PLACIDUS
                    else -> request.config.houseSystem
                }
                val rashi = Rashi.fromIndex(rawHouse.rashiIndex)

                HouseDetails(
                    houseNumber = rawHouse.houseNumber,
                    system = houseSystem,
                    cuspLongitude = rawHouse.cuspLongitude,
                    startLongitude = rawHouse.startLongitude,
                    endLongitude = rawHouse.endLongitude,
                    rashiPosition = RashiPosition(
                        rashi = rashi,
                        degreeInSign = rawHouse.degreeInRashi,
                        totalSiderealLongitude = rawHouse.cuspLongitude,
                    ),
                )
            }

            val publicAspects = rawResult.aspects.map { rawAspect ->
                Aspect(
                    firstBody = mapBodyId(rawAspect.firstBody),
                    secondBody = mapBodyId(rawAspect.secondBody),
                    type = mapAspectType(rawAspect.type),
                    exactAngle = rawAspect.exactAngle,
                    actualSeparation = rawAspect.actualSeparation,
                    orb = rawAspect.orb,
                )
            }

            val publicPlanetStates = rawResult.planetStates.map { rawState ->
                PlanetState(
                    body = mapBodyId(rawState.bodyId),
                    motionState = mapMotionState(rawState.motionState),
                    combustionState = mapCombustionState(rawState.combustionState),
                    separationFromSun = rawState.separationFromSun,
                    combustionThresholdDegrees = rawState.combustionThresholdDegrees,
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
                    lagna = publicLagna,
                    houses = publicHouses,
                    aspects = publicAspects,
                    planetStates = publicPlanetPlanetStatesCheck(publicPlanetStates),
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

    private fun publicPlanetPlanetStatesCheck(states: List<PlanetState>): List<PlanetState> = states

    private fun mapBodyId(id: BodyId): CelestialBody = when (id) {
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

    private fun mapAspectType(type: com.aynvora.astro.aspects.AspectType): AspectType = when (type) {
        com.aynvora.astro.aspects.AspectType.CONJUNCTION -> AspectType.CONJUNCTION
        com.aynvora.astro.aspects.AspectType.SEXTILE -> AspectType.SEXTILE
        com.aynvora.astro.aspects.AspectType.SQUARE -> AspectType.SQUARE
        com.aynvora.astro.aspects.AspectType.TRINE -> AspectType.TRINE
        com.aynvora.astro.aspects.AspectType.OPPOSITION -> AspectType.OPPOSITION
    }

    private fun mapMotionState(state: com.aynvora.astro.states.PlanetMotionState): PlanetMotionState = when (state) {
        com.aynvora.astro.states.PlanetMotionState.DIRECT -> PlanetMotionState.DIRECT
        com.aynvora.astro.states.PlanetMotionState.RETROGRADE -> PlanetMotionState.RETROGRADE
    }

    private fun mapCombustionState(state: com.aynvora.astro.states.CombustionState): CombustionState = when (state) {
        com.aynvora.astro.states.CombustionState.NORMAL -> CombustionState.NORMAL
        com.aynvora.astro.states.CombustionState.COMBUST -> CombustionState.COMBUST
        com.aynvora.astro.states.CombustionState.NOT_APPLICABLE -> CombustionState.NOT_APPLICABLE
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

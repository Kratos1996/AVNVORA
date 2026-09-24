package com.aynvora.core.internal

import com.aynvora.astro.AstroEngine
import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData as InternalBirthData
import com.aynvora.astro.BodyId
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.astro.dignity.DignityType as InternalDignityType
import com.aynvora.astro.dignity.PlanetaryDignityCalculator
import com.aynvora.astro.dignity.PlanetaryDignityPosition as InternalPlanetaryDignityPosition
import com.aynvora.astro.relationship.CompoundRelationshipType as InternalCompoundRelationshipType
import com.aynvora.astro.relationship.NaturalRelationshipType as InternalNaturalRelationshipType
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.relationship.PlanetaryRelationshipPosition as InternalPlanetaryRelationshipPosition
import com.aynvora.astro.relationship.TemporaryRelationshipType as InternalTemporaryRelationshipType
import com.aynvora.astro.shadbala.ChestaBalaPosition as InternalChestaBalaPosition
import com.aynvora.astro.shadbala.DigBalaPosition as InternalDigBalaPosition
import com.aynvora.astro.shadbala.DrikBalaPosition as InternalDrikBalaPosition
import com.aynvora.astro.shadbala.KalaBalaPosition as InternalKalaBalaPosition
import com.aynvora.astro.shadbala.NaisargikaBalaPosition as InternalNaisargikaBalaPosition
import com.aynvora.astro.shadbala.PlanetaryShadbalaPosition as InternalPlanetaryShadbalaPosition
import com.aynvora.astro.shadbala.ShadbalaCompleteness as InternalShadbalaCompleteness
import com.aynvora.astro.shadbala.SthanaBalaPosition as InternalSthanaBalaPosition
import com.aynvora.astro.ashtakavarga.AshtakavargaCompleteness as InternalAshtakavargaCompleteness
import com.aynvora.astro.ashtakavarga.AshtakavargaContributor as InternalAshtakavargaContributor
import com.aynvora.astro.ashtakavarga.AshtakavargaResult as InternalAshtakavargaResult
import com.aynvora.astro.ashtakavarga.BhinnashtakavargaChart as InternalBhinnashtakavargaChart
import com.aynvora.astro.ashtakavarga.BhinnashtakavargaSignScore as InternalBhinnashtakavargaSignScore
import com.aynvora.astro.ashtakavarga.SarvashtakavargaChart as InternalSarvashtakavargaChart
import com.aynvora.astro.ashtakavarga.SarvashtakavargaSignScore as InternalSarvashtakavargaSignScore
import com.aynvora.astro.ashtakavarga.ShodhitaAshtakavargaResult as InternalShodhitaAshtakavargaResult
import com.aynvora.astro.ashtakavarga.ShodhitaBhinnashtakavargaChart as InternalShodhitaBhinnashtakavargaChart
import com.aynvora.astro.ashtakavarga.ShodhitaBhinnashtakavargaSignScore as InternalShodhitaBhinnashtakavargaSignScore
import com.aynvora.astro.ashtakavarga.ShodhitaSarvashtakavargaChart as InternalShodhitaSarvashtakavargaChart
import com.aynvora.astro.ashtakavarga.ShodhitaSarvashtakavargaSignScore as InternalShodhitaSarvashtakavargaSignScore
import com.aynvora.astro.ashtakavarga.AshtakavargaPindaResult as InternalAshtakavargaPindaResult
import com.aynvora.astro.ashtakavarga.PlanetaryPindaResult as InternalPlanetaryPindaResult
import com.aynvora.astro.varga.DivisionalChart as InternalDivisionalChart
import com.aynvora.astro.varga.VargaChartResult
import com.aynvora.astro.varga.VargaPosition
import com.aynvora.core.models.AshtakavargaCompleteness
import com.aynvora.core.models.AshtakavargaContributor
import com.aynvora.core.models.AshtakavargaResult
import com.aynvora.core.models.Bhinnashtakavarga
import com.aynvora.core.models.BhinnashtakavargaSignScore
import com.aynvora.core.models.Sarvashtakavarga
import com.aynvora.core.models.SarvashtakavargaSignScore
import com.aynvora.core.models.ShodhitaAshtakavargaResult
import com.aynvora.core.models.ShodhitaBhinnashtakavarga
import com.aynvora.core.models.ShodhitaBhinnashtakavargaSignScore
import com.aynvora.core.models.ShodhitaSarvashtakavarga
import com.aynvora.core.models.ShodhitaSarvashtakavargaSignScore
import com.aynvora.core.models.AshtakavargaPinda
import com.aynvora.core.models.PlanetaryPinda
import com.aynvora.core.models.Aspect
import com.aynvora.core.models.AspectType
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.ChestaBala
import com.aynvora.core.models.CombustionState
import com.aynvora.core.models.CompoundRelationshipType
import com.aynvora.core.models.DigBala
import com.aynvora.core.models.DignityType
import com.aynvora.core.models.DivisionalChart
import com.aynvora.core.models.DivisionalChartResult
import com.aynvora.core.models.DivisionalPosition
import com.aynvora.core.models.DrikBala
import com.aynvora.core.models.EngineMetadata
import com.aynvora.core.models.HouseDetails
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.KalaBala
import com.aynvora.core.models.LagnaDetails
import com.aynvora.core.models.NaisargikaBala
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.NakshatraPosition
import com.aynvora.core.models.NaturalRelationshipType
import com.aynvora.core.models.PlanetMotionState
import com.aynvora.core.models.PlanetState
import com.aynvora.core.models.PlanetaryDignity
import com.aynvora.core.models.PlanetaryPosition
import com.aynvora.core.models.PlanetaryRelationship
import com.aynvora.core.models.PlanetaryShadbala
import com.aynvora.core.models.Rashi
import com.aynvora.core.models.RashiPosition
import com.aynvora.core.models.ShadbalaCompleteness
import com.aynvora.core.models.SthanaBala
import com.aynvora.core.models.TemporaryRelationshipType
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
            "DIVISIONAL_CHARTS_VARGAS",
            "D1_RASHI",
            "D2_HORA",
            "D3_DREKKANA",
            "D4_CHATURTHAMSA",
            "D7_SAPTAMSA",
            "D9_NAVAMSA",
            "D10_DASAMSA",
            "D12_DWADASAMSA",
            "D16_SHODASAMSA",
            "D20_VIMSAMSA",
            "D24_CHATURVIMSAMSA",
            "D27_BHAMSA",
            "D30_TRIMSAMSA",
            "D40_KHAVEDAMSA",
            "D45_AKSHAVEDAMSA",
            "D60_SHASHTIAMSA",
            "PLANETARY_DIGNITIES",
            "EXALTATION",
            "DEBILITATION",
            "MOOLATRIKONA",
            "OWN_SIGN",
            "NATURAL_RELATIONSHIPS",
            "TEMPORARY_RELATIONSHIPS",
            "COMPOUND_RELATIONSHIPS",
            "PANCHADHA_MAITRI",
            "SHADBALA",
            "STHANA_BALA",
            "DIG_BALA",
            "NAISARGIKA_BALA",
            "UCHCHA_BALA",
            "SAPTAVARGAJA_BALA",
            "KENDRA_BALA",
            "DREKKANA_BALA",
            "OJHAYUGMARASYAMSA_BALA",
            "KALA_BALA",
            "CHESTA_BALA",
            "DRIK_BALA",
            "NATHONNATHA_BALA",
            "PAKSHA_BALA",
            "TRIBHAGA_BALA",
            "VARA_BALA",
            "HORA_BALA",
            "MASA_BALA",
            "VARSHA_BALA",
            "AYANA_BALA",
            "YUDDHA_BALA",
            "CHESTA_KENDRA",
            "DRISHTI_BALA",
            "ASHTAKAVARGA",
            "BHINNASHTAKAVARGA",
            "SARVASHTAKAVARGA",
            "PRASTARASHTAKAVARGA",
            "SURYA_ASHTAKAVARGA",
            "CHANDRA_ASHTAKAVARGA",
            "KUJA_ASHTAKAVARGA",
            "BUDHA_ASHTAKAVARGA",
            "GURU_ASHTAKAVARGA",
            "SHUKRA_ASHTAKAVARGA",
            "SHANI_ASHTAKAVARGA",
            "TRIKONA_SHODHANA",
            "EKADHIPATYA_SHODHANA",
            "SHODHITA_ASHTAKAVARGA",
            "ASHTAKAVARGA_PINDA",
            "RASHI_PINDA",
            "GRAHA_PINDA",
            "SHODHYA_PINDA",
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
                vargaRulesetId = request.config.vargaRulesetId,
                requestedDivisionalCharts = request.config.requestedDivisionalCharts.map { mapDivisionalChart(it) }.toSet(),
                ashtakavargaRulesetId = request.config.ashtakavargaRulesetId,
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

            val publicDivisionalCharts = rawResult.divisionalCharts.map { (chart, result) ->
                mapInternalDivisionalChart(chart) to mapVargaChartResult(result)
            }.toMap()

            val publicDignities = rawResult.planetaryDignities.map { mapPlanetaryDignity(it) }
            val publicRelationships = rawResult.planetaryRelationships.map { mapPlanetaryRelationship(it) }
            val publicShadbala = rawResult.shadbala.map { mapPlanetaryShadbala(it) }
            val publicAshtakavarga = rawResult.ashtakavarga?.let { mapAshtakavargaResult(it) }
            val publicShodhitaAshtakavarga = rawResult.shodhitaAshtakavarga?.let { mapShodhitaAshtakavargaResult(it) }
            val publicAshtakavargaPinda = rawResult.ashtakavargaPinda?.let { mapAshtakavargaPindaResult(it) }

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
                    divisionalCharts = publicDivisionalCharts,
                    planetaryDignities = publicDignities,
                    planetaryRelationships = publicRelationships,
                    shadbala = publicShadbala,
                    ashtakavarga = publicAshtakavarga,
                    shodhitaAshtakavarga = publicShodhitaAshtakavarga,
                    ashtakavargaPinda = publicAshtakavargaPinda,
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

    private fun mapDivisionalChart(chart: DivisionalChart): InternalDivisionalChart =
        InternalDivisionalChart.valueOf(chart.name)

    private fun mapInternalDivisionalChart(chart: InternalDivisionalChart): DivisionalChart =
        DivisionalChart.valueOf(chart.name)

    private fun mapVargaChartResult(raw: VargaChartResult): DivisionalChartResult {
        val lagnaPos = raw.lagnaPosition?.let { mapVargaPosition(it) }
        val positions = raw.positions.map { mapVargaPosition(it) }
        return DivisionalChartResult(
            chart = mapInternalDivisionalChart(raw.chart),
            rulesetId = raw.rulesetId,
            isSupported = raw.isSupported,
            lagnaPosition = lagnaPos,
            positions = positions,
        )
    }

    private fun mapVargaPosition(raw: VargaPosition): DivisionalPosition {
        val body = raw.bodyId?.let { mapBodyId(it) }
        val sourceRashi = Rashi.fromIndex(raw.sourceRashiIndex)
        val resultingRashi = Rashi.fromIndex(raw.resultingRashiIndex)
        return DivisionalPosition(
            body = body,
            isLagna = raw.isLagna,
            sourceLongitude = raw.sourceLongitude,
            sourceRashi = sourceRashi,
            divisionIndex = raw.divisionIndex,
            resultingRashi = resultingRashi,
            degreeInResultingRashi = raw.degreeInResultingRashi,
            resultingLongitude = raw.resultingLongitude,
        )
    }

    suspend fun executeDivisionalChart(
        request: ChartRequest,
        chart: DivisionalChart,
    ): AynvoraResult<DivisionalChartResult> {
        val updatedRequest = request.copy(
            config = request.config.copy(
                requestedDivisionalCharts = setOf(chart),
            ),
        )
        return when (val chartResult = execute(updatedRequest)) {
            is AynvoraResult.Success -> {
                val varga = chartResult.value.divisionalCharts[chart]
                if (varga != null) {
                    AynvoraResult.Success(varga, chartResult.metadata)
                } else {
                    AynvoraResult.Failure.CalculationFailure(
                        code = "VARGA_NOT_CALCULATED",
                        message = "Calculation did not yield divisional chart ${chart.name}.",
                    )
                }
            }
            is AynvoraResult.Failure -> chartResult
        }
    }

    suspend fun executeDivisionalCharts(
        request: ChartRequest,
        charts: Set<DivisionalChart>,
    ): AynvoraResult<Map<DivisionalChart, DivisionalChartResult>> {
        val updatedRequest = request.copy(
            config = request.config.copy(
                requestedDivisionalCharts = charts,
            ),
        )
        return when (val chartResult = execute(updatedRequest)) {
            is AynvoraResult.Success -> AynvoraResult.Success(chartResult.value.divisionalCharts, chartResult.metadata)
            is AynvoraResult.Failure -> chartResult
        }
    }

    suspend fun executeDignities(
        request: ChartRequest,
        chart: DivisionalChart = DivisionalChart.D1,
    ): AynvoraResult<List<PlanetaryDignity>> {
        return if (chart == DivisionalChart.D1) {
            when (val chartResult = execute(request)) {
                is AynvoraResult.Success -> AynvoraResult.Success(chartResult.value.planetaryDignities, chartResult.metadata)
                is AynvoraResult.Failure -> chartResult
            }
        } else {
            when (val vargaResult = executeDivisionalChart(request, chart)) {
                is AynvoraResult.Success -> {
                    val planetPositions = vargaResult.value.positions.filter { it.body != null && !it.isLagna }
                    val signMap = planetPositions.associate { mapCelestialBodyToBodyId(it.body!!) to it.resultingRashi.index }
                    val internalChart = mapDivisionalChart(chart)
                    val dignities = planetPositions.map { pos ->
                        val bodyId = mapCelestialBodyToBodyId(pos.body!!)
                        val internalDignity = PlanetaryDignityCalculator.evaluateDignity(
                            bodyId = bodyId,
                            rashiIndex = pos.resultingRashi.index,
                            degreeInSign = pos.degreeInResultingRashi,
                            signMap = signMap,
                            chart = internalChart,
                        )
                        mapPlanetaryDignity(internalDignity)
                    }
                    AynvoraResult.Success(dignities, vargaResult.metadata)
                }
                is AynvoraResult.Failure -> vargaResult
            }
        }
    }

    suspend fun executeRelationships(
        request: ChartRequest,
        chart: DivisionalChart = DivisionalChart.D1,
    ): AynvoraResult<List<PlanetaryRelationship>> {
        return if (chart == DivisionalChart.D1) {
            when (val chartResult = execute(request)) {
                is AynvoraResult.Success -> AynvoraResult.Success(chartResult.value.planetaryRelationships, chartResult.metadata)
                is AynvoraResult.Failure -> chartResult
            }
        } else {
            when (val vargaResult = executeDivisionalChart(request, chart)) {
                is AynvoraResult.Success -> {
                    val planetPositions = vargaResult.value.positions.filter { it.body != null && !it.isLagna }
                    val signMap = planetPositions.associate { mapCelestialBodyToBodyId(it.body!!) to it.resultingRashi.index }
                    val internalChart = mapDivisionalChart(chart)
                    val internalRelationships = PlanetaryRelationshipCalculator.calculateRelationshipsFromSignMap(
                        signMap = signMap,
                        chart = internalChart,
                    )
                    val publicRelationships = internalRelationships.map { mapPlanetaryRelationship(it) }
                    AynvoraResult.Success(publicRelationships, vargaResult.metadata)
                }
                is AynvoraResult.Failure -> vargaResult
            }
        }
    }

    suspend fun executeShadbala(
        request: ChartRequest,
    ): AynvoraResult<List<PlanetaryShadbala>> {
        return when (val chartResult = execute(request)) {
            is AynvoraResult.Success -> AynvoraResult.Success(chartResult.value.shadbala, chartResult.metadata)
            is AynvoraResult.Failure -> chartResult
        }
    }

    suspend fun executeAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<AshtakavargaResult> {
        return when (val chartResult = execute(request)) {
            is AynvoraResult.Success -> {
                val av = chartResult.value.ashtakavarga
                if (av != null) {
                    AynvoraResult.Success(av, chartResult.metadata)
                } else {
                    AynvoraResult.Failure.CalculationFailure(
                        code = "ASHTAKAVARGA_NOT_CALCULATED",
                        message = "Ashtakavarga calculation was not produced for the provided chart inputs.",
                    )
                }
            }
            is AynvoraResult.Failure -> chartResult
        }
    }

    suspend fun executeShodhitaAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<ShodhitaAshtakavargaResult> {
        return when (val chartResult = execute(request)) {
            is AynvoraResult.Success -> {
                val sav = chartResult.value.shodhitaAshtakavarga
                if (sav != null) {
                    AynvoraResult.Success(sav, chartResult.metadata)
                } else {
                    AynvoraResult.Failure.CalculationFailure(
                        code = "SHODHITA_ASHTAKAVARGA_NOT_CALCULATED",
                        message = "Shodhita Ashtakavarga calculation was not produced for the provided chart inputs.",
                    )
                }
            }
            is AynvoraResult.Failure -> chartResult
        }
    }

    suspend fun executeAshtakavargaPinda(
        request: ChartRequest,
    ): AynvoraResult<AshtakavargaPinda> {
        return when (val chartResult = execute(request)) {
            is AynvoraResult.Success -> {
                val pinda = chartResult.value.ashtakavargaPinda
                if (pinda != null) {
                    AynvoraResult.Success(pinda, chartResult.metadata)
                } else {
                    AynvoraResult.Failure.CalculationFailure(
                        code = "ASHTAKAVARGA_PINDA_NOT_CALCULATED",
                        message = "Ashtakavarga Pinda calculation was not produced for the provided chart inputs.",
                    )
                }
            }
            is AynvoraResult.Failure -> chartResult
        }
    }

    private fun mapDignityType(type: InternalDignityType): DignityType = when (type) {
        InternalDignityType.EXALTATION -> DignityType.EXALTATION
        InternalDignityType.DEBILITATION -> DignityType.DEBILITATION
        InternalDignityType.MOOLATRIKONA -> DignityType.MOOLATRIKONA
        InternalDignityType.OWN_SIGN -> DignityType.OWN_SIGN
        InternalDignityType.GREAT_FRIEND_SIGN -> DignityType.GREAT_FRIEND_SIGN
        InternalDignityType.FRIEND_SIGN -> DignityType.FRIEND_SIGN
        InternalDignityType.NEUTRAL_SIGN -> DignityType.NEUTRAL_SIGN
        InternalDignityType.ENEMY_SIGN -> DignityType.ENEMY_SIGN
        InternalDignityType.GREAT_ENEMY_SIGN -> DignityType.GREAT_ENEMY_SIGN
        InternalDignityType.NOT_APPLICABLE -> DignityType.NOT_APPLICABLE
    }

    private fun mapPlanetaryDignity(raw: InternalPlanetaryDignityPosition): PlanetaryDignity =
        PlanetaryDignity(
            body = mapBodyId(raw.bodyId),
            chart = mapInternalDivisionalChart(raw.chart),
            rashi = Rashi.fromIndex(raw.sourceRashiIndex),
            signLord = raw.signLordBodyId?.let { mapBodyId(it) },
            dignityType = mapDignityType(raw.dignityType),
            isExalted = raw.isExalted,
            isDebilitated = raw.isDebilitated,
            isMoolatrikona = raw.isMoolatrikona,
            isOwnSign = raw.isOwnSign,
            deepExaltationDegree = raw.deepExaltationDegree,
            deepDebilitationDegree = raw.deepDebilitationDegree,
            degreeInSign = raw.degreeInSign,
            ruleId = raw.ruleId,
        )

    private fun mapNaturalRelationship(type: InternalNaturalRelationshipType): NaturalRelationshipType = when (type) {
        InternalNaturalRelationshipType.FRIEND -> NaturalRelationshipType.FRIEND
        InternalNaturalRelationshipType.NEUTRAL -> NaturalRelationshipType.NEUTRAL
        InternalNaturalRelationshipType.ENEMY -> NaturalRelationshipType.ENEMY
        InternalNaturalRelationshipType.NOT_APPLICABLE -> NaturalRelationshipType.NOT_APPLICABLE
    }

    private fun mapTemporaryRelationship(type: InternalTemporaryRelationshipType): TemporaryRelationshipType = when (type) {
        InternalTemporaryRelationshipType.FRIEND -> TemporaryRelationshipType.FRIEND
        InternalTemporaryRelationshipType.ENEMY -> TemporaryRelationshipType.ENEMY
        InternalTemporaryRelationshipType.NOT_APPLICABLE -> TemporaryRelationshipType.NOT_APPLICABLE
    }

    private fun mapCompoundRelationship(type: InternalCompoundRelationshipType): CompoundRelationshipType = when (type) {
        InternalCompoundRelationshipType.GREAT_FRIEND -> CompoundRelationshipType.GREAT_FRIEND
        InternalCompoundRelationshipType.FRIEND -> CompoundRelationshipType.FRIEND
        InternalCompoundRelationshipType.NEUTRAL -> CompoundRelationshipType.NEUTRAL
        InternalCompoundRelationshipType.ENEMY -> CompoundRelationshipType.ENEMY
        InternalCompoundRelationshipType.GREAT_ENEMY -> CompoundRelationshipType.GREAT_ENEMY
        InternalCompoundRelationshipType.NOT_APPLICABLE -> CompoundRelationshipType.NOT_APPLICABLE
    }

    private fun mapPlanetaryRelationship(raw: InternalPlanetaryRelationshipPosition): PlanetaryRelationship =
        PlanetaryRelationship(
            sourceBody = mapBodyId(raw.sourceBodyId),
            targetBody = mapBodyId(raw.targetBodyId),
            chart = mapInternalDivisionalChart(raw.chart),
            naturalRelationship = mapNaturalRelationship(raw.naturalRelationship),
            temporaryRelationship = mapTemporaryRelationship(raw.temporaryRelationship),
            compoundRelationship = mapCompoundRelationship(raw.compoundRelationship),
            sourceRashi = Rashi.fromIndex(raw.sourceRashiIndex),
            targetRashi = Rashi.fromIndex(raw.targetRashiIndex),
            relativeHouseDistance = raw.relativeHouseDistance,
        )

    private fun mapShadbalaCompleteness(raw: InternalShadbalaCompleteness): ShadbalaCompleteness = when (raw) {
        InternalShadbalaCompleteness.COMPLETE -> ShadbalaCompleteness.COMPLETE
        InternalShadbalaCompleteness.PARTIAL_FOUNDATION -> ShadbalaCompleteness.PARTIAL_FOUNDATION
        InternalShadbalaCompleteness.UNSUPPORTED -> ShadbalaCompleteness.UNSUPPORTED
    }

    private fun mapSthanaBala(raw: InternalSthanaBalaPosition): SthanaBala =
        SthanaBala(
            uchchaBalaVirupas = raw.uchchaBalaVirupas,
            saptavargajaBalaVirupas = raw.saptavargajaBalaVirupas,
            ojhayugmarasyamsaBalaVirupas = raw.ojhayugmarasyamsaBalaVirupas,
            kendraBalaVirupas = raw.kendraBalaVirupas,
            drekkanaBalaVirupas = raw.drekkanaBalaVirupas,
            totalVirupas = raw.totalVirupas,
            totalRupas = raw.totalRupas,
            isEvaluated = raw.isEvaluated,
        )

    private fun mapDigBala(raw: InternalDigBalaPosition): DigBala =
        DigBala(
            powerfulPointDegrees = raw.powerfulPointDegrees,
            zeroPointDegrees = raw.zeroPointDegrees,
            arcDegrees = raw.arcDegrees,
            virupas = raw.virupas,
            rupas = raw.rupas,
            isEvaluated = raw.isEvaluated,
        )

    private fun mapNaisargikaBala(raw: InternalNaisargikaBalaPosition): NaisargikaBala =
        NaisargikaBala(
            virupas = raw.virupas,
            rupas = raw.rupas,
            rank = raw.rank,
            isEvaluated = raw.isEvaluated,
        )

    private fun mapKalaBala(raw: InternalKalaBalaPosition): KalaBala =
        KalaBala(
            nathonnathaBalaVirupas = raw.nathonnathaBalaVirupas,
            pakshaBalaVirupas = raw.pakshaBalaVirupas,
            tribhagaBalaVirupas = raw.tribhagaBalaVirupas,
            varaBalaVirupas = raw.varaBalaVirupas,
            horaBalaVirupas = raw.horaBalaVirupas,
            masaBalaVirupas = raw.masaBalaVirupas,
            varshaBalaVirupas = raw.varshaBalaVirupas,
            ayanaBalaVirupas = raw.ayanaBalaVirupas,
            yuddhaBalaVirupas = raw.yuddhaBalaVirupas,
            totalVirupas = raw.totalVirupas,
            totalRupas = raw.totalRupas,
            isEvaluated = raw.isEvaluated,
            deferredSubcomponents = raw.deferredSubcomponents,
        )

    private fun mapChestaBala(raw: InternalChestaBalaPosition): ChestaBala =
        ChestaBala(
            isRetrograde = raw.isRetrograde,
            dailyMotionDegrees = raw.dailyMotionDegrees,
            chestaKendraDegrees = raw.chestaKendraDegrees,
            virupas = raw.virupas,
            rupas = raw.rupas,
            motionCategory = raw.motionCategory,
            isEvaluated = raw.isEvaluated,
            deferredSubcomponents = raw.deferredSubcomponents,
        )

    private fun mapDrikBala(raw: InternalDrikBalaPosition): DrikBala =
        DrikBala(
            beneficAspectVirupas = raw.beneficAspectVirupas,
            maleficAspectVirupas = raw.maleficAspectVirupas,
            virupas = raw.virupas,
            rupas = raw.rupas,
            isEvaluated = raw.isEvaluated,
            deferredSubcomponents = raw.deferredSubcomponents,
        )

    private fun mapPlanetaryShadbala(raw: InternalPlanetaryShadbalaPosition): PlanetaryShadbala =
        PlanetaryShadbala(
            body = mapBodyId(raw.bodyId),
            sthanaBala = mapSthanaBala(raw.sthanaBala),
            digBala = mapDigBala(raw.digBala),
            naisargikaBala = mapNaisargikaBala(raw.naisargikaBala),
            kalaBala = mapKalaBala(raw.kalaBala),
            chestaBala = mapChestaBala(raw.chestaBala),
            drikBala = mapDrikBala(raw.drikBala),
            completeness = mapShadbalaCompleteness(raw.completeness),
            isComplete = raw.isComplete,
            totalVirupas = raw.totalVirupas,
            totalRupas = raw.totalRupas,
            deferredComponents = raw.deferredComponents,
            rulesetId = raw.rulesetId,
        )

    private fun mapAshtakavargaResult(raw: InternalAshtakavargaResult): AshtakavargaResult =
        AshtakavargaResult(
            rulesetId = raw.rulesetId,
            bhinnashtakavarga = raw.bhinnashtakavarga.map { (body, chart) ->
                mapBodyId(body) to mapBhinnashtakavarga(chart)
            }.toMap(),
            sarvashtakavarga = mapSarvashtakavarga(raw.sarvashtakavarga),
            completeness = mapAshtakavargaCompleteness(raw.completeness),
            unsupportedBodies = raw.unsupportedBodies.map { mapBodyId(it) },
            shodhana = raw.shodhana?.let { mapShodhitaAshtakavargaResult(it) },
            pinda = raw.pinda?.let { mapAshtakavargaPindaResult(it) },
        )

    private fun mapAshtakavargaPindaResult(raw: InternalAshtakavargaPindaResult): AshtakavargaPinda =
        AshtakavargaPinda(
            rulesetId = raw.rulesetId,
            planetaryPindas = raw.planetaryPindas.map { (body, pinda) ->
                mapBodyId(body) to mapPlanetaryPindaResult(pinda)
            }.toMap(),
            totalRasiPinda = raw.totalRasiPinda,
            totalGrahaPinda = raw.totalGrahaPinda,
            totalShodhyaPinda = raw.totalShodhyaPinda,
            completeness = mapAshtakavargaCompleteness(raw.completeness),
            unsupportedBodies = raw.unsupportedBodies.map { mapBodyId(it) },
        )

    private fun mapPlanetaryPindaResult(raw: InternalPlanetaryPindaResult): PlanetaryPinda =
        PlanetaryPinda(
            targetBody = mapBodyId(raw.targetBody),
            rulesetId = raw.rulesetId,
            rasiPinda = raw.rasiPinda,
            grahaPinda = raw.grahaPinda,
            shodhyaPinda = raw.shodhyaPinda,
            rasiContributions = raw.rasiContributions.map { (rashiIndex, bindus) ->
                Rashi.fromIndex(rashiIndex) to bindus
            }.toMap(),
            grahaContributions = raw.grahaContributions.map { (body, bindus) ->
                mapBodyId(body) to bindus
            }.toMap(),
        )

    private fun mapShodhitaAshtakavargaResult(raw: InternalShodhitaAshtakavargaResult): ShodhitaAshtakavargaResult =
        ShodhitaAshtakavargaResult(
            rulesetId = raw.rulesetId,
            shodhitaBhinnashtakavarga = raw.shodhitaBhinnashtakavarga.map { (body, chart) ->
                mapBodyId(body) to mapShodhitaBhinnashtakavarga(chart)
            }.toMap(),
            shodhitaSarvashtakavarga = mapShodhitaSarvashtakavarga(raw.shodhitaSarvashtakavarga),
            completeness = mapAshtakavargaCompleteness(raw.completeness),
            unsupportedBodies = raw.unsupportedBodies.map { mapBodyId(it) },
        )

    private fun mapShodhitaBhinnashtakavarga(raw: InternalShodhitaBhinnashtakavargaChart): ShodhitaBhinnashtakavarga =
        ShodhitaBhinnashtakavarga(
            targetBody = mapBodyId(raw.targetBody),
            rulesetId = raw.rulesetId,
            signScores = raw.signScores.map { score ->
                ShodhitaBhinnashtakavargaSignScore(
                    rashi = Rashi.fromIndex(score.rashiIndex),
                    rawBindus = score.rawBindus,
                    trikonaReducedBindus = score.trikonaReducedBindus,
                    ekadhipatyaReducedBindus = score.ekadhipatyaReducedBindus,
                )
            },
            rawTotalBindus = raw.rawTotalBindus,
            trikonaTotalBindus = raw.trikonaTotalBindus,
            shodhitaTotalBindus = raw.shodhitaTotalBindus,
        )

    private fun mapShodhitaSarvashtakavarga(raw: InternalShodhitaSarvashtakavargaChart): ShodhitaSarvashtakavarga =
        ShodhitaSarvashtakavarga(
            rulesetId = raw.rulesetId,
            signScores = raw.signScores.map { score ->
                ShodhitaSarvashtakavargaSignScore(
                    rashi = Rashi.fromIndex(score.rashiIndex),
                    rawTotalBindus = score.rawTotalBindus,
                    trikonaTotalBindus = score.trikonaTotalBindus,
                    shodhitaTotalBindus = score.shodhitaTotalBindus,
                    planetShodhitaBindus = score.planetShodhitaBindus.map { (body, bindus) ->
                        mapBodyId(body) to bindus
                    }.toMap(),
                )
            },
            grandTotalRawBindus = raw.grandTotalRawBindus,
            grandTotalTrikonaBindus = raw.grandTotalTrikonaBindus,
            grandTotalShodhitaBindus = raw.grandTotalShodhitaBindus,
        )

    private fun mapBhinnashtakavarga(raw: InternalBhinnashtakavargaChart): Bhinnashtakavarga =
        Bhinnashtakavarga(
            targetBody = mapBodyId(raw.targetBody),
            rulesetId = raw.rulesetId,
            signScores = raw.signScores.map { score ->
                BhinnashtakavargaSignScore(
                    rashi = Rashi.fromIndex(score.rashiIndex),
                    binduCount = score.binduCount,
                    rekhaCount = score.rekhaCount,
                    contributingBodies = score.contributingBodies.map { mapAshtakavargaContributor(it) },
                )
            },
            totalBindus = raw.totalBindus,
            totalRekhas = raw.totalRekhas,
            contributorGrid = raw.contributorGrid.map { (c, row) ->
                mapAshtakavargaContributor(c) to row
            }.toMap(),
        )

    private fun mapSarvashtakavarga(raw: InternalSarvashtakavargaChart): Sarvashtakavarga =
        Sarvashtakavarga(
            rulesetId = raw.rulesetId,
            signScores = raw.signScores.map { score ->
                SarvashtakavargaSignScore(
                    rashi = Rashi.fromIndex(score.rashiIndex),
                    totalBindus = score.totalBindus,
                    totalRekhas = score.totalRekhas,
                    planetBindus = score.planetBindus.map { (body, bindus) ->
                        mapBodyId(body) to bindus
                    }.toMap(),
                )
            },
            grandTotalBindus = raw.grandTotalBindus,
            grandTotalRekhas = raw.grandTotalRekhas,
            isInvariantValid = raw.isInvariantValid,
        )

    private fun mapAshtakavargaContributor(c: InternalAshtakavargaContributor): AshtakavargaContributor = when (c) {
        InternalAshtakavargaContributor.SUN -> AshtakavargaContributor.SUN
        InternalAshtakavargaContributor.MOON -> AshtakavargaContributor.MOON
        InternalAshtakavargaContributor.MARS -> AshtakavargaContributor.MARS
        InternalAshtakavargaContributor.MERCURY -> AshtakavargaContributor.MERCURY
        InternalAshtakavargaContributor.JUPITER -> AshtakavargaContributor.JUPITER
        InternalAshtakavargaContributor.VENUS -> AshtakavargaContributor.VENUS
        InternalAshtakavargaContributor.SATURN -> AshtakavargaContributor.SATURN
        InternalAshtakavargaContributor.LAGNA -> AshtakavargaContributor.LAGNA
    }

    private fun mapAshtakavargaCompleteness(c: InternalAshtakavargaCompleteness): AshtakavargaCompleteness = when (c) {
        InternalAshtakavargaCompleteness.COMPLETE -> AshtakavargaCompleteness.COMPLETE
        InternalAshtakavargaCompleteness.PARTIAL -> AshtakavargaCompleteness.PARTIAL
        InternalAshtakavargaCompleteness.UNSUPPORTED -> AshtakavargaCompleteness.UNSUPPORTED
    }

    private fun mapCelestialBodyToBodyId(body: CelestialBody): BodyId = when (body) {
        CelestialBody.SUN -> BodyId.SUN
        CelestialBody.MOON -> BodyId.MOON
        CelestialBody.MERCURY -> BodyId.MERCURY
        CelestialBody.VENUS -> BodyId.VENUS
        CelestialBody.MARS -> BodyId.MARS
        CelestialBody.JUPITER -> BodyId.JUPITER
        CelestialBody.SATURN -> BodyId.SATURN
        CelestialBody.RAHU -> BodyId.RAHU
        CelestialBody.KETU -> BodyId.KETU
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

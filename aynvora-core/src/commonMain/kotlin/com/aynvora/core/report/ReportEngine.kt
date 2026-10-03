package com.aynvora.core.report

import com.aynvora.astro.dasha.DashaPeriod
import com.aynvora.astro.yogas.YogaDoshaEngine
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.result.AynvoraResult

interface ReportGenerator {
    val reportType: ReportType
    fun generate(input: ReportGeneratorInput, resolver: ReportTextResolver): ReportDocument
}

/** Generator registry keeps feature-specific document logic outside a universal type switch. */
class ReportGeneratorRegistry(
    generators: List<ReportGenerator> = listOf(
        KundaliReportGenerator(),
        GarudaPuranReportGenerator(),
        TarotReportGenerator(),
        PalmistryReportGenerator(),
        NumerologyReportGenerator(),
        GemstoneReportGenerator(),
        GitaReportGenerator(),
    )
) {
    private val entries = generators.toList()

    init {
        require(entries.map { it.reportType.id }
            .distinct().size == entries.size) { "Only one report generator may be registered per type" }
    }

    fun find(type: ReportType): ReportGenerator? =
        entries.firstOrNull { it.reportType.id == type.id }

    fun all(): List<ReportGenerator> = entries.toList()
}

class GenerateReportUseCase(
    private val sdk: AynvoraSdk,
    private val generators: ReportGeneratorRegistry = ReportGeneratorRegistry(),
    private val analyticsTracker: AnalyticsTracker = sdk.analytics,
) {
    suspend fun execute(
        request: ReportGenerationRequest,
        resolver: ReportTextResolver
    ): ReportGenerationResult {
        if (request.generatedAtEpochMs < 0L) return unavailable(
            request.reportType,
            ReportErrorCode.INVALID_REPORT_REQUEST,
            resolver.text(ReportTextKey.INVALID_INPUT)
        )
        if (request.language != resolver.language) return unavailable(
            request.reportType,
            ReportErrorCode.LOCALIZATION_MISSING,
            resolver.text(ReportTextKey.LOCALIZATION_ERROR)
        )
        val hasPreparedGarudaContent = request.reportType == ReportType.GARUDA_PURAN &&
                (request.generatorInput as? GarudaPuranReportInput)?.items?.isNotEmpty() == true
        if (ReportTypeRegistry.featureStatus(request.reportType.feature) == ReportFeatureStatus.FOUNDATION_ONLY && !hasPreparedGarudaContent) {
            return unavailable(
                request.reportType,
                ReportErrorCode.FOUNDATION_ONLY,
                resolver.text(ReportTextKey.FOUNDATION_ONLY_REASON),
                ReportFeatureStatus.FOUNDATION_ONLY
            )
        }
        val generator = generators.find(request.reportType)
            ?: return unavailableForMissingGenerator(request.reportType, resolver)
        if (generator.reportType != request.reportType) {
            return unavailable(
                request.reportType,
                ReportErrorCode.INVALID_REPORT_REQUEST,
                resolver.text(ReportTextKey.INVALID_INPUT)
            )
        }
        if (request.reportType != ReportType.KUNDALI) {
            val input = request.generatorInput
                ?: return unavailable(
                    request.reportType,
                    ReportErrorCode.INSUFFICIENT_DATA,
                    resolver.text(ReportTextKey.INSUFFICIENT_DATA)
                )
            return generateFrom(request.reportType, input, resolver)
        }
        val chartRequest = request.chartRequest
            ?: return unavailable(
                request.reportType,
                ReportErrorCode.NO_DATA,
                resolver.text(ReportTextKey.NOT_PROVIDED)
            )

        val chartResult = when (val result = sdk.calculateChart(chartRequest)) {
            is AynvoraResult.Success -> result.value
            is AynvoraResult.Failure -> {
                val code =
                    if (result is AynvoraResult.Failure.InvalidInput) ReportErrorCode.INSUFFICIENT_DATA else ReportErrorCode.CALCULATION_UNAVAILABLE
                val reason =
                    if (code == ReportErrorCode.INSUFFICIENT_DATA) resolver.text(ReportTextKey.INVALID_INPUT) else resolver.text(
                        ReportTextKey.CALCULATION_FAILED
                    )
                return unavailable(request.reportType, code, reason)
            }
        }
        val moon = chartResult.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }
            ?: return unavailable(
                request.reportType,
                ReportErrorCode.INSUFFICIENT_DATA,
                resolver.text(ReportTextKey.INSUFFICIENT_DATA)
            )
        if (chartResult.planetaryPositions.isEmpty()) {
            return unavailable(
                request.reportType,
                ReportErrorCode.INSUFFICIENT_DATA,
                resolver.text(ReportTextKey.INSUFFICIENT_DATA)
            )
        }

        val panchang = runCatching {
            sdk.calculatePanchang(
                chartResult.julianDay,
                chartRequest.config.ayanamsa.name
            )
        }
            .getOrElse {
                return unavailable(
                    request.reportType,
                    ReportErrorCode.CALCULATION_UNAVAILABLE,
                    resolver.text(ReportTextKey.CALCULATION_FAILED)
                )
            }
        val dasha = runCatching {
            sdk.calculateDasha(
                chartResult.julianDay,
                moon.siderealLongitude,
                calculateAntardashas = true,
                calculatePratyantardashas = true
            )
        }.getOrElse {
            return unavailable(
                request.reportType,
                ReportErrorCode.CALCULATION_UNAVAILABLE,
                resolver.text(ReportTextKey.CALCULATION_FAILED)
            )
        }

        val document = try {
            generator.generate(
                KundaliReportInput(
                    chart = chartResult,
                    panchang = panchang,
                    dasha = dasha,
                    calculationVersion = chartResult.engineVersion,
                    identity = request.identity,
                    generatedAtEpochMs = request.generatedAtEpochMs,
                    language = request.language,
                    evidenceGraph = request.evidenceGraph,
                ),
                resolver,
            )
        } catch (_: Exception) {
            return unavailable(
                request.reportType,
                ReportErrorCode.CALCULATION_UNAVAILABLE,
                resolver.text(ReportTextKey.CALCULATION_FAILED)
            )
        }
        analyticsTracker.track(AnalyticsEvent.ReportGenerated(request.reportType.id))
        return ReportGenerationResult.Generated(document)
    }

    fun generateFrom(
        type: ReportType,
        input: ReportGeneratorInput,
        resolver: ReportTextResolver
    ): ReportGenerationResult {
        val hasPreparedGarudaContent = type == ReportType.GARUDA_PURAN &&
                (input as? GarudaPuranReportInput)?.items?.isNotEmpty() == true
        if (ReportTypeRegistry.featureStatus(type.feature) == ReportFeatureStatus.FOUNDATION_ONLY && !hasPreparedGarudaContent) {
            return unavailable(
                type,
                ReportErrorCode.FOUNDATION_ONLY,
                resolver.text(ReportTextKey.FOUNDATION_ONLY_REASON),
                ReportFeatureStatus.FOUNDATION_ONLY
            )
        }
        val generator =
            generators.find(type) ?: return unavailableForMissingGenerator(type, resolver)
        if (generator.reportType != type) return unavailable(
            type,
            ReportErrorCode.INVALID_REPORT_REQUEST,
            resolver.text(ReportTextKey.INVALID_INPUT)
        )
        if (resolver.language !in ReportLanguage.entries) {
            return unavailable(
                type,
                ReportErrorCode.LOCALIZATION_MISSING,
                resolver.text(ReportTextKey.LOCALIZATION_ERROR)
            )
        }
        return runCatching {
            generator.generate(input, resolver).also {
                require(it.metadata.reportTypeId == type.id && it.metadata.language == resolver.language)
            }
        }.map { document ->
            analyticsTracker.track(AnalyticsEvent.ReportGenerated(type.id))
            if (type == ReportType.GARUDA_PURAN) analyticsTracker.track(AnalyticsEvent.GarudaPuranReportGenerated)
            ReportGenerationResult.Generated(document)
        }
            .getOrElse {
                unavailable(
                    type,
                    ReportErrorCode.INVALID_REPORT_REQUEST,
                    resolver.text(ReportTextKey.INVALID_INPUT)
                )
            }
    }

    private fun unavailableForMissingGenerator(
        type: ReportType,
        resolver: ReportTextResolver
    ): ReportGenerationResult.Unavailable {
        val status = ReportTypeRegistry.featureStatus(type.feature)
        val foundationOnly = status == ReportFeatureStatus.FOUNDATION_ONLY
        val code =
            if (foundationOnly) ReportErrorCode.FOUNDATION_ONLY else ReportErrorCode.FEATURE_NOT_IMPLEMENTED
        val reason =
            resolver.text(if (foundationOnly) ReportTextKey.FOUNDATION_ONLY_REASON else ReportTextKey.REPORT_NOT_IMPLEMENTED)
        return unavailable(type, code, reason, status)
    }

    private fun unavailable(
        type: ReportType,
        code: ReportErrorCode,
        reason: ReportText,
        status: ReportFeatureStatus? = null
    ) =
        ReportGenerationResult.Unavailable(type.id, code, reason, status)
}

class KundaliReportGenerator : ReportGenerator {
    override val reportType: ReportType = ReportType.KUNDALI

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver
    ): ReportDocument {
        require(input is KundaliReportInput) { "Kundali generator requires KundaliReportInput" }
        require(input.language == resolver.language) { "Report language and text resolver language differ" }
        require(input.chart.planetaryPositions.isNotEmpty()) { "Chart result contains no planetary positions" }
        val chart = input.chart
        val config = chart.config
        val profile = "${config.profile.name}|${config.ayanamsa.name}|${config.houseSystem.name}"
        val source = ReportSource(
            "aynvora.astro_engine",
            resolver.text(ReportTextKey.SOURCE_ASTRO_ENGINE).value
        )
        val calculation =
            ReportCalculationReference("natal_chart", profile, chart.engineVersion, source.sourceId)
        val sections = mutableListOf<ReportSection>()
        val unavailable = mutableListOf<ReportSectionAvailability>()

        fun include(
            id: String,
            title: ReportTextKey,
            blocks: List<ReportBlock>,
            calcId: String = calculation.calculationId,
            subsections: List<ReportSubsection> = emptyList(),
        ) {
            val reference = calculation.copy(calculationId = calcId)
            val evidence = ReportEvidence(
                "$calcId:${id}",
                ReportContentKind.CALCULATION,
                resolver.text(title),
                reference,
                source
            )
            sections += ReportSection(
                id,
                resolver.text(title),
                blocks.toList(),
                subsections.toList(),
                evidence = listOf(evidence)
            )
        }

        fun omit(
            id: String,
            title: ReportTextKey,
            code: ReportUnavailableReason = ReportUnavailableReason.RESULT_NOT_INCLUDED
        ) {
            val reason = when (code) {
                ReportUnavailableReason.DIVISIONAL_CHARTS_NOT_REQUESTED -> ReportTextKey.DIVISIONAL_NOT_REQUESTED
                ReportUnavailableReason.TRANSIT_EPOCH_NOT_REQUESTED -> ReportTextKey.TRANSIT_EPOCH_MISSING
                ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE -> ReportTextKey.NOT_FROM_ENGINE
                ReportUnavailableReason.INTERPRETATION_EVIDENCE_NOT_PROVIDED -> ReportTextKey.EVIDENCE_NOT_PROVIDED
                ReportUnavailableReason.APPROVED_CONTENT_NOT_INSTALLED -> ReportTextKey.GARUDA_PACKAGE_MISSING
                ReportUnavailableReason.CONTENT_UNAVAILABLE_IN_LANGUAGE -> ReportTextKey.GARUDA_LANGUAGE_CONTENT_MISSING
                ReportUnavailableReason.TOPIC_NOT_AVAILABLE_IN_SOURCE -> ReportTextKey.GARUDA_TOPIC_NOT_IN_SOURCE
                ReportUnavailableReason.RESULT_NOT_INCLUDED -> ReportTextKey.RESULT_NOT_INCLUDED
            }
            unavailable += ReportSectionAvailability(
                id,
                resolver.text(title),
                ReportSectionStatus.OMITTED,
                code,
                resolver.text(reason)
            )
        }

        fun table(
            headers: List<ReportTextKey>,
            rows: List<List<String>>,
            kind: ReportContentKind = ReportContentKind.FACT
        ) =
            ReportTable(headers.map(resolver::text), rows, kind)

        fun number(value: Double) = resolver.number(value, 4)

        val birth = chart.birthData
        val coordinates = birth.place.coordinates
        val birthRows = buildList {
            if (input.identity.displayName != null) add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.VALUE),
                    input.identity.displayName,
                    ReportContentKind.USER_CONTEXT
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.BIRTH_DATE),
                    resolver.birthDate(birth.date.year, birth.date.month, birth.date.day)
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.BIRTH_TIME),
                    resolver.birthTime(birth.time.hour, birth.time.minute, birth.time.second)
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.BIRTH_PLACE),
                    birth.place.name.ifBlank { resolver.text(ReportTextKey.NOT_PROVIDED).value })
            )
            add(ReportKeyValue(resolver.text(ReportTextKey.TIME_ZONE), birth.place.timezoneId))
            add(ReportKeyValue(resolver.text(ReportTextKey.LATITUDE), number(coordinates.latitude)))
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.LONGITUDE),
                    number(coordinates.longitude)
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.JULIAN_DAY),
                    resolver.number(chart.julianDay, 6),
                    ReportContentKind.CALCULATION
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.AYANAMSA),
                    "${config.ayanamsa.name} (${number(chart.ayanamsaDegrees)}°)",
                    ReportContentKind.CALCULATION
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.CALCULATION_PROFILE),
                    profile,
                    ReportContentKind.SOURCE
                )
            )
            add(
                ReportKeyValue(
                    resolver.text(ReportTextKey.ENGINE_VERSION),
                    chart.engineVersion,
                    ReportContentKind.SOURCE
                )
            )
        }
        include("birth_details", ReportTextKey.BIRTH_DETAILS, birthRows)

        include(
            "panchang", ReportTextKey.PANCHANG, listOf(
                table(
                    listOf(ReportTextKey.VALUE, ReportTextKey.STATUS),
                    listOf(
                        listOf(
                            resolver.text(ReportTextKey.TITHI).value,
                            resolver.tithiName(input.panchang.tithi)
                        ),
                        listOf(
                            resolver.text(ReportTextKey.VARA).value,
                            resolver.varaName(input.panchang.vara)
                        ),
                        listOf(
                            resolver.text(ReportTextKey.NAKSHATRA).value,
                            resolver.nakshatraName(
                                Nakshatra.values()
                                    .first { it.index == input.panchang.nakshatraIndex })
                        ),
                        listOf(
                            resolver.text(ReportTextKey.YOGA).value,
                            resolver.yogaName(input.panchang.yoga)
                        ),
                        listOf(
                            resolver.text(ReportTextKey.KARANA).value,
                            resolver.karanaName(input.panchang.karana)
                        ),
                    ),
                    ReportContentKind.CALCULATION,
                )
            ), "panchang:${input.panchang.profileId}:${input.panchang.varaConvention.name}"
        )

        val lagna = chart.lagna
        if (lagna != null) {
            val ascSign = lagna.rashiPosition
            include(
                "ascendant", ReportTextKey.ASCENDANT, listOf(
                    ReportKeyValue(
                        resolver.text(ReportTextKey.SIGN),
                        resolver.signName(ascSign.rashi),
                        ReportContentKind.CALCULATION
                    ),
                    ReportKeyValue(
                        resolver.text(ReportTextKey.DEGREE),
                        number(ascSign.degreeInSign),
                        ReportContentKind.CALCULATION
                    ),
                    ReportKeyValue(
                        resolver.text(ReportTextKey.NAKSHATRA_COLUMN),
                        resolver.nakshatraName(lagna.nakshatraPosition.nakshatra),
                        ReportContentKind.CALCULATION
                    ),
                    ReportKeyValue(
                        resolver.text(ReportTextKey.PADA),
                        lagna.nakshatraPosition.pada.toString(),
                        ReportContentKind.CALCULATION
                    ),
                    ReportKeyValue(
                        resolver.text(ReportTextKey.VALUE),
                        number(ascSign.totalSiderealLongitude),
                        ReportContentKind.CALCULATION
                    ),
                )
            )
        } else omit("ascendant", ReportTextKey.ASCENDANT)

        val moon = chart.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }
        if (moon != null) {
            include(
                "moon_sign",
                ReportTextKey.MOON_SIGN,
                listOf(
                    ReportKeyValue(
                        resolver.text(ReportTextKey.SIGN),
                        resolver.signName(moon.rashiPosition.rashi),
                        ReportContentKind.CALCULATION
                    )
                )
            )
            include(
                "nakshatra", ReportTextKey.NAKSHATRA, listOf(
                    ReportKeyValue(
                        resolver.text(ReportTextKey.NAKSHATRA_COLUMN),
                        resolver.nakshatraName(moon.nakshatraPosition.nakshatra),
                        ReportContentKind.CALCULATION
                    ),
                    ReportKeyValue(
                        resolver.text(ReportTextKey.PADA),
                        moon.nakshatraPosition.pada.toString(),
                        ReportContentKind.CALCULATION
                    ),
                )
            )
        } else {
            omit("moon_sign", ReportTextKey.MOON_SIGN)
            omit("nakshatra", ReportTextKey.NAKSHATRA)
        }

        val positions = chart.planetaryPositions
        if (positions.isNotEmpty()) {
            include(
                "planetary_positions", ReportTextKey.PLANETARY_POSITIONS, listOf(
                    table(
                        listOf(
                            ReportTextKey.BODY,
                            ReportTextKey.SIGN,
                            ReportTextKey.DEGREE,
                            ReportTextKey.NAKSHATRA_COLUMN,
                            ReportTextKey.PADA,
                            ReportTextKey.HOUSE
                        ),
                        positions.map {
                            listOf(
                                resolver.bodyName(it.body),
                                resolver.signName(it.rashiPosition.rashi),
                                number(it.rashiPosition.degreeInSign),
                                resolver.nakshatraName(it.nakshatraPosition.nakshatra),
                                it.nakshatraPosition.pada.toString(),
                                it.houseNumber.toString()
                            )
                        },
                        ReportContentKind.CALCULATION,
                    )
                )
            )
        } else omit("planetary_positions", ReportTextKey.PLANETARY_POSITIONS)

        if (chart.houses.isNotEmpty()) include(
            "house_placements", ReportTextKey.HOUSE_PLACEMENTS, listOf(
                table(
                    listOf(
                        ReportTextKey.HOUSE,
                        ReportTextKey.SIGN,
                        ReportTextKey.DEGREE,
                        ReportTextKey.START,
                        ReportTextKey.END
                    ),
                    chart.houses.map {
                        listOf(
                            it.houseNumber.toString(),
                            resolver.signName(it.rashiPosition.rashi),
                            number(it.rashiPosition.degreeInSign),
                            number(it.startLongitude),
                            number(it.endLongitude)
                        )
                    },
                    ReportContentKind.CALCULATION,
                )
            ), "houses:${chart.houses.first().system.name}"
        ) else omit("house_placements", ReportTextKey.HOUSE_PLACEMENTS)

        val planetStates = chart.planetStates
        if (planetStates.isNotEmpty()) {
            include(
                "retrograde",
                ReportTextKey.RETROGRADE,
                listOf(
                    table(
                        listOf(ReportTextKey.BODY, ReportTextKey.STATUS),
                        planetStates.map {
                            listOf(
                                resolver.bodyName(it.body),
                                resolver.enumLabel(it.motionState.name)
                            )
                        },
                        ReportContentKind.CALCULATION
                    )
                )
            )
            val combustion = planetStates.filter { it.combustionState.name != "NOT_APPLICABLE" }
            if (combustion.isNotEmpty()) include(
                "combustion",
                ReportTextKey.COMBUSTION,
                listOf(
                    table(
                        listOf(ReportTextKey.BODY, ReportTextKey.STATUS),
                        combustion.map {
                            listOf(
                                resolver.bodyName(it.body),
                                resolver.enumLabel(it.combustionState.name)
                            )
                        },
                        ReportContentKind.CALCULATION
                    )
                )
            ) else omit("combustion", ReportTextKey.COMBUSTION)
        } else {
            omit("retrograde", ReportTextKey.RETROGRADE)
            omit("combustion", ReportTextKey.COMBUSTION)
        }

        if (chart.aspects.isNotEmpty()) include(
            "aspects", ReportTextKey.ASPECTS, listOf(
                table(
                    listOf(
                        ReportTextKey.BODY,
                        ReportTextKey.VALUE,
                        ReportTextKey.BODY,
                        ReportTextKey.DEGREE
                    ),
                    chart.aspects.map {
                        listOf(
                            resolver.bodyName(it.firstBody),
                            resolver.enumLabel(it.type.name),
                            resolver.bodyName(it.secondBody),
                            number(it.actualSeparation)
                        )
                    },
                    ReportContentKind.CALCULATION,
                )
            )
        ) else omit("aspects", ReportTextKey.ASPECTS)

        if (chart.planetaryDignities.isNotEmpty()) include(
            "dignities", ReportTextKey.DIGNITIES, listOf(
                table(
                    listOf(ReportTextKey.BODY, ReportTextKey.SIGN, ReportTextKey.STATUS),
                    chart.planetaryDignities.map {
                        listOf(
                            resolver.bodyName(it.body),
                            resolver.signName(it.rashi),
                            resolver.enumLabel(it.dignityType.name)
                        )
                    },
                    ReportContentKind.CALCULATION,
                )
            )
        ) else omit("dignities", ReportTextKey.DIGNITIES)

        if (chart.planetaryRelationships.isNotEmpty()) include(
            "relationships", ReportTextKey.RELATIONSHIPS, listOf(
                table(
                    listOf(ReportTextKey.BODY, ReportTextKey.VALUE, ReportTextKey.STATUS),
                    chart.planetaryRelationships.map {
                        listOf(
                            resolver.bodyName(it.sourceBody),
                            resolver.bodyName(it.targetBody),
                            resolver.enumLabel(it.compoundRelationship.name)
                        )
                    },
                    ReportContentKind.CALCULATION,
                )
            )
        ) else omit("relationships", ReportTextKey.RELATIONSHIPS)

        if (chart.shadbala.isNotEmpty()) include(
            "shadbala", ReportTextKey.SHADBALA, listOf(
                table(
                    listOf(ReportTextKey.BODY, ReportTextKey.VALUE, ReportTextKey.STATUS),
                    chart.shadbala.map {
                        listOf(
                            resolver.bodyName(it.body),
                            it.totalRupas?.let(::number)
                                ?: resolver.text(ReportTextKey.NOT_PROVIDED).value,
                            resolver.enumLabel(it.completeness.name)
                        )
                    },
                    ReportContentKind.CALCULATION,
                )
            )
        ) else omit("shadbala", ReportTextKey.SHADBALA)

        chart.ashtakavarga?.let { av ->
            include(
                "ashtakavarga", ReportTextKey.ASHTAKAVARGA, listOf(
                    table(
                        listOf(ReportTextKey.SIGN, ReportTextKey.VALUE),
                        av.sarvashtakavarga.signScores.map {
                            listOf(
                                resolver.signName(it.rashi),
                                it.totalBindus.toString()
                            )
                        },
                        ReportContentKind.CALCULATION,
                    )
                ), "ashtakavarga:${av.rulesetId}"
            )
        } ?: omit("ashtakavarga", ReportTextKey.ASHTAKAVARGA)

        chart.shodhitaAshtakavarga?.let { shodhana ->
            include(
                "shodhana", ReportTextKey.SHODHANA, listOf(
                    table(
                        listOf(ReportTextKey.SIGN, ReportTextKey.VALUE),
                        shodhana.shodhitaSarvashtakavarga.signScores.map {
                            listOf(
                                resolver.signName(
                                    it.rashi
                                ), it.shodhitaTotalBindus.toString()
                            )
                        },
                        ReportContentKind.CALCULATION,
                    )
                ), "shodhana:${shodhana.rulesetId}"
            )
        } ?: omit("shodhana", ReportTextKey.SHODHANA)

        chart.ashtakavargaPinda?.let { pinda ->
            include(
                "pinda", ReportTextKey.PINDA, listOf(
                    table(
                        listOf(ReportTextKey.BODY, ReportTextKey.VALUE, ReportTextKey.STATUS),
                        pinda.planetaryPindas.values.sortedBy { it.targetBody.name }.map {
                            listOf(
                                resolver.bodyName(it.targetBody),
                                it.rasiPinda.toString(),
                                it.shodhyaPinda.toString()
                            )
                        },
                        ReportContentKind.CALCULATION,
                    )
                ), "pinda:${pinda.rulesetId}"
            )
        } ?: omit("pinda", ReportTextKey.PINDA)

        val vargaSubsections = chart.divisionalCharts.entries.sortedBy { it.key.divisionNumber }
            .map { (division, varga) ->
                val rows = listOfNotNull(varga.lagnaPosition?.let {
                    listOf(
                        resolver.text(ReportTextKey.ASCENDANT).value,
                        resolver.signName(it.resultingRashi),
                        number(it.degreeInResultingRashi)
                    )
                }) + varga.positions.map {
                    listOf(
                        it.body?.let(resolver::bodyName)
                            ?: resolver.text(ReportTextKey.ASCENDANT).value,
                        resolver.signName(it.resultingRashi),
                        number(it.degreeInResultingRashi)
                    )
                }
                ReportSubsection(
                    id = division.name,
                    title = ReportText(division.name, division.name),
                    blocks = listOf(
                        table(
                            listOf(
                                ReportTextKey.BODY,
                                ReportTextKey.SIGN,
                                ReportTextKey.DEGREE
                            ), rows, ReportContentKind.CALCULATION
                        )
                    ),
                )
            }
        if (vargaSubsections.isNotEmpty()) {
            include(
                "vargas",
                ReportTextKey.VARGAS,
                emptyList(),
                "vargas:${config.vargaRulesetId}",
                vargaSubsections
            )
        } else omit(
            "vargas",
            ReportTextKey.VARGAS,
            ReportUnavailableReason.DIVISIONAL_CHARTS_NOT_REQUESTED
        )

        fun dashaRows(period: DashaPeriod): List<List<String>> {
            val levelKey = when (period.level) {
                1 -> ReportTextKey.MAHADASHA
                2 -> ReportTextKey.ANTARDASHA
                3 -> ReportTextKey.PRATYANTARDASHA
                else -> ReportTextKey.DASHA_LEVEL
            }
            val current = listOf(
                resolver.text(levelKey).value,
                resolver.enumLabel(period.planet.name),
                resolver.number(period.startJulianDay, 4),
                resolver.number(period.endJulianDay, 4),
            )
            return listOf(current) + period.subPeriods.flatMap(::dashaRows)
        }

        val dashaTableRows = input.dasha.mahadashas.flatMap(::dashaRows)
        if (dashaTableRows.isNotEmpty()) include(
            "dasha", ReportTextKey.DASHA, listOf(
                table(
                    listOf(
                        ReportTextKey.DASHA_LEVEL,
                        ReportTextKey.DASHA_PLANET,
                        ReportTextKey.START,
                        ReportTextKey.END
                    ), dashaTableRows, ReportContentKind.CALCULATION,
                )
            ), "vimshottari:${input.dasha.rulesetId}"
        ) else omit("dasha", ReportTextKey.DASHA)

        omit(
            "transits",
            ReportTextKey.TRANSITS,
            ReportUnavailableReason.TRANSIT_EPOCH_NOT_REQUESTED
        )
        if (input.includeYogas || input.includeDoshas) {
            val planetHouses = mutableMapOf<String, Int>()
            for (p in chart.planetaryPositions) {
                planetHouses[p.body.name] = p.houseNumber
            }
            val evaluation = YogaDoshaEngine.evaluate(planetHouses)

            if (input.includeYogas) {
                val presentYogas = evaluation.yogas.filter { it.isPresent }
                if (presentYogas.isNotEmpty()) {
                    val rows = presentYogas.map {
                        listOf(it.name, it.description, it.ruleReference)
                    }
                    include(
                        "yogas",
                        ReportTextKey.YOGAS,
                        listOf(
                            table(
                                listOf(ReportTextKey.VALUE, ReportTextKey.STATUS, ReportTextKey.RULESET),
                                rows,
                                ReportContentKind.CALCULATION
                            )
                        ),
                        "yogas:parashari"
                    )
                } else {
                    omit("yogas", ReportTextKey.YOGAS, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
                }
            } else {
                omit("yogas", ReportTextKey.YOGAS, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
            }

            if (input.includeDoshas) {
                val presentDoshas = evaluation.doshas.filter { it.isPresent }
                if (presentDoshas.isNotEmpty()) {
                    val rows = presentDoshas.map {
                        listOf(it.name, it.strength, it.ruleReference)
                    }
                    include(
                        "dosha",
                        ReportTextKey.DOSHA,
                        listOf(
                            table(
                                listOf(ReportTextKey.VALUE, ReportTextKey.STATUS, ReportTextKey.RULESET),
                                rows,
                                ReportContentKind.CALCULATION
                            )
                        ),
                        "doshas:parashari"
                    )
                } else {
                    omit("dosha", ReportTextKey.DOSHA, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
                }
            } else {
                omit("dosha", ReportTextKey.DOSHA, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
            }
        } else {
            omit("yogas", ReportTextKey.YOGAS, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
            omit("dosha", ReportTextKey.DOSHA, ReportUnavailableReason.NOT_PROVIDED_BY_ASTRO_ENGINE)
        }
        omit(
            "timing",
            ReportTextKey.TIMING,
            ReportUnavailableReason.INTERPRETATION_EVIDENCE_NOT_PROVIDED
        )
        omit(
            "predictions",
            ReportTextKey.PREDICTIONS,
            ReportUnavailableReason.INTERPRETATION_EVIDENCE_NOT_PROVIDED
        )

        if (input.evidenceGraph != null) {
            val eligible = input.evidenceGraph.nodes.values
                .filter { it.provenance.locale == input.language.code && it.evidenceId.isNotBlank() && it.summary.isNotBlank() }
                .sortedBy { it.evidenceId }
            if (eligible.isNotEmpty()) {
                val eligibleIds = eligible.map { it.evidenceId }.toSet()
                val edgeRows = input.evidenceGraph.edges
                    .filter { it.sourceEvidenceId in eligibleIds && it.targetEvidenceId in eligibleIds }
                    .sortedWith(
                        compareBy(
                            { it.sourceEvidenceId },
                            { it.targetEvidenceId },
                            { it.relationship })
                    )
                    .map {
                        listOf(
                            it.sourceEvidenceId,
                            it.targetEvidenceId,
                            resolver.enumLabel(it.relationship)
                        )
                    }
                val subsections = if (edgeRows.isEmpty()) emptyList() else listOf(
                    ReportSubsection(
                        id = "evidence_relationships",
                        title = resolver.text(ReportTextKey.EVIDENCE_RELATIONSHIPS),
                        blocks = listOf(
                            table(
                                listOf(
                                    ReportTextKey.SOURCE_EVIDENCE,
                                    ReportTextKey.TARGET_EVIDENCE,
                                    ReportTextKey.RELATIONSHIP
                                ), edgeRows, ReportContentKind.SOURCE
                            )
                        ),
                    ),
                )
                val graphEvidence = eligible.map { item ->
                    val sourceId =
                        item.provenance.sourceName.ifBlank { item.provenance.rulesetOrEdition }
                    val contentKind = when (item.category) {
                        EvidenceCategory.FACT -> ReportContentKind.FACT
                        EvidenceCategory.DERIVED_FACT -> ReportContentKind.CALCULATION
                        EvidenceCategory.TRADITIONAL_RULE -> ReportContentKind.SOURCE
                        EvidenceCategory.INTERPRETATION -> ReportContentKind.INTERPRETATION
                        EvidenceCategory.USER_CONTEXT -> ReportContentKind.USER_CONTEXT
                    }
                    ReportEvidence(
                        evidenceId = item.evidenceId,
                        contentKind = contentKind,
                        text = ReportText(item.evidenceId, item.summary),
                        calculation = ReportCalculationReference(
                            calculationId = item.ruleId ?: item.evidenceId,
                            calculationProfile = item.provenance.calculationProfile
                                ?: item.provenance.rulesetOrEdition,
                            calculationVersion = item.provenance.engineVersion,
                            sourceId = sourceId,
                        ),
                        source = ReportSource(
                            sourceId,
                            item.provenance.sourceName.ifBlank { sourceId }),
                    )
                }
                val evidenceRows = eligible.map { item ->
                    listOf(
                        item.evidenceId,
                        item.summary,
                        resolver.enumLabel(item.category.name),
                        item.provenance.sourceName,
                        item.provenance.calculationProfile.orEmpty(),
                        item.provenance.rulesetOrEdition,
                        item.provenance.engineVersion,
                        resolver.enumLabel(ReportReferenceStatus.NOT_ASSESSED.name),
                    )
                }
                sections += ReportSection(
                    id = "evidence",
                    title = resolver.text(ReportTextKey.EVIDENCE),
                    blocks = listOf(
                        table(
                            listOf(
                                ReportTextKey.EVIDENCE_ID,
                                ReportTextKey.VALUE,
                                ReportTextKey.EVIDENCE_CATEGORY,
                                ReportTextKey.CONTENT_SOURCE,
                                ReportTextKey.PROFILE,
                                ReportTextKey.RULESET,
                                ReportTextKey.ENGINE_VERSION,
                                ReportTextKey.REFERENCE_STATUS
                            ),
                            evidenceRows,
                            ReportContentKind.SOURCE,
                        )
                    ),
                    subsections = subsections,
                    evidence = graphEvidence,
                )
            } else omit(
                "evidence",
                ReportTextKey.EVIDENCE,
                ReportUnavailableReason.INTERPRETATION_EVIDENCE_NOT_PROVIDED
            )
        } else omit(
            "evidence",
            ReportTextKey.EVIDENCE,
            ReportUnavailableReason.INTERPRETATION_EVIDENCE_NOT_PROVIDED
        )

        include(
            "evidence_provenance", ReportTextKey.EVIDENCE_PROVENANCE, listOf(
                ReportKeyValue(
                    resolver.text(ReportTextKey.PROFILE),
                    profile,
                    ReportContentKind.SOURCE
                ),
                ReportKeyValue(
                    resolver.text(ReportTextKey.ENGINE_VERSION),
                    chart.engineVersion,
                    ReportContentKind.SOURCE
                ),
                ReportKeyValue(
                    resolver.text(ReportTextKey.REFERENCE_STATUS),
                    resolver.enumLabel(ReportReferenceStatus.NOT_ASSESSED.name),
                    ReportContentKind.SOURCE
                ),
            )
        )

        if (unavailable.isNotEmpty()) include(
            "limitations",
            ReportTextKey.LIMITATIONS,
            unavailable.map { ReportKeyValue(it.title, it.reason.value, ReportContentKind.FACT) })

        val ordered = sections.sortedBy {
            sectionOrder.indexOf(it.id).let { index -> if (index < 0) Int.MAX_VALUE else index }
        }
        return ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "kundali-${
                    chart.julianDay.toBits().toString(16)
                }-${input.language.code}",
                reportTypeId = reportType.id,
                generatedAtEpochMs = input.generatedAtEpochMs,
                language = input.language,
                version = ReportVersion("1.0.0", input.calculationVersion, "1.0.0"),
                identity = input.identity,
                feature = reportType.feature,
                featureStatus = ReportFeatureStatus.IMPLEMENTED,
            ),
            title = resolver.text(ReportTextKey.KUNDALI_TITLE),
            sections = ordered,
            availability = unavailable,
            disclaimer = ReportDisclaimer(
                resolver.text(ReportTextKey.DISCLAIMER_TITLE),
                resolver.text(ReportTextKey.DISCLAIMER_TEXT)
            ),
            graph = input.evidenceGraph,
        )
    }

    private companion object {
        val sectionOrder = listOf(
            "birth_details",
            "panchang",
            "ascendant",
            "moon_sign",
            "nakshatra",
            "planetary_positions",
            "house_placements",
            "retrograde",
            "combustion",
            "aspects",
            "dignities",
            "relationships",
            "shadbala",
            "ashtakavarga",
            "shodhana",
            "pinda",
            "vargas",
            "dasha",
            "transits",
            "yogas",
            "dosha",
            "timing",
            "predictions",
            "evidence",
            "evidence_provenance",
            "limitations",
            "disclaimer",
        )
    }
}

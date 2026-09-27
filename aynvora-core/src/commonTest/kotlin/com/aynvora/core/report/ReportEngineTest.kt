package com.aynvora.core.report

import com.aynvora.core.Aynvora
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.Coordinates
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ReportEngineTest {
    private val text = TestReportTextResolver(ReportLanguage.ENGLISH)
    private val request = ReportGenerationRequest(
        reportType = ReportType.KUNDALI,
        chartRequest = ChartRequest(
            BirthData(
                BirthDate(1990, 5, 15),
                BirthTime(14, 30),
                BirthPlace("New Delhi", Coordinates(28.6139, 77.2090), "Asia/Kolkata")
            ),
            CalculationConfig(),
        ),
        language = ReportLanguage.ENGLISH,
        generatedAtEpochMs = 1_700_000_000_000,
    )

    @Test
    fun generatesDeterministicKundaliDocumentFromStructuredEngineResults() = runBlocking {
        val useCase = GenerateReportUseCase(Aynvora.create())
        val generated = useCase.execute(request, text)
        assertTrue(
            generated is ReportGenerationResult.Generated,
            "Expected generated report, got $generated"
        )
        val first = assertIs<ReportGenerationResult.Generated>(generated).document
        val second =
            assertIs<ReportGenerationResult.Generated>(useCase.execute(request, text)).document
        assertEquals(first, second)
        assertEquals("kundali", first.metadata.reportTypeId)
        assertEquals("1.0.0", first.metadata.version.reportSchemaVersion)
        assertTrue(first.sections.any { it.id == "planetary_positions" })
        assertTrue(first.sections.any { it.id == "birth_details" })
        assertTrue(first.sections.any { it.id == "evidence_provenance" })
        val dashaTable =
            first.sections.single { it.id == "dasha" }.blocks.filterIsInstance<ReportTable>()
                .single()
        assertTrue(dashaTable.rows.any { it[0] == text.text(ReportTextKey.MAHADASHA).value })
        assertTrue(dashaTable.rows.any { it[0] == text.text(ReportTextKey.ANTARDASHA).value })
        assertTrue(dashaTable.rows.any { it[0] == text.text(ReportTextKey.PRATYANTARDASHA).value })
        assertTrue(first.sections.none { it.id == "transits" || it.id == "yogas" || it.id == "dosha" })
        assertTrue(first.sectionAvailability.any { it.sectionId == "transits" && it.status == ReportSectionStatus.OMITTED })
        assertTrue(first.sectionAvailability.any { it.sectionId == "vargas" && it.reasonCode == ReportUnavailableReason.DIVISIONAL_CHARTS_NOT_REQUESTED })
        val transitAvailability = first.sectionAvailability.single { it.sectionId == "transits" }
        val limitation =
            first.sections.single { it.id == "limitations" }.blocks.filterIsInstance<ReportKeyValue>()
                .single { it.label == transitAvailability.title }
        assertEquals(transitAvailability.reason.value, limitation.value)
        val actualOrder = first.sections.map { it.id }
        assertEquals(actualOrder, actualOrder.sortedBy { ReportSectionOrder.ids.indexOf(it) })
    }

    @Test
    fun canonicalReportCatalogContainsAllIndependentReportTypes() {
        val descriptors = ReportTypeRegistry.all()
        assertEquals(12, descriptors.size)
        assertEquals(12, descriptors.map { it.type.id }.distinct().size)
        assertEquals(
            setOf(
                "kundali",
                "gemstone",
                "numerology",
                "rudraksha",
                "jadi",
                "yantra",
                "palmistry",
                "tarot",
                "gita",
                "lal_kitab",
                "garuda_puran",
                "daily_guidance"
            ), descriptors.map { it.type.id }.toSet()
        )
        assertEquals(
            ReportFeatureStatus.FOUNDATION_ONLY,
            ReportTypeRegistry.featureStatus(ReportType.RUDRAKSHA.feature)
        )
        assertEquals(
            ReportFeatureStatus.IMPLEMENTED,
            ReportTypeRegistry.featureStatus(ReportType.GEMSTONE.feature)
        )
        assertEquals(
            ReportFeatureStatus.IMPLEMENTED,
            ReportTypeRegistry.featureStatus(ReportType.KUNDALI.feature)
        )
    }

    @Test
    fun missingKundaliInputReturnsTypedNoData() = runBlocking {
        val missing = request.copy(chartRequest = null)
        val result = assertIs<ReportGenerationResult.Unavailable>(
            GenerateReportUseCase(Aynvora.create()).execute(
                missing,
                text
            )
        )
        assertEquals(ReportErrorCode.NO_DATA, result.code)
        assertEquals("kundali", result.reportTypeId)
    }

    @Test
    fun requestedVargasAreNamedByDivisionAndKeptAsIndependentSubsections() = runBlocking {
        val chartRequest = request.chartRequest!!
        val withD1AndD9 = chartRequest.copy(
            config = chartRequest.config.copy(
                requestedDivisionalCharts = setOf(
                    com.aynvora.core.models.DivisionalChart.D1,
                    com.aynvora.core.models.DivisionalChart.D9
                )
            ),
        )
        val result = assertIs<ReportGenerationResult.Generated>(
            GenerateReportUseCase(Aynvora.create()).execute(
                request.copy(chartRequest = withD1AndD9),
                text
            ),
        ).document
        val vargas = result.sections.single { it.id == "vargas" }
        assertEquals(listOf("D1", "D9"), vargas.subsections.map { it.id })
        assertTrue(vargas.subsections.all { it.blocks.single() is ReportTable })
    }

    @Test
    fun evidenceGraphPreservesKindsProvenanceAndOnlyMatchingLocaleEdges() = runBlocking {
        fun item(
            id: String,
            category: com.aynvora.core.intelligence.EvidenceCategory,
            locale: String,
            summary: String
        ) =
            com.aynvora.core.intelligence.EvidenceItem(
                evidenceId = id,
                domain = com.aynvora.core.feature.CoreFeatureId.ASTROLOGY,
                category = category,
                ruleId = "rule_$id",
                summary = summary,
                provenance = com.aynvora.core.intelligence.EvidenceProvenance(
                    domain = com.aynvora.core.feature.CoreFeatureId.ASTROLOGY,
                    sourceName = "Parashara corpus",
                    rulesetOrEdition = "PARASHARA_V1",
                    engineVersion = "rules-1",
                    calculationProfile = "STANDARD_VEDIC",
                    timestampEpochMs = 1_700_000_000_000,
                    locale = locale,
                ),
            )

        val graph = com.aynvora.core.intelligence.EvidenceGraph(
            queryId = "query-1",
            nodes = mapOf(
                "node-1" to item(
                    "node-1",
                    com.aynvora.core.intelligence.EvidenceCategory.INTERPRETATION,
                    "en",
                    "A source-backed reflection."
                ),
                "node-2" to item(
                    "node-2",
                    com.aynvora.core.intelligence.EvidenceCategory.FACT,
                    "en",
                    "A calculated placement."
                ),
                "node-hi" to item(
                    "node-hi",
                    com.aynvora.core.intelligence.EvidenceCategory.FACT,
                    "hi",
                    "हिंदी साक्ष्य"
                ),
            ),
            edges = listOf(
                com.aynvora.core.intelligence.EvidenceGraphEdge("node-2", "node-1", "APPLIES_RULE"),
                com.aynvora.core.intelligence.EvidenceGraphEdge("node-hi", "node-1", "MODIFIES"),
            ),
        )
        val document = assertIs<ReportGenerationResult.Generated>(
            GenerateReportUseCase(Aynvora.create()).execute(
                request.copy(evidenceGraph = graph),
                text
            ),
        ).document
        val evidence = document.sections.single { it.id == "evidence" }
        assertEquals(listOf("node-1", "node-2"), evidence.evidence.map { it.evidenceId })
        assertEquals(
            listOf(ReportContentKind.INTERPRETATION, ReportContentKind.FACT),
            evidence.evidence.map { it.contentKind })
        val table = evidence.blocks.filterIsInstance<ReportTable>().single()
        assertTrue(table.headers.any { it.key == ReportTextKey.EVIDENCE_CATEGORY.key })
        assertTrue(table.rows.flatten().none { it == "हिंदी साक्ष्य" || it == "node-hi" })
        val relations = evidence.subsections.single()
        assertEquals("evidence_relationships", relations.id)
        val relationRows = (relations.blocks.single() as ReportTable).rows
        assertEquals(1, relationRows.size)
        assertEquals("node-2", relationRows.single().first())
    }

    @Test
    fun foundationOnlyReportDoesNotRunOrInventCalculations() = runBlocking {
        val rudraksha = request.copy(reportType = ReportType.RUDRAKSHA, chartRequest = null)
        val result = assertIs<ReportGenerationResult.Unavailable>(
            GenerateReportUseCase(Aynvora.create()).execute(
                rudraksha,
                text
            )
        )
        assertEquals(ReportErrorCode.FOUNDATION_ONLY, result.code)
        assertEquals(ReportFeatureStatus.FOUNDATION_ONLY, result.featureStatus)
        assertTrue(result.reason.value.contains("foundation"))
    }

    @Test
    fun extensionReportTypesAreIsolatedAndUnregisteredReportsAreTyped() = runBlocking {
        val custom =
            ReportType.Extension("com.example.sample", com.aynvora.core.feature.CoreFeatureId.TAROT)
        val result = assertIs<ReportGenerationResult.Unavailable>(
            GenerateReportUseCase(Aynvora.create()).execute(
                request.copy(reportType = custom),
                text
            ),
        )
        assertEquals(custom.id, result.reportTypeId)
        assertEquals(ReportErrorCode.FEATURE_NOT_IMPLEMENTED, result.code)
    }

    @Test
    fun registeredExtensionGeneratorReceivesItsTypedInputWithoutKundaliCalculations() =
        runBlocking {
            val custom = ReportType.Extension(
                "com.example.sample",
                com.aynvora.core.feature.CoreFeatureId.ASTROLOGY
            )
            val expectedInput = object : ReportGeneratorInput {}
            val generator = object : ReportGenerator {
                override val reportType = custom
                override fun generate(
                    input: ReportGeneratorInput,
                    resolver: ReportTextResolver
                ): ReportDocument {
                    assertSame(expectedInput, input)
                    return sampleDocument(emptyList(), custom.id)
                }
            }
            val useCase =
                GenerateReportUseCase(Aynvora.create(), ReportGeneratorRegistry(listOf(generator)))
            val result = useCase.execute(
                request.copy(
                    reportType = custom,
                    chartRequest = null,
                    generatorInput = expectedInput
                ), text
            )
            assertEquals(
                custom.id,
                assertIs<ReportGenerationResult.Generated>(result).document.metadata.reportTypeId
            )
        }

    @Test
    fun documentFactoryDefensivelyCopiesLists() {
        val rows = mutableListOf(listOf("Mars", "Aries"))
        val sections = mutableListOf(
            ReportSection(
                "positions",
                text.text(ReportTextKey.PLANETARY_POSITIONS),
                listOf(
                    ReportTable(
                        listOf(
                            text.text(ReportTextKey.BODY),
                            text.text(ReportTextKey.SIGN)
                        ), rows
                    )
                )
            )
        )
        val doc = sampleDocument(sections)
        sections.clear()
        rows.clear()
        assertEquals(1, doc.sections.size)
        assertEquals(
            listOf(listOf("Mars", "Aries")),
            (doc.sections.single().blocks.single() as ReportTable).rows
        )
    }

    @Test
    fun pdfUseCasePassesTheSameDocumentAndReportsFailure() {
        val doc = sampleDocument(emptyList())
        var received: ReportDocument? = null
        val exporter = object : ReportPdfGenerator {
            override fun generate(document: ReportDocument): ReportPdfResult {
                received = document
                return ReportPdfResult.Failed(ReportPdfError.Failed("test_failure"))
            }
        }
        val result = GenerateReportPdfUseCase(exporter).execute(doc)
        assertSame(doc, received)
        assertEquals(
            ReportErrorCode.PDF_GENERATION_FAILED,
            assertIs<ReportPdfOutcome.Failure>(result).code
        )
    }

    @Test
    fun pdfShareAndUnsupportedPlatformEventsStayTypedAndPrivacySafe() {
        val events = mutableListOf<AnalyticsEvent>()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event
            }
        }
        val document = sampleDocument(emptyList())
        val pdfUseCase = GenerateReportPdfUseCase(object : ReportPdfGenerator {
            override fun generate(document: ReportDocument) = ReportPdfResult.Generated(
                ReportPdfArtifact(
                    "kundali.pdf",
                    bytes = byteArrayOf(37, 80, 68, 70)
                )
            )
        }, tracker)
        val artifact = assertIs<ReportPdfOutcome.Generated>(pdfUseCase.execute(document)).artifact
        val share = ShareReportUseCase(object : ReportShareService {
            override fun share(artifact: ReportPdfArtifact) = ReportShareResult.Shared
        }, tracker)
        assertEquals(ReportShareResult.Shared, share.execute(artifact, "kundali"))
        assertEquals(
            mapOf("report_type" to "kundali"),
            events.first { it.name == "report_pdf_generated" }.params
        )
        assertEquals(
            mapOf("report_type" to "kundali"),
            events.first { it.name == "report_shared" }.params
        )
        assertTrue(events.flatMap { it.params.values }
            .none { it.toString().contains("birth") || it.toString().contains("r1") })
        assertEquals(
            mapOf("report_type" to "custom"),
            AnalyticsEvent.ReportGenerated("birth_1990").params
        )
        assertEquals(
            mapOf("report_type" to "kundali", "error_code" to "unknown_error"),
            AnalyticsEvent.ReportPdfFailed("kundali", "birth_1990").params
        )

        val unsupported =
            GenerateReportPdfUseCase(UnsupportedReportPdfGenerator()).execute(document)
        assertEquals(
            ReportErrorCode.PLATFORM_UNSUPPORTED,
            assertIs<ReportPdfOutcome.Failure>(unsupported).code
        )
    }

    @Test
    fun reportAnalyticsEventsContainOnlyNonSensitiveFields() {
        val events = mutableListOf<AnalyticsEvent>()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event
            }
        }
        val useCase = GenerateReportUseCase(Aynvora.create(), analyticsTracker = tracker)
        val generated = runBlocking { useCase.execute(request, text) }
        assertIs<ReportGenerationResult.Generated>(generated)
        val event = events.single { it.name == "report_generated" }
        assertEquals(mapOf("report_type" to "kundali"), event.params)
        assertTrue(event.params.values.none {
            it.toString().contains("1990") || it.toString().contains("28.6139") || it.toString()
                .contains("New Delhi")
        })
    }

    private fun sampleDocument(
        sections: List<ReportSection>,
        reportTypeId: String = "kundali"
    ): ReportDocument {
        val metadata = ReportMetadata(
            "r1",
            reportTypeId,
            1L,
            ReportLanguage.ENGLISH,
            ReportVersion("1.0.0", "0.3.0", "1.0.0"),
            ReportIdentity(),
            com.aynvora.core.feature.CoreFeatureId.ASTROLOGY,
            ReportFeatureStatus.IMPLEMENTED
        )
        return ReportDocumentFactory.create(
            metadata,
            text.text(ReportTextKey.KUNDALI_TITLE),
            sections,
            emptyList(),
            ReportDisclaimer(
                text.text(ReportTextKey.DISCLAIMER_TITLE),
                text.text(ReportTextKey.DISCLAIMER_TEXT)
            )
        )
    }
}

internal class TestReportTextResolver(override val language: ReportLanguage) : ReportTextResolver {
    override fun text(key: ReportTextKey) = ReportText(key.key, key.key)
    override fun bodyName(body: com.aynvora.core.models.CelestialBody) = body.name
    override fun signName(sign: com.aynvora.core.models.Rashi) = sign.name
    override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra) = nakshatra.name
    override fun enumLabel(identifier: String) = identifier
    override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi) = tithi.displayName
    override fun varaName(vara: com.aynvora.astro.panchang.Vara) = vara.displayName
    override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga) = yoga.displayName
    override fun karanaName(karana: com.aynvora.astro.panchang.Karana) = karana.displayName
    override fun number(value: Double, decimalPlaces: Int) = value.toString()
    override fun birthDate(year: Int, month: Int, day: Int) = "$day/$month/$year"
    override fun birthTime(hour: Int, minute: Int, second: Int) = "$hour:$minute:$second"
    override fun generatedAtUtc(epochMillis: Long) = epochMillis.toString()
}

private object ReportSectionOrder {
    val ids = listOf(
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

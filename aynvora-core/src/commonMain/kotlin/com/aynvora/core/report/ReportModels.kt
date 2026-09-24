package com.aynvora.core.report

import com.aynvora.astro.dasha.VimshottariDashaTimeline
import com.aynvora.astro.panchang.PanchangSnapshot
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import kotlinx.serialization.Serializable

@Serializable
sealed interface ReportType {
    val id: String
    val feature: CoreFeatureId

    @Serializable
    data object KUNDALI : ReportType {
        override val id = "kundali";
        override val feature = CoreFeatureId.ASTROLOGY
    }

    @Serializable
    data object GEMSTONE : ReportType {
        override val id = "gemstone";
        override val feature = CoreFeatureId.GEMSTONE
    }

    @Serializable
    data object NUMEROLOGY : ReportType {
        override val id = "numerology";
        override val feature = CoreFeatureId.NUMEROLOGY
    }

    @Serializable
    data object RUDRAKSHA : ReportType {
        override val id = "rudraksha";
        override val feature = CoreFeatureId.RUDRAKSHA
    }

    @Serializable
    data object JADI : ReportType {
        override val id = "jadi";
        override val feature = CoreFeatureId.JADI
    }

    @Serializable
    data object YANTRA : ReportType {
        override val id = "yantra";
        override val feature = CoreFeatureId.YANTRA
    }

    @Serializable
    data object PALMISTRY : ReportType {
        override val id = "palmistry";
        override val feature = CoreFeatureId.PALMISTRY
    }

    @Serializable
    data object TAROT : ReportType {
        override val id = "tarot";
        override val feature = CoreFeatureId.TAROT
    }

    @Serializable
    data object GITA : ReportType {
        override val id = "gita";
        override val feature = CoreFeatureId.GITA
    }

    @Serializable
    data object LAL_KITAB : ReportType {
        override val id = "lal_kitab";
        override val feature = CoreFeatureId.LAL_KITAB
    }

    @Serializable
    data object GARUDA_PURAN : ReportType {
        override val id = "garuda_puran";
        override val feature = CoreFeatureId.GARUDA_PURAN
    }

    @Serializable
    data object DAILY_GUIDANCE : ReportType {
        override val id = "daily_guidance";
        override val feature = CoreFeatureId.DAILY_GUIDANCE
    }

    @Serializable
    data class Extension(override val id: String, override val feature: CoreFeatureId) :
        ReportType {
        init {
            require(id.matches(Regex("[a-z][a-z0-9_.-]{1,63}")))
        }
    }
}

@Serializable
enum class ReportLanguage(val code: String) { ENGLISH("en"), HINDI("hi") }
@Serializable
enum class ReportFeatureStatus { IMPLEMENTED, FOUNDATION_ONLY, LIMITED }
@Serializable
enum class ReportSectionStatus { INCLUDED, LIMITED, OMITTED }
@Serializable
enum class ReportUnavailableReason {
    RESULT_NOT_INCLUDED, DIVISIONAL_CHARTS_NOT_REQUESTED, TRANSIT_EPOCH_NOT_REQUESTED,
    NOT_PROVIDED_BY_ASTRO_ENGINE, INTERPRETATION_EVIDENCE_NOT_PROVIDED,
    APPROVED_CONTENT_NOT_INSTALLED, CONTENT_UNAVAILABLE_IN_LANGUAGE, TOPIC_NOT_AVAILABLE_IN_SOURCE,
}

@Serializable
enum class ReportContentKind { FACT, CALCULATION, INTERPRETATION, SOURCE, USER_CONTEXT, DISCLAIMER }
@Serializable
enum class ReportReferenceStatus { NOT_ASSESSED, EXACT_MATCH, TOLERANCE_MATCH, PROFILE_DIFFERENCE, ROUNDING_DIFFERENCE, REFERENCE_AMBIGUITY, UNSUPPORTED }

@Serializable
data class ReportText(val key: String, val value: String) { init {
    require(key.isNotBlank() && value.isNotBlank())
}
}

@Serializable
data class ReportVersion(
    val reportSchemaVersion: String,
    val calculationVersion: String,
    val contentVersion: String
)

@Serializable
data class ReportIdentity(val displayName: String? = null, val birthProfileId: String? = null)
@Serializable
data class ReportMetadata(
    val reportId: String,
    val reportTypeId: String,
    val generatedAtEpochMs: Long,
    val language: ReportLanguage,
    val version: ReportVersion,
    val identity: ReportIdentity,
    val feature: CoreFeatureId,
    val featureStatus: ReportFeatureStatus,
)

@Serializable
data class ReportSource(val sourceId: String, val name: String, val citation: String? = null)
@Serializable
data class ReportCalculationReference(
    val calculationId: String,
    val calculationProfile: String,
    val calculationVersion: String,
    val sourceId: String,
    val referenceStatus: ReportReferenceStatus = ReportReferenceStatus.NOT_ASSESSED,
)

@Serializable
data class ReportEvidence(
    val evidenceId: String,
    val contentKind: ReportContentKind,
    val text: ReportText,
    val calculation: ReportCalculationReference,
    val source: ReportSource,
)

@Serializable
sealed interface ReportBlock
@Serializable
data class ReportParagraph(val kind: ReportContentKind, val text: ReportText) : ReportBlock
@Serializable
data class ReportKeyValue(
    val label: ReportText,
    val value: String,
    val kind: ReportContentKind = ReportContentKind.FACT
) : ReportBlock

@Serializable
data class ReportMetric(
    val label: ReportText,
    val value: String,
    val unit: ReportText? = null,
    val kind: ReportContentKind = ReportContentKind.CALCULATION
) : ReportBlock

@Serializable
data class ReportTable(
    val headers: List<ReportText>,
    val rows: List<List<String>>,
    val kind: ReportContentKind = ReportContentKind.FACT
) : ReportBlock {
    init {
        require(headers.isNotEmpty() && rows.all { it.size == headers.size })
    }
}

@Serializable
data class ReportInterpretation(
    val text: ReportText,
    val traditionId: String,
    val source: ReportSource,
    val supportingEvidenceIds: List<String>,
) : ReportBlock

@Serializable
data class ReportSubsection(
    val id: String,
    val title: ReportText,
    val blocks: List<ReportBlock>,
    val evidence: List<ReportEvidence> = emptyList()
)

@Serializable
data class ReportSection(
    val id: String,
    val title: ReportText,
    val blocks: List<ReportBlock>,
    val subsections: List<ReportSubsection> = emptyList(),
    val evidence: List<ReportEvidence> = emptyList(),
)

@Serializable
data class ReportSectionAvailability(
    val sectionId: String,
    val title: ReportText,
    val status: ReportSectionStatus,
    val reasonCode: ReportUnavailableReason,
    val reason: ReportText,
)

@Serializable
data class ReportDisclaimer(val title: ReportText, val body: ReportText)

@Serializable
data class ReportDocument(
    val metadata: ReportMetadata,
    val title: ReportText,
    val sections: List<ReportSection>,
    val sectionAvailability: List<ReportSectionAvailability>,
    val disclaimer: ReportDisclaimer,
    val evidenceGraph: EvidenceGraph? = null,
) {
    init {
        require(metadata.reportTypeId.isNotBlank())
        require(sections.map { it.id }
            .distinct().size == sections.size) { "Report section ids must be unique" }
    }
}

object ReportDocumentFactory {
    private fun copyBlocks(blocks: List<ReportBlock>) = blocks.map { block ->
        when (block) {
            is ReportTable -> block.copy(
                headers = block.headers.toList(),
                rows = block.rows.map { it.toList() })

            is ReportInterpretation -> block.copy(supportingEvidenceIds = block.supportingEvidenceIds.toList())
            else -> block
        }
    }.toList()

    fun create(
        metadata: ReportMetadata,
        title: ReportText,
        sections: List<ReportSection>,
        availability: List<ReportSectionAvailability>,
        disclaimer: ReportDisclaimer,
        graph: EvidenceGraph? = null
    ) =
        ReportDocument(
            metadata,
            title,
            sections.map {
                it.copy(
                    blocks = copyBlocks(it.blocks),
                    subsections = it.subsections.map { sub ->
                        sub.copy(
                            blocks = copyBlocks(sub.blocks),
                            evidence = sub.evidence.toList()
                        )
                    },
                    evidence = it.evidence.toList()
                )
            }.toList(),
            availability.toList(),
            disclaimer,
            graph?.copy(nodes = graph.nodes.toMap(), edges = graph.edges.toList()),
        )
}

@Serializable
enum class ReportErrorCode {
    NO_DATA, INSUFFICIENT_DATA, FEATURE_NOT_IMPLEMENTED, FOUNDATION_ONLY, CALCULATION_UNAVAILABLE,
    LOCALIZATION_MISSING, PDF_GENERATION_FAILED, PLATFORM_UNSUPPORTED, INVALID_REPORT_REQUEST,
}

@Serializable
sealed interface ReportGenerationResult {
    @Serializable
    data class Generated(val document: ReportDocument) : ReportGenerationResult
    @Serializable
    data class Unavailable(
        val reportTypeId: String,
        val code: ReportErrorCode,
        val reason: ReportText,
        val featureStatus: ReportFeatureStatus? = null
    ) : ReportGenerationResult
}

data class ReportGenerationRequest(
    val reportType: ReportType,
    val chartRequest: ChartRequest? = null,
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val identity: ReportIdentity = ReportIdentity(),
    val evidenceGraph: EvidenceGraph? = null,
    val generatorInput: ReportGeneratorInput? = null,
)

interface ReportGeneratorInput

data class KundaliReportInput(
    val chart: ChartResult,
    val panchang: PanchangSnapshot,
    val dasha: VimshottariDashaTimeline,
    val calculationVersion: String,
    val identity: ReportIdentity,
    val generatedAtEpochMs: Long,
    val language: ReportLanguage,
    val evidenceGraph: EvidenceGraph? = null,
) : ReportGeneratorInput

interface ReportTextResolver {
    val language: ReportLanguage
    fun text(key: ReportTextKey): ReportText
    fun bodyName(body: com.aynvora.core.models.CelestialBody): String
    fun signName(sign: com.aynvora.core.models.Rashi): String
    fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String
    fun enumLabel(identifier: String): String
    fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String
    fun varaName(vara: com.aynvora.astro.panchang.Vara): String
    fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String
    fun karanaName(karana: com.aynvora.astro.panchang.Karana): String
    fun number(value: Double, decimalPlaces: Int = 2): String
    fun birthDate(year: Int, month: Int, day: Int): String
    fun birthTime(hour: Int, minute: Int, second: Int): String
    fun generatedAtUtc(epochMillis: Long): String
}

@Serializable
enum class ReportTextKey(val key: String) {
    KUNDALI_TITLE("report.kundali.title"), GEMSTONE_TITLE("report.gemstone.title"), NUMEROLOGY_TITLE(
        "report.numerology.title"
    ),
    RUDRAKSHA_TITLE("report.rudraksha.title"), JADI_TITLE("report.jadi.title"), YANTRA_TITLE("report.yantra.title"),
    PALMISTRY_TITLE("report.palmistry.title"), TAROT_TITLE("report.tarot.title"), GITA_TITLE("report.gita.title"),
    LAL_KITAB_TITLE("report.lal_kitab.title"), GARUDA_PURAN_TITLE("report.garuda_puran.title"), DAILY_GUIDANCE_TITLE(
        "report.daily_guidance.title"
    ),
    BIRTH_DETAILS("report.section.birth_details"), PANCHANG("report.section.panchang"), ASCENDANT("report.section.ascendant"),
    MOON_SIGN("report.section.moon_sign"), NAKSHATRA("report.section.nakshatra"), PLANETARY_POSITIONS(
        "report.section.planetary_positions"
    ),
    HOUSE_PLACEMENTS("report.section.house_placements"), RETROGRADE("report.section.retrograde"), COMBUSTION(
        "report.section.combustion"
    ),
    ASPECTS("report.section.aspects"), DIGNITIES("report.section.dignities"), RELATIONSHIPS("report.section.relationships"),
    SHADBALA("report.section.shadbala"), ASHTAKAVARGA("report.section.ashtakavarga"), SHODHANA("report.section.shodhana"),
    PINDA("report.section.pinda"), VARGAS("report.section.vargas"), DASHA("report.section.dasha"), TRANSITS(
        "report.section.transits"
    ),
    YOGAS("report.section.yogas"), DOSHA("report.section.dosha"), TIMING("report.section.timing"), PREDICTIONS(
        "report.section.predictions"
    ),
    EVIDENCE("report.section.evidence"), EVIDENCE_PROVENANCE("report.section.evidence_provenance"), LIMITATIONS(
        "report.section.limitations"
    ),
    DISCLAIMER_TITLE("report.disclaimer.title"),
    DISCLAIMER_BODY("report.disclaimer.body"), BIRTH_DATE("report.label.birth_date"), BIRTH_TIME("report.label.birth_time"),
    BIRTH_PLACE("report.label.birth_place"), TIME_ZONE("report.label.time_zone"), LATITUDE("report.label.latitude"), LONGITUDE(
        "report.label.longitude"
    ),
    JULIAN_DAY("report.label.julian_day"), AYANAMSA("report.label.ayanamsa"), CALCULATION_PROFILE("report.label.calculation_profile"),
    ENGINE_VERSION("report.label.engine_version"), CALCULATION_ID("report.label.calculation_id"), SOURCE_ID(
        "report.label.source_id"
    ),
    TRADITION_ID("report.label.tradition_id"), RULESET("report.label.ruleset"), EVIDENCE_ID("report.column.evidence_id"),
    EVIDENCE_CATEGORY("report.column.evidence_category"), SOURCE_EVIDENCE("report.column.source_evidence"),
    TARGET_EVIDENCE("report.column.target_evidence"), RELATIONSHIP("report.column.relationship"),
    EVIDENCE_RELATIONSHIPS("report.evidence.relationships"), BODY("report.column.body"), SIGN("report.column.sign"), DEGREE(
        "report.column.degree"
    ),
    NAKSHATRA_COLUMN("report.column.nakshatra"), PADA("report.column.pada"), HOUSE("report.column.house"), STATUS(
        "report.column.status"
    ),
    VALUE("report.column.value"), START("report.column.start"), END("report.column.end"), PROFILE("report.label.profile"),
    DASHA_LEVEL("report.column.dasha_level"), DASHA_PLANET("report.column.dasha_planet"),
    MAHADASHA("report.dasha.mahadasha"), ANTARDASHA("report.dasha.antardasha"), PRATYANTARDASHA("report.dasha.pratyantardasha"),
    REFERENCE_STATUS("report.label.reference_status"), TITHI("report.label.tithi"), VARA("report.label.vara"),
    YOGA("report.label.yoga"), KARANA("report.label.karana"), NOT_PROVIDED("report.reason.not_provided"), NO_INTERPRETATION(
        "report.reason.no_interpretation"
    ),
    DISCLAIMER_TEXT("report.disclaimer.content"), FOUNDATION_ONLY_REASON("report.reason.foundation_only"), REPORT_NOT_IMPLEMENTED(
        "report.reason.report_not_implemented"
    ),
    LOADING("report.state.loading"), EMPTY("report.state.empty"), ERROR("report.state.error"), GENERATE_PDF(
        "report.action.generate_pdf"
    ),
    SHARE("report.action.share"), PDF_READY("report.state.pdf_ready"), PDF_FAILED("report.state.pdf_failed"), OPEN_REPORT(
        "report.action.open_kundali"
    ),
    SOURCE_ASTRO_ENGINE("report.source.astro_engine"), SOURCE_CLASSICAL_PROFILE("report.source.classical_profile"),
    INVALID_INPUT("report.reason.invalid_input"), CALCULATION_FAILED("report.reason.calculation_failed"),
    INSUFFICIENT_DATA("report.reason.insufficient_data"), LOCALIZATION_ERROR("report.reason.localization_error"),
    INTERPRETATION_NOT_AVAILABLE("report.reason.interpretation_not_available"),
    SECTION_NAVIGATION("report.section_navigation"), REPORT_METADATA("report.metadata"), SCHEMA_VERSION(
        "report.schema_version"
    ),
    CALCULATION_VERSION("report.calculation_version"), CONTENT_VERSION("report.content_version"), CONTENT_FACT(
        "report.content_kind.fact"
    ),
    CONTENT_CALCULATION("report.content_kind.calculation"), CONTENT_INTERPRETATION("report.content_kind.interpretation"),
    CONTENT_SOURCE("report.content_kind.source"), CONTENT_USER_CONTEXT("report.content_kind.user_context"),
    CONTENT_DISCLAIMER("report.content_kind.disclaimer"), DIVISIONAL_NOT_REQUESTED("report.reason.divisional_not_requested"),
    TRANSIT_EPOCH_MISSING("report.reason.transit_epoch_missing"), NOT_FROM_ENGINE("report.reason.not_from_engine"),
    EVIDENCE_NOT_PROVIDED("report.reason.evidence_not_provided"), RESULT_NOT_INCLUDED("report.reason.result_not_included"),
    GENERATED_AT("report.generated_at"), SAVE_PDF_DIALOG("report.save_pdf_dialog"), PDF_GENERATING("report.state.pdf_generating"),
    GARUDA_INTRODUCTION("garuda.report.introduction"), GARUDA_SOURCE_INFORMATION("garuda.report.source_information"),
    GARUDA_AVAILABLE_TOPICS("garuda.report.available_topics"), GARUDA_TOPIC_DETAILS("garuda.report.topic_details"),
    GARUDA_TRADITIONAL_TEACHINGS("garuda.report.traditional_teachings"), GARUDA_SOURCE_REFERENCES("garuda.report.source_references"),
    GARUDA_LIMITATIONS("garuda.report.limitations"), GARUDA_SOURCE_TEXT("garuda.source_text"), GARUDA_SOURCE_MEANING(
        "garuda.source_meaning"
    ),
    GARUDA_REFERENCE("garuda.reference"), GARUDA_TOPIC_INTRODUCTION("garuda.topic.introduction"),
    GARUDA_TOPIC_DIALOGUE_CONTEXT("garuda.topic.dialogue_context"), GARUDA_TOPIC_DHARMA_AND_CONDUCT(
        "garuda.topic.dharma_and_conduct"
    ),
    GARUDA_TOPIC_TRADITIONAL_TEACHINGS("garuda.topic.traditional_teachings"), GARUDA_TOPIC_LIFE_GUIDANCE(
        "garuda.topic.life_guidance"
    ),
    GARUDA_TOPIC_DEATH_AND_AFTERLIFE("garuda.topic.death_and_afterlife"), GARUDA_TOPIC_KARMA("garuda.topic.karma"),
    GARUDA_TOPIC_RITUAL_PRACTICES("garuda.topic.ritual_practices"), GARUDA_TOPIC_SPIRITUAL_GUIDANCE(
        "garuda.topic.spiritual_guidance"
    ),
    GARUDA_TOPIC_OTHER_SOURCE_BACKED("garuda.topic.other_source_backed"), GARUDA_TOPIC_NOT_IN_SOURCE(
        "garuda.status.topic_not_in_source"
    ),
    GARUDA_PACKAGE_MISSING("garuda.status.package_missing"), GARUDA_LANGUAGE_CONTENT_MISSING("garuda.status.language_content_missing"),
    GARUDA_DISCLAIMER_TITLE("garuda.disclaimer.title"), GARUDA_DISCLAIMER_BODY("garuda.disclaimer.body"),
    GARUDA_SCRIPTURAL_PREFIX("garuda.scriptural_prefix"), GARUDA_COLUMN_TOPIC("garuda.column.topic"),
    GARUDA_COLUMN_CONTENT_COUNT("garuda.column.content_count"), GARUDA_COLUMN_EDITION("garuda.column.edition"),
    GARUDA_COLUMN_CHAPTER("garuda.column.chapter"), GARUDA_COLUMN_SECTION("garuda.column.section"),
    GARUDA_COLUMN_REFERENCE("garuda.column.canonical_reference"), GARUDA_COLUMN_CONTENT_VERSION("garuda.column.content_version"),
    GARUDA_COLUMN_LANGUAGE("garuda.column.language"),
    GARUDA_CONTENT_NOT_INSTALLED("garuda.report.unavailable.package"), GARUDA_LANGUAGE_NOT_INSTALLED(
        "garuda.report.unavailable.language"
    ),
}

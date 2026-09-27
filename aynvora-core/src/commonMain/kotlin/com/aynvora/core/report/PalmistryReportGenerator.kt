package com.aynvora.core.report

import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmistryContentPackage

/**
 * Input for the Palmistry / Hastrekha report.
 */
data class PalmistryReportInput(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val session: PalmReadingSession,
    val evidenceGraph: EvidenceGraph? = null,
    val identity: ReportIdentity = ReportIdentity(),
) : ReportGeneratorInput

/**
 * Report generator for Palmistry / Hastrekha.
 *
 * Produces structured, non-medical, localized self-reflection reports grounded
 * in classical Samudrika Shastra tradition.
 *
 * Uses key-driven localization via [ReportTextResolver].
 */
class PalmistryReportGenerator : ReportGenerator {

    override val reportType: ReportType = ReportType.PALMISTRY

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver,
    ): ReportDocument {
        require(input is PalmistryReportInput) { "PalmistryReportGenerator requires PalmistryReportInput" }
        require(resolver.language == input.language) { "Resolver language must match input language" }

        val session = input.session
        val sections = mutableListOf<ReportSection>()

        // 1. Disclaimer Section
        val disclaimerTitle = resolver.rawText(
            "palmistry.report.disclaimer.title",
            "Traditional Disclosure & Ethical Guidance"
        )
        val disclaimerText = resolver.rawText(
            "palmistry.report.disclaimer.body",
            "Hastrekha observations are presented solely for traditional cultural reflection, self-inquiry, and personal perspective. They do not constitute guaranteed future prophecies, medical diagnosis, or legal/financial advice.",
        )

        sections += ReportSection(
            id = "disclaimer",
            title = ReportText("palmistry.report.disclaimer.title", disclaimerTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText("palmistry.report.disclaimer.body", disclaimerText),
                    kind = ReportContentKind.DISCLAIMER,
                )
            )
        )

        // 2. Hand & Measurement Overview
        val handKey =
            if (session.handType == HandType.RIGHT) "palmistry.results.hand_right_label" else "palmistry.results.hand_left_label"
        val handStr = resolver.rawText(
            handKey,
            if (session.handType == HandType.RIGHT) "Right Hand" else "Left Hand"
        )

        val overviewTitle =
            resolver.rawText("palmistry.report.overview.title", "Palm Anatomical Overview")
        val selectedHandLabel = resolver.rawText("palmistry.report.selected_hand", "Selected Hand")
        val palmShapeLabel = resolver.rawText("palmistry.report.palm_shape", "Palm Shape")
        val lineClarityLabel =
            resolver.rawText("palmistry.report.line_clarity", "Overall Line Clarity")
        val analysisVersionLabel =
            resolver.rawText("palmistry.report.analysis_version", "Analysis Version")
        val tableFeatureLabel = resolver.rawText("palmistry.report.table.feature", "Feature")
        val tableValueLabel = resolver.rawText("palmistry.report.table.value", "Observed Value")

        val finding = session.finding

        sections += ReportSection(
            id = "overview",
            title = ReportText("palmistry.report.overview.title", overviewTitle),
            blocks = listOf(
                ReportKeyValue(
                    label = ReportText("palmistry.h1", selectedHandLabel),
                    value = handStr,
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("palmistry.h2", palmShapeLabel),
                    value = finding?.shape?.name ?: "STANDARD",
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("palmistry.h3", lineClarityLabel),
                    value = "${((finding?.overallClarity ?: 0.8f) * 100).toInt()}%",
                    kind = ReportContentKind.FACT,
                ),
                ReportTable(
                    headers = listOf(
                        ReportText("palmistry.report.table.feature", tableFeatureLabel),
                        ReportText("palmistry.report.table.value", tableValueLabel),
                    ),
                    rows = listOf(
                        listOf(selectedHandLabel, handStr),
                        listOf(palmShapeLabel, finding?.shape?.name ?: "STANDARD"),
                        listOf(
                            lineClarityLabel,
                            "${((finding?.overallClarity ?: 0.8f) * 100).toInt()}%"
                        ),
                        listOf(analysisVersionLabel, finding?.analysisVersion ?: "1.0.0"),
                    ),
                    kind = ReportContentKind.FACT,
                )
            )
        )

        // 3. Detected Line Findings & Meanings
        val linesTitle = resolver.rawText(
            "palmistry.report.lines.title",
            "Major Lines & Traditional Reflections"
        )
        val cautionPrefix = resolver.rawText("palmistry.report.caution_prefix", "↳ Reflection: ")
        val lineBlocks = mutableListOf<ReportBlock>()

        session.meanings.forEach { meaning ->
            lineBlocks += ReportKeyValue(
                label = ReportText("meaning_${meaning.featureType}", meaning.title),
                value = meaning.description,
                kind = ReportContentKind.FACT,
            )
            lineBlocks += ReportParagraph(
                text = ReportText(
                    "meaning_interp_${meaning.featureType}",
                    meaning.traditionalInterpretation
                ),
                kind = ReportContentKind.INTERPRETATION,
            )
            if (meaning.caution.isNotBlank()) {
                lineBlocks += ReportParagraph(
                    text = ReportText("caution_text", "$cautionPrefix${meaning.caution}"),
                    kind = ReportContentKind.DISCLAIMER,
                )
            }
        }

        sections += ReportSection(
            id = "lines",
            title = ReportText("palmistry.report.lines.title", linesTitle),
            blocks = lineBlocks,
        )

        // 4. Questions & Answers (if present)
        if (session.questions.isNotEmpty()) {
            val qnaTitle = resolver.rawText(
                "palmistry.report.qna.title",
                "Conversational Inquiry & Reflections"
            )
            val qnaBlocks = mutableListOf<ReportBlock>()

            session.questions.forEachIndexed { idx, q ->
                qnaBlocks += ReportKeyValue(
                    label = ReportText("q_${idx}", "Q${idx + 1}"),
                    value = q.questionText,
                    kind = ReportContentKind.USER_CONTEXT,
                )
                val ans = q.answerInterpretation ?: q.answerSummary ?: ""
                qnaBlocks += ReportParagraph(
                    text = ReportText("ans_${idx}", ans),
                    kind = ReportContentKind.INTERPRETATION,
                )
            }

            sections += ReportSection(
                id = "questions",
                title = ReportText("palmistry.report.qna.title", qnaTitle),
                blocks = qnaBlocks,
            )
        }

        // 5. Source Attribution
        val attrTitle = resolver.rawText("palmistry.report.attr.title", "Source & Provenance")
        val attrBody = resolver.rawText(
            "palmistry.report.attr.body",
            mapOf(
                "source" to PalmistryContentPackage.SOURCE_SAMUDRIKA,
                "version" to PalmistryContentPackage.CONTENT_VERSION
            ),
            "Content Source: ${PalmistryContentPackage.SOURCE_SAMUDRIKA} (Version ${PalmistryContentPackage.CONTENT_VERSION}). Analyzed strictly on-device by the AYNVORA Palm Vision & Intelligence Platform.",
        )

        sections += ReportSection(
            id = "attribution",
            title = ReportText("palmistry.report.attr.title", attrTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText("attr_body", attrBody),
                    kind = ReportContentKind.SOURCE,
                )
            )
        )

        return ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "palm_${session.id}",
                reportTypeId = ReportType.PALMISTRY.id,
                generatedAtEpochMs = input.generatedAtEpochMs,
                language = input.language,
                version = ReportVersion(
                    reportSchemaVersion = "1.0.0",
                    calculationVersion = session.finding?.analysisVersion ?: "1.0.0",
                    contentVersion = PalmistryContentPackage.CONTENT_VERSION,
                ),
                identity = input.identity,
                feature = reportType.feature,
                featureStatus = ReportFeatureStatus.IMPLEMENTED,
            ),
            title = resolver.text(ReportTextKey.PALMISTRY_TITLE),
            sections = sections,
            availability = emptyList(),
            disclaimer = ReportDisclaimer(
                title = ReportText("palmistry.report.disclaimer.title", disclaimerTitle),
                body = ReportText("palmistry.report.disclaimer.body", disclaimerText),
            ),
            graph = input.evidenceGraph,
        )
    }
}

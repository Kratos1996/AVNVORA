package com.aynvora.core.report

import com.aynvora.core.ai.knowledge.GitaKnowledgePack
import com.aynvora.core.ai.knowledge.GitaVerseEntry
import com.aynvora.core.intelligence.EvidenceGraph

/**
 * Input for the Bhagavad Gita Scriptural Reflection Report.
 */
data class GitaReportInput(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val verses: List<GitaVerseEntry> = emptyList(),
    val identity: ReportIdentity = ReportIdentity(),
    val evidenceGraph: EvidenceGraph? = null,
) : ReportGeneratorInput

/**
 * Report generator for Bhagavad Gita scripture reflections.
 * Produces structured, non-fatalistic, scripturally-grounded reports from verified public domain editions.
 */
class GitaReportGenerator : ReportGenerator {

    override val reportType: ReportType = ReportType.GITA

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver,
    ): ReportDocument {
        require(input is GitaReportInput) { "GitaReportGenerator requires GitaReportInput" }
        require(resolver.language == input.language) { "Resolver language must match input language" }

        val gitaPack = GitaKnowledgePack(input.language.code)
        val defaultVerses = GitaKnowledgePack.defaultGitaVerses()
        val verses = if (input.verses.isNotEmpty()) input.verses else defaultVerses

        val sections = mutableListOf<ReportSection>()

        // 1. Scriptural Foundation & Ethical Disclosure
        val disclosureTitle = resolver.rawText("gita.report.disclosure_title", "Scriptural Provenance & Ethical Reflection")
        val disclosureBody = resolver.rawText(
            "gita.report.disclosure_body",
            "This Bhagavad Gita reflection draws strictly from canonical, verified verses of the Srimad Bhagavad Gita in the public domain. It offers philosophical wisdom, contemplation on duty (Dharma), and mindful action (Nishkama Karma). It does not provide deterministic life predictions or claim supernatural certainty."
        )

        sections += ReportSection(
            id = "scriptural_disclosure",
            title = ReportText("gita.report.disclosure_title", disclosureTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText("gita.report.disclosure_body", disclosureBody),
                    kind = ReportContentKind.DISCLAIMER,
                )
            )
        )

        // 2. Selected Verses & Teachings
        val versesTitle = resolver.rawText("gita.report.selected_verses_title", "Sacred Verses & Philosophical Meaning")
        val verseSubsections = verses.map { verse ->
            val subTitle = "Chapter ${verse.chapter}, Verse ${verse.verse}"
            val blocks = listOf(
                ReportParagraph(
                    text = ReportText("gita.verse.sanskrit.${verse.chapter}.${verse.verse}", verse.sanskrit),
                    kind = ReportContentKind.FACT,
                ),
                ReportParagraph(
                    text = ReportText("gita.verse.transliteration.${verse.chapter}.${verse.verse}", verse.transliteration),
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("gita.label.translation", "Translation"),
                    value = verse.translation,
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("gita.label.commentary", "Commentary & Guidance"),
                    value = verse.traditionalCommentary,
                    kind = ReportContentKind.INTERPRETATION,
                ),
                ReportKeyValue(
                    label = ReportText("gita.label.themes", "Core Themes"),
                    value = verse.themes.joinToString(", "),
                    kind = ReportContentKind.FACT,
                ),
            )
            ReportSubsection(
                id = "verse_${verse.chapter}_${verse.verse}",
                title = ReportText("gita.verse.heading", subTitle),
                blocks = blocks,
            )
        }

        sections += ReportSection(
            id = "sacred_verses",
            title = ReportText("gita.report.selected_verses_title", versesTitle),
            blocks = emptyList(),
            subsections = verseSubsections,
        )

        // 3. Source Provenance
        val sourceInfo = gitaPack.sourceReferences.firstOrNull()
        val provenanceTitle = resolver.rawText("gita.report.provenance_title", "Source Edition & Textual Authority")
        val provenanceRows = listOf(
            listOf("Source", sourceInfo?.sourceName ?: "Srimad Bhagavad Gita"),
            listOf("Edition", sourceInfo?.rulesetOrEdition ?: "Canonical Public Domain Edition"),
            listOf("Provenance", sourceInfo?.provenancePolicy ?: "Public Domain"),
            listOf("Tradition", "Vedanta / Sanatana Dharma (Mahabharata Bhishma Parva)"),
        )

        sections += ReportSection(
            id = "gita_provenance",
            title = ReportText("gita.report.provenance_title", provenanceTitle),
            blocks = listOf(
                ReportTable(
                    headers = listOf(ReportText("col.attribute", "Attribute"), ReportText("col.detail", "Detail")),
                    rows = provenanceRows,
                    kind = ReportContentKind.SOURCE,
                )
            )
        )

        val metadata = ReportMetadata(
            reportId = "gita_${input.generatedAtEpochMs}",
            reportTypeId = ReportType.GITA.id,
            generatedAtEpochMs = input.generatedAtEpochMs,
            language = input.language,
            version = ReportVersion(
                reportSchemaVersion = "1.0.0",
                calculationVersion = "1.0.0",
                contentVersion = "1.0.0",
            ),
            identity = input.identity,
            feature = com.aynvora.core.feature.CoreFeatureId.GITA,
            featureStatus = ReportFeatureStatus.IMPLEMENTED,
        )

        return ReportDocument(
            metadata = metadata,
            title = ReportText("gita.report.title", resolver.rawText("report.gita.title", "Srimad Bhagavad Gita Reflection")),
            sections = sections,
            sectionAvailability = emptyList(),
            disclaimer = ReportDisclaimer(
                title = ReportText("gita.report.disclosure_title", disclosureTitle),
                body = ReportText("gita.report.disclosure_body", disclosureBody),
            ),
            evidenceGraph = input.evidenceGraph,
        )
    }
}

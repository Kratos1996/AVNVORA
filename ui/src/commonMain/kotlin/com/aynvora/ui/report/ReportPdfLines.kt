package com.aynvora.ui.report

import com.aynvora.core.report.ReportBlock
import com.aynvora.core.report.ReportContentKind
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportEvidence
import com.aynvora.core.report.ReportInterpretation
import com.aynvora.core.report.ReportKeyValue
import com.aynvora.core.report.ReportMetric
import com.aynvora.core.report.ReportParagraph
import com.aynvora.core.report.ReportTable
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver

enum class ReportPdfLineKind { TITLE, HEADING, SUBHEADING, BODY, TABLE_HEADER, METADATA }
data class ReportPdfLine(val text: String, val kind: ReportPdfLineKind)

/** Converts the already-localized ReportDocument into shared drawing lines for host PDF renderers. */
fun ReportDocument.toReportPdfLines(resolver: ReportTextResolver): List<ReportPdfLine> = buildList {
    add(ReportPdfLine(title.value, ReportPdfLineKind.TITLE))
    add(
        ReportPdfLine(
            "${resolver.text(ReportTextKey.REPORT_METADATA).value} · ${
                resolver.generatedAtUtc(
                    metadata.generatedAtEpochMs
                )
            }", ReportPdfLineKind.METADATA
        )
    )
    add(
        ReportPdfLine(
            "${resolver.text(ReportTextKey.SCHEMA_VERSION).value}: ${metadata.version.reportSchemaVersion}",
            ReportPdfLineKind.METADATA
        )
    )
    add(
        ReportPdfLine(
            "${resolver.text(ReportTextKey.CALCULATION_VERSION).value}: ${metadata.version.calculationVersion}",
            ReportPdfLineKind.METADATA
        )
    )
    add(
        ReportPdfLine(
            "${resolver.text(ReportTextKey.CONTENT_VERSION).value}: ${metadata.version.contentVersion}",
            ReportPdfLineKind.METADATA
        )
    )
    metadata.identity.displayName?.let { add(ReportPdfLine(it, ReportPdfLineKind.METADATA)) }
    sections.forEach { section ->
        add(ReportPdfLine(section.title.value, ReportPdfLineKind.HEADING))
        section.blocks.forEach { block -> addBlock(this, block, resolver) }
        section.subsections.forEach { subsection ->
            add(ReportPdfLine(subsection.title.value, ReportPdfLineKind.SUBHEADING))
            subsection.blocks.forEach { block -> addBlock(this, block, resolver) }
            subsection.evidence.forEach { addEvidenceLine(this, it, resolver) }
        }
        section.evidence.forEach { addEvidenceLine(this, it, resolver) }
    }
    add(ReportPdfLine(disclaimer.title.value, ReportPdfLineKind.HEADING))
    add(ReportPdfLine(disclaimer.body.value, ReportPdfLineKind.BODY))
}

private fun addBlock(
    lines: MutableList<ReportPdfLine>,
    block: ReportBlock,
    resolver: ReportTextResolver
) {
    when (block) {
        is ReportParagraph -> lines += ReportPdfLine(
            "${resolver.contentKindLabel(block.kind)}: ${block.text.value}",
            ReportPdfLineKind.BODY
        )

        is ReportKeyValue -> lines += ReportPdfLine(
            "${resolver.contentKindLabel(block.kind)} · ${block.label.value}: ${block.value}",
            ReportPdfLineKind.BODY
        )

        is ReportMetric -> lines += ReportPdfLine(
            "${resolver.contentKindLabel(block.kind)} · ${block.label.value}: ${block.value}${
                block.unit?.let { " ${it.value}" }.orEmpty()
            }", ReportPdfLineKind.BODY
        )

        is ReportTable -> {
            lines += ReportPdfLine(
                resolver.contentKindLabel(block.kind),
                ReportPdfLineKind.METADATA
            )
            lines += ReportPdfLine(
                block.headers.joinToString("  |  ") { it.value },
                ReportPdfLineKind.TABLE_HEADER
            )
            block.rows.forEach { row ->
                lines += ReportPdfLine(
                    row.joinToString("  |  "),
                    ReportPdfLineKind.BODY
                )
            }
        }

        is ReportInterpretation -> lines += ReportPdfLine(
            "${resolver.text(ReportTextKey.CONTENT_INTERPRETATION).value}: ${block.text.value} · ${block.source.name} · ${
                resolver.text(
                    ReportTextKey.TRADITION_ID
                ).value
            }: ${block.traditionId} · ${block.supportingEvidenceIds.joinToString()}",
            ReportPdfLineKind.BODY
        )
    }
}

private fun addEvidenceLine(
    lines: MutableList<ReportPdfLine>,
    evidence: ReportEvidence,
    resolver: ReportTextResolver
) {
    lines += ReportPdfLine(
        "${resolver.text(ReportTextKey.CONTENT_SOURCE).value}: ${evidence.source.name} · ${
            resolver.text(
                ReportTextKey.SOURCE_ID
            ).value
        }: ${evidence.source.sourceId} · ${resolver.text(ReportTextKey.CALCULATION_ID).value}: ${evidence.calculation.calculationId} · ${
            resolver.text(
                ReportTextKey.PROFILE
            ).value
        }: ${evidence.calculation.calculationProfile} · ${resolver.text(ReportTextKey.CALCULATION_VERSION).value}: ${evidence.calculation.calculationVersion} · ${
            resolver.enumLabel(
                evidence.calculation.referenceStatus.name
            )
        }",
        ReportPdfLineKind.METADATA,
    )
}

private fun ReportTextResolver.contentKindLabel(kind: ReportContentKind): String = text(
    when (kind) {
        ReportContentKind.FACT -> ReportTextKey.CONTENT_FACT
        ReportContentKind.CALCULATION -> ReportTextKey.CONTENT_CALCULATION
        ReportContentKind.INTERPRETATION -> ReportTextKey.CONTENT_INTERPRETATION
        ReportContentKind.SOURCE -> ReportTextKey.CONTENT_SOURCE
        ReportContentKind.USER_CONTEXT -> ReportTextKey.CONTENT_USER_CONTEXT
        ReportContentKind.DISCLAIMER -> ReportTextKey.CONTENT_DISCLAIMER
    },
).value

package com.aynvora.core.report

import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotDisclaimer
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpread

/**
 * Input for the Tarot reflection report.
 *
 * Created only from a validated local [TarotReading] and associated localized [TarotCardContent].
 * Never connected to astrological calculation or supernatural claims.
 *
 * Governance invariants:
 * - All card content must be pre-fetched approved local content.
 * - The report is strictly non-predictive. No claim about future events is permitted.
 * - The [reading] must not have been tampered with after being drawn.
 */
data class TarotReportInput(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val reading: TarotReading,
    val cardContents: List<TarotCardContent>,
    val spread: TarotSpread,
    val evidenceGraph: EvidenceGraph? = null,
    val identity: ReportIdentity = ReportIdentity(),
) : ReportGeneratorInput {
    init {
        require(reading.draws.isNotEmpty()) { "Tarot report requires at least one drawn card" }
        if (cardContents.isNotEmpty()) {
            require(cardContents.all { it.language == language.code }) {
                "All card content must be in the same language as the report"
            }
        }
    }
}

/**
 * Report generator for the Tarot Reflection feature.
 *
 * Produces a structured, localized reflection report from a completed [TarotReading] and
 * its approved [TarotCardContent].
 *
 * Report sections:
 * 1. Disclaimer (non-predictive disclosure)
 * 2. Spread Overview (spread type and draw summary table)
 * 3. Card Reflections (one section per drawn card: position, orientation, meaning, keywords)
 * 4. Content Attribution (source provenance for all content items)
 *
 * Governance:
 * - NEVER produces text suggesting predictive certainty.
 * - All card meanings are sourced from pre-approved [TarotCardContent].
 * - No AI-generated content is introduced in this generator.
 * - Report clearly marks every interpretation as "reflective" not "prophetic".
 */
class TarotReportGenerator : ReportGenerator {

    override val reportType: ReportType = ReportType.TAROT

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver,
    ): ReportDocument {
        require(input is TarotReportInput) {
            "TarotReportGenerator requires TarotReportInput"
        }
        require(resolver.language == input.language) {
            "Report resolver language must match input language"
        }

        val cardContentIndex = input.cardContents.associateBy { it.cardId }
        val sections = mutableListOf<ReportSection>()

        // ── Section 1: Disclaimer ────────────────────────────────────────────
        sections += buildDisclaimerSection(resolver)

        // ── Section 2: Spread Overview ───────────────────────────────────────
        sections += buildSpreadOverviewSection(input, cardContentIndex, resolver)

        // ── Section 3: Card-by-Card Reflection ──────────────────────────────
        input.reading.draws.forEachIndexed { index, draw ->
            val content = cardContentIndex[draw.card.id]
            sections += buildCardReflectionSection(index, draw, content, resolver)
        }

        // ── Section 4: Attribution & Source Information ──────────────────────
        sections += buildAttributionSection(input, cardContentIndex, resolver)

        val disclaimerTitle = resolver.rawText(
            "tarot.report.disclaimer_title",
            "Reflection & Mindful Use Disclosure"
        )
        val disclaimerBody = resolver.rawText(
            "tarot.report.disclaimer_body",
            TarotDisclaimer.ENGLISH_TEXT
        )

        return ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "tarot_${input.reading.id}",
                reportTypeId = ReportType.TAROT.id,
                generatedAtEpochMs = input.generatedAtEpochMs,
                language = input.language,
                version = ReportVersion(
                    reportSchemaVersion = "1.0.0",
                    calculationVersion = "DETERMINISTIC_DRAW",
                    contentVersion = input.cardContents.firstOrNull()?.contentVersion?.toString()
                        ?: "1",
                ),
                identity = input.identity,
                feature = reportType.feature,
                featureStatus = ReportFeatureStatus.IMPLEMENTED,
            ),
            title = resolver.text(ReportTextKey.TAROT_TITLE),
            sections = sections,
            availability = emptyList(),
            disclaimer = ReportDisclaimer(
                title = ReportText("tarot.disclaimer_title", disclaimerTitle),
                body = ReportText("tarot.disclaimer_body", disclaimerBody),
            ),
            graph = input.evidenceGraph,
        )
    }

    private fun buildDisclaimerSection(resolver: ReportTextResolver): ReportSection {
        val disclaimerTitle = resolver.rawText(
            "tarot.report.disclaimer_title",
            "Reflection & Mindful Use Disclosure"
        )
        val disclaimerBody = resolver.rawText(
            "tarot.report.disclaimer_body",
            "Tarot readings in AYNVORA are offered strictly for relaxation, introspection, and personal reflection. They do not constitute scientific prediction, medical, legal, or financial advice, nor guarantee future events. All interpretations are contemplative perspectives, not absolute outcomes."
        )

        return ReportSection(
            id = "tarot_disclaimer",
            title = ReportText("tarot.disclaimer_title", disclaimerTitle),
            blocks = listOf(
                ReportParagraph(
                    kind = ReportContentKind.DISCLAIMER,
                    text = ReportText("tarot.disclaimer_body", disclaimerBody),
                ),
            ),
        )
    }

    private fun buildSpreadOverviewSection(
        input: TarotReportInput,
        cardContentIndex: Map<String, TarotCardContent>,
        resolver: ReportTextResolver,
    ): ReportSection {
        val sectionTitle = resolver.rawText("tarot.report.spread_overview", "Spread Overview")
        val spreadName = input.spread.name
        val spreadDesc = input.spread.description

        val deckLabel = resolver.rawText("tarot.report.label.deck", "Deck")
        val spreadLabel = resolver.rawText("tarot.report.label.spread", "Spread")
        val cardsLabel = resolver.rawText("tarot.report.label.cards_drawn", "Cards Drawn")

        val tableRows = input.reading.draws.map { draw ->
            val content = cardContentIndex[draw.card.id]
            val orientationLabel = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                resolver.rawText("tarot.report.orientation.upright", "Upright")
            } else {
                resolver.rawText("tarot.report.orientation.reversed", "Reversed")
            }
            listOf(
                draw.position.name,
                content?.title ?: draw.card.name,
                orientationLabel,
            )
        }

        val positionHeader = resolver.rawText("tarot.report.column.position", "Position")
        val cardHeader = resolver.rawText("tarot.report.column.card", "Card")
        val orientationHeader = resolver.rawText("tarot.report.column.orientation", "Orientation")

        return ReportSection(
            id = "tarot_spread_overview",
            title = ReportText("tarot.report.spread_overview", sectionTitle),
            blocks = listOf(
                ReportKeyValue(
                    label = ReportText("tarot.report.label.spread", spreadLabel),
                    value = spreadName,
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("tarot.report.label.deck", deckLabel),
                    value = input.reading.deckId.replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("tarot.report.label.cards_drawn", cardsLabel),
                    value = input.reading.draws.size.toString(),
                    kind = ReportContentKind.FACT,
                ),
                ReportParagraph(
                    kind = ReportContentKind.SOURCE,
                    text = ReportText("tarot.report.spread_description", spreadDesc),
                ),
                ReportTable(
                    headers = listOf(
                        ReportText("tarot.report.column.position", positionHeader),
                        ReportText("tarot.report.column.card", cardHeader),
                        ReportText("tarot.report.column.orientation", orientationHeader),
                    ),
                    rows = tableRows,
                    kind = ReportContentKind.FACT,
                ),
            ),
        )
    }

    private fun buildCardReflectionSection(
        index: Int,
        draw: TarotCardDraw,
        content: TarotCardContent?,
        resolver: ReportTextResolver,
    ): ReportSection {
        val cardTitle = content?.title ?: draw.card.name
        val orientationLabel = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
            resolver.rawText("tarot.report.orientation.upright", "Upright")
        } else {
            resolver.rawText("tarot.report.orientation.reversed", "Reversed")
        }

        val sectionTitle = "$cardTitle ($orientationLabel)"
        val positionLabel = resolver.rawText("tarot.report.label.position", "Position")
        val keywordsLabel = resolver.rawText("tarot.report.label.keywords", "Keywords")

        val blocks = mutableListOf<ReportBlock>()

        blocks += ReportKeyValue(
            label = ReportText("tarot.report.label.position", positionLabel),
            value = draw.position.name,
            kind = ReportContentKind.FACT,
        )

        content?.let { c ->
            if (c.shortDescription.isNotBlank()) {
                blocks += ReportParagraph(
                    kind = ReportContentKind.FACT,
                    text = ReportText("tarot.report.card.short_desc", c.shortDescription),
                )
            }

            val meaning = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                c.uprightMeaning else c.reversedMeaning

            blocks += ReportInterpretation(
                text = ReportText("tarot.report.card.meaning.$index", meaning),
                traditionId = "tarot_contemplative",
                source = ReportSource(
                    sourceId = c.sourceAttribution,
                    name = c.sourceAttribution,
                    citation = "AYNVORA Contemplative Traditions Archive · Content Version ${c.contentVersion}",
                ),
                supportingEvidenceIds = listOf("tarot_card_${draw.card.id}"),
            )

            if (c.keywords.isNotEmpty()) {
                blocks += ReportKeyValue(
                    label = ReportText("tarot.report.label.keywords", keywordsLabel),
                    value = c.keywords.joinToString(" · "),
                    kind = ReportContentKind.FACT,
                )
            }
        } ?: run {
            blocks += ReportParagraph(
                kind = ReportContentKind.DISCLAIMER,
                text = ReportText(
                    "tarot.report.content_unavailable",
                    resolver.rawText(
                        "tarot.report.content_unavailable",
                        "Reflective content not available for this card."
                    ),
                ),
            )
        }

        return ReportSection(
            id = "tarot_card_$index",
            title = ReportText("tarot.report.card.section.$index", sectionTitle),
            blocks = blocks,
        )
    }

    private fun buildAttributionSection(
        input: TarotReportInput,
        cardContentIndex: Map<String, TarotCardContent>,
        resolver: ReportTextResolver,
    ): ReportSection {
        val sectionTitle = resolver.rawText(
            "tarot.report.attribution",
            "Content Attribution & Provenance"
        )

        val attributions = input.reading.draws
            .mapNotNull { draw -> cardContentIndex[draw.card.id] }
            .distinctBy { it.sourceAttribution }

        val sourceLabel = resolver.rawText("tarot.report.column.source", "Source")
        val versionLabel = resolver.rawText("tarot.report.column.version", "Version")
        val languageLabel = resolver.rawText("tarot.report.column.language", "Language")

        val tableRows = attributions.map { content ->
            listOf(
                content.sourceAttribution,
                "v${content.contentVersion}",
                content.language.uppercase(),
            )
        }

        val nonPredictiveNotice = resolver.rawText(
            "tarot.report.non_predictive_notice",
            "This report is for reflective contemplation only. No predictive claims are made."
        )

        return ReportSection(
            id = "tarot_attribution",
            title = ReportText("tarot.report.attribution", sectionTitle),
            blocks = listOf(
                ReportTable(
                    headers = listOf(
                        ReportText("tarot.report.column.source", sourceLabel),
                        ReportText("tarot.report.column.version", versionLabel),
                        ReportText("tarot.report.column.language", languageLabel),
                    ),
                    rows = tableRows.ifEmpty {
                        listOf(
                            listOf(
                                "AYNVORA Contemplative Traditions Archive",
                                "v1",
                                input.language.code.uppercase()
                            )
                        )
                    },
                    kind = ReportContentKind.SOURCE,
                ),
                ReportParagraph(
                    kind = ReportContentKind.DISCLAIMER,
                    text = ReportText("tarot.report.non_predictive_notice", nonPredictiveNotice),
                ),
            ),
        )
    }
}

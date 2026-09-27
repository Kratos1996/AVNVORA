package com.aynvora.core.report

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.gemstone.GemstoneCatalog
import com.aynvora.core.gemstone.GemstoneCompatibilityStatus
import com.aynvora.core.gemstone.GemstoneRecommendationPackage
import com.aynvora.core.gemstone.GemstoneWearingContext
import com.aynvora.core.intelligence.EvidenceGraph

/**
 * Input for Gemstone Report generation.
 */
data class GemstoneReportInput(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val recommendationPackage: GemstoneRecommendationPackage,
    val wearingContext: GemstoneWearingContext? = null,
    val evidenceGraph: EvidenceGraph? = null,
    val identity: ReportIdentity = ReportIdentity(),
) : ReportGeneratorInput

/**
 * Deterministic report generator for Gemstone / Navaratna recommendations.
 * Produces structured, non-predictive, reflective reports without hardcoded UI strings.
 */
class GemstoneReportGenerator : ReportGenerator {

    override val reportType: ReportType = ReportType.GEMSTONE

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver,
    ): ReportDocument {
        require(input is GemstoneReportInput) { "GemstoneReportGenerator requires GemstoneReportInput" }
        require(resolver.language == input.language) { "Resolver language must match input language" }

        val pkg = input.recommendationPackage
        val astro = pkg.astroProfile
        val sections = mutableListOf<ReportSection>()

        // 1. Traditional Purpose & Ethical Disclaimer Section
        val disclaimerTitle = resolver.rawText(
            "gemstone.report.disclaimer_title",
            "Traditional Jyotisha Disclosure & Mindful Use"
        )
        val disclaimerBody =
            resolver.rawText("gemstone.report.disclaimer_body", pkg.ethicalDisclaimer)

        sections += ReportSection(
            id = "disclaimer",
            title = ReportText("gemstone.report.disclaimer_title", disclaimerTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText("gemstone.report.disclaimer_body", disclaimerBody),
                    kind = ReportContentKind.DISCLAIMER,
                )
            )
        )

        // 2. Astrological Foundation (Lagna, Moon Sign, Benefics)
        val astroOverviewTitle = resolver.rawText(
            "gemstone.report.astro_overview_title",
            "Astrological Foundation & Bhava Lords"
        )
        sections += ReportSection(
            id = "astro_overview",
            title = ReportText("gemstone.report.astro_overview_title", astroOverviewTitle),
            blocks = listOf(
                ReportKeyValue(
                    label = ReportText(
                        "gemstone.report.lagna_label",
                        resolver.rawText("gemstone.report.lagna_label", "Ascendant (Lagna)")
                    ),
                    value = "${astro.lagnaRashi.name} (Lord: ${astro.lagnaLord.name})",
                ),
                ReportKeyValue(
                    label = ReportText(
                        "gemstone.report.moon_label",
                        resolver.rawText("gemstone.report.moon_label", "Moon Sign (Chandra Rashi)")
                    ),
                    value = "${astro.moonRashi.name} (Lord: ${astro.moonLord.name})",
                ),
                ReportKeyValue(
                    label = ReportText(
                        "gemstone.report.benefics_label",
                        resolver.rawText(
                            "gemstone.report.benefics_label",
                            "Functional Benefics (Trikonadhipatis)"
                        )
                    ),
                    value = astro.functionalBenefics.joinToString(", ") { it.name },
                ),
            )
        )

        // 3. Primary Recommendations (Jeevan, Bhagya, Punya Ratnas)
        val recSectionTitle = resolver.rawText(
            "gemstone.report.recommendations_title",
            "Traditional Gemstone Recommendations"
        )
        val recBlocks = mutableListOf<ReportBlock>()

        for (rec in pkg.primaryRecommendations) {
            val desc = GemstoneCatalog.findByType(rec.gemstoneType)
            recBlocks.add(
                ReportKeyValue(
                    label = ReportText(
                        "gemstone.report.rec_${rec.gemstoneType.name.lowercase()}_label",
                        rec.title
                    ),
                    value = "${desc.commonName} (${desc.sanskritName})",
                )
            )
            recBlocks.add(
                ReportParagraph(
                    text = ReportText(
                        "gemstone.report.rec_${rec.gemstoneType.name.lowercase()}_desc",
                        rec.rationale
                    ),
                    kind = ReportContentKind.INTERPRETATION,
                )
            )
            recBlocks.add(
                ReportKeyValue(
                    label = ReportText("gemstone.report.metal_finger_label", "Setting & Placement"),
                    value = "${rec.recommendedMetal.name} on ${rec.recommendedFinger.displayName} (${rec.recommendedFinger.sanskritName})",
                )
            )
            recBlocks.add(
                ReportKeyValue(
                    label = ReportText("gemstone.report.day_time_label", "Day & Timing"),
                    value = rec.recommendedDayTime,
                )
            )
        }

        sections += ReportSection(
            id = "recommendations",
            title = ReportText("gemstone.report.recommendations_title", recSectionTitle),
            blocks = recBlocks,
        )

        // 4. Cautionary Contraindications (Dusthana Lords)
        if (pkg.cautionedGemstones.isNotEmpty()) {
            val cautionTitle = resolver.rawText(
                "gemstone.report.cautions_title",
                "Cautionary Notes & Excluded Gemstones"
            )
            val cautionBlocks = mutableListOf<ReportBlock>()
            for (caution in pkg.cautionedGemstones) {
                val desc = GemstoneCatalog.findByType(caution.gemstoneType)
                cautionBlocks.add(
                    ReportKeyValue(
                        label = ReportText(
                            "gemstone.report.caution_${caution.gemstoneType.name.lowercase()}_label",
                            "${desc.commonName} (${desc.sanskritName})"
                        ),
                        value = caution.rationale,
                    )
                )
            }
            sections += ReportSection(
                id = "cautions",
                title = ReportText("gemstone.report.cautions_title", cautionTitle),
                blocks = cautionBlocks,
            )
        }

        // 5. Wearing Inventory & Compatibility Audit
        val inventory = input.wearingContext?.wornItems ?: emptyList()
        val invTitle =
            resolver.rawText("gemstone.report.inventory_title", "Currently Worn Gemstones Audit")
        val invBlocks = mutableListOf<ReportBlock>()

        if (inventory.isEmpty()) {
            invBlocks.add(
                ReportParagraph(
                    text = ReportText(
                        "gemstone.report.no_inventory",
                        "No active gemstones recorded in wearing inventory."
                    ),
                    kind = ReportContentKind.USER_CONTEXT,
                )
            )
        } else {
            for (item in inventory) {
                val statusStr = if (item.isCurrentlyWorn) "Currently Worn" else "Owned / Inactive"
                invBlocks.add(
                    ReportKeyValue(
                        label = ReportText(
                            "gemstone.report.inv_item_${item.id}",
                            "${item.type.sanskritName} (${item.type.name})"
                        ),
                        value = "$statusStr • ${item.approximateCaratWeight} carats • ${item.metal.name}",
                    )
                )
            }
            if (pkg.inventoryConflictCount > 0) {
                invBlocks.add(
                    ReportParagraph(
                        text = ReportText(
                            "gemstone.report.inv_conflict_warning",
                            "Warning: ${pkg.inventoryConflictCount} classical planetary conflict(s) detected with your current inventory."
                        ),
                        kind = ReportContentKind.DISCLAIMER,
                    )
                )
            }
        }

        sections += ReportSection(
            id = "inventory_audit",
            title = ReportText("gemstone.report.inventory_title", invTitle),
            blocks = invBlocks,
        )

        // 6. Source Provenance
        val sourcesTitle = resolver.rawText(
            "gemstone.report.sources_title",
            "Classical Textual Sources & Provenance"
        )
        sections += ReportSection(
            id = "sources",
            title = ReportText("gemstone.report.sources_title", sourcesTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText(
                        "gemstone.report.sources_body",
                        "Grounded in Brihat Parashara Hora Shastra (Adhyaya 3 & 34), Phaladeepika (Adhyaya 2), Garuda Purana (Saroddhara Ratnapariksha), and Brihat Samhita (Adhyaya 80-83)."
                    ),
                    kind = ReportContentKind.SOURCE,
                )
            )
        )

        val metadata = ReportMetadata(
            reportId = "gemstone_${astro.lagnaRashi.name}_${input.generatedAtEpochMs}",
            reportTypeId = "gemstone",
            generatedAtEpochMs = input.generatedAtEpochMs,
            language = input.language,
            version = ReportVersion(
                reportSchemaVersion = "1.0.0",
                calculationVersion = "1.0.0",
                contentVersion = "1.0.0",
            ),
            identity = input.identity,
            feature = CoreFeatureId.GEMSTONE,
            featureStatus = ReportFeatureStatus.IMPLEMENTED,
        )

        return ReportDocument(
            metadata = metadata,
            title = ReportText(
                "gemstone.report.title",
                resolver.rawText("gemstone.report.title", "Vedic Gemstone & Navaratna Analysis")
            ),
            sections = sections,
            sectionAvailability = emptyList(),
            disclaimer = ReportDisclaimer(
                title = ReportText("gemstone.report.disclaimer_title", disclaimerTitle),
                body = ReportText("gemstone.report.disclaimer_body", disclaimerBody),
            ),
            evidenceGraph = input.evidenceGraph,
        )
    }
}

package com.aynvora.core.report

import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.numerology.LoShuArrowStatus
import com.aynvora.core.numerology.NUMEROLOGY_DISCLAIMER
import com.aynvora.core.numerology.NumerologyInterpretationPackage
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.numerology.NumerologyRulesetRegistry

/**
 * Input for Numerology Report generation.
 */
data class NumerologyReportInput(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val result: NumerologyResult,
    val evidenceGraph: EvidenceGraph? = null,
    val identity: ReportIdentity = ReportIdentity(),
) : ReportGeneratorInput

/**
 * Deterministic report generator for consumer Numerology.
 *
 * Grounded in classical traditions:
 * - Pythagorean, Chaldean, Indian Ank Jyotish, Agrippan
 * - Lo Shu Magic Square
 * - Hebrew Gematria (Ragil & Mispar Gadol)
 * - Arabic Abjad (Mashriqi & Maghribi)
 * - Indian Katapayadi
 * - Chinese Nine Star Ki
 * - Tarot Birth Cards
 *
 * Produces structured, non-predictive, reflective reports without hardcoded UI strings.
 */
class NumerologyReportGenerator : ReportGenerator {

    override val reportType: ReportType = ReportType.NUMEROLOGY

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver,
    ): ReportDocument {
        require(input is NumerologyReportInput) { "NumerologyReportGenerator requires NumerologyReportInput" }
        require(resolver.language == input.language) { "Resolver language must match input language" }

        val result = input.result
        val profile = result.profile
        val ruleset = NumerologyRulesetRegistry.find(profile.rulesetId)
            ?: NumerologyRuleset.CHALDEAN_CHEIRO_V1

        val sections = mutableListOf<ReportSection>()

        // 1. Disclaimer Section
        val disclaimerTitle = resolver.rawText(
            "numerology.report.disclaimer_title",
            "Traditional Disclosure & Reflective Purpose"
        )
        val disclaimerBody =
            resolver.rawText("numerology.report.disclaimer_body", NUMEROLOGY_DISCLAIMER)

        sections += ReportSection(
            id = "disclaimer",
            title = ReportText("numerology.report.disclaimer_title", disclaimerTitle),
            blocks = listOf(
                ReportParagraph(
                    text = ReportText("numerology.report.disclaimer_body", disclaimerBody),
                    kind = ReportContentKind.DISCLAIMER,
                )
            )
        )

        // 2. Tradition & Ruleset Overview
        val methodOverviewTitle = resolver.rawText(
            "numerology.report.method_overview_title",
            "Tradition & Calculation Authority"
        )
        val traditionLabel = resolver.rawText("numerology.report.tradition_label", "Tradition")
        val rulesetLabel = resolver.rawText("numerology.report.ruleset_label", "Ruleset ID")
        val authorityLabel =
            resolver.rawText("numerology.report.authority_label", "Authority / Source")
        val versionLabel = resolver.rawText("numerology.report.version_label", "Ruleset Version")

        sections += ReportSection(
            id = "tradition_overview",
            title = ReportText("numerology.report.method_overview_title", methodOverviewTitle),
            blocks = listOf(
                ReportKeyValue(
                    label = ReportText("numerology.report.tradition_label", traditionLabel),
                    value = ruleset.name,
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("numerology.report.ruleset_label", rulesetLabel),
                    value = ruleset.id,
                    kind = ReportContentKind.FACT,
                ),
                ReportKeyValue(
                    label = ReportText("numerology.report.authority_label", authorityLabel),
                    value = ruleset.primarySourceReference,
                    kind = ReportContentKind.SOURCE,
                ),
                ReportKeyValue(
                    label = ReportText("numerology.report.version_label", versionLabel),
                    value = ruleset.version,
                    kind = ReportContentKind.FACT,
                ),
            )
        )

        // 3. Calculated Values Section
        val valuesTitle =
            resolver.rawText("numerology.report.values_title", "Calculated Core Values")
        val valueBlocks = mutableListOf<ReportBlock>()

        profile.radical?.let { rad ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.radical_label",
                    resolver.rawText(
                        "numerology.report.radical_label",
                        "Radical / Moolank / Birth Day Number"
                    )
                ),
                value = "${rad.radicalValue} (${rad.rulingPlanet})",
                kind = ReportContentKind.CALCULATION,
            )
        }

        profile.destiny?.let { des ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.destiny_label",
                    resolver.rawText(
                        "numerology.report.destiny_label",
                        "Destiny / Bhagyank / Life Path Number"
                    )
                ),
                value = "${des.destinyValue} (${des.rulingPlanet})",
                kind = ReportContentKind.CALCULATION,
            )
        }

        profile.nameNumber?.let { nam ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.name_label",
                    resolver.rawText("numerology.report.name_label", "Name / Expression Number")
                ),
                value = "${nam.nameValue} (${nam.system.name})",
                kind = ReportContentKind.CALCULATION,
            )
        }

        profile.soulUrge?.let { su ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.soul_urge_label",
                    resolver.rawText(
                        "numerology.report.soul_urge_label",
                        "Soul Urge / Heart's Desire Number"
                    )
                ),
                value = "${su.soulUrgeValue}",
                kind = ReportContentKind.CALCULATION,
            )
        }

        profile.personality?.let { per ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.personality_label",
                    resolver.rawText("numerology.report.personality_label", "Personality Number")
                ),
                value = "${per.personalityValue}",
                kind = ReportContentKind.CALCULATION,
            )
        }

        // Specialized ruleset results
        result.loShuResult?.let { loShu ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.loshu_present",
                    resolver.rawText("numerology.report.loshu_present", "Lo Shu Present Digits")
                ),
                value = loShu.grid.presentDigits.joinToString(", "),
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.loshu_missing",
                    resolver.rawText("numerology.report.loshu_missing", "Lo Shu Missing Digits")
                ),
                value = loShu.grid.missingDigits.joinToString(", ").ifEmpty { "None" },
                kind = ReportContentKind.CALCULATION,
            )
            val strengthArrows = loShu.grid.arrows
                .filter { it.status == LoShuArrowStatus.ARROW_OF_STRENGTH }
                .map { it.planeName }
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.loshu_arrows",
                    resolver.rawText("numerology.report.loshu_arrows", "Completed Arrows")
                ),
                value = strengthArrows.joinToString(", ").ifEmpty { "None" },
                kind = ReportContentKind.CALCULATION,
            )
        }

        result.gematriaResult?.let { gem ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.gematria_standard",
                    resolver.rawText(
                        "numerology.report.gematria_standard",
                        "Gematria Absolute Value"
                    )
                ),
                value = gem.absoluteValue.toString(),
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.gematria_reduced",
                    resolver.rawText("numerology.report.gematria_reduced", "Gematria Reduced Value")
                ),
                value = gem.reducedValue.toString(),
                kind = ReportContentKind.CALCULATION,
            )
        }

        result.abjadResult?.let { abj ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.abjad_kabir",
                    resolver.rawText(
                        "numerology.report.abjad_kabir",
                        "Abjad al-Kabir (Major Value)"
                    )
                ),
                value = abj.kabirValue.toString(),
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.abjad_saghir",
                    resolver.rawText(
                        "numerology.report.abjad_saghir",
                        "Abjad al-Saghir (Reduced Value)"
                    )
                ),
                value = abj.saghirValue.toString(),
                kind = ReportContentKind.CALCULATION,
            )
        }

        result.katapayadiResult?.let { kata ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.katapayadi_digits",
                    resolver.rawText(
                        "numerology.report.katapayadi_digits",
                        "Extracted Digit Sequence"
                    )
                ),
                value = kata.digitSequence,
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.katapayadi_final",
                    resolver.rawText(
                        "numerology.report.katapayadi_final",
                        "Reversed Number (Ankanam Vamato Gatih)"
                    )
                ),
                value = kata.finalNumber,
                kind = ReportContentKind.CALCULATION,
            )
        }

        result.nineStarKiResult?.let { nsk ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.nsk_solar_year",
                    resolver.rawText("numerology.report.nsk_solar_year", "Calculated Solar Year")
                ),
                value = nsk.solarYear.toString(),
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.nsk_star",
                    resolver.rawText("numerology.report.nsk_star", "Principal Star")
                ),
                value = "${nsk.principalStar.starNumber} - ${nsk.principalStar.starName} (${nsk.principalStar.trigram}, ${nsk.principalStar.element})",
                kind = ReportContentKind.CALCULATION,
            )
        }

        result.tarotBirthCardResult?.let { tarot ->
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.tarot_personality",
                    resolver.rawText(
                        "numerology.report.tarot_personality",
                        "Tarot Personality Card"
                    )
                ),
                value = "${tarot.personalityCardNumber} - ${tarot.personalityCardName}",
                kind = ReportContentKind.CALCULATION,
            )
            valueBlocks += ReportKeyValue(
                label = ReportText(
                    "numerology.report.tarot_soul",
                    resolver.rawText("numerology.report.tarot_soul", "Tarot Soul Card")
                ),
                value = "${tarot.soulCardNumber} - ${tarot.soulCardName}",
                kind = ReportContentKind.CALCULATION,
            )
            if (tarot.shadowCardNumber != null && tarot.shadowCardName != null) {
                valueBlocks += ReportKeyValue(
                    label = ReportText(
                        "numerology.report.tarot_shadow",
                        resolver.rawText("numerology.report.tarot_shadow", "Tarot Shadow Card")
                    ),
                    value = "${tarot.shadowCardNumber} - ${tarot.shadowCardName}",
                    kind = ReportContentKind.CALCULATION,
                )
            }
        }

        sections += ReportSection(
            id = "calculated_values",
            title = ReportText("numerology.report.values_title", valuesTitle),
            blocks = valueBlocks,
        )

        // 4. Calculation Traces Section ("How this was calculated")
        val tracesTitle =
            resolver.rawText("numerology.report.trace_title", "Calculation Transparency & Traces")
        val traceBlocks = mutableListOf<ReportBlock>()

        profile.calculationTraces.forEach { (type, trace) ->
            traceBlocks += ReportKeyValue(
                label = ReportText("numerology.trace.step.${type.name}", type.name),
                value = "${trace.rawInput} -> ${trace.compoundSum} -> ${trace.finalValue}",
                kind = ReportContentKind.CALCULATION,
            )
        }

        if (traceBlocks.isNotEmpty()) {
            sections += ReportSection(
                id = "calculation_traces",
                title = ReportText("numerology.report.trace_title", tracesTitle),
                blocks = traceBlocks,
            )
        }

        // 5. Traditional Reflective Interpretations Section
        val interpTitle = resolver.rawText(
            "numerology.report.interpretations_title",
            "Traditional Reflective Insights"
        )
        val interpBlocks = mutableListOf<ReportBlock>()

        val interpretationBundle = NumerologyInterpretationPackage.resolveInterpretations(result)

        // Non-personality notice for mnemonic or alphanumeric traditions
        if (!interpretationBundle.isPersonalityInterpretation && interpretationBundle.nonPersonalityNoticeKey != null) {
            val noticeText = resolver.rawText(
                interpretationBundle.nonPersonalityNoticeKey!!,
                "This tradition is an alphanumeric/mnemonic system, not a personality horoscope."
            )
            interpBlocks += ReportParagraph(
                kind = ReportContentKind.INTERPRETATION,
                text = ReportText(interpretationBundle.nonPersonalityNoticeKey!!, noticeText),
            )
        }

        for (interp in interpretationBundle.allInterpretations) {
            val titleText = resolver.rawText(interp.titleKey, interp.subjectId)
            val summaryText = resolver.rawText(interp.summaryKey, "Contemplative archetype")
            val reflectionText =
                resolver.rawText(interp.reflectionKey, "Traditional reflective theme")

            interpBlocks += ReportParagraph(
                kind = ReportContentKind.INTERPRETATION,
                text = ReportText(interp.titleKey, "$titleText — $summaryText"),
            )
            interpBlocks += ReportParagraph(
                kind = ReportContentKind.INTERPRETATION,
                text = ReportText(interp.reflectionKey, reflectionText),
            )
            if (interp.sourceReferences.isNotEmpty()) {
                interpBlocks += ReportKeyValue(
                    label = ReportText(
                        "numerology.interp.source_citation_label",
                        resolver.rawText(
                            "numerology.interp.source_citation_label",
                            "Traditional Source"
                        )
                    ),
                    value = interp.sourceReferences.joinToString("; "),
                    kind = ReportContentKind.SOURCE,
                )
            }
        }

        if (interpBlocks.isNotEmpty()) {
            sections += ReportSection(
                id = "interpretations",
                title = ReportText("numerology.report.interpretations_title", interpTitle),
                blocks = interpBlocks,
            )
        }

        val reportDocTitle =
            resolver.rawText("numerology.report.doc_title", "${ruleset.name} Numerology Report")

        return ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "numerology_${ruleset.id}_${input.generatedAtEpochMs}",
                reportTypeId = ReportType.NUMEROLOGY.id,
                generatedAtEpochMs = input.generatedAtEpochMs,
                language = input.language,
                version = ReportVersion(
                    reportSchemaVersion = "1.0.0",
                    calculationVersion = ruleset.version,
                    contentVersion = NumerologyInterpretationPackage.CONTENT_VERSION,
                ),
                identity = input.identity,
                feature = reportType.feature,
                featureStatus = ReportFeatureStatus.IMPLEMENTED,
            ),
            title = ReportText("numerology.report.doc_title", reportDocTitle),
            sections = sections,
            availability = emptyList(),
            disclaimer = ReportDisclaimer(
                title = ReportText("numerology.report.disclaimer_title", disclaimerTitle),
                body = ReportText("numerology.report.disclaimer_body", disclaimerBody),
            ),
            graph = input.evidenceGraph,
        )
    }
}

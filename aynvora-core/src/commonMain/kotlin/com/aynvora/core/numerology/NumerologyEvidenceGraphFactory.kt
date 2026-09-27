package com.aynvora.core.numerology

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.ConflictStatus
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceGraphEdge
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance

/**
 * Constructs an immutable, fully traceable [EvidenceGraph] from a deterministic [NumerologyResult].
 *
 * Guarantees that every numerological statement can answer "Why did this appear?" deterministically.
 * The EvidenceGraph serves as the authoritative boundary for future SLM/AI explanation layers:
 * AI consumes these structured nodes and must never recalculate or override numbers.
 */
object NumerologyEvidenceGraphFactory {

    fun create(
        result: NumerologyResult,
        request: NumerologyRequest,
        timestampEpochMs: Long = 0L,
    ): EvidenceGraph {
        val profile = result.profile
        val ruleset =
            NumerologyRuleset.fromId(profile.rulesetId) ?: NumerologyRuleset.CHALDEAN_CHEIRO_V1
        val queryId = "numerology_${profile.birthDateDisplay}_${ruleset.id}"

        val nodes = mutableMapOf<String, EvidenceItem>()
        val edges = mutableListOf<EvidenceGraphEdge>()

        val baseProvenance = EvidenceProvenance(
            domain = CoreFeatureId.NUMEROLOGY,
            sourceName = ruleset.name,
            rulesetOrEdition = ruleset.id,
            engineVersion = ruleset.version,
            calculationProfile = ruleset.primarySourceReference,
            timestampEpochMs = timestampEpochMs,
        )

        // 1. FACT: Normalized Birth Date
        val birthDateNodeId = "num_fact_birth_date"
        nodes[birthDateNodeId] = EvidenceItem(
            evidenceId = birthDateNodeId,
            domain = CoreFeatureId.NUMEROLOGY,
            category = EvidenceCategory.FACT,
            ruleId = null,
            summary = "Birth Date: ${profile.birthDateDisplay}",
            rawPayloadJson = "{\"day\":${request.birthDay},\"month\":${request.birthMonth},\"year\":${request.birthYear}}",
            provenance = baseProvenance,
            priority = 100,
            conflictStatus = ConflictStatus.SUPPORTING,
        )

        // 2. TRADITIONAL_RULE: Ruleset Authority
        val rulesetNodeId = "num_rule_ruleset"
        nodes[rulesetNodeId] = EvidenceItem(
            evidenceId = rulesetNodeId,
            domain = CoreFeatureId.NUMEROLOGY,
            category = EvidenceCategory.TRADITIONAL_RULE,
            ruleId = ruleset.id,
            summary = "${ruleset.name} (${ruleset.authorityDescription})",
            rawPayloadJson = "{\"source\":\"${ruleset.primarySourceReference}\",\"reduction\":\"${ruleset.dateReductionMethod.name}\",\"masterPolicy\":\"${ruleset.masterNumberPolicy.name}\"}",
            provenance = baseProvenance,
            priority = 90,
            conflictStatus = ConflictStatus.SUPPORTING,
        )

        // 3. DERIVED_FACT: Radical Number (Moolank)
        if (profile.radical != null) {
            val radicalNodeId = "num_derived_radical"
            nodes[radicalNodeId] = EvidenceItem(
                evidenceId = radicalNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = profile.radical.sourceRule,
                summary = "Radical Number: ${profile.radical.radicalValue} (${profile.radical.rulingPlanet})",
                rawPayloadJson = "{\"value\":${profile.radical.radicalValue},\"isMaster\":${profile.radical.isMasterNumber},\"planet\":\"${profile.radical.rulingPlanet}\"}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, radicalNodeId, "DERIVES"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, radicalNodeId, "APPLIES_RULE"))
        }

        // 4. DERIVED_FACT: Destiny Number (Bhagyank)
        if (profile.destiny != null) {
            val destinyNodeId = "num_derived_destiny"
            nodes[destinyNodeId] = EvidenceItem(
                evidenceId = destinyNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = profile.destiny.sourceRule,
                summary = "Destiny Number: ${profile.destiny.destinyValue} (${profile.destiny.rulingPlanet})",
                rawPayloadJson = "{\"value\":${profile.destiny.destinyValue},\"isMaster\":${profile.destiny.isMasterNumber},\"rawSum\":${profile.destiny.rawSum}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, destinyNodeId, "DERIVES"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, destinyNodeId, "APPLIES_RULE"))
        }

        // 5. FACT & DERIVED_FACT: Name Number, Soul Urge, Personality
        if (profile.nameNumber != null) {
            val nameFactNodeId = "num_fact_name"
            nodes[nameFactNodeId] = EvidenceItem(
                evidenceId = nameFactNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = null,
                summary = "Normalized Name: ${profile.nameNumber.fullName}",
                rawPayloadJson = "{\"fullName\":\"${profile.nameNumber.fullName}\"}",
                provenance = baseProvenance,
                priority = 70,
                conflictStatus = ConflictStatus.SUPPORTING,
            )

            val nameDerivedNodeId = "num_derived_name"
            nodes[nameDerivedNodeId] = EvidenceItem(
                evidenceId = nameDerivedNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = profile.nameNumber.sourceRule,
                summary = "Name Number: ${profile.nameNumber.nameValue} (${profile.nameNumber.system.name})",
                rawPayloadJson = "{\"value\":${profile.nameNumber.nameValue},\"system\":\"${profile.nameNumber.system.name}\"}",
                provenance = baseProvenance,
                priority = 65,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(nameFactNodeId, nameDerivedNodeId, "DERIVES"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, nameDerivedNodeId, "APPLIES_RULE"))

            if (profile.soulUrge != null) {
                val suNodeId = "num_derived_soul_urge"
                nodes[suNodeId] = EvidenceItem(
                    evidenceId = suNodeId,
                    domain = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.DERIVED_FACT,
                    ruleId = profile.soulUrge.sourceRule,
                    summary = "Soul Urge Number: ${profile.soulUrge.soulUrgeValue}",
                    rawPayloadJson = "{\"value\":${profile.soulUrge.soulUrgeValue}}",
                    provenance = baseProvenance,
                    priority = 60,
                    conflictStatus = ConflictStatus.SUPPORTING,
                )
                edges.add(EvidenceGraphEdge(nameFactNodeId, suNodeId, "DERIVES"))
            }

            if (profile.personality != null) {
                val persNodeId = "num_derived_personality"
                nodes[persNodeId] = EvidenceItem(
                    evidenceId = persNodeId,
                    domain = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.DERIVED_FACT,
                    ruleId = profile.personality.sourceRule,
                    summary = "Personality Number: ${profile.personality.personalityValue}",
                    rawPayloadJson = "{\"value\":${profile.personality.personalityValue}}",
                    provenance = baseProvenance,
                    priority = 60,
                    conflictStatus = ConflictStatus.SUPPORTING,
                )
                edges.add(EvidenceGraphEdge(nameFactNodeId, persNodeId, "DERIVES"))
            }
        }

        // 6. Pinnacles
        profile.pinnacles.forEach { pinnacle ->
            val pinNodeId = "num_derived_pinnacle_${pinnacle.pinnacleOrder}"
            nodes[pinNodeId] = EvidenceItem(
                evidenceId = pinNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = pinnacle.sourceRule,
                summary = "Pinnacle ${pinnacle.pinnacleOrder}: ${pinnacle.pinnacleValue} (Ages ${pinnacle.startAge}–${pinnacle.endAge ?: "end"})",
                rawPayloadJson = "{\"order\":${pinnacle.pinnacleOrder},\"value\":${pinnacle.pinnacleValue},\"startAge\":${pinnacle.startAge},\"endAge\":${pinnacle.endAge}}",
                provenance = baseProvenance,
                priority = 50,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, pinNodeId, "DERIVES"))
        }

        // 7. Personal Year
        profile.personalYears.forEach { py ->
            val pyNodeId = "num_derived_personal_year_${py.targetYear}"
            nodes[pyNodeId] = EvidenceItem(
                evidenceId = pyNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = py.sourceRule,
                summary = "Personal Year ${py.targetYear}: ${py.personalYearValue} (${py.rulingPlanet})",
                rawPayloadJson = "{\"year\":${py.targetYear},\"value\":${py.personalYearValue}}",
                provenance = baseProvenance,
                priority = 55,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, pyNodeId, "DERIVES"))
        }

        // 8. Combinations
        profile.combinations.forEach { comb ->
            val combNodeId = "num_derived_combination_${comb.radicalValue}_${comb.destinyValue}"
            nodes[combNodeId] = EvidenceItem(
                evidenceId = combNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = comb.sourceRule,
                summary = "Radical ${comb.radicalValue} & Destiny ${comb.destinyValue} Relationship: ${comb.relationshipType.name}",
                rawPayloadJson = "{\"radical\":${comb.radicalValue},\"destiny\":${comb.destinyValue},\"relationship\":\"${comb.relationshipType.name}\"}",
                provenance = baseProvenance,
                priority = 75,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            if (nodes.containsKey("num_derived_radical")) {
                edges.add(EvidenceGraphEdge("num_derived_radical", combNodeId, "CONTRIBUTES"))
            }
            if (nodes.containsKey("num_derived_destiny")) {
                edges.add(EvidenceGraphEdge("num_derived_destiny", combNodeId, "CONTRIBUTES"))
            }
        }

        // 9. Planetary Association
        if (profile.planetaryAssociation != null) {
            val planetNodeId = "num_derived_planetary_association"
            val pa = profile.planetaryAssociation
            nodes[planetNodeId] = EvidenceItem(
                evidenceId = planetNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = pa.sourceRule,
                summary = "Planetary Association: ${pa.sanskritName} / ${pa.englishName} (${pa.tradition})",
                rawPayloadJson = "{\"number\":${pa.number},\"planetId\":\"${pa.planetId.name}\",\"sanskritName\":\"${pa.sanskritName}\",\"englishName\":\"${pa.englishName}\"}",
                provenance = baseProvenance,
                priority = 78,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            if (nodes.containsKey("num_derived_radical")) {
                edges.add(
                    EvidenceGraphEdge(
                        "num_derived_radical",
                        planetNodeId,
                        "ASSOCIATES_PLANET"
                    )
                )
            }
        }

        // 10. Lo Shu Magic Square (Classical Luoshu Tradition)
        if (profile.loShu != null) {
            val grid = profile.loShu
            val loShuDigitsNodeId = "num_fact_loshu_digits"
            nodes[loShuDigitsNodeId] = EvidenceItem(
                evidenceId = loShuDigitsNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = grid.sourceRule,
                summary = "Lo Shu Digits: ${grid.extractedDigits} (Filtered Zeros: ${grid.zeroDigitsCount})",
                rawPayloadJson = "{\"extractedDigits\":${grid.extractedDigits},\"zeroDigitsCount\":${grid.zeroDigitsCount}}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, loShuDigitsNodeId, "EXTRACTS_DIGITS"))

            val loShuFreqNodeId = "num_derived_loshu_frequencies"
            nodes[loShuFreqNodeId] = EvidenceItem(
                evidenceId = loShuFreqNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = grid.sourceRule,
                summary = "Lo Shu Frequencies: Present=${grid.presentDigits}, Missing=${grid.missingDigits}",
                rawPayloadJson = "{\"frequencies\":${grid.frequencies.map { "{\"digit\":${it.digit},\"count\":${it.count}}" }}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(loShuDigitsNodeId, loShuFreqNodeId, "COUNTS_FREQUENCY"))

            val loShuArrowsNodeId = "num_derived_loshu_arrows"
            val strengthArrows =
                grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_STRENGTH }
            val weaknessArrows =
                grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_WEAKNESS }
            nodes[loShuArrowsNodeId] = EvidenceItem(
                evidenceId = loShuArrowsNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = grid.sourceRule,
                summary = "Lo Shu Arrows: ${strengthArrows.size} Strength, ${weaknessArrows.size} Weakness",
                rawPayloadJson = "{\"strength\":${strengthArrows.map { "\"${it.strengthName}\"" }},\"weakness\":${weaknessArrows.map { "\"${it.weaknessName}\"" }}}",
                provenance = baseProvenance,
                priority = 75,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(loShuFreqNodeId, loShuArrowsNodeId, "EVALUATES_ARROWS"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, loShuArrowsNodeId, "APPLIES_RULE"))
        }

        // 11. Hebrew Gematria (Classical Tradition)
        if (profile.gematria != null) {
            val gem = profile.gematria
            val hebrewFactNodeId = "num_fact_hebrew_text"
            nodes[hebrewFactNodeId] = EvidenceItem(
                evidenceId = hebrewFactNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = gem.sourceRule,
                summary = "Hebrew Text: ${gem.normalizedHebrew} (Raw: ${gem.rawText})",
                rawPayloadJson = "{\"rawText\":\"${gem.rawText}\",\"normalizedHebrew\":\"${gem.normalizedHebrew}\"}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )

            val gematriaAbsoluteNodeId = "num_derived_gematria_absolute"
            nodes[gematriaAbsoluteNodeId] = EvidenceItem(
                evidenceId = gematriaAbsoluteNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = gem.sourceRule,
                summary = "Mispar Hechrachi (Absolute Value): ${gem.absoluteValue}",
                rawPayloadJson = "{\"absoluteValue\":${gem.absoluteValue},\"letterValues\":${gem.letterValues.map { "{\"letter\":\"${it.first}\",\"value\":${it.second}}" }}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(
                EvidenceGraphEdge(
                    hebrewFactNodeId,
                    gematriaAbsoluteNodeId,
                    "COMPUTES_ABSOLUTE"
                )
            )
            edges.add(EvidenceGraphEdge(rulesetNodeId, gematriaAbsoluteNodeId, "APPLIES_RULE"))

            val gematriaReducedNodeId = "num_derived_gematria_reduced"
            nodes[gematriaReducedNodeId] = EvidenceItem(
                evidenceId = gematriaReducedNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = gem.sourceRule,
                summary = "Mispar Katan (Small Root Reduction): ${gem.reducedValue}",
                rawPayloadJson = "{\"reducedValue\":${gem.reducedValue}}",
                provenance = baseProvenance,
                priority = 75,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(
                EvidenceGraphEdge(
                    gematriaAbsoluteNodeId,
                    gematriaReducedNodeId,
                    "REDUCES_ROOT"
                )
            )
        }

        // 12. Arabic Abjad (Hisab al-Jummal Mashriqi)
        if (profile.abjad != null) {
            val abj = profile.abjad
            val arabicFactNodeId = "num_fact_arabic_text"
            nodes[arabicFactNodeId] = EvidenceItem(
                evidenceId = arabicFactNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = abj.sourceRule,
                summary = "Arabic Text: ${abj.normalizedArabic} (Raw: ${abj.rawText})",
                rawPayloadJson = "{\"rawText\":\"${abj.rawText}\",\"normalizedArabic\":\"${abj.normalizedArabic}\"}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )

            val abjadKabirNodeId = "num_derived_abjad_kabir"
            nodes[abjadKabirNodeId] = EvidenceItem(
                evidenceId = abjadKabirNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = abj.sourceRule,
                summary = "Jummal Kabir (Great Sum): ${abj.jummalKabir}",
                rawPayloadJson = "{\"jummalKabir\":${abj.jummalKabir},\"letterValues\":${abj.letterValues.map { "{\"letter\":\"${it.first}\",\"value\":${it.second}}" }}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(arabicFactNodeId, abjadKabirNodeId, "COMPUTES_KABIR"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, abjadKabirNodeId, "APPLIES_RULE"))

            val abjadSaghirNodeId = "num_derived_abjad_saghir"
            nodes[abjadSaghirNodeId] = EvidenceItem(
                evidenceId = abjadSaghirNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = abj.sourceRule,
                summary = "Jummal Saghir (Small Root Reduction): ${abj.jummalSaghir}",
                rawPayloadJson = "{\"jummalSaghir\":${abj.jummalSaghir}}",
                provenance = baseProvenance,
                priority = 75,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(abjadKabirNodeId, abjadSaghirNodeId, "REDUCES_ROOT"))
        }

        // 13. Indian Katapayadi Numerical Mnemonic System
        if (profile.katapayadi != null) {
            val kata = profile.katapayadi
            val kataFactNodeId = "num_fact_katapayadi_text"
            nodes[kataFactNodeId] = EvidenceItem(
                evidenceId = kataFactNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = kata.sourceRule,
                summary = "Katapayadi Input: ${kata.rawText} (Normalized: ${kata.normalizedText})",
                rawPayloadJson = "{\"rawText\":\"${kata.rawText}\",\"normalizedText\":\"${kata.normalizedText}\"}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )

            val kataDigitsNodeId = "num_derived_katapayadi_digits"
            nodes[kataDigitsNodeId] = EvidenceItem(
                evidenceId = kataDigitsNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = kata.sourceRule,
                summary = "Phoneme Consonant Digits (Left-to-Right): ${kata.digitSequence}",
                rawPayloadJson = "{\"digitSequence\":\"${kata.digitSequence}\",\"phonemes\":${kata.phonemes.map { "{\"akshara\":\"${it.akshara}\",\"consonant\":\"${it.mappedConsonant}\",\"digit\":${it.digit}}" }}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(kataFactNodeId, kataDigitsNodeId, "PARSES_PHONEMES"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, kataDigitsNodeId, "APPLIES_RULE"))

            val kataNumberNodeId = "num_derived_katapayadi_number"
            nodes[kataNumberNodeId] = EvidenceItem(
                evidenceId = kataNumberNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = kata.sourceRule,
                summary = "Ankānām Vāmato Gatiḥ (Reversed Final Number): ${kata.finalNumber}",
                rawPayloadJson = "{\"finalNumber\":\"${kata.finalNumber}\",\"isReversed\":${kata.isReversed}}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(kataDigitsNodeId, kataNumberNodeId, "REVERSES_DIGITS"))
        }

        // 14. Chinese Nine Star Ki (Flying Star Feng Shui)
        if (profile.nineStarKi != null) {
            val nsk = profile.nineStarKi
            val nskSolarYearNodeId = "num_fact_solar_year"
            nodes[nskSolarYearNodeId] = EvidenceItem(
                evidenceId = nskSolarYearNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = nsk.sourceRule,
                summary = "Solar Year: ${nsk.solarYear} (Li Chun: ${nsk.liChunIsoTimestamp}, IsBeforeLiChun: ${nsk.isBeforeLiChun})",
                rawPayloadJson = "{\"solarYear\":${nsk.solarYear},\"liChun\":\"${nsk.liChunIsoTimestamp}\",\"isBeforeLiChun\":${nsk.isBeforeLiChun}}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(
                EvidenceGraphEdge(
                    birthDateNodeId,
                    nskSolarYearNodeId,
                    "CALCULATES_SOLAR_YEAR"
                )
            )

            val nskPrincipalNodeId = "num_derived_nine_star_principal"
            nodes[nskPrincipalNodeId] = EvidenceItem(
                evidenceId = nskPrincipalNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = nsk.sourceRule,
                summary = "Principal Star: ${nsk.principalStar.number} (${nsk.principalStar.englishName} / ${nsk.principalStar.chineseName}, Element: ${nsk.principalStar.element}, Trigram: ${nsk.principalStar.trigram})",
                rawPayloadJson = "{\"number\":${nsk.principalStar.number},\"name\":\"${nsk.principalStar.englishName}\",\"element\":\"${nsk.principalStar.element}\",\"trigram\":\"${nsk.principalStar.trigram}\"}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(
                EvidenceGraphEdge(
                    nskSolarYearNodeId,
                    nskPrincipalNodeId,
                    "COMPUTES_PRINCIPAL_STAR"
                )
            )
            edges.add(EvidenceGraphEdge(rulesetNodeId, nskPrincipalNodeId, "APPLIES_RULE"))
        }

        // 15. Tarot Birth Cards (Greer 1984 Reduction)
        if (profile.tarotBirthCard != null) {
            val tbc = profile.tarotBirthCard
            val tarotSumNodeId = "num_fact_tarot_raw_sum"
            nodes[tarotSumNodeId] = EvidenceItem(
                evidenceId = tarotSumNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                ruleId = tbc.sourceRule,
                summary = "Tarot Calendar Sum: ${tbc.rawSum} (Day=${request.birthDay}, Month=${request.birthMonth}, Year=${request.birthYear})",
                rawPayloadJson = "{\"rawSum\":${tbc.rawSum},\"firstReduction\":${tbc.firstReduction},\"finalReduction\":${tbc.finalReduction}}",
                provenance = baseProvenance,
                priority = 85,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(birthDateNodeId, tarotSumNodeId, "SUMS_DATE"))

            val personalityCardNodeId = "num_derived_tarot_personality_card"
            nodes[personalityCardNodeId] = EvidenceItem(
                evidenceId = personalityCardNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = tbc.sourceRule,
                summary = "Personality Card: #${tbc.personalityCardNumber} (${tbc.personalityCardName})",
                rawPayloadJson = "{\"number\":${tbc.personalityCardNumber},\"name\":\"${tbc.personalityCardName}\"}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(tarotSumNodeId, personalityCardNodeId, "REDUCES_CARD"))
            edges.add(EvidenceGraphEdge(rulesetNodeId, personalityCardNodeId, "APPLIES_RULE"))

            val soulCardNodeId = "num_derived_tarot_soul_card"
            nodes[soulCardNodeId] = EvidenceItem(
                evidenceId = soulCardNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = tbc.sourceRule,
                summary = "Soul Card: #${tbc.soulCardNumber} (${tbc.soulCardName})",
                rawPayloadJson = "{\"number\":${tbc.soulCardNumber},\"name\":\"${tbc.soulCardName}\"}",
                provenance = baseProvenance,
                priority = 80,
                conflictStatus = ConflictStatus.SUPPORTING,
            )
            edges.add(EvidenceGraphEdge(personalityCardNodeId, soulCardNodeId, "REDUCES_SOUL"))
        }

        // ====================================================================
        // PHASE 10.5: TRADITIONAL INTERPRETATION NODES & PROVENANCE EDGES
        // ====================================================================
        val interpretationBundle = NumerologyInterpretationPackage.resolveInterpretations(result)
        for (interp in interpretationBundle.allInterpretations) {
            val interpNodeId = "num_interp_${interp.contentId}"
            nodes[interpNodeId] = EvidenceItem(
                evidenceId = interpNodeId,
                domain = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.INTERPRETATION,
                ruleId = interp.rulesetId,
                summary = "${interp.subjectId}: Traditional Interpretation",
                rawPayloadJson = "{\"titleKey\":\"${interp.titleKey}\",\"summaryKey\":\"${interp.summaryKey}\",\"reflectionKey\":\"${interp.reflectionKey}\",\"source\":\"${interp.sourceReferences.firstOrNull() ?: ""}\",\"isPersonality\":${interp.isPersonalityInterpretation}}",
                provenance = baseProvenance.copy(
                    referenceId = interp.sourceReferences.firstOrNull(),
                    contentVersion = interp.contentVersion,
                    contentType = "TRADITIONAL_INTERPRETATION",
                ),
                priority = 70,
                conflictStatus = ConflictStatus.SUPPORTING,
            )

            // Link edge from ruleset
            edges.add(EvidenceGraphEdge(rulesetNodeId, interpNodeId, "AUTHORIZES_INTERPRETATION"))

            // Link edge from derived fact if matching calculation type
            val parentFactId = when (interp.calculationType) {
                NumerologyCalculationType.RADICAL_NUMBER -> "num_derived_radical"
                NumerologyCalculationType.DESTINY_NUMBER -> "num_derived_destiny"
                NumerologyCalculationType.NAME_NUMBER -> "num_derived_name_number"
                NumerologyCalculationType.PERSONAL_YEAR -> "num_derived_personal_year"
                NumerologyCalculationType.SOUL_URGE_NUMBER -> "num_derived_soul_urge"
                NumerologyCalculationType.PERSONALITY_NUMBER -> "num_derived_personality"
                NumerologyCalculationType.LO_SHU_GRID, NumerologyCalculationType.LO_SHU_ARROWS -> "num_derived_loshu_grid"
                NumerologyCalculationType.GEMATRIA_ABSOLUTE_VALUE, NumerologyCalculationType.GEMATRIA_REDUCED_VALUE -> "num_derived_hebrew_gematria"
                NumerologyCalculationType.ABJAD_KABIR_VALUE, NumerologyCalculationType.ABJAD_SAGHIR_VALUE -> "num_derived_arabic_abjad"
                NumerologyCalculationType.KATAPAYADI_CODING -> "num_derived_katapayadi"
                NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL -> "num_derived_nine_star_ki"
                NumerologyCalculationType.TAROT_BIRTH_CARD -> "num_derived_tarot_personality_card"
                else -> null
            }

            if (parentFactId != null && nodes.containsKey(parentFactId)) {
                edges.add(EvidenceGraphEdge(parentFactId, interpNodeId, "INTERPRETS"))
            }
        }

        return EvidenceGraph(
            queryId = queryId,
            nodes = nodes,
            edges = edges,
        )
    }
}

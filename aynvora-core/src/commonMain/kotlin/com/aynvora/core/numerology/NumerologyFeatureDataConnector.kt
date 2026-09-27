package com.aynvora.core.numerology

import com.aynvora.core.ai.AiEvidence
import com.aynvora.core.ai.AiFeatureDataConnector
import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceProvenance

/**
 * Domain connector extracting verified, immutable [AiEvidence] from [NumerologyResult].
 *
 * Implements the core privacy and architectural invariant:
 * - AI never touches DAOs or Room databases.
 * - Extracts structured mathematical facts and verified source-backed interpretations.
 * - Enforces [AiPrivacyClass.STRICT_LOCAL_ONLY] on all evidence items.
 */
class NumerologyFeatureDataConnector : AiFeatureDataConnector<NumerologyResult> {

    override val featureId: CoreFeatureId = CoreFeatureId.NUMEROLOGY

    override fun extractEvidence(input: NumerologyResult): List<AiEvidence> {
        val evidenceList = mutableListOf<AiEvidence>()
        val profile = input.profile
        val ruleset = NumerologyRuleset.fromId(profile.rulesetId)
        val rulesetId = ruleset?.id ?: profile.rulesetId
        val rulesetName = ruleset?.name ?: profile.rulesetId

        val baseProvenance = EvidenceProvenance(
            domain = CoreFeatureId.NUMEROLOGY,
            sourceName = ruleset?.primarySourceReference ?: "AYNVORA Numerology Engine",
            rulesetOrEdition = rulesetId,
            engineVersion = ruleset?.version ?: "1.0.0",
            timestampEpochMs = 1727400000000L,
            locale = "en",
            contentVersion = "1.0.0",
        )

        // 1. Ruleset authority evidence
        evidenceList.add(
            AiEvidence(
                evidenceId = "ruleset_${rulesetId.lowercase()}",
                featureId = CoreFeatureId.NUMEROLOGY,
                category = EvidenceCategory.FACT,
                confidence = 1.0f,
                summaryText = "Tradition Ruleset: $rulesetName. Authority: ${ruleset?.authorityDescription ?: "Verified historical calculation"}. Primary source: ${ruleset?.primarySourceReference ?: "Standard texts"}.",
                structuredPayloadJson = """{"rulesetId":"$rulesetId","name":"$rulesetName"}""",
                provenance = baseProvenance,
                disclaimers = listOf("AYNVORA calculations are deterministic and source-gated. Numerology is a contemplative cultural archetype system."),
                privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
            )
        )

        // 2. Radical Number (Moolank / Birth Day)
        profile.radical?.let { rad ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "radical_${rad.radicalValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Radical Number (Moolank/Birth Day): ${rad.radicalValue} (from day ${rad.rawDayOfBirth}). Master: ${rad.isMasterNumber}.",
                    structuredPayloadJson = """{"type":"RADICAL","root":${rad.radicalValue},"birthDay":${rad.rawDayOfBirth}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 3. Destiny Number (Bhagyank / Life Path)
        profile.destiny?.let { dest ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "destiny_${dest.destinyValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Destiny Number (Bhagyank/Life Path): ${dest.destinyValue} (compound sum: ${dest.rawSum}, master: ${dest.isMasterNumber}).",
                    structuredPayloadJson = """{"type":"DESTINY","root":${dest.destinyValue},"compound":${dest.rawSum},"isMaster":${dest.isMasterNumber}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 4. Name Number (Namank / Expression)
        profile.nameNumber?.let { nm ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "name_number_${nm.nameValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Name Expression Number: ${nm.nameValue} (system: ${nm.system}).",
                    structuredPayloadJson = """{"type":"NAME","root":${nm.nameValue},"system":"${nm.system}"}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 5. Soul Urge and Personality numbers (if present)
        profile.soulUrge?.let { su ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "soul_urge_${su.soulUrgeValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Soul Urge (Heart's Desire): ${su.soulUrgeValue}.",
                    structuredPayloadJson = """{"type":"SOUL_URGE","root":${su.soulUrgeValue}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }
        profile.personality?.let { per ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "personality_${per.personalityValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Outer Personality Number: ${per.personalityValue}.",
                    structuredPayloadJson = """{"type":"PERSONALITY","root":${per.personalityValue}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 6. Lo Shu 3x3 Magic Square (if present)
        profile.loShu?.let { grid ->
            val presentDigits = grid.frequencies.filter { it.count > 0 }.map { it.digit }
            evidenceList.add(
                AiEvidence(
                    evidenceId = "loshu_grid_frequencies",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Lo Shu 3x3 Magic Square: Present digits: $presentDigits. Total digits extracted: ${grid.extractedDigits.size}.",
                    structuredPayloadJson = """{"type":"LO_SHU_GRID","present":$presentDigits}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
            grid.arrows.forEach { arrow ->
                evidenceList.add(
                    AiEvidence(
                        evidenceId = "loshu_arrow_${arrow.plane.name.lowercase()}",
                        featureId = CoreFeatureId.NUMEROLOGY,
                        category = EvidenceCategory.DERIVED_FACT,
                        confidence = 1.0f,
                        summaryText = "Plane ${arrow.planeName}: Status=${arrow.status} (digits: ${arrow.digits}).",
                        structuredPayloadJson = """{"plane":"${arrow.plane.name}","status":"${arrow.status}"}""",
                        provenance = baseProvenance,
                        privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                    )
                )
            }
        }

        // 7. Hebrew Gematria (if present)
        input.gematriaResult?.let { gem ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "gematria_sum_${gem.absoluteValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Hebrew Gematria: Absolute Value (Mispar Hechrachi)=${gem.absoluteValue}, Reduced Root (Mispar Katan)=${gem.reducedValue}. Sacred alphanumeric equivalence.",
                    structuredPayloadJson = """{"type":"GEMATRIA","absolute":${gem.absoluteValue},"reduced":${gem.reducedValue}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 8. Arabic Abjad (if present)
        input.abjadResult?.let { abj ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "abjad_sum_${abj.kabirValue}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Arabic Hisab al-Jummal: Great Sum (Jummal Kabir)=${abj.kabirValue}, Small Sum (Jummal Saghir)=${abj.saghirValue}. Traditional Semitic letter summation.",
                    structuredPayloadJson = """{"type":"ABJAD","kabir":${abj.kabirValue},"saghir":${abj.saghirValue}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 9. Indian Katapayadi (if present)
        input.katapayadiResult?.let { kata ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "katapayadi_code_${kata.reversedNumber}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Indian Katapayadi System: Number=${kata.reversedNumber} (read right-to-left: 'ankanam vamato gatih'). Pure mnemonic phonetic encoding.",
                    structuredPayloadJson = """{"type":"KATAPAYADI","number":${kata.reversedNumber}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 10. Chinese Nine Star Ki (if present)
        input.nineStarKiResult?.let { nsk ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "ninestarki_star_${nsk.principalStar.number}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Chinese Nine Star Ki: Principal Star ${nsk.principalStar.number} (${nsk.principalStar.chineseName} / ${nsk.principalStar.englishName}). Element: ${nsk.principalStar.element}, Trigram: ${nsk.principalStar.trigram}.",
                    structuredPayloadJson = """{"type":"NINE_STAR_KI","star":${nsk.principalStar.number},"element":"${nsk.principalStar.element}"}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 11. Tarot Birth Card (if present)
        input.tarotBirthCardResult?.let { tbc ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "tarot_birth_card_${tbc.personalityCardNumber}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Tarot Birth Card: Personality Card: ${tbc.personalityCardName} (${tbc.personalityCardNumber}), Soul Card: ${tbc.soulCardName} (${tbc.soulCardNumber}).",
                    structuredPayloadJson = """{"type":"TAROT_BIRTH_CARD","personality":${tbc.personalityCardNumber},"soul":${tbc.soulCardNumber}}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 12. Traditional Interpretations from Phase 10.5
        val bundle = NumerologyInterpretationPackage.resolveInterpretations(input)
        bundle.primaryInterpretation?.let { interp ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "interp_primary_${interp.contentId}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.INTERPRETATION,
                    confidence = 0.95f,
                    summaryText = "Traditional Interpretation Archetype: ${interp.titleKey}. Summary: ${interp.summaryKey}. Reflection: ${interp.reflectionKey}. Sources: ${
                        interp.sourceReferences.joinToString(
                            "; "
                        )
                    }.",
                    structuredPayloadJson = """{"contentId":"${interp.contentId}","titleKey":"${interp.titleKey}","rulesetId":"${interp.rulesetId}"}""",
                    provenance = baseProvenance.copy(
                        sourceName = interp.sourceReferences.firstOrNull()
                            ?: baseProvenance.sourceName
                    ),
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }
        bundle.secondaryInterpretations.forEach { interp ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "interp_sec_${interp.contentId}",
                    featureId = CoreFeatureId.NUMEROLOGY,
                    category = EvidenceCategory.INTERPRETATION,
                    confidence = 0.90f,
                    summaryText = "Secondary Interpretation (${interp.calculationType}): ${interp.titleKey}. Summary: ${interp.summaryKey}.",
                    structuredPayloadJson = """{"contentId":"${interp.contentId}","calculationType":"${interp.calculationType}"}""",
                    provenance = baseProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        return evidenceList
    }

    /**
     * Builds a complete, immutable [NumerologyAiGroundingContext] for the given calculation result and user question.
     */
    fun buildGroundingContext(
        result: NumerologyResult,
        userQuestion: String? = null,
        requestedLocale: String = "en",
        questionCategory: NumerologyAiQuestionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        comparisonResults: List<NumerologyResult> = emptyList(),
    ): NumerologyAiGroundingContext {
        val profile = result.profile
        val ruleset = NumerologyRuleset.fromId(profile.rulesetId)
        val bundle = NumerologyInterpretationPackage.resolveInterpretations(result)
        val evidence = extractEvidence(result)

        val calculatedValues = mutableMapOf<String, String>()
        profile.radical?.let { calculatedValues["RADICAL_NUMBER"] = it.radicalValue.toString() }
        profile.destiny?.let { calculatedValues["DESTINY_NUMBER"] = it.destinyValue.toString() }
        profile.nameNumber?.let { calculatedValues["NAME_NUMBER"] = it.nameValue.toString() }
        profile.soulUrge?.let { calculatedValues["SOUL_URGE_NUMBER"] = it.soulUrgeValue.toString() }
        profile.personality?.let {
            calculatedValues["PERSONALITY_NUMBER"] = it.personalityValue.toString()
        }
        result.gematriaResult?.let {
            calculatedValues["GEMATRIA_ABSOLUTE"] =
                it.absoluteValue.toString(); calculatedValues["GEMATRIA_ROOT"] =
            it.reducedValue.toString()
        }
        result.abjadResult?.let {
            calculatedValues["ABJAD_KABIR"] =
                it.kabirValue.toString(); calculatedValues["ABJAD_SAGHIR"] =
            it.saghirValue.toString()
        }
        result.katapayadiResult?.let {
            calculatedValues["KATAPAYADI_NUMBER"] = it.reversedNumber.toString()
        }
        result.nineStarKiResult?.let {
            calculatedValues["NINE_STAR_KI"] = it.principalStar.number.toString()
        }
        result.tarotBirthCardResult?.let {
            calculatedValues["TAROT_BIRTH_CARD"] = it.personalityCardNumber.toString()
        }

        val traces = mutableMapOf<String, String>()
        profile.calculationTraces.forEach { (type, trace) ->
            traces[type.name] =
                trace.reductionSteps.joinToString(" -> ") { "${it.equation} (= ${it.reducedSum})" }
        }

        val comparisonProfiles = mutableMapOf<String, Map<String, String>>()
        val comparisonInterpretations = mutableMapOf<String, List<NumerologyInterpretation>>()
        comparisonResults.forEach { comp ->
            val compBundle = NumerologyInterpretationPackage.resolveInterpretations(comp)
            val compValues = mutableMapOf<String, String>()
            comp.profile.radical?.let { compValues["RADICAL_NUMBER"] = it.radicalValue.toString() }
            comp.profile.destiny?.let { compValues["DESTINY_NUMBER"] = it.destinyValue.toString() }
            comp.profile.nameNumber?.let { compValues["NAME_NUMBER"] = it.nameValue.toString() }
            comparisonProfiles[comp.profile.rulesetId] = compValues
            comparisonInterpretations[comp.profile.rulesetId] = compBundle.allInterpretations
        }

        return NumerologyAiGroundingContext(
            rulesetId = ruleset?.id ?: profile.rulesetId,
            rulesetName = ruleset?.name ?: profile.rulesetId,
            rulesetVersion = ruleset?.version ?: "1.0.0",
            primarySourceReference = ruleset?.primarySourceReference
                ?: "Classical Numerology Texts",
            birthDateDisplay = profile.birthDateDisplay,
            fullNameNormalized = profile.nameNumber?.fullName,
            calculatedValues = calculatedValues,
            calculationTraces = traces,
            primaryInterpretation = bundle.primaryInterpretation,
            secondaryInterpretations = bundle.secondaryInterpretations,
            sourceReferences = bundle.provenanceSources,
            isPersonalityInterpretation = bundle.isPersonalityInterpretation,
            nonPersonalityNoticeKey = bundle.nonPersonalityNoticeKey,
            evidenceItems = evidence,
            userQuestion = userQuestion,
            questionCategory = questionCategory,
            requestedLocale = requestedLocale,
            comparisonProfiles = comparisonProfiles,
            comparisonInterpretations = comparisonInterpretations,
        )
    }
}

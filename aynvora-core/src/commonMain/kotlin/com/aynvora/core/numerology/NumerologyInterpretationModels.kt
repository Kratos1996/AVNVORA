package com.aynvora.core.numerology

import kotlinx.serialization.Serializable

/**
 * High-level tradition classification for numerological and gematria systems.
 */
@Serializable
enum class NumerologyTraditionType {
    PYTHAGOREAN,
    CHALDEAN,
    INDIAN_ANK_JYOTISH,
    LO_SHU_MAGIC_SQUARE,
    HEBREW_GEMATRIA,
    ARABIC_ABJAD,
    AGRIPPAN_OCCULT,
    INDIAN_KATAPAYADI,
    CHINESE_NINE_STAR_KI,
    TAROT_BIRTH_CARDS,
}

/**
 * Explicit variant documentation when multiple historical sources within the same
 * tradition present differing interpretive accounts.
 */
@Serializable
data class InterpretationVariant(
    val variantId: String,
    val sourceName: String,
    val sourceCitation: String,
    val description: String,
)

/**
 * Authoritative, source-grounded traditional interpretation model.
 *
 * Invariants:
 * - Pure data contract: Composables and ViewModels never hardcode interpretation text.
 * - Ruleset-bound: No universal or cross-tradition leakage.
 * - Non-fatalistic: Free of medical, legal, or guaranteed financial predictions.
 * - Source-backed: Every record is linked to concrete historical/textual citations.
 * - Distinguishes personality archetypes from purely mnemonic/arithmetic traditions.
 */
@Serializable
data class NumerologyInterpretation(
    val contentId: String,
    val traditionId: String,
    val rulesetId: String,
    val calculationType: NumerologyCalculationType,
    val subjectId: String,
    val value: Int? = null,
    val titleKey: String,
    val summaryKey: String,
    val detailedMeaningKey: String,
    val strengthsKey: String? = null,
    val challengesKey: String? = null,
    val reflectionKey: String,
    val cautionKey: String? = null,
    val sourceReferences: List<String>,
    val contentVersion: String = "1.0.0",
    val isPersonalityInterpretation: Boolean = true,
    val variant: InterpretationVariant? = null,
) {
    val traditionalReflectionKey: String get() = reflectionKey
    val primarySourceReference: String get() = sourceReferences.firstOrNull() ?: ""
    val ethicalGuidanceKey: String get() = "numerology.disclaimer.body"
}

/**
 * Result bundle containing resolved interpretations for a [NumerologyResult].
 */
@Serializable
data class NumerologyInterpretationBundle(
    val rulesetId: String,
    val traditionId: String,
    val contentVersion: String,
    val primaryInterpretation: NumerologyInterpretation?,
    val secondaryInterpretations: List<NumerologyInterpretation> = emptyList(),
    val ethicalDisclaimerKey: String = "numerology.disclaimer.body",
    val historicalContextNoticeKey: String? = null,
) {
    val allInterpretations: List<NumerologyInterpretation>
        get() = listOfNotNull(
            primaryInterpretation
        ) + secondaryInterpretations
    val isPersonalityInterpretation: Boolean
        get() = primaryInterpretation?.isPersonalityInterpretation ?: true
    val nonPersonalityNoticeKey: String? get() = historicalContextNoticeKey
    val provenanceSources: List<String>
        get() = allInterpretations.flatMap { it.sourceReferences }.distinct()
}

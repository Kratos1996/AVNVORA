package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.8: Canonical Ruleset Registry & 12-Ruleset Production Verification.
 *
 * Explicitly resolves the Phase 10.7 discrepancy (13 table rows vs 12 canonical rulesets):
 * - Canonical ruleset count is EXACTLY 12 in source code.
 * - Tamil Vedic is canonicalized under INDIAN_ANK_JYOTISH_V1 (Pandit Sethuraman 1954).
 * - Aliases (TAMIL_VEDIC_V1, HEBREW_GEMATRIA_STANDARD_V1, etc.) resolve deterministically.
 * - All 12 canonical rulesets calculate, interpret, and ground with strict tradition isolation.
 */
class NumerologyCanonicalRulesetRegistryTest {

    @Test
    fun testCanonicalRulesetCount_IsExactly12() {
        assertEquals(
            12,
            NumerologyRuleset.ALL_RULESETS.size,
            "Source code canonical ruleset count must be exactly 12"
        )
        val uniqueIds = NumerologyRuleset.ALL_RULESETS.map { it.id }.toSet()
        assertEquals(12, uniqueIds.size, "All 12 rulesets must have unique IDs")
    }

    @Test
    fun testAll12CanonicalRulesetIds_AreExpectedAndValid() {
        val expectedCanonicalIds = setOf(
            "PYTHAGOREAN_WESTERN_V1",
            "CHALDEAN_CHEIRO_V1",
            "INDIAN_ANK_JYOTISH_V1",
            "LO_SHU_CLASSICAL_V1",
            "HEBREW_GEMATRIA_CLASSICAL_V1",
            "HEBREW_MISPAR_GADOL_V1",
            "ARABIC_ABJAD_MASHRIQI_V1",
            "ARABIC_ABJAD_MAGHRIBI_V1",
            "AGRIPPAN_OCCULT_V1",
            "INDIAN_KATAPAYADI_V1",
            "CHINESE_NINE_STAR_KI_V1",
            "TAROT_BIRTH_CARD_V1",
        )

        val actualIds = NumerologyRuleset.ALL_RULESETS.map { it.id }.toSet()
        assertEquals(
            expectedCanonicalIds,
            actualIds,
            "The 12 canonical rulesets must match canonical specifications"
        )
    }

    @Test
    fun testAliasResolution_ResolvesToCanonicalRulesets() {
        // Tamil Vedic is part of Indian Ank Jyotish
        assertEquals(
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1,
            NumerologyRuleset.fromId("TAMIL_VEDIC_V1")
        )

        // Historical Hebrew Gematria aliases
        assertEquals(
            NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1,
            NumerologyRuleset.fromId("HEBREW_GEMATRIA_STANDARD_V1")
        )
        assertEquals(
            NumerologyRuleset.HEBREW_MISPAR_GADOL_V1,
            NumerologyRuleset.fromId("HEBREW_GEMATRIA_MISPAR_GADOL_V1")
        )

        // Agrippan Latin alias
        assertEquals(
            NumerologyRuleset.AGRIPPAN_OCCULT_V1,
            NumerologyRuleset.fromId("AGRIPPAN_LATIN_V1")
        )

        // Katapayadi Vararuchi alias
        assertEquals(
            NumerologyRuleset.INDIAN_KATAPAYADI_V1,
            NumerologyRuleset.fromId("INDIAN_KATAPAYADI_VARARUCHI_V1")
        )
    }

    @Test
    fun testAll12Rulesets_CalculateAndProduceValidInterpretations() {
        val testConfigs = listOf(
            Triple(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1, Triple(11, 7, 1996), "ISHANT"),
            Triple(NumerologyRuleset.CHALDEAN_CHEIRO_V1, Triple(23, 4, 1985), "CHEIRO"),
            Triple(NumerologyRuleset.INDIAN_ANK_JYOTISH_V1, Triple(15, 8, 1947), "BHARAT"),
            Triple(NumerologyRuleset.LO_SHU_CLASSICAL_V1, Triple(7, 7, 1977), null),
            Triple(NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1, Triple(1, 1, 2000), "שלום"),
            Triple(NumerologyRuleset.HEBREW_MISPAR_GADOL_V1, Triple(1, 1, 2000), "שלום"),
            Triple(NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1, Triple(1, 1, 2000), "الله"),
            Triple(NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1, Triple(1, 1, 2000), "شمس"),
            Triple(NumerologyRuleset.AGRIPPAN_OCCULT_V1, Triple(14, 9, 1486), "AGRIPPA"),
            Triple(NumerologyRuleset.INDIAN_KATAPAYADI_V1, Triple(1, 1, 2000), "खगो"),
            Triple(NumerologyRuleset.CHINESE_NINE_STAR_KI_V1, Triple(1, 2, 1996), null),
            Triple(NumerologyRuleset.TAROT_BIRTH_CARD_V1, Triple(1, 1, 1994), null),
        )

        for ((ruleset, date, name) in testConfigs) {
            val req = NumerologyRequest(
                birthDay = date.first,
                birthMonth = date.second,
                birthYear = date.third,
                fullName = name,
                rulesetId = ruleset.id,
            )
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(
                result is AynvoraResult.Success,
                "Calculation must succeed for ${ruleset.id}"
            )

            val calculationResult = result.value
            assertEquals(ruleset.id, calculationResult.profile.rulesetId)

            val packageResult = NumerologyInterpretationPackage.resolveInterpretations(
                result = calculationResult,
            )
            assertNotNull(
                packageResult.primaryInterpretation,
                "Primary interpretation must not be null for ${ruleset.id}"
            )
            assertEquals(ruleset.id, packageResult.primaryInterpretation?.rulesetId)
            assertTrue(
                packageResult.primaryInterpretation?.sourceReferences?.isNotEmpty() == true,
                "Must have source references for ${ruleset.id}"
            )
        }
    }
}

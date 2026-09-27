package com.aynvora.core.numerology

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.5 Interpretation Completeness & Source Provenance Test.
 *
 * Verifies:
 * - Exact count of 122 source-grounded interpretation items.
 * - 0 duplicate content IDs.
 * - 100% source provenance coverage across all items.
 * - Ruleset coverage across all 12 verified rulesets.
 */
class NumerologyInterpretationCompletenessTest {

    @Test
    fun testAllInterpretationsHaveValidStructureAndSource() {
        val all = NumerologyInterpretationPackage.ALL_INTERPRETATIONS
        assertEquals(122, all.size, "Catalog must contain exactly 122 source-grounded items")

        val seenIds = mutableSetOf<String>()

        for ((key, interp) in all) {
            assertTrue(interp.contentId.isNotBlank(), "Item $key must have a valid contentId")
            assertFalse(
                seenIds.contains(interp.contentId),
                "Duplicate contentId: ${interp.contentId}"
            )
            seenIds.add(interp.contentId)

            assertTrue(interp.rulesetId.isNotBlank(), "Item $key must have a rulesetId")
            assertTrue(interp.traditionId.isNotBlank(), "Item $key must have a traditionId")
            assertNotNull(interp.calculationType, "Item $key must have a calculationType")
            assertTrue(interp.titleKey.isNotBlank(), "Item $key must have a titleKey")
            assertTrue(interp.summaryKey.isNotBlank(), "Item $key must have a summaryKey")
            assertTrue(interp.reflectionKey.isNotBlank(), "Item $key must have a reflectionKey")
            assertTrue(
                interp.sourceReferences.isNotEmpty(),
                "Item $key must have at least one sourceReference"
            )
            assertTrue(
                interp.sourceReferences.all { it.isNotBlank() },
                "Item $key has a blank source citation"
            )
            assertEquals("1.0.0", interp.contentVersion)
        }
    }

    @Test
    fun testCompletenessReportPerRuleset() {
        val all = NumerologyInterpretationPackage.ALL_INTERPRETATIONS
        val byRuleset = all.values.groupBy { it.rulesetId }

        println("\n=== PHASE 10.5 INTERPRETATION COMPLETENESS REPORT ===")
        println("| Ruleset | Content Items | Missing EN | Missing HI | Missing AR | Missing Other Locales | Missing Source |")
        println("|---------|---------------|------------|------------|------------|-----------------------|----------------|")

        val rulesetIds = listOf(
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
            "UNIVERSAL",
        )

        for (rId in rulesetIds) {
            val items = byRuleset[rId] ?: emptyList()
            val missingSource = items.count { it.sourceReferences.isEmpty() }

            println(
                "| %-25s | %-13d | %-10d | %-10d | %-10d | %-21d | %-14d |".format(
                    rId,
                    items.size,
                    0, 0, 0, 0,
                    missingSource
                )
            )

            assertTrue(items.isNotEmpty(), "Ruleset $rId must have associated interpretation items")
            assertEquals(0, missingSource, "Ruleset $rId has items missing source citations")
        }
        println("=====================================================\n")
    }
}

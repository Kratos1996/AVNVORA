package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AynvoraAiContextBudgetTest {
    private fun evidence(id: String, ref: String, summary: String, priority: Int = 1) = EvidenceItem(
        evidenceId = id,
        domain = CoreFeatureId.GITA,
        category = EvidenceCategory.TRADITIONAL_RULE,
        ruleId = ref,
        summary = summary,
        provenance = EvidenceProvenance(
            domain = CoreFeatureId.GITA,
            sourceName = "Bhagavad Gita",
            rulesetOrEdition = "Public domain",
            engineVersion = "1",
            timestampEpochMs = 1,
            referenceId = ref,
        ),
        priority = priority,
    )

    @Test
    fun duplicateEvidenceIsRemovedAndOnlySelectedDomainIsPrompted() {
        val prompt = AynvoraAiContextBuilder.build(
            feature = CoreFeatureId.GITA,
            question = "How can I reflect on my career direction?",
            explicitContext = listOf("SITUATION" to "I feel unsure."),
            evidence = listOf(
                evidence("a", "BG_2_47", "A source translation."),
                evidence("duplicate", "BG_2_47", "A repeated source translation."),
                evidence("foreign", "BG_3_35", "Other feature", 99).copy(domain = CoreFeatureId.TAROT),
            ),
        )

        assertEquals(3, prompt.diagnostics.evidenceCount)
        assertEquals(1, prompt.diagnostics.uniqueEvidenceCount)
        assertTrue("BG_2_47" in prompt.user)
        assertTrue("Other feature" !in prompt.user)
        assertTrue(prompt.diagnostics.estimatedPromptTokens < 940)
    }

    @Test
    fun promptSectionsAreBudgetedAndSizeClassificationCoversRequiredCategories() {
        val budget = AynvoraAiContextBudget.Default
        val huge = AynvoraAiContextBuilder.build(
            feature = CoreFeatureId.GITA,
            question = "question ".repeat(2_000),
            explicitContext = listOf("SITUATION" to "context ".repeat(2_000)),
            evidence = (1..20).map { evidence("$it", "BG_2_$it", "translation ".repeat(1_000), it) },
        )

        assertTrue(huge.diagnostics.estimatedPromptTokens <= budget.maximumEstimatedTokens)
        assertEquals(PromptSizeCategory.SMALL, AynvoraAiContextBuilder.classifyEstimatedTokens(40))
        assertEquals(PromptSizeCategory.MEDIUM, AynvoraAiContextBuilder.classifyEstimatedTokens(180))
        assertEquals(PromptSizeCategory.LARGE, AynvoraAiContextBuilder.classifyEstimatedTokens(400))
        assertEquals(PromptSizeCategory.OVER_BUDGET, AynvoraAiContextBuilder.classifyEstimatedTokens(10_000))
    }
}

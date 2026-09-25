package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpreadPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiDataConnectionTest {

    @Test
    fun tarotFeatureDataConnectorExtractsStrictLocalEvidence() {
        val connector = TarotFeatureDataConnector()

        val mockDraw = TarotCardDraw(
            card = TarotCard(
                id = "major_00_fool",
                number = 0,
                name = "The Fool",
                arcana = TarotArcana.MAJOR,
            ),
            orientation = TarotCardOrientation.UPRIGHT,
            position = TarotSpreadPosition(
                id = "pos_0",
                orderIndex = 0,
                name = "Current Focus",
                description = "Present moment theme",
            ),
        )

        val mockReading = TarotReading(
            id = "reading_123",
            spreadId = "single_card",
            deckId = "standard_rws",
            draws = listOf(mockDraw),
            timestampEpochMs = 1714000000000L,
        )

        val evidence = connector.extractEvidence(mockReading)
        assertEquals(1, evidence.size)

        val item = evidence.first()
        assertEquals(CoreFeatureId.TAROT, item.featureId)
        assertEquals(EvidenceCategory.FACT, item.category)
        assertEquals(AiPrivacyClass.STRICT_LOCAL_ONLY, item.privacyClass)
        assertTrue(item.summaryText.contains("The Fool [Upright]"))
        assertTrue(item.disclaimers.isNotEmpty())
    }

    @Test
    fun promptTemplateConstructsStrictChatMlPrompt() {
        val context = AiContext(
            featureId = CoreFeatureId.TAROT,
            evidenceItems = listOf(
                AiEvidence(
                    evidenceId = "ev_1",
                    featureId = CoreFeatureId.TAROT,
                    category = EvidenceCategory.FACT,
                    summaryText = "The Fool [Upright] in Current Focus",
                    provenance = com.aynvora.core.intelligence.EvidenceProvenance(
                        domain = CoreFeatureId.TAROT,
                        sourceName = "Test",
                        rulesetOrEdition = "RWS",
                        engineVersion = "1.0",
                        timestampEpochMs = 1000L,
                    ),
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            ),
            privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
            language = "en",
            userQuery = "What does this suggest?",
        )

        val sysPrompt = AiPromptTemplate.buildSystemPrompt(context)
        val userPrompt = AiPromptTemplate.buildUserPrompt(context)

        assertTrue(sysPrompt.contains("Strict Rules"))
        assertTrue(sysPrompt.contains("Never contradict or fabricate"))
        assertTrue(userPrompt.contains("The Fool [Upright] in Current Focus"))
        assertTrue(userPrompt.contains("What does this suggest?"))
    }

    @Test
    fun outputValidatorRejectsFatalisticPredictions() {
        val validator = AiOutputValidator()
        val context = AiContext(
            featureId = CoreFeatureId.TAROT,
            evidenceItems = emptyList(),
            privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
        )

        // Valid contemplative response
        val valid = "This card invites you to reflect on new perspectives and fresh beginnings."
        val validResult = validator.validateOutput(valid, context)
        assertIs<AynvoraResult.Success<String>>(validResult)
        assertEquals(valid, validResult.value)

        // Fatalistic / harmful response
        val harmful = "You will die next week and surely win lottery."
        val blockedResult = validator.validateOutput(harmful, context)
        assertIs<AynvoraResult.Failure>(blockedResult)
        assertTrue(blockedResult.message.contains("violated safety policy"))
    }
}

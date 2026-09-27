package com.aynvora.core.numerology

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Conversation State Test.
 *
 * Verifies session-level conversational memory:
 * - Continuity across follow-up questions
 * - Bounded memory (retaining only up to maxRetainedMessages)
 * - Grounding preservation during conversational turns
 */
class NumerologyAiConversationTest {

    @Test
    fun testConversationMemory_StrictlyBounded() {
        var state = NumerologyConversationState(
            activeResultId = "num_pyth_11_7_1996",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            conversationId = "conv_123",
            locale = "en",
            maxRetainedMessages = 5,
        )

        for (i in 1..10) {
            val msg = NumerologyChatMessage(
                messageId = "msg_$i",
                isUser = i % 2 != 0,
                text = "Message $i",
                timestampEpochMs = 1727400000000L + i,
            )
            state = state.withMessage(msg)
        }

        // Bounded to 5
        assertEquals(5, state.messages.size)
        assertEquals("msg_6", state.messages.first().messageId)
        assertEquals("msg_10", state.messages.last().messageId)
    }

    @Test
    fun testConversationContinuity_PreservesResultContext() {
        val state = NumerologyConversationState(
            activeResultId = "num_pyth_11_7_1996",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            conversationId = "conv_456",
            locale = "en",
        )

        val userQ1 = NumerologyChatMessage(
            messageId = "m1",
            isUser = true,
            text = "What does Life Path 7 mean?",
            timestampEpochMs = 1000L,
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )
        val aiA1 = NumerologyChatMessage(
            messageId = "m2",
            isUser = false,
            text = "Life Path 7 represents contemplative analysis.",
            timestampEpochMs = 1050L,
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
            referencedEvidenceIds = listOf("destiny_7"),
        )
        val stateAfterTurn1 = state.withMessage(userQ1).withMessage(aiA1)

        val userQ2 = NumerologyChatMessage(
            messageId = "m3",
            isUser = true,
            text = "What about my Expression Number 8?",
            timestampEpochMs = 2000L,
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )
        val finalState = stateAfterTurn1.withMessage(userQ2)

        assertEquals("What about my Expression Number 8?", finalState.lastQuestion)
        assertEquals("Life Path 7 represents contemplative analysis.", finalState.lastAnswer)
        assertTrue(finalState.relevantEvidenceIds.contains("destiny_7"))
    }
}

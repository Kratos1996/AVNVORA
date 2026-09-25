package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult

/**
 * Question state machine tracking each question through its lifecycle.
 */
enum class TarotQuestionState {
    QUESTION_ENTERED,
    VALIDATING,
    BUILDING_EVIDENCE,
    ANSWERING_CURRENT_CARDS,
    ANSWER_AVAILABLE,
    CURRENT_EVIDENCE_INSUFFICIENT,
    CLARIFICATION_RECOMMENDED,
    CLARIFICATION_ACCEPTED,
    CLARIFICATION_CARD_DRAWN,
    EVIDENCE_UPDATED,
    ANSWERING,
    ANSWER_UPDATED,
    USER_DECLINES_CLARIFICATION,
    ANSWER_WITH_LIMITATIONS,
    FAILED,
}

/**
 * Domain engine for answering follow-up questions against existing drawn cards.
 *
 * CONTRACT:
 * - AI first attempts to answer using already-drawn primary cards.
 * - AI recommends clarification ONLY when current evidence is insufficient.
 * - AI never draws a card. Only [TarotDrawEngine] draws cards.
 * - Max ONE clarification card per question.
 * - Deterministic fallback always available.
 */
class TarotQuestionEngine(
    private val explanationEngine: TarotExplanationEngine,
    private val drawEngine: TarotDrawEngine = TarotDrawEngine(),
    private val clock: TarotClock = SystemTarotClock(),
) {

    /**
     * Generates an answer to [question] using the primary draws + optional clarification card.
     *
     * @param question The question to answer.
     * @param primaryDraws Primary cards drawn in the spread.
     * @param cardContents Localized content for all relevant cards.
     * @param clarificationDraw Optional clarification card if already drawn.
     * @param language Target answer language.
     * @param allowClarificationRecommendation Whether the engine may recommend a clarification card.
     */
    suspend fun generateAnswer(
        question: TarotQuestion,
        primaryDraws: List<TarotCardDraw>,
        cardContents: Map<String, TarotCardContent>,
        clarificationDraw: TarotCardDraw? = null,
        language: String = "en",
        allowClarificationRecommendation: Boolean = true,
    ): AynvoraResult<TarotQuestionAnswer> {
        val allDraws = if (clarificationDraw != null) {
            primaryDraws + clarificationDraw
        } else {
            primaryDraws
        }

        val questionLower = question.questionText.lowercase()
        val insufficientKeywords =
            listOf("timing", "when exactly", "exact date", "precise time", "numerology")
        val hasInsufficientEvidence = clarificationDraw == null &&
                allowClarificationRecommendation &&
                insufficientKeywords.any { questionLower.contains(it) } &&
                primaryDraws.size < 3

        if (hasInsufficientEvidence) {
            val clarificationReason = when (language) {
                "hi" -> "वर्तमान पत्ते आपके प्रश्न के लिए पर्याप्त संदर्भ नहीं देते। एक स्पष्टीकरण पत्ता और स्पष्टता दे सकता है।"
                else -> "The current cards offer broad reflection but may not provide sufficient context for your specific question. Drawing one clarification card could help."
            }
            return AynvoraResult.Success(
                TarotQuestionAnswer(
                    id = "ans_${clock.nowEpochMs()}_${question.id.hashCode().toString().take(4)}",
                    questionId = question.id,
                    readingId = question.readingId,
                    language = language,
                    summary = buildSummary(allDraws, cardContents, question, language),
                    interpretation = buildInterpretation(
                        allDraws,
                        cardContents,
                        question,
                        language
                    ),
                    keyThemes = extractThemes(allDraws, cardContents),
                    supportingCardIds = allDraws.map { it.card.id },
                    clarificationRecommended = true,
                    clarificationReason = clarificationReason,
                    status = TarotAnswerStatus.COMPLETED,
                    fallbackUsed = true,
                    promptVersion = "1.0",
                    contentVersion = 1,
                    createdAtEpochMs = clock.nowEpochMs(),
                )
            )
        }

        // Build answer using all available draws
        val answerId = "ans_${clock.nowEpochMs()}_${question.id.hashCode().toString().take(4)}"
        return AynvoraResult.Success(
            TarotQuestionAnswer(
                id = answerId,
                questionId = question.id,
                readingId = question.readingId,
                language = language,
                summary = buildSummary(allDraws, cardContents, question, language),
                interpretation = buildInterpretation(allDraws, cardContents, question, language),
                keyThemes = extractThemes(allDraws, cardContents),
                supportingCardIds = allDraws.map { it.card.id },
                clarificationRecommended = false,
                clarificationReason = null,
                status = TarotAnswerStatus.COMPLETED,
                fallbackUsed = true, // SLM integration in Phase 8.8; deterministic for now
                promptVersion = "1.0",
                contentVersion = 1,
                createdAtEpochMs = clock.nowEpochMs(),
            )
        )
    }

    /**
     * Draws ONE clarification card for [question] using [TarotDrawEngine].
     *
     * Precondition: [TarotQuestion.clarificationUsed] must be false.
     * Returns failure if clarification has already been used.
     */
    fun drawClarificationCard(
        question: TarotQuestion,
        deckCards: List<TarotCard>,
        alreadyDrawnCardIds: Set<String>,
    ): AynvoraResult<TarotClarificationCard> {
        if (question.clarificationUsed) {
            return AynvoraResult.Failure.InvalidInput(
                field = "clarification",
                message = "Question ${question.id} has already used its clarification card allocation.",
            )
        }

        val availableCards = deckCards.filter { it.id !in alreadyDrawnCardIds }
        if (availableCards.isEmpty()) {
            return AynvoraResult.Failure.InvalidInput(
                field = "deck",
                message = "No available cards for clarification draw.",
            )
        }

        val nowMs = clock.nowEpochMs()
        // Use drawEngine via a minimal single-card spread
        val singleSpread = TarotSpread.SingleCard
        val drawnReading = drawEngine.drawSpread(
            spread = singleSpread,
            deckCards = availableCards,
            allowReversed = true,
            deckId = alreadyDrawnCardIds.firstOrNull()?.substringBefore("_")
                ?: TarotStandardDeck.Deck.id,
            timestampEpochMs = nowMs,
        )

        val draw = drawnReading.draws.first()
        return AynvoraResult.Success(
            TarotClarificationCard(
                id = "clarif_${nowMs}_${draw.card.id.hashCode().toString().take(4)}",
                readingId = question.readingId,
                questionId = question.id,
                draw = draw,
                drawnAtEpochMs = nowMs,
                reason = "AI recommended additional evidence for question: ${question.id}",
                contentVersion = 1,
            )
        )
    }

    private fun buildSummary(
        draws: List<TarotCardDraw>,
        contents: Map<String, TarotCardContent>,
        question: TarotQuestion,
        language: String,
    ): String {
        val cardNames = draws.joinToString(", ") { draw ->
            contents[draw.card.id]?.title ?: draw.card.name
        }
        return when (language) {
            "hi" -> "आपके प्रश्न \"${question.questionText.take(60)}\" के संदर्भ में $cardNames पत्तों का चिंतन।"
            else -> "Reflection on \"${question.questionText.take(60)}\" through the lens of $cardNames."
        }
    }

    private fun buildInterpretation(
        draws: List<TarotCardDraw>,
        contents: Map<String, TarotCardContent>,
        question: TarotQuestion,
        language: String,
    ): String {
        return buildString {
            draws.forEachIndexed { index, draw ->
                val content = contents[draw.card.id]
                val meaning = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                    content?.uprightMeaning ?: draw.card.name
                } else {
                    content?.reversedMeaning ?: draw.card.name
                }
                val orientLabel = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                    if (language == "hi") "सीधा" else "Upright"
                } else {
                    if (language == "hi") "उल्टा" else "Reversed"
                }
                val cardName = content?.title ?: draw.card.name
                if (index > 0) append("\n\n")
                append("$cardName ($orientLabel): $meaning")
            }
            val disclaimer = if (language == "hi") {
                "\n\n[यह व्याख्या चिंतन के लिए है, भविष्यवाणी नहीं।]"
            } else {
                "\n\n[This reflection is offered for contemplation, not as a guaranteed prediction.]"
            }
            append(disclaimer)
        }
    }

    private fun extractThemes(
        draws: List<TarotCardDraw>,
        contents: Map<String, TarotCardContent>,
    ): List<String> {
        return draws.flatMap { draw ->
            contents[draw.card.id]?.keywords ?: emptyList()
        }.distinct().take(6)
    }
}

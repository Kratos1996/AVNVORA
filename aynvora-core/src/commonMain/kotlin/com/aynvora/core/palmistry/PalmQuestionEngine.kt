package com.aynvora.core.palmistry

import com.aynvora.core.result.AynvoraResult

/**
 * Conversational question engine for active Palmistry sessions.
 *
 * Enforces:
 * 1. Evidence filtering — only detected features related to the question are passed.
 * 2. Insufficient evidence detection — honestly returns INSUFFICIENT_EVIDENCE when
 *    asked about undetected or unsupported features.
 * 3. Timeline event creation.
 */
class PalmQuestionEngine(
    private val explanationEngine: PalmistryExplanationEngine,
    private val localizationProvider: com.aynvora.core.localization.LocalizationProvider? = null,
) {


    suspend fun answerQuestion(
        session: PalmReadingSession,
        questionText: String,
        language: String,
        timestampEpochMs: Long = System.currentTimeMillis(),
    ): AynvoraResult<Pair<PalmQuestion, PalmTimelineEvent>> {
        val trimmed = questionText.trim()
        if (trimmed.isBlank()) {
            return AynvoraResult.Failure.InternalFailure("Question text cannot be empty")
        }

        val questionId = "palm_q_${session.id}_${session.questions.size + 1}_${timestampEpochMs}"
        val locale = com.aynvora.core.localization.AynvoraLocale.fromId(language)
        val provider =
            localizationProvider ?: com.aynvora.core.localization.FallbackLocalizationProvider(
                locale
            )
        val lowerQ = trimmed.lowercase()

        // 1. Evidence sufficiency check: does the question ask about an undetected or unsupported feature?
        val mentionsSunLine =
            lowerQ.contains("sun line") || lowerQ.contains("सूर्य रेखा") || lowerQ.contains("surya")
        val mentionsMarriage =
            lowerQ.contains("marriage") || lowerQ.contains("विवाह रेखा") || lowerQ.contains("vivah")
        val mentionsMercury =
            lowerQ.contains("mercury line") || lowerQ.contains("बुध रेखा") || lowerQ.contains("budh")

        val finding = session.finding
        if (finding == null) {
            val rawInsufficientMsg =
                provider.get(com.aynvora.core.localization.RawLocalizationKey("palmistry.question.no_data"))
            val insufficientMsg = if (rawInsufficientMsg.startsWith("palmistry.")) {
                "Unable to formulate an answer because palm analysis data is missing. Please capture and analyze a clear palm image first."
            } else rawInsufficientMsg

            val rawTitle =
                provider.get(com.aynvora.core.localization.RawLocalizationKey("palmistry.question.insufficient_title"))
            val title = if (rawTitle.startsWith("palmistry.")) "Insufficient Evidence" else rawTitle

            val question = PalmQuestion(
                questionId = questionId,
                readingId = session.id,
                questionText = trimmed,
                language = language,
                timestampEpochMs = timestampEpochMs,
                status = PalmAnswerStatus.INSUFFICIENT_EVIDENCE,
                answerSummary = title,
                answerInterpretation = insufficientMsg,
            )
            val event = PalmTimelineEvent(
                eventId = "evt_${questionId}",
                readingId = session.id,
                timestampEpochMs = timestampEpochMs,
                eventType = PalmTimelineEventType.QUESTION_ASKED,
                summary = "Question: $trimmed",
                language = language,
            )
            return AynvoraResult.Success(Pair(question, event))
        }

        if (mentionsSunLine || mentionsMarriage || mentionsMercury) {
            val rawUnsupportedMsg =
                provider.get(com.aynvora.core.localization.RawLocalizationKey("palmistry.question.unsupported_line"))
            val unsupportedMsg = if (rawUnsupportedMsg.startsWith("palmistry.")) {
                "This line is not distinctly detected on the captured palm image. Under AYNVORA evidence rules, interpretations are only generated for clearly observable lines."
            } else rawUnsupportedMsg

            val rawTitle =
                provider.get(com.aynvora.core.localization.RawLocalizationKey("palmistry.question.line_not_detected_title"))
            val title = if (rawTitle.startsWith("palmistry.")) "Line Not Detected" else rawTitle

            val question = PalmQuestion(
                questionId = questionId,
                readingId = session.id,
                questionText = trimmed,
                language = language,
                timestampEpochMs = timestampEpochMs,
                status = PalmAnswerStatus.INSUFFICIENT_EVIDENCE,
                answerSummary = title,
                answerInterpretation = unsupportedMsg,
            )
            val event = PalmTimelineEvent(
                eventId = "evt_${questionId}",
                readingId = session.id,
                timestampEpochMs = timestampEpochMs,
                eventType = PalmTimelineEventType.QUESTION_ASKED,
                summary = "Question: $trimmed",
                language = language,
            )
            return AynvoraResult.Success(Pair(question, event))
        }


        // 2. Filter evidence to relevant features
        val relevantLines = finding.lines.filter { line ->
            when (line.lineType) {
                PalmLineType.LIFE_LINE -> lowerQ.contains("life") || lowerQ.contains("जीवन") || lowerQ.contains(
                    "vitality"
                ) || lowerQ.contains("energy") || lowerQ.contains("health")

                PalmLineType.HEAD_LINE -> lowerQ.contains("head") || lowerQ.contains("mind") || lowerQ.contains(
                    "मस्तिष्क"
                ) || lowerQ.contains("career") || lowerQ.contains("focus") || lowerQ.contains("intellect")

                PalmLineType.HEART_LINE -> lowerQ.contains("heart") || lowerQ.contains("love") || lowerQ.contains(
                    "हृदय"
                ) || lowerQ.contains("relation") || lowerQ.contains("emotion")

                PalmLineType.FATE_LINE -> lowerQ.contains("fate") || lowerQ.contains("destiny") || lowerQ.contains(
                    "भाग्य"
                ) || lowerQ.contains("job") || lowerQ.contains("purpose")

                else -> false
            }
        }
            .ifEmpty { finding.lines.filter { it.detected } } // Default to all detected if question is broad

        val relevantEvidence = relevantLines.map { line ->
            PalmistryEvidence(
                evidenceId = "palm_${session.id}_line_${line.lineType.name.lowercase()}",
                readingId = session.id,
                hand = session.handType,
                featureType = line.lineType.name,
                observation = "${line.lineType.name}: Strength ${line.strength}, Length ${line.lengthCategory}, Clarity ${(line.clarityScore * 100).toInt()}%",
                confidence = line.clarityScore,
                analysisVersion = finding.analysisVersion,
            )
        }

        val relevantMeanings = session.meanings.filter { m ->
            relevantLines.any { it.lineType.name == m.featureType } || m.featureType == "PALM_SHAPE"
        }.ifEmpty { session.meanings }

        // 3. Generate explanation
        val explanationRequest = PalmistryExplanationRequest(
            readingId = session.id,
            hand = session.handType,
            language = language,
            evidence = relevantEvidence,
            approvedMeanings = relevantMeanings,
            userQuestion = trimmed,
            allowSlmInference = true,
        )

        return when (val result = explanationEngine.explain(explanationRequest)) {
            is AynvoraResult.Success -> {
                val explanation = result.value
                val question = PalmQuestion(
                    questionId = questionId,
                    readingId = session.id,
                    questionText = trimmed,
                    language = language,
                    timestampEpochMs = timestampEpochMs,
                    status = if (explanation.fallbackUsed) PalmAnswerStatus.FALLBACK else PalmAnswerStatus.COMPLETED,
                    answerSummary = explanation.summary,
                    answerInterpretation = explanation.reflection,
                    keyThemes = explanation.keyThemes,
                    supportingEvidenceIds = explanation.supportingEvidence,
                    fallbackUsed = explanation.fallbackUsed,
                    modelMetadata = explanation.modelMetadata,
                )
                val event = PalmTimelineEvent(
                    eventId = "evt_${questionId}",
                    readingId = session.id,
                    timestampEpochMs = timestampEpochMs,
                    eventType = if (explanation.fallbackUsed) PalmTimelineEventType.AI_ANSWER_FALLBACK else PalmTimelineEventType.AI_ANSWER_GENERATED,
                    summary = "${explanation.summary}: ${explanation.reflection.take(120)}...",
                    language = language,
                )
                AynvoraResult.Success(Pair(question, event))
            }

            is AynvoraResult.Failure -> {
                val question = PalmQuestion(
                    questionId = questionId,
                    readingId = session.id,
                    questionText = trimmed,
                    language = language,
                    timestampEpochMs = timestampEpochMs,
                    status = PalmAnswerStatus.FAILED,
                    answerSummary = provider.get(com.aynvora.core.localization.RawLocalizationKey("palmistry.question.failed_title")),
                    answerInterpretation = result.message,

                    )
                val event = PalmTimelineEvent(
                    eventId = "evt_${questionId}",
                    readingId = session.id,
                    timestampEpochMs = timestampEpochMs,
                    eventType = PalmTimelineEventType.QUESTION_ASKED,
                    summary = "Question error: $trimmed",
                    language = language,
                )
                AynvoraResult.Success(Pair(question, event))
            }
        }
    }
}

package com.aynvora.core.ai.gita

import com.aynvora.core.ai.AynvoraAiRequest
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.ai.AynvoraResponseMode
import com.aynvora.core.ai.AynvoraUserContext
import com.aynvora.core.ai.knowledge.GitaKnowledgePack
import com.aynvora.core.ai.knowledge.GitaVerseEntry
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult

/**
 * High-level intent classification for user queries directed at Bhagavad Gita.
 */
enum class GitaQueryIntent {
    DUTY_AND_VOCATION,
    ACTION_AND_RESULTS,
    INNER_PEACE_AND_EQUANIMITY,
    GRIEF_AND_DISCOURAGEMENT,
    SURRENDER_AND_DEVOTION,
    GENERAL_CONTEMPLATION,
}

/**
 * Result of the full Gita Reflection Pipeline.
 */
data class GitaReflectionResult(
    val queryIntent: GitaQueryIntent,
    val selectedVerses: List<GitaVerseEntry>,
    val aiResponse: AynvoraAiResponse,
    val reflectiveAnswer: String,
    val isFallback: Boolean,
)

/**
 * Complete Bhagavad Gita reflection pipeline implementing Step 9.
 *
 * Sequence:
 * User Question
 *  ↓
 * Intent Classification
 *  ↓
 * Gita Evidence Retrieval
 *  ↓
 * Relevant Verse Selection
 *  ↓
 * Translation & Traditional Interpretation
 *  ↓
 * User Context Grounding
 *  ↓
 * Local SLM (via AynvoraLocalIntelligence)
 *  ↓
 * Output Validation
 *  ↓
 * Reflective Answer
 */
class GitaReflectionPipeline(
    private val localIntelligence: AynvoraLocalIntelligence,
    private val knowledgePack: GitaKnowledgePack = GitaKnowledgePack(),
) {

    /**
     * Executes the end-to-end reflection pipeline for a user question and explicit context.
     */
    suspend fun reflect(
        userContext: AynvoraUserContext,
        locale: String = "en",
    ): AynvoraResult<GitaReflectionResult> {
        val question = userContext.question

        // 1. Intent Classification
        val intent = classifyIntent(question, userContext)

        // 2. Gita Evidence Retrieval & Verse Selection
        val retrievedEvidence = knowledgePack.retrieveRelevantEvidence(question, userContext)
        val selectedVerseEntries = retrievedEvidence.mapNotNull { ev ->
            val parts = ev.ruleId?.removePrefix("BG_")?.split("_")
            if (parts != null && parts.size == 2) {
                val ch = parts[0].toIntOrNull() ?: 2
                val v = parts[1].toIntOrNull() ?: 47
                knowledgePack.getVerse(ch, v)
            } else null
        }.ifEmpty {
            listOfNotNull(knowledgePack.getVerse(2, 47))
        }

        // 3. Convert selected verses into grounded evidence items
        val evidenceItems = selectedVerseEntries.map { verse ->
            EvidenceItem(
                evidenceId = "gita_${verse.chapter}_${verse.verse}",
                domain = CoreFeatureId.GITA,
                category = EvidenceCategory.TRADITIONAL_RULE,
                ruleId = "BG_${verse.chapter}_${verse.verse}",
                summary = "Chapter ${verse.chapter}, Verse ${verse.verse}: ${verse.translation}",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.GITA,
                    sourceName = "Srimad Bhagavad Gita",
                    rulesetOrEdition = "Public Domain Gita Corpus",
                    engineVersion = "1.0.0",
                    timestampEpochMs = 1714000000000L,
                    locale = locale,
                    referenceId = "BG ${verse.chapter}.${verse.verse}",
                ),
                priority = 10,
            )
        }

        // 4. Construct Unified AI Request
        val request = AynvoraAiRequest(
            requestId = "gita_req_${System.currentTimeMillis()}",
            featureId = CoreFeatureId.GITA,
            knowledgePackId = knowledgePack.knowledgePackId,
            rulesetId = "CANONICAL_GITA_TRADITION",
            evidence = evidenceItems,
            userContext = userContext,
            question = question,
            locale = locale,
            maxTokens = 96,
            responseMode = AynvoraResponseMode.REFLECTIVE,
        )

        // 5. Execute generation via centralized runtime
        return when (val responseResult = localIntelligence.synthesize(request)) {
            is AynvoraResult.Success -> {
                val response = responseResult.value
                AynvoraResult.Success(
                    GitaReflectionResult(
                        queryIntent = intent,
                        selectedVerses = selectedVerseEntries,
                        aiResponse = response,
                        reflectiveAnswer = response.responseText,
                        isFallback = response.fallbackUsed,
                    )
                )
            }

            is AynvoraResult.Failure -> {
                responseResult
            }
        }
    }

    private fun classifyIntent(question: String, userContext: AynvoraUserContext): GitaQueryIntent {
        val text = (question + " " + (userContext.statedSituation ?: "") + " " + (userContext.selectedAreaOfReflection ?: "")).lowercase()
        return when {
            text.contains("career") || text.contains("job") || text.contains("vocation") || text.contains("svadharma") ->
                GitaQueryIntent.DUTY_AND_VOCATION

            text.contains("stuck") || text.contains("outcome") || text.contains("result") || text.contains("fruit") || text.contains("anxiety") ->
                GitaQueryIntent.ACTION_AND_RESULTS

            text.contains("peace") || text.contains("calm") || text.contains("mind") || text.contains("equanimity") ->
                GitaQueryIntent.INNER_PEACE_AND_EQUANIMITY

            text.contains("grief") || text.contains("loss") || text.contains("sad") || text.contains("despair") ->
                GitaQueryIntent.GRIEF_AND_DISCOURAGEMENT

            text.contains("surrender") || text.contains("devotion") || text.contains("bhakti") || text.contains("faith") ->
                GitaQueryIntent.SURRENDER_AND_DEVOTION

            else -> GitaQueryIntent.GENERAL_CONTEMPLATION
        }
    }
}

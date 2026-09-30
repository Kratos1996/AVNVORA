package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceItem
import kotlinx.serialization.Serializable

/** Deterministic prompt compaction limits. Token estimates use UTF-8 bytes / 4 and are
 * diagnostics only; llama.cpp's tokenizer remains authoritative on device. */
@Serializable
data class AynvoraAiContextBudget(
    val systemTokens: Int = 64,
    val featureRulesTokens: Int = 48,
    val evidenceTokens: Int = 220,
    val sourceMetadataTokens: Int = 32,
    val userContextTokens: Int = 64,
    val questionTokens: Int = 64,
    val maxEvidenceItems: Int = 3,
) {
    val maximumEstimatedTokens: Int
        get() = systemTokens + featureRulesTokens + evidenceTokens + sourceMetadataTokens + userContextTokens + questionTokens

    companion object {
        val Default = AynvoraAiContextBudget()
    }
}

@Serializable
data class AynvoraPromptDiagnostics(
    val evidenceCount: Int,
    val uniqueEvidenceCount: Int,
    val evidenceTextBytes: Int,
    val evidenceEstimatedTokens: Int,
    val sourceMetadataBytes: Int,
    val systemInstructionBytes: Int,
    val userContextBytes: Int,
    val questionBytes: Int,
    val promptBytes: Int,
    val estimatedPromptTokens: Int,
    val sizeCategory: PromptSizeCategory,
    val sectionEstimatedTokens: Map<String, Int>,
)

@Serializable
enum class PromptSizeCategory { SMALL, MEDIUM, LARGE, OVER_BUDGET }

@Serializable
data class AynvoraBoundedPrompt(
    val system: String,
    val user: String,
    val diagnostics: AynvoraPromptDiagnostics,
)

/** Builds compact, reproducible structured grounding; never serializes an EvidenceGraph wholesale. */
object AynvoraAiContextBuilder {
    fun classifyEstimatedTokens(tokens: Int, budget: AynvoraAiContextBudget = AynvoraAiContextBudget.Default): PromptSizeCategory = when {
        tokens < 128 -> PromptSizeCategory.SMALL
        tokens < 256 -> PromptSizeCategory.MEDIUM
        tokens <= budget.maximumEstimatedTokens -> PromptSizeCategory.LARGE
        else -> PromptSizeCategory.OVER_BUDGET
    }

    fun build(
        feature: CoreFeatureId,
        question: String,
        explicitContext: List<Pair<String, String>>,
        evidence: List<EvidenceItem>,
        budget: AynvoraAiContextBudget = AynvoraAiContextBudget.Default,
    ): AynvoraBoundedPrompt {
        val unique = evidence.sortedByDescending { it.priority }
            .distinctBy { it.ruleId ?: it.evidenceId }
            .filter { it.domain == feature }
            .take(budget.maxEvidenceItems)

        val system = bounded(
            "Answer using only supplied evidence. Distinguish source text from reflection. " +
                "Do not invent citations, predict certainty, or give medical or financial advice.",
            budget.systemTokens,
        )
        val featureRules = when (feature) {
            CoreFeatureId.GITA -> "FEATURE=GITA\nLabel scripture as source and any application as reflection."
            else -> "FEATURE=${feature.name}"
        }
        val rules = bounded(featureRules, budget.featureRulesTokens)
        val sourceLines = mutableListOf<String>()
        val evidenceLines = mutableListOf<String>()
        var evidenceUsed = 0
        var sourceUsed = 0
        for (item in unique) {
            val ref = item.provenance.referenceId ?: item.ruleId ?: item.evidenceId
            val metadata = "$ref:${item.provenance.sourceName}"
            val metadataRoom = (budget.sourceMetadataTokens * 4 - sourceUsed).coerceAtLeast(0)
            val compactMetadata = truncate(metadata, metadataRoom)
            if (compactMetadata.isNotBlank()) {
                sourceLines += "SOURCE=$compactMetadata"
                sourceUsed += compactMetadata.encodeToByteArray().size
            }
            val evidenceRoom = (budget.evidenceTokens * 4 - evidenceUsed).coerceAtLeast(0)
            if (evidenceRoom == 0) break
            val line = "VERSE=${ref}\nSCRIPTURE=${item.summary}"
            val compact = truncate(line, evidenceRoom)
            if (compact.isNotBlank()) {
                evidenceLines += compact
                evidenceUsed += compact.encodeToByteArray().size
            }
        }
        val contextText = explicitContext
            .filter { it.second.isNotBlank() }
            .joinToString("\n") { (key, value) -> "$key=${value}" }
        val context = bounded(contextText, budget.userContextTokens)
        val boundedQuestion = bounded(question, budget.questionTokens)
        val user = buildString {
            append(rules)
            if (evidenceLines.isNotEmpty()) append("\nEVIDENCE\n").append(evidenceLines.joinToString("\n"))
            if (sourceLines.isNotEmpty()) append("\nSOURCES\n").append(sourceLines.joinToString("\n"))
            if (context.isNotBlank()) append("\nUSER_CONTEXT=").append(context)
            append("\nQUESTION=").append(boundedQuestion)
        }
        val allEvidence = evidence.joinToString("\n") { it.summary }
        val sourceText = sourceLines.joinToString("\n")
        val sections = mapOf(
            "system" to estimate(system),
            "feature_rules" to estimate(rules),
            "evidence" to estimate(evidenceLines.joinToString("\n")),
            "source_metadata" to estimate(sourceText),
            "user_context" to estimate(context),
            "question" to estimate(boundedQuestion),
        )
        val promptBytes = (system + user).encodeToByteArray().size
        return AynvoraBoundedPrompt(system, user, AynvoraPromptDiagnostics(
            evidenceCount = evidence.size,
            uniqueEvidenceCount = unique.size,
            evidenceTextBytes = allEvidence.encodeToByteArray().size,
            evidenceEstimatedTokens = estimate(allEvidence),
            sourceMetadataBytes = sourceText.encodeToByteArray().size,
            systemInstructionBytes = system.encodeToByteArray().size,
            userContextBytes = context.encodeToByteArray().size,
            questionBytes = boundedQuestion.encodeToByteArray().size,
            promptBytes = promptBytes,
            estimatedPromptTokens = estimate(system + user),
            sizeCategory = classifyEstimatedTokens(estimate(system + user), budget),
            sectionEstimatedTokens = sections,
        ))
    }

    private fun estimate(text: String): Int = (text.encodeToByteArray().size + 3) / 4

    private fun bounded(value: String, maxTokens: Int): String = truncate(value.trim(), maxTokens * 4)

    private fun truncate(value: String, maxBytes: Int): String {
        if (maxBytes <= 0) return ""
        if (value.encodeToByteArray().size <= maxBytes) return value
        var end = minOf(value.length, maxBytes)
        while (end > 0 && value.substring(0, end).encodeToByteArray().size > maxBytes) end--
        val space = value.lastIndexOf(' ', end - 1)
        if (space > 0) end = space
        return value.substring(0, end).trimEnd()
    }
}

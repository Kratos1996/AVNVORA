package com.aynvora.core.ai

import com.aynvora.core.astrology.knowledge.AstroEvidenceBundle
import com.aynvora.core.astrology.knowledge.AstroEvidenceKind
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem

/**
 * Structured Grounded Prompt Builder conforming to Phase 10.26 Section 12.
 *
 * Clearly separates:
 * - CALCULATION FACTS
 * - KNOWLEDGE RULES
 * - SOURCE METADATA
 * - USER QUESTION
 * - RESPONSE INSTRUCTIONS
 *
 * Invariant: Astronomical values are calculated strictly by the deterministic engines,
 * never by the generative model.
 */
object PromptBuilder {

    data class GroundedPrompt(
        val systemPrompt: String,
        val userPrompt: String,
        val fullText: String,
        val calculationFactCount: Int,
        val knowledgeRuleCount: Int,
        val sourceCount: Int,
    )

    fun build(
        question: String,
        evidence: AstroEvidenceBundle,
        instructions: String = "Explain the reflection strictly using the calculation facts and knowledge rules above. Do not recalculate or alter astronomical positions, signs, or lords. If source details are unavailable, state 'source unavailable'.",
    ): GroundedPrompt {
        val calcFacts = mutableListOf<String>()
        val rules = mutableListOf<String>()
        val sources = mutableListOf<String>()

        evidence.items.forEach { item ->
            when (item.kind) {
                AstroEvidenceKind.DETERMINISTIC_CALCULATION -> {
                    calcFacts.add(item.text)
                }
                AstroEvidenceKind.KNOWLEDGE_RULE -> {
                    rules.add(item.text)
                    val src = item.metadata["sourceTitle"] ?: item.sourceId
                    if (!src.isNullOrBlank()) {
                        sources.add(src)
                    }
                }
                AstroEvidenceKind.LOCAL_KNOWLEDGE -> {
                    rules.add(item.text)
                    val src = item.metadata["sourceTitle"] ?: item.sourceId
                    if (!src.isNullOrBlank()) {
                        sources.add(src)
                    }
                }
                AstroEvidenceKind.REMOTE_WEB -> {
                    // Handled if present
                }
            }
        }

        return buildFromComponents(
            calculationFacts = calcFacts,
            knowledgeRules = rules,
            sources = sources.distinct(),
            userQuestion = question,
            responseInstructions = instructions,
        )
    }

    fun buildFromEvidenceItems(
        question: String,
        evidence: List<EvidenceItem>,
        instructions: String = "Explain the reflection strictly using the calculation facts and knowledge rules above. Do not recalculate or alter astronomical positions, signs, or lords. If source details are unavailable, state 'source unavailable'.",
    ): GroundedPrompt {
        val calcFacts = mutableListOf<String>()
        val rules = mutableListOf<String>()
        val sources = mutableListOf<String>()

        evidence.forEach { item ->
            when (item.category) {
                EvidenceCategory.FACT,
                EvidenceCategory.DERIVED_FACT -> {
                    calcFacts.add(item.summary)
                }
                EvidenceCategory.TRADITIONAL_RULE -> {
                    rules.add(item.summary)
                }
                EvidenceCategory.INTERPRETATION,
                EvidenceCategory.USER_CONTEXT -> {
                    rules.add(item.summary)
                }
            }
            val src = item.provenance.sourceName
            if (src.isNotBlank()) {
                val ref = item.provenance.referenceId
                val fullSrc = if (!ref.isNullOrBlank()) "$src ($ref)" else src
                sources.add(fullSrc)
            }
        }

        return buildFromComponents(
            calculationFacts = calcFacts,
            knowledgeRules = rules,
            sources = sources.distinct(),
            userQuestion = question,
            responseInstructions = instructions,
        )
    }

    fun buildFromComponents(
        calculationFacts: List<String>,
        knowledgeRules: List<String>,
        sources: List<String>,
        userQuestion: String,
        responseInstructions: String,
    ): GroundedPrompt {
        val systemPrompt = "You are AYNVORA On-Device Classical Astrology Intelligence. Answer solely using the provided facts and rules. Never recalculate or guess astronomical values."

        val userPrompt = buildString {
            append("CALCULATION FACTS:\n")
            if (calculationFacts.isEmpty()) {
                append("None\n")
            } else {
                calculationFacts.forEach { append("- ").append(it).append("\n") }
            }

            append("\nKNOWLEDGE RULES:\n")
            if (knowledgeRules.isEmpty()) {
                append("None\n")
            } else {
                knowledgeRules.forEach { append("- ").append(it).append("\n") }
            }

            append("\nSOURCE METADATA:\n")
            if (sources.isEmpty()) {
                append("Source unavailable\n")
            } else {
                sources.forEach { append("- ").append(it).append("\n") }
            }

            append("\nUSER QUESTION:\n")
            append(userQuestion.trim()).append("\n")

            append("\nRESPONSE INSTRUCTIONS:\n")
            append(responseInstructions.trim())
        }

        val fullText = "$systemPrompt\n\n$userPrompt"

        return GroundedPrompt(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            fullText = fullText,
            calculationFactCount = calculationFacts.size,
            knowledgeRuleCount = knowledgeRules.size,
            sourceCount = sources.size,
        )
    }
}

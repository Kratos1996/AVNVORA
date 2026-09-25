package com.aynvora.core.ai

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotReading
import kotlinx.serialization.Serializable

/**
 * Data privacy classification governing which data may touch the AI layer.
 */
@Serializable
enum class AiPrivacyClass {
    /** Public domain scriptures, card titles, classical aphorisms. */
    PUBLIC,

    /** Generic non-identifying application state. */
    NON_SENSITIVE,

    /** User-specific readings, selected spread, birth rashi. */
    PERSONAL,

    /** Exact birth timestamp, latitude/longitude, user personal notes. */
    SENSITIVE,

    /**
     * STRICT_LOCAL_ONLY: Must NEVER leave the physical device memory.
     * Guaranteed zero-telemetry, zero-cloud, and stripped of telemetry listeners.
     */
    STRICT_LOCAL_ONLY,
}

/**
 * Structured evidence item bound to verified domain provenance.
 */
@Serializable
data class AiEvidence(
    val evidenceId: String,
    val featureId: CoreFeatureId,
    val category: EvidenceCategory,
    val confidence: Float = 1.0f,
    val summaryText: String,
    val structuredPayloadJson: String? = null,
    val provenance: EvidenceProvenance,
    val disclaimers: List<String> = emptyList(),
    val privacyClass: AiPrivacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
)

/**
 * Validated context passed to on-device AI synthesis.
 */
@Serializable
data class AiContext(
    val featureId: CoreFeatureId,
    val evidenceItems: List<AiEvidence>,
    val privacyClass: AiPrivacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
    val language: String = "en",
    val userQuery: String? = null,
)

/**
 * Domain connector contract mapping raw domain models to verified [AiEvidence].
 * The AI assistant interacts ONLY with this connector output, never touching raw DAOs or databases.
 */
interface AiFeatureDataConnector<in T> {
    val featureId: CoreFeatureId
    fun extractEvidence(input: T): List<AiEvidence>
}

/**
 * Tarot domain connector converting verified tarot card draws to structured evidence.
 */
class TarotFeatureDataConnector : AiFeatureDataConnector<TarotReading> {
    override val featureId: CoreFeatureId = CoreFeatureId.TAROT

    override fun extractEvidence(input: TarotReading): List<AiEvidence> {
        val evidenceList = mutableListOf<AiEvidence>()

        input.draws.forEachIndexed { index, drawnCard ->
            val pos = drawnCard.position
            val card = drawnCard.card
            val isReversed = drawnCard.orientation == TarotCardOrientation.REVERSED
            val orientation = if (isReversed) "Reversed" else "Upright"

            evidenceList.add(
                AiEvidence(
                    evidenceId = "tarot_draw_${input.id}_pos_$index",
                    featureId = CoreFeatureId.TAROT,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Position ${pos.orderIndex + 1} (${pos.name}): ${card.name} [$orientation] - Arcana: ${card.arcana.name}, Suit: ${card.suit?.name ?: "None"}.",
                    structuredPayloadJson = """{"cardId":"${card.id}","name":"${card.name}","isReversed":$isReversed,"position":"${pos.name}"}""",
                    provenance = EvidenceProvenance(
                        domain = CoreFeatureId.TAROT,
                        sourceName = "AYNVORA Tarot Engine",
                        rulesetOrEdition = "RWS_STANDARD_78",
                        engineVersion = "1.0.0",
                        timestampEpochMs = input.timestampEpochMs,
                        locale = "en",
                    ),
                    disclaimers = listOf("Tarot readings are symbolic reflections for personal contemplation, not deterministic prophecies."),
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        return evidenceList
    }
}

/**
 * Formats structured AI context into strict, hallucination-resistant prompts (ChatML).
 */
object AiPromptTemplate {

    fun buildSystemPrompt(context: AiContext): String {
        val isHindi = context.language.lowercase().startsWith("hi")
        return if (isHindi) {
            """
            आप AYNVORA के ऑन-डिवाइस ज्ञान सहायक हैं।
            नियम:
            1. आप केवल नीचे दिए गए आधिकारिक साक्ष्यों के आधार पर चिंतन और सारांश प्रस्तुत करेंगे।
            2. कोई भी मनगढ़ंत या असत्यापित भविष्यवाणी न करें।
            3. कभी भी पूर्ण भाग्यवादी, चिकित्सीय या वित्तीय सलाह न दें।
            4. केवल साक्ष्य में दी गई जानकारी पर आधारित सौम्य, ज्ञानवर्धक व्याख्या दें।
            """.trimIndent()
        } else {
            """
            You are the on-device contemplative intelligence engine of AYNVORA.
            Strict Rules:
            1. You explain and synthesize ONLY the verified evidence provided below.
            2. Never contradict or fabricate planetary positions, scripture verses, or card draws.
            3. Never provide medical, financial, legal, or deterministic absolute fatalistic claims.
            4. Offer reflective, balanced wisdom grounded strictly in the provided facts.
            """.trimIndent()
        }
    }

    fun buildUserPrompt(context: AiContext): String {
        return buildString {
            if (!context.userQuery.isNullOrBlank()) {
                append("User Question: ${context.userQuery}\n\n")
            }
            append("Grounded Evidence Context:\n")
            context.evidenceItems.forEachIndexed { i, ev ->
                append("${i + 1}. [${ev.category}] ${ev.summaryText}\n")
            }
            append("\nPlease synthesize a reflective, insightful interpretation based strictly on these items.")
        }
    }
}

/**
 * Validates generated SLM output against evidence rules and safety policies.
 */
class AiOutputValidator {

    fun validateOutput(rawText: String, context: AiContext): AynvoraResult<String> {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return AynvoraResult.Failure.InternalFailure("AI generation produced empty content")
        }

        val lower = trimmed.lowercase()

        // 1. Fatalistic predictions
        val fatalisticKeywords = listOf(
            "will die", "you will die", "death is certain", "death is guaranteed",
            "accident is certain", "will get cancer",
            "निधन निश्चित है", "मृत्यु होगी", "मौत निश्चित",
        )
        for (badWord in fatalisticKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting fatalistic future claims: '$badWord'"
                )
            }
        }

        // 2. Medical claims
        val medicalKeywords = listOf(
            "will cure", "cures cancer", "stop taking medicine", "stop your medication",
            "cure any disease", "बीमारी ठीक हो जाएगी", "दवा बंद कर दें", "इलाज की जरूरत नहीं",
        )
        for (badWord in medicalKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting medical claims: '$badWord'"
                )
            }
        }

        // 3. Financial guarantees
        val financialKeywords = listOf(
            "guaranteed wealth", "surely win lottery", "100% profit guaranteed",
            "guaranteed jackpot", "धन लाभ निश्चित है", "लॉटरी जीतेंगे", "करोड़पति बनना तय",
        )
        for (badWord in financialKeywords) {
            if (lower.contains(badWord)) {
                return AynvoraResult.Failure.InternalFailure(
                    "Output violated safety policy by asserting financial guarantees: '$badWord'"
                )
            }
        }

        return AynvoraResult.Success(trimmed)
    }
}


package com.aynvora.core.astrology.knowledge

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AynvoraAiOutputValidator
import com.aynvora.core.ai.AynvoraAiRequest
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.ai.AynvoraResponseMode
import com.aynvora.core.ai.AynvoraUserContext
import com.aynvora.core.ai.OutputValidationResult
import com.aynvora.core.ai.AynvoraValidationStatus
import com.aynvora.core.astrology.prediction.KnowledgeChunk
import com.aynvora.core.astrology.prediction.KnowledgeRule
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AstroFunctionCall(val tool: String, val arguments: JsonObject)

@Serializable
data class AstroRegisteredToolResult(val toolId: String, val resultJson: String, val evidence: List<AstroEvidenceItem> = emptyList(), val status: String = "AVAILABLE")

@Serializable
data class AstroGroundedAiResult(val functionCall: AstroFunctionCall, val toolResult: AstroRegisteredToolResult, val evidence: AstroEvidenceBundle, val answer: AynvoraAiResponse, val nativeVerified: Boolean, val fallbackUsed: Boolean)

fun interface AstroFunctionCallPlanner {
    suspend fun selectTool(question: String, context: AstroPageContext, availableTools: List<AstroToolDescriptor>): AstroFunctionCall
}
fun interface AstroRegisteredTool { suspend fun execute(arguments: JsonObject, context: AstroPageContext): AstroRegisteredToolResult }
fun interface AstroGroundedResponseGenerator { suspend fun generate(question: String, context: AstroPageContext, evidence: AstroEvidenceBundle, locale: String, mode: AstroAnswerMode, requestTimestampEpochMs: Long): AynvoraResult<AynvoraAiResponse> }

/** Model-facing tool-call decoder. The only output accepted is one JSON object for a registered available tool. */
object AstroFunctionCallParser {
    fun parse(response: String): AstroFunctionCall {
        val root = Json.parseToJsonElement(response.trim()).jsonObject
        require(root.keys == setOf("tool", "arguments")) { "Tool call must contain exactly tool and arguments." }
        val name = root["tool"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank) ?: error("Tool name is required.")
        val arguments = root["arguments"]?.jsonObject ?: error("Tool arguments must be a JSON object.")
        return AstroFunctionCall(name, arguments)
    }
}

/** Uses the same loaded local inference engine for constrained tool planning; it cannot execute generated code. */
class InferenceBackedAstroToolPlanner(private val inference: AiInferenceEngine) : AstroFunctionCallPlanner {
    override suspend fun selectTool(question: String, context: AstroPageContext, availableTools: List<AstroToolDescriptor>): AstroFunctionCall {
        require(availableTools.isNotEmpty())
        val toolsJson = Json.encodeToString(availableTools.filter { it.status == AstroToolStatus.AVAILABLE })
        val response = inference.generate(AiGenerationRequest(
            requestId = "tool-plan-${context.pageId}",
            systemPrompt = "Select exactly one registered tool for the request. Return strict JSON only: {\"tool\":\"toolId\",\"arguments\":{}}. Never calculate values. Never invent tool IDs.",
            userPrompt = "Page: ${context.pageId}; feature: ${context.featureId}; tradition: ${context.traditionId ?: "unspecified"}; question: $question\nTools: $toolsJson",
            temperature = 0.0f,
            maxTokens = 160,
            language = "en",
        ))
        val generation = (response as? AynvoraResult.Success)?.value ?: error("Local tool planning inference failed.")
        return AstroFunctionCallParser.parse(generation.text)
    }
}

/** Adapts grounded evidence to the existing local-intelligence prompt and validator path. */
class LocalIntelligenceGroundedResponseGenerator(
    private val intelligence: AynvoraLocalIntelligence,
    private val packId: String = "astro-grounded-v1",
    private val packVersion: String = "1",
) : AstroGroundedResponseGenerator {
    override suspend fun generate(question: String, context: AstroPageContext, evidence: AstroEvidenceBundle, locale: String, mode: AstroAnswerMode, requestTimestampEpochMs: Long): AynvoraResult<AynvoraAiResponse> {
        val evidenceItems = evidence.items.map { item ->
            val category = when (item.kind) {
                AstroEvidenceKind.DETERMINISTIC_CALCULATION -> EvidenceCategory.DERIVED_FACT
                AstroEvidenceKind.KNOWLEDGE_RULE -> EvidenceCategory.TRADITIONAL_RULE
                AstroEvidenceKind.LOCAL_KNOWLEDGE, AstroEvidenceKind.REMOTE_WEB -> EvidenceCategory.FACT
            }
            EvidenceItem(
                evidenceId = item.evidenceId,
                domain = CoreFeatureId.ASTROLOGY,
                category = category,
                ruleId = item.evidenceId.takeIf { item.kind == AstroEvidenceKind.KNOWLEDGE_RULE },
                summary = item.text,
                rawPayloadJson = Json.encodeToString(item),
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.ASTROLOGY,
                    sourceName = item.metadata["sourceTitle"] ?: item.metadata["domain"] ?: item.sourceId ?: item.kind.name,
                    rulesetOrEdition = item.metadata["sourceCommit"] ?: item.metadata["version"] ?: item.metadata["quality"] ?: packVersion,
                    engineVersion = "aynvora-astro-grounding-1",
                    timestampEpochMs = item.metadata["retrievedAt"]?.toLongOrNull() ?: requestTimestampEpochMs,
                    locale = locale,
                    referenceId = item.sourceRef,
                    contentVersion = item.metadata["version"] ?: item.metadata["packVersion"] ?: packVersion,
                    contentType = item.kind.name,
                ),
            )
        }
        val request = AynvoraAiRequest(
            requestId = "astro-ask-${context.pageId}-${question.hashCode()}",
            featureId = CoreFeatureId.ASTROLOGY,
            knowledgePackId = packId,
            rulesetId = evidence.packVersions.values.firstOrNull(),
            evidence = evidenceItems,
            userContext = AynvoraUserContext(question = question),
            question = "$question\n\nRequested answer mode: ${mode.name}. Keep every statement within the supplied evidence.",
            maxTokens = when (mode) { AstroAnswerMode.SHORT -> 100; AstroAnswerMode.NORMAL -> 240; AstroAnswerMode.DETAILED -> 400; AstroAnswerMode.DEEP -> 512 },
            locale = locale,
            responseMode = AynvoraResponseMode.DIRECT_EXPLANATION,
        )
        return intelligence.synthesize(request)
    }
}

/**
 * Executes one registered tool, fuses its result with local rules/chunks, then sends that exact evidence
 * to the current native/local AI pipeline. Remote search is excluded unless the caller explicitly opts in.
 */
class AynvoraAiToolExecutor(
    private val planner: AstroFunctionCallPlanner,
    private val tools: Map<String, AstroRegisteredTool>,
    private val responseGenerator: AstroGroundedResponseGenerator,
    private val sources: AstroKnowledgeSourceRegistry,
    private val chunks: List<KnowledgeChunk> = emptyList(),
    private val rules: List<KnowledgeRule> = emptyList(),
    private val webProvider: RemoteWebSearchProvider? = null,
) {
    init { require(tools.keys.all { id -> AstroToolRegistry.all(true).any { it.toolId == id && it.status == AstroToolStatus.AVAILABLE } }) { "Only registered available tools can be executable." } }

    suspend fun ask(question: String, context: AstroPageContext, locale: String = "en", answerMode: AstroAnswerMode = AstroAnswerMode.NORMAL, includeWeb: Boolean = false, requestTimestampEpochMs: Long): AynvoraResult<AstroGroundedAiResult> {
        if (question.isBlank()) return AynvoraResult.Failure.InvalidInput("question", "Question must not be blank.")
        val descriptors = AstroToolRegistry.all(includeWeb).filter { it.status == AstroToolStatus.AVAILABLE && (it.toolId in tools || it.toolId == "searchWeb" && includeWeb && webProvider != null) }
        val call = runCatching { planner.selectTool(question, context, descriptors) }.getOrElse { return AynvoraResult.Failure.CalculationFailure("TOOL_PLAN_FAILED", it.message ?: "Tool planning failed.") }
        if (call.tool !in descriptors.map { it.toolId }) return AynvoraResult.Failure.UnsupportedConfiguration("Model requested an unavailable or unregistered tool: ${call.tool}")
        val toolResult = if (call.tool == "searchWeb") {
            val query = call.arguments["query"]?.jsonPrimitive?.content ?: question
            val web = runCatching { webProvider!!.search(stripPrivateData(query), requestTimestampEpochMs, context.traditionId) }.getOrElse { return AynvoraResult.Failure.CalculationFailure("WEB_SEARCH_FAILED", it.message ?: "Remote web search failed.") }
            AstroRegisteredToolResult("searchWeb", Json.encodeToString(web), status = "REMOTE_WEB")
        } else runCatching { tools.getValue(call.tool).execute(call.arguments, context) }.getOrElse { return AynvoraResult.Failure.CalculationFailure("TOOL_EXECUTION_FAILED", it.message ?: "Registered tool failed.") }
        val deterministic = context.evidence + toolResult.evidence
        val webEvidence = if (call.tool == "searchWeb") runCatching { Json.decodeFromString<List<WebEvidence>>(toolResult.resultJson) }.getOrDefault(emptyList()) else emptyList()
        val fused = KnowledgeRetriever(sources).retrieve(question, chunks, rules, deterministic = deterministic, web = webEvidence)
        val generated = responseGenerator.generate(question, context, fused, locale, answerMode, requestTimestampEpochMs)
        if (generated !is AynvoraResult.Success) return generated.asFailure()
        val answer = generated.value
        if (answer.validationStatus != AynvoraValidationStatus.VALIDATED) return AynvoraResult.Failure.CalculationFailure("AI_VALIDATION_FAILED", "Grounded answer did not pass output validation.")
        return AynvoraResult.Success(AstroGroundedAiResult(call, toolResult, fused, answer, answer.executionMode.isNative && !answer.fallbackUsed, answer.fallbackUsed))
    }

    private fun stripPrivateData(query: String): String = query
        .replace(Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE), "[email]")
        .replace(Regex("\\+?[0-9][0-9 ()-]{7,}[0-9]"), "[phone]")
        .replace(Regex("\\b(?:19|20)\\d{2}[-/.](?:0?[1-9]|1[0-2])[-/.](?:0?[1-9]|[12]\\d|3[01])\\b"), "[date]")
        .replace(Regex("\\b\\d{1,2}\\s+[A-Z][a-z]{2,8}\\s+(?:19|20)\\d{2}\\b"), "[date]")
        .replace(Regex("\\b(?:at\\s+)?(?:[01]?\\d|2[0-3]):[0-5]\\d(?:\\s*[ap]m)?\\b", RegexOption.IGNORE_CASE), "[time]")
        .replace(Regex("\\b-?\\d{1,3}\\.\\d{3,}\\s*,\\s*-?\\d{1,3}\\.\\d{3,}\\b"), "[coordinates]")
        .replace(Regex("\\b\\d{1,6}\\s+(?:[A-Z0-9.'-]+\\s+){1,5}(?:street|st|road|rd|avenue|ave|lane|ln|drive|dr|boulevard|blvd)\\b", RegexOption.IGNORE_CASE), "[address]")
        .replace(Regex("\\b(?:flat|apartment|apt|house|door)\\s*(?:no\\.?\\s*)?\\d+[A-Z]?\\b", RegexOption.IGNORE_CASE), "[address]")
        .take(500)

    private fun <T> AynvoraResult<T>.asFailure(): AynvoraResult.Failure = (this as? AynvoraResult.Failure) ?: AynvoraResult.Failure.CalculationFailure("AI_INFERENCE_FAILED", "Local AI generation failed.")
}

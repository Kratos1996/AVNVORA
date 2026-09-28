package com.aynvora.core.ai

import com.aynvora.core.ai.knowledge.AynvoraKnowledgePack
import com.aynvora.core.ai.knowledge.GitaKnowledgePack
import com.aynvora.core.ai.knowledge.VedicAstrologyKnowledgePack
import com.aynvora.core.ai.knowledge.TarotKnowledgePack
import com.aynvora.core.ai.knowledge.NumerologyKnowledgePack
import com.aynvora.core.ai.knowledge.PalmistryKnowledgePack
import com.aynvora.core.ai.knowledge.GemstoneKnowledgePack
import com.aynvora.core.ai.knowledge.GarudaPuranKnowledgePack
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Low-level execution driver backing [AynvoraLocalIntelligence].
 */
interface LocalAiRuntime {
    val executionMode: AiExecutionMode
    fun isAvailable(): Boolean
    fun isNativeVerified(): Boolean
    fun getNativeLibraryStatus(): String = "NOT_VERIFIED"
    fun getJniStatus(): String = "UNLINKED"
    suspend fun loadModel(variant: AiModelVariant, filePath: String): AynvoraResult<Unit>
    suspend fun unloadModel(): AynvoraResult<Unit>
    suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse>
    suspend fun cancel(requestId: String): Boolean
    fun getStatus(): AiInferenceStatus
    fun getLoadedModel(): AiModelVariant?
    fun getDiagnostics(): AiInferenceDiagnostics
}

/**
 * Production implementation of [LocalAiRuntime] wrapping [AiInferenceEngine].
 */
class DefaultLocalAiRuntime(
    private val inferenceEngine: AiInferenceEngine = LocalNativeInferenceEngine(),
    private val nativeRuntimeDriver: NativeAiRuntime? = LlamaNativeRuntimeDriver(),
) : LocalAiRuntime {

    override val executionMode: AiExecutionMode
        get() {
            // CRITICAL STATUS RULE (Phase 10.12):
            // Can ONLY be LOCAL_NATIVE if native library is verified AND model is actually loaded in native engine AND real tokens were generated
            if (isNativeVerified() &&
                inferenceEngine.getStatus() == AiInferenceStatus.READY &&
                inferenceEngine.getLoadedModel() != null &&
                inferenceEngine.hasGeneratedTokens()
            ) {
                return AiExecutionMode.LOCAL_NATIVE
            }
            if (inferenceEngine is LocalSimulationEngine && inferenceEngine.getStatus() == AiInferenceStatus.READY && inferenceEngine.getLoadedModel() != null) {
                return AiExecutionMode.LOCAL_SIMULATION
            }
            return AiExecutionMode.DETERMINISTIC_FALLBACK
        }

    override fun isAvailable(): Boolean {
        return nativeRuntimeDriver?.isAvailable() ?: (inferenceEngine.getStatus() != AiInferenceStatus.ERROR)
    }

    override fun isNativeVerified(): Boolean {
        // Truthful native status: only verified if native bridge is packaged and functioning
        return nativeRuntimeDriver?.isAvailable() == true
    }

    override fun getNativeLibraryStatus(): String =
        nativeRuntimeDriver?.getNativeLibraryStatus() ?: "NOT_VERIFIED (libllama.so pending build packaging)"

    override fun getJniStatus(): String =
        nativeRuntimeDriver?.getJniStatus() ?: "UNLINKED"

    override suspend fun loadModel(variant: AiModelVariant, filePath: String): AynvoraResult<Unit> {
        return inferenceEngine.load(variant, filePath)
    }

    override suspend fun unloadModel(): AynvoraResult<Unit> {
        return inferenceEngine.unload()
    }

    override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
        return inferenceEngine.generate(request)
    }

    override suspend fun cancel(requestId: String): Boolean {
        return inferenceEngine.cancel(requestId)
    }

    override fun getStatus(): AiInferenceStatus = inferenceEngine.getStatus()

    override fun getLoadedModel(): AiModelVariant? = inferenceEngine.getLoadedModel()

    override fun getDiagnostics(): AiInferenceDiagnostics = inferenceEngine.getDiagnostics()
}

/**
 * Single Unified On-Device Intelligence Platform for AYNVORA.
 *
 * Implements Step 2 of the unified architecture:
 * - Centralizes model lifecycle, loading, availability, inference, streaming, cancellation,
 *   timeout, memory protection, language capability, execution mode, fallback, and diagnostics.
 * - Feature code MUST NOT create its own model or runtime instance.
 * - Domain intelligence is supplied through structured [AynvoraKnowledgePack]s and [EvidenceGraph].
 */
interface AynvoraLocalIntelligence {
    val lifecycleManager: AiModelLifecycleManager?
    val runtime: LocalAiRuntime

    suspend fun initialize(): AynvoraResult<Unit>
    suspend fun synthesize(request: AynvoraAiRequest): AynvoraResult<AynvoraAiResponse>
    suspend fun loadModel(): AynvoraResult<Unit>
    suspend fun unloadModel(): AynvoraResult<Unit>
    suspend fun cancel(requestId: String): Boolean

    fun getExecutionMode(): AiExecutionMode
    fun getInstalledModel(): AiModelVariant?
    fun isModelLoaded(): Boolean
    fun isNativeVerified(): Boolean
    fun getNativeLibraryStatus(): String = runtime.getNativeLibraryStatus()
    fun getJniStatus(): String = runtime.getJniStatus()
    fun getDiagnostics(): AiInferenceDiagnostics
    fun getKnowledgePack(featureId: CoreFeatureId): AynvoraKnowledgePack?
    fun registerKnowledgePack(pack: AynvoraKnowledgePack)
    fun getAllKnowledgePacks(): List<AynvoraKnowledgePack>
}

/**
 * Canonical implementation of [AynvoraLocalIntelligence].
 */
class DefaultAynvoraLocalIntelligence(
    override val runtime: LocalAiRuntime = DefaultLocalAiRuntime(),
    override val lifecycleManager: AiModelLifecycleManager? = null,
    private val outputValidator: AynvoraAiOutputValidator = AynvoraAiOutputValidator(),
    initialKnowledgePacks: List<AynvoraKnowledgePack> = emptyList(),
) : AynvoraLocalIntelligence {

    private val mutex = Mutex()
    private val knowledgePacks = mutableMapOf<CoreFeatureId, AynvoraKnowledgePack>()
    private var lastInferenceDurationMs: Long = 0L

    init {
        // Register default feature knowledge packs
        registerKnowledgePack(VedicAstrologyKnowledgePack())
        registerKnowledgePack(GitaKnowledgePack())
        registerKnowledgePack(TarotKnowledgePack())
        registerKnowledgePack(NumerologyKnowledgePack())
        registerKnowledgePack(PalmistryKnowledgePack())
        registerKnowledgePack(GemstoneKnowledgePack())
        registerKnowledgePack(GarudaPuranKnowledgePack())

        initialKnowledgePacks.forEach { registerKnowledgePack(it) }
    }

    override fun registerKnowledgePack(pack: AynvoraKnowledgePack) {
        knowledgePacks[pack.featureId] = pack
    }

    override fun getKnowledgePack(featureId: CoreFeatureId): AynvoraKnowledgePack? {
        return knowledgePacks[featureId]
    }

    override fun getAllKnowledgePacks(): List<AynvoraKnowledgePack> {
        return knowledgePacks.values.toList()
    }

    override suspend fun initialize(): AynvoraResult<Unit> {
        lifecycleManager?.initialize()
        val installed = getInstalledModel()
        if (installed != null && runtime.getStatus() == AiInferenceStatus.UNLOADED) {
            val state = lifecycleManager?.state?.value
            if (state is AiModelLifecycleState.Ready) {
                runtime.loadModel(installed, state.localFilePath)
            }
        }
        return AynvoraResult.Success(Unit)
    }

    override suspend fun loadModel(): AynvoraResult<Unit> {
        val state = lifecycleManager?.state?.value
        return if (state is AiModelLifecycleState.Ready) {
            runtime.loadModel(state.installedVariant, state.localFilePath)
        } else {
            AynvoraResult.Failure.CalculationFailure(
                code = "MODEL_NOT_INSTALLED",
                message = "Cannot load AI model: model is not installed on this device",
            )
        }
    }

    override suspend fun unloadModel(): AynvoraResult<Unit> {
        return runtime.unloadModel()
    }

    override suspend fun cancel(requestId: String): Boolean {
        return runtime.cancel(requestId)
    }

    override fun getExecutionMode(): AiExecutionMode = runtime.executionMode

    override fun getInstalledModel(): AiModelVariant? {
        val state = lifecycleManager?.state?.value
        return if (state is AiModelLifecycleState.Ready) state.installedVariant else null
    }

    override fun isModelLoaded(): Boolean {
        return runtime.getStatus() == AiInferenceStatus.READY && runtime.getLoadedModel() != null
    }

    override fun isNativeVerified(): Boolean = runtime.isNativeVerified()

    override fun getNativeLibraryStatus(): String = runtime.getNativeLibraryStatus()

    override fun getJniStatus(): String = runtime.getJniStatus()

    override fun getDiagnostics(): AiInferenceDiagnostics = runtime.getDiagnostics()

    override suspend fun synthesize(request: AynvoraAiRequest): AynvoraResult<AynvoraAiResponse> {
        val startTime = System.currentTimeMillis()
        val loadedModel = runtime.getLoadedModel()
        val isRuntimeReady = runtime.getStatus() == AiInferenceStatus.READY && loadedModel != null

        // 1. Fallback condition checks: model not installed / not ready / unsupported locale
        if (!isRuntimeReady) {
            val fallbackReason = if (loadedModel == null) "MODEL_NOT_INSTALLED" else "RUNTIME_NOT_READY"
            return executeDeterministicFallback(request, fallbackReason, startTime)
        }

        val reqLocale = request.locale.lowercase().trim()
        val isLangSupported = loadedModel.supportedLanguages.any { reqLocale.startsWith(it) }
        if (!isLangSupported) {
            return executeDeterministicFallback(
                request = request,
                reason = "LOCALE_NOT_SUPPORTED_BY_MODEL_${loadedModel.modelId}",
                startTime = startTime,
            )
        }

        // 2. Build structured grounding prompt
        val pack = getKnowledgePack(request.featureId)
        val systemPrompt = buildSystemPrompt(request, pack, loadedModel)
        val userPrompt = buildUserPrompt(request, pack)

        val generationRequest = AiGenerationRequest(
            requestId = request.requestId,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            temperature = 0.6f,
            maxTokens = 512,
            language = request.locale,
            evidenceProvenance = request.evidence.map {
                AiProvenance(
                    sourceDomain = it.domain.name,
                    calculationRulesetOrEdition = it.provenance.rulesetOrEdition,
                    verifiedTimestampEpochMs = it.provenance.timestampEpochMs,
                    isRetrievedFact = true,
                )
            },
        )

        // 3. Execute generation on shared runtime
        val generationResult = runtime.generate(generationRequest)
        val endTime = System.currentTimeMillis()
        val latencyMs = endTime - startTime
        lastInferenceDurationMs = latencyMs

        when (generationResult) {
            is AynvoraResult.Success -> {
                val rawText = generationResult.value.text

                // 4. Output Safety & Schema Validation
                when (val validation = outputValidator.validate(rawText, request)) {
                    is OutputValidationResult.Valid -> {
                        val response = AynvoraAiResponse(
                            requestId = request.requestId,
                            responseText = validation.sanitizedText,
                            executionMode = runtime.executionMode,
                            featureId = request.featureId,
                            knowledgePackId = request.knowledgePackId,
                            sourceReferences = pack?.sourceReferences ?: emptyList(),
                            validationStatus = AynvoraValidationStatus.VALIDATED,
                            fallbackUsed = false,
                            fallbackReason = null,
                            latencyMs = latencyMs,
                            modelMetadata = "${loadedModel.modelId} (${loadedModel.name})",
                            provenance = generationRequest.evidenceProvenance,
                            reflectiveSynthesis = if (request.responseMode == AynvoraResponseMode.CROSS_FEATURE_REFLECTION) validation.sanitizedText else null,
                            domainSections = mapOf(request.featureId.name to validation.sanitizedText),
                        )
                        return AynvoraResult.Success(response)
                    }

                    is OutputValidationResult.Invalid -> {
                        // Validation failed -> safely delegate to deterministic fallback
                        return executeDeterministicFallback(
                            request = request,
                            reason = "VALIDATION_FAILED: ${validation.reason}",
                            startTime = startTime,
                            rejectedModel = loadedModel,
                        )
                    }
                }
            }

            is AynvoraResult.Failure -> {
                // Runtime generation failure or timeout -> fallback safely
                return executeDeterministicFallback(
                    request = request,
                    reason = "INFERENCE_FAILED: ${generationResult.message}",
                    startTime = startTime,
                    rejectedModel = loadedModel,
                )
            }
        }
    }

    private fun executeDeterministicFallback(
        request: AynvoraAiRequest,
        reason: String,
        startTime: Long,
        rejectedModel: AiModelVariant? = null,
    ): AynvoraResult<AynvoraAiResponse> {
        val pack = getKnowledgePack(request.featureId)
        val relevantEvidence = if (request.evidence.isNotEmpty()) {
            request.evidence
        } else {
            pack?.retrieveRelevantEvidence(request.question, request.userContext) ?: emptyList()
        }

        val relevantInterpretations = pack?.retrieveRelevantInterpretations(request.question) ?: emptyList()

        val fallbackText = buildString {
            if (request.responseMode == AynvoraResponseMode.CROSS_FEATURE_REFLECTION) {
                append("[CROSS-FEATURE REFLECTION: ${request.contributingDomains.joinToString(" + ") { it.name }}]\n\n")
            }

            if (relevantEvidence.isNotEmpty()) {
                append("Verified Evidence:\n")
                relevantEvidence.forEach { ev ->
                    append("• [${ev.domain.name}] ${ev.summary}\n")
                }
                append("\n")
            }

            if (relevantInterpretations.isNotEmpty()) {
                append("Traditional Guidance:\n")
                relevantInterpretations.forEach { interp ->
                    append("• ${interp.primarySymbolOrKey}: ${interp.verifiedMeaning}\n")
                }
                append("\n")
            }

            val question = request.question.ifBlank { request.userContext.question }
            if (question.isNotBlank()) {
                append("Contemplation for: \"$question\"\n")
                if (request.userContext.statedSituation != null) {
                    append("Stated situation: \"${request.userContext.statedSituation}\"\n")
                }
                append("Reflecting upon this classical wisdom, direct your attention to grounded action and discernment, preserving inner balance regardless of transient outcomes.")
            }
        }

        val latencyMs = System.currentTimeMillis() - startTime
        val modelMeta = if (rejectedModel != null) {
            "${rejectedModel.modelId} (Rejected/Failed: $reason)"
        } else {
            "Deterministic Rule Engine (Fallback: $reason)"
        }

        val response = AynvoraAiResponse(
            requestId = request.requestId,
            responseText = fallbackText,
            executionMode = AiExecutionMode.DETERMINISTIC_FALLBACK,
            featureId = request.featureId,
            knowledgePackId = request.knowledgePackId,
            sourceReferences = pack?.sourceReferences ?: emptyList(),
            validationStatus = AynvoraValidationStatus.FALLBACK_APPLIED,
            fallbackUsed = true,
            fallbackReason = reason,
            latencyMs = latencyMs,
            modelMetadata = modelMeta,
            provenance = relevantEvidence.map {
                AiProvenance(
                    sourceDomain = it.domain.name,
                    calculationRulesetOrEdition = it.provenance.rulesetOrEdition,
                    verifiedTimestampEpochMs = it.provenance.timestampEpochMs,
                    isRetrievedFact = true,
                )
            },
            reflectiveSynthesis = if (request.responseMode == AynvoraResponseMode.CROSS_FEATURE_REFLECTION) fallbackText else null,
            domainSections = mapOf(request.featureId.name to fallbackText),
        )

        return AynvoraResult.Success(response)
    }

    private fun buildSystemPrompt(
        request: AynvoraAiRequest,
        pack: AynvoraKnowledgePack?,
        model: AiModelVariant,
    ): String {
        return buildString {
            append("You are the AYNVORA Unified Local Intelligence engine running on-device ($model).\n")
            append("You are an explanation, synthesis, and reflection engine.\n")
            append("CRITICAL INVARIANTS:\n")
            append("- You are NOT a calculation authority, scripture authority, medical authority, or financial authority.\n")
            append("- Ground your synthesis strictly on the provided Evidence and Knowledge Pack below.\n")
            append("- Do not manufacture facts, unverified predictions, or scientific medical claims.\n")
            append("- Maintain a calm, respectful, contemplative, and empowering tone.\n")
            append("- Avoid fatalistic certainty. Connect scripture/tradition to the user's explicit question without judgment.\n")
            if (pack?.exclusions?.isNotEmpty() == true) {
                append("- Strict exclusions:\n")
                pack.exclusions.forEach { append("  * $it\n") }
            }
            if (request.responseMode == AynvoraResponseMode.CROSS_FEATURE_REFLECTION) {
                append("- CROSS-FEATURE REFLECTION: Clearly distinguish each separate tradition with explicit headings ([DOMAIN EVIDENCE], [TRADITIONAL SOURCE], [REFLECTIVE SYNTHESIS]). Never merge distinct traditions into one single authority.\n")
            }
        }
    }

    private fun buildUserPrompt(
        request: AynvoraAiRequest,
        pack: AynvoraKnowledgePack?,
    ): String {
        return buildString {
            append("FEATURE: ${request.featureId.name}\n")
            append("KNOWLEDGE PACK: ${request.knowledgePackId}\n\n")

            if (request.evidence.isNotEmpty()) {
                append("RETRIEVED EVIDENCE:\n")
                request.evidence.forEach { ev ->
                    append("- [${ev.domain.name}] (${ev.category}): ${ev.summary} (Source: ${ev.provenance.sourceName})\n")
                }
                append("\n")
            }

            val interpretations = pack?.retrieveRelevantInterpretations(request.question) ?: emptyList()
            if (interpretations.isNotEmpty()) {
                append("CANONICAL INTERPRETATION RECORDS:\n")
                interpretations.forEach { interp ->
                    append("- ${interp.primarySymbolOrKey}: ${interp.verifiedMeaning}\n")
                }
                append("\n")
            }

            append("USER CONTEXT:\n")
            append("Question: ${request.question}\n")
            if (request.userContext.statedSituation != null) {
                append("Stated Situation: ${request.userContext.statedSituation}\n")
            }
            if (request.userContext.statedGoal != null) {
                append("Stated Goal: ${request.userContext.statedGoal}\n")
            }
            if (request.userContext.selectedAreaOfReflection != null) {
                append("Area of Reflection: ${request.userContext.selectedAreaOfReflection}\n")
            }
            append("\nPlease provide a grounded, reflective synthesis connecting the verified evidence to the question.")
        }
    }
}

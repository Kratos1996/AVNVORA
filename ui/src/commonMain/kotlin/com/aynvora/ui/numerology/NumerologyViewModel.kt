package com.aynvora.ui.numerology

import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.event.AynvoraNavigationEffect
import com.aynvora.core.event.AynvoraNavigationTarget
import com.aynvora.core.numerology.NumerologyCalculationType
import com.aynvora.core.numerology.NumerologyHistoryEntry
import com.aynvora.core.numerology.NumerologyHistoryRepository
import com.aynvora.core.numerology.NumerologyRepository
import com.aynvora.core.numerology.NumerologyRequest
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.numerology.DeterministicNumerologyExplanationEngine
import com.aynvora.core.numerology.NumerologyAiQuestionCategory
import com.aynvora.core.numerology.NumerologyChatMessage
import com.aynvora.core.numerology.NumerologyConversationState
import com.aynvora.core.numerology.NumerologyExplanationEngine
import com.aynvora.core.numerology.NumerologyFeatureDataConnector
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.event.AynvoraEventType
import com.aynvora.core.event.AynvoraUiEvent
import com.aynvora.core.report.NumerologyReportGenerator
import com.aynvora.core.report.NumerologyReportInput
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.result.AynvoraResult
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel for the full Consumer Numerology experience.
 *
 * Implements strict decoupled architecture:
 * Composable -> NumerologyUiEvent -> Event SDK -> ViewModel -> Repository / Engine -> UiState.
 *
 * Zero formulas in UI.
 * Zero direct Composable -> repository calls.
 * Zero analytics PII leakage.
 */
class NumerologyViewModel(
    private val numerologyRepository: NumerologyRepository,
    private val historyRepository: NumerologyHistoryRepository,
    private val reportGenerator: NumerologyReportGenerator = NumerologyReportGenerator(),
    private val aiExplanationEngine: NumerologyExplanationEngine = DeterministicNumerologyExplanationEngine(),
    eventDispatcher: AynvoraEventDispatcher? = null,
) : AynvoraBaseViewModel<NumerologyUiEvent, NumerologyUiState, AynvoraEffect>(
    initialState = NumerologyUiState(),
    eventDispatcher = eventDispatcher,
) {
    private val internalDispatcher: AynvoraEventDispatcher? = eventDispatcher

    init {
        registerEventHandler(::handleEvent)
        loadHistory()
        // Execute initial calculation with default parameters
        viewModelScope.launch {
            calculateCurrent()
        }
    }

    private suspend fun handleEvent(event: NumerologyUiEvent) {
        when (event) {
            is NumerologyUiEvent.SelectTab -> {
                updateState { copy(activeTab = event.tab) }
            }

            is NumerologyUiEvent.SelectRuleset -> {
                val ruleset = NumerologyRuleset.fromId(event.rulesetId)
                    ?: NumerologyRuleset.CHALDEAN_CHEIRO_V1
                val defaultScript = when (ruleset.id) {
                    NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
                    NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> if (currentState.scriptText.isBlank()) "שלום" else currentState.scriptText

                    NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
                    NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> if (currentState.scriptText.isBlank()) "بسم الله" else currentState.scriptText

                    NumerologyRuleset.INDIAN_KATAPAYADI_V1.id -> if (currentState.scriptText.isBlank()) "गोपीभाग्यमधुव्रात" else currentState.scriptText
                    else -> currentState.scriptText
                }
                updateState {
                    copy(
                        selectedRuleset = ruleset,
                        scriptText = defaultScript,
                        validationErrorKey = null,
                        calculationError = null,
                    )
                }
                calculateCurrent()
            }

            is NumerologyUiEvent.UpdateDay -> updateState {
                copy(
                    day = event.day,
                    validationErrorKey = null
                )
            }

            is NumerologyUiEvent.UpdateMonth -> updateState {
                copy(
                    month = event.month,
                    validationErrorKey = null
                )
            }

            is NumerologyUiEvent.UpdateYear -> updateState {
                copy(
                    year = event.year,
                    validationErrorKey = null
                )
            }

            is NumerologyUiEvent.UpdateName -> updateState {
                copy(
                    name = event.name,
                    validationErrorKey = null
                )
            }

            is NumerologyUiEvent.UpdateScriptText -> updateState {
                copy(
                    scriptText = event.text,
                    validationErrorKey = null
                )
            }

            is NumerologyUiEvent.UpdateTargetYear -> updateState { copy(targetYear = event.year) }
            is NumerologyUiEvent.UpdateTargetMonth -> updateState { copy(targetMonth = event.month) }
            is NumerologyUiEvent.UpdateTime -> updateState {
                copy(
                    hour = event.hour,
                    minute = event.minute
                )
            }

            is NumerologyUiEvent.Calculate -> {
                calculateCurrent()
            }

            is NumerologyUiEvent.ResetForm -> {
                updateState {
                    copy(
                        day = "11",
                        month = "7",
                        year = "1996",
                        name = "ISHANT",
                        scriptText = "",
                        targetYear = "2026",
                        targetMonth = "9",
                        validationErrorKey = null,
                        calculationError = null,
                    )
                }
                calculateCurrent()
            }

            is NumerologyUiEvent.ToggleTrace -> {
                updateState {
                    copy(
                        showTrace = if (event.calculationType == null) !showTrace else true,
                        selectedTraceType = event.calculationType ?: selectedTraceType,
                    )
                }
            }

            is NumerologyUiEvent.ToggleSourceTransparency -> {
                updateState { copy(showSource = !showSource) }
            }

            is NumerologyUiEvent.ToggleCompareRuleset -> {
                val updated = if (event.isSelected) {
                    currentState.compareRulesetIds + event.rulesetId
                } else {
                    currentState.compareRulesetIds - event.rulesetId
                }
                updateState { copy(compareRulesetIds = updated) }
            }

            is NumerologyUiEvent.RunComparison -> {
                runComparison()
            }

            is NumerologyUiEvent.RequestReport -> {
                generateReport()
            }

            is NumerologyUiEvent.DismissReport -> {
                updateState { copy(generatedReport = null, reportError = null) }
            }

            is NumerologyUiEvent.OpenHistory -> {
                loadHistory()
                updateState { copy(activeTab = NumerologyTab.HISTORY) }
            }

            is NumerologyUiEvent.SelectHistoryEntry -> {
                val entry = currentState.historyEntries.firstOrNull { it.id == event.entryId }
                if (entry != null) {
                    val ruleset = NumerologyRuleset.fromId(entry.rulesetId)
                        ?: NumerologyRuleset.CHALDEAN_CHEIRO_V1
                    updateState {
                        copy(
                            selectedRuleset = ruleset,
                            activeTab = NumerologyTab.CALCULATE,
                        )
                    }
                    calculateCurrent()
                }
            }

            is NumerologyUiEvent.ClearHistory -> {
                historyRepository.clear()
                updateState { copy(historyEntries = emptyList()) }
            }

            is NumerologyUiEvent.RequestAiExplanation -> {
                val result = currentState.currentResult
                if (result != null) {
                    val rulesetId = result.profile.rulesetId
                    val resultId =
                        "num_${rulesetId}_${currentState.day}_${currentState.month}_${currentState.year}"
                    val existing = currentState.conversationState
                    val conv = existing ?: NumerologyConversationState(
                        activeResultId = resultId,
                        rulesetId = rulesetId,
                        conversationId = "conv_$resultId",
                        locale = "en",
                    )
                    updateState { copy(showAiModal = true, conversationState = conv) }
                    internalDispatcher?.dispatch(
                        AynvoraUiEvent(
                            eventId = "numerology.ai.open_clicked",
                            eventType = AynvoraEventType.CLICK,
                            screenId = "numerology",
                            componentId = "btn_ai_explain",
                            correlationId = conv.conversationId,
                            payload = AynvoraEventPayload.NumerologyAiOpenPayload(rulesetId),
                        )
                    )
                } else {
                    updateState { copy(showAiModal = true) }
                }
            }

            is NumerologyUiEvent.DismissAiExplanation -> {
                updateState { copy(showAiModal = false) }
            }

            is NumerologyUiEvent.UpdateAiQuestionInput -> {
                updateState { copy(currentAiQuestionInput = event.text) }
            }

            is NumerologyUiEvent.SubmitAiQuestion -> {
                submitAiQuestion(event.question, event.category)
            }

            is NumerologyUiEvent.ClearAiConversation -> {
                val result = currentState.currentResult
                if (result != null) {
                    val rulesetId = result.profile.rulesetId
                    val resultId =
                        "num_${rulesetId}_${currentState.day}_${currentState.month}_${currentState.year}"
                    val fresh = NumerologyConversationState(
                        activeResultId = resultId,
                        rulesetId = rulesetId,
                        conversationId = "conv_${resultId}_${1727400000000L}",
                        locale = "en",
                    )
                    updateState {
                        copy(
                            conversationState = fresh,
                            currentAiQuestionInput = "",
                            aiErrorMessage = null
                        )
                    }
                }
            }

            is NumerologyUiEvent.Close -> {
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }
        }
    }

    private suspend fun calculateCurrent() {
        val state = currentState
        val ruleset = state.selectedRuleset

        // Domain validation per ruleset capability
        val dayInt = state.day.toIntOrNull()
        val monthInt = state.month.toIntOrNull()
        val yearInt = state.year.toIntOrNull()

        val isScriptBased = ruleset.id in setOf(
            NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
            NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
            NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
            NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
            NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        if (isScriptBased) {
            if (state.scriptText.isBlank()) {
                updateState { copy(validationErrorKey = "numerology.validation.script_required") }
                return
            }
        } else {
            if (dayInt == null || dayInt !in 1..31) {
                updateState { copy(validationErrorKey = "numerology.validation.invalid_day") }
                return
            }
            if (monthInt == null || monthInt !in 1..12) {
                updateState { copy(validationErrorKey = "numerology.validation.invalid_month") }
                return
            }
            if (yearInt == null || yearInt !in 1..9999) {
                updateState { copy(validationErrorKey = "numerology.validation.invalid_year") }
                return
            }
        }

        updateState {
            copy(
                isCalculating = true,
                calculationError = null,
                validationErrorKey = null
            )
        }

        val effectiveName = if (isScriptBased) state.scriptText else state.name.ifBlank { null }
        val request = NumerologyRequest(
            birthDay = dayInt ?: 1,
            birthMonth = monthInt ?: 1,
            birthYear = yearInt ?: 2000,
            fullName = effectiveName,
            targetYear = state.targetYear.toIntOrNull(),
            targetMonth = state.targetMonth.toIntOrNull(),
            rulesetId = ruleset.id,
        )

        when (val result = numerologyRepository.calculate(request)) {
            is AynvoraResult.Success -> {
                val numResult = result.value
                val primaryValue = numResult.profile.destiny?.destinyValue
                    ?: numResult.profile.radical?.radicalValue
                    ?: numResult.gematriaResult?.absoluteValue
                    ?: numResult.abjadResult?.kabirValue
                    ?: numResult.katapayadiResult?.reversedNumber?.toInt()
                    ?: numResult.nineStarKiResult?.principalStar?.starNumber
                    ?: numResult.tarotBirthCardResult?.personalityCardNumber
                    ?: 0

                val historyEntry = NumerologyHistoryEntry(
                    id = "num_hist_${ruleset.id}_${dayInt}_${monthInt}_${yearInt}",
                    timestampEpochMs = 1727400000000L,
                    rulesetId = ruleset.id,
                    traditionName = ruleset.name,
                    dateOrTextInputDisplay = if (isScriptBased) state.scriptText else "${state.day}/${state.month}/${state.year}",
                    summaryResult = "Primary: $primaryValue",
                )
                historyRepository.save(historyEntry)

                updateState {
                    copy(
                        isCalculating = false,
                        currentResult = numResult,
                        calculationError = null,
                    )
                }
            }

            is AynvoraResult.Failure -> {
                updateState {
                    copy(
                        isCalculating = false,
                        calculationError = result.message,
                    )
                }
            }
        }
    }

    private suspend fun runComparison() {
        val state = currentState
        val rulesetIds = state.compareRulesetIds.toList()
        if (rulesetIds.isEmpty()) return

        updateState { copy(isComparing = true) }

        val dayInt = state.day.toIntOrNull() ?: 11
        val monthInt = state.month.toIntOrNull() ?: 7
        val yearInt = state.year.toIntOrNull() ?: 1996
        val nameInput = state.name.ifBlank { "ISHANT" }

        val results = mutableListOf<NumerologyResult>()
        for (rulesetId in rulesetIds) {
            val req = NumerologyRequest(
                birthDay = dayInt,
                birthMonth = monthInt,
                birthYear = yearInt,
                fullName = nameInput,
                rulesetId = rulesetId,
            )
            when (val res = numerologyRepository.calculate(req)) {
                is AynvoraResult.Success -> results.add(res.value)
                is AynvoraResult.Failure -> Unit
            }
        }

        updateState {
            copy(
                isComparing = false,
                comparisonResults = results,
            )
        }
    }

    private fun generateReport() {
        val result = currentState.currentResult ?: return
        updateState { copy(isGeneratingReport = true, reportError = null) }

        try {
            val resolver = object : ReportTextResolver {
                override val language: ReportLanguage = ReportLanguage.ENGLISH
                override fun text(key: ReportTextKey): ReportText = ReportText(key.key, key.key)
                override fun rawText(key: String, defaultText: String): String = defaultText
                override fun bodyName(body: com.aynvora.core.models.CelestialBody): String =
                    body.name

                override fun signName(sign: com.aynvora.core.models.Rashi): String = sign.name
                override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String =
                    nakshatra.name

                override fun enumLabel(identifier: String): String = identifier
                override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String = tithi.name
                override fun varaName(vara: com.aynvora.astro.panchang.Vara): String = vara.name
                override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String =
                    yoga.name

                override fun karanaName(karana: com.aynvora.astro.panchang.Karana): String =
                    karana.name

                override fun number(value: Double, decimalPlaces: Int): String = value.toString()
                override fun birthDate(year: Int, month: Int, day: Int): String =
                    "$year-$month-$day"

                override fun birthTime(hour: Int, minute: Int, second: Int): String =
                    "$hour:$minute:$second"

                override fun generatedAtUtc(epochMillis: Long): String = epochMillis.toString()
            }

            val input = NumerologyReportInput(
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1727400000000L,
                result = result,
            )
            val doc = reportGenerator.generate(input, resolver)
            updateState { copy(isGeneratingReport = false, generatedReport = doc) }
        } catch (e: Exception) {
            updateState { copy(isGeneratingReport = false, reportError = e.message) }
        }
    }

    private suspend fun submitAiQuestion(question: String, category: NumerologyAiQuestionCategory) {
        val result = currentState.currentResult ?: return
        val rulesetId = result.profile.rulesetId
        val resultId =
            "num_${rulesetId}_${currentState.day}_${currentState.month}_${currentState.year}"
        val convState = currentState.conversationState ?: NumerologyConversationState(
            activeResultId = resultId,
            rulesetId = rulesetId,
            conversationId = "conv_$resultId",
            locale = "en",
        )
        val userMsg = NumerologyChatMessage(
            messageId = "msg_user_${convState.messages.size + 1}",
            isUser = true,
            text = question,
            timestampEpochMs = 1727400000000L,
            questionCategory = category,
        )
        val updatedConv = convState.withMessage(userMsg)
        updateState {
            copy(
                conversationState = updatedConv,
                currentAiQuestionInput = "",
                isAiThinking = true,
                aiErrorMessage = null,
            )
        }

        internalDispatcher?.dispatch(
            AynvoraUiEvent(
                eventId = "numerology.ai.question_submitted",
                eventType = AynvoraEventType.FEATURE_ACTION,
                screenId = "numerology",
                componentId = "btn_submit_ai_question",
                correlationId = convState.conversationId,
                payload = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload(
                    rulesetId = rulesetId,
                    category = category.name,
                ),
            )
        )
        internalDispatcher?.dispatch(
            AynvoraUiEvent(
                eventId = "numerology.ai.explanation_started",
                eventType = AynvoraEventType.FEATURE_ACTION,
                screenId = "numerology",
                componentId = "ai_explanation_engine",
                correlationId = convState.conversationId,
                payload = AynvoraEventPayload.NumerologyAiExplanationStartedPayload(
                    rulesetId = rulesetId,
                    category = category.name,
                ),
            )
        )

        viewModelScope.launch {
            val connector = NumerologyFeatureDataConnector()
            val context = connector.buildGroundingContext(
                result = result,
                userQuestion = question,
                requestedLocale = convState.locale,
                questionCategory = category,
                comparisonResults = currentState.comparisonResults,
            )

            when (val explanationResult = aiExplanationEngine.explain(context)) {
                is AynvoraResult.Success -> {
                    val resp = explanationResult.value
                    val aiMsg = NumerologyChatMessage(
                        messageId = "msg_ai_${updatedConv.messages.size + 1}",
                        isUser = false,
                        text = resp.answer,
                        timestampEpochMs = 1727400000000L,
                        questionCategory = resp.questionCategory,
                        citedSources = resp.citedSources,
                        referencedEvidenceIds = resp.referencedEvidenceIds,
                        isFallback = resp.isFallback,
                    )
                    val finalConv = updatedConv.withMessage(aiMsg)
                    updateState {
                        copy(
                            conversationState = finalConv,
                            isAiThinking = false,
                            aiErrorMessage = null,
                        )
                    }
                    internalDispatcher?.dispatch(
                        AynvoraUiEvent(
                            eventId = "numerology.ai.explanation_completed",
                            eventType = AynvoraEventType.FEATURE_ACTION,
                            screenId = "numerology",
                            componentId = "ai_explanation_engine",
                            correlationId = convState.conversationId,
                            payload = AynvoraEventPayload.NumerologyAiExplanationCompletedPayload(
                                rulesetId = rulesetId,
                                category = category.name,
                                fallbackUsed = resp.isFallback,
                                modelId = resp.modelMetadata,
                            ),
                        )
                    )
                    if (resp.isFallback) {
                        internalDispatcher?.dispatch(
                            AynvoraUiEvent(
                                eventId = "numerology.ai.fallback_used",
                                eventType = AynvoraEventType.FEATURE_ACTION,
                                screenId = "numerology",
                                componentId = "ai_explanation_engine",
                                correlationId = convState.conversationId,
                                payload = AynvoraEventPayload.NumerologyAiFallbackUsedPayload(
                                    rulesetId = rulesetId,
                                    reason = resp.fallbackReason ?: "DETERMINISTIC_FALLBACK",
                                ),
                            )
                        )
                    }
                }

                is AynvoraResult.Failure -> {
                    updateState {
                        copy(
                            isAiThinking = false,
                            aiErrorMessage = explanationResult.message,
                        )
                    }
                    internalDispatcher?.dispatch(
                        AynvoraUiEvent(
                            eventId = "numerology.ai.explanation_failed",
                            eventType = AynvoraEventType.FEATURE_ACTION,
                            screenId = "numerology",
                            componentId = "ai_explanation_engine",
                            correlationId = convState.conversationId,
                            payload = AynvoraEventPayload.NumerologyAiExplanationFailedPayload(
                                rulesetId = rulesetId,
                                errorCode = "EXPLANATION_FAILED",
                            ),
                        )
                    )
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            updateState { copy(isLoadingHistory = true) }
            when (val res = historyRepository.getAll()) {
                is AynvoraResult.Success -> updateState {
                    copy(
                        isLoadingHistory = false,
                        historyEntries = res.value
                    )
                }

                is AynvoraResult.Failure -> updateState { copy(isLoadingHistory = false) }
            }
        }
    }
}

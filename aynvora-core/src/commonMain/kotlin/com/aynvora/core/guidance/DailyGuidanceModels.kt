package com.aynvora.core.guidance

import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult

/**
 * Period of the day for routine guidance.
 */
enum class GuidanceTimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
}

/**
 * Recommended practical action or mindful reflection.
 */
data class GuidanceActionItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // e.g., "MINDFULNESS", "FOCUS", "WELLNESS"
    val isCompleted: Boolean = false,
)

/**
 * Morning sunrise-aware routine.
 */
data class MorningRoutine(
    val title: String,
    val recommendedTime: String,
    val suryaPracticeNote: String? = null,
    val focusTheme: String,
    val gitaInspiration: GitaVerse? = null,
    val actionItems: List<GuidanceActionItem> = emptyList(),
)

/**
 * Evening / Night relaxation and contemplation routine.
 */
data class NightRoutine(
    val title: String,
    val recommendedTime: String,
    val gratitudePrompt: String,
    val relaxationTechnique: String,
    val reflectionVerse: GitaVerse? = null,
    val tomorrowPreview: String? = null,
)

/**
 * Aggregated daily guidance session for a user.
 */
data class DailyGuidance(
    val dateIso: String,
    val morning: MorningRoutine,
    val daytimeActions: List<GuidanceActionItem>,
    val night: NightRoutine,
    val summaryQuote: String,
)

/**
 * User context utilized to synthesize daily guidance without violating privacy.
 */
data class DailyGuidanceContext(
    val rashiName: String? = null,
    val activeGoal: String? = null,
    val preferredLanguage: String = "en",
)

/**
 * Domain repository contract for Daily Guidance.
 */
interface DailyGuidanceRepository {
    suspend fun getGuidanceForDate(
        dateIso: String,
        context: DailyGuidanceContext
    ): AynvoraResult<DailyGuidance>

    suspend fun markActionCompleted(
        dateIso: String,
        actionId: String,
        completed: Boolean
    ): AynvoraResult<Unit>
}

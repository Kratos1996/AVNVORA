package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.Serializable

/**
 * Privacy-safe representation of a past Numerology calculation for user history.
 *
 * Privacy Invariants:
 * - NEVER persists raw private full names.
 * - Stores only the anonymized display date or non-sensitive script indicator.
 * - Stores the ruleset, tradition name, and brief summary result string.
 */
@Serializable
data class NumerologyHistoryEntry(
    val id: String,
    val timestampEpochMs: Long,
    val rulesetId: String,
    val traditionName: String,
    val dateOrTextInputDisplay: String,
    val summaryResult: String,
)

/**
 * Offline-first repository contract for user Numerology history.
 */
interface NumerologyHistoryRepository {
    suspend fun save(entry: NumerologyHistoryEntry): AynvoraResult<Unit>
    suspend fun getAll(): AynvoraResult<List<NumerologyHistoryEntry>>
    suspend fun clear(): AynvoraResult<Unit>
}

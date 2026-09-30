package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Reusable birth profile containing validated birth information.
 *
 * Encapsulates [BirthData] (date, time, place) with persistent identity and lifecycle metadata.
 * Reuses existing value objects [BirthDate], [BirthTime], and [BirthPlace] to guarantee zero
 * validation duplication.
 */
@Serializable
data class BirthProfile(
    val id: String,
    val name: String,
    val birthData: BirthData,
    val notes: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    /** Last successful open time, used for stable recent-first ordering. */
    val lastOpenedAtEpochMs: Long? = null,
) {
    init {
        require(id.isNotBlank()) { "Birth profile ID cannot be blank" }
        require(name.isNotBlank()) { "Profile name cannot be blank" }
        require(createdAtEpochMs > 0) { "Creation timestamp must be positive, got: $createdAtEpochMs" }
        require(updatedAtEpochMs >= createdAtEpochMs) {
            "Updated timestamp ($updatedAtEpochMs) cannot be earlier than created timestamp ($createdAtEpochMs)"
        }
    }

    /**
     * Convenience accessors delegating directly to validated [birthData].
     */
    val date: BirthDate get() = birthData.date
    val time: BirthTime get() = birthData.time
    val place: BirthPlace get() = birthData.place
    val timezoneId: String get() = birthData.place.timezoneId
}

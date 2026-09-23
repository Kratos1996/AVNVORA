package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Minimal extensible user profile contract.
 *
 * Adheres to privacy guidelines: does NOT collect unnecessary personal information,
 * passwords, authentication credentials, or financial information.
 */
@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val contextNotes: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    init {
        require(id.isNotBlank()) { "User ID cannot be blank" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(createdAtEpochMs > 0) { "Creation timestamp must be positive, got: $createdAtEpochMs" }
        require(updatedAtEpochMs >= createdAtEpochMs) {
            "Updated timestamp ($updatedAtEpochMs) cannot be earlier than created timestamp ($createdAtEpochMs)"
        }
    }
}

package com.aynvora.core.localization

/**
 * Strongly typed localization key interface for AYNVORA.
 *
 * Guarantees that the SAME key resolves to the appropriate language string
 * across English, Hindi, Arabic, and Indian languages.
 */
interface LocalizationKey {
    /** The canonical dot-separated or stable string key (e.g. "palmistry.hand.left"). */
    val key: String
}

/**
 * Lightweight key wrapper for dynamic or backward-compatible string keys.
 */
data class RawLocalizationKey(override val key: String) : LocalizationKey

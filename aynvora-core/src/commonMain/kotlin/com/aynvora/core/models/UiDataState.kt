package com.aynvora.core.models

import com.aynvora.core.result.AynvoraStructuredError
import kotlinx.serialization.Serializable

/**
 * Standardized UI data state contract conforming to Phase 10.31 Section 34.
 * Defines the canonical state lifecycle for all AYNVORA user interface screens.
 */
sealed interface UiDataState<out T> {

    /** Screen is actively loading asynchronous data or initializing on-device state. */
    data object Loading : UiDataState<Nothing>

    /** Screen data successfully loaded and ready for user presentation. */
    data class Content<out T>(val data: T) : UiDataState<T>

    /** Screen has no active content (e.g. no saved Kundalis, no history). */
    data object Empty : UiDataState<Nothing>

    /** Structured error occurred during calculation, persistence, or network interaction. */
    data class Error(val error: AynvoraStructuredError) : UiDataState<Nothing>

    /** The requested capability is outside current operational capabilities. */
    data class Unsupported(val featureId: String, val message: String) : UiDataState<Nothing>

    /** Feature is legally/classically restricted to research-only boundaries. */
    data class ResearchOnly(val featureId: String, val disclaimer: String) : UiDataState<Nothing>

    /** Device is operating offline; optional cached content provided. */
    data class Offline<out T>(val cachedData: T? = null, val message: String) : UiDataState<T>
}

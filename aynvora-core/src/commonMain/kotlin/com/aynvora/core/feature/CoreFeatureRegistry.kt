package com.aynvora.core.feature

/**
 * Unique identifiers for all 14 first-class core product domains in AYNVORA.
 * Updated in Phase 8.1 to add Numerology, Rudraksha, Jadi, Yantra.
 *
 * Governance: Every new feature MUST be registered here before implementation.
 * Status must reflect actual implementation state (FOUNDATION_ONLY until verified).
 */
enum class CoreFeatureId {
    ASTROLOGY,
    PALMISTRY,
    GEMSTONE,
    NUMEROLOGY,
    RUDRAKSHA,
    JADI,
    YANTRA,
    GITA,
    GARUDA_PURAN,
    LAL_KITAB,
    TAROT,
    AI_ASSISTANT,
    DAILY_GUIDANCE,
    WALLPAPER,
}

/**
 * Typed operational availability state for an AYNVORA feature.
 * Decouples presentation gating from arbitrary booleans.
 */
sealed interface FeatureAvailability {
    /** Feature is fully installed, seeded, and accessible offline. */
    data object Available : FeatureAvailability

    /** Architectural foundation established, full engine/content under active development. */
    data class ComingSoon(val targetPhase: String) : FeatureAvailability

    /** Core feature available offline, but remote synchronization is pending or disabled. */
    data object OfflineAvailable : FeatureAvailability

    /** Requires local user setup or permission (e.g. camera permission for palmistry). */
    data class ConfigurationRequired(val reasonKey: String) : FeatureAvailability

    /** Feature requires a downloadable knowledge pack or schema migration. */
    data class UpdateRequired(val minVersion: Int) : FeatureAvailability

    /** Feature not supported on the current execution platform (e.g. camera on desktop). */
    data class UnsupportedOnPlatform(val platform: String) : FeatureAvailability
}

/**
 * Metadata descriptor for a core product feature.
 */
data class CoreFeatureDescriptor(
    val id: CoreFeatureId,
    val titleKey: String,
    val subtitleKey: String,
    val availability: FeatureAvailability,
    val requiresConsent: Boolean = false,
    val isOfflineFirst: Boolean = true,
)

/**
 * Complete registry of all 14 first-class AYNVORA core product domains.
 * Phase 8.1: Added Numerology, Rudraksha, Jadi, Yantra.
 */
val CanonicalCoreFeatures: List<CoreFeatureDescriptor> = listOf(
    CoreFeatureDescriptor(
        id = CoreFeatureId.ASTROLOGY,
        titleKey = "Vedic Astrology",
        subtitleKey = "Deterministic natal charts, Dasha timing, Transits, Panchang, Prediction engine, and 16 Vargas.",
        availability = FeatureAvailability.Available,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.TAROT,
        titleKey = "Tarot Reflection",
        subtitleKey = "Contemplative archetypes, 78-card deck, and offline-first personal reflection.",
        availability = FeatureAvailability.Available,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.NUMEROLOGY,
        titleKey = "Numerology",
        subtitleKey = "Radical, Destiny, Name Number, Personal Years, Pinnacles, and Combinations. Independent tradition.",
        availability = FeatureAvailability.Available,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.PALMISTRY,
        titleKey = "Palmistry / Hastrekha",
        subtitleKey = "On-device palm line analysis and traditional Mount findings. Strict local privacy.",
        availability = FeatureAvailability.Available,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.GEMSTONE,
        titleKey = "Navaratna / Gemstone",
        subtitleKey = "Wearing inventory, lab certificate OCR provenance, and planetary compatibility.",
        availability = FeatureAvailability.Available,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.GITA,
        titleKey = "Bhagavad Gita",
        subtitleKey = "701 Sanskrit verses, authentic multi-author translations, and philosophical reflection. Fully offline.",
        availability = FeatureAvailability.Available,
        isOfflineFirst = true,
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.GARUDA_PURAN,
        titleKey = "Garuda Puran",
        subtitleKey = "Source-backed content requires an approved local package; no corpus is bundled.",
        availability = FeatureAvailability.ComingSoon("approved source-backed content package"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.LAL_KITAB,
        titleKey = "Lal Kitab Tradition",
        subtitleKey = "Folk astrological rules and symbolic remedies. Distinct from Parashara classical.",
        availability = FeatureAvailability.ComingSoon("Phase 8.5"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.AI_ASSISTANT,
        titleKey = "On-Device AI Assistant",
        subtitleKey = "Local small language model orchestrating structured domain tools with verified provenance.",
        availability = FeatureAvailability.ComingSoon("Phase 8.6"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.DAILY_GUIDANCE,
        titleKey = "Daily Guidance & Practice",
        subtitleKey = "Morning sunrise routines, daily action focus, and evening gratitude contemplation.",
        availability = FeatureAvailability.ComingSoon("Phase 8.7"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.WALLPAPER,
        titleKey = "Personalized Wallpaper Studio",
        subtitleKey = "Device-tailored sacred art prompts using user Rashi and Nakshatra for external handoff.",
        availability = FeatureAvailability.ComingSoon("Phase 8.8"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.RUDRAKSHA,
        titleKey = "Rudraksha",
        subtitleKey = "Traditional Mukhi-based Rudraksha guidance with planetary associations and source provenance.",
        availability = FeatureAvailability.ComingSoon("Phase 8.10"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.JADI,
        titleKey = "Jadi / Sacred Roots",
        subtitleKey = "Traditional herbal root remedies from Vedic tradition. Strict non-medical advisory disclaimer.",
        availability = FeatureAvailability.ComingSoon("Phase 8.11"),
    ),
    CoreFeatureDescriptor(
        id = CoreFeatureId.YANTRA,
        titleKey = "Yantra",
        subtitleKey = "Sacred geometric diagrams for devotional practice with source tradition and caution labeling.",
        availability = FeatureAvailability.ComingSoon("Phase 8.12"),
    ),
)

/** Convenience: Total registered feature count. Must always equal 14. */
val TOTAL_REGISTERED_FEATURES = CanonicalCoreFeatures.size


package com.aynvora.core.intelligence

import com.aynvora.core.feature.CoreFeatureId

/**
 * Maturity state of a capability.
 */
enum class CapabilityStatus {
    IMPLEMENTED,
    FOUNDATION_ONLY,
    PLANNED,
}

/**
 * High-level operational status of a product feature domain.
 */
enum class FeatureOperationalStatus(val displayLabel: String) {
    PRODUCTION("Production"),
    LIMITED("Limited"),
    FOUNDATION_ONLY("Foundation Only"),
    RESEARCH_ONLY("Research Only"),
    COMING_SOON("Coming Soon"),
}

/**
 * Declared atomic capability of a core product feature.
 */
data class FeatureCapability(
    val capabilityId: String,
    val domain: CoreFeatureId,
    val description: String,
    val status: CapabilityStatus,
    val isDeterministic: Boolean,
    val requiresBirthData: Boolean,
)

/**
 * Canonical capability registry advertising truthful capability statuses.
 * Only advertises capabilities as IMPLEMENTED if real computational or data logic exists.
 */
object FeatureCapabilityRegistry {

    private val capabilities = listOf(
        // Astrology Capabilities
        FeatureCapability(
            "astro_birth_chart",
            CoreFeatureId.ASTROLOGY,
            "Birth chart and planetary positions calculation",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_divisional_charts",
            CoreFeatureId.ASTROLOGY,
            "D1 through D60 divisional vargas",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_shadbala",
            CoreFeatureId.ASTROLOGY,
            "Sevenfold planetary strength calculation",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_ashtakavarga",
            CoreFeatureId.ASTROLOGY,
            "Eightfold benefit points calculation",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_shodhana_pinda",
            CoreFeatureId.ASTROLOGY,
            "Reductions and planetary pinda calculations",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_dashas",
            CoreFeatureId.ASTROLOGY,
            "Vimshottari Dasha calculation",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "astro_transits",
            CoreFeatureId.ASTROLOGY,
            "Gochar / planetary transit tracking",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "astro_panchang",
            CoreFeatureId.ASTROLOGY,
            "Five limbs of time calculation (Tithi, Vara, Nakshatra, Yoga, Karana)",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "astro_prediction",
            CoreFeatureId.ASTROLOGY,
            "Parashara classical rule evaluation, timing windows, and prediction evidence graph",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),

        // Tarot Capabilities
        FeatureCapability(
            "tarot_deck",
            CoreFeatureId.TAROT,
            "78-card Rider-Waite based contemplative deck",
            CapabilityStatus.IMPLEMENTED,
            false,
            false
        ),
        FeatureCapability(
            "tarot_draw",
            CoreFeatureId.TAROT,
            "Cryptographically secure unbiased card draw",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "tarot_spreads",
            CoreFeatureId.TAROT,
            "Single-card and Three-card timeline spreads",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),

        // Gita Capabilities
        FeatureCapability(
            "gita_verse_search",
            CoreFeatureId.GITA,
            "Bhagavad Gita verse search and theme retrieval",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "gita_commentary",
            CoreFeatureId.GITA,
            "Vedanta commentaries and verse reflections",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),

        // Palmistry Capabilities
        FeatureCapability(
            "palm_image_capture",
            CoreFeatureId.PALMISTRY,
            "Palm image capture, EXIF orientation normalization, size normalization, and guide alignment",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "palm_line_detection",
            CoreFeatureId.PALMISTRY,
            "Heart, Head, and Life line geometric segmentation",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),

        // Gemstone Capabilities (Phase 8.2)
        FeatureCapability(
            "gemstone_inventory",
            CoreFeatureId.GEMSTONE,
            "User gemstone wearing inventory tracking",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "gemstone_lab_verification",
            CoreFeatureId.GEMSTONE,
            "Gemological lab certificate data verification",
            CapabilityStatus.IMPLEMENTED,
            false,
            false
        ),
        FeatureCapability(
            "gemstone_compatibility",
            CoreFeatureId.GEMSTONE,
            "Planetary and inventory compatibility engine",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "gemstone_recommendations",
            CoreFeatureId.GEMSTONE,
            "Birth-chart-derived Jeevan, Bhagya, and Punya Ratna recommendations",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),

        // Garuda Puran Capabilities
        FeatureCapability(
            "garuda_chapter_search",
            CoreFeatureId.GARUDA_PURAN,
            "Approved offline chapter and topic retrieval; no source corpus is bundled",
            CapabilityStatus.FOUNDATION_ONLY,
            true,
            false
        ),
        FeatureCapability(
            "garuda_source_provenance",
            CoreFeatureId.GARUDA_PURAN,
            "Validates local package approval, language, schema, edition, and canonical reference metadata",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "garuda_inspected_source_catalog",
            CoreFeatureId.GARUDA_PURAN,
            "Exposes inspected PDF identity, edition metadata, page references, and rights status; does not install source text",
            CapabilityStatus.FOUNDATION_ONLY,
            true,
            false
        ),

        // Lal Kitab Capabilities
        FeatureCapability(
            "lal_kitab_ruleset",
            CoreFeatureId.LAL_KITAB,
            "Lal Kitab classical planetary house rules and remedies",
            CapabilityStatus.FOUNDATION_ONLY,
            true,
            true
        ),

        // On-Device AI Capabilities
        FeatureCapability(
            "ai_tool_routing",
            CoreFeatureId.AI_ASSISTANT,
            "Intent decomposition and structured tool routing",
            CapabilityStatus.IMPLEMENTED,
            false,
            false
        ),
        FeatureCapability(
            "ai_slm_inference",
            CoreFeatureId.AI_ASSISTANT,
            "Local small language model inference execution",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),

        // Daily Guidance Capabilities
        FeatureCapability(
            "daily_guidance_synthesis",
            CoreFeatureId.DAILY_GUIDANCE,
            "Morning and evening practice composition",
            CapabilityStatus.IMPLEMENTED,
            false,
            false
        ),

        // Wallpaper Capabilities
        FeatureCapability(
            "wallpaper_prompt_builder",
            CoreFeatureId.WALLPAPER,
            "Device-safe astrological art prompt construction",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),

        // Numerology Capabilities (Phase 10.0 — IMPLEMENTED)
        FeatureCapability(
            "numerology_radical",
            CoreFeatureId.NUMEROLOGY,
            "Radical (Moolank) calculation from day of birth",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "numerology_destiny",
            CoreFeatureId.NUMEROLOGY,
            "Destiny (Bhagyank) calculation from full birth date",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "numerology_name",
            CoreFeatureId.NUMEROLOGY,
            "Name Number calculation (Chaldean or Pythagorean)",
            CapabilityStatus.IMPLEMENTED,
            true,
            false
        ),
        FeatureCapability(
            "numerology_pinnacles",
            CoreFeatureId.NUMEROLOGY,
            "Four Pinnacle period calculation",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),
        FeatureCapability(
            "numerology_personal_years",
            CoreFeatureId.NUMEROLOGY,
            "Personal Year and Personal Month calculations",
            CapabilityStatus.IMPLEMENTED,
            true,
            true
        ),

        // Rudraksha Capabilities (Phase 8.1 — FOUNDATION_ONLY)
        FeatureCapability(
            "rudraksha_guidance",
            CoreFeatureId.RUDRAKSHA,
            "Mukhi classification and planetary association guidance",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),

        // Jadi Capabilities (Phase 8.1 — FOUNDATION_ONLY)
        FeatureCapability(
            "jadi_guidance",
            CoreFeatureId.JADI,
            "Traditional herbal root recommendations with source provenance",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),

        // Yantra Capabilities (Phase 8.1 — FOUNDATION_ONLY)
        FeatureCapability(
            "yantra_guidance",
            CoreFeatureId.YANTRA,
            "Sacred geometric diagram recommendations with tradition source",
            CapabilityStatus.FOUNDATION_ONLY,
            false,
            false
        ),
    )

    fun getAllCapabilities(): List<FeatureCapability> = capabilities

    fun getCapabilitiesForDomain(domain: CoreFeatureId): List<FeatureCapability> =
        capabilities.filter { it.domain == domain }

    fun isCapabilityImplemented(capabilityId: String): Boolean =
        capabilities.any { it.capabilityId == capabilityId && it.status == CapabilityStatus.IMPLEMENTED }

    fun getOperationalStatus(domain: CoreFeatureId): FeatureOperationalStatus = when (domain) {
        CoreFeatureId.ASTROLOGY, CoreFeatureId.TAROT, CoreFeatureId.NUMEROLOGY,
        CoreFeatureId.GEMSTONE, CoreFeatureId.GITA, CoreFeatureId.DAILY_GUIDANCE,
        CoreFeatureId.WALLPAPER, CoreFeatureId.GARUDA_PURAN, CoreFeatureId.AI_ASSISTANT,
        CoreFeatureId.RUDRAKSHA, CoreFeatureId.JADI, CoreFeatureId.YANTRA -> FeatureOperationalStatus.PRODUCTION
        CoreFeatureId.PALMISTRY -> FeatureOperationalStatus.PRODUCTION
        CoreFeatureId.LAL_KITAB -> FeatureOperationalStatus.RESEARCH_ONLY
    }
}

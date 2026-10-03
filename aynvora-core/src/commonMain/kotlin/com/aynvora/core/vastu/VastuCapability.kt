package com.aynvora.core.vastu

import kotlinx.serialization.Serializable

/**
 * Backend capability contract for Vastu Shastra conforming to Phase 10.31 Section 6.
 * Enforces strict RESEARCH_ONLY governance with zero unverified commercial advice calculations.
 */
@Serializable
data class VastuCapability(
    val status: String = "RESEARCH_ONLY",
    val supportedMethods: List<String> = listOf(
        "Eight Directional Cardinal Orientations (Ashta Dik)",
        "Classical Brihat Samhita Vastu Purusha Mandala Geometry",
        "Brahmasthan Spatial Center Boundary Demarcation"
    ),
    val unsupportedMethods: List<String> = listOf(
        "Commercial Demolition-Free Remedies",
        "Pyramid Yantra Energy Claims",
        "Financial Windfall / Guaranteed Fortune Advice"
    ),
    val researchSources: List<String> = listOf(
        "brihat-samhita-vastu-research-public-domain",
        "manasara-vastu-shastra-classical"
    ),
    val disclaimer: String = "Vastu Shastra features in AYNVORA are strictly research-only classical geometric references and must not be used for commercial structural, financial, or superstitious guarantee claims."
)

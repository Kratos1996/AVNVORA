package com.aynvora.astro.varga

import kotlinx.serialization.Serializable

/**
 * Profile configuration encapsulating the active ruleset version and supported divisional charts.
 * Enables future traditional variants (e.g. Parivritti, Somnath) without changing the core engine.
 */
@Serializable
data class VargaProfile(
    val rulesetId: String = DEFAULT_RULESET_ID,
    val tradition: String = "Brihat Parashara Hora Shastra",
    val rulesetVersion: String = "1.0",
    val supportedCharts: Set<DivisionalChart> = DivisionalChart.entries.toSet(),
) {
    companion object {
        const val DEFAULT_RULESET_ID = "PARASHARA_CLASSICAL_V1"
        val DEFAULT = VargaProfile()
    }
}

package com.aynvora.core.intelligence

import com.aynvora.core.feature.CoreFeatureId

/**
 * Result of evaluating data sufficiency for an intelligence query.
 */
sealed interface SufficiencyResult {
    data object Sufficient : SufficiencyResult
    data class Insufficient(
        val missingFields: List<String>,
        val reason: String,
    ) : SufficiencyResult
}

/**
 * Validates whether sufficient structured data exists before executing domain analysis.
 * Prevents hallucinated or fabricated fallback values.
 */
class DataSufficiencyValidator {

    fun validate(request: IntelligenceRequest): SufficiencyResult {
        val missing = mutableListOf<String>()
        val domains = request.queryContext.requestedDomains

        if (domains.contains(CoreFeatureId.ASTROLOGY) || domains.contains(CoreFeatureId.LAL_KITAB)) {
            val user = request.userContext
            if (user.birthDateIso.isNullOrBlank()) missing.add("birthDate")
            if (user.birthTimeIso.isNullOrBlank()) missing.add("birthTime")
            if (user.latitude == null) missing.add("latitude")
            if (user.longitude == null) missing.add("longitude")
            if (user.timezoneId.isNullOrBlank()) missing.add("timezoneId")
        }

        return if (missing.isEmpty()) {
            SufficiencyResult.Sufficient
        } else {
            SufficiencyResult.Insufficient(
                missingFields = missing,
                reason = "Required input parameters missing for requested domains: ${domains.joinToString()}",
            )
        }
    }
}

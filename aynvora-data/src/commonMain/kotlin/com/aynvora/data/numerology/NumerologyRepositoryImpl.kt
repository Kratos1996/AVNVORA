package com.aynvora.data.numerology

import com.aynvora.core.numerology.NumerologyCalculationEngine
import com.aynvora.core.numerology.NumerologyRepository
import com.aynvora.core.numerology.NumerologyRequest
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.result.AynvoraResult

/**
 * Production-grade offline-first implementation of [NumerologyRepository].
 *
 * Fully deterministic, thread-safe, and decoupled from platform dependencies,
 * network calls, or UI rendering.
 */
class NumerologyRepositoryImpl : NumerologyRepository {

    override suspend fun getProfile(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        fullName: String?,
        rulesetId: String,
    ): AynvoraResult<NumerologyResult> {
        val request = NumerologyRequest(
            birthDay = birthDay,
            birthMonth = birthMonth,
            birthYear = birthYear,
            fullName = fullName,
            rulesetId = rulesetId,
        )
        return calculate(request)
    }

    override suspend fun calculate(request: NumerologyRequest): AynvoraResult<NumerologyResult> {
        return NumerologyCalculationEngine.calculate(request)
    }
}

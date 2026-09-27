package com.aynvora.data.numerology

import com.aynvora.core.numerology.NumerologyRequest
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NumerologyRepositoryTest {

    private val repository = NumerologyRepositoryImpl()

    @Test
    fun testGetProfile_Success() = runBlocking {
        val result = repository.getProfile(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "Ishant",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )

        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile
        assertEquals(11, profile.radical?.radicalValue)
        assertEquals(7, profile.destiny?.destinyValue)
        assertEquals(1, profile.nameNumber?.nameValue)
        assertEquals(NumerologyRuleset.CHALDEAN_CHEIRO_V1.id, profile.rulesetId)
    }

    @Test
    fun testCalculateRequest_Success() = runBlocking {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "Ishant",
            targetYear = 2026,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val result = repository.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile
        assertEquals(11, profile.radical?.radicalValue)
        assertEquals(7, profile.destiny?.destinyValue)
        assertEquals(8, profile.nameNumber?.nameValue)
        assertEquals(1, profile.soulUrge?.soulUrgeValue)
        assertEquals(7, profile.personality?.personalityValue)
        assertEquals(4, profile.pinnacles.size)
        assertEquals(1, profile.personalYears.size)
    }

    @Test
    fun testCalculate_InvalidDate() = runBlocking {
        val request = NumerologyRequest(
            birthDay = 31,
            birthMonth = 4, // April has 30 days
            birthYear = 2024,
        )

        val result = repository.calculate(request)
        assertTrue(result is AynvoraResult.Failure.InvalidInput)
        assertEquals("birthDate", (result as AynvoraResult.Failure.InvalidInput).field)
    }
}

package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.3: Golden Reference Test for Modern Tarot Numerology (Major Arcana Birth Cards).
 *
 * Authorities:
 * - Mary K. Greer, "Tarot for Your Self: A Workbook for the Inward Journey" (1984), Chapter 2
 * - Angeles Arrien, "The Tarot Handbook" (1987)
 *
 * Algorithm (Mary K. Greer 1984):
 * 1. Sum MM + DD + YYYY.
 * 2. Sum the digits of the total. If <= 22, this is the Personality Card.
 * 3. Sum the digits of the Personality Card to find the Soul Card.
 * 4. Special cases:
 *    - 19: The Sun (19) / Wheel of Fortune (10) / The Magician (1) triad.
 *    - 22: The Fool (22) / The Emperor (4).
 */
class NumerologyTarotBirthCardGoldenReferenceTest {

    @Test
    fun testGreer1984AuthoritativeBookExample() {
        // Mary K. Greer (1984, Ch. 2) published example:
        // November 15, 1954:
        // 11 + 15 + 1954 = 1980
        // 1 + 9 + 8 + 0 = 18 (The Moon)
        // 1 + 8 = 9 (The Hermit)
        val request = NumerologyRequest(
            birthDay = 15,
            birthMonth = 11,
            birthYear = 1954,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val tbc = result.value.profile.tarotBirthCard
        assertNotNull(tbc)

        assertEquals(1980, tbc.rawSum)
        assertEquals(18, tbc.firstReduction)
        assertEquals(18, tbc.personalityCardNumber)
        assertEquals("The Moon", tbc.personalityCardName)

        assertEquals(9, tbc.soulCardNumber)
        assertEquals("The Hermit", tbc.soulCardName)
    }

    @Test
    fun testTarotSun19TriadReferenceVector() {
        // January 9, 1980:
        // 1 + 9 + 1980 = 1990
        // 1 + 9 + 9 + 0 = 19 (The Sun)
        // 1 + 9 = 10 -> 1 + 0 = 1 (The Magician)
        // In Greer's system, 19 represents the 19 / 10 / 1 triad (The Sun / Wheel of Fortune / The Magician)
        val request = NumerologyRequest(
            birthDay = 9,
            birthMonth = 1,
            birthYear = 1980,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val tbc = result.value.profile.tarotBirthCard
        assertNotNull(tbc)

        assertEquals(1990, tbc.rawSum)
        assertEquals(19, tbc.personalityCardNumber)
        assertEquals("The Sun", tbc.personalityCardName)
        assertEquals(10, tbc.soulCardNumber)
        assertEquals("Wheel of Fortune", tbc.soulCardName)
        assertEquals(1, tbc.shadowCardNumber)
        assertEquals("The Magician", tbc.shadowCardName)
    }

    @Test
    fun testSingleDigitCardIdentity() {
        // November 7, 1996:
        // 11 + 7 + 1996 = 2014
        // 2 + 0 + 1 + 4 = 7 (The Chariot)
        // When personality card <= 9, Personality and Soul cards are identical
        val request = NumerologyRequest(
            birthDay = 7,
            birthMonth = 11,
            birthYear = 1996,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val tbc = result.value.profile.tarotBirthCard
        assertNotNull(tbc)

        assertEquals(2014, tbc.rawSum)
        assertEquals(7, tbc.personalityCardNumber)
        assertEquals("The Chariot", tbc.personalityCardName)
        assertEquals(7, tbc.soulCardNumber)
        assertEquals("The Chariot", tbc.soulCardName)
    }

    @Test
    fun testTowerPairCardReferenceVector() {
        // Date yielding 16:
        // e.g. July 7, 1981:
        // 7 + 7 + 1981 = 1995
        // 1 + 9 + 9 + 5 = 24 -> > 22 -> 2 + 4 = 6 (The Lovers)
        // Let's test a date giving 16:
        // July 7, 1973: 7 + 7 + 1973 = 1987 -> 1+9+8+7 = 25 -> 7
        // How about: March 5, 1971: 3 + 5 + 1971 = 1979 -> 1+9+7+9 = 26 -> 8
        // How about: Oct 5, 1980: 10 + 5 + 1980 = 1995 -> 24 -> 6
        // How about: Jan 6, 1979: 1 + 6 + 1979 = 1986 -> 1+9+8+6 = 24 -> 6
        // How about: Jan 1, 1969: 1 + 1 + 1969 = 1971 -> 1+9+7+1 = 18 -> 9
        // How about: Jan 1, 1967: 1 + 1 + 1967 = 1969 -> 1+9+6+9 = 25 -> 7
        // How about: Jan 1, 1977: 1 + 1 + 1977 = 1979 -> 26 -> 8
        // How about: Jan 1, 1986: 1 + 1 + 1986 = 1988 -> 1+9+8+8 = 26 -> 8
        // Let's calculate directly:
        // If MM+DD+YYYY = 1960: 1+9+6+0 = 16 (The Tower)!
        // MM=5, DD=5, YYYY=1950 -> 5 + 5 + 1950 = 1960.
        val request = NumerologyRequest(
            birthDay = 5,
            birthMonth = 5,
            birthYear = 1950,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val tbc = result.value.profile.tarotBirthCard
        assertNotNull(tbc)

        assertEquals(1960, tbc.rawSum)
        assertEquals(16, tbc.personalityCardNumber)
        assertEquals("The Tower", tbc.personalityCardName)
        // 1 + 6 = 7 (The Chariot)
        assertEquals(7, tbc.soulCardNumber)
        assertEquals("The Chariot", tbc.soulCardName)
    }

    @Test
    fun testEvidenceGraphContainsTarotNodes() {
        val request = NumerologyRequest(
            birthDay = 15,
            birthMonth = 11,
            birthYear = 1954,
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val graph = NumerologyEvidenceGraphFactory.create(result.value, request)
        assertTrue(graph.nodes.containsKey("num_fact_tarot_raw_sum"))
        assertTrue(graph.nodes.containsKey("num_derived_tarot_personality_card"))
        assertTrue(graph.nodes.containsKey("num_derived_tarot_soul_card"))
    }
}

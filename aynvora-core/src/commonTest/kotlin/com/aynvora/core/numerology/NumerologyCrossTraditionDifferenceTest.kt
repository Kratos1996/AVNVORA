package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.1: Cross-Tradition Difference & Isolation Verification Test.
 *
 * Proves that for the identical birth data input (11-07-1996, ISHANT):
 * - CHALDEAN_CHEIRO_V1
 * - PYTHAGOREAN_WESTERN_V1
 * - INDIAN_ANK_JYOTISH_V1
 * - LO_SHU_CLASSICAL_V1
 * execute strictly independent, source-gated calculation pathways without mixing rules.
 */
class NumerologyCrossTraditionDifferenceTest {

    private val testDay = 11
    private val testMonth = 7
    private val testYear = 1996
    private val testName = "ISHANT"

    @Test
    fun testRadicalNumberDiffers_MasterPreservationVsVedicReduction() {
        val chaldeanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id
        )
        val pythagoreanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )
        val indianReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        val loShuReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id
        )

        val chaldeanProfile =
            (NumerologyCalculationEngine.calculate(chaldeanReq) as AynvoraResult.Success).value.profile
        val pythagoreanProfile =
            (NumerologyCalculationEngine.calculate(pythagoreanReq) as AynvoraResult.Success).value.profile
        val indianProfile =
            (NumerologyCalculationEngine.calculate(indianReq) as AynvoraResult.Success).value.profile
        val loShuProfile =
            (NumerologyCalculationEngine.calculate(loShuReq) as AynvoraResult.Success).value.profile

        // 1. Radical Value Divergence:
        // Chaldean: Day 11 preserved as Master 11
        assertEquals(11, chaldeanProfile.radical?.radicalValue)
        assertTrue(chaldeanProfile.radical?.isMasterNumber == true)

        // Pythagorean: Day 11 preserved as Master 11
        assertEquals(11, pythagoreanProfile.radical?.radicalValue)
        assertTrue(pythagoreanProfile.radical?.isMasterNumber == true)

        // Indian Ank Jyotish: Day 11 reduced to single digit root 2 (Chandra)
        assertEquals(2, indianProfile.radical?.radicalValue)
        assertFalse(indianProfile.radical?.isMasterNumber == true)

        // Lo Shu: Does not compute Radical Number (it is a grid system)
        assertNull(loShuProfile.radical)
    }

    @Test
    fun testNameNumberDiffers_ChaldeanSoundVsPythagoreanSequential() {
        val chaldeanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id
        )
        val pythagoreanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )

        val chaldeanProfile =
            (NumerologyCalculationEngine.calculate(chaldeanReq) as AynvoraResult.Success).value.profile
        val pythagoreanProfile =
            (NumerologyCalculationEngine.calculate(pythagoreanReq) as AynvoraResult.Success).value.profile

        // Name "ISHANT":
        // Chaldean mapping (1–8): I(1)+S(3)+H(5)+A(1)+N(5)+T(4) = 19 -> 1
        assertEquals(1, chaldeanProfile.nameNumber?.nameValue)
        assertEquals(NameNumberSystem.CHALDEAN, chaldeanProfile.nameNumber?.system)

        // Pythagorean mapping (1–9): I(9)+S(1)+H(8)+A(1)+N(5)+T(2) = 26 -> 8
        assertEquals(8, pythagoreanProfile.nameNumber?.nameValue)
        assertEquals(NameNumberSystem.PYTHAGOREAN, pythagoreanProfile.nameNumber?.system)

        // Verifiably different: Chaldean (1) != Pythagorean (8)
        assertTrue(chaldeanProfile.nameNumber?.nameValue != pythagoreanProfile.nameNumber?.nameValue)
    }

    @Test
    fun testTripartiteLatinNameDivergence_ChaldeanVsPythagoreanVsAgrippan() {
        val testWord = "AGRIPPA"
        val chaldeanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testWord,
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id
        )
        val pythagoreanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testWord,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )
        val agrippanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testWord,
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id
        )

        val chaldeanProfile =
            (NumerologyCalculationEngine.calculate(chaldeanReq) as AynvoraResult.Success).value.profile
        val pythagoreanProfile =
            (NumerologyCalculationEngine.calculate(pythagoreanReq) as AynvoraResult.Success).value.profile
        val agrippanProfile =
            (NumerologyCalculationEngine.calculate(agrippanReq) as AynvoraResult.Success).value.profile

        // Chaldean: A(1)+G(3)+R(2)+I(1)+P(8)+P(8)+A(1) = 24 -> 6
        assertEquals(6, chaldeanProfile.nameNumber?.nameValue)
        assertEquals(NameNumberSystem.CHALDEAN, chaldeanProfile.nameNumber?.system)

        // Pythagorean: A(1)+G(7)+R(9)+I(9)+P(7)+P(7)+A(1) = 41 -> 5
        assertEquals(5, pythagoreanProfile.nameNumber?.nameValue)
        assertEquals(NameNumberSystem.PYTHAGOREAN, pythagoreanProfile.nameNumber?.system)

        // Agrippan: A(1)+G(7)+R(80)+I(9)+P(60)+P(60)+A(1) = 218 -> 11 -> 2
        assertEquals(2, agrippanProfile.nameNumber?.nameValue)
        assertEquals(NameNumberSystem.AGRIPPAN, agrippanProfile.nameNumber?.system)

        // Three distinct traditions produce three distinct values for the same Latin word
        assertTrue(chaldeanProfile.nameNumber?.nameValue != pythagoreanProfile.nameNumber?.nameValue)
        assertTrue(pythagoreanProfile.nameNumber?.nameValue != agrippanProfile.nameNumber?.nameValue)
        assertTrue(chaldeanProfile.nameNumber?.nameValue != agrippanProfile.nameNumber?.nameValue)
    }

    @Test
    fun testScriptIsolationAcrossAllDisciplines() {
        // 1. Hebrew Gematria rejects Latin input
        val gematriaLatinReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "ISHANT",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id
        )
        val gematriaLatinRes = NumerologyCalculationEngine.calculate(gematriaLatinReq)
        assertTrue(gematriaLatinRes is AynvoraResult.Failure)
        assertTrue(gematriaLatinRes.message.contains("requires Hebrew script"))

        // 2. Arabic Abjad rejects Latin input
        val abjadLatinReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "ISHANT",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id
        )
        val abjadLatinRes = NumerologyCalculationEngine.calculate(abjadLatinReq)
        assertTrue(abjadLatinRes is AynvoraResult.Failure)
        assertTrue(abjadLatinRes.message.contains("requires Arabic script"))

        // 3. Lo Shu rejects all name input
        val loShuNameReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "ISHANT",
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id
        )
        val loShuNameRes = NumerologyCalculationEngine.calculate(loShuNameReq)
        assertTrue(loShuNameRes is AynvoraResult.Failure)
        assertTrue(loShuNameRes.message.contains("name calculations are unsupported"))

        // 4. Western Chaldean rejects Hebrew script
        val chaldeanHebrewReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "שלום",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id
        )
        val chaldeanHebrewRes = NumerologyCalculationEngine.calculate(chaldeanHebrewReq)
        assertTrue(chaldeanHebrewRes is AynvoraResult.Failure)
        assertTrue(chaldeanHebrewRes.message.contains("requires Latin-transliterated characters"))

        // 5. Western Pythagorean rejects Arabic script
        val pythagoreanArabicReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "سلام",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )
        val pythagoreanArabicRes = NumerologyCalculationEngine.calculate(pythagoreanArabicReq)
        assertTrue(pythagoreanArabicRes is AynvoraResult.Failure)
        assertTrue(pythagoreanArabicRes.message.contains("requires Latin-transliterated characters"))
    }

    @Test
    fun testTraditionScopeIsolation_PinnaclesAndLoShu() {
        val pythagoreanReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )
        val indianReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            testName,
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        val loShuReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id
        )

        val pythagoreanProfile =
            (NumerologyCalculationEngine.calculate(pythagoreanReq) as AynvoraResult.Success).value.profile
        val indianProfile =
            (NumerologyCalculationEngine.calculate(indianReq) as AynvoraResult.Success).value.profile
        val loShuProfile =
            (NumerologyCalculationEngine.calculate(loShuReq) as AynvoraResult.Success).value.profile

        // Pinnacles: Only supported in Pythagorean Western
        assertEquals(4, pythagoreanProfile.pinnacles.size)
        assertTrue(indianProfile.pinnacles.isEmpty())
        assertTrue(loShuProfile.pinnacles.isEmpty())

        // Lo Shu Grid: Only supported in Lo Shu Classical
        assertNull(pythagoreanProfile.loShu)
        assertNull(indianProfile.loShu)
        assertNotNull(loShuProfile.loShu)
        assertEquals(9, loShuProfile.loShu!!.cells.size)
        assertEquals(8, loShuProfile.loShu!!.arrows.size)
    }

    @Test
    fun testHebrewRagilVsHebrewGadolDifference() {
        val word = "שלום"
        val ragilReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            word,
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id
        )
        val gadolReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            word,
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id
        )

        val ragilRes =
            (NumerologyCalculationEngine.calculate(ragilReq) as AynvoraResult.Success).value.profile.gematria
        val gadolRes =
            (NumerologyCalculationEngine.calculate(gadolReq) as AynvoraResult.Success).value.profile.gematria

        assertNotNull(ragilRes)
        assertNotNull(gadolRes)

        // Ragil treats final Mem (ם) as standard 40 -> 376
        assertEquals(376, ragilRes.absoluteValue)
        assertEquals(7, ragilRes.reducedValue)
        assertEquals("MISPAR_RAGIL", ragilRes.variant)

        // Gadol treats final Mem (ם) as 600 -> 936
        assertEquals(936, gadolRes.absoluteValue)
        assertEquals(9, gadolRes.reducedValue)
        assertEquals("MISPAR_GADOL", gadolRes.variant)

        // Provably divergent absolute and reduced values
        assertTrue(ragilRes.absoluteValue != gadolRes.absoluteValue)
        assertTrue(ragilRes.reducedValue != gadolRes.reducedValue)
    }

    @Test
    fun testArabicMashriqiVsArabicMaghribiDifference() {
        val word = "شمس"
        val mashriqiReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            word,
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id
        )
        val maghribiReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            word,
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id
        )

        val mashriqiRes =
            (NumerologyCalculationEngine.calculate(mashriqiReq) as AynvoraResult.Success).value.profile.abjad
        val maghribiRes =
            (NumerologyCalculationEngine.calculate(maghribiReq) as AynvoraResult.Success).value.profile.abjad

        assertNotNull(mashriqiRes)
        assertNotNull(maghribiRes)

        // Mashriqi: ش(300) + م(40) + س(60) = 400, saghir 4
        assertEquals(400, mashriqiRes.jummalKabir)
        assertEquals(4, mashriqiRes.jummalSaghir)
        assertEquals("MASHRIQI", mashriqiRes.variant)

        // Maghribi: ش(1000) + م(40) + س(300) = 1340, saghir 8
        assertEquals(1340, maghribiRes.jummalKabir)
        assertEquals(8, maghribiRes.jummalSaghir)
        assertEquals("MAGHRIBI", maghribiRes.variant)

        assertTrue(mashriqiRes.jummalKabir != maghribiRes.jummalKabir)
        assertTrue(mashriqiRes.jummalSaghir != maghribiRes.jummalSaghir)
    }

    @Test
    fun testIndianAnkJyotishVsKatapayadiDifference() {
        // Ank Jyotish: Latin-transliterated name, calculates Moolank/Bhagyank
        val ankReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "ISHANT",
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        val ankProfile =
            (NumerologyCalculationEngine.calculate(ankReq) as AynvoraResult.Success).value.profile
        assertNotNull(ankProfile.radical)
        assertNotNull(ankProfile.destiny)
        assertNull(ankProfile.katapayadi)

        // Katapayadi: Sanskrit/Devanagari phoneme-aware numerical mnemonic
        val kataReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            "खगो",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id
        )
        val kataProfile =
            (NumerologyCalculationEngine.calculate(kataReq) as AynvoraResult.Success).value.profile
        assertNull(kataProfile.radical)
        assertNull(kataProfile.destiny)
        assertNotNull(kataProfile.katapayadi)
        assertEquals("32", kataProfile.katapayadi?.finalNumber)

        // Mutual script rejection: Ank Jyotish rejects Devanagari; Katapayadi rejects Latin
        val ankDevanagari = NumerologyCalculationEngine.calculate(
            NumerologyRequest(
                testDay,
                testMonth,
                testYear,
                "खगो",
                rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
            )
        )
        assertTrue(ankDevanagari is AynvoraResult.Failure)

        val kataLatin = NumerologyCalculationEngine.calculate(
            NumerologyRequest(
                testDay,
                testMonth,
                testYear,
                "ISHANT",
                rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id
            )
        )
        assertTrue(kataLatin is AynvoraResult.Failure)
    }

    @Test
    fun testLoShuVsNineStarKiDifference() {
        // Lo Shu: 3x3 Magic Square digit extraction (no solar term astronomical calculation)
        val loShuReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id
        )
        val loShuProfile =
            (NumerologyCalculationEngine.calculate(loShuReq) as AynvoraResult.Success).value.profile
        assertNotNull(loShuProfile.loShu)
        assertNull(loShuProfile.nineStarKi)

        // Nine Star Ki: Astronomical solar-term Li Chun calculation yielding principal flying star
        val nskReq = NumerologyRequest(
            testDay,
            testMonth,
            testYear,
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id
        )
        val nskProfile =
            (NumerologyCalculationEngine.calculate(nskReq) as AynvoraResult.Success).value.profile
        assertNull(nskProfile.loShu)
        assertNotNull(nskProfile.nineStarKi)
        assertEquals(4, nskProfile.nineStarKi?.principalStar?.number)
    }

    @Test
    fun testTarotBirthCardVsStandardNumerologyDifference() {
        // Birth date: 15-11-1954
        // Pythagorean Life Path: 11(2) + 15(6) + 1954(1) = 9
        val pythReq =
            NumerologyRequest(15, 11, 1954, rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id)
        val pythProfile =
            (NumerologyCalculationEngine.calculate(pythReq) as AynvoraResult.Success).value.profile
        assertEquals(9, pythProfile.destiny?.destinyValue)
        assertNull(pythProfile.tarotBirthCard)

        // Tarot Birth Card (Greer 1984): 11 + 15 + 1954 = 1980 -> 18 (The Moon) / 9 (The Hermit)
        val tarotReq =
            NumerologyRequest(15, 11, 1954, rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id)
        val tarotProfile =
            (NumerologyCalculationEngine.calculate(tarotReq) as AynvoraResult.Success).value.profile
        assertNull(tarotProfile.destiny)
        assertNotNull(tarotProfile.tarotBirthCard)
        assertEquals(18, tarotProfile.tarotBirthCard?.personalityCardNumber)
        assertEquals("The Moon", tarotProfile.tarotBirthCard?.personalityCardName)
        assertEquals(9, tarotProfile.tarotBirthCard?.soulCardNumber)
        assertEquals("The Hermit", tarotProfile.tarotBirthCard?.soulCardName)
    }

    @Test
    fun testTraceRulesetIntegrityAllTwelveRulesets() {
        val rulesetsWithInputs = listOf(
            Pair(NumerologyRuleset.CHALDEAN_CHEIRO_V1, "ISHANT"),
            Pair(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1, "ISHANT"),
            Pair(NumerologyRuleset.INDIAN_ANK_JYOTISH_V1, "ISHANT"),
            Pair(NumerologyRuleset.LO_SHU_CLASSICAL_V1, null),
            Pair(NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1, "שלום"),
            Pair(NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1, "سلام"),
            Pair(NumerologyRuleset.AGRIPPAN_OCCULT_V1, "AGRIPPA"),
            Pair(NumerologyRuleset.HEBREW_MISPAR_GADOL_V1, "שלום"),
            Pair(NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1, "سلام"),
            Pair(NumerologyRuleset.INDIAN_KATAPAYADI_V1, "खगो"),
            Pair(NumerologyRuleset.CHINESE_NINE_STAR_KI_V1, null),
            Pair(NumerologyRuleset.TAROT_BIRTH_CARD_V1, null),
        )

        for ((ruleset, name) in rulesetsWithInputs) {
            val req = NumerologyRequest(testDay, testMonth, testYear, name, rulesetId = ruleset.id)
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(
                result is AynvoraResult.Success,
                "Ruleset ${ruleset.id} calculation failed: ${(result as? AynvoraResult.Failure)?.message}"
            )
            val profile = result.value.profile

            assertEquals(ruleset.id, profile.rulesetId)
            for ((type, trace) in profile.calculationTraces) {
                assertEquals(ruleset.id, trace.rulesetId)
                assertEquals(type, trace.calculationType)
                assertTrue(trace.reductionSteps.isNotEmpty())
            }
        }
    }
}

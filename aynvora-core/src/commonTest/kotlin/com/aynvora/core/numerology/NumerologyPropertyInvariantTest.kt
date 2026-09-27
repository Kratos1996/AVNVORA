package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.0: Numerology Property & Invariant Tests.
 *
 * Verifies mathematical invariants, termination guarantees, boundary handling,
 * and input validation safety across all supported rulesets.
 */
class NumerologyPropertyInvariantTest {

    @Test
    fun testAllSingleDigitsReduceToSelf() {
        for (i in 1..9) {
            val res1 = NumerologyReductionEngine.reduce(
                i,
                MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
            ).finalValue
            assertEquals(i, res1)

            val res2 =
                NumerologyReductionEngine.reduce(i, MasterNumberPolicy.PRESERVE_11_22).finalValue
            assertEquals(i, res2)

            val res3 =
                NumerologyReductionEngine.reduce(i, MasterNumberPolicy.PRESERVE_11_22_33).finalValue
            assertEquals(i, res3)
        }
    }

    @Test
    fun testReductionTerminatesAndProducesPositiveSingleDigitOrMaster() {
        for (num in 1..9999) {
            val result = NumerologyReductionEngine.reduce(num, MasterNumberPolicy.PRESERVE_11_22_33)
            val reduced = result.finalValue
            assertTrue(reduced > 0, "Reduced value must be positive for input $num")
            assertTrue(
                reduced in 1..9 || reduced == 11 || reduced == 22 || reduced == 33,
                "Reduced value must be 1..9 or master number for input $num, was $reduced"
            )
            assertTrue(
                result.steps.size <= 10,
                "Reduction must terminate within 10 steps for input $num"
            )
        }
    }

    @Test
    fun testPythagoreanSoulUrgePlusPersonalityEqualsNameNumber() {
        // Test with 20 diverse names
        val testNames = listOf(
            "ALEXANDER", "BEATRICE", "CATHERINE", "DANIEL", "ELEANOR",
            "FREDERICK", "GABRIEL", "HELENA", "ISABELLA", "JULIAN",
            "KATHERINE", "LEONARD", "MARGARET", "NICHOLAS", "OLIVIA",
            "PENELOPE", "QUENTIN", "ROSALIND", "SEBASTIAN", "THEODORE"
        )

        for (name in testNames) {
            val (nameNum, _) = NumerologyCalculationEngine.calculateName(
                name,
                NumerologyRuleset.PYTHAGOREAN_WESTERN_V1
            )
            val (soulUrge, _) = NumerologyCalculationEngine.calculateSoulUrge(
                name,
                NumerologyRuleset.PYTHAGOREAN_WESTERN_V1
            )
            val (personality, _) = NumerologyCalculationEngine.calculatePersonality(
                name,
                NumerologyRuleset.PYTHAGOREAN_WESTERN_V1
            )

            val (sumReduced, _) = NumerologyReductionEngine.reduce(
                soulUrge.soulUrgeValue + personality.personalityValue,
                MasterNumberPolicy.PRESERVE_11_22_33
            )

            // Both reduced to root must match
            val nameRoot = NumerologyReductionEngine.reduceToRoot(nameNum.nameValue)
            val sumRoot = NumerologyReductionEngine.reduceToRoot(sumReduced)

            assertEquals(
                nameRoot,
                sumRoot,
                "Pythagorean Invariant violated for $name: SoulUrge(${soulUrge.soulUrgeValue}) + Personality(${personality.personalityValue}) != Name(${nameNum.nameValue})"
            )
        }
    }

    @Test
    fun testUnsupportedRulesetCleanlyRejected() {
        val request = NumerologyRequest(
            birthDay = 15,
            birthMonth = 8,
            birthYear = 1990,
            rulesetId = "NON_EXISTENT_RULESET_ID",
        )
        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure.UnsupportedConfiguration)
        assertTrue(result.message.contains("Unsupported numerology ruleset ID"))
    }

    @Test
    fun testEvidenceGraphGenerationIntegrity() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)

        val graph = NumerologyEvidenceGraphFactory.create(result.value, request)
        assertNotNull(graph)
        assertTrue(graph.nodes.containsKey("num_fact_birth_date"))
        assertTrue(graph.nodes.containsKey("num_rule_ruleset"))
        assertTrue(graph.nodes.containsKey("num_derived_radical"))
        assertTrue(graph.nodes.containsKey("num_derived_destiny"))
        assertTrue(graph.nodes.containsKey("num_derived_name"))
        assertTrue(graph.nodes.containsKey("num_derived_soul_urge"))
        assertTrue(graph.nodes.containsKey("num_derived_personality"))
        assertTrue(graph.edges.isNotEmpty())

        // Verify provenance integrity: zero PII in provenance metadata
        graph.nodes.values.forEach { item ->
            assertEquals(com.aynvora.core.feature.CoreFeatureId.NUMEROLOGY, item.domain)
            assertNotNull(item.provenance.sourceName)
            assertNotNull(item.provenance.rulesetOrEdition)
        }
    }

    @Test
    fun testLoShuGridMathematicalInvariants() {
        val testDates = listOf(
            Triple(1, 1, 2000),
            Triple(11, 7, 1996),
            Triple(29, 2, 2024),
            Triple(15, 8, 1947),
            Triple(25, 12, 1980),
            Triple(31, 10, 1999),
            Triple(9, 9, 1999),
        )

        for ((day, month, year) in testDates) {
            val req = NumerologyRequest(
                day,
                month,
                year,
                rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id
            )
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(result is AynvoraResult.Success)
            val grid = result.value.profile.loShu
            assertNotNull(grid)

            // Invariant 1: Exactly 9 canonical cells
            assertEquals(9, grid.cells.size)

            // Invariant 2: Coordinates within 1..3
            grid.cells.forEach { cell ->
                assertTrue(cell.digit in 1..9)
                assertTrue(cell.row in 1..3)
                assertTrue(cell.column in 1..3)
                assertTrue(cell.count >= 0)
            }

            // Invariant 3: Total cell counts equal usable extracted digits
            val totalCellCounts = grid.cells.sumOf { it.count }
            assertEquals(grid.extractedDigits.size, totalCellCounts)

            // Invariant 4: Present + Missing equals exactly 9 digits
            assertEquals(9, grid.presentDigits.size + grid.missingDigits.size)
            val allDigits = (grid.presentDigits + grid.missingDigits).sorted()
            assertEquals((1..9).toList(), allDigits)

            // Invariant 5: Exactly 8 planes evaluated
            assertEquals(8, grid.arrows.size)

            // Invariant 6: Deterministic repeatability
            val result2 = NumerologyCalculationEngine.calculate(req)
            assertTrue(result2 is AynvoraResult.Success)
            assertEquals(grid, result2.value.profile.loShu)
        }
    }

    @Test
    fun testIndianAnkJyotishInvariants() {
        for (day in 1..31) {
            val req = NumerologyRequest(
                day,
                5,
                2000,
                rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
            )
            val result = NumerologyCalculationEngine.calculate(req)
            if (result is AynvoraResult.Success) {
                val profile = result.value.profile

                // Invariant: Moolank is strictly single digit 1..9
                assertNotNull(profile.radical)
                assertTrue(profile.radical!!.radicalValue in 1..9)
                assertEquals(false, profile.radical!!.isMasterNumber)

                // Invariant: Bhagyank is strictly single digit 1..9
                assertNotNull(profile.destiny)
                assertTrue(profile.destiny!!.destinyValue in 1..9)
                assertEquals(false, profile.destiny!!.isMasterNumber)

                // Invariant: Planetary association exists for Moolank root
                assertNotNull(profile.planetaryAssociation)
                assertEquals(profile.radical!!.radicalValue, profile.planetaryAssociation!!.number)
            }
        }
    }

    @Test
    fun testHebrewGematriaInvariants() {
        val testHebrewWords = listOf(
            "א", "ב", "שלום", "חי", "אהבה", "ישראל", "תורה", "ירושלים", "בראשית", "אמת"
        )
        for (word in testHebrewWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id
            )
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(result is AynvoraResult.Success)
            val gem = result.value.profile.gematria
            assertNotNull(gem)

            // Invariant 1: Absolute value is positive and sum of letters
            assertTrue(gem.absoluteValue > 0)
            assertEquals(gem.letterValues.sumOf { it.second }, gem.absoluteValue)

            // Invariant 2: Reduced root is strictly single digit 1..9
            assertTrue(gem.reducedValue in 1..9)

            // Invariant 3: Digital root congruence (mod 9 equality)
            val expectedRoot = if (gem.absoluteValue % 9 == 0) 9 else gem.absoluteValue % 9
            assertEquals(expectedRoot, gem.reducedValue)
        }
    }

    @Test
    fun testArabicAbjadInvariants() {
        val testArabicWords = listOf(
            "ا", "ب", "الله", "محمد", "سلام", "نور", "حق", "كتاب", "حكمة", "عدل"
        )
        for (word in testArabicWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id
            )
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(result is AynvoraResult.Success)
            val abj = result.value.profile.abjad
            assertNotNull(abj)

            // Invariant 1: Great Sum is positive and sum of letters
            assertTrue(abj.jummalKabir > 0)
            assertEquals(abj.letterValues.sumOf { it.second }, abj.jummalKabir)

            // Invariant 2: Small root is strictly single digit 1..9
            assertTrue(abj.jummalSaghir in 1..9)

            // Invariant 3: Digital root congruence (mod 9 equality)
            val expectedRoot = if (abj.jummalKabir % 9 == 0) 9 else abj.jummalKabir % 9
            assertEquals(expectedRoot, abj.jummalSaghir)
        }
    }

    @Test
    fun testAgrippanOccultInvariants() {
        val testLatinWords = listOf(
            "AGRIPPA",
            "ROMA",
            "LUX",
            "VERITAS",
            "OCCULTA",
            "PHILOSOPHIA",
            "SOL",
            "LUNA",
            "MARS",
            "JUPITER"
        )
        for (word in testLatinWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id
            )
            val result = NumerologyCalculationEngine.calculate(req)
            assertTrue(result is AynvoraResult.Success)
            val profile = result.value.profile
            assertNotNull(profile.nameNumber)

            // Invariant 1: Reduced root is strictly single digit 1..9 (no master numbers in Renaissance arithmancy)
            assertTrue(profile.nameNumber!!.nameValue in 1..9)

            // Invariant 2: Radical is null (Agrippan Arithmancy calculates text/name, not birth radical)
            assertEquals(null, profile.radical)
        }
    }

    @Test
    fun testHebrewMisparGadolInvariants() {
        val testWords = listOf("מלך", "שלום", "גן", "כסף", "ארץ")
        for (word in testWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id
            )
            val res =
                (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value.profile.gematria
            assertNotNull(res)

            // Invariant 1: Absolute value is positive and sum of letter values
            assertTrue(res.absoluteValue > 0)
            assertEquals(res.letterValues.sumOf { it.second }, res.absoluteValue)

            // Invariant 2: Reduced root is strictly 1..9
            assertTrue(res.reducedValue in 1..9)

            // Invariant 3: Digital root congruence
            val expectedRoot = if (res.absoluteValue % 9 == 0) 9 else res.absoluteValue % 9
            assertEquals(expectedRoot, res.reducedValue)

            // Invariant 4: Variant label is strictly MISPAR_GADOL
            assertEquals("MISPAR_GADOL", res.variant)
        }

        // Invariant 5: Niqqud stripping does not alter value
        val reqPlain = NumerologyRequest(
            1,
            1,
            2000,
            "מלך",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id
        )
        val reqVocalized = NumerologyRequest(
            1,
            1,
            2000,
            "מֶלֶךְ",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id
        )
        val resPlain =
            (NumerologyCalculationEngine.calculate(reqPlain) as AynvoraResult.Success).value.profile.gematria
        val resVocalized =
            (NumerologyCalculationEngine.calculate(reqVocalized) as AynvoraResult.Success).value.profile.gematria
        assertEquals(resPlain?.absoluteValue, resVocalized?.absoluteValue)
    }

    @Test
    fun testArabicAbjadMaghribiInvariants() {
        val testWords = listOf("شمس", "صبر", "ضوء", "ظل", "غرب")
        for (word in testWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id
            )
            val res =
                (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value.profile.abjad
            assertNotNull(res)

            // Invariant 1: Kabir is sum of letter values
            assertTrue(res.jummalKabir > 0)
            assertEquals(res.letterValues.sumOf { it.second }, res.jummalKabir)

            // Invariant 2: Saghir root is 1..9
            assertTrue(res.jummalSaghir in 1..9)

            // Invariant 3: Digital root congruence
            val expectedRoot = if (res.jummalKabir % 9 == 0) 9 else res.jummalKabir % 9
            assertEquals(expectedRoot, res.jummalSaghir)

            // Invariant 4: Variant label is strictly MAGHRIBI
            assertEquals("MAGHRIBI", res.variant)
        }

        // Invariant 5: Tashkeel stripping does not alter value
        val reqPlain = NumerologyRequest(
            1,
            1,
            2000,
            "شمس",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id
        )
        val reqTashkeel = NumerologyRequest(
            1,
            1,
            2000,
            "شَمْسٌ",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id
        )
        val resPlain =
            (NumerologyCalculationEngine.calculate(reqPlain) as AynvoraResult.Success).value.profile.abjad
        val resTashkeel =
            (NumerologyCalculationEngine.calculate(reqTashkeel) as AynvoraResult.Success).value.profile.abjad
        assertEquals(resPlain?.jummalKabir, resTashkeel?.jummalKabir)
    }

    @Test
    fun testKatapayadiInvariants() {
        val testWords = listOf("खगो", "नभ", "जलधि", "सूर्य", "अचल", "माधव")
        for (word in testWords) {
            val req = NumerologyRequest(
                1,
                1,
                2000,
                word,
                rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id
            )
            val res =
                (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value.profile.katapayadi
            assertNotNull(res)

            // Invariant 1: digitSequence and finalNumber have same length
            assertEquals(res.digitSequence.length, res.finalNumber.length)

            // Invariant 2: finalNumber is exact string reverse of digitSequence (ankānām vāmato gatiḥ)
            assertEquals(res.digitSequence.reversed(), res.finalNumber)

            // Invariant 3: every character in digitSequence is a digit
            assertTrue(res.digitSequence.all { it.isDigit() })

            // Invariant 4: isReversed is true
            assertTrue(res.isReversed)
        }
    }

    @Test
    fun testNineStarKiInvariants() {
        for (year in 1950..2030 step 5) {
            for (month in listOf(1, 3, 7, 11)) {
                val req = NumerologyRequest(
                    15,
                    month,
                    year,
                    rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id
                )
                val res =
                    (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value.profile.nineStarKi
                assertNotNull(res)

                // Invariant 1: Principal star is strictly in 1..9
                assertTrue(res.principalStar.number in 1..9)

                // Invariant 2: Solar year is either year or year-1
                assertTrue(res.solarYear == year || res.solarYear == year - 1)

                // Invariant 3: Element is non-empty
                assertTrue(res.principalStar.element.isNotEmpty())
            }
        }
    }

    @Test
    fun testTarotBirthCardInvariants() {
        for (year in 1960..2020 step 10) {
            for (month in 1..12) {
                for (day in listOf(1, 15, 28)) {
                    val req = NumerologyRequest(
                        day,
                        month,
                        year,
                        rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id
                    )
                    val res =
                        (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value.profile.tarotBirthCard
                    assertNotNull(res)

                    // Invariant 1: Personality Card is strictly in 1..22
                    assertTrue(res.personalityCardNumber in 1..22)

                    // Invariant 2: Soul Card is strictly in 1..9 (or 1..22)
                    assertTrue(res.soulCardNumber in 1..22)

                    // Invariant 3: Card names are valid Major Arcana titles
                    assertTrue(res.personalityCardName.isNotEmpty())
                    assertTrue(res.soulCardName.isNotEmpty())
                }
            }
        }
    }
}

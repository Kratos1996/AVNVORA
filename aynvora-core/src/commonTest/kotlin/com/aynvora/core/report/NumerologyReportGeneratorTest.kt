package com.aynvora.core.report

import com.aynvora.core.numerology.NumerologyCalculationEngine
import com.aynvora.core.numerology.NumerologyRequest
import com.aynvora.core.numerology.NumerologyRuleset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NumerologyReportGeneratorTest {

    private val generator = NumerologyReportGenerator()

    private val testResolver = object : ReportTextResolver {
        override val language: ReportLanguage = ReportLanguage.ENGLISH
        override fun text(key: ReportTextKey): ReportText = ReportText(key.key, key.key)
        override fun rawText(key: String, defaultText: String): String = defaultText
        override fun bodyName(body: com.aynvora.core.models.CelestialBody): String = body.name
        override fun signName(sign: com.aynvora.core.models.Rashi): String = sign.name
        override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String =
            nakshatra.name

        override fun enumLabel(identifier: String): String = identifier
        override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String = tithi.name
        override fun varaName(vara: com.aynvora.astro.panchang.Vara): String = vara.name
        override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String = yoga.name
        override fun karanaName(karana: com.aynvora.astro.panchang.Karana): String = karana.name
        override fun number(value: Double, decimalPlaces: Int): String = value.toString()
        override fun birthDate(year: Int, month: Int, day: Int): String = "$year-$month-$day"
        override fun birthTime(hour: Int, minute: Int, second: Int): String =
            "$hour:$minute:$second"

        override fun generatedAtUtc(epochMillis: Long): String = epochMillis.toString()
    }

    @Test
    fun testGeneratePythagoreanReport() {
        val req = NumerologyRequest(
            11,
            7,
            1996,
            "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)

        assertNotNull(doc)
        assertEquals(ReportType.NUMEROLOGY.id, doc.metadata.reportTypeId)
        assertTrue(doc.sections.any { it.id == "disclaimer" })
        assertTrue(doc.sections.any { it.id == "tradition_overview" })
        assertTrue(doc.sections.any { it.id == "calculated_values" })
        assertTrue(doc.sections.any { it.id == "calculation_traces" })
        assertTrue(doc.sections.any { it.id == "interpretations" })
    }

    @Test
    fun testGenerateChaldeanReport() {
        val req = NumerologyRequest(
            11,
            7,
            1996,
            "ISHANT",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateIndianReport() {
        val req = NumerologyRequest(
            11,
            7,
            1996,
            "ISHANT",
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateLoShuReport() {
        val req =
            NumerologyRequest(11, 7, 1996, rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id)
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        val valuesSection = doc.sections.first { it.id == "calculated_values" }
        assertTrue(valuesSection.blocks.isNotEmpty())
    }

    @Test
    fun testGenerateGematriaReport() {
        val req = NumerologyRequest(
            1,
            1,
            2000,
            "שלום",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateAbjadReport() {
        val req = NumerologyRequest(
            1,
            1,
            2000,
            "بسم الله",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateKatapayadiReport() {
        val req = NumerologyRequest(
            1,
            1,
            2000,
            "गोपीभाग्यमधुव्रात",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id
        )
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateNineStarKiReport() {
        val req =
            NumerologyRequest(11, 7, 1996, rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id)
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }

    @Test
    fun testGenerateTarotBirthCardsReport() {
        val req =
            NumerologyRequest(11, 7, 1996, rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id)
        val result =
            (NumerologyCalculationEngine.calculate(req) as com.aynvora.core.result.AynvoraResult.Success).value

        val input = NumerologyReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1727400000000L,
            result = result,
        )
        val doc = generator.generate(input, testResolver)
        assertNotNull(doc)
        assertTrue(doc.sections.any { it.id == "calculated_values" })
    }
}

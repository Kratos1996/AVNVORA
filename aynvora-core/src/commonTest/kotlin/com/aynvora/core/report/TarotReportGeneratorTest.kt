package com.aynvora.core.report

import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpread
import com.aynvora.core.tarot.TarotSpreadPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

/**
 * Unit tests for [TarotReportGenerator].
 *
 * Verifies:
 * 1. Single-card reading generates valid 4-section document (disclaimer + overview + reflection + attribution).
 * 2. Three-card reading generates 6-section document (disclaimer + overview + 3 reflections + attribution).
 * 3. Empty draws are rejected.
 * 4. Language mismatch is rejected.
 * 5. Report explicitly includes non-predictive disclaimer.
 * 6. Each card reflection section uses approved content meanings.
 * 7. Missing content is handled gracefully (placeholder shown).
 */
class TarotReportGeneratorTest {

    private val generator = TarotReportGenerator()

    // ── Sample cards & content ───────────────────────────────────────────────

    private val fool =
        TarotCard(id = "major_00_fool", number = 0, name = "The Fool", arcana = TarotArcana.MAJOR)
    private val highPriestess = TarotCard(
        id = "major_02_high_priestess",
        number = 2,
        name = "The High Priestess",
        arcana = TarotArcana.MAJOR
    )
    private val emperor = TarotCard(
        id = "major_04_emperor",
        number = 4,
        name = "The Emperor",
        arcana = TarotArcana.MAJOR
    )

    private val foolContent = TarotCardContent(
        cardId = "major_00_fool",
        language = "en",
        title = "The Fool",
        shortDescription = "A leap of faith and new beginnings.",
        keywords = listOf("Beginning", "Spontaneity", "Freedom"),
        uprightMeaning = "New beginnings, optimism, and the courage to step into the unknown.",
        reversedMeaning = "A moment of pause, reconsideration, or ungrounded choices.",
        sourceAttribution = "AYNVORA Contemplative Traditions Archive (Public Domain)",
        contentVersion = 1,
    )

    private val highPriestessContent = TarotCardContent(
        cardId = "major_02_high_priestess",
        language = "en",
        title = "The High Priestess",
        shortDescription = "Inner knowing and divine feminine wisdom.",
        keywords = listOf("Intuition", "Silence", "Mystery"),
        uprightMeaning = "Trust your intuition; knowledge lives within.",
        reversedMeaning = "Suppressed intuition or secrets coming to light.",
        sourceAttribution = "AYNVORA Contemplative Traditions Archive (Public Domain)",
        contentVersion = 1,
    )

    private val emperorContent = TarotCardContent(
        cardId = "major_04_emperor",
        language = "en",
        title = "The Emperor",
        shortDescription = "Authority, structure, and the power of intention.",
        keywords = listOf("Structure", "Authority", "Stability"),
        uprightMeaning = "Take charge with clarity and intentional action.",
        reversedMeaning = "Reflect on where rigidity may be limiting growth.",
        sourceAttribution = "AYNVORA Contemplative Traditions Archive (Public Domain)",
        contentVersion = 1,
    )

    private val singlePosition = TarotSpreadPosition(
        id = "single_focus",
        orderIndex = 0,
        name = "Current Focus",
        description = "Theme or perspective for present reflection.",
    )

    private val testResolver = object : ReportTextResolver {
        override val language = ReportLanguage.ENGLISH
        override fun text(key: ReportTextKey) = ReportText(key.key, key.key)
        override fun bodyName(body: com.aynvora.core.models.CelestialBody) = body.name
        override fun signName(sign: com.aynvora.core.models.Rashi) = sign.name
        override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra) = nakshatra.name
        override fun enumLabel(identifier: String) = identifier
        override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi) = tithi.name
        override fun varaName(vara: com.aynvora.astro.panchang.Vara) = vara.name
        override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga) = yoga.name
        override fun karanaName(karana: com.aynvora.astro.panchang.Karana) = karana.name
        override fun number(value: Double, decimalPlaces: Int) = value.toString()
        override fun birthDate(year: Int, month: Int, day: Int) = "$year-$month-$day"
        override fun birthTime(hour: Int, minute: Int, second: Int) = "$hour:$minute:$second"
        override fun generatedAtUtc(epochMillis: Long) = epochMillis.toString()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun singleCardReading() = TarotReading(
        id = "test_single_001",
        spreadId = TarotSpread.SingleCard.id,
        deckId = "rider_waite_smith_standard",
        draws = listOf(
            TarotCardDraw(
                card = fool,
                orientation = TarotCardOrientation.UPRIGHT,
                position = singlePosition,
            )
        ),
        timestampEpochMs = 1_000_000L,
    )

    private fun singleCardInput(reading: TarotReading = singleCardReading()) = TarotReportInput(
        language = ReportLanguage.ENGLISH,
        generatedAtEpochMs = 1_000_000L,
        reading = reading,
        cardContents = listOf(foolContent),
        spread = TarotSpread.SingleCard,
    )

    private fun threeCardReading() = TarotReading(
        id = "test_three_001",
        spreadId = TarotSpread.ThreeCardTimeline.id,
        deckId = "rider_waite_smith_standard",
        draws = listOf(
            TarotCardDraw(
                fool,
                TarotCardOrientation.UPRIGHT,
                TarotSpread.ThreeCardTimeline.positions[0]
            ),
            TarotCardDraw(
                highPriestess,
                TarotCardOrientation.REVERSED,
                TarotSpread.ThreeCardTimeline.positions[1]
            ),
            TarotCardDraw(
                emperor,
                TarotCardOrientation.UPRIGHT,
                TarotSpread.ThreeCardTimeline.positions[2]
            ),
        ),
        timestampEpochMs = 2_000_000L,
    )

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    fun singleCardReadingGenerates4Sections() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        // Expect: disclaimer + spread_overview + tarot_card_0 + attribution = 4 sections
        assertEquals(4, doc.sections.size, "Single card report should have 4 sections")
    }

    @Test
    fun threeCardReadingGenerates6Sections() {
        val reading = threeCardReading()
        val input = TarotReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 2_000_000L,
            reading = reading,
            cardContents = listOf(foolContent, highPriestessContent, emperorContent),
            spread = TarotSpread.ThreeCardTimeline,
        )
        val doc = generator.generate(input, testResolver)

        // Expect: disclaimer + spread_overview + 3 card sections + attribution = 6 sections
        assertEquals(6, doc.sections.size, "Three-card report should have 6 sections")
    }

    @Test
    fun reportMetadataMatchesInput() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        assertEquals(ReportType.TAROT.id, doc.metadata.reportTypeId)
        assertEquals(ReportLanguage.ENGLISH, doc.metadata.language)
        assertEquals(ReportFeatureStatus.IMPLEMENTED, doc.metadata.featureStatus)
        assertTrue(
            doc.metadata.reportId.contains("test_single_001"),
            "Report ID should contain reading ID"
        )
    }

    @Test
    fun disclaimerSectionIsFirstAndContainsDisclaimerKind() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        val disclaimerSection = doc.sections.first()
        assertEquals("tarot_disclaimer", disclaimerSection.id)
        val disclaimerBlocks = disclaimerSection.blocks.filterIsInstance<ReportParagraph>()
            .filter { it.kind == ReportContentKind.DISCLAIMER }
        assertTrue(
            disclaimerBlocks.isNotEmpty(),
            "Disclaimer section must contain a DISCLAIMER block"
        )
    }

    @Test
    fun cardReflectionSectionIncludesUprightMeaningForUprightCard() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        val cardSection = doc.sections.first { it.id == "tarot_card_0" }
        val interpretationBlock =
            cardSection.blocks.filterIsInstance<ReportInterpretation>().first()
        assertEquals(foolContent.uprightMeaning, interpretationBlock.text.value)
    }

    @Test
    fun cardReflectionSectionIncludesReversedMeaningForReversedCard() {
        val reversedReading = TarotReading(
            id = "test_reversed_001",
            spreadId = TarotSpread.SingleCard.id,
            deckId = "rider_waite_smith_standard",
            draws = listOf(
                TarotCardDraw(fool, TarotCardOrientation.REVERSED, singlePosition)
            ),
            timestampEpochMs = 1_000_000L,
        )
        val input = TarotReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1_000_000L,
            reading = reversedReading,
            cardContents = listOf(foolContent),
            spread = TarotSpread.SingleCard,
        )
        val doc = generator.generate(input, testResolver)

        val cardSection = doc.sections.first { it.id == "tarot_card_0" }
        val interpretationBlock =
            cardSection.blocks.filterIsInstance<ReportInterpretation>().first()
        assertEquals(foolContent.reversedMeaning, interpretationBlock.text.value)
    }

    @Test
    fun missingCardContentProducesPlaceholderBlock() {
        val reading = singleCardReading()
        val input = TarotReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1_000_000L,
            reading = reading,
            cardContents = emptyList(), // No content provided
            spread = TarotSpread.SingleCard,
        )
        val doc = generator.generate(input, testResolver)

        val cardSection = doc.sections.first { it.id == "tarot_card_0" }
        val disclaimerBlocks = cardSection.blocks.filterIsInstance<ReportParagraph>()
            .filter { it.kind == ReportContentKind.DISCLAIMER }
        assertTrue(
            disclaimerBlocks.isNotEmpty(),
            "Missing content should produce a DISCLAIMER placeholder block"
        )
    }

    @Test
    fun emptyDrawsIsRejected() {
        assertFails {
            TarotReportInput(
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1_000_000L,
                reading = TarotReading(
                    id = "empty",
                    spreadId = "single_card",
                    deckId = "rider_waite_smith_standard",
                    draws = emptyList(),
                    timestampEpochMs = 0L,
                ),
                cardContents = emptyList(),
                spread = TarotSpread.SingleCard,
            )
        }
    }

    @Test
    fun languageMismatchIsRejected() {
        val input = singleCardInput()
        val hiResolver = object : ReportTextResolver by testResolver {
            override val language = ReportLanguage.HINDI
        }
        assertFails {
            generator.generate(input, hiResolver) // resolver=HINDI, input=ENGLISH → must fail
        }
    }

    @Test
    fun attributionSectionIsLastAndContainsSourceTable() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        val attributionSection = doc.sections.last()
        assertEquals("tarot_attribution", attributionSection.id)
        val tables = attributionSection.blocks.filterIsInstance<ReportTable>()
        assertTrue(tables.isNotEmpty(), "Attribution section must contain a source table")
    }

    @Test
    fun spreadOverviewContainsDrawTable() {
        val input = singleCardInput()
        val doc = generator.generate(input, testResolver)

        val overviewSection = doc.sections.first { it.id == "tarot_spread_overview" }
        val tables = overviewSection.blocks.filterIsInstance<ReportTable>()
        assertTrue(tables.isNotEmpty(), "Spread overview must contain a table of drawn cards")
        val drawTable = tables.first()
        assertEquals(1, drawTable.rows.size, "Single card draw should have 1 row")
        assertEquals("Current Focus", drawTable.rows[0][0])
        assertEquals(foolContent.title, drawTable.rows[0][1])
    }

    @Test
    fun reportTypeIsCorrect() {
        assertEquals(ReportType.TAROT, generator.reportType)
    }
}

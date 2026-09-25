package com.aynvora.data.garudapuran

import com.aynvora.core.garudapuran.GarudaSourceComparison
import com.aynvora.core.garudapuran.GarudaSourceVariantType

/**
 * Editorial comparison registry documenting textual, structural, and recension variants
 * between Wood 1911 (Saroddhara / Pretakalpa), Dutt 1908 (Purvakhanda), and Gita Press 1416.
 *
 * Rules:
 * - Differences preserve both source references.
 * - Neither source is automatically asserted as the sole correct reading.
 * - Categorized by EXACT_MATCH, SOURCE_VARIANT, EDITION_VARIANT, TEXT_UNCERTAIN.
 */
object GarudaSourceComparisonRegistry {

    val comparisons: List<GarudaSourceComparison> = listOf(
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_RECENSION_SCOPE",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 1,
            primaryVerse = 1,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_DUTT_1908_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_DUTT_1908_ID,
            secondaryChapter = 1,
            secondaryVerse = 1,
            variantType = GarudaSourceVariantType.SOURCE_VARIANT,
            comparisonNotes = "Recension scope difference: Wood 1911 translates Navanidhirama's 16-chapter Saroddhara (extracted Pretakalpa essence focusing on death, rites, and liberation). Dutt 1908 translates the encyclopedic Purvakhanda (cosmology, medicine, gems, astronomy, and dharma). Both are genuine historical traditions with distinct textual scopes.",
        ),
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_CH16_PHALA_SHRUTI",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 16,
            primaryVerse = 117,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_2_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_2_ID,
            secondaryChapter = 17,
            secondaryVerse = 1,
            variantType = GarudaSourceVariantType.EDITION_VARIANT,
            comparisonNotes = "Chapter division variant: Wood 1911 contains exactly 16 chapters, integrating the Phala-shruti conclusion directly into Chapter 16 (vv. 115–120: 'Thus in sixteen chapters I have related to you the extracted essence of all the scriptures'). Gita Press (Code 1416) segregates the Phala-shruti into a 17th chapter. The AYNVORA approved package adheres to the Wood 1911 16-chapter structure without synthetic chapter fabrication.",
        ),
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_NAIMISHA_INVOCATION",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 1,
            primaryVerse = 2,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_DUTT_1908_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_DUTT_1908_ID,
            secondaryChapter = 1,
            secondaryVerse = 2,
            variantType = GarudaSourceVariantType.EXACT_MATCH,
            comparisonNotes = "Thematic agreement: Both Wood 1911 and Dutt 1908 open in the sacred forest of Naimisha with Shaunaka and other rishis questioning Suta during a protracted Vedic sacrifice.",
        ),
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_CHITRAGUPTA_SRAVANAS",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 3,
            primaryVerse = 8,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_DUTT_1908_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_DUTT_1908_ID,
            secondaryChapter = 227,
            secondaryVerse = 15,
            variantType = GarudaSourceVariantType.EXACT_MATCH,
            comparisonNotes = "Thematic agreement: Both traditions document Chitragupta recording deeds with the assistance of the Sravanas (sons of Brahma who observe human actions across worlds).",
        ),
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_VAITARANI_METAPHOR",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 2,
            primaryVerse = 15,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_DUTT_1908_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_DUTT_1908_ID,
            secondaryChapter = 226,
            secondaryVerse = 10,
            variantType = GarudaSourceVariantType.SOURCE_VARIANT,
            comparisonNotes = "Textual variant: Wood 1911 describes River Vaitarani as spanning 100 yojanas with an etymological note on 'vitarana' (charity/gift enabling passage). Dutt 1908 provides a broader geographic and moral description without identical lexical verse structure.",
        ),
        GarudaSourceComparison(
            canonicalReferenceId = "GP_COMPARE_SANSKRIT_OCR_READING",
            primarySourceId = InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            primaryEditionId = InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            primaryChapter = 1,
            primaryVerse = 1,
            secondarySourceId = InspectedGarudaPuranSources.SOURCE_GRETIL_ID,
            secondaryEditionId = InspectedGarudaPuranSources.EDITION_GRETIL_ID,
            secondaryChapter = 1,
            secondaryVerse = 1,
            variantType = GarudaSourceVariantType.TEXT_UNCERTAIN,
            comparisonNotes = "Text uncertain / OCR divergence: The 1911 printed Sanskrit text in Panini Office scan has minor broken conjuncts in Devanagari OCR; GRETIL scholarly e-text provides clean classical Sanskrit reading for textual cross-verification.",
        ),
    )

    fun getComparisonsForChapter(chapterNumber: Int): List<GarudaSourceComparison> =
        comparisons.filter { it.primaryChapter == chapterNumber }

    fun getComparison(canonicalReferenceId: String): GarudaSourceComparison? =
        comparisons.firstOrNull { it.canonicalReferenceId == canonicalReferenceId }
}

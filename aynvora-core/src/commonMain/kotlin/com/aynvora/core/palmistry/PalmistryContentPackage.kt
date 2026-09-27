package com.aynvora.core.palmistry

import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.FallbackLocalizationProvider
import com.aynvora.core.localization.LocalizationProvider
import com.aynvora.core.localization.RawLocalizationKey

/**
 * Approved traditional Hastrekha content package.
 *
 * Grounded in classical Samudrika Shastra texts (Brihat Samhita, Hastasanjeevani).
 * Strictly non-fatalistic, non-medical, and contemplative.
 *
 * Uses key-driven localization: identical keys resolve across English, Hindi, Arabic, and Indian languages.
 */
object PalmistryContentPackage {

    const val CONTENT_VERSION = "1.0.0"
    const val SOURCE_SAMUDRIKA = "Samudrika Shastra (Hastrekha Prakarana)"

    /**
     * Resolves traditional meanings for findings using the unified [LocalizationProvider].
     */
    fun getMeaningsForFindings(
        finding: PalmFinding,
        localization: LocalizationProvider,
    ): List<PalmistryMeaning> {
        val results = mutableListOf<PalmistryMeaning>()

        // 1. Palm Shape Meaning
        results.add(getPalmShapeMeaning(finding.shape, localization))

        // 2. Line Meanings for Detected Lines
        finding.lines.filter { it.detected }.forEach { line ->
            val meaning = getLineMeaning(line, localization)
            if (meaning != null) {
                results.add(meaning)
            }
        }

        return results
    }

    /**
     * Backward-compatible overload accepting a language code.
     */
    fun getMeaningsForFindings(
        finding: PalmFinding,
        language: String = "en",
    ): List<PalmistryMeaning> {
        val locale = AynvoraLocale.fromId(language)
        return getMeaningsForFindings(finding, FallbackLocalizationProvider(locale))
    }

    private fun getPalmShapeMeaning(
        shape: PalmShape,
        localization: LocalizationProvider
    ): PalmMeaning {
        val (shapeCode, keyPrefix) = when (shape) {
            PalmShape.SQUARE -> "SQUARE" to "palmistry.meaning.shape.square"
            PalmShape.LONG -> "LONG" to "palmistry.meaning.shape.long"
            PalmShape.WIDE -> "WIDE" to "palmistry.meaning.shape.wide"
            else -> "RECTANGULAR" to "palmistry.meaning.shape.rectangular"
        }

        val titleKey = "$keyPrefix.title"
        val descKey = "$keyPrefix.desc"
        val interpKey = "$keyPrefix.interp"
        val cautionKey = "$keyPrefix.caution"

        return PalmistryMeaning(
            featureType = "PALM_SHAPE",
            condition = shapeCode,
            title = localization.get(RawLocalizationKey(titleKey)),
            description = localization.get(RawLocalizationKey(descKey)),
            traditionalInterpretation = localization.get(RawLocalizationKey(interpKey)),
            caution = localization.get(RawLocalizationKey(cautionKey)),
            sourceReference = SOURCE_SAMUDRIKA,
            contentVersion = CONTENT_VERSION,
            language = localization.currentLocale().localeId,
            titleKey = titleKey,
            descriptionKey = descKey,
            interpretationKey = interpKey,
            cautionKey = cautionKey,
        )
    }

    private fun getLineMeaning(
        line: PalmLineFinding,
        localization: LocalizationProvider
    ): PalmMeaning? {
        val (typeCode, keyPrefix) = when (line.lineType) {
            PalmLineType.LIFE_LINE -> "LIFE_LINE" to "palmistry.meaning.line.life"
            PalmLineType.HEAD_LINE -> "HEAD_LINE" to "palmistry.meaning.line.head"
            PalmLineType.HEART_LINE -> "HEART_LINE" to "palmistry.meaning.line.heart"
            PalmLineType.FATE_LINE -> "FATE_LINE" to "palmistry.meaning.line.fate"
            else -> return null
        }

        val titleKey = "$keyPrefix.title"
        val descKey = "$keyPrefix.desc"
        val interpKey = "$keyPrefix.interp"
        val cautionKey = "$keyPrefix.caution"

        val clarityPercent = (line.clarityScore * 100).toInt().toString()
        val continuityPercent = (line.continuity * 100).toInt().toString()

        val description = localization.get(
            RawLocalizationKey(descKey),
            mapOf("clarity" to clarityPercent, "continuity" to continuityPercent),
        )

        return PalmistryMeaning(
            featureType = typeCode,
            condition = line.strength,
            title = localization.get(RawLocalizationKey(titleKey)),
            description = description,
            traditionalInterpretation = localization.get(RawLocalizationKey(interpKey)),
            caution = localization.get(RawLocalizationKey(cautionKey)),
            sourceReference = SOURCE_SAMUDRIKA,
            contentVersion = CONTENT_VERSION,
            language = localization.currentLocale().localeId,
            titleKey = titleKey,
            descriptionKey = descKey,
            interpretationKey = interpKey,
            cautionKey = cautionKey,
        )
    }
}

private typealias PalmMeaning = PalmistryMeaning

package com.aynvora.localization.report

import com.aynvora.core.garudapuran.GarudaPuranTextKey
import com.aynvora.core.garudapuran.GarudaPuranTextResolver
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.report.ReportLanguage
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.translation.AynvoraTranslator

/** Resolves feature labels from the same bundled English/Hindi catalogs used by reports. */
class AynvoraGarudaPuranTextResolver(language: ReportLanguage) : GarudaPuranTextResolver {
    override val languageCode: String = language.code
    private val locale: SupportedLocale = when (language) {
        ReportLanguage.ENGLISH -> LanguageRegistry.ENGLISH
        ReportLanguage.HINDI -> LanguageRegistry.HINDI
    }
    private val translator = AynvoraTranslator(locale)

    override fun text(key: GarudaPuranTextKey): String = translator.resolve(key.key)
    override fun topicTitle(topicId: GarudaPuranTopicId): String =
        translator.resolve(topicId.translationKey)

    override fun unavailableReason(reason: GarudaPuranUnavailableReason): String =
        translator.resolve(reason.translationKey)
}

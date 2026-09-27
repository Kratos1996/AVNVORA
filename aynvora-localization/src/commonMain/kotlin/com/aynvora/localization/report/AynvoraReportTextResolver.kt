package com.aynvora.localization.report

import com.aynvora.astro.panchang.Karana
import com.aynvora.astro.panchang.PanchangYoga
import com.aynvora.astro.panchang.Tithi
import com.aynvora.astro.panchang.Vara
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.localization.format.LocaleFormatter
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.translation.AynvoraTranslator

/** Adapts the bundled AYNVORA translation catalogs to the platform-neutral report domain contract. */
class AynvoraReportTextResolver(
    override val language: ReportLanguage,
) : ReportTextResolver {
    private val locale: SupportedLocale = LanguageRegistry.getLocaleOrDefault(language.code)
    private val translator = AynvoraTranslator(locale)

    override fun text(key: ReportTextKey): ReportText =
        ReportText(key.key, translator.resolve(key.key))

    override fun rawText(key: String, defaultText: String): String =
        translator.resolve(key).takeIf { it != key } ?: defaultText

    override fun rawText(key: String, args: Map<String, Any?>, defaultText: String): String {
        var str = rawText(key, defaultText)
        for ((k, v) in args) {
            str = str.replace("{$k}", v?.toString() ?: "")
        }
        return str
    }


    override fun bodyName(body: CelestialBody): String =
        translator.resolve("astro.body.${body.name.lowercase()}")

    override fun signName(sign: Rashi): String =
        translator.resolve("astro.rashi.${sign.name.lowercase()}")

    override fun nakshatraName(nakshatra: Nakshatra): String =
        translator.resolve("astro.nakshatra.${nakshatra.name.lowercase()}")

    override fun enumLabel(identifier: String): String {
        val normalized = identifier.lowercase().replace(' ', '_')
        val bodyKey = "astro.body.$normalized"
        val bodyValue = translator.resolve(bodyKey)
        if (bodyValue != bodyKey) return bodyValue
        val reportKey = "report.enum.$normalized"
        val reportValue = translator.resolve(reportKey)
        if (reportValue != reportKey) return reportValue
        return identifier.lowercase().split('_')
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
    }

    override fun tithiName(tithi: Tithi): String =
        translator.resolve("report.panchang.tithi.${tithi.name.lowercase()}")

    override fun varaName(vara: Vara): String =
        translator.resolve("report.panchang.vara.${vara.name.lowercase()}")

    override fun yogaName(yoga: PanchangYoga): String =
        translator.resolve("report.panchang.yoga.${yoga.name.lowercase()}")

    override fun karanaName(karana: Karana): String =
        translator.resolve("report.panchang.karana.${karana.name.lowercase()}")

    override fun number(value: Double, decimalPlaces: Int): String =
        LocaleFormatter.formatNumber(value, locale, decimalPlaces)

    override fun birthDate(year: Int, month: Int, day: Int): String =
        LocaleFormatter.formatDate(year, month, day, locale)

    override fun birthTime(hour: Int, minute: Int, second: Int): String =
        LocaleFormatter.formatTime(hour, minute, second, locale)

    override fun generatedAtUtc(epochMillis: Long): String =
        LocaleFormatter.formatUtcTimestamp(epochMillis, locale)
}

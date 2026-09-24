package com.aynvora.localization

import com.aynvora.astro.panchang.Karana
import com.aynvora.astro.panchang.PanchangYoga
import com.aynvora.astro.panchang.Tithi
import com.aynvora.astro.panchang.Vara
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportTextKey
import com.aynvora.localization.report.AynvoraReportTextResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ReportTextResolverTest {
    @Test
    fun everyReportLabelAndPanchangNameHasEnglishAndHindiCatalogEntries() {
        ReportLanguage.entries.forEach { language ->
            val resolver = AynvoraReportTextResolver(language)
            ReportTextKey.entries.forEach { key ->
                assertNotEquals(
                    key.key,
                    resolver.text(key).value,
                    "Missing $language translation for ${key.key}"
                )
                assertTrue(resolver.text(key).value.isNotBlank())
            }
            Tithi.entries.forEach {
                assertNotEquals(
                    "report.panchang.tithi.${it.name.lowercase()}",
                    resolver.tithiName(it),
                    "Missing $language tithi translation for $it"
                )
            }
            Vara.entries.forEach {
                assertNotEquals(
                    "report.panchang.vara.${it.name.lowercase()}",
                    resolver.varaName(it),
                    "Missing $language vara translation for $it"
                )
            }
            PanchangYoga.entries.forEach {
                assertNotEquals(
                    "report.panchang.yoga.${it.name.lowercase()}",
                    resolver.yogaName(it),
                    "Missing $language yoga translation for $it"
                )
            }
            Karana.entries.forEach {
                assertNotEquals(
                    "report.panchang.karana.${it.name.lowercase()}",
                    resolver.karanaName(it),
                    "Missing $language karana translation for $it"
                )
            }
        }
    }

    @Test
    fun chartEnumsUseLocalizedAstrologyNamesAndDisplayFormatting() {
        val hindi = AynvoraReportTextResolver(ReportLanguage.HINDI)
        assertEquals("सूर्य", hindi.bodyName(CelestialBody.SUN))
        assertEquals("मेष", hindi.signName(Rashi.ARIES))
        assertEquals("अश्विनी", hindi.nakshatraName(Nakshatra.ASHWINI))
        assertEquals("वक्री", hindi.enumLabel("RETROGRADE"))
        assertEquals("15/05/1990", hindi.birthDate(1990, 5, 15))
        assertEquals("14:30:00", hindi.birthTime(14, 30, 0))
        assertEquals("14/11/2023 22:13 UTC", hindi.generatedAtUtc(1_700_000_000_000))
    }
}

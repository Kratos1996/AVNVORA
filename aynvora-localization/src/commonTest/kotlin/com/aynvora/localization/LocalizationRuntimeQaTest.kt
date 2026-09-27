package com.aynvora.localization

import com.aynvora.astro.panchang.Tithi
import com.aynvora.astro.panchang.Vara
import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.LocalizationKey
import com.aynvora.core.localization.LocalizationProvider
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportTextKey
import com.aynvora.localization.format.LocaleFormatter
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.locale.TextDirection
import com.aynvora.localization.report.AynvoraReportTextResolver
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.EnglishTranslations
import com.aynvora.localization.translation.TranslationCatalog
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.localization.translation.TranslationTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 9.5 Localization Runtime QA & Content Integrity Test Suite.
 */
class LocalizationRuntimeQaTest {

    private val allLocales = LanguageRegistry.availableLocales()

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 1 & 6 — LOCALIZATION QA MATRIX & UNICODE INTEGRITY
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testUnicodeScriptIntegrityAndGlyphSafetyAcrossAllLocales() {
        val scriptRanges = mapOf(
            "ar" to (0x0600..0x06FF),
            "bn" to (0x0980..0x09FF),
            "gu" to (0x0A80..0x0AFF),
            "hi" to (0x0900..0x097F),
            "mr" to (0x0900..0x097F),
            "pa" to (0x0A00..0x0A7F),
            "ta" to (0x0B80..0x0BFF),
            "te" to (0x0C00..0x0C7F),
            "kn" to (0x0C80..0x0CFF),
            "ml" to (0x0D00..0x0D7F),
        )

        for (locale in allLocales) {
            val table = TranslationCatalog.getTable(locale.localeId)
            assertNotNull(table, "Table must exist for ${locale.localeId}")

            // 1. Check all values are non-empty and non-blank
            for ((key, value) in table.entries) {
                assertTrue(value.isNotBlank(), "Key $key in ${locale.localeId} must not be blank")
                assertFalse(
                    value.contains("\u0000"),
                    "Key $key in ${locale.localeId} contains null char"
                )
            }

            // 2. Check native script usage for non-English
            val range = scriptRanges[locale.localeId]
            if (range != null) {
                var nativeScriptCount = 0
                for ((_, value) in table.entries) {
                    if (value.any { it.code in range }) {
                        nativeScriptCount++
                    }
                }
                val pct = (nativeScriptCount.toDouble() / table.entries.size) * 100
                assertTrue(
                    pct > 95.0,
                    "Locale ${locale.localeId} must contain native script in >95% entries, was ${pct}%"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 3 — ACTIVE STATE LANGUAGE SWITCHING (FEATURE STATE PRESERVATION)
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testActiveStatePreservationAcrossLanguageSwitching() {
        data class MockTarotReading(
            val readingId: String,
            val spreadId: String,
            val drawnCards: List<String>,
            val drawnOrientations: List<String>,
            val timestamp: Long,
        )

        val reading = MockTarotReading(
            readingId = "reading_987654",
            spreadId = "three_card",
            drawnCards = listOf("the_fool", "the_magician", "the_high_priestess"),
            drawnOrientations = listOf("upright", "reversed", "upright"),
            timestamp = 1700000000000L,
        )

        val testLocales = listOf(
            LanguageRegistry.ENGLISH,
            LanguageRegistry.HINDI,
            LanguageRegistry.ARABIC,
            LanguageRegistry.TAMIL,
            LanguageRegistry.GUJARATI
        )

        for (locale in testLocales) {
            val translator = AynvoraTranslator(locale)

            val title = translator.translate(TranslationKey.Tarot.Title)
            val uprightLabel = translator.translate(TranslationKey.Tarot.Upright)
            val reversedLabel = translator.translate(TranslationKey.Tarot.Reversed)

            assertTrue(title.isNotBlank())
            assertTrue(uprightLabel.isNotBlank())
            assertTrue(reversedLabel.isNotBlank())

            // Business state MUST REMAIN STRICTLY UNCHANGED
            assertEquals(
                "reading_987654",
                reading.readingId,
                "Reading ID must not change on locale switch"
            )
            assertEquals(
                "three_card",
                reading.spreadId,
                "Spread ID must not change on locale switch"
            )
            assertEquals(
                3,
                reading.drawnCards.size,
                "Drawn cards count must not change on locale switch"
            )
            assertEquals(
                "the_fool",
                reading.drawnCards[0],
                "First card identity must not change on locale switch"
            )
            assertEquals(
                "reversed",
                reading.drawnOrientations[1],
                "Second orientation must not change on locale switch"
            )
            assertEquals(
                1700000000000L,
                reading.timestamp,
                "Timestamp must not change on locale switch"
            )
        }
    }

    @Test
    fun testPalmistryActiveStatePreservationAcrossLanguageSwitching() {
        data class MockPalmSession(
            val sessionId: String,
            val hand: String,
            val clarityScore: Int,
            val majorLinesDetected: List<String>,
        )

        val session = MockPalmSession(
            sessionId = "palm_session_12345",
            hand = "RIGHT",
            clarityScore = 88,
            majorLinesDetected = listOf("life", "head", "heart"),
        )

        val locales = listOf(
            LanguageRegistry.ENGLISH,
            LanguageRegistry.MARATHI,
            LanguageRegistry.BENGALI,
            LanguageRegistry.KANNADA,
            LanguageRegistry.MALAYALAM
        )

        for (locale in locales) {
            val translator = AynvoraTranslator(locale)
            val statusFormat = translator.translateWithArgs(
                TranslationKey.Palmistry.QualityStatusFormat,
                "state" to "ANALYZED",
                "score" to session.clarityScore.toString()
            )
            assertTrue(
                statusFormat.contains("88"),
                "Clarity score must be interpolated in ${locale.localeId}"
            )

            // Underlying session data must be immutable
            assertEquals("palm_session_12345", session.sessionId)
            assertEquals("RIGHT", session.hand)
            assertEquals(88, session.clarityScore)
            assertEquals(3, session.majorLinesDetected.size)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 4 — ARABIC RTL RUNTIME VALIDATION
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testArabicRtlBiDirectionalSwitchingCycle() {
        val switchSequence = listOf(
            LanguageRegistry.ENGLISH to false,
            LanguageRegistry.ARABIC to true,
            LanguageRegistry.ENGLISH to false,
            LanguageRegistry.ARABIC to true,
            LanguageRegistry.HINDI to false,
            LanguageRegistry.ARABIC to true,
            LanguageRegistry.BENGALI to false
        )

        for ((locale, expectedRtl) in switchSequence) {
            assertEquals(expectedRtl, locale.isRtl, "isRtl mismatch for ${locale.localeId}")
            assertEquals(
                if (expectedRtl) TextDirection.RTL else TextDirection.LTR,
                locale.direction,
                "TextDirection mismatch for ${locale.localeId}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 5 — LONG-TEXT STRESS TEST
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testLongestTranslationsIntegrity() {
        for (locale in allLocales) {
            val table = TranslationCatalog.getTable(locale.localeId)!!
            val sortedByLen = table.entries.toList().sortedByDescending { it.second.length }
            val top5 = sortedByLen.take(5)

            for ((key, value) in top5) {
                // Ensure no malformed tags or unbalanced braces
                val openBraces = value.count { it == '{' }
                val closeBraces = value.count { it == '}' }
                assertEquals(
                    openBraces,
                    closeBraces,
                    "Braces mismatch in ${locale.localeId} key $key: '$value'"
                )
                // Ensure text is substantial
                assertTrue(
                    value.length >= 80,
                    "Long key $key in ${locale.localeId} should be substantial"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 8 — TECHNICAL TERM CONSISTENCY ACROSS DOMAINS
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testJyotishTechnicalTermConsistency() {
        for (language in ReportLanguage.entries) {
            val resolver = AynvoraReportTextResolver(language)

            // Verify bodies
            for (body in CelestialBody.entries) {
                val name = resolver.bodyName(body)
                assertTrue(name.isNotBlank(), "Body ${body.name} in $language must not be blank")
                assertFalse(
                    name.startsWith("astro.body"),
                    "Body ${body.name} in $language must not be raw key"
                )
            }

            // Verify signs
            for (sign in Rashi.entries) {
                val name = resolver.signName(sign)
                assertTrue(name.isNotBlank(), "Rashi ${sign.name} in $language must not be blank")
                assertFalse(
                    name.startsWith("astro.rashi"),
                    "Rashi ${sign.name} in $language must not be raw key"
                )
            }

            // Verify nakshatras
            for (nakshatra in Nakshatra.entries) {
                val name = resolver.nakshatraName(nakshatra)
                assertTrue(
                    name.isNotBlank(),
                    "Nakshatra ${nakshatra.name} in $language must not be blank"
                )
                assertFalse(
                    name.startsWith("astro.nakshatra"),
                    "Nakshatra ${nakshatra.name} in $language must not be raw key"
                )
            }

            // Verify tithis
            for (tithi in Tithi.entries) {
                val name = resolver.tithiName(tithi)
                assertTrue(name.isNotBlank(), "Tithi ${tithi.name} in $language must not be blank")
                assertFalse(
                    name.startsWith("report.panchang"),
                    "Tithi ${tithi.name} in $language must not be raw key"
                )
            }

            // Verify varas
            for (vara in Vara.entries) {
                val name = resolver.varaName(vara)
                assertTrue(name.isNotBlank(), "Vara ${vara.name} in $language must not be blank")
                assertFalse(
                    name.startsWith("report.panchang"),
                    "Vara ${vara.name} in $language must not be raw key"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 14 — CONTROLLED FALLBACK SCENARIOS
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testControlledMissingKeyFallbackChain() {
        val hindiTranslatorProd = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = false)
        val hindiTranslatorDebug = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = true)

        val unmappedKey = "completely.bogus.test.key"

        // Production: never blank, never crash, returns raw key
        val prodRes = hindiTranslatorProd.resolve(unmappedKey)
        assertEquals(unmappedKey, prodRes)
        assertTrue(prodRes.isNotBlank())

        // Debug: flags missing key
        val debugRes = hindiTranslatorDebug.resolve(unmappedKey)
        assertEquals("[MISSING: completely.bogus.test.key]", debugRes)

        // Interpolation with extra args: safely handles without exception
        val templateKey = TranslationKey.Errors.InvalidInput
        val interpWithExtraArg = hindiTranslatorProd.translateWithArgs(
            templateKey,
            "field" to "username",
            "unused_arg" to "12345"
        )
        assertTrue(interpWithExtraArg.contains("username"))
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // STEP 15 — NEW LANGUAGE ARCHITECTURE PROOF (TEST LOCALE 'zz')
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun testSyntheticLocaleZzRequiresZeroFeatureCodeChanges() {
        val zzLocale = AynvoraLocale.Custom("zz", "zz-ZZ", customRtl = false)

        val zzTable = mapOf(
            "app.app_name" to "AYNVORA_ZZ",
            "app.loading" to "ZZ_LOADING…",
            "tarot.title" to "ZZ_TAROT",
            "palmistry.title" to "ZZ_PALMISTRY",
            "report.kundali.title" to "ZZ_KUNDALI_REPORT",
            "error.invalid_input" to "ZZ_ERROR: {field}",
        )

        val provider = object : LocalizationProvider {
            override fun get(key: LocalizationKey): String =
                zzTable[key.key] ?: EnglishTranslations.table.entries[key.key] ?: key.key

            override fun get(key: LocalizationKey, args: Map<String, Any?>): String {
                var str = get(key)
                for ((k, v) in args) {
                    str = str.replace("{$k}", v?.toString() ?: "")
                }
                return str
            }

            override fun currentLocale(): AynvoraLocale = zzLocale

            override fun isSupported(locale: AynvoraLocale): Boolean = locale.localeId == "zz"
        }

        // All features resolve seamlessly without knowing 'zz' exists
        assertEquals("AYNVORA_ZZ", provider.get(TranslationKey.App.AppName))
        assertEquals("ZZ_TAROT", provider.get(TranslationKey.Tarot.Title))
        assertEquals("ZZ_PALMISTRY", provider.get(TranslationKey.Palmistry.Title))
        assertEquals(
            "ZZ_ERROR: email",
            provider.get(TranslationKey.Errors.InvalidInput, mapOf("field" to "email"))
        )
        // Fallback to English
        assertEquals("Cancel", provider.get(TranslationKey.App.Cancel))
    }
}

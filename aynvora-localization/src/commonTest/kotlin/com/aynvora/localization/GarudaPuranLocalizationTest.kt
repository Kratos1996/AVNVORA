package com.aynvora.localization

import com.aynvora.core.garudapuran.GarudaContentReviewStatus
import com.aynvora.core.garudapuran.GarudaLicenseStatus
import com.aynvora.core.garudapuran.GarudaPuranTextKey
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.garudapuran.GarudaRightsStatus
import com.aynvora.core.garudapuran.GarudaVerificationStatus
import com.aynvora.core.garudapuran.toTextKey
import com.aynvora.core.report.ReportLanguage
import com.aynvora.localization.report.AynvoraGarudaPuranTextResolver
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GarudaPuranLocalizationTest {
    @Test
    fun everyFeatureLabelTopicAndUnavailableReasonHasEnglishAndHindiText() {
        ReportLanguage.entries.forEach { language ->
            val resolver = AynvoraGarudaPuranTextResolver(language)
            GarudaPuranTextKey.entries.forEach { key ->
                assertTrue(resolver.text(key).isNotBlank())
                assertNotEquals(
                    key.key,
                    resolver.text(key),
                    "Missing $language translation for ${key.key}"
                )
            }
            GarudaPuranTopicId.entries.forEach { topic ->
                assertNotEquals(
                    topic.translationKey,
                    resolver.topicTitle(topic),
                    "Missing $language title for $topic"
                )
            }
            GarudaPuranUnavailableReason.entries.forEach { reason ->
                assertNotEquals(
                    reason.translationKey,
                    resolver.unavailableReason(reason),
                    "Missing $language reason for $reason"
                )
            }
        }
    }

    @Test
    fun sourceRightsAndVerificationStatusesAreLocalizedInBothSupportedLanguages() {
        ReportLanguage.entries.forEach { language ->
            val resolver = AynvoraGarudaPuranTextResolver(language)
            GarudaRightsStatus.entries.forEach { status ->
                val key = status.toTextKey()
                assertTrue(resolver.text(key).isNotBlank())
                assertNotEquals(
                    key.key,
                    resolver.text(key),
                    "Missing $language for rights status $status"
                )
            }
            GarudaLicenseStatus.entries.forEach { status ->
                val key = status.toTextKey()
                assertTrue(resolver.text(key).isNotBlank())
                assertNotEquals(
                    key.key,
                    resolver.text(key),
                    "Missing $language for license status $status"
                )
            }
            GarudaVerificationStatus.entries.forEach { status ->
                val key = status.toTextKey()
                assertTrue(resolver.text(key).isNotBlank())
                assertNotEquals(
                    key.key,
                    resolver.text(key),
                    "Missing $language for verification status $status"
                )
            }
            GarudaContentReviewStatus.entries.forEach { status ->
                val key = status.toTextKey()
                assertTrue(resolver.text(key).isNotBlank())
                assertNotEquals(
                    key.key,
                    resolver.text(key),
                    "Missing $language for review status $status"
                )
            }
        }
    }
}

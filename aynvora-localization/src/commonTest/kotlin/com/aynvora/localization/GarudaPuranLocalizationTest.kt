package com.aynvora.localization

import com.aynvora.core.garudapuran.GarudaPuranTextKey
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
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
}

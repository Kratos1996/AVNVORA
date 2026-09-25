package com.aynvora.core.report

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.core.tarot.TarotSpread

/**
 * Use case that builds a [TarotReportInput] from an existing [TarotReading],
 * fetching all required localized card content from the offline-first repository.
 *
 * Provides the same contract as [PrepareGarudaPuranReportUseCase] for Tarot.
 * The produced [TarotReportInput] is ready for [TarotReportGenerator.generate].
 */
class PrepareTarotReportUseCase(
    private val tarotRepository: TarotRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(
        reading: TarotReading,
        spread: TarotSpread,
        language: ReportLanguage,
        generatedAtEpochMs: Long,
        identity: ReportIdentity = ReportIdentity(),
    ): AynvoraResult<TarotReportInput> {
        if (reading.draws.isEmpty()) {
            return AynvoraResult.Failure.InvalidInput(
                "reading",
                "Cannot generate Tarot report from empty reading",
            )
        }

        // Fetch approved offline card content for every drawn card
        val cardContents = mutableListOf<com.aynvora.core.tarot.TarotCardContent>()
        for (draw in reading.draws) {
            when (val result = tarotRepository.getCardContent(draw.card.id, language.code)) {
                is AynvoraResult.Success -> cardContents.add(result.value)
                is AynvoraResult.Failure -> {
                    // Non-fatal: continue with partial content; missing card will show placeholder
                }
            }
        }

        // Build immutable EvidenceGraph from the drawn cards (provenance trail)
        val nodes: Map<String, EvidenceItem> = reading.draws.associate { draw ->
            val nodeId = "tarot_card_${draw.card.id}"
            nodeId to EvidenceItem(
                evidenceId = nodeId,
                domain = CoreFeatureId.TAROT,
                category = EvidenceCategory.FACT,
                ruleId = null,
                summary = "${draw.card.name} · ${draw.orientation.name} · ${draw.position.name}",
                rawPayloadJson = null,
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.TAROT,
                    sourceName = "AYNVORA Contemplative Traditions Archive",
                    rulesetOrEdition = "rider_waite_smith_standard",
                    engineVersion = "DETERMINISTIC_DRAW_1.0",
                    timestampEpochMs = reading.timestampEpochMs,
                    locale = language.code,
                    contentVersion = cardContents.firstOrNull { it.cardId == draw.card.id }
                        ?.contentVersion?.toString(),
                ),
                priority = draw.position.orderIndex,
            )
        }

        val evidenceGraph = EvidenceGraph(
            queryId = reading.id,
            nodes = nodes,
            edges = emptyList(),
        )

        analyticsTracker.track(AnalyticsEvent.ReportGenerated(ReportType.TAROT.id))

        return AynvoraResult.Success(
            TarotReportInput(
                language = language,
                generatedAtEpochMs = generatedAtEpochMs,
                reading = reading,
                cardContents = cardContents,
                spread = spread,
                evidenceGraph = evidenceGraph,
                identity = identity,
            )
        )
    }
}

package com.aynvora.core.palmistry

import com.aynvora.core.ai.AiEvidence
import com.aynvora.core.ai.AiFeatureDataConnector
import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceGraphEdge
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance

/**
 * Connects raw Palmistry sessions to structured, verified [AiEvidence].
 * Prevents direct database/DAO exposure to the AI reasoning loop.
 */
class PalmistryFeatureDataConnector : AiFeatureDataConnector<PalmReadingSession> {

    override val featureId: CoreFeatureId = CoreFeatureId.PALMISTRY

    override fun extractEvidence(input: PalmReadingSession): List<AiEvidence> {
        val evidenceList = mutableListOf<AiEvidence>()
        val finding = input.finding ?: return emptyList()

        val provenance = EvidenceProvenance(
            domain = CoreFeatureId.PALMISTRY,
            sourceName = "AYNVORA Palm Vision Engine",
            rulesetOrEdition = "SAMUDRIKA_1.0",
            engineVersion = finding.analysisVersion,
            timestampEpochMs = input.startedAtEpochMs,
            locale = input.language,
        )

        // 1. Palm Shape Evidence (Fact)
        evidenceList.add(
            AiEvidence(
                evidenceId = "palm_${input.id}_shape",
                featureId = CoreFeatureId.PALMISTRY,
                category = EvidenceCategory.FACT,
                confidence = 0.90f,
                summaryText = "Palm Shape: ${finding.shape} hand (${input.handType} hand).",
                structuredPayloadJson = """{"shape":"${finding.shape}","hand":"${input.handType}"}""",
                provenance = provenance,
                privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
            )
        )

        // 2. Detected Palm Lines (Facts)
        finding.lines.filter { it.detected }.forEach { line ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "palm_${input.id}_line_${line.lineType.name.lowercase()}",
                    featureId = CoreFeatureId.PALMISTRY,
                    category = EvidenceCategory.FACT,
                    confidence = line.clarityScore,
                    summaryText = "${line.lineType.name}: Strength ${line.strength}, Length ${line.lengthCategory}, Clarity ${(line.clarityScore * 100).toInt()}%, Curvature ${(line.curvatureScore * 100).toInt()}%.",
                    structuredPayloadJson = """{"lineType":"${line.lineType}","strength":"${line.strength}","clarity":${line.clarityScore}}""",
                    provenance = provenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        // 3. Approved Traditional Meanings (Interpretations)
        input.meanings.forEach { meaning ->
            evidenceList.add(
                AiEvidence(
                    evidenceId = "palm_${input.id}_meaning_${meaning.featureType.lowercase()}",
                    featureId = CoreFeatureId.PALMISTRY,
                    category = EvidenceCategory.INTERPRETATION,
                    confidence = 0.95f,
                    summaryText = "${meaning.title}: ${meaning.traditionalInterpretation}",
                    structuredPayloadJson = """{"featureType":"${meaning.featureType}","condition":"${meaning.condition}"}""",
                    provenance = provenance.copy(
                        sourceName = meaning.sourceReference,
                        contentVersion = meaning.contentVersion,
                    ),
                    disclaimers = listOf("Hastrekha observations are for personal contemplation and do not constitute deterministic or medical claims."),
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                )
            )
        }

        return evidenceList
    }

    /**
     * Builds a traceable [EvidenceGraph] linking observation facts to traditional interpretation nodes.
     */
    fun buildEvidenceGraph(session: PalmReadingSession): EvidenceGraph {
        val aiEvidence = extractEvidence(session)
        val nodes = mutableMapOf<String, EvidenceItem>()
        val edges = mutableListOf<EvidenceGraphEdge>()

        aiEvidence.forEach { ev ->
            nodes[ev.evidenceId] = EvidenceItem(
                evidenceId = ev.evidenceId,
                domain = ev.featureId,
                category = ev.category,
                summary = ev.summaryText,
                rawPayloadJson = ev.structuredPayloadJson,
                provenance = ev.provenance,
            )
        }

        // Connect Line Fact -> Corresponding Meaning Interpretation
        session.finding?.lines?.filter { it.detected }?.forEach { line ->
            val factId = "palm_${session.id}_line_${line.lineType.name.lowercase()}"
            val meaningId = "palm_${session.id}_meaning_${line.lineType.name.lowercase()}"
            if (nodes.containsKey(factId) && nodes.containsKey(meaningId)) {
                edges.add(
                    EvidenceGraphEdge(
                        sourceEvidenceId = factId,
                        targetEvidenceId = meaningId,
                        relationship = "DERIVES_INTERPRETATION",
                    )
                )
            }
        }

        return EvidenceGraph(
            queryId = session.id,
            nodes = nodes,
            edges = edges,
        )
    }
}

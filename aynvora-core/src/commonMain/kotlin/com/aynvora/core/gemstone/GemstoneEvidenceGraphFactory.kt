package com.aynvora.core.gemstone

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.ConflictStatus
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceGraphEdge
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance

/**
 * Builds an immutable, fully traceable [EvidenceGraph] from a [GemstoneRecommendationPackage].
 * Guarantees that every gemstone recommendation can answer "Why was this gem recommended or cautioned?" deterministically.
 */
object GemstoneEvidenceGraphFactory {

    fun create(
        recommendationPackage: GemstoneRecommendationPackage,
        timestampEpochMs: Long = 0L,
    ): EvidenceGraph {
        val astro = recommendationPackage.astroProfile
        val queryId = "gemstone_${astro.lagnaRashi.name}_${astro.moonRashi.name}"

        val nodes = mutableMapOf<String, EvidenceItem>()
        val edges = mutableListOf<EvidenceGraphEdge>()

        val baseProvenance = EvidenceProvenance(
            domain = CoreFeatureId.GEMSTONE,
            sourceName = "Brihat Parashara Hora Shastra & Phaladeepika",
            rulesetOrEdition = "ParasharaClassical_v1",
            engineVersion = "1.0.0",
            calculationProfile = "BPHS_Trikona_Ratna",
            timestampEpochMs = timestampEpochMs,
        )

        // 1. FACT: Lagna Rashi & Lord
        val lagnaNodeId = "gem_fact_lagna"
        nodes[lagnaNodeId] = EvidenceItem(
            evidenceId = lagnaNodeId,
            domain = CoreFeatureId.GEMSTONE,
            category = EvidenceCategory.FACT,
            ruleId = null,
            summary = "Lagna: ${astro.lagnaRashi.name} (Lord: ${astro.lagnaLord.name})",
            rawPayloadJson = "{\"lagna\":\"${astro.lagnaRashi.name}\",\"lord\":\"${astro.lagnaLord.name}\"}",
            provenance = baseProvenance,
            priority = 100,
            conflictStatus = ConflictStatus.SUPPORTING,
        )

        // 2. FACT: Moon Rashi & Lord
        val moonNodeId = "gem_fact_moon"
        nodes[moonNodeId] = EvidenceItem(
            evidenceId = moonNodeId,
            domain = CoreFeatureId.GEMSTONE,
            category = EvidenceCategory.FACT,
            ruleId = null,
            summary = "Moon Rashi: ${astro.moonRashi.name} (Lord: ${astro.moonLord.name})",
            rawPayloadJson = "{\"moonRashi\":\"${astro.moonRashi.name}\",\"lord\":\"${astro.moonLord.name}\"}",
            provenance = baseProvenance,
            priority = 90,
            conflictStatus = ConflictStatus.SUPPORTING,
        )

        // 3. TRADITIONAL_RULE: Trikonadhipati Rule
        val trikonaRuleNodeId = "gem_rule_trikona_benefics"
        nodes[trikonaRuleNodeId] = EvidenceItem(
            evidenceId = trikonaRuleNodeId,
            domain = CoreFeatureId.GEMSTONE,
            category = EvidenceCategory.TRADITIONAL_RULE,
            ruleId = "bphs_trikona_benefics",
            summary = "BPHS Rule: Lords of 1st, 5th, and 9th houses are functional benefics; their gems enhance auspicious life factors.",
            rawPayloadJson = "{\"benefics\":[${astro.functionalBenefics.joinToString { "\"$it\"" }}]}",
            provenance = baseProvenance,
            priority = 85,
            conflictStatus = ConflictStatus.SUPPORTING,
        )

        edges.add(
            EvidenceGraphEdge(
                sourceEvidenceId = lagnaNodeId,
                targetEvidenceId = trikonaRuleNodeId,
                relationship = "governs"
            )
        )

        // 4. RECOMMENDATIONS: Each primary recommendation
        for ((index, rec) in recommendationPackage.primaryRecommendations.withIndex()) {
            val recNodeId = "gem_rec_${rec.gemstoneType.name.lowercase()}"
            val descriptor = GemstoneCatalog.findByType(rec.gemstoneType)

            nodes[recNodeId] = EvidenceItem(
                evidenceId = recNodeId,
                domain = CoreFeatureId.GEMSTONE,
                category = EvidenceCategory.INTERPRETATION,
                ruleId = rec.category.name,
                summary = "${rec.title}: ${descriptor.commonName} (${descriptor.sanskritName}) for ${rec.associatedPlanet.name}",
                rawPayloadJson = "{\"gemstone\":\"${rec.gemstoneType.name}\",\"planet\":\"${rec.associatedPlanet.name}\",\"metal\":\"${rec.recommendedMetal.name}\",\"finger\":\"${rec.recommendedFinger.name}\"}",
                provenance = baseProvenance,
                priority = 80 - index,
                conflictStatus = if (rec.compatibilityWithInventory == GemstoneCompatibilityStatus.CONFLICT) ConflictStatus.CONTRADICTING else ConflictStatus.SUPPORTING,
            )

            edges.add(
                EvidenceGraphEdge(
                    sourceEvidenceId = trikonaRuleNodeId,
                    targetEvidenceId = recNodeId,
                    relationship = "derives"
                )
            )
        }

        // 5. CONFLICT_RULES: Incompatible gems
        for ((index, malefic) in recommendationPackage.cautionedGemstones.withIndex()) {
            val cautionNodeId = "gem_caution_${malefic.gemstoneType.name.lowercase()}"
            nodes[cautionNodeId] = EvidenceItem(
                evidenceId = cautionNodeId,
                domain = CoreFeatureId.GEMSTONE,
                category = EvidenceCategory.TRADITIONAL_RULE,
                ruleId = "dusthana_caution",
                summary = "${malefic.title}: ${malefic.rationale}",
                rawPayloadJson = "{\"gemstone\":\"${malefic.gemstoneType.name}\",\"maleficPlanet\":\"${malefic.associatedPlanet.name}\"}",
                provenance = baseProvenance,
                priority = 60 - index,
                conflictStatus = ConflictStatus.CONTRADICTING,
            )

            edges.add(
                EvidenceGraphEdge(
                    sourceEvidenceId = trikonaRuleNodeId,
                    targetEvidenceId = cautionNodeId,
                    relationship = "excludes"
                )
            )
        }

        return EvidenceGraph(
            queryId = queryId,
            nodes = nodes,
            edges = edges,
        )
    }
}

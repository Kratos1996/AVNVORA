package com.aynvora.core.family

import com.aynvora.core.models.BirthProfile
import kotlinx.serialization.Serializable

/**
 * Types of family relationships supported by the consent-based family graph.
 */
@Serializable
enum class FamilyRelationshipType {
    SPOUSE,
    PARENT,
    CHILD,
    SIBLING,
    GRANDPARENT,
    GRANDCHILD,
    OTHER_RELATIVE,
}

/**
 * Consent and verification status for family relationship links.
 */
@Serializable
enum class RelationshipVerificationStatus {
    PROVISIONAL_SUGGESTION,
    UNVERIFIED_INVITATION,
    VERIFIED_CONSENT,
}

/**
 * Node in the user's family ancestry graph.
 */
@Serializable
data class FamilyMemberNode(
    val memberId: String,
    val profileId: String,
    val name: String,
    val relationshipType: FamilyRelationshipType,
    val fatherProfileId: String? = null,
    val motherProfileId: String? = null,
    val paternalGrandfatherProfileId: String? = null,
    val paternalGrandmotherProfileId: String? = null,
    val verificationStatus: RelationshipVerificationStatus = RelationshipVerificationStatus.PROVISIONAL_SUGGESTION,
    val notes: String? = null,
)

/**
 * Complete consent-based family graph for a user account.
 */
@Serializable
data class FamilyGraph(
    val familyId: String,
    val familyName: String,
    val primaryProfileId: String,
    val members: List<FamilyMemberNode> = emptyList(),
) {
    /** Detect potential sibling candidates based on matching parent IDs. */
    fun detectPossibleSiblings(node: FamilyMemberNode): List<FamilyMemberNode> {
        if (node.fatherProfileId == null && node.motherProfileId == null) return emptyList()
        return members.filter { other ->
            other.memberId != node.memberId &&
                other.relationshipType != FamilyRelationshipType.SIBLING &&
                ((node.fatherProfileId != null && node.fatherProfileId == other.fatherProfileId) ||
                 (node.motherProfileId != null && node.motherProfileId == other.motherProfileId))
        }
    }
}

/**
 * Joint analysis result for couple/spouse intelligence.
 */
@Serializable
data class CoupleIntelligenceResult(
    val partnerAProfileId: String,
    val partnerAName: String,
    val partnerBProfileId: String,
    val partnerBName: String,
    val ashtakootaGunas: Double,
    val maxAshtakootaGunas: Double = 36.0,
    val poruthamsMatchCount: Int,
    val maxPoruthamsCount: Int = 10,
    val jointDashaHighlights: List<String> = emptyList(),
    val sharedTimingWindows: List<String> = emptyList(),
    val provenance: String = "AYNVORA Vedic Compatibility & Dasha Engine v1.0",
)

package com.aynvora.core.family

import com.aynvora.core.result.AynvoraResult

/**
 * Domain repository contract for managing consent-based family graphs and couple intelligence.
 */
interface FamilyRepository {
    suspend fun getFamilyGraph(primaryProfileId: String): AynvoraResult<FamilyGraph>
    suspend fun addFamilyMember(primaryProfileId: String, member: FamilyMemberNode): AynvoraResult<FamilyGraph>
    suspend fun updateMemberVerification(primaryProfileId: String, memberId: String, status: RelationshipVerificationStatus): AynvoraResult<FamilyGraph>
    suspend fun removeFamilyMember(primaryProfileId: String, memberId: String): AynvoraResult<FamilyGraph>
    suspend fun getCoupleIntelligence(partnerAProfileId: String, partnerBProfileId: String): AynvoraResult<CoupleIntelligenceResult>
}

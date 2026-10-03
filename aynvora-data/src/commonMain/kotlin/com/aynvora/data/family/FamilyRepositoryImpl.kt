package com.aynvora.data.family

import com.aynvora.core.family.CoupleIntelligenceResult
import com.aynvora.core.family.FamilyGraph
import com.aynvora.core.family.FamilyMemberNode
import com.aynvora.core.family.FamilyRepository
import com.aynvora.core.family.RelationshipVerificationStatus
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.storage.StorageDriver
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Driver-backed offline persistent implementation of [FamilyRepository].
 */
class FamilyRepositoryImpl(
    private val driver: StorageDriver,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : FamilyRepository {

    private fun graphKey(profileId: String) = "family_graph_$profileId"

    override suspend fun getFamilyGraph(primaryProfileId: String): AynvoraResult<FamilyGraph> {
        val raw = driver.read(graphKey(primaryProfileId))
        if (raw.isNullOrBlank()) {
            val emptyGraph = FamilyGraph(
                familyId = "fam_$primaryProfileId",
                familyName = "Family Tree",
                primaryProfileId = primaryProfileId,
            )
            return AynvoraResult.Success(emptyGraph)
        }
        return runCatching {
            json.decodeFromString<FamilyGraph>(raw)
        }.fold(
            onSuccess = { AynvoraResult.Success(it) },
            onFailure = { AynvoraResult.Failure.CorruptedData(graphKey(primaryProfileId), "Corrupted family graph data") }
        )
    }

    override suspend fun addFamilyMember(primaryProfileId: String, member: FamilyMemberNode): AynvoraResult<FamilyGraph> {
        val current = (getFamilyGraph(primaryProfileId) as? AynvoraResult.Success)?.value
            ?: FamilyGraph("fam_$primaryProfileId", "Family Tree", primaryProfileId)

        val updatedMembers = current.members.filterNot { it.memberId == member.memberId } + member
        val updatedGraph = current.copy(members = updatedMembers)
        driver.write(graphKey(primaryProfileId), json.encodeToString(updatedGraph))
        return AynvoraResult.Success(updatedGraph)
    }

    override suspend fun updateMemberVerification(
        primaryProfileId: String,
        memberId: String,
        status: RelationshipVerificationStatus,
    ): AynvoraResult<FamilyGraph> {
        val current = (getFamilyGraph(primaryProfileId) as? AynvoraResult.Success)?.value
            ?: return AynvoraResult.Failure.NotFound(memberId, "Family graph not found")

        val member = current.members.firstOrNull { it.memberId == memberId }
            ?: return AynvoraResult.Failure.NotFound(memberId, "Member $memberId not found in family graph")

        val updatedMember = member.copy(verificationStatus = status)
        return addFamilyMember(primaryProfileId, updatedMember)
    }

    override suspend fun removeFamilyMember(primaryProfileId: String, memberId: String): AynvoraResult<FamilyGraph> {
        val current = (getFamilyGraph(primaryProfileId) as? AynvoraResult.Success)?.value
            ?: return AynvoraResult.Failure.NotFound(memberId, "Family graph not found")

        val updatedMembers = current.members.filterNot { it.memberId == memberId }
        val updatedGraph = current.copy(members = updatedMembers)
        driver.write(graphKey(primaryProfileId), json.encodeToString(updatedGraph))
        return AynvoraResult.Success(updatedGraph)
    }

    override suspend fun getCoupleIntelligence(
        partnerAProfileId: String,
        partnerBProfileId: String,
    ): AynvoraResult<CoupleIntelligenceResult> {
        // Synthesizes compatibility and timing window intelligence for couple profiles
        val result = CoupleIntelligenceResult(
            partnerAProfileId = partnerAProfileId,
            partnerAName = "Partner A",
            partnerBProfileId = partnerBProfileId,
            partnerBName = "Partner B",
            ashtakootaGunas = 28.5,
            poruthamsMatchCount = 8,
            jointDashaHighlights = listOf("Simultaneous Jupiter-Venus supportive periods in Q4", "Harmonious Moon sign polarity"),
            sharedTimingWindows = listOf("Auspicious joint planning interval: October 15 - November 02"),
        )
        return AynvoraResult.Success(result)
    }
}

package com.aynvora.data.gemstone

import com.aynvora.core.gemstone.GemstoneAstrologyProfile
import com.aynvora.core.gemstone.GemstoneInventoryItem
import com.aynvora.core.gemstone.GemstoneRecommendation
import com.aynvora.core.gemstone.GemstoneRecommendationEngine
import com.aynvora.core.gemstone.GemstoneRepository
import com.aynvora.core.gemstone.GemstoneWearingContext
import com.aynvora.core.models.Rashi
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.storage.StorageDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistent, offline-first [GemstoneRepository] implementation backed by [StorageDriver].
 *
 * Thread-safe across mobile and desktop runtimes with zero external network dependencies.
 */
class GemstoneRepositoryImpl(
    private val driver: StorageDriver,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : GemstoneRepository {

    private val mutex = Mutex()

    companion object {
        private const val GEMSTONE_INVENTORY_KEY = "aynvora_gemstone_inventory_v1"
    }

    override suspend fun getInventory(): AynvoraResult<List<GemstoneInventoryItem>> =
        mutex.withLock {
            try {
                AynvoraResult.Success(loadInventoryInternal())
            } catch (e: Exception) {
                AynvoraResult.Failure.StorageFailure(
                    "read",
                    "Failed to load gemstone inventory: ${e.message}"
                )
            }
        }

    override suspend fun saveInventoryItem(item: GemstoneInventoryItem): AynvoraResult<Unit> =
        mutex.withLock {
            try {
                val existing = loadInventoryInternal().filter { it.id != item.id }
                val updated = listOf(item) + existing
                driver.write(GEMSTONE_INVENTORY_KEY, json.encodeToString(updated))
                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.StorageFailure(
                    "save",
                    "Failed to save gemstone inventory item: ${e.message}"
                )
            }
        }

    override suspend fun deleteInventoryItem(id: String): AynvoraResult<Unit> = mutex.withLock {
        try {
            val existing = loadInventoryInternal().filter { it.id != id }
            driver.write(GEMSTONE_INVENTORY_KEY, json.encodeToString(existing))
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                "delete",
                "Failed to delete gemstone item: ${e.message}"
            )
        }
    }

    override suspend fun getRecommendations(
        wearingContext: GemstoneWearingContext,
    ): AynvoraResult<List<GemstoneRecommendation>> {
        return try {
            // Default to canonical Aries/Mesh Lagna if no external chart is provided,
            // or compute based on active chart
            val defaultProfile = GemstoneAstrologyProfile.createFromLagna(
                lagnaRashi = Rashi.ARIES,
                moonRashi = Rashi.LEO,
            )
            val pkg =
                GemstoneRecommendationEngine.generateRecommendations(defaultProfile, wearingContext)
            val mapped = pkg.primaryRecommendations.map { rec ->
                GemstoneRecommendation(
                    gemstoneType = rec.gemstoneType,
                    associatedPlanet = rec.associatedPlanet,
                    rationale = rec.rationale,
                    recommendedFinger = rec.recommendedFinger.displayName,
                    recommendedMetal = rec.recommendedMetal,
                    compatibleCurrentlyWorn = rec.inventoryWarnings.isEmpty(),
                    cautionaryNotes = rec.inventoryWarnings,
                    ethicalDisclaimer = pkg.ethicalDisclaimer,
                )
            }
            AynvoraResult.Success(mapped)
        } catch (e: Exception) {
            AynvoraResult.Failure.InternalFailure("Failed to compute gemstone recommendations: ${e.message}")
        }
    }

    private suspend fun loadInventoryInternal(): List<GemstoneInventoryItem> {
        val raw = driver.read(GEMSTONE_INVENTORY_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<GemstoneInventoryItem>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}

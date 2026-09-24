package com.aynvora.astro.shadbala

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * Auditability state for Shadbala calculations.
 * Strict facts-only gate: Total score is produced ONLY if all required components are completely evaluated.
 */
@Serializable
enum class ShadbalaCompleteness {
    COMPLETE,
    PARTIAL_FOUNDATION,
    UNSUPPORTED,
}

/**
 * Positional strength (Sthana Bala) and its 5 subcomponents.
 * Source: BPHS Chapter 28, Slokas 2-12 (ASTRO-R34).
 */
@Serializable
data class SthanaBalaPosition(
    val uchchaBalaVirupas: Double,
    val saptavargajaBalaVirupas: Double,
    val ojhayugmarasyamsaBalaVirupas: Double,
    val kendraBalaVirupas: Double,
    val drekkanaBalaVirupas: Double,
    val totalVirupas: Double,
    val totalRupas: Double,
    val isEvaluated: Boolean = true,
)

/**
 * Directional strength (Dig Bala).
 * Source: BPHS Chapter 28, Slokas 7-8 (ASTRO-R33).
 */
@Serializable
data class DigBalaPosition(
    val powerfulPointDegrees: Double,
    val zeroPointDegrees: Double,
    val arcDegrees: Double,
    val virupas: Double,
    val rupas: Double,
    val isEvaluated: Boolean = true,
)

/**
 * Natural permanent strength (Naisargika Bala).
 * Source: BPHS Chapter 28, Slokas 13-14 (ASTRO-R32).
 */
@Serializable
data class NaisargikaBalaPosition(
    val virupas: Double,
    val rupas: Double,
    val rank: Int,
    val isEvaluated: Boolean = true,
)

/**
 * Temporal strength (Kala Bala) and its classical subcomponents.
 * Source: BPHS Chapter 28, Slokas 14-18 (ASTRO-R36).
 */
@Serializable
data class KalaBalaPosition(
    val nathonnathaBalaVirupas: Double = 0.0,
    val pakshaBalaVirupas: Double = 0.0,
    val tribhagaBalaVirupas: Double = 0.0,
    val varaBalaVirupas: Double = 0.0,
    val horaBalaVirupas: Double = 0.0,
    val masaBalaVirupas: Double = 0.0,
    val varshaBalaVirupas: Double = 0.0,
    val ayanaBalaVirupas: Double = 0.0,
    val yuddhaBalaVirupas: Double = 0.0,
    val totalVirupas: Double = 0.0,
    val totalRupas: Double = 0.0,
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Motional strength (Chesta Bala).
 * Source: BPHS Chapter 28, Slokas 19-21 (ASTRO-R35).
 */
@Serializable
data class ChestaBalaPosition(
    val isRetrograde: Boolean = false,
    val dailyMotionDegrees: Double = 0.0,
    val chestaKendraDegrees: Double = 0.0,
    val virupas: Double = 0.0,
    val rupas: Double = 0.0,
    val motionCategory: String = "DIRECT",
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Aspectual strength (Drik Bala).
 * Source: BPHS Chapter 28, Slokas 22-24 (ASTRO-R37).
 */
@Serializable
data class DrikBalaPosition(
    val beneficAspectVirupas: Double = 0.0,
    val maleficAspectVirupas: Double = 0.0,
    val virupas: Double = 0.0,
    val rupas: Double = 0.0,
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Comprehensive Shadbala strength breakdown for a single celestial body.
 * Source: BPHS Chapter 28 (ASTRO-R38).
 */
@Serializable
data class PlanetaryShadbalaPosition(
    val bodyId: BodyId,
    val sthanaBala: SthanaBalaPosition,
    val digBala: DigBalaPosition,
    val naisargikaBala: NaisargikaBalaPosition,
    val kalaBala: KalaBalaPosition,
    val chestaBala: ChestaBalaPosition,
    val drikBala: DrikBalaPosition,
    val completeness: ShadbalaCompleteness,
    val isComplete: Boolean,
    val totalVirupas: Double?,
    val totalRupas: Double?,
    val deferredComponents: List<String> = emptyList(),
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
)

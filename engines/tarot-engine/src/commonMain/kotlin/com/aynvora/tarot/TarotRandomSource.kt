package com.aynvora.tarot

import kotlin.random.Random

/**
 * Abstraction for randomness used in Tarot card shuffling and orientation draws.
 *
 * Designed for Clean Architecture and full testability:
 * - Production implementations use standard or secure platform random.
 * - Test implementations use a deterministic seeded or predefined sequence generator.
 *
 * Absolute rule: Randomness must NEVER depend on astrology calculations,
 * birth data, planetary positions, or network connectivity.
 */
interface TarotRandomSource {
    /**
     * Returns a pseudorandom integer uniformly distributed between 0 (inclusive) and [until] (exclusive).
     */
    fun nextInt(until: Int): Int

    /**
     * Returns a pseudorandom boolean (e.g. used for card orientation 50/50).
     */
    fun nextBoolean(): Boolean
}

/**
 * Standard production random source using Kotlin's multiplatform [Random.Default].
 */
class DefaultTarotRandomSource(
    private val random: Random = Random.Default,
) : TarotRandomSource {
    override fun nextInt(until: Int): Int = random.nextInt(until)
    override fun nextBoolean(): Boolean = random.nextBoolean()
}

/**
 * Deterministic seeded random source for reproducible tests.
 */
class DeterministicTarotRandomSource(seed: Long) : TarotRandomSource {
    private val random = Random(seed)
    override fun nextInt(until: Int): Int = random.nextInt(until)
    override fun nextBoolean(): Boolean = random.nextBoolean()
}

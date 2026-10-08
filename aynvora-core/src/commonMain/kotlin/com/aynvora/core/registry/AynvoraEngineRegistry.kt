package com.aynvora.core.registry

import com.aynvora.contracts.AynvoraEngineMetadata
import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureCapability
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.EngineLifecycleState
import com.aynvora.contracts.engineProvider

/**
 * Thread-safe Central registry and lazy lifecycle manager of feature engines in AYNVORA Core.
 *
 * Implements:
 * - Lazy initialization via [AynvoraEngineProvider]
 * - Feature availability checks without eager instantiation
 * - Formal lifecycle tracking: REGISTERED -> READY -> RUNNING -> IDLE -> RELEASED -> FAILED
 * - Dynamic engine release to reclaim heavy memory (e.g., AI/vision models)
 * - Complete isolation from feature implementation packages (no direct engine imports)
 */
class AynvoraEngineRegistry(
    providers: List<AynvoraEngineProvider> = emptyList(),
    customEngines: Map<AynvoraFeatureId, AynvoraFeatureEngine> = emptyMap(),
) {
    private val lock = Any()
    private val providerMap = mutableMapOf<AynvoraFeatureId, AynvoraEngineProvider>()
    private val activeEngines = mutableMapOf<AynvoraFeatureId, AynvoraFeatureEngine>()
    private val lifecycleMap = mutableMapOf<AynvoraFeatureId, EngineLifecycleState>()

    init {
        providers.forEach { register(it) }
        customEngines.forEach { (id, engine) -> register(engine) }
    }

    /**
     * Register a lazy provider for a feature engine.
     * The engine is NOT instantiated until explicitly requested.
     */
    fun register(provider: AynvoraEngineProvider) {
        synchronized(lock) {
            providerMap[provider.featureId] = provider
            lifecycleMap[provider.featureId] = EngineLifecycleState.REGISTERED
            if (provider.featureId == AynvoraFeatureId.DAILY_GUIDANCE) {
                providerMap[AynvoraFeatureId.GUIDANCE] = provider
                lifecycleMap[AynvoraFeatureId.GUIDANCE] = EngineLifecycleState.REGISTERED
            } else if (provider.featureId == AynvoraFeatureId.GUIDANCE) {
                providerMap[AynvoraFeatureId.DAILY_GUIDANCE] = provider
                lifecycleMap[AynvoraFeatureId.DAILY_GUIDANCE] = EngineLifecycleState.REGISTERED
            }
        }
    }

    /**
     * Register an already-instantiated feature engine.
     */
    fun register(engine: AynvoraFeatureEngine) {
        synchronized(lock) {
            activeEngines[engine.featureId] = engine
            providerMap[engine.featureId] = engineProvider(engine.featureId) { engine }
            lifecycleMap[engine.featureId] = EngineLifecycleState.READY
            if (engine.featureId == AynvoraFeatureId.DAILY_GUIDANCE) {
                activeEngines[AynvoraFeatureId.GUIDANCE] = engine
                providerMap[AynvoraFeatureId.GUIDANCE] = engineProvider(AynvoraFeatureId.GUIDANCE) { engine }
                lifecycleMap[AynvoraFeatureId.GUIDANCE] = EngineLifecycleState.READY
            } else if (engine.featureId == AynvoraFeatureId.GUIDANCE) {
                activeEngines[AynvoraFeatureId.DAILY_GUIDANCE] = engine
                providerMap[AynvoraFeatureId.DAILY_GUIDANCE] = engineProvider(AynvoraFeatureId.DAILY_GUIDANCE) { engine }
                lifecycleMap[AynvoraFeatureId.DAILY_GUIDANCE] = EngineLifecycleState.READY
            }
        }
    }

    /**
     * Unregister an engine provider and any active instance.
     */
    fun unregister(featureId: AynvoraFeatureId) {
        synchronized(lock) {
            providerMap.remove(featureId)
            activeEngines.remove(featureId)
            lifecycleMap.remove(featureId)
        }
    }

    /**
     * Check whether an engine is available (either registered as a provider or already active).
     */
    fun isAvailable(featureId: AynvoraFeatureId): Boolean {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }
            return providerMap.containsKey(canonical) || activeEngines.containsKey(canonical)
        }
    }

    /**
     * Query the current lifecycle state of a feature engine.
     */
    fun getLifecycleState(featureId: AynvoraFeatureId): EngineLifecycleState {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }
            return lifecycleMap[canonical] ?: EngineLifecycleState.RELEASED
        }
    }

    /**
     * Update the lifecycle state (used by execution router).
     */
    fun setLifecycleState(featureId: AynvoraFeatureId, state: EngineLifecycleState) {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }
            lifecycleMap[canonical] = state
        }
    }

    /**
     * Lazily resolve and initialize the feature engine.
     * Transitions state: REGISTERED -> READY upon first call.
     */
    fun getEngine(featureId: AynvoraFeatureId): AynvoraFeatureEngine? {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }

            activeEngines[canonical]?.let { return it }

            val provider = providerMap[canonical] ?: return null
            return try {
                val engine = provider.create()
                activeEngines[canonical] = engine
                lifecycleMap[canonical] = EngineLifecycleState.READY
                engine
            } catch (t: Throwable) {
                lifecycleMap[canonical] = EngineLifecycleState.FAILED
                null
            }
        }
    }

    /**
     * Find active engine instance without instantiating if only registered.
     */
    fun find(featureId: AynvoraFeatureId): AynvoraFeatureEngine? {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }
            return activeEngines[canonical]
        }
    }

    /**
     * Release an active engine instance to free heavy resources (e.g. AI model memory, vision buffers).
     * The provider remains registered, so the engine can be lazily reloaded on subsequent demand.
     */
    fun release(featureId: AynvoraFeatureId) {
        synchronized(lock) {
            val canonical = when (featureId) {
                AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
                else -> featureId
            }
            activeEngines.remove(canonical)
            if (providerMap.containsKey(canonical)) {
                lifecycleMap[canonical] = EngineLifecycleState.RELEASED
            } else {
                lifecycleMap.remove(canonical)
            }
        }
    }

    /**
     * Release all active engine instances.
     */
    fun releaseAll() {
        synchronized(lock) {
            val activeKeys = activeEngines.keys.toList()
            activeKeys.forEach { release(it) }
        }
    }

    /**
     * Retrieve metadata for all currently available features.
     */
    fun getAllMetadata(): List<AynvoraEngineMetadata> {
        synchronized(lock) {
            val features = (providerMap.keys + activeEngines.keys).distinct()
            return features.map { featureId ->
                metadata(featureId)
            }
        }
    }

    /**
     * Retrieve metadata for a specific feature.
     */
    fun metadata(featureId: AynvoraFeatureId): AynvoraEngineMetadata {
        synchronized(lock) {
            val active = activeEngines[featureId]
            return AynvoraEngineMetadata(
                engineId = "com.aynvora.${featureId.name.lowercase()}",
                featureId = featureId,
                version = active?.version ?: "1.0.0",
                capabilities = active?.capabilities?.toList() ?: emptyList(),
                isOfflineCapable = true,
            )
        }
    }

    fun getRegisteredFeatures(): Set<AynvoraFeatureId> {
        synchronized(lock) {
            return (providerMap.keys + activeEngines.keys).toSet()
        }
    }
}

package com.aynvora.sdk

import com.aynvora.ai.engine.AiEngineProvider
import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.garudapuran.engine.GarudaPuranEngineProvider
import com.aynvora.gemstone.engine.GemstoneEngineProvider
import com.aynvora.gita.engine.GitaEngineProvider
import com.aynvora.guidance.engine.GuidanceEngineProvider
import com.aynvora.jadi.engine.JadiEngineProvider
import com.aynvora.numerology.engine.NumerologyEngineProvider
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.report.engine.ReportEngineProvider
import com.aynvora.rudraksha.engine.RudrakshaEngineProvider
import com.aynvora.tarot.engine.TarotEngineProvider
import com.aynvora.yantra.engine.YantraEngineProvider

/**
 * Convenience helper to instantiate providers for all official engines.
 * Only usable when all engine modules are on the runtime classpath (such as in official apps).
 */
object AynvoraBundledEngines {
    /**
     * Instantiates all providers whose classes exist on the runtime classpath.
     * Safely returns only available providers without crashing if some feature modules are omitted.
     */
    fun allProviders(): List<AynvoraEngineProvider> = listOfNotNull(
        safeCreate { AstroEngineProvider() },
        safeCreate { PalmEngineProvider() },
        safeCreate { NumerologyEngineProvider() },
        safeCreate { TarotEngineProvider() },
        safeCreate { GemstoneEngineProvider() },
        safeCreate { GitaEngineProvider() },
        safeCreate { GarudaPuranEngineProvider() },
        safeCreate { RudrakshaEngineProvider() },
        safeCreate { JadiEngineProvider() },
        safeCreate { YantraEngineProvider() },
        safeCreate { GuidanceEngineProvider() },
        safeCreate { AiEngineProvider() },
        safeCreate { ReportEngineProvider() },
    )

    private fun safeCreate(factory: () -> AynvoraEngineProvider): AynvoraEngineProvider? {
        return try {
            factory()
        } catch (_: Throwable) {
            null
        }
    }
}

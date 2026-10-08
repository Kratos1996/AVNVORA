package com.aynvora.sdk.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Automated architectural guard tests enforcing Part 39:
 * - Engine modules MUST NOT import Compose, Android UI, Firebase Analytics.
 * - Feature engines MUST NOT directly import other feature engines.
 * - Report and AI engines MUST NOT import feature calculations directly.
 * - UI MUST NOT import feature engine classes directly.
 */
class ArchitecturalGuardTest {

    private fun findRootProject(): File {
        var current = File(".").canonicalFile
        while (current.parentFile != null) {
            if (File(current, "settings.gradle.kts").exists()) {
                return current
            }
            current = current.parentFile
        }
        return File(".").canonicalFile
    }

    @Test
    fun testEnginesDoNotImportComposeOrAndroidUiOrFirebase() {
        val root = findRootProject()
        val enginesDir = File(root, "engines")
        val astroDir = File(root, "astro-engine")

        val allEngineDirs = listOfNotNull(
            astroDir.takeIf { it.exists() },
            *(enginesDir.listFiles { f -> f.isDirectory } ?: emptyArray())
        )

        val violations = mutableListOf<String>()

        allEngineDirs.forEach { dir ->
            val srcDir = File(dir, "src/commonMain")
            if (srcDir.exists()) {
                srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val lines = file.readLines()
                    lines.forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import androidx.compose")) {
                            violations.add("${file.relativeTo(root)}:${index + 1} imports Compose")
                        }
                        if (trimmed.startsWith("import android.view") || trimmed.startsWith("import android.app")) {
                            violations.add("${file.relativeTo(root)}:${index + 1} imports Android UI")
                        }
                        if (trimmed.startsWith("import com.google.firebase")) {
                            violations.add("${file.relativeTo(root)}:${index + 1} imports Firebase SDK")
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "Engine architectural violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testFeatureEnginesDoNotImportEachOther() {
        val root = findRootProject()
        val palmistrySrc = File(root, "engines/palmistry-engine/src/commonMain")
        val violations = mutableListOf<String>()

        if (palmistrySrc.exists()) {
            palmistrySrc.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.aynvora.astro.") ||
                        trimmed.startsWith("import com.aynvora.tarot.") ||
                        trimmed.startsWith("import com.aynvora.numerology.")
                    ) {
                        violations.add("${file.relativeTo(root)}:${index + 1} cross-imports another feature engine: $trimmed")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "Cross-engine dependency violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testReportEngineDoesNotImportFeatureImplementations() {
        val root = findRootProject()
        val reportSrc = File(root, "engines/report-engine/src/commonMain")
        val violations = mutableListOf<String>()

        if (reportSrc.exists()) {
            reportSrc.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.aynvora.astro.AstroEngine") ||
                        trimmed.startsWith("import com.aynvora.palmistry.PalmImageAnalysisEngine")
                    ) {
                        violations.add("${file.relativeTo(root)}:${index + 1} report-engine imports feature calculation: $trimmed")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "Report engine isolation violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testAiEngineDoesNotImportFeatureImplementations() {
        val root = findRootProject()
        val aiSrc = File(root, "engines/ai-engine/src/commonMain")
        val violations = mutableListOf<String>()

        if (aiSrc.exists()) {
            aiSrc.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.aynvora.astro.") ||
                        trimmed.startsWith("import com.aynvora.palmistry.") ||
                        trimmed.startsWith("import com.aynvora.tarot.")
                    ) {
                        violations.add("${file.relativeTo(root)}:${index + 1} ai-engine imports feature implementation: $trimmed")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "AI engine isolation violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testUiDoesNotImportFeatureEngineImplementations() {
        val root = findRootProject()
        val uiSrc = File(root, "ui/src/commonMain")
        val violations = mutableListOf<String>()

        if (uiSrc.exists()) {
            uiSrc.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.aynvora.astro.engine.") ||
                        trimmed.startsWith("import com.aynvora.palmistry.engine.") ||
                        trimmed.startsWith("import com.aynvora.tarot.engine.") ||
                        trimmed.startsWith("import com.aynvora.numerology.engine.") ||
                        trimmed.startsWith("import com.aynvora.gemstone.engine.") ||
                        trimmed.startsWith("import com.aynvora.gita.engine.") ||
                        trimmed.startsWith("import com.aynvora.garudapuran.engine.") ||
                        trimmed.startsWith("import com.aynvora.rudraksha.engine.") ||
                        trimmed.startsWith("import com.aynvora.jadi.engine.") ||
                        trimmed.startsWith("import com.aynvora.yantra.engine.") ||
                        trimmed.startsWith("import com.aynvora.guidance.engine.") ||
                        trimmed.startsWith("import com.aynvora.ai.engine.") ||
                        trimmed.startsWith("import com.aynvora.report.engine.")
                    ) {
                        violations.add("${file.relativeTo(root)}:${index + 1} UI directly imports feature engine: $trimmed")
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "UI engine isolation violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testDesignSystemDoesNotImportFeatureEngines() {
        val root = findRootProject()
        val dsSrc = File(root, "design-system/src/commonMain")
        val violations = mutableListOf<String>()

        if (dsSrc.exists()) {
            dsSrc.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.aynvora.astro.") ||
                        trimmed.startsWith("import com.aynvora.palmistry.") ||
                        trimmed.startsWith("import com.aynvora.tarot.") ||
                        trimmed.startsWith("import com.aynvora.ai.")
                    ) {
                        violations.add("${file.relativeTo(root)}:${index + 1} design-system imports feature: $trimmed")
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "design-system isolation violations found:\n${violations.joinToString("\n")}")
    }

    @Test
    fun testEnginesDoNotImportUiOrDesignSystem() {
        val root = findRootProject()
        val enginesDir = File(root, "engines")
        val astroDir = File(root, "astro-engine")
        val allEngineDirs = listOfNotNull(
            astroDir.takeIf { it.exists() },
            *(enginesDir.listFiles { f -> f.isDirectory } ?: emptyArray())
        )

        val violations = mutableListOf<String>()
        allEngineDirs.forEach { dir ->
            val srcDir = File(dir, "src/commonMain")
            if (srcDir.exists()) {
                srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    file.readLines().forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import com.aynvora.ui.") ||
                            trimmed.startsWith("import com.aynvora.designsystem.")
                        ) {
                            violations.add("${file.relativeTo(root)}:${index + 1} engine imports UI or design-system: $trimmed")
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "Engine importing UI violations found:\n${violations.joinToString("\n")}")
    }
}


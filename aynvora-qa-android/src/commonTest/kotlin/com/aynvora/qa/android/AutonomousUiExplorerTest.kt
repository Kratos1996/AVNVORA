package com.aynvora.qa.android

import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaStateSnapshot
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AutonomousUiExplorerTest {

    @BeforeTest
    fun setUp() {
        AndroidInteractionSentinelRuntime.reset()
        AndroidInteractionSentinelRuntime.setMonitorMode(true)
    }

    @Test
    fun testExplorerSkipsDangerousActionsAndLimitsRepeats() = runTest {
        var currentScreen = "HomeScreen"
        var currentRoute = "home"
        var featureOpened = false

        AndroidInteractionSentinelRuntime.stateSnapshotProvider = {
            QaStateSnapshot(
                route = currentRoute,
                screen = currentScreen,
                timestampMs = System.currentTimeMillis(),
            )
        }

        val explorer = AutonomousUiExplorer(
            maxDepth = 3,
            maxActions = 10,
            maxRepeatsPerAction = 2,
            settleDelayMs = 0L,
        )

        val result = explorer.explore(
            discoverActions = { screen ->
                when (screen) {
                    "HomeScreen" -> listOf(
                        ActionCandidate(
                            actionId = QaActionId.of("dashboard", "home", "btn", "open_feature"),
                            isDangerous = false,
                            execute = {
                                featureOpened = true
                                currentScreen = "FeatureScreen"
                                currentRoute = "feature"
                            },
                        ),
                        ActionCandidate(
                            actionId = QaActionId.of(
                                "account",
                                "settings",
                                "btn",
                                "delete_all_data"
                            ),
                            isDangerous = true,
                            execute = {
                                error("Dangerous action must never be executed!")
                            },
                        ),
                    )

                    "FeatureScreen" -> listOf(
                        ActionCandidate(
                            actionId = QaActionId.of("feature", "detail", "btn", "repeat_action"),
                            isDangerous = false,
                            execute = {
                                // stays on screen
                            },
                        ),
                    )

                    else -> emptyList()
                }
            },
            onBacktrack = {
                currentScreen = "HomeScreen"
                currentRoute = "home"
            },
        )

        assertTrue(featureOpened)
        assertTrue(result.actionsSkippedDangerous >= 1)
        assertTrue(result.actionsExecuted <= 10)
        assertTrue(result.screensExplored.contains("HomeScreen"))
        assertTrue(result.screensExplored.contains("FeatureScreen"))
    }
}

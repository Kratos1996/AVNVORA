package com.aynvora.data.ai

import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.data.storage.InMemoryStorageDriver
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiModelPersistenceRestartTest {

    @Test
    fun testModelSurvivesProcessRestart() = runBlocking {
        val persistentDriver = InMemoryStorageDriver()
        val variant = AiModelCatalog.allVariants.first()

        // 1. App Process 1: User downloads and installs model
        val repoProcess1 = AiModelStorageRepositoryImpl(driver = persistentDriver)
        val downloadRes = repoProcess1.downloadModel(variant) { _, _ -> }
        val tempPath = (downloadRes as com.aynvora.core.result.AynvoraResult.Success).value
        repoProcess1.commitInstallation(tempPath, variant)

        assertTrue(repoProcess1.isModelInstalled(variant.modelId))
        assertEquals(variant, repoProcess1.getInstalledModel())

        // 2. SIMULATE APP TERMINATION & COLD RESTART
        // repoProcess2 has completely blank in-memory state, only connected to persistentDriver
        val repoProcess2 = AiModelStorageRepositoryImpl(driver = persistentDriver)
        val lifecycleManagerProcess2 = AiModelLifecycleManager(repoProcess2)

        // Before initialize, defaults to NotInstalled
        assertEquals(AiModelLifecycleState.NotInstalled, lifecycleManagerProcess2.state.value)

        // Initialize runs on startup (just like in AynvoraAppViewModel)
        lifecycleManagerProcess2.initialize()

        // State must immediately be Ready!
        val state = lifecycleManagerProcess2.state.value
        assertTrue(state is AiModelLifecycleState.Ready)
        assertEquals(
            variant.modelId,
            (state as AiModelLifecycleState.Ready).installedVariant.modelId
        )
        assertNotNull(repoProcess2.getModelFilePath(variant.modelId))

        // 3. User uninstalls model
        lifecycleManagerProcess2.deleteInstalledModel()
        assertEquals(AiModelLifecycleState.NotInstalled, lifecycleManagerProcess2.state.value)

        // 4. SIMULATE RESTART AFTER UNINSTALLATION
        val repoProcess3 = AiModelStorageRepositoryImpl(driver = persistentDriver)
        val lifecycleManagerProcess3 = AiModelLifecycleManager(repoProcess3)
        lifecycleManagerProcess3.initialize()

        assertNull(repoProcess3.getInstalledModel())
        assertEquals(AiModelLifecycleState.NotInstalled, lifecycleManagerProcess3.state.value)
    }
}

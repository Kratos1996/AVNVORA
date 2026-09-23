package com.aynvora.data

import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.security.StorageCipher
import com.aynvora.data.storage.AynvoraStorageEngine
import com.aynvora.data.storage.InMemoryStorageDriver
import com.aynvora.data.storage.MigrationRunner
import com.aynvora.data.storage.StorageMigration
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StorageEngineTest {

    @Test
    fun testOfflineReadInitializesCleanDefaultContainer() {
        runBlocking {
            val driver = InMemoryStorageDriver()
            val engine = AynvoraStorageEngine(driver = driver)

            val result = engine.readContainer()
            assertIs<AynvoraResult.Success<*>>(result)
            // Verify stored in driver
            assertTrue(driver.exists(AynvoraStorageEngine.DEFAULT_STORAGE_KEY))
        }
    }

    @Test
    fun testCorruptedJsonPayloadReturnsCorruptedDataFailure() {
        runBlocking {
            val driver = InMemoryStorageDriver()
            // Corrupt storage payload with non-JSON text
            driver.write(AynvoraStorageEngine.DEFAULT_STORAGE_KEY, "{ not valid json !! }")

            val engine = AynvoraStorageEngine(driver = driver)
            val result = engine.readContainer()

            assertIs<AynvoraResult.Failure.CorruptedData>(result)
            assertEquals(AynvoraStorageEngine.DEFAULT_STORAGE_KEY, result.resourceId)
        }
    }

    @Test
    fun testSchemaDowngradeFailsDeterministically() {
        runBlocking {
            val driver = InMemoryStorageDriver()
            // Stored payload has a futuristic schema version 999
            val futuristicJson = """
                {
                    "schemaVersion": 999,
                    "userProfiles": {},
                    "birthProfiles": {},
                    "savedCharts": {},
                    "userPreferences": {
                        "theme": "SYSTEM",
                        "languageCode": "en",
                        "defaultCalculationProfile": "STANDARD_VEDIC",
                        "defaultAyanamsa": "LAHIRI_CHITRAPAKSHA",
                        "defaultHouseSystem": "EQUAL_HOUSE"
                    }
                }
            """.trimIndent()
            driver.write(AynvoraStorageEngine.DEFAULT_STORAGE_KEY, futuristicJson)

            val engine = AynvoraStorageEngine(driver = driver)
            val result = engine.readContainer()

            assertIs<AynvoraResult.Failure.MigrationFailure>(result)
            assertEquals(999, result.fromVersion)
            assertEquals(MigrationRunner.CURRENT_SCHEMA_VERSION, result.toVersion)
        }
    }

    @Test
    fun testSequentialMigrationExecution() {
        runBlocking {
            val migrationStep1to2 = object : StorageMigration {
                override val fromVersion: Int = 1
                override val toVersion: Int = 2
                override suspend fun migrate(containerJson: String): String {
                    return containerJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2")
                }
            }

            val runner = MigrationRunner(migrations = listOf(migrationStep1to2))
            val initialJson = "{\"schemaVersion\": 1, \"data\": \"ok\"}"
            val migrated = runner.execute(initialJson, currentVersion = 1, targetVersion = 2)

            assertEquals("{\"schemaVersion\": 2, \"data\": \"ok\"}", migrated)
        }
    }

    @Test
    fun testMissingMigrationStepFailsDeterministically() {
        runBlocking {
            val runner = MigrationRunner(migrations = emptyList())
            val initialJson = "{\"schemaVersion\": 1}"

            try {
                runner.execute(initialJson, currentVersion = 1, targetVersion = 2)
                kotlin.test.fail("Expected IllegalStateException for missing migration path")
            } catch (e: IllegalStateException) {
                assertTrue(e.message?.contains("Missing migration path") == true)
            }
        }
    }

    @Test
    fun testStorageCipherIntegration() {
        runBlocking {
            val testCipher = object : StorageCipher {
                override fun encrypt(plainText: String): String = "ENC::$plainText"
                override fun decrypt(cipherText: String): String {
                    if (!cipherText.startsWith("ENC::")) error("Invalid cipher prefix")
                    return cipherText.removePrefix("ENC::")
                }
            }

            val driver = InMemoryStorageDriver()
            val engine = AynvoraStorageEngine(driver = driver, cipher = testCipher)

            // Read/Init will write an encrypted payload
            val readResult = engine.readContainer()
            assertIs<AynvoraResult.Success<*>>(readResult)

            val rawStored = driver.read(AynvoraStorageEngine.DEFAULT_STORAGE_KEY)
            assertTrue(rawStored != null && rawStored.startsWith("ENC::"))

            // Clear cache and read again to test decryption flow
            engine.invalidateCache()
            val secondRead = engine.readContainer()
            assertIs<AynvoraResult.Success<*>>(secondRead)
        }
    }
}

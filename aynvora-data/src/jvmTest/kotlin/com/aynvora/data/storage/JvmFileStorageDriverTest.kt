package com.aynvora.data.storage

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmFileStorageDriverTest {

    private lateinit var testDir: File
    private lateinit var driver: JvmFileStorageDriver

    @BeforeTest
    fun setup() {
        testDir = File(System.getProperty("java.io.tmpdir"), "aynvora_test_${System.currentTimeMillis()}")
        testDir.mkdirs()
        driver = JvmFileStorageDriver(testDir)
    }

    @AfterTest
    fun tearDown() {
        testDir.deleteRecursively()
    }

    @Test
    fun testFilePersistenceAndAtomicWrite() {
        runBlocking {
            assertNull(driver.read("test_key"))
            assertFalse(driver.exists("test_key"))

            driver.write("test_key", "{\"hello\":\"world\"}")
            assertTrue(driver.exists("test_key"))
            assertEquals("{\"hello\":\"world\"}", driver.read("test_key"))

            val keys = driver.keys()
            assertTrue(keys.contains("test_key"))

            driver.delete("test_key")
            assertFalse(driver.exists("test_key"))
            assertNull(driver.read("test_key"))
        }
    }
}

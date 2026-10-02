package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.Aynvora
import com.aynvora.core.models.*
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VarshaphalDeterminismTest {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    private fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testVarshaphalJsonDeterminismThreeConsecutiveRuns() = runBlocking {
        val sdk = Aynvora.create()

        val request = ChartRequest(
            birthData = BirthData(
                date = BirthDate(2000, 4, 14),
                time = BirthTime(14, 30),
                place = BirthPlace("New Delhi", Coordinates(28.6139, 77.2090), "Asia/Kolkata"),
            ),
            config = CalculationConfig(houseSystem = HouseSystem.WHOLE_SIGN),
        )
        val targetYear = 2024

        // Run 1
        val outcome1 = sdk.calculateVarshaphal(request, targetYear)
        assertIs<AynvoraResult.Success<VarshaphalResult>>(outcome1)
        val res1 = outcome1.value
        val json1 = json.encodeToString(res1)
        val hash1 = sha256(json1)

        // Run 2
        val outcome2 = sdk.calculateVarshaphal(request, targetYear)
        assertIs<AynvoraResult.Success<VarshaphalResult>>(outcome2)
        val res2 = outcome2.value
        val json2 = json.encodeToString(res2)
        val hash2 = sha256(json2)

        // Run 3
        val outcome3 = sdk.calculateVarshaphal(request, targetYear)
        assertIs<AynvoraResult.Success<VarshaphalResult>>(outcome3)
        val res3 = outcome3.value
        val json3 = json.encodeToString(res3)
        val hash3 = sha256(json3)

        // Assert all 3 JSON hashes are byte-for-byte identical
        assertEquals(hash1, hash2, "Run 1 and Run 2 hashes must be identical")
        assertEquals(hash2, hash3, "Run 2 and Run 3 hashes must be identical")
        assertEquals(json1, json2, "Run 1 and Run 2 canonical JSON strings must match")
        assertEquals(json2, json3, "Run 2 and Run 3 canonical JSON strings must match")

        // Validate key deterministic fields
        assertEquals(res1.muntha?.sign, res2.muntha?.sign)
        assertEquals(res1.muntha?.longitude, res2.muntha?.longitude)
        assertEquals(res1.muntha?.lord, res2.muntha?.lord)
        assertEquals(res1.varsheshwara?.selectedPlanet, res2.varsheshwara?.selectedPlanet)
        assertEquals(res1.sahams.size, res2.sahams.size)
        assertEquals(res1.muddaDasha.size, res2.muddaDasha.size)
    }
}

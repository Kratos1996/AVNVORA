package com.aynvora.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AiModelCatalogTest {

    @Test
    fun catalogContainsVerifiedQwenVariants() {
        val variants = AiModelCatalog.allVariants
        assertTrue(variants.isNotEmpty())
        assertEquals(5, variants.size)

        for (v in variants) {
            assertTrue(v.modelId.isNotBlank())
            assertTrue(v.name.isNotBlank())
            assertTrue(v.fileSizeBytes > 100_000_000L, "Model file size must be substantial")
            assertEquals(
                64,
                v.sha256Checksum.length,
                "SHA-256 hash must be exactly 64 hex characters"
            )
            assertTrue(v.minRamBytes > 0)
            assertTrue(v.minFreeStorageBytes > v.fileSizeBytes)
            assertTrue(v.supportedLanguages.contains("en"))
            assertTrue(v.supportedLanguages.contains("hi"))
            assertEquals(AiRuntimeType.GGUF, v.runtime)
        }
    }

    @Test
    fun findByIdReturnsExactMatch() {
        val model = AiModelCatalog.findById("qwen2.5-0.5b-instruct-q4_k_m")
        assertNotNull(model)
        assertEquals("0.5B", model.parameterCount)
        assertEquals(AiQuantization.Q4_K_M, model.quantization)
    }

    @Test
    fun modelInfoConversionPreservesAttributes() {
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        val info = variant.toModelInfo(isAvailableLocally = true)

        assertEquals(variant.modelId, info.modelId)
        assertEquals(variant.name, info.name)
        assertEquals(variant.parameterCount, info.parameterCount)
        assertEquals(variant.quantization.name, info.quantizationFormat)
        assertEquals(variant.fileSizeBytes, info.fileSizeBytes)
        assertTrue(info.isAvailableLocally)
    }
}

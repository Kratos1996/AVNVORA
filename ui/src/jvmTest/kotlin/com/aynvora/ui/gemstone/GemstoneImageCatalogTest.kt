package com.aynvora.ui.gemstone

import com.aynvora.core.gemstone.GemstoneCatalog
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GemstoneImageCatalogTest {
    private val resources = File("../design-system/src/commonMain/composeResources")
    private val manifest = File(resources, "files/gemstone_image_manifest.json")

    @Test
    fun everyCatalogGemstoneHasOneBundledImageAndManifestEntry() {
        val catalogIds = GemstoneCatalog.NAVARATNA.map { it.id }
        val imageIds = GemstoneImageCatalog.assets.map { it.canonicalId }

        assertEquals(catalogIds.size, catalogIds.toSet().size, "Catalog canonical IDs must be unique")
        assertEquals(imageIds.size, imageIds.toSet().size, "Image canonical IDs must be unique")
        assertEquals(catalogIds.toSet(), imageIds.toSet(), "Each catalog gemstone must map to exactly one image")
        assertTrue(manifest.isFile, "Image provenance manifest must be bundled")

        val manifestText = manifest.readText()
        for (asset in GemstoneImageCatalog.assets) {
            val bundledFile = File(resources, asset.resourcePath)
            assertTrue(bundledFile.isFile, "Missing bundled asset ${asset.resourcePath}")
            assertTrue(manifestText.contains("\"canonical_id\": \"${asset.canonicalId}\""))
            assertTrue(manifestText.contains("\"filename\": \"${asset.resourcePath}\""))
            assertTrue(manifestText.contains(asset.sourcePage))
            assertEquals("NOT_VERIFIED", asset.licenseStatus)
        }
    }

    @Test
    fun noOrphanDrawableFilesExist() {
        val drawableDir = File(resources, "drawable")
        val productionFiles = drawableDir.listFiles()
            ?.filter {
                it.isFile && it.name.startsWith("gem_") &&
                    it.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp")
            }
            ?.map { "drawable/${it.name}" }
            ?.toSet()
            .orEmpty()
        val referencedFiles = GemstoneImageCatalog.assets.map { it.resourcePath }.toSet()
        assertNotNull(drawableDir.listFiles(), "Gemstone drawable directory must exist")
        assertEquals(referencedFiles, productionFiles, "Bundled gemstone images must not be orphaned")
    }
}

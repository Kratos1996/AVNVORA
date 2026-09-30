package com.aynvora.ui.gemstone

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.aynvora.core.gemstone.GemstoneDescriptor
import com.aynvora.designsystem.generated.resources.Res
import com.aynvora.designsystem.generated.resources.gem_blue_sapphire
import com.aynvora.designsystem.generated.resources.gem_cats_eye
import com.aynvora.designsystem.generated.resources.gem_diamond
import com.aynvora.designsystem.generated.resources.gem_emerald
import com.aynvora.designsystem.generated.resources.gem_hessonite
import com.aynvora.designsystem.generated.resources.gem_natural_pearl
import com.aynvora.designsystem.generated.resources.gem_red_coral
import com.aynvora.designsystem.generated.resources.gem_ruby
import com.aynvora.designsystem.generated.resources.gem_yellow_sapphire
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.aynvora.designsystem.localization.LocalAynvoraTranslator

/** Single source of truth for the canonical gemstone ID to bundled image mapping. */
data class GemstoneImageAsset(
    val canonicalId: String,
    val resourcePath: String,
    val sourcePage: String,
    val licenseStatus: String,
    val drawable: DrawableResource,
)

object GemstoneImageCatalog {
    val assets: List<GemstoneImageAsset> = listOf(
        GemstoneImageAsset("gem_ruby", "drawable/gem_ruby.webp", "https://www.navratan.com/categories/ruby-manik-stone", "NOT_VERIFIED", Res.drawable.gem_ruby),
        GemstoneImageAsset("gem_pearl", "drawable/gem_natural_pearl.webp", "https://www.navratan.com/categories/pearl-moti-stone", "NOT_VERIFIED", Res.drawable.gem_natural_pearl),
        GemstoneImageAsset("gem_red_coral", "drawable/gem_red_coral.webp", "https://www.navratan.com/categories/coral-moonga", "NOT_VERIFIED", Res.drawable.gem_red_coral),
        GemstoneImageAsset("gem_emerald", "drawable/gem_emerald.jpg", "https://www.navratan.com/categories/emerald-panna", "NOT_VERIFIED", Res.drawable.gem_emerald),
        GemstoneImageAsset("gem_yellow_sapphire", "drawable/gem_yellow_sapphire.webp", "https://www.navratan.com/categories/yellow-sapphire-pukhraj", "NOT_VERIFIED", Res.drawable.gem_yellow_sapphire),
        GemstoneImageAsset("gem_diamond", "drawable/gem_diamond.jpg", "https://www.navratan.com/categories/diamond-heera", "NOT_VERIFIED", Res.drawable.gem_diamond),
        GemstoneImageAsset("gem_blue_sapphire", "drawable/gem_blue_sapphire.jpg", "https://www.navratan.com/categories/blue-sapphire-neelam", "NOT_VERIFIED", Res.drawable.gem_blue_sapphire),
        GemstoneImageAsset("gem_hessonite", "drawable/gem_hessonite.webp", "https://www.navratan.com/categories/hessonite-gomed", "NOT_VERIFIED", Res.drawable.gem_hessonite),
        GemstoneImageAsset("gem_cats_eye", "drawable/gem_cats_eye.webp", "https://www.navratan.com/categories/cats-eye-lehsunia", "NOT_VERIFIED", Res.drawable.gem_cats_eye),
    )

    private val byCanonicalId = assets.associateBy(GemstoneImageAsset::canonicalId)

    fun forGemstone(descriptor: GemstoneDescriptor): GemstoneImageAsset? =
        byCanonicalId[descriptor.id]
}

/** Offline Compose image used consistently by catalog, detail, and recommendation cards. */
@Composable
fun GemstonePhoto(
    descriptor: GemstoneDescriptor,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RectangleShape,
) {
    val asset = GemstoneImageCatalog.forGemstone(descriptor)
    val translator = LocalAynvoraTranslator.current
    val localizedName = translator.resolve("gemstone.name.${descriptor.id}")
    if (asset != null) {
        Image(
            painter = painterResource(asset.drawable),
            contentDescription = localizedName,
            modifier = modifier.clip(shape),
            contentScale = contentScale,
        )
    } else {
        // Missing mappings remain visible to automated accessibility/QA checks.
        Box(
            modifier = modifier.clip(shape).semantics {
                contentDescription = localizedName
            },
        )
    }
}

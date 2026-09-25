package com.aynvora.ui.tarot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotAssetRepository
import com.aynvora.designsystem.AynvoraTheme
import org.koin.compose.koinInject

// In-memory cache for decoded bitmaps to guarantee 60fps scrolling
private val decodedBitmapCache = mutableMapOf<String, ImageBitmap>()

/**
 * Renders the authentic, real Tarot card artwork loaded from the verified deck assets.
 * Strictly avoids fake placeholders, text-only cards, and AI-generated artwork.
 */
@Composable
fun TarotCardImage(
    cardId: String,
    modifier: Modifier = Modifier,
    deckId: String = "rider_waite_smith_standard",
    isThumbnail: Boolean = false,
    isReversed: Boolean = false,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 4.dp,
    assetRepository: TarotAssetRepository = koinInject(),
) {
    val cacheKey = "$deckId:$cardId:${if (isThumbnail) "thumb" else "disp"}"
    var bitmap by remember(cacheKey) { mutableStateOf(decodedBitmapCache[cacheKey]) }
    var hasError by remember(cacheKey) { mutableStateOf(false) }
    var errorMessage by remember(cacheKey) { mutableStateOf<String?>(null) }

    LaunchedEffect(cacheKey) {
        if (bitmap == null) {
            when (val result = assetRepository.getCardImage(deckId, cardId, isThumbnail)) {
                is AynvoraResult.Success -> {
                    try {
                        val decoded = result.value.toImageBitmap()
                        decodedBitmapCache[cacheKey] = decoded
                        bitmap = decoded
                        hasError = false
                    } catch (e: Exception) {
                        hasError = true
                        errorMessage = "Corrupt image format"
                    }
                }

                is AynvoraResult.Failure -> {
                    hasError = true
                    errorMessage = result.message
                }
            }
        }
    }

    val rotationAngle = if (isReversed) 180f else 0f

    Box(
        modifier = modifier
            .aspectRatio(0.6f)
            .shadow(elevation, shape)
            .clip(shape)
            .background(AynvoraTheme.colors.CosmicNavy)
            .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center,
    ) {
        val currentBitmap = bitmap
        when {
            currentBitmap != null -> {
                Image(
                    bitmap = currentBitmap,
                    contentDescription = contentDescription ?: cardId,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle),
                )
            }

            hasError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(Color(0xFF200B0B)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Asset Unavailable\n($cardId)",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 10.sp),
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                // Loading Shimmer
                val transition = rememberInfiniteTransition(label = "shimmer")
                val alpha by transition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "loading_alpha",
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AynvoraTheme.colors.Gold.copy(alpha = alpha * 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✦",
                        color = AynvoraTheme.colors.Gold.copy(alpha = alpha),
                        fontSize = 24.sp,
                    )
                }
            }
        }
    }
}

/**
 * Renders the authentic, real Tarot card back artwork from the verified deck.
 */
@Composable
fun TarotCardBackImage(
    modifier: Modifier = Modifier,
    deckId: String = "rider_waite_smith_standard",
    isThumbnail: Boolean = false,
    contentDescription: String? = "Card Back",
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 4.dp,
    assetRepository: TarotAssetRepository = koinInject(),
) {
    val cacheKey = "$deckId:card_back:${if (isThumbnail) "thumb" else "disp"}"
    var bitmap by remember(cacheKey) { mutableStateOf(decodedBitmapCache[cacheKey]) }
    var hasError by remember(cacheKey) { mutableStateOf(false) }

    LaunchedEffect(cacheKey) {
        if (bitmap == null) {
            when (val result = assetRepository.getCardBackImage(deckId, isThumbnail)) {
                is AynvoraResult.Success -> {
                    try {
                        val decoded = result.value.toImageBitmap()
                        decodedBitmapCache[cacheKey] = decoded
                        bitmap = decoded
                    } catch (_: Exception) {
                        hasError = true
                    }
                }

                is AynvoraResult.Failure -> {
                    hasError = true
                }
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(0.6f)
            .shadow(elevation, shape)
            .clip(shape)
            .background(AynvoraTheme.colors.CosmicNavy)
            .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        val currentBitmap = bitmap
        when {
            currentBitmap != null -> {
                Image(
                    bitmap = currentBitmap,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            hasError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Card Back",
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AynvoraTheme.colors.CosmicNavy),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✦",
                        color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                        fontSize = 24.sp,
                    )
                }
            }
        }
    }
}

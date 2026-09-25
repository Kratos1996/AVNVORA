package com.aynvora.ui.tarot

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes raw JPEG or PNG byte array into a Compose [ImageBitmap].
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap

package com.aynvora.ui.tarot

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: error("Failed to decode byte array to ImageBitmap")
    return bitmap.asImageBitmap()
}

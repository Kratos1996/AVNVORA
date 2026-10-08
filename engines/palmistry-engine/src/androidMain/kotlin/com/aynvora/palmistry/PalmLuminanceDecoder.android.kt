package com.aynvora.palmistry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.max

actual fun decodeImageToLuminanceGrid(
    bytes: ByteArray,
    targetWidth: Int,
    targetHeight: Int,
): LuminanceGrid? {
    if (bytes.isEmpty()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val origW = bounds.outWidth
        val origH = bounds.outHeight
        if (origW <= 0 || origH <= 0) return null

        var sampleSize = 1
        val maxDim = max(origW, origH)
        val targetMax = max(targetWidth, targetHeight)
        while (maxDim / (sampleSize * 2) >= targetMax) {
            sampleSize *= 2
        }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null
        val scaled = if (decoded.width != targetWidth || decoded.height != targetHeight) {
            Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true).also {
                if (it != decoded) decoded.recycle()
            }
        } else {
            decoded
        }

        val intPixels = IntArray(targetWidth * targetHeight)
        scaled.getPixels(intPixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
        scaled.recycle()

        val floats = FloatArray(targetWidth * targetHeight)
        for (i in intPixels.indices) {
            val color = intPixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            // Standard Rec. 601 luma formula: 0.299 R + 0.587 G + 0.114 B
            floats[i] = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
        }
        LuminanceGrid(targetWidth, targetHeight, floats)
    } catch (_: Throwable) {
        null
    }
}

package com.aynvora.ui.palmistry

import android.graphics.BitmapFactory

/**
 * Android actual: uses [BitmapFactory.Options] with [BitmapFactory.Options.inJustDecodeBounds] = true
 * to read JPEG/PNG header dimensions without allocating the full bitmap in memory.
 */
actual fun decodePalmImageDimensions(bytes: ByteArray): Pair<Int, Int> {
    if (bytes.isEmpty()) return Pair(0, 0)
    return try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        val w = opts.outWidth
        val h = opts.outHeight
        if (w > 0 && h > 0) Pair(w, h) else Pair(0, 0)
    } catch (_: Exception) {
        Pair(0, 0)
    }
}

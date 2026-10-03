package com.aynvora.ui.palmistry

import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import javax.imageio.stream.MemoryCacheImageInputStream

/**
 * JVM (Desktop) actual: uses [ImageIO] to read image dimensions from the byte stream
 * without fully decoding the image. Falls back to full read if stream-based reading fails.
 */
actual fun decodePalmImageDimensions(bytes: ByteArray): Pair<Int, Int> {
    if (bytes.isEmpty()) return Pair(0, 0)
    return try {
        // Try stream-based reading (no full decode)
        val stream = MemoryCacheImageInputStream(ByteArrayInputStream(bytes))
        val readers = ImageIO.getImageReaders(stream)
        if (readers.hasNext()) {
            val reader = readers.next()
            try {
                reader.input = stream
                val w = reader.getWidth(0)
                val h = reader.getHeight(0)
                if (w > 0 && h > 0) Pair(w, h) else Pair(0, 0)
            } finally {
                reader.dispose()
                stream.close()
            }
        } else {
            // Fallback: full decode to get dimensions
            val img = ImageIO.read(ByteArrayInputStream(bytes))
            if (img != null) Pair(img.width, img.height) else Pair(0, 0)
        }
    } catch (_: Exception) {
        Pair(0, 0)
    }
}

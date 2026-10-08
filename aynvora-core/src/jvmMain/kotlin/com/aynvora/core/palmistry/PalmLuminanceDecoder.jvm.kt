package com.aynvora.core.palmistry

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual fun decodeImageToLuminanceGrid(
    bytes: ByteArray,
    targetWidth: Int,
    targetHeight: Int,
): LuminanceGrid? {
    if (bytes.isEmpty()) return null
    return try {
        val img = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
        val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val g = scaled.createGraphics()
        g.drawImage(img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null)
        g.dispose()

        val floats = FloatArray(targetWidth * targetHeight)
        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val rgb = scaled.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val gVal = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                floats[y * targetWidth + x] = (0.299f * r + 0.587f * gVal + 0.114f * b) / 255.0f
            }
        }
        LuminanceGrid(targetWidth, targetHeight, floats)
    } catch (_: Throwable) {
        null
    }
}

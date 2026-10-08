package com.aynvora.palmistry

/**
 * 2D normalized luminance matrix representing decoded image pixel brightness.
 * Values are in [0.0..1.0] where 0.0 is pure black and 1.0 is pure white.
 */
data class LuminanceGrid(
    val width: Int,
    val height: Int,
    val pixels: FloatArray,
) {
    init {
        require(width > 0 && height > 0) { "Grid dimensions must be positive" }
        require(pixels.size == width * height) { "Pixels size (${pixels.size}) must match width*height (${width * height})" }
    }

    fun get(x: Int, y: Int): Float {
        if (x !in 0 until width || y !in 0 until height) return 0.5f
        return pixels[y * width + x]
    }

    fun getNormalized(normX: Float, normY: Float): Float {
        val px = (normX.coerceIn(0f, 1f) * (width - 1)).toInt()
        val py = (normY.coerceIn(0f, 1f) * (height - 1)).toInt()
        return get(px, py)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LuminanceGrid) return false
        if (width != other.width || height != other.height) return false
        return pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

/**
 * Expect declaration for platform-native image decoding into an efficient grayscale luminance grid.
 */
expect fun decodeImageToLuminanceGrid(
    bytes: ByteArray,
    targetWidth: Int = 160,
    targetHeight: Int = 160,
): LuminanceGrid?

fun createFallbackLuminanceGrid(
    bytes: ByteArray,
    sourceWidth: Int = 0,
    sourceHeight: Int = 0,
    targetWidth: Int = 160,
    targetHeight: Int = 160,
): LuminanceGrid {
    val pixels = FloatArray(targetWidth * targetHeight)
    if (bytes.isEmpty()) {
        pixels.fill(0.5f)
        return LuminanceGrid(targetWidth, targetHeight, pixels)
    }

    val sW = when {
        sourceWidth > 0 -> sourceWidth
        bytes.size >= 1000 && (bytes.size % 480 == 0) -> 480
        bytes.size >= 1000 && (bytes.size % 640 == 0) -> 640
        else -> bytes.size
    }
    val sH = when {
        sourceHeight > 0 -> sourceHeight
        sW > 0 && bytes.size >= sW -> bytes.size / sW
        else -> 1
    }

    if (sW > 1 && sH > 1 && bytes.size >= sW * sH) {
        for (ty in 0 until targetHeight) {
            val sy = (ty * sH) / targetHeight
            val rowOffset = sy * sW
            for (tx in 0 until targetWidth) {
                val sx = (tx * sW) / targetWidth
                val byteVal = (bytes[rowOffset + sx].toInt() and 0xFF) / 255.0f
                pixels[ty * targetWidth + tx] = byteVal
            }
        }
        return LuminanceGrid(targetWidth, targetHeight, pixels)
    }

    val byteLen = bytes.size
    for (ty in 0 until targetHeight) {
        for (tx in 0 until targetWidth) {
            val idx = (ty * targetWidth + tx) % byteLen
            val byteVal = (bytes[idx].toInt() and 0xFF) / 255.0f
            pixels[ty * targetWidth + tx] = byteVal
        }
    }
    return LuminanceGrid(targetWidth, targetHeight, pixels)
}

package com.aynvora.ui.palmistry

/**
 * Decodes the pixel dimensions of a JPEG/PNG image from its raw byte array.
 *
 * Returns (width, height) in pixels.
 * Returns (0, 0) if the data cannot be decoded (empty, corrupt, or unsupported format).
 *
 * This function does NOT allocate the full decoded bitmap — it only reads image header
 * metadata to extract dimension information.
 */
expect fun decodePalmImageDimensions(bytes: ByteArray): Pair<Int, Int>

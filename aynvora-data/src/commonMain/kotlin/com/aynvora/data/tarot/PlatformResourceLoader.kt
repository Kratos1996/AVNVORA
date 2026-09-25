package com.aynvora.data.tarot

/**
 * Loads raw bytes for a packaged resource from the application bundle/classpath.
 */
expect fun loadTarotResourceBytes(resourcePath: String): ByteArray?

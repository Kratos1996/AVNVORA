package com.aynvora.data.tarot

private class JvmResourceLoaderSentinel

actual fun loadTarotResourceBytes(resourcePath: String): ByteArray? {
    val clean = resourcePath.removePrefix("/")
    val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(clean)
        ?: JvmResourceLoaderSentinel::class.java.classLoader?.getResourceAsStream(clean)
        ?: ClassLoader.getSystemResourceAsStream(clean)
    return stream?.use { it.readBytes() }
}

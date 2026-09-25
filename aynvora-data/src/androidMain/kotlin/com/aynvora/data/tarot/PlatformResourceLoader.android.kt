package com.aynvora.data.tarot

private class AndroidResourceLoaderSentinel

actual fun loadTarotResourceBytes(resourcePath: String): ByteArray? {
    val clean = resourcePath.removePrefix("/")
    val stream = AndroidResourceLoaderSentinel::class.java.classLoader?.getResourceAsStream(clean)
        ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(clean)
        ?: ClassLoader.getSystemResourceAsStream(clean)
    return stream?.use { it.readBytes() }
}

package com.aynvora.qa.android

actual object AndroidExceptionCorrelator {
    private var attached = false

    actual fun attach() {
        if (attached) return
        attached = true
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AndroidInteractionSentinelRuntime.recordUnhandledException(throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}

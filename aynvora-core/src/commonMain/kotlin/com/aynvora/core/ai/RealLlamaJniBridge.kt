package com.aynvora.core.ai

/**
 * Production JNI Bridge connecting Kotlin to native llama.cpp (libllama.so).
 *
 * Implements Step 5 of Phase 10.11:
 * - Attempts to link the native shared library via [System.loadLibrary].
 * - If libllama.so is missing, captures the [UnsatisfiedLinkError] with full diagnostic fidelity.
 * - Guarantees that [isAvailable] returns true ONLY when dynamic linking succeeded.
 * - Prevents silent crashes, SIGSEGV, or fake native generation.
 */
class RealLlamaJniBridge : NativeLibraryBridge {

    companion object {
        const val LIBRARY_NAME = "llama"

        private var isLoaded: Boolean = false
        private var linkError: Throwable? = null

        init {
            try {
                try {
                    System.loadLibrary("omp")
                } catch (_: Throwable) {
                    // OpenMP runtime may be statically linked
                }
                try {
                    System.loadLibrary("ggml")
                } catch (_: Throwable) {
                    // ggml may be statically linked or packed with llama
                }
                System.loadLibrary(LIBRARY_NAME)
                isLoaded = true
            } catch (t: Throwable) {
                isLoaded = false
                linkError = t
            }
        }

        fun isLibraryLoaded(): Boolean = isLoaded

        fun getLinkError(): Throwable? = linkError

        fun getLinkStatusMessage(): String {
            return if (isLoaded) {
                "LOADED (lib$LIBRARY_NAME.so verified and linked via JNI)"
            } else {
                val err = linkError
                if (err != null) {
                    "UNLINKED (${err::class.simpleName}: ${err.message})"
                } else {
                    "NOT_VERIFIED (lib$LIBRARY_NAME.so pending build packaging)"
                }
            }
        }
    }

    override fun isAvailable(): Boolean = isLoaded

    override fun load(modelPath: String, contextLength: Int, threads: Int): Long {
        if (!isLoaded) return -1L
        return try {
            nativeLoad(modelPath, contextLength, threads)
        } catch (_: Throwable) {
            -1L
        }
    }

    override fun generate(
        handle: Long,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        onTokenGenerated: (String) -> Boolean,
    ): String {
        if (!isLoaded || handle <= 0L) {
            throw IllegalStateException("Cannot execute native inference: lib$LIBRARY_NAME.so is not linked")
        }
        return nativeGenerate(handle, prompt, maxTokens, temperature)
    }

    override fun cancel(handle: Long) {
        if (isLoaded && handle > 0L) {
            try {
                nativeCancel(handle)
            } catch (_: Throwable) {
                // Ignore cancellation failures
            }
        }
    }

    override fun release(handle: Long) {
        if (isLoaded && handle > 0L) {
            try {
                nativeRelease(handle)
            } catch (_: Throwable) {
                // Ignore teardown failures
            }
        }
    }

    // JNI Native function bindings
    private external fun nativeLoad(modelPath: String, contextLength: Int, threads: Int): Long
    private external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int, temperature: Float): String
    private external fun nativeCancel(handle: Long)
    private external fun nativeRelease(handle: Long)
}

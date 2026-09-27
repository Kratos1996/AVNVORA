package com.aynvora.ui.gemstone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGemstoneCertificatePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit,
): GemstoneCertificatePicker {
    return remember {
        object : GemstoneCertificatePicker {
            override fun launchCamera() {
                // Desktop / JVM fallback: supply sample certificate data
                val sampleCert = "AYNVORA_SAMPLE_CERTIFICATE_METADATA_JVM".encodeToByteArray()
                onImageCaptured(sampleCert, true)
            }

            override fun launchGallery() {
                // Desktop / JVM fallback: supply sample certificate data
                val sampleCert = "AYNVORA_SAMPLE_CERTIFICATE_METADATA_JVM".encodeToByteArray()
                onImageCaptured(sampleCert, false)
            }
        }
    }
}

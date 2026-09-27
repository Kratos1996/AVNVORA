package com.aynvora.ui.gemstone

import androidx.compose.runtime.Composable

/**
 * Multiplatform abstraction for launching device camera or photo picker to capture
 * gemstone laboratory certificate images.
 */
interface GemstoneCertificatePicker {
    fun launchCamera()
    fun launchGallery()
}

@Composable
expect fun rememberGemstoneCertificatePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit,
): GemstoneCertificatePicker

package com.aynvora.ui.palmistry

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic interface for invoking the real camera and system photo picker.
 */
interface PalmImagePicker {
    fun launchCamera()
    fun launchGallery()
}

/**
 * Creates and remembers a [PalmImagePicker] tailored to the execution platform.
 * On Android, utilizes [androidx.activity.result.contract.ActivityResultContracts] with zero unnecessary permissions.
 * On JVM / Desktop, provides a safe fallback for regression tests.
 */
@Composable
expect fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit = {},
): PalmImagePicker

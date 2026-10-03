package com.aynvora.ui.palmistry

import androidx.compose.runtime.Composable
import com.aynvora.core.palmistry.PalmCaptureError

/**
 * Platform-agnostic interface for invoking the real camera and system photo picker.
 */
interface PalmImagePicker {
    fun launchCamera()
    fun launchGallery()
}

/**
 * Creates and remembers a [PalmImagePicker] tailored to the execution platform.
 * On Android, utilizes [androidx.activity.result.contract.ActivityResultContracts] with EXIF rotation and size normalization.
 * On JVM / Desktop, provides file picker for gallery and truthful unsupported status for camera hardware.
 */
@Composable
expect fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (PalmCaptureError) -> Unit = {},
): PalmImagePicker

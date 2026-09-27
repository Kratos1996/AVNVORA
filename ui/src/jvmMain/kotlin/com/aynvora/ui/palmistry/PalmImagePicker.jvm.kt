package com.aynvora.ui.palmistry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit,
): PalmImagePicker {
    return remember {
        object : PalmImagePicker {
            override fun launchCamera() {
                val mockBytes = ByteArray(1024) { (it % 255).toByte() }
                onImageCaptured(mockBytes, true)
            }

            override fun launchGallery() {
                val mockBytes = ByteArray(2048) { ((it * 2) % 255).toByte() }
                onImageCaptured(mockBytes, false)
            }
        }
    }
}

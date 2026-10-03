package com.aynvora.ui.palmistry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.aynvora.core.palmistry.PalmCaptureError
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.max

@Composable
actual fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (PalmCaptureError) -> Unit,
): PalmImagePicker {
    return remember {
        object : PalmImagePicker {
            override fun launchCamera() {
                // Truthfully report that camera hardware capture is unsupported on desktop environments
                onError(
                    PalmCaptureError.cameraUnavailable(
                        "Direct camera hardware capture is not supported on Desktop. Please select an image from your files using the Gallery option, or use a mobile device."
                    )
                )
            }

            override fun launchGallery() {
                try {
                    val chooser = JFileChooser().apply {
                        dialogTitle = "Select Palm Photo"
                        fileFilter = FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png")
                        isMultiSelectionEnabled = false
                    }
                    val result = chooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val file = chooser.selectedFile
                        if (file != null && file.exists() && file.length() > 0) {
                            val originalImage = ImageIO.read(file)
                            if (originalImage != null) {
                                val normalizedBytes = normalizeJvmImage(originalImage)
                                onImageCaptured(normalizedBytes, false)
                            } else {
                                onError(PalmCaptureError.invalidImage("Selected file could not be decoded as an image"))
                            }
                        } else {
                            onError(PalmCaptureError.invalidImage("Selected file is empty or does not exist"))
                        }
                    } else {
                        onError(PalmCaptureError.cancelled("File selection was cancelled"))
                    }
                } catch (e: Exception) {
                    onError(PalmCaptureError.captureFailed("Failed to load selected image: ${e.message}"))
                }
            }
        }
    }
}

/**
 * Normalizes desktop input image to standard JPEG format, capping max dimension at 1920px.
 */
private fun normalizeJvmImage(img: BufferedImage): ByteArray {
    val maxDim = max(img.width, img.height)
    val scaledImg = if (maxDim > 1920) {
        val factor = 1920.0 / maxDim
        val targetW = (img.width * factor).toInt()
        val targetH = (img.height * factor).toInt()
        val scaled = BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB)
        val g = scaled.createGraphics()
        g.drawImage(img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH), 0, 0, null)
        g.dispose()
        scaled
    } else {
        if (img.type == BufferedImage.TYPE_INT_RGB) img else {
            val converted = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
            val g = converted.createGraphics()
            g.drawImage(img, 0, 0, null)
            g.dispose()
            converted
        }
    }

    return ByteArrayOutputStream().use { output ->
        ImageIO.write(scaledImg, "jpg", output)
        output.toByteArray()
    }
}

package com.aynvora.ui.palmistry

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.aynvora.core.palmistry.PalmCaptureError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

@Composable
actual fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (PalmCaptureError) -> Unit,
): PalmImagePicker {
    val context = LocalContext.current
    // Wrap callbacks in rememberUpdatedState so the activity result launchers always
    // reference the latest lambda even when the parent composable recomposes.
    val currentOnImageCaptured by rememberUpdatedState(onImageCaptured)
    val currentOnError by rememberUpdatedState(onError)
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val file = tempCameraFile
        if (success && file != null && file.exists() && file.length() > 0) {
            try {
                val rawBytes = file.readBytes()
                val normalizedBytes = normalizeImageBytes(rawBytes, file.absolutePath)
                if (normalizedBytes.isNotEmpty()) {
                    currentOnImageCaptured(normalizedBytes, true)
                } else {
                    currentOnError(PalmCaptureError.invalidImage("Normalized image data was empty"))
                }
            } catch (e: Exception) {
                currentOnError(PalmCaptureError.captureFailed("Failed to process captured camera image: ${e.message}"))
            } finally {
                runCatching { file.delete() }
            }
        } else if (!success) {
            if (file != null && file.exists()) {
                runCatching { file.delete() }
            }
            currentOnError(PalmCaptureError.cancelled("Camera capture was cancelled by user"))
        } else {
            currentOnError(PalmCaptureError.captureFailed("No image data captured from camera"))
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            launchCameraInternal(
                context,
                { tempCameraFile = it },
                { tempCameraUri = it },
                takePictureLauncher::launch,
                currentOnError,
            )
        } else {
            currentOnError(PalmCaptureError.permissionDenied())
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val normalizedBytes = normalizeImageBytes(bytes, null)
                    if (normalizedBytes.isNotEmpty()) {
                        currentOnImageCaptured(normalizedBytes, false)
                    } else {
                        currentOnError(PalmCaptureError.invalidImage("Selected gallery image was empty after processing"))
                    }
                } else {
                    currentOnError(PalmCaptureError.invalidImage("Selected gallery image was empty"))
                }
            } catch (e: Exception) {
                currentOnError(PalmCaptureError.captureFailed("Failed to read gallery image: ${e.message}"))
            }
        } else {
            currentOnError(PalmCaptureError.cancelled("Gallery photo selection was cancelled"))
        }
    }

    return remember(context) {
        object : PalmImagePicker {
            override fun launchCamera() {
                val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                if (!hasCamera) {
                    currentOnError(PalmCaptureError.cameraUnavailable("Device does not report camera hardware"))
                    return
                }

                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    launchCameraInternal(
                        context,
                        { tempCameraFile = it },
                        { tempCameraUri = it },
                        takePictureLauncher::launch,
                        currentOnError,
                    )
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            override fun launchGallery() {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        }
    }
}

private fun launchCameraInternal(
    context: Context,
    setFile: (File) -> Unit,
    setUri: (Uri) -> Unit,
    launch: (Uri) -> Unit,
    onError: (PalmCaptureError) -> Unit,
) {
    try {
        val cacheDir = File(context.cacheDir, "palm_images").apply { mkdirs() }
        val photoFile = File(cacheDir, "palm_capture_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.report-files"
        val photoUri = FileProvider.getUriForFile(context, authority, photoFile)
        setFile(photoFile)
        setUri(photoUri)
        launch(photoUri)
    } catch (e: Exception) {
        onError(PalmCaptureError.captureFailed("Unable to initialize camera capture: ${e.message}"))
    }
}

/**
 * Normalizes input image by:
 * 1. Correcting EXIF orientation so image is upright.
 * 2. Downsampling to a max dimension of 1920px to prevent memory exhaustion during line analysis.
 * 3. Compressing to a standard JPEG byte stream.
 */
internal fun normalizeImageBytes(bytes: ByteArray, filePath: String?): ByteArray {
    if (bytes.isEmpty()) return bytes

    return try {
        // Read EXIF orientation
        val orientation = if (filePath != null && File(filePath).exists()) {
            ExifInterface(filePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } else {
            ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        // Measure bounds
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        val origWidth = boundsOptions.outWidth
        val origHeight = boundsOptions.outHeight

        if (origWidth <= 0 || origHeight <= 0) return bytes

        // Calculate sample size for max dimension of 1920
        val maxDim = max(origWidth, origHeight)
        var sampleSize = 1
        while (maxDim / sampleSize > 1920) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return bytes

        val transformedBitmap = if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            if (rotated != decoded) decoded.recycle()
            rotated
        } else {
            decoded
        }

        val output = ByteArrayOutputStream()
        transformedBitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
        transformedBitmap.recycle()
        output.toByteArray()
    } catch (_: Exception) {
        bytes
    }
}

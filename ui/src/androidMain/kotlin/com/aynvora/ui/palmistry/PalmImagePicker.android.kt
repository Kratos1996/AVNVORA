package com.aynvora.ui.palmistry

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberPalmImagePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit,
): PalmImagePicker {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            val file = tempCameraFile
            if (file != null && file.exists() && file.length() > 0) {
                try {
                    val bytes = file.readBytes()
                    onImageCaptured(bytes, true)
                } catch (e: Exception) {
                    onError("Failed to read captured camera image: ${e.message}")
                }
            } else {
                onError("No image data captured from camera")
            }
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
                onError
            )
        } else {
            onError("Camera permission denied. Please allow camera access in Settings.")
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    onImageCaptured(bytes, false)
                } else {
                    onError("Selected gallery image was empty")
                }
            } catch (e: Exception) {
                onError("Failed to read gallery image: ${e.message}")
            }
        }
    }

    return remember(context) {
        object : PalmImagePicker {
            override fun launchCamera() {
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
                        onError
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
    onError: (String) -> Unit,
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
        onError("Unable to launch camera: ${e.message}")
    }
}

package com.aynvora.ui.gemstone

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
actual fun rememberGemstoneCertificatePicker(
    onImageCaptured: (ByteArray, isCamera: Boolean) -> Unit,
    onError: (String) -> Unit,
): GemstoneCertificatePicker {
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
                onError("No certificate image data captured from camera")
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
                    onError("Selected certificate image was empty")
                }
            } catch (e: Exception) {
                onError("Failed to read certificate image: ${e.message}")
            }
        }
    }

    return remember(context) {
        object : GemstoneCertificatePicker {
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
    launchTakePicture: (Uri) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val cacheDir = File(context.cacheDir, "shared-reports").apply { mkdirs() }
        val file = File.createTempFile("cert_capture_", ".jpg", cacheDir)
        setFile(file)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        setUri(uri)
        launchTakePicture(uri)
    } catch (e: Exception) {
        onError("Could not prepare camera storage: ${e.message}")
    }
}

package com.taiwanlife.teamwalk.utils

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaPickerHelper(
    private val activity: ComponentActivity,
    private val authority: String,
    private val onResult: (Uri?) -> Unit
) {

    private var currentImageUri: Uri? = null

    private val cameraLauncher =
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            onResult(if (success) currentImageUri else null)
        }

    private val imagePickerLauncher =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onResult(uri)
        }

    private val filePickerLauncher =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            onResult(uri)
        }

    fun launchCamera() {
        val photoFile = createImageFile()
        currentImageUri = FileProvider.getUriForFile(activity, authority, photoFile)
        currentImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }

    fun pickFile(mimeTypes: Array<String> = arrayOf("*/*")) {
        filePickerLauncher.launch(mimeTypes)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timestamp}_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }
}

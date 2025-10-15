package com.vergil.lottery.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object ImagePicker {


    @Composable
    fun rememberImagePicker(
        onImageSelected: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                coroutineScope.launch {
                    try {


                        onImageSelected(selectedUri.toString())
                    } catch (e: Exception) {
                        onError("图片选择失败: ${e.message}")
                    }
                }
            }
        }


        object {
            fun launch() {
                launcher.launch("image/*")
            }
        }
    }

    suspend fun copyImageToInternalStorage(
        context: Context,
        uri: Uri,
        fileName: String = "custom_background.jpg"
    ): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }


    fun deleteCustomBackground(context: Context, fileName: String = "custom_background.jpg") {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }


    fun hasCustomBackground(context: Context, fileName: String = "custom_background.jpg"): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }
}

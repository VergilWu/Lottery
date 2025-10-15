package com.vergil.lottery.presentation.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


@Composable
fun rememberImagePickerLauncher(
    context: Context,
    onImageSelected: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                try {
                    val filePath = copyImageToInternalStorage(context, selectedUri)
                    onImageSelected(filePath)
                } catch (e: Exception) {
                    onError("图片处理失败: ${e.message}")
                }
            }
        }
    }


    return {
        launcher.launch("image/*")
    }
}

private suspend fun copyImageToInternalStorage(
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

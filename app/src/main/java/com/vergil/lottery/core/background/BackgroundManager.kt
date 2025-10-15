package com.vergil.lottery.core.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class BackgroundManager(private val context: Context) {

    companion object {
        private const val CUSTOM_BACKGROUND_FILE = "custom_background.jpg"
        private const val DEFAULT_BACKGROUND_LIGHT = "wallpaper_light"
        private const val DEFAULT_BACKGROUND_DARK = "wallpaper_dark"
    }


    fun getCurrentBackgroundResId(isDarkTheme: Boolean): Int {
        return if (hasCustomBackground()) {

            getCustomBackgroundResId()
        } else {

            getDefaultBackgroundResId(isDarkTheme)
        }
    }


    fun getCurrentBackgroundPath(): String? {
        return if (hasCustomBackground()) {
            getCustomBackgroundPath()
        } else {
            null
        }
    }


    private fun getDefaultBackgroundResId(isDarkTheme: Boolean): Int {
        return if (isDarkTheme) {
            context.resources.getIdentifier(DEFAULT_BACKGROUND_DARK, "drawable", context.packageName)
        } else {
            context.resources.getIdentifier(DEFAULT_BACKGROUND_LIGHT, "drawable", context.packageName)
        }
    }


    private fun getCustomBackgroundResId(): Int {


        return getDefaultBackgroundResId(false)
    }


    fun hasCustomBackground(): Boolean {
        val file = File(context.filesDir, CUSTOM_BACKGROUND_FILE)
        return file.exists()
    }


    fun getCustomBackgroundPath(): String? {
        val file = File(context.filesDir, CUSTOM_BACKGROUND_FILE)
        return if (file.exists()) file.absolutePath else null
    }


    suspend fun setCustomBackground(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, CUSTOM_BACKGROUND_FILE)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        }
    }


    fun deleteCustomBackground() {
        val file = File(context.filesDir, CUSTOM_BACKGROUND_FILE)
        if (file.exists()) {
            file.delete()
        }
    }


    suspend fun getBackgroundBitmap(isDarkTheme: Boolean): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (hasCustomBackground()) {
                val file = File(context.filesDir, CUSTOM_BACKGROUND_FILE)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            } else {
                val resId = getDefaultBackgroundResId(isDarkTheme)
                BitmapFactory.decodeResource(context.resources, resId)
            }
        }
    }
}


@Composable
fun rememberBackgroundManager(): BackgroundManager {
    val context = LocalContext.current
    return remember { BackgroundManager(context) }
}

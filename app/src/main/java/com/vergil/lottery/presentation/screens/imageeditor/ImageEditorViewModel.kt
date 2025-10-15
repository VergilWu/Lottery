package com.vergil.lottery.presentation.screens.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.mvi.MviViewModel
import com.vergil.lottery.di.AppModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class ImageEditorViewModel(
    private val context: Context = AppModule.applicationContext
) : MviViewModel<ImageEditorContract.Intent, ImageEditorContract.State, ImageEditorContract.Effect>(
    initialState = ImageEditorContract.State()
) {

    override fun handleIntent(intent: ImageEditorContract.Intent) {
        when (intent) {
            is ImageEditorContract.Intent.LoadImage -> {
                loadImage(intent.imagePath)
            }
            is ImageEditorContract.Intent.RotateLeft -> {
                rotateImage(-90f)
            }
            is ImageEditorContract.Intent.RotateRight -> {
                rotateImage(90f)
            }
            is ImageEditorContract.Intent.ZoomIn -> {
                zoomImage(1.2f)
            }
            is ImageEditorContract.Intent.ZoomOut -> {
                zoomImage(0.8f)
            }
            is ImageEditorContract.Intent.Reset -> {
                resetImage()
            }
            is ImageEditorContract.Intent.SaveImage -> {
                saveImage()
            }
        }
    }

    fun loadImage(imagePath: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(imagePath)
                }

                if (bitmap != null) {
                    setState { 
                        copy(
                            imageBitmap = bitmap,
                            isLoading = false,
                            rotation = 0f,
                            scale = 1f
                        ) 
                    }
                    Timber.d("Image loaded successfully: $imagePath")
                } else {
                    setState { 
                        copy(
                            isLoading = false,
                            error = "无法加载图片"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load image")
                setState { 
                    copy(
                        isLoading = false,
                        error = "加载图片失败: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun rotateImage(degrees: Float) {
        val currentState = state.value
        val newRotation = (currentState.rotation + degrees) % 360f

        setState { copy(rotation = newRotation) }
        Timber.d("Image rotated by $degrees degrees, new rotation: $newRotation")
    }

    private fun zoomImage(factor: Float) {
        val currentState = state.value
        val newScale = (currentState.scale * factor).coerceIn(0.1f, 5.0f)

        setState { copy(scale = newScale) }
        Timber.d("Image zoomed by factor $factor, new scale: $newScale")
    }

    private fun resetImage() {
        setState { 
            copy(
                rotation = 0f,
                scale = 1f
            ) 
        }
        Timber.d("Image reset to original state")
    }

    private fun saveImage() {
        val currentState = state.value
        val originalBitmap = currentState.imageBitmap

        if (originalBitmap == null) {
            setEffect(ImageEditorContract.Effect.ShowToast("没有可保存的图片"))
            return
        }

        viewModelScope.launch {
            try {
                val editedBitmap = withContext(Dispatchers.IO) {
                    applyTransformations(originalBitmap, currentState.rotation, currentState.scale)
                }

                val savedPath = withContext(Dispatchers.IO) {
                    saveBitmapToFile(editedBitmap)
                }

                setEffect(ImageEditorContract.Effect.SaveImage(savedPath))
                Timber.d("Image saved successfully: $savedPath")

            } catch (e: Exception) {
                Timber.e(e, "Failed to save image")
                setEffect(ImageEditorContract.Effect.ShowToast("保存失败: ${e.message}"))
            }
        }
    }

    private fun applyTransformations(bitmap: Bitmap, rotation: Float, scale: Float): Bitmap {
        val matrix = Matrix()


        if (rotation != 0f) {
            matrix.postRotate(rotation)
        }


        if (scale != 1f) {
            matrix.postScale(scale, scale)
        }

        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(context.filesDir, "edited_background.jpg")
        val outputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }
}

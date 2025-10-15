package com.vergil.lottery.presentation.screens.imageeditor

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState


object ImageEditorContract {


    sealed interface Intent : MviIntent {
        data class LoadImage(val imagePath: String) : Intent
        data object RotateLeft : Intent
        data object RotateRight : Intent
        data object ZoomIn : Intent
        data object ZoomOut : Intent
        data object Reset : Intent
        data object SaveImage : Intent
    }


    @Immutable
    data class State(
        val imageBitmap: Bitmap? = null,
        val rotation: Float = 0f,
        val scale: Float = 1f,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class SaveImage(val imagePath: String) : Effect
    }
}

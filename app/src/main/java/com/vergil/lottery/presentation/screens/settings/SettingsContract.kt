package com.vergil.lottery.presentation.screens.settings

import androidx.compose.runtime.Immutable
import com.vergil.lottery.core.mvi.MviEffect
import com.vergil.lottery.core.mvi.MviIntent
import com.vergil.lottery.core.mvi.MviState


object SettingsContract {


    sealed interface Intent : MviIntent {
        data object SelectCustomBackground : Intent  
        data object ResetToDefaultBackground : Intent  
        data class SetUseCustomBackground(val enabled: Boolean) : Intent  
        data class SetCardOpacity(val opacity: Float) : Intent  
        data object ResetCardOpacityToDefault : Intent  
    }


    @Immutable
    data class State(
        val customBackgroundPath: String? = null,  
        val useCustomBackground: Boolean = false,  
        val cardOpacity: Float = 0.8f,  
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MviState


    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data object OpenImagePicker : Effect  
    }
}

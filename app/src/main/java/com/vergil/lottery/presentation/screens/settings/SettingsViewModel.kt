package com.vergil.lottery.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.mvi.MviViewModel
import com.vergil.lottery.data.local.preferences.PreferencesManager
import com.vergil.lottery.di.AppModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


class SettingsViewModel(
    private val preferencesManager: PreferencesManager = AppModule.preferencesManager
) : MviViewModel<SettingsContract.Intent, SettingsContract.State, SettingsContract.Effect>(
    initialState = SettingsContract.State()
) {

    init {

        loadBackgroundSettings()
    }

    override fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.SelectCustomBackground -> {
                setEffect(SettingsContract.Effect.OpenImagePicker)
            }

            is SettingsContract.Intent.ResetToDefaultBackground -> {
                resetToDefaultBackground()
            }

            is SettingsContract.Intent.SetUseCustomBackground -> {
                setUseCustomBackground(intent.enabled)
            }

            is SettingsContract.Intent.SetCardOpacity -> {
                setCardOpacity(intent.opacity)
            }

            is SettingsContract.Intent.ResetCardOpacityToDefault -> {
                resetCardOpacityToDefault()
            }
        }
    }

    private fun loadBackgroundSettings() {

        preferencesManager.customBackgroundPath
            .onEach { path ->
                setState { copy(customBackgroundPath = path) }
            }
            .launchIn(viewModelScope)


        preferencesManager.useCustomBackground
            .onEach { enabled ->
                setState { copy(useCustomBackground = enabled) }
            }
            .launchIn(viewModelScope)


        preferencesManager.cardOpacity
            .onEach { opacity ->
                setState { copy(cardOpacity = opacity) }
            }
            .launchIn(viewModelScope)
    }

    private fun resetToDefaultBackground() {
        viewModelScope.launch {
            try {
                preferencesManager.resetToDefaultBackground()
                setEffect(SettingsContract.Effect.ShowToast("已恢复默认背景"))
                Timber.d("Background reset to default")
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset background")
                setEffect(SettingsContract.Effect.ShowToast("恢复失败"))
            }
        }
    }

    private fun setUseCustomBackground(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setUseCustomBackground(enabled)
                val message = if (enabled) "已启用自定义背景" else "已关闭自定义背景"
                setEffect(SettingsContract.Effect.ShowToast(message))
                Timber.d("Custom background enabled: $enabled")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set custom background")
                setEffect(SettingsContract.Effect.ShowToast("设置失败"))
            }
        }
    }


    fun setCustomBackgroundPath(path: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setCustomBackgroundPath(path)
                setEffect(SettingsContract.Effect.ShowToast("背景设置成功"))
                Timber.d("Custom background path set: $path")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set custom background path")
                setEffect(SettingsContract.Effect.ShowToast("设置失败"))
            }
        }
    }

    private fun setCardOpacity(opacity: Float) {
        viewModelScope.launch {
            try {
                preferencesManager.setCardOpacity(opacity)
                Timber.d("Card opacity set: $opacity")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set card opacity")
                setEffect(SettingsContract.Effect.ShowToast("设置失败"))
            }
        }
    }

    private fun resetCardOpacityToDefault() {
        viewModelScope.launch {
            try {
                preferencesManager.resetCardOpacityToDefault()
                setEffect(SettingsContract.Effect.ShowToast("已恢复默认透明度"))
                Timber.d("Card opacity reset to default")
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset card opacity")
                setEffect(SettingsContract.Effect.ShowToast("恢复失败"))
            }
        }
    }
}

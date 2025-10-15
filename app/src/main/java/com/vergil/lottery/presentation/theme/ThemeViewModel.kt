package com.vergil.lottery.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.data.local.preferences.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber


class ThemeViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {


    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )


    val autoThemeEnabled: StateFlow<Boolean> = preferencesManager.autoThemeEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )


    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesManager.setThemeMode(mode)
                Timber.d("Theme mode changed to: $mode")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set theme mode")
            }
        }
    }


    fun setAutoThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setAutoThemeEnabled(enabled)
                Timber.d("Auto theme enabled: $enabled")
            } catch (e: Exception) {
                Timber.e(e, "Failed to set auto theme")
            }
        }
    }
}


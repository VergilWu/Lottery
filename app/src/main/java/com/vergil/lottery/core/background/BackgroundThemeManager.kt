package com.vergil.lottery.core.background

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.vergil.lottery.core.constants.ThemeMode
import com.vergil.lottery.data.local.preferences.PreferencesManager
import com.vergil.lottery.di.AppModule
import kotlinx.coroutines.flow.combine


class BackgroundThemeManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val backgroundManager = BackgroundManager(context)


    fun getCurrentBackgroundResId(isDarkTheme: Boolean): Int {
        return backgroundManager.getCurrentBackgroundResId(isDarkTheme)
    }


    fun shouldUseCustomBackground(): Boolean {
        return backgroundManager.hasCustomBackground()
    }


    suspend fun setCustomBackground(uri: android.net.Uri): String {
        return backgroundManager.setCustomBackground(uri)
    }


    fun deleteCustomBackground() {
        backgroundManager.deleteCustomBackground()
    }
}


@Composable
fun rememberBackgroundThemeManager(): BackgroundThemeManager {
    val context = LocalContext.current
    val preferencesManager = AppModule.preferencesManager
    return remember { BackgroundThemeManager(context, preferencesManager) }
}


data class BackgroundState(
    val backgroundResId: Int,
    val isCustomBackground: Boolean,
    val isLoading: Boolean = false
)


@Composable
fun rememberBackgroundState(
    themeMode: ThemeMode,
    useCustomBackground: Boolean
): BackgroundState {
    val backgroundThemeManager = rememberBackgroundThemeManager()
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val backgroundResId = backgroundThemeManager.getCurrentBackgroundResId(isDarkTheme)
    val isCustomBackground = backgroundThemeManager.shouldUseCustomBackground() && useCustomBackground

    return BackgroundState(
        backgroundResId = backgroundResId,
        isCustomBackground = isCustomBackground
    )
}

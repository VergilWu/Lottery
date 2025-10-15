package com.vergil.lottery.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.constants.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lottery_preferences")


class PreferencesManager(private val context: Context) {

    private val dataStore = context.dataStore


    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            val ordinal = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
            ThemeMode.fromOrdinal(ordinal)
        }


    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.ordinal
        }
    }


    val defaultLotteryType: Flow<LotteryType> = dataStore.data
        .map { preferences ->
            val code = preferences[PreferencesKeys.DEFAULT_LOTTERY_TYPE] ?: LotteryType.SSQ.code
            LotteryType.fromCode(code) ?: LotteryType.SSQ
        }


    suspend fun setDefaultLotteryType(type: LotteryType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_LOTTERY_TYPE] = type.code
        }
    }


    val userName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "彩票达人"
        }


    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }


    val favoriteModeEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FAVORITE_MODE_ENABLED] == 1
        }


    suspend fun toggleFavoriteMode() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_MODE_ENABLED] ?: 0
            preferences[PreferencesKeys.FAVORITE_MODE_ENABLED] = if (current == 1) 0 else 1
        }
    }


    val autoThemeEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_THEME_ENABLED] == 1
        }


    suspend fun setAutoThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_THEME_ENABLED] = if (enabled) 1 else 0
        }
    }


    val customBackgroundPath: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CUSTOM_BACKGROUND_PATH]
        }


    suspend fun setCustomBackgroundPath(path: String?) {
        dataStore.edit { preferences ->
            if (path != null) {
                preferences[PreferencesKeys.CUSTOM_BACKGROUND_PATH] = path
            } else {
                preferences.remove(PreferencesKeys.CUSTOM_BACKGROUND_PATH)
            }
        }
    }


    val useCustomBackground: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_CUSTOM_BACKGROUND] == 1
        }


    suspend fun setUseCustomBackground(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_CUSTOM_BACKGROUND] = if (enabled) 1 else 0
        }
    }


    suspend fun resetToDefaultBackground() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CUSTOM_BACKGROUND_PATH)
            preferences[PreferencesKeys.USE_CUSTOM_BACKGROUND] = 0
        }
    }


    val cardOpacity: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CARD_OPACITY] ?: 0.8f 
        }


    suspend fun setCardOpacity(opacity: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARD_OPACITY] = opacity.coerceIn(0.0f, 1.0f)
        }
    }


    suspend fun resetCardOpacityToDefault() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARD_OPACITY] = 0.8f
        }
    }


    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Timber.d("All preferences cleared")
    }
}


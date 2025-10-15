package com.vergil.lottery.data.local.preferences

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferencesKeys {

    val DATASTORE_VERSION = intPreferencesKey("datastore_version_v2")


    const val CURRENT_VERSION = 2


    val THEME_MODE = intPreferencesKey("theme_mode_v2")


    val DEFAULT_LOTTERY_TYPE = stringPreferencesKey("default_lottery_type_v2")


    val USER_NAME = stringPreferencesKey("user_name_v2")


    val FAVORITE_MODE_ENABLED = intPreferencesKey("favorite_mode_enabled_v2")


    val AUTO_THEME_ENABLED = intPreferencesKey("auto_theme_enabled_v2")


    val CUSTOM_BACKGROUND_PATH = stringPreferencesKey("custom_background_path_v2")


    val USE_CUSTOM_BACKGROUND = intPreferencesKey("use_custom_background_v2")


    val CARD_OPACITY = floatPreferencesKey("card_opacity_v1")

}


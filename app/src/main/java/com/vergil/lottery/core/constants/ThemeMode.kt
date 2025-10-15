package com.vergil.lottery.core.constants


enum class ThemeMode {
    LIGHT,    
    DARK,     
    SYSTEM;   

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode {
            return entries.getOrNull(ordinal) ?: SYSTEM
        }
    }
}


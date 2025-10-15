package com.vergil.lottery.core.constants

import com.vergil.lottery.BuildConfig

object AppConstants {
    const val BASE_URL = "https://www.szxk365.com/api/openapi.lottery/"
    const val API_KEY = BuildConfig.API_KEY
    const val API_TIMEOUT_CONNECT = 10_000L
    const val API_TIMEOUT_READ = 30_000L
    const val API_TIMEOUT_WRITE = 30_000L

    const val DATABASE_NAME = "lottery_db"
    const val DATABASE_VERSION = 1

    const val DATASTORE_NAME = "lottery_preferences"

    const val CACHE_MAX_SIZE = 50
    const val CACHE_EXPIRATION_MINUTES = 5L

    const val HISTORY_DEFAULT_SIZE = 100
    const val HISTORY_MAX_SIZE = 200

    const val DEBUG_ADMIN_USERNAME = "admin"
    const val DEBUG_ADMIN_PASSWORD = "admin@lottery2025"
}


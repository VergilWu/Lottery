package com.vergil.lottery.data.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.model.DrawResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit


class CacheManager(private val context: Context) {

    companion object {
        private const val CACHE_DURATION_HOURS = 10L
        private const val CACHE_VERSION_KEY = "cache_version"
        private const val LAST_UPDATE_KEY_PREFIX = "last_update_"
        private const val CACHE_DATA_KEY_PREFIX = "cache_data_"
        private const val IS_CACHE_VALID_KEY_PREFIX = "is_cache_valid_"

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lottery_cache")
    }

    private val dataStore = context.dataStore


    suspend fun isCacheValid(lotteryType: LotteryType): Boolean {
        val lastUpdateKey = longPreferencesKey("${LAST_UPDATE_KEY_PREFIX}${lotteryType.code}")
        val lastUpdate = dataStore.data.map { it[lastUpdateKey] }.first() ?: 0L
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdate
        val isValid = timeDiff < TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS)

        Timber.d("Cache check for ${lotteryType.code}: lastUpdate=$lastUpdate, timeDiff=${timeDiff}ms, isValid=$isValid")
        return isValid
    }


    suspend fun getLastUpdateTime(lotteryType: LotteryType): Long {
        val lastUpdateKey = longPreferencesKey("${LAST_UPDATE_KEY_PREFIX}${lotteryType.code}")
        return dataStore.data.map { it[lastUpdateKey] ?: 0L }.first()
    }


    suspend fun updateCache(lotteryType: LotteryType, data: String, version: String? = null) {
        val currentTime = System.currentTimeMillis()


        val lastUpdateKey = longPreferencesKey("${LAST_UPDATE_KEY_PREFIX}${lotteryType.code}")
        val cacheDataKey = stringPreferencesKey("${CACHE_DATA_KEY_PREFIX}${lotteryType.code}")
        val isValidKey = booleanPreferencesKey("${IS_CACHE_VALID_KEY_PREFIX}${lotteryType.code}")

        dataStore.edit { preferences ->
            preferences[lastUpdateKey] = currentTime
            preferences[cacheDataKey] = data
            preferences[isValidKey] = true


            version?.let { v ->
                val versionKey = stringPreferencesKey("${CACHE_VERSION_KEY}_${lotteryType.code}")
                preferences[versionKey] = v
            }
        }

        Timber.d("Cache updated for ${lotteryType.code} at $currentTime")
    }


    suspend fun getCachedData(lotteryType: LotteryType): String? {
        val cacheDataKey = stringPreferencesKey("${CACHE_DATA_KEY_PREFIX}${lotteryType.code}")
        val isValidKey = booleanPreferencesKey("${IS_CACHE_VALID_KEY_PREFIX}${lotteryType.code}")

        val data = dataStore.data.map { preferences ->
            val data = preferences[cacheDataKey]
            val isValid = preferences[isValidKey] ?: false
            if (isValid) data else null
        }.first()

        Timber.d("Retrieved cached data for ${lotteryType.code}: ${data?.length ?: 0} chars")
        return data
    }


    suspend fun clearCache(lotteryType: LotteryType) {
        val lastUpdateKey = longPreferencesKey("${LAST_UPDATE_KEY_PREFIX}${lotteryType.code}")
        val cacheDataKey = stringPreferencesKey("${CACHE_DATA_KEY_PREFIX}${lotteryType.code}")
        val isValidKey = booleanPreferencesKey("${IS_CACHE_VALID_KEY_PREFIX}${lotteryType.code}")
        val versionKey = stringPreferencesKey("${CACHE_VERSION_KEY}_${lotteryType.code}")

        dataStore.edit { preferences ->
            preferences.remove(lastUpdateKey)
            preferences.remove(cacheDataKey)
            preferences.remove(isValidKey)
            preferences.remove(versionKey)
        }

        Timber.d("Cache cleared for ${lotteryType.code}")
    }


    suspend fun clearAllCache() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Timber.d("All cache cleared")
    }


    suspend fun getCacheStats(): Map<LotteryType, CacheStats> {
        val stats = mutableMapOf<LotteryType, CacheStats>()

        for (lotteryType in LotteryType.entries) {
            val lastUpdate = getLastUpdateTime(lotteryType)
            val isValid = isCacheValid(lotteryType)
            val cachedData = getCachedData(lotteryType)

            stats[lotteryType] = CacheStats(
                lastUpdate = lastUpdate,
                isValid = isValid,
                dataSize = cachedData?.length ?: 0,
                hasData = !cachedData.isNullOrEmpty()
            )
        }

        return stats
    }


    suspend fun shouldRefreshInBackground(lotteryType: LotteryType): Boolean {
        val lastUpdate = getLastUpdateTime(lotteryType)
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdate
        val refreshThreshold = TimeUnit.HOURS.toMillis(8) 

        return timeDiff > refreshThreshold
    }
}


data class CacheStats(
    val lastUpdate: Long,
    val isValid: Boolean,
    val dataSize: Int,
    val hasData: Boolean
) {
    val ageInHours: Long
        get() = (System.currentTimeMillis() - lastUpdate) / TimeUnit.HOURS.toMillis(1)

    val isExpired: Boolean
        get() = ageInHours >= 10
}

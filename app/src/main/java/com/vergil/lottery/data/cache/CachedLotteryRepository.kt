package com.vergil.lottery.data.cache

import android.content.Context
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.core.util.Result
import com.vergil.lottery.data.repository.LotteryRepository
import com.vergil.lottery.domain.model.DrawResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString


class CachedLotteryRepository(
    private val context: Context,
    private val repository: LotteryRepository
) {

    private val cacheManager = CacheManager(context)
    private val json = Json { ignoreUnknownKeys = true }

    fun getLatestDraw(lotteryCode: String): Flow<Result<DrawResult>> {
        val lotteryType = LotteryType.fromCode(lotteryCode) ?: return flow { 
            emit(Result.Error(Exception("Invalid lottery code: $lotteryCode")))
        }

        return flow {

            val isCacheValid = cacheManager.isCacheValid(lotteryType)

            if (isCacheValid) {

                val cachedData = cacheManager.getCachedData(lotteryType)
                if (!cachedData.isNullOrEmpty()) {
                    try {
                        val drawResult = json.decodeFromString<DrawResult>(cachedData)
                        Timber.d("Returning cached data for $lotteryCode")
                        emit(Result.Success(drawResult))


                        if (cacheManager.shouldRefreshInBackground(lotteryType)) {
                            refreshInBackground(lotteryCode)
                        }
                        return@flow
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to parse cached data for $lotteryCode")

                        cacheManager.clearCache(lotteryType)
                    }
                }
            }


            Timber.d("Cache invalid or empty for $lotteryCode, fetching from network")
            repository.getLatestDraw(lotteryCode)
                .catch { exception ->

                    val cachedData = cacheManager.getCachedData(lotteryType)
                    if (!cachedData.isNullOrEmpty()) {
                        try {
                            val drawResult = json.decodeFromString<DrawResult>(cachedData)
                            Timber.d("Network failed, returning stale cache for $lotteryCode")
                            emit(Result.Success(drawResult))
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to parse stale cache for $lotteryCode")
                            emit(Result.Error(exception))
                        }
                    } else {
                        emit(Result.Error(exception))
                    }
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {

                            val jsonData = json.encodeToString(result.data)
                            cacheManager.updateCache(lotteryType, jsonData)
                            emit(result)
                        }
                        is Result.Error -> emit(result)
                        is Result.Loading -> emit(result)
                    }
                }
        }
    }

    fun getHistory(lotteryCode: String, size: Int, forceRefresh: Boolean): Flow<Result<List<DrawResult>>> {
        val lotteryType = LotteryType.fromCode(lotteryCode) ?: return flow { 
            emit(Result.Error(Exception("Invalid lottery code: $lotteryCode")))
        }

        return flow {

            if (forceRefresh) {
                cacheManager.clearCache(lotteryType)
            }


            val isCacheValid = cacheManager.isCacheValid(lotteryType)

            if (isCacheValid) {

                val cachedData = cacheManager.getCachedData(lotteryType)
                if (!cachedData.isNullOrEmpty()) {
                    try {
                        val historyList = json.decodeFromString<List<DrawResult>>(cachedData)
                        Timber.d("Returning cached history for $lotteryCode (${historyList.size} items)")
                        emit(Result.Success(historyList))


                        if (cacheManager.shouldRefreshInBackground(lotteryType)) {
                            refreshHistoryInBackground(lotteryCode, size)
                        }
                        return@flow
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to parse cached history for $lotteryCode")
                        cacheManager.clearCache(lotteryType)
                    }
                }
            }


            Timber.d("Cache invalid for $lotteryCode history, fetching from network")
            repository.getHistory(lotteryCode, size, forceRefresh)
                .catch { exception ->

                    val cachedData = cacheManager.getCachedData(lotteryType)
                    if (!cachedData.isNullOrEmpty()) {
                        try {
                            val historyList = json.decodeFromString<List<DrawResult>>(cachedData)
                            Timber.d("Network failed, returning stale cache for $lotteryCode history")
                            emit(Result.Success(historyList))
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to parse stale cache for $lotteryCode history")
                            emit(Result.Error(exception))
                        }
                    } else {
                        emit(Result.Error(exception))
                    }
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {

                            val jsonData = json.encodeToString(result.data)
                            cacheManager.updateCache(lotteryType, jsonData)
                            emit(result)
                        }
                        is Result.Error -> emit(result)
                        is Result.Loading -> emit(result)
                    }
                }
        }
    }


    private suspend fun refreshInBackground(lotteryCode: String) {
        try {
            Timber.d("Background refresh for $lotteryCode")
            repository.getLatestDraw(lotteryCode)
                .catch { exception ->
                    Timber.w(exception, "Background refresh failed for $lotteryCode")
                }
                .collect { result ->
                    if (result is Result.Success) {
                        val lotteryType = LotteryType.fromCode(lotteryCode)
                        if (lotteryType != null) {
                            val jsonData = json.encodeToString(result.data)
                            cacheManager.updateCache(lotteryType, jsonData)
                            Timber.d("Background refresh completed for $lotteryCode")
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Background refresh error for $lotteryCode")
        }
    }


    private suspend fun refreshHistoryInBackground(lotteryCode: String, size: Int) {
        try {
            Timber.d("Background refresh history for $lotteryCode")
            repository.getHistory(lotteryCode, size, forceRefresh = true)
                .catch { exception ->
                    Timber.w(exception, "Background history refresh failed for $lotteryCode")
                }
                .collect { result ->
                    if (result is Result.Success) {
                        val lotteryType = LotteryType.fromCode(lotteryCode)
                        if (lotteryType != null) {
                            val jsonData = json.encodeToString(result.data)
                            cacheManager.updateCache(lotteryType, jsonData)
                            Timber.d("Background history refresh completed for $lotteryCode")
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.w(e, "Background history refresh error for $lotteryCode")
        }
    }


    suspend fun getCacheStats(): Map<LotteryType, CacheStats> {
        return cacheManager.getCacheStats()
    }


    suspend fun clearAllCache() {
        cacheManager.clearAllCache()
    }


    suspend fun clearCache(lotteryType: LotteryType) {
        cacheManager.clearCache(lotteryType)
    }
}

package com.vergil.lottery.data.repository

import com.vergil.lottery.core.constants.AppConstants
import com.vergil.lottery.core.util.Result
import com.vergil.lottery.data.local.dao.DrawHistoryDao
import com.vergil.lottery.data.local.entity.DrawHistoryEntity
import com.vergil.lottery.data.remote.LotteryApiService
import com.vergil.lottery.domain.model.DrawResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber


class LotteryRepository(
    private val apiService: LotteryApiService,
    private val drawHistoryDao: DrawHistoryDao
) {


    fun getLatestDraw(lotteryCode: String): Flow<Result<DrawResult>> = flow {
        try {
            emit(Result.Loading)


            val cached = drawHistoryDao.getLatest(lotteryCode)
            if (cached != null) {
                Timber.d("Loading from cache: ${cached.issue}")
                emit(Result.Success(cached.toDomain()))
            }


            val response = apiService.getLatestDraw(lotteryCode)
            if (response.code == 1) {
                val drawResult = response.data.toDomain()


                val entity = DrawHistoryEntity.fromDomain(drawResult)
                drawHistoryDao.insert(entity)
                Timber.d("Cached latest draw: ${drawResult.issue}")


                drawHistoryDao.deleteOldRecords(lotteryCode, AppConstants.HISTORY_DEFAULT_SIZE)

                emit(Result.Success(drawResult))
            } else {

                if (cached == null) {
                    emit(Result.Error(Exception(response.msg)))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch latest draw for $lotteryCode")

            val cached = drawHistoryDao.getLatest(lotteryCode)
            if (cached == null) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)


    fun getDrawByIssue(issue: String, lotteryCode: String): Flow<Result<DrawResult>> = flow {
        try {
            emit(Result.Loading)


            val cached = drawHistoryDao.getByIssue(lotteryCode, issue)
            if (cached != null) {
                Timber.d("Loaded from cache: ${cached.issue}")
                emit(Result.Success(cached.toDomain()))
                return@flow
            }


            val response = apiService.getDrawByIssue(issue, lotteryCode)
            if (response.code == 1) {
                val drawResult = response.data.toDomain()


                val entity = DrawHistoryEntity.fromDomain(drawResult)
                drawHistoryDao.insert(entity)
                Timber.d("Cached draw: ${drawResult.issue}")

                emit(Result.Success(drawResult))
            } else {
                emit(Result.Error(Exception(response.msg)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch draw for issue $issue")
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)


    fun getHistory(lotteryCode: String, size: Int = 100, forceRefresh: Boolean = false): Flow<Result<List<DrawResult>>> = flow {
        try {
            emit(Result.Loading)


            if (!forceRefresh) {
                val cachedList = drawHistoryDao.getHistoryByCodeOnce(lotteryCode, size)
                if (cachedList.isNotEmpty()) {
                    Timber.d("Loaded ${cachedList.size} records from cache")
                    emit(Result.Success(cachedList.map { it.toDomain() }))
                }
            }


            val response = apiService.getHistory(lotteryCode, size)
            if (response.code == 1) {
                val drawResults = response.data.map { it.toDomain() }


                val entities = drawResults.map { DrawHistoryEntity.fromDomain(it) }
                drawHistoryDao.insertAll(entities)
                Timber.d("Cached ${entities.size} records")


                drawHistoryDao.deleteOldRecords(lotteryCode, AppConstants.HISTORY_DEFAULT_SIZE)

                emit(Result.Success(drawResults))
            } else {

                val cachedList = drawHistoryDao.getHistoryByCodeOnce(lotteryCode, size)
                if (cachedList.isEmpty()) {
                    emit(Result.Error(Exception(response.msg)))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch history for $lotteryCode")

            val cachedList = drawHistoryDao.getHistoryByCodeOnce(lotteryCode, size)
            if (cachedList.isEmpty()) {
                emit(Result.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)


    fun observeHistory(lotteryCode: String, size: Int = 100): Flow<List<DrawResult>> {
        return drawHistoryDao.getHistoryByCode(lotteryCode, size)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }


    suspend fun clearCache(lotteryCode: String) {
        drawHistoryDao.clearByCode(lotteryCode)
        Timber.d("Cleared cache for $lotteryCode")
    }


    suspend fun clearAllCache() {
        drawHistoryDao.clearAll()
        Timber.d("Cleared all cache")
    }
}

